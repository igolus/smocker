package com.jenetics.smocker.ui.util;

import java.util.HashMap;

import javax.enterprise.event.Observes;

import com.jenetics.smocker.model.Connection;
import com.jenetics.smocker.model.EntityWithId;
import com.jenetics.smocker.model.JavaApplication;
import com.jenetics.smocker.ui.SmockerUI;
import com.vaadin.ui.UI;

public class EventManager {
	
	public static final String CONNECTIONS = "Connections";
	public static final String JAVA_APPLICATIONS = "Java Applications";

	public void newConnection(@Observes Connection conn) {
		refreshView(CONNECTIONS, conn);
	}
	
   public void newJavaApplication(@Observes JavaApplication application) {
		refreshView(JAVA_APPLICATIONS, application);
	}

	private void refreshView(String viewName, EntityWithId entityWithId) {
		if (SmockerUI.getInstance() != null && SmockerUI.getInstance().getSession() != null) {
			SmockerUI.getInstance().refreshView(viewName, entityWithId);
		}
	}
}
