package com.jenetics.smocker.jseval;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.jboss.logging.Logger;
import org.reflections.Reflections;

import com.eclipsesource.v8.JavaCallback;
import com.eclipsesource.v8.JavaVoidCallback;
import com.eclipsesource.v8.V8;
import com.eclipsesource.v8.V8Array;
import com.eclipsesource.v8.V8Object;
import com.google.common.io.CharStreams;
import com.jenetics.smocker.dao.DaoConfig;
import com.jenetics.smocker.jseval.callBack.LoggerCallBack;
import com.jenetics.smocker.model.CommunicationMocked;
import com.jenetics.smocker.ui.SmockerUI;
import com.jenetics.smocker.util.NetworkReaderUtility;
import com.jenetics.smocker.util.SmockerException;
import com.jenetics.smocker.util.SmockerRuntimeException;

public class JSEvaluator {

	private static final String TARGET_HOST = "targetHost";
	private static final String REAL_INPUT = "realInput";

	private JSEvaluator() {
		super();
	}

	private static final String ERROR_EVALUATING_SCRIPT = "Error evaluating script";
	private static Logger logger = Logger.getLogger(JSEvaluator.class);
	private static Map<String, JavaCallback> mapMethodNameJavaCallBack = null;
	private static Map<String, JavaVoidCallback> mapMethodNameJavaVoidCallBack = null;

	/**
	 * 
	 * @param input
	 * @param comm
	 * @param providedInput if null take the info from comm
	 * @param providedOutput if null take the info from comm
	 * @param code if null take the info from comm
	 * @return
	 * @throws IOException 
	 */
	public static String[] runScript(String base64Input, String realInput, CommunicationMocked comm, String providedInput, 
			String providedOutput, String code, long index) throws SmockerException, IOException {
		RuntimeAndLogger runtimeAndLogger = getRuntimeAndLogger();

		String script = "var output = matchAndReturnOutput(recordDate, realInput, bas64Input,"
				+ "providedInput, providedOutput, index, targetHost, targetPort);\n";

		runtimeAndLogger.getRuntime().add("recordDate", comm.getDateTime().toString());
		runtimeAndLogger.getRuntime().add(REAL_INPUT, realInput);
		runtimeAndLogger.getRuntime().add("index", index);
		if (base64Input == null) {
			base64Input = NetworkReaderUtility.encode(realInput);
		}
		runtimeAndLogger.getRuntime().add("bas64Input", base64Input);
		runtimeAndLogger.getRuntime().add(
				"providedInput", providedInput == null ? NetworkReaderUtility.decode(comm.getRequest()) : providedInput);
		runtimeAndLogger.getRuntime().add(
				"providedOutput", providedOutput== null ? NetworkReaderUtility.decode(comm.getResponse()) : providedOutput);
		runtimeAndLogger.getRuntime().add(TARGET_HOST, comm.getConnection().getHost());
		runtimeAndLogger.getRuntime().add("targetPort", comm.getConnection().getPort());


		if (code == null) {
			code = comm.getSourceJs();
		}
		if (code == null) {
			return new String[0];
		}

		InputStream inputStream = JSEvaluator.class
				.getClassLoader().getResourceAsStream("function.js");
		String source = null;
		try (final Reader reader = new InputStreamReader(inputStream)) {
			source = CharStreams.toString(reader);
		}

		//String embeddedCode = 
		String globalCode = DaoConfig.getSingleConfig().getGlobalJsFunction();
		code = code + "\n" + (globalCode != null ? globalCode : "");
		code = code + "\n" + (source != null ? source : "");

		String output;

		try {
			runtimeAndLogger.reset();
			registerAnnotedFunction(runtimeAndLogger.getRuntime());
			runtimeAndLogger.getRuntime().executeVoidScript(script + code);
			output = runtimeAndLogger.getRuntime().getString("output");
		}
		catch (SmockerRuntimeException smockerRuntimeException) {
			throw new SmockerException( smockerRuntimeException.getCause() );
		}
		catch (Exception ex) {
			SmockerUI.log(Level.SEVERE, ERROR_EVALUATING_SCRIPT, ex);
			throw new SmockerException(ex);
		}

		log(runtimeAndLogger.getCallBack());
		return new String[] {runtimeAndLogger.getCallBack().toString(), output};
	}



