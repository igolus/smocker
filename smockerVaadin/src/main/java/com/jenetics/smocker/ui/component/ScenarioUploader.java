package com.jenetics.smocker.ui.component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import javax.inject.Inject;

import org.jboss.logging.Logger;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jenetics.smocker.dao.DaoManager;
import com.jenetics.smocker.dao.DaoManagerByModel;
import com.jenetics.smocker.model.CommunicationMocked;
import com.jenetics.smocker.model.ConnectionMocked;
import com.jenetics.smocker.model.JavaApplicationMocked;
import com.jenetics.smocker.model.Scenario;
import com.jenetics.smocker.ui.SmockerUI;
import com.jenetics.smocker.util.SmockerUtility;
import com.vaadin.ui.Upload.Receiver;
import com.vaadin.ui.Upload.SucceededEvent;
import com.vaadin.ui.Upload.SucceededListener;

public class ScenarioUploader implements Receiver, SucceededListener {
	
	private Logger logger = Logger.getLogger(ScenarioUploader.class);
	
	DaoManager<Scenario> daoManagerScenario = DaoManagerByModel.getDaoManager(Scenario.class);
	DaoManager<ConnectionMocked> daoManagerConnectionMocked = 
			DaoManagerByModel.getDaoManager(ConnectionMocked.class);
	DaoManager<JavaApplicationMocked> daoManagerJavaApplicationMocked = 
			DaoManagerByModel.getDaoManager(JavaApplicationMocked.class);

	
	private static ObjectMapper mapper = new ObjectMapper();
	private static final String SEP = "_";
	private ByteArrayOutputStream bos;

	public ScenarioUploader() {
		super();
		this.bos = new ByteArrayOutputStream();
		mapper.configure(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE, false);
	}

	@Override
	public void uploadSucceeded(SucceededEvent event) {
		String content = bos.toString();
		try {
			Scenario scenarioImported = mapper.readValue(content, Scenario.class);
			scenarioImported.setId((long)0);
			String newName = findNewNameScenario(scenarioImported.getName());
			scenarioImported.setName(newName);
			createNewScenario(scenarioImported);
		} catch (IOException e) {
			SmockerUI.log(Level.SEVERE, SmockerUtility.getStackTrace(e));
		}
	}

	private List<CommunicationMocked> clone(List<CommunicationMocked> source) {
		List<CommunicationMocked> clonedList = new ArrayList<>();
		for (CommunicationMocked commSource : source) {
			CommunicationMocked cloneCommunication = cloneCommunication(commSource);
			cloneCommunication.setActivated(false);
			if (cloneCommunication !=null) {
				clonedList.add(cloneCommunication);
			}
		}
		return clonedList;
	}

	public static CommunicationMocked cloneCommunication(CommunicationMocked commSource) {
		try {
			StringWriter pw = new StringWriter();
			mapper.writeValue(pw, commSource);
			CommunicationMocked clonedComm = mapper.readValue(pw.toString(), CommunicationMocked.class);
			clonedComm.setId(null);
			clonedComm.setActivated(false);
			return clonedComm;
		} catch (IOException e) {
			SmockerUI.log(Level.SEVERE, SmockerUtility.getStackTrace(e));
		}
		return null;
	}

	private void createNewScenario(Scenario scenarioImported) {
		//associated comm to connection
		List<CommunicationMocked> communicationsMocked = clone(scenarioImported.getCommunicationsMocked());
		scenarioImported.getCommunicationsMocked().clear();
		daoManagerScenario.create(scenarioImported);

		String host = scenarioImported.getHost();
		String ip = scenarioImported.getIp();
		int port = scenarioImported.getPort();

		List<ConnectionMocked> listConnectionMocked = 
				daoManagerConnectionMocked.queryList("SELECT conn FROM ConnectionMocked conn WHERE conn.host = '" + host 
						+ "' and conn.port = '" + port + "'");

		ConnectionMocked targetConnectionMocked = listConnectionMocked.stream().findFirst().orElse(null);
		if (listConnectionMocked.size() == 0) {
			if (communicationsMocked.size() > 0) {
				targetConnectionMocked = new ConnectionMocked();
				targetConnectionMocked.setHost(host);
				targetConnectionMocked.setPort(port);
				targetConnectionMocked.setIp(ip);
				daoManagerConnectionMocked.create(targetConnectionMocked);
			}
		}


		for (CommunicationMocked comm : communicationsMocked) {
			targetConnectionMocked.getCommunications().add(comm);
			scenarioImported.getCommunicationsMocked().add(comm);
			comm.setConnection(targetConnectionMocked);
			comm.setScenario(scenarioImported);
		}

		daoManagerScenario.create(scenarioImported);
	}

	@Override
	public OutputStream receiveUpload(String filename, String mimeType) {
		return bos;
	}

	private String findNewNameScenario (String originalName) {
		if (findScenarioOfName(originalName).size() == 0) {
			return originalName;
		}
		int index = 1;
		while (findScenarioOfName(originalName + SEP + index).size() != 0)
		{
			index++;
		}
		return originalName + SEP + index;
	}

	private List<Scenario> findScenarioOfName(String name) {
		return daoManagerScenario.queryList("SELECT scenario FROM Scenario scenario WHERE scenario.name = '" + name + "'");
	}

}
