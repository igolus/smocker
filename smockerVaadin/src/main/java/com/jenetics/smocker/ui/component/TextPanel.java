package com.jenetics.smocker.ui.component;


import com.vaadin.ui.TextArea;
import com.vaadin.ui.VerticalLayout;

public class TextPanel extends VerticalLayout {

	private TextArea logTextArea;
	
	public TextPanel(boolean readonly) {
		this("", readonly);
	}

	public TextPanel(String text, boolean readonly) {
		super();
		logTextArea = new TextArea();
		logTextArea.setSizeFull();
		logTextArea.setWordWrap(false);
		
		logTextArea.setValue(text);
		logTextArea.setReadOnly(readonly);
		
		VerticalLayout panel = new VerticalLayout();
		panel.addComponent(logTextArea);
		panel.setSizeFull();
		
		addComponent(panel);
		setSizeFull();
	}
	
	public void setText(String text) {
		logTextArea.setValue(text);
	}
	
	public String getText() {
		return logTextArea.getValue();
	}
	
}