	private static void registerAnnotedFunction(V8 runtime) {
		String scannedPackages = Optional.ofNullable(
				System.getProperty("smockerFunctionPackages")).orElse("");
		String[] packagesArray = scannedPackages.split(",");
		List<String> listPackageToScan = new ArrayList<>(Arrays.asList(packagesArray));
		listPackageToScan.add("com.jenetics.smocker.jseval.SmockerFunction");

		//us map to avoid performance issues
		if (mapMethodNameJavaCallBack == null) {
			mapMethodNameJavaCallBack = new HashMap<>();
			mapMethodNameJavaVoidCallBack = new HashMap<>();

			for (String packageToScan : listPackageToScan) {
				Reflections reflections = new Reflections(packageToScan);
				Set<Class<?>> annotatedRootView = reflections.getTypesAnnotatedWith(SmockerFunctionClass.class);

				for (Class<?> classWithFunction : annotatedRootView) {
					Method[] methodsInFUnctionClass = classWithFunction.getMethods();
					for (Method method : methodsInFUnctionClass) {
						if (method.isAnnotationPresent(SmockerMethod.class) && Modifier.isStatic(method.getModifiers())) {
							if (method.getReturnType() == null) {
								mapMethodNameJavaVoidCallBack.put(method.getName(), 
										getJavaVoidCallBack(method));
							}
							else {
								mapMethodNameJavaCallBack.put(method.getName(), 
										getJavaCallBack(method));
							}
						}
					}
				}
			}
		}

		for (Map.Entry<String, JavaVoidCallback> entry : mapMethodNameJavaVoidCallBack.entrySet()) {
			runtime.registerJavaMethod(entry.getValue(), entry.getKey());
		}
		for (Map.Entry<String, JavaCallback> entry : mapMethodNameJavaCallBack.entrySet()) {
			runtime.registerJavaMethod(entry.getValue(), entry.getKey());
		}
	}

	private static JavaCallback getJavaCallBack(Method method) {

		JavaCallback callBack = (V8Object receiver, V8Array parameters) -> {
			try {
				return invokeAndReturn(method, parameters);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				throw new SmockerRuntimeException(e);
			}
		};

		return callBack;
	}

	private static JavaVoidCallback getJavaVoidCallBack(Method method) {
		JavaVoidCallback callBack = new JavaVoidCallback() {
			@Override	
			public void invoke(V8Object receiver, V8Array parameters) {
				try {
					invokeAndReturn(method, parameters);
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					throw new SmockerRuntimeException(e);
				}
			}
		};
		return callBack;
	}

	private static Object invokeAndReturn(Method method, V8Array parameters) 
			throws IllegalAccessException, InvocationTargetException {
		List<Object> listArgs = new ArrayList<>();
		for (int i = 0; i < parameters.length(); i++) {
			listArgs.add(parameters.get(i));
		}
		return method.invoke(null, listArgs.toArray());
	}

	private static void log(LoggerCallBack callBack) {
		SmockerUI.log(Level.INFO, callBack.toString());
	}

	public static RuntimeAndLogger getRuntimeAndLogger() {
		V8 runtime = V8.createV8Runtime();

		LoggerCallBack logger = new LoggerCallBack();
		runtime.registerJavaMethod(logger, "smockerLog");

		registerAnnotedFunction(runtime);
		return new RuntimeAndLogger(runtime, logger);
	}

	public static boolean filter(String functionName, String input, String output) throws SmockerException {
		String code = DaoConfig.getSingleConfig().getFilterJsFunction();
		String globalCode = DaoConfig.getSingleConfig().getGlobalJsFunction();
		code = code + "\n" + globalCode;

		RuntimeAndLogger runtimeAndLogger = getRuntimeAndLogger();
		String script = "var match = " + functionName +  "(realInput, realOutput);\n";
		runtimeAndLogger.getRuntime().add(REAL_INPUT, input);
		runtimeAndLogger.getRuntime().add("realOutput", output);
		Boolean result = false;
		try {
			runtimeAndLogger.getRuntime().executeVoidScript(script + code);
			result = runtimeAndLogger.getRuntime().getBoolean("match");
		}
		catch (Exception ex) {
			SmockerUI.log(Level.SEVERE, ERROR_EVALUATING_SCRIPT, ex);
			throw new SmockerException(ex);
		}
		log(runtimeAndLogger.getCallBack());
		return result;
	}

