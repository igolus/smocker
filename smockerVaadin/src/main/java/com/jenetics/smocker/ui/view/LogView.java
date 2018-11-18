package com.jenetics.smocker.ui.view;

import java.util.logging.Level;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.vaadin.easyapp.util.ActionContainer;
import org.vaadin.easyapp.util.ActionContainerBuilder;
import org.vaadin.easyapp.util.EasyAppLayout;
import org.vaadin.easyapp.util.ActionContainer.InsertPosition;
import org.vaadin.easyapp.util.annotations.ContentView;

import com.jenetics.smocker.ui.SmockerUI;
import com.jenetics.smocker.ui.component.LoggerPanel;
import com.jenetics.smocker.ui.dialog.Dialog;
import com.vaadin.annotations.Push;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.spring.annotation.ViewScope;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Button.ClickEvent;

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
		verticalLayout.setWidth("100%");
		addComponent(verticalLayout);
	}

	public void appendMessage(Level level, String message) {
		panel.asyncAppendMessage(level, message);
	}
	
	public void appendMessage(Level level, String message, Exception ex) {
		panel.asyncAppendMessage(level, message + ExceptionUtils.getStackTrace(ex));
	}
	
	@Override
	public ActionContainer buildActionContainer() {
		ActionContainerBuilder builder = new ActionContainerBuilder(SmockerUI.BUNDLE_NAME)
				.addButton("Clean_Button", VaadinIcons.MINUS, "Clean_ToolTip",  () -> true		
						, this::clean, org.vaadin.easyapp.util.ActionContainer.Position.LEFT, InsertPosition.AFTER);
		return builder.build();
	}
	
	public void clean(ClickEvent event) {
		panel.clean();
	}

}
