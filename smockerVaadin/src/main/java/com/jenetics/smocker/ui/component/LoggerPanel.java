package com.jenetics.smocker.ui.component;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import com.jenetics.smocker.threading.ExecutorBean;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;

public class LoggerPanel extends VerticalLayout {

	private static final String BR = "<BR/>";
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

	public void asyncAppendMessage(Level level, String message) {
		Message source =  new Message(level, message);
		ExecutorBean.executeAsync(source, this::appendMessage);
	}

	public void appendMessage(Message message) {
		appendMessage(message.getLevel(), message.getMessage());
	}

	public void appendMessage(Level level, String message) {
		LogRecord record = new LogRecord(level, message);
		record.setLoggerName("Smocker");
		String formattedMessage = new SimpleFormatter().format(record);

		if (level == Level.SEVERE) {
			formattedMessage = "<font color=\"red\">" + formattedMessage + "</font>";
		}
		if (level == Level.INFO) {
			formattedMessage = "<font color=\"green\">" + formattedMessage + "</font>";
		}
		if (level == Level.WARNING) {
			formattedMessage = "<font color=\"yellow\">" + formattedMessage + "</font>";
		}
		formattedMessage = StringUtils.replace(formattedMessage, System.lineSeparator(), BR);

		buffer.append(formattedMessage);
		if (buffer.length() > maxSize) {
			int firstLine = buffer.toString().indexOf(BR);

			buffer.replace(0, firstLine + BR.length() - 1, "");
		}
		logTextArea.setValue(null);
		logTextArea.setValue(buffer.toString());
	}

	public void appendMessage(Level level, String message, Exception ex) {
		appendMessage(level, message + System.lineSeparator() + ExceptionUtils.getStackTrace(ex));
	}

	private class Message {
		private Level level;
		private String message;

		private Message(Level level, String message) {
			super();
			this.level = level;
			this.message = message;
		}

		public Level getLevel() {
			return level;
		}
		public String getMessage() {
			return message;
		}
	}

	public void clean() {
		buffer = new StringBuilder();
		logTextArea.setValue(buffer.toString());
	}


}