	public static void trace(String functionName, String input, String output) 
			throws SmockerException {
		String code = DaoConfig.getSingleConfig().getTraceFunctionJsFunction();
		code = appendGlobalCode(code);

		RuntimeAndLogger runtimeAndLogger = getRuntimeAndLogger();

		String script =  functionName +  "(realInput, realOutput);\n";
		runtimeAndLogger.getRuntime().add(REAL_INPUT, input);
		runtimeAndLogger.getRuntime().add("realOutput", output);

		try {
			runtimeAndLogger.getRuntime().executeVoidScript(script + code);
		}
		catch (Exception ex) {
			SmockerUI.log(Level.SEVERE, ERROR_EVALUATING_SCRIPT, ex);
			throw new SmockerException(ex);
		}
	}
	
	public static String commName(String functionName, String input) 
			throws SmockerException {
		String code = DaoConfig.getSingleConfig().getFormatDisplayJsFunction();
		code = appendGlobalCode(code);

		RuntimeAndLogger runtimeAndLogger = getRuntimeAndLogger();

		String script =  functionName +  "(realInput);\n";
		runtimeAndLogger.getRuntime().add(REAL_INPUT, input);
		
		try {
			Object retV8 = runtimeAndLogger.getRuntime().executeScript(script + code);
			if (retV8 instanceof String) {
				return (String) retV8;
			}
			return null;
		}
		catch (Exception ex) {
			SmockerUI.log(Level.SEVERE, ERROR_EVALUATING_SCRIPT, ex);
			throw new SmockerException(ex);
		}
	}

	public static String formatAndDisplay(String functionName, String input) 
			throws SmockerException {
		String code = DaoConfig.getSingleConfig().getFormatDisplayJsFunction();
		code = appendGlobalCode(code);

		RuntimeAndLogger runtimeAndLogger = getRuntimeAndLogger();

		String script = "var ret = " + functionName +  "(realInput);\n";
		runtimeAndLogger.getRuntime().add(REAL_INPUT, input);
		String output = null;
		try {
			runtimeAndLogger.getRuntime().executeVoidScript(script + code);
			output = runtimeAndLogger.getRuntime().getString("ret");
		}
		catch (Exception ex) {
			SmockerUI.log(Level.SEVERE, ERROR_EVALUATING_SCRIPT, ex);
			throw new SmockerException(ex);
		}
		output = escapeJsStringResult(output);
		log(runtimeAndLogger.getCallBack());
		return output;
	}



	private static String appendGlobalCode(String code) {
		String globalCode = DaoConfig.getSingleConfig().getGlobalJsFunction();
		if (globalCode != null) {
			code = code + "\n" + globalCode;
		}
		if (code == null) {
			code = "";
		}
		return code;
	}

	private static String escapeJsStringResult(String output) {
		try {
			output = StringEscapeUtils.unescapeJava(output);
			if (!StringUtils.isEmpty(output) && 
					output.charAt(0) == '\"' && output.charAt(output.length() - 1) == '\"') {
				output = output.substring(1, output.length() - 1);
			}
		}
		catch (Exception e) {
			logger.error("Unable to escape output", e);
		}
		return output;
	}

	private static class RuntimeAndLogger {
		private V8 runtime;
		private LoggerCallBack callBack;

		private RuntimeAndLogger(V8 runtime, LoggerCallBack callBack) {
			super();
			this.runtime = runtime;
			this.callBack = callBack;
		}

		public V8 getRuntime() {
			return runtime;
		}

		public LoggerCallBack getCallBack() {
			return callBack;
		}

		public void reset() {
			callBack.reset();
		}
	}

}
