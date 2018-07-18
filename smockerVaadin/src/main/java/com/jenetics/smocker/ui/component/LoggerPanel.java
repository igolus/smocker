package com.jenetics.smocker.ui.component;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

import org.apache.commons.lang3.StringUtils;

import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;

public class LoggerPanel extends VerticalLayout {
	
	private static final int DEFAULT_MAX_SIZE = 40000;
	private StringBuilder buffer = new StringBuilder();
	private int maxSize;
	private Label logTextArea;
	
	public LoggerPanel() {
		this(DEFAULT_MAX_SIZE);
	}
	
	public LoggerPanel(int maxSize) {
		super();
		this.maxSize = maxSize;
		logTextArea = new Label();
		logTextArea.setContentMode(ContentMode.HTML);
		logTextArea.setSizeFull();
		
		Panel panel = new Panel();
		panel.setSizeFull();
		panel.setContent(logTextArea);
		panel.getContent().setSizeUndefined();
		
		addComponent(panel);
	}

	public void appendMessage(Level level, String message) {
		String formattedMessage = new SimpleFormatter().format(new LogRecord(level, message));
		formattedMessage = StringUtils.replace(formattedMessage, System.lineSeparator(), "<BR/>");
		
		buffer.append(formattedMessage);
		if (buffer.length() > maxSize) {
			buffer.replace(0, buffer.length() - maxSize, "");
		}
		logTextArea.setValue(buffer.toString());
	}
	

}
