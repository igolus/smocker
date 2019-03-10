package com.jenetics.smocker.jseval;

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
import org.vaadin.easyapp.util.annotations.RootView;

import com.eclipsesource.v8.JavaCallback;
import com.eclipsesource.v8.JavaVoidCallback;
import com.eclipsesource.v8.NodeJS;
import com.eclipsesource.v8.V8;
import com.eclipsesource.v8.V8Array;
import com.eclipsesource.v8.V8Object;
import com.google.common.collect.Multiset.Entry;
import com.jenetics.smocker.dao.DaoConfig;
import com.jenetics.smocker.jseval.callBack.LoggerCallBack;
import com.jenetics.smocker.jseval.callBack.SmockerJsEnvCallBackAdd;
import com.jenetics.smocker.jseval.callBack.SmockerJsEnvCallBackGet;
import com.jenetics.smocker.jseval.callBack.SmockerJsEnvCallBackRemove;
import com.jenetics.smocker.model.CommunicationMocked;
import com.jenetics.smocker.ui.SmockerUI;
import com.jenetics.smocker.util.NetworkReaderUtility;
import com.jenetics.smocker.util.SmockerException;
import com.jenetics.smocker.util.SmockerRuntimeException;

public class JSEvaluator {

	private static Logger logger = Logger.getLogger(JSEvaluator.class);
	//private static RuntimeAndLogger runtimeAndLogger; 
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
	 */
	public static String[] runScript(String base64Input, String realInput, CommunicationMocked comm, String providedInput, 
			String providedOutput, String code, long index) throws SmockerException {
		RuntimeAndLogger runtimeAndLogger = getRuntimeAndLogger();

		String script = "var output = matchAndReturnOutput(recordDate, realInput, bas64Input,"
				+ "providedInput, providedOutput, index, targetHost, targetPort);\n";
		
		runtimeAndLogger.getRuntime().add("recordDate", comm.getDateTime().toString());
		runtimeAndLogger.getRuntime().add("realInput", realInput);
		runtimeAndLogger.getRuntime().add("index", index);
		if (base64Input == null) {
			base64Input = NetworkReaderUtility.encode(realInput);
		}
		runtimeAndLogger.getRuntime().add("bas64Input", base64Input);
		runtimeAndLogger.getRuntime().add(
				"providedInput", providedInput == null ? NetworkReaderUtility.decode(comm.getRequest()) : providedInput);
		runtimeAndLogger.getRuntime().add(
				"providedOutput", providedOutput== null ? NetworkReaderUtility.decode(comm.getResponse()) : providedOutput);
		runtimeAndLogger.getRuntime().add("targetHost", comm.getConnection().getHost());
		runtimeAndLogger.getRuntime().add("targetPort", comm.getConnection().getPort());


		if (code == null) {
			code = comm.getSourceJs();
		}
		if (code == null) {
			return null;
		}

		String globalCode = DaoConfig.getSingleConfig().getGlobalJsFunction();
		code = code + "\n" + (globalCode != null ? globalCode : "");

		String output;

		try {
			runtimeAndLogger.reset();
			runtimeAndLogger.getRuntime().executeVoidScript(script + code);
			output = runtimeAndLogger.getRuntime().getString("output");
		}
		catch (Exception ex) {
			SmockerUI.log(Level.SEVERE, "Error evaluating script", ex);
			if (ex instanceof SmockerRuntimeException) {
				throw new SmockerException( ((SmockerRuntimeException)ex).getCause() );
			}
			throw new SmockerException(ex);
		}
		
		//runtimeAndLogger.getRuntime().release(false);
		log(runtimeAndLogger.getCallBack());
		return new String[] {runtimeAndLogger.getCallBack().toString(), output};
	}
	
	

