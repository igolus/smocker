package com.jenetics.smocker.jseval;

import java.util.logging.Level;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.jboss.logging.Logger;

import com.eclipsesource.v8.JavaCallback;
import com.eclipsesource.v8.NodeJS;
import com.eclipsesource.v8.V8;
import com.jenetics.smocker.dao.DaoConfig;
import com.jenetics.smocker.jseval.callBack.LoggerCallBack;
import com.jenetics.smocker.jseval.callBack.SmockerJsEnvCallBackAdd;
import com.jenetics.smocker.jseval.callBack.SmockerJsEnvCallBackGet;
import com.jenetics.smocker.jseval.callBack.SmockerJsEnvCallBackRemove;
import com.jenetics.smocker.model.CommunicationMocked;
import com.jenetics.smocker.ui.SmockerUI;
import com.jenetics.smocker.util.NetworkReaderUtility;
import com.jenetics.smocker.util.SmockerException;

public class JSEvaluator {
	
	private static Logger logger = Logger.getLogger(JSEvaluator.class); 
	
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
		//NodeJS nodeJS = NodeJS.createNodeJS();
		RuntimeAndLogger runtimeAndLogger = getRuntimeAndLogger();
		
		SmockerJsEnvCallBackAdd smockerJsEnvCallBackAdd = new SmockerJsEnvCallBackAdd();
		runtimeAndLogger.getRuntime().registerJavaMethod(smockerJsEnvCallBackAdd, "smockerAddToEnv");
		
		SmockerJsEnvCallBackGet smockerJsEnvCallBackGet = new SmockerJsEnvCallBackGet();
		runtimeAndLogger.getRuntime().registerJavaMethod(smockerJsEnvCallBackGet, "smockerGetFromEnv");
		
		SmockerJsEnvCallBackRemove smockerJsEnvCallBackRemove = new SmockerJsEnvCallBackRemove();
		runtimeAndLogger.getRuntime().registerJavaMethod(smockerJsEnvCallBackRemove, "smockerRemFromEnv");
		
		String script = "var output = matchAndReturnOutput(recordDate, realInput, bas64Input,"
				+ "providedInput, providedOutput, index);\n";
		
		runtimeAndLogger.getRuntime().add("recordDate", comm.getDateTime().toString());
		runtimeAndLogger.getRuntime().add("providedInput", providedInput == null ? NetworkReaderUtility.decode(comm.getRequest()) : providedInput);
		runtimeAndLogger.getRuntime().add("providedOutput", providedOutput== null ? NetworkReaderUtility.decode(comm.getResponse()) : providedOutput);
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
			runtimeAndLogger.getRuntime().executeVoidScript(script + code);
			output = runtimeAndLogger.getRuntime().getString("output");
		}
		catch (Exception ex) {
			SmockerUI.log(Level.SEVERE, "Error evaluating script", ex);
			throw new SmockerException(ex);
		}
		runtimeAndLogger.getRuntime().release(false);
		log(runtimeAndLogger.getCallBack());
		return new String[] {runtimeAndLogger.getCallBack().toString(), output};
	}
	
	private static void log(LoggerCallBack callBack) {
		SmockerUI.log(Level.INFO, callBack.toString());
	}

	public static RuntimeAndLogger getRuntimeAndLogger() {
		NodeJS nodeJS = NodeJS.createNodeJS();
		V8 runtime =nodeJS.getRuntime();

		LoggerCallBack logger = new LoggerCallBack();
		runtime.registerJavaMethod(logger, "smockerLog");
		
		runtime.registerJavaMethod((JavaCallback) (receiver, args) -> {
		    return NetworkReaderUtility.encode(args.get(0).toString());
		  }, "btoa");
		runtime.registerJavaMethod((JavaCallback) (receiver, args) -> {
			return NetworkReaderUtility.decode(args.get(0).toString());
		  }, "atob");
		
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
	}
	
}
