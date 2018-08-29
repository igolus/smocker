package com.jenetics.smocker.ui.view;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

import org.apache.commons.lang3.StringUtils;
import org.vaadin.easyapp.util.EasyAppLayout;
import org.vaadin.easyapp.util.annotations.ContentView;

import com.vaadin.annotations.Push;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.spring.annotation.ViewScope;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
@Push
@ViewScope
@ContentView(sortingOrder = 3, viewName = "LogsView", icon = "icons/log-file-1-504262.png", homeView = true, rootViewParent = ConnectionsRoot.class)
public class LogView extends EasyAppLayout {

	private static final int DEFAULT_MAX_SIZE = 40000;
	private StringBuilder buffer = new StringBuilder();
	private int maxSize;
	private Label logTextArea;
	
	public LogView() {
		this(DEFAULT_MAX_SIZE);
	}
	
	public LogView(int maxSize) {
		this.maxSize = maxSize;
		logTextArea = new Label();
		logTextArea.setContentMode(ContentMode.HTML);
		logTextArea.setSizeFull();
		
		Panel panel = new Panel();
		panel.setSizeFull();
		panel.setContent(logTextArea);
		panel.getContent().setSizeUndefined();
		
		VerticalLayout verticalLayout = new VerticalLayout();
		verticalLayout.addComponent(panel);
		verticalLayout.setSizeFull();
		
		addComponent(verticalLayout);
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
