package com.jenetics.smocker.ui.component;


import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.VerticalLayout;

public class TextPanel extends VerticalLayout {

	private TextArea logTextArea;

	public TextPanel(String text) {
		super();
		logTextArea = new TextArea();
		//logTextArea.setContentMode(ContentMode.TEXT);
		logTextArea.setSizeFull();
		
		logTextArea.setValue(text);
		
		VerticalLayout panel = new VerticalLayout();
		panel.addComponent(logTextArea);
		panel.setSizeFull();
		
		//panel.setContent(logTextArea);
		//panel.getContent().setSizeUndefined();
		
		addComponent(panel);
		setSizeFull();
	}
	
	public void setText(String text) {
		logTextArea.setValue(text);
	}
	
}
