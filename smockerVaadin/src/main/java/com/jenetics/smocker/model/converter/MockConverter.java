package com.jenetics.smocker.model.converter;

import java.util.List;
import java.util.Set;

import com.jenetics.smocker.dao.DaoManager;
import com.jenetics.smocker.dao.IDaoManager;
import com.jenetics.smocker.model.Communication;
import com.jenetics.smocker.model.CommunicationMocked;
import com.jenetics.smocker.model.Connection;
import com.jenetics.smocker.model.ConnectionMocked;
import com.jenetics.smocker.model.JavaApplication;
import com.jenetics.smocker.model.JavaApplicationMocked;
import com.jenetics.smocker.ui.SmockerUI;

public class MockConverter {

	private MockConverter() {
		super();
		// TODO Auto-generated constructor stub
	}

	private static IDaoManager<ConnectionMocked> daoManagerConnectionMocked = new DaoManager<>(ConnectionMocked.class,
			SmockerUI.getEm());
	private static IDaoManager<JavaApplicationMocked> daoManagerJavaApplicationMocked = new DaoManager<>(
			JavaApplicationMocked.class, SmockerUI.getEm());

	/**
	 * convert connection to connectionMocked
	 * 
	 * @param sourceConnection
	 */
	public static void convertConnection(Connection sourceConnection) {
		JavaApplication sourceJavaApplication = sourceConnection.getJavaApplication();
		JavaApplicationMocked targetJavaApplicationMocked = null;
		List<JavaApplicationMocked> allJavaApplicationsMocked = daoManagerJavaApplicationMocked.listAll();
		// check if there is already a mocked java application
		for (JavaApplicationMocked javaApplicationMocked : allJavaApplicationsMocked) {
			if (javaApplicationMocked.getClassQualifiedName().equals(sourceJavaApplication.getClassQualifiedName())) {
				targetJavaApplicationMocked = javaApplicationMocked;
				break;
			}
		}
		// otherwise create it
		if (targetJavaApplicationMocked == null) {
			targetJavaApplicationMocked = new JavaApplicationMocked();
			targetJavaApplicationMocked.setClassQualifiedName(sourceJavaApplication.getClassQualifiedName());
			daoManagerJavaApplicationMocked.create(targetJavaApplicationMocked);
		}

		ConnectionMocked targetConnectionMocked = null;
		// check if the connection is already defined
		Set<ConnectionMocked> connectionsMocked = targetJavaApplicationMocked.getConnections();
		for (ConnectionMocked connectionMocked : connectionsMocked) {
			if (connectionMocked.getHost().equals(sourceConnection.getHost())
					&& connectionMocked.getPort().equals(sourceConnection.getPort())) {
				targetConnectionMocked = connectionMocked;
				break;
			}
		}
		// otherwise create it
		if (targetConnectionMocked == null) {
			targetConnectionMocked = new ConnectionMocked();
			targetConnectionMocked.setHost(sourceConnection.getHost());
			targetConnectionMocked.setPort(sourceConnection.getPort());
			
			targetJavaApplicationMocked.getConnections().add(targetConnectionMocked);
			targetConnectionMocked.setJavaApplication(targetJavaApplicationMocked);
			
			daoManagerConnectionMocked.create(targetConnectionMocked);
		}

		Set<Communication> sourceCommunications = sourceConnection.getCommunications();
		for (Communication communication : sourceCommunications) {
			CommunicationMocked communicationMocked = new CommunicationMocked();
			communicationMocked.setRequest(communication.getRequest());
			communicationMocked.setResponse(communication.getResponse());
			communicationMocked.setConnection(targetConnectionMocked);
			targetConnectionMocked.getCommunications().add(communicationMocked);
		}
		daoManagerConnectionMocked.update(targetConnectionMocked);
	}
}
