package com.jenetics.smocker.ui.util;

import javax.enterprise.event.Observes;

import com.jenetics.smocker.model.Communication;
import com.jenetics.smocker.model.Connection;
import com.jenetics.smocker.model.EntityWithId;
import com.jenetics.smocker.model.JavaApplication;
import com.jenetics.smocker.ui.SmockerUI;

public class EventManager {

	public static final String CONNECTIONS = "Connections";
	public static final String JAVA_APPLICATIONS = "Java Applications";

	public void newConnection(@Observes Connection conn) {
		refreshView(conn);
	}

	public void newJavaApplication(@Observes JavaApplication application) {
		refreshView(application);
	}

	public void newCommunication(@Observes Communication comm) {
		//refreshView(comm);
	}

	private void refreshView(EntityWithId entityWithId) {
		if (SmockerUI.getInstance() != null && SmockerUI.getInstance().getSession() != null) {
			SmockerUI.getInstance().refreshView(entityWithId);
		}
	}
}
