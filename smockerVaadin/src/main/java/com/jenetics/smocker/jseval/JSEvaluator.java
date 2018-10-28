package com.jenetics.smocker.jseval;

import java.util.logging.Level;

import com.eclipsesource.v8.NodeJS;
import com.eclipsesource.v8.V8;
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
		
		String script = "var output = matchAndReturnOutput(recordDate, realInput, providedInput, providedOutput);\n";
		
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
	
}
