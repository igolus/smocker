package com.jenetics.smocker.ui.component.javascript;

import java.util.Date;

import org.vaadin.aceeditor.AceEditor;
import org.vaadin.aceeditor.AceMode;
import org.vaadin.aceeditor.AceTheme;
import org.vaadin.easyapp.util.EasyAppLayout;

import com.eclipsesource.v8.NodeJS;
import com.eclipsesource.v8.V8;
import com.jenetics.smocker.model.CommunicationMocked;
import com.jenetics.smocker.ui.component.TextPanel;
import com.jenetics.smocker.util.SmockerUtility;

@SuppressWarnings("serial")
public class JsEditor extends EasyAppLayout {

	private static final String DEFAULT_JS = "function matchAndReturnOutput(recordDate, realInput, providedInput, providedOutput)" + System.lineSeparator()
		+ "{" + System.lineSeparator()
		+ "  if (realInput == providedInput)" + System.lineSeparator()
		+ "  {" + System.lineSeparator()
		+ "    return providedOutput;" + System.lineSeparator()
		+ "  }" + System.lineSeparator()
		+ "  return null;" + System.lineSeparator()
		+ "}";

	private AceEditor aceEditor;

	private TextPanel selectedRequestPane;
	private TextPanel selectedResponsePane;

	public JsEditor(CommunicationMocked communicationMocked, TextPanel selectedRequestPane, TextPanel selectedResponsePane) {
		super();
		this.selectedRequestPane = selectedRequestPane;
		this.selectedResponsePane = selectedResponsePane;
		
		aceEditor = new AceEditor();
		setDefaultCodeJsEditor(aceEditor);
		aceEditor.setMode(AceMode.javascript);
		aceEditor.setTheme(AceTheme.eclipse);
		aceEditor.setSizeFull();
		addComponent(aceEditor);
		setSizeFull();
	}

	private void setDefaultCodeJsEditor(AceEditor aceEditor) {
		aceEditor.setValue(DEFAULT_JS);
	}

	public String[] runScript(String input, Date recordDate) {
		NodeJS nodeJS = NodeJS.createNodeJS();
		V8 runtime =nodeJS.getRuntime();

		Logger logger = new Logger();
		runtime.registerJavaMethod(logger, "consolelog");
		
		String script = "var output = matchAndReturnOutput(recordDate, realInput, providedInput, providedOutput);\n";
		
		runtime.add("recordDate", recordDate.toString());
		runtime.add("realInput", input);
		runtime.add("providedInput", selectedRequestPane.getText());
		runtime.add("providedOutput", selectedResponsePane.getText());
		String output;
		
		try {
			runtime.executeVoidScript(script + aceEditor.getValue());
			output = runtime.getString("output");
		}
		catch (Exception ex) {
			return new String[] {SmockerUtility.getStackTrace(ex), null};
		}
		runtime.release(false);
		return new String[] {logger.toString(), output};
	}
	
	public String getJSSource() {
		return aceEditor.getValue();
	}
	
	public void setJSSource(String source) {
		aceEditor.setValue(source);
	}


}

