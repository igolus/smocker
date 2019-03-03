package com.jenetics.smocker.ui.util;

import javax.enterprise.event.Observes;

import com.jenetics.smocker.dao.DaoConfigUpdaterThread;
import com.jenetics.smocker.model.Communication;
import com.jenetics.smocker.model.Connection;
import com.jenetics.smocker.model.EntityWithId;
import com.jenetics.smocker.model.JavaApplication;
import com.jenetics.smocker.model.event.CommunicationsRemoved;
import com.jenetics.smocker.ui.SmockerUI;

public class EventManager {

	public static final String CONNECTIONS = "Connections";
	public static final String JAVA_APPLICATIONS = "Java Applications";

	public void newConnection(@Observes Connection conn) {
		if (DaoConfigUpdaterThread.getSingleConf().isAutorefesh()) {
			refreshView(conn);
		}
	}

	public void newJavaApplication(@Observes JavaApplication application) {
		if (DaoConfigUpdaterThread.getSingleConf().isAutorefesh()) {
			refreshView(application);
		}
	}

	public void newCommunication(@Observes Communication comm) {
		if (DaoConfigUpdaterThread.getSingleConf().isAutorefesh()) {
			refreshView(comm);
		}
	}
	
	public void newCommunicationsRemoved(@Observes CommunicationsRemoved commsRemoved) {
		SmockerUI.getInstance().remove(commsRemoved);
	}
	

	private void refreshView(EntityWithId entityWithId) {
		if (SmockerUI.getInstance() != null) {
			SmockerUI.getInstance().refreshView(entityWithId);
		}
	}
	
	
}