	private static void registerAnnotedFunction(V8 runtime) {
		String scannedPackages = Optional.ofNullable(
				System.getProperty("smockerFunctionPackages")).orElse("");
		String[] packagesArray = scannedPackages.split(",");
		List<String> listPackageToScan = new ArrayList<>(Arrays.asList(packagesArray));
		//add Internal smocker Package
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
						if (method.isAnnotationPresent(SmockerMethod.class)) {
							if (Modifier.isStatic(method.getModifiers())) {
								if (method.getReturnType() == null) {
									mapMethodNameJavaVoidCallBack.put(method.getName(), 
											getJavaVoidCallBack(classWithFunction, method, runtime));
								}
								else {
									mapMethodNameJavaCallBack.put(method.getName(), 
											getJavaCallBack(classWithFunction, method, runtime));
								}
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
	
	private static JavaCallback getJavaCallBack(Class<?> classWithFunction, Method method, V8 runtime) {
		JavaCallback callBack = new JavaCallback() {
			@Override
			public Object invoke(V8Object receiver, V8Array parameters) {
				try {
					return invokeAndReturn(method, parameters);
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					throw new SmockerRuntimeException(e);
				}
			}
		};
		return callBack;
	}
	
	private static JavaVoidCallback getJavaVoidCallBack(Class<?> classWithFunction, Method method, V8 runtime) {
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

//	private static void referenceMethodInRuntime(Class<?> classWithFunction, Method method, V8 runtime) 
//	{
//		//void method
//		if (method.getReturnType() == null) {
//			JavaVoidCallback callBack = new JavaVoidCallback() {
//				@Override	
//				public void invoke(V8Object receiver, V8Array parameters) {
//					try {
//						invokeAndReturn(method, parameters);
//					} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
//						throw new SmockerRuntimeException(e);
//					}
//				}
//			};
//			runtime.registerJavaMethod(callBack, method.getName());
//		}
//		else {
//			JavaCallback callBack = new JavaCallback() {
//
//				@Override
//				public Object invoke(V8Object receiver, V8Array parameters) {
//					try {
//						return invokeAndReturn(method, parameters);
//					} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
//						throw new SmockerRuntimeException(e);
//					}
//				}
//			};
//			runtime.registerJavaMethod(callBack, method.getName());
//		}
//	}

	private static Object invokeAndReturn(Method method, V8Array parameters) 
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
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
			NodeJS nodeJS = NodeJS.createNodeJS();
			V8 runtime =nodeJS.getRuntime();

			LoggerCallBack logger = new LoggerCallBack();
			runtime.registerJavaMethod(logger, "smockerLog");
			
			registerAnnotedFunction(runtime);
			return new RuntimeAndLogger(runtime, logger);
	}

	public static boolean filter(String functionName, String input) throws SmockerException {
		String code = DaoConfig.getSingleConfig().getFilterJsFunction();
		String globalCode = DaoConfig.getSingleConfig().getGlobalJsFunction();
		code = code + "\n" + globalCode;

		RuntimeAndLogger runtimeAndLogger = getRuntimeAndLogger();
		String script = "var match = " + functionName +  "(realInput);\n";
		runtimeAndLogger.getRuntime().add("realInput", input);
		Boolean output = false;
		try {
			runtimeAndLogger.getRuntime().executeVoidScript(script + code);
			output = runtimeAndLogger.getRuntime().getBoolean("match");
		}
		catch (Exception ex) {
			SmockerUI.log(Level.SEVERE, "Error evaluating script", ex);
			throw new SmockerException(ex);
		}
		log(runtimeAndLogger.getCallBack());
		return output;
	}


	public static String formatAndDisplay(String functionName, String input) 
			throws SmockerException {
		String code = DaoConfig.getSingleConfig().getFormatDisplayJsFunction();
		String globalCode = DaoConfig.getSingleConfig().getGlobalJsFunction();
		if (globalCode != null) {
			code = code + "\n" + globalCode;
		}
		if (code == null) {
			code = "";
		}

		RuntimeAndLogger runtimeAndLogger = getRuntimeAndLogger();

		String script = "var ret = " + functionName +  "(realInput);\n";
		runtimeAndLogger.getRuntime().add("realInput", input);
		String output = null;
		try {
			runtimeAndLogger.getRuntime().executeVoidScript(script + code);
			output = runtimeAndLogger.getRuntime().getString("ret");
		}
		catch (Exception ex) {
			SmockerUI.log(Level.SEVERE, "Error evaluating script", ex);
			throw new SmockerException(ex);
		}
		output = escapeJsStringResult(output);
		log(runtimeAndLogger.getCallBack());
		return output;
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
