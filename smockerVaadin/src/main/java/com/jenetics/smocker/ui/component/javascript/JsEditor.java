package com.jenetics.smocker.ui.component.javascript;

import java.util.Date;

import org.vaadin.aceeditor.AceEditor;
import org.vaadin.aceeditor.AceMode;
import org.vaadin.aceeditor.AceTheme;
import org.vaadin.easyapp.util.EasyAppLayout;

import com.eclipsesource.v8.NodeJS;
import com.eclipsesource.v8.V8;
import com.jenetics.smocker.jseval.JSEvaluator;
import com.jenetics.smocker.model.CommunicationMocked;
import com.jenetics.smocker.ui.component.TextPanel;
import com.jenetics.smocker.util.SmockerException;
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
		//aceEditor.au
		addComponent(aceEditor);
		setSizeFull();
	}

	private void setDefaultCodeJsEditor(AceEditor aceEditor) {
		aceEditor.setValue(DEFAULT_JS);
	}

	public String[] runScript(String input, CommunicationMocked comm) {
		String[] output = null;
		try {
			output = JSEvaluator.runScript(input, comm, selectedRequestPane.getText(), selectedResponsePane.getText(), aceEditor.getValue());
		}
		catch (SmockerException smockerEx) {
			return new String[] {SmockerUtility.getStackTrace(smockerEx.getCause()), null};
		}
		return output;
	}
	
	public String getJSSource() {
		return aceEditor.getValue();
	}
	
	public void setJSSource(String source) {
		aceEditor.setValue(source);
	}


}

