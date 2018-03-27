package com.jenetics.smocker.ui.view;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

import org.vaadin.easyapp.util.EasyAppLayout;
import org.vaadin.easyapp.util.annotations.ContentView;

import com.vaadin.annotations.Push;
import com.vaadin.spring.annotation.ViewScope;
import com.vaadin.ui.TextArea;

@SuppressWarnings("serial")
@Push
@ViewScope
@ContentView(sortingOrder = 1, viewName = "Logs", icon = "icons/Java-icon.png", homeView = true, rootViewParent = ConnectionsRoot.class)
public class LogView extends EasyAppLayout {

	private StringBuilder buffer = new StringBuilder();
	private int maxSize;
	private TextArea logTextArea;
	private static SimpleFormatter simpleFormatter = new SimpleFormatter();

	public LogView(int maxSize, String pattern) {
		this.maxSize = maxSize;
		logTextArea = new TextArea();
		logTextArea.setSizeFull();

		simpleFormatter = new SimpleFormatter();
		// TODO Auto-generated constructor stub
	}

	public void appendMessage(Level level, String message) {

		String formattedMessage = new SimpleFormatter().format(new LogRecord(level, message));

		buffer.append(formattedMessage);
		if (buffer.length() > maxSize) {
			buffer.replace(0, maxSize - buffer.length(), "");
		}
		logTextArea.setValue(buffer.toString());
	}

}
