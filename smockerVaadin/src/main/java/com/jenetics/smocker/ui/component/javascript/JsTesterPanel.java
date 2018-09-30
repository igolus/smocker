package com.jenetics.smocker.ui.component.javascript;

import java.util.Date;

import org.vaadin.easyapp.util.ActionContainer;
import org.vaadin.easyapp.util.ActionContainerBuilder;
import org.vaadin.easyapp.util.EasyAppLayout;
import org.vaadin.easyapp.util.ActionContainer.InsertPosition;
import org.vaadin.easyapp.util.ActionContainer.Position;

import com.jenetics.smocker.model.CommunicationMocked;
import com.jenetics.smocker.ui.SmockerUI;
import com.jenetics.smocker.ui.component.TextPanel;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.ui.VerticalSplitPanel;
import com.vaadin.ui.Button.ClickEvent;

public class JsTesterPanel extends EasyAppLayout {

	private static final String LOG_KEYWORD = "===== LOG :";
	private static final String OUTPUT_KEYWORD = "===== OUTPUT :";
	private JsEditor jsEditor;
	private String[] result;
	private TextPanel textPanelInput;
	private TextPanel textPanelOutput;
	private CommunicationMocked comm;

	public JsTesterPanel(String sourceInput, JsEditor jsEditor, CommunicationMocked comm) {
		this.jsEditor = jsEditor;
		this.comm = comm;
		VerticalSplitPanel mainLayout = new VerticalSplitPanel();
		textPanelInput = new TextPanel(false);
		textPanelInput.setText(sourceInput);
		textPanelInput.setSizeFull();
		mainLayout.setFirstComponent(textPanelInput);
		
		textPanelOutput = new TextPanel(true);
		textPanelOutput.setSizeFull();
		mainLayout.setSecondComponent(textPanelOutput);
		
		mainLayout.setSizeFull();
		
		addComponent(mainLayout);
	}
	
	@Override
	public ActionContainer buildActionContainer() {
		ActionContainerBuilder builder = new ActionContainerBuilder(SmockerUI.BUNDLE_NAME)
				.addButton("Play_Button", VaadinIcons.PLAY, null,  () -> true			
						, this::test, Position.LEFT, InsertPosition.AFTER);
		return builder.build();
	}
	
	public void test(ClickEvent event) {
		result = jsEditor.runScript(textPanelInput.getText(), comm);
		String logs = result[0];
		String outputResult = result[1];
		
		StringBuffer buff = new StringBuffer();
		if (logs != null) {
			buff.append(LOG_KEYWORD).append(System.lineSeparator());
			buff.append(logs);
		}
		if (outputResult != null) {
			buff.append(OUTPUT_KEYWORD).append(System.lineSeparator());
			buff.append(outputResult);
		}
		textPanelOutput.setText(buff.toString());
	}

	public String getSourceInput() {
		return textPanelInput.getText();
	}
	
	public void setSourceInput(String textInput) {
		textPanelInput.setText(textInput);
	}
}
