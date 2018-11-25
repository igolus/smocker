package com.jenetics.smocker.jseval;

import java.util.logging.Level;

import org.apache.commons.lang3.StringUtils;

import com.eclipsesource.v8.NodeJS;
import com.eclipsesource.v8.V8;
import com.eclipsesource.v8.V8Object;
import com.jenetics.smocker.dao.DaoConfig;
import com.jenetics.smocker.model.CommunicationMocked;
import com.jenetics.smocker.ui.SmockerUI;
import com.jenetics.smocker.ui.component.javascript.Logger;
import com.jenetics.smocker.util.NetworkReaderUtility;
import com.jenetics.smocker.util.SmockerException;

public class JSEvaluator {
	
	/**
	 * 
	 * @param input
	 * @param comm
	 * @param providedInput if null take the info from comm
	 * @param providedOutput if null take the info from comm
	 * @param code if null take the info from comm
	 * @return
	 */
	public static String[] runScript(String input, CommunicationMocked comm, String providedInput, 
			String providedOutput, String code) throws SmockerException {
		NodeJS nodeJS = NodeJS.createNodeJS();
		V8 runtime =nodeJS.getRuntime();

		Logger logger = new Logger();
		runtime.registerJavaMethod(logger, "consolelog");
		
		String script = "var output = matchAndReturnOutput(recordDate, realInput, "
				+ "providedInput, providedOutput);\n";
		
		runtime.add("recordDate", comm.getDateTime().toString());
		runtime.add("realInput", input);
		runtime.add("providedInput", providedInput == null ? NetworkReaderUtility.decode(comm.getRequest()) : providedInput);
		runtime.add("providedOutput", providedOutput== null ? NetworkReaderUtility.decode(comm.getResponse()) : providedOutput);
		
		if (code == null) {
			code = comm.getSourceJs();
		}
		String output;
		
		try {
			runtime.executeVoidScript(script + code);
			output = runtime.getString("output");
		}
		catch (Exception ex) {
			SmockerUI.log(Level.SEVERE, "Error evaluating script", ex);
			throw new SmockerException(ex);
		}
		runtime.release(false);
		return new String[] {logger.toString(), output};
	}
	
	public static boolean filter(String functionName, String input) throws SmockerException {
		String code = DaoConfig.getSingleConfig().getFilterJsFunction();
		String globalCode = DaoConfig.getSingleConfig().getGlobalJsFunction();
		code = code + "\n" + globalCode;
		
		NodeJS nodeJS = NodeJS.createNodeJS();
		V8 runtime =nodeJS.getRuntime();
		String script = "var match = " + functionName +  "(realInput);\n";
		runtime.add("realInput", input);
		Boolean output = false;
		try {
			runtime.executeVoidScript(script + code);
			output = runtime.getBoolean("match");
		}
		catch (Exception ex) {
			SmockerUI.log(Level.SEVERE, "Error evaluating script", ex);
			throw new SmockerException(ex);
		}
		return output;
	}
	
	
	public static String formatAndDisplay(String functionName, String input) 
			throws SmockerException {
		String code = DaoConfig.getSingleConfig().getFormatDisplayJsFunction();
		String globalCode = DaoConfig.getSingleConfig().getGlobalJsFunction();
		code = code + "\n" + globalCode;
		
		NodeJS nodeJS = NodeJS.createNodeJS();
		V8 runtime =nodeJS.getRuntime();
		V8Object json = runtime.getObject("JSON");
		
		
		String script = "var ret = " + functionName +  "(realInput);\n";
		runtime.add("realInput", input);
		String output = null;
		try {
			runtime.executeVoidScript(script + code);
			output = runtime.getString("ret");
			//StringUtils.replace("\"\"", searchString, replacement)(output, 1, output.length());
			
		}
		catch (Exception ex) {
			SmockerUI.log(Level.SEVERE, "Error evaluating script", ex);
			throw new SmockerException(ex);
		}
		return output;
	}
	
}
