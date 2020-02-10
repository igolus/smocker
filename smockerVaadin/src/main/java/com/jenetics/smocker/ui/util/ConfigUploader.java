package com.jenetics.smocker.ui.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import javax.inject.Inject;

import org.jboss.logging.Logger;
import org.springframework.beans.BeanUtils;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jenetics.smocker.dao.DaoConfig;
import com.jenetics.smocker.dao.DaoManager;
import com.jenetics.smocker.dao.DaoManagerByModel;
import com.jenetics.smocker.model.CommunicationMocked;
import com.jenetics.smocker.model.ConnectionMocked;
import com.jenetics.smocker.model.JavaApplicationMocked;
import com.jenetics.smocker.model.Scenario;
import com.jenetics.smocker.model.config.SmockerConf;
import com.jenetics.smocker.ui.SmockerUI;
import com.jenetics.smocker.ui.dialog.Dialog;
import com.jenetics.smocker.util.SmockerUtility;
import com.vaadin.ui.Upload.Receiver;
import com.vaadin.ui.Upload.SucceededEvent;
import com.vaadin.ui.Upload.SucceededListener;

public class ConfigUploader implements Receiver, SucceededListener {
	
	private Logger logger = Logger.getLogger(ConfigUploader.class);
	private static ObjectMapper mapper = new ObjectMapper();
	private ByteArrayOutputStream bos;

	private String content;

	public ConfigUploader() {
		super();
		this.bos = new ByteArrayOutputStream();
		mapper.configure(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE, false);
	}

	@Override
	public void uploadSucceeded(SucceededEvent event) {
		content = bos.toString();
		Dialog.ask(SmockerUI.getBundle().getString("override_Config_Question"), null, this::overrideConfig, null);
	}
	
	private void overrideConfig() {
		try {
			SmockerConf newConf = mapper.readValue(content, SmockerConf.class);
			SmockerConf singleConfig = DaoConfig.getSingleConfig();
			
			singleConfig.setDefaultMockFunction(newConf.getDefaultMockFunction());
			singleConfig.setTraceFunctionJsFunction(newConf.getTraceFunctionJsFunction());
			singleConfig.setFilterJsFunction(newConf.getFilterJsFunction());
			singleConfig.setFormatDisplayJsFunction(newConf.getFormatDisplayJsFunction());
			singleConfig.setGlobalJsFunction(newConf.getGlobalJsFunction());
			
			singleConfig.setDuplicateHosts(newConf.getDuplicateHosts());
			singleConfig.setExcludedHosts(newConf.getExcludedHosts());
			singleConfig.setIncludedHosts(newConf.getIncludedHosts());
			
			DaoConfig.saveConfig();
		} catch (IOException e) {
			Dialog.warning(SmockerUI.getBundleValue("warn_unable_to_import_conf"));
			SmockerUI.log(Level.SEVERE, SmockerUtility.getStackTrace(e));
		}
	}

	@Override
	public OutputStream receiveUpload(String filename, String mimeType) {
		return bos;
	}

}
