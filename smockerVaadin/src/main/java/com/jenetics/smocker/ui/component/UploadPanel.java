package com.jenetics.smocker.ui.component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import javax.inject.Inject;

import org.jboss.logging.Logger;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.jenetics.smocker.dao.DaoManager;
import com.jenetics.smocker.dao.DaoManagerByModel;
import com.jenetics.smocker.model.CommunicationMocked;
import com.jenetics.smocker.model.ConnectionMocked;
import com.jenetics.smocker.model.JavaApplicationMocked;
import com.jenetics.smocker.model.Scenario;
import com.jenetics.smocker.ui.SmockerUI;
import com.jenetics.smocker.util.SmockerUtility;
import com.vaadin.ui.Upload;
import com.vaadin.ui.Upload.Receiver;
import com.vaadin.ui.Upload.SucceededEvent;
import com.vaadin.ui.Upload.SucceededListener;
import com.vaadin.ui.VerticalLayout;

public class UploadPanel extends VerticalLayout {
	

	@Inject
	private Logger logger;
	
	DaoManager<Scenario> daoManagerScenario = DaoManagerByModel.getDaoManager(Scenario.class);
	DaoManager<ConnectionMocked> daoManagerConnectionMocked = DaoManagerByModel.getDaoManager(ConnectionMocked.class);
	DaoManager<JavaApplicationMocked> daoManagerJavaApplicationMocked = DaoManagerByModel.getDaoManager(JavaApplicationMocked.class);
	
	public UploadPanel() {
		ScenarioUploader uploader = new ScenarioUploader();
		Upload upload = new Upload(SmockerUI.getBundleValue("ScenarioUpload"), uploader);
		upload.addSucceededListener(uploader);
		addComponent(upload);
	}
	
	
	private class ScenarioUploader implements Receiver, SucceededListener {
		private ObjectMapper mapper = new ObjectMapper();
		private static final String SEP = "_";
		private ByteArrayOutputStream bos;

		private ScenarioUploader() {
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
			//System.out.println(content);
		}
		
		private List<CommunicationMocked> clone(Set<CommunicationMocked> source) {
			List<CommunicationMocked> clonedList = new ArrayList<>();
			for (CommunicationMocked commSource : source) {
				
				try {
					StringWriter pw = new StringWriter();
					mapper.writeValue(pw, commSource);
					clonedList.add(mapper.readValue(pw.toString(), CommunicationMocked.class));
				} catch (IOException e) {
					SmockerUI.log(Level.SEVERE, SmockerUtility.getStackTrace(e));
				} 
				
			}
			return clonedList;
		}

		private void createNewScenario(Scenario scenarioImported) {
			//associated comm to connection
			List<CommunicationMocked> communicationsMocked = clone(scenarioImported.getCommunicationsMocked());
			scenarioImported.getCommunicationsMocked().clear();
			daoManagerScenario.create(scenarioImported);
			
			String host = scenarioImported.getHost();
			int port = scenarioImported.getPort();
			String classQualifiedName = scenarioImported.getClassQualifiedName();
			
			List<ConnectionMocked> listConnectionMocked = 
					daoManagerConnectionMocked.queryList("SELECT conn FROM ConnectionMocked conn WHERE conn.host = '" + host 
					+ "' and conn.port = '" + port + "'");
			
			ConnectionMocked targetConnectionMocked = null;
			JavaApplicationMocked targetJavaApplicationMocked = null;
			if (listConnectionMocked.size() == 0) {
				targetConnectionMocked = new ConnectionMocked();
				targetConnectionMocked.setHost(host);
				targetConnectionMocked.setPort(port);
				targetConnectionMocked.setScenario(scenarioImported);
			}
			else {
				targetConnectionMocked = listConnectionMocked.get(0);
				targetJavaApplicationMocked = targetConnectionMocked.getJavaApplication();
			}
			if (targetJavaApplicationMocked == null) {
				targetJavaApplicationMocked = new JavaApplicationMocked();
				targetJavaApplicationMocked.setClassQualifiedName(classQualifiedName);
				targetConnectionMocked.setJavaApplication(targetJavaApplicationMocked);
				targetJavaApplicationMocked.getConnections().add(targetConnectionMocked);
				daoManagerJavaApplicationMocked.create(targetJavaApplicationMocked);
			}
			
			
			for (CommunicationMocked comm : communicationsMocked) {
				comm.setConnection(targetConnectionMocked);
				comm.setScenario(scenarioImported);
			}
			daoManagerScenario.update(scenarioImported);
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
	
}
