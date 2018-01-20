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
		targetJavaApplicationMocked = findOrCreateTargetJavaApplication(sourceJavaApplication);
		
		ConnectionMocked targetConnectionMocked = findOrCreateTargetConnection(sourceConnection,
				targetJavaApplicationMocked);
		
		addCommunications(sourceConnection, targetConnectionMocked);
	}
	
	/**
	 * convert JavaApplication to javaApplicationMocked
	 * 
	 * @param sourceConnection
	 */
	public static void convertJavaApplication(JavaApplication sourceJavaApplication) {
		JavaApplicationMocked targetJavaApplicationMocked = findOrCreateTargetJavaApplication(sourceJavaApplication);

		Set<Connection> connections = sourceJavaApplication.getConnections();	
		
		for (Connection sourceConnection : connections) {
			ConnectionMocked targetConnectionMocked = findOrCreateTargetConnection(sourceConnection, targetJavaApplicationMocked);
			Set<Communication> sourceCommunications = sourceConnection.getCommunications();
			addCommunications(sourceConnection, targetConnectionMocked);
		}
		
	}


	private static void addCommunications(Connection sourceConnection, ConnectionMocked targetConnectionMocked) {
		Set<Communication> sourceCommunications = sourceConnection.getCommunications();
		for (Communication communication : sourceCommunications) {
			CommunicationMocked communicationMocked = new CommunicationMocked();
			communicationMocked.setRequest(communication.getRequest());
			communicationMocked.setResponse(communication.getResponse());
			communicationMocked.setDateTime(communication.getDateTime());
			communicationMocked.setConnection(targetConnectionMocked);
			
			targetConnectionMocked.getCommunications().add(communicationMocked);
		}
		daoManagerConnectionMocked.update(targetConnectionMocked);
	}

	private static ConnectionMocked findOrCreateTargetConnection(Connection sourceConnection,
			JavaApplicationMocked targetJavaApplicationMocked) {
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
		return targetConnectionMocked;
	}
	

	private static JavaApplicationMocked findOrCreateTargetJavaApplication(JavaApplication sourceJavaApplication) {
		
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
			targetJavaApplicationMocked.setSourceHost(sourceJavaApplication.getSourceHost());
			targetJavaApplicationMocked.setSourcePort(sourceJavaApplication.getSourcePort());
			daoManagerJavaApplicationMocked.create(targetJavaApplicationMocked);
		}
		return targetJavaApplicationMocked;
	}
	
}
