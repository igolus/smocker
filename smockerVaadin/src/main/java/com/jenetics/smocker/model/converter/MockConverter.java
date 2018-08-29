package com.jenetics.smocker.model.converter;

import java.util.List;
import java.util.Set;

import com.jenetics.smocker.dao.DaoManager;
import com.jenetics.smocker.dao.DaoManagerByModel;
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

	private static IDaoManager<ConnectionMocked> daoManagerConnectionMocked = DaoManagerByModel.getDaoManager(ConnectionMocked.class);
	private static IDaoManager<JavaApplicationMocked> daoManagerJavaApplicationMocked = DaoManagerByModel.getDaoManager(JavaApplicationMocked.class);

	
	/**
	 * convert communication to communicationMocked
	 * 
	 * @param sourceConnection
	 */
	public static void convertcommunication(Communication sourceCommunication) {
		Connection sourceConnection = sourceCommunication.getConnection();
		JavaApplication sourceJavaApplication = sourceConnection.getJavaApplication();
		JavaApplicationMocked targetJavaApplicationMocked = null;
		targetJavaApplicationMocked = findOrCreateTargetJavaApplication(sourceJavaApplication);
		
		ConnectionMocked targetConnectionMocked = findOrCreateTargetConnection(sourceConnection,
				targetJavaApplicationMocked);
		
		addSingleCommunicationToConnection(targetConnectionMocked, sourceCommunication, true);
	}
	
	
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
			addSingleCommunicationToConnection(targetConnectionMocked, communication, false);
		}
		daoManagerConnectionMocked.update(targetConnectionMocked);
	}

	private static void addSingleCommunicationToConnection(
			ConnectionMocked targetConnectionMocked, Communication communication, boolean updateIdBb ) {
		CommunicationMocked communicationMocked = new CommunicationMocked();
		communicationMocked.setScenario(DaoManagerByModel.getUNDEFINED_SCENARIO());
		communicationMocked.setRequest(communication.getRequest());
		communicationMocked.setResponse(communication.getResponse());
		communicationMocked.setDateTime(communication.getDateTime());
		communicationMocked.setConnection(targetConnectionMocked);
		
		targetConnectionMocked.getCommunications().add(communicationMocked);
		
		if (updateIdBb) {
			daoManagerConnectionMocked.update(targetConnectionMocked);
		}
		//
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
			targetConnectionMocked.setScenario(DaoManagerByModel.getUNDEFINED_SCENARIO());
			targetConnectionMocked.setHost(sourceConnection.getHost());
			targetConnectionMocked.setPort(sourceConnection.getPort());
			
			targetJavaApplicationMocked.getConnections().add(targetConnectionMocked);
			targetConnectionMocked.setJavaApplication(targetJavaApplicationMocked);
			
			daoManagerConnectionMocked.create(targetConnectionMocked);
			//update javaapplication
			daoManagerJavaApplicationMocked.update(targetJavaApplicationMocked);
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
			daoManagerJavaApplicationMocked.create(targetJavaApplicationMocked);
		}
		return targetJavaApplicationMocked;
	}
	
}
