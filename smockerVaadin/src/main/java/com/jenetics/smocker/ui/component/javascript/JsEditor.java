package com.jenetics.smocker.ui.component.javascript;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.vaadin.aceeditor.AceEditor;
import org.vaadin.aceeditor.AceMode;
import org.vaadin.aceeditor.AceTheme;
import org.vaadin.easyapp.util.EasyAppLayout;

import com.eclipsesource.v8.JavaCallback;
import com.eclipsesource.v8.JavaVoidCallback;
import com.eclipsesource.v8.NodeJS;
import com.eclipsesource.v8.V8;
import com.eclipsesource.v8.V8Array;
import com.eclipsesource.v8.V8Object;
import com.jenetics.smocker.model.CommunicationMocked;
import com.jenetics.smocker.ui.component.TextPanel;
import com.jenetics.smocker.util.SmockerUtility;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.VerticalSplitPanel;

@SuppressWarnings("serial")
public class JsEditor extends EasyAppLayout {

	private AceEditor aceEditor;
	private TextPanel logTextArea;
	private TextPanel selectedRequestPane;
	private TextPanel selectedResponsePane;

	public JsEditor(CommunicationMocked communicationMocked, TextPanel selectedRequestPane, TextPanel selectedResponsePane) {
		super();
		//this.communicationMocked = communicationMocked;
		this.selectedRequestPane = selectedRequestPane;
		this.selectedResponsePane = selectedResponsePane;
		
		VerticalSplitPanel mainLayout = new VerticalSplitPanel();
		aceEditor = new AceEditor();
		aceEditor.setMode(AceMode.javascript);
		aceEditor.setTheme(AceTheme.eclipse);
		aceEditor.setSizeFull();
		mainLayout.setFirstComponent(aceEditor);

		logTextArea = new TextPanel(false);
		logTextArea.setSizeFull();

		mainLayout.setSplitPosition(75, Unit.PERCENTAGE);
		mainLayout.setSecondComponent(logTextArea);

		addComponent(mainLayout);
		setSizeFull();
	}

	public void runScript() {
		NodeJS nodeJS = NodeJS.createNodeJS();
		V8 runtime =nodeJS.getRuntime();

		Logger logger = new Logger();
		runtime.registerJavaMethod(logger, "consolelog");
		
		
		String script = "var match = matchAndReturnOutput(realInput, providedInput, providedOutput);\n";
		
		runtime.add("realInput", "Real input");
		runtime.add("providedInput", selectedRequestPane.getText());
		runtime.add("providedOutput", selectedResponsePane.getText());
		
		try {
			runtime.executeVoidScript(script + aceEditor.getValue());
			String match = runtime.getString("match");
		}
		catch (Exception ex) {
			logTextArea.setText(SmockerUtility.getStackTrace(ex));
			return;
		}
		runtime.release(false);
		logTextArea.setText(logger.toString());
	}


}

