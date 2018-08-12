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

import com.eclipsesource.v8.NodeJS;
import com.eclipsesource.v8.V8;
import com.eclipsesource.v8.V8Object;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalSplitPanel;

@SuppressWarnings("serial")
public class JsEditor extends EasyAppLayout {

	private AceEditor aceEditor;
	private Label logTextArea;

	public JsEditor() {
		super();
		VerticalSplitPanel mainLayout = new VerticalSplitPanel();
		aceEditor = new AceEditor();
		aceEditor.setMode(AceMode.javascript);
		aceEditor.setTheme(AceTheme.eclipse);
		aceEditor.setSizeFull();
		mainLayout.setFirstComponent(aceEditor);

		logTextArea = new Label();
		logTextArea.setSizeFull();

		mainLayout.setSplitPosition(75, Unit.PERCENTAGE);
		//		LoggerPanel loggerPanel = new LoggerPanel();
		//		loggerPanel.setSizeFull();

		Panel panel = new Panel();
		panel.setSizeFull();
		panel.setContent(logTextArea);
		panel.getContent().setSizeUndefined();

		mainLayout.setSecondComponent(panel);

		addComponent(mainLayout);
		setSizeFull();
	}

	public void runScript() {
		//		try {
		NodeJS nodeJS = NodeJS.createNodeJS();
		//nodeJS.getRuntime().redefine
		//nodeJS.getRuntime().exec
		nodeJS.getRuntime().executeVoidScript(aceEditor.getValue());
		
//		Console console = new Console();
//		V8Object v8Console = new V8Object(v8);
//		v8.add("console", v8Console);
//		v8Console.registerJavaMethod(console, "log", "log", new Class<?>[] { String.class });
//		v8Console.registerJavaMethod(console, "err", "err", new Class<?>[] { String.class });
//		v8Console.release();
//
//		V8 runtime = V8.createV8Runtime();
//		Object result = runtime.executeScript(aceEditor.getValue());
//		logTextArea.setValue(result.toString());

		//			ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
		//			ScriptContext context = engine.getContext();
		//			StringWriter sw = new StringWriter();
		//			context.setWriter(sw);
		//			context.setErrorWriter(sw);
		//			
		//			engine.eval( aceEditor.getValue());
		//			
		//			logTextArea.setValue(sw.toString());
		//			sw.close();
		//		} catch (ScriptException | IOException e) {
		//			// TODO Auto-generated catch block
		//			logTextArea.setValue(e.getMessage());
		//			//e.printStackTrace();
		//		}

		//		ByteArrayOutputStream out = new ByteArrayOutputStream();
		//		Context jsContext = Context.newBuilder("js")
		//                .out(out)
		//                .option("js.Strict", "true")
		//                .allowAllAccess(true)
		//                .build();
		//		
		//		
		//		//Context jsContext = Context.create("js");
		//		Value value = jsContext.eval("js", aceEditor.getValue());
		//		logTextArea.setCaption(new String(out.toByteArray()));
	}


}

