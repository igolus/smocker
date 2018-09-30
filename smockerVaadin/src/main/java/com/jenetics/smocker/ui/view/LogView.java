package com.jenetics.smocker.ui.view;

import java.util.logging.Level;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.vaadin.easyapp.util.EasyAppLayout;
import org.vaadin.easyapp.util.annotations.ContentView;

import com.jenetics.smocker.ui.component.LoggerPanel;
import com.vaadin.annotations.Push;
import com.vaadin.spring.annotation.ViewScope;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
@Push
@ViewScope
@ContentView(sortingOrder = 3, viewName = "LogsView", icon = "icons/log-file-1-504262.png", homeView = true, rootViewParent = ConnectionsRoot.class)
public class LogView extends EasyAppLayout {

	private static final int DEFAULT_MAX_SIZE = 40000;
	private LoggerPanel panel;
	
	public LogView() {
		this(DEFAULT_MAX_SIZE);
	}
	
	public LogView(int maxSize) {
		panel = new LoggerPanel(maxSize);
		panel.setSizeFull();
		VerticalLayout verticalLayout = new VerticalLayout();
		verticalLayout.addComponent(panel);
		verticalLayout.setSizeFull();
		addComponent(verticalLayout);
	}

	public void appendMessage(Level level, String message) {
		panel.appendMessage(level, message);
	}
	
	public void appendMessage(Level level, String message, Exception ex) {
		panel.appendMessage(level, message + ExceptionUtils.getStackTrace(ex));
	}


}
