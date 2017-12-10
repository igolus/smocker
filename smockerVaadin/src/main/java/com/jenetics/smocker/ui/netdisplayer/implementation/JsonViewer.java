package com.jenetics.smocker.ui.netdisplayer.implementation;

import java.util.ResourceBundle;

import org.jboss.logging.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jenetics.smocker.ui.netdisplayer.ComponentWithDisplayChange;
import com.jenetics.smocker.util.NetworkReaderUtility;
import com.vaadin.ui.Component;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TextArea;

public class JsonViewer implements ComponentWithDisplayChange {

	private String defaultTitle;

	private static ResourceBundle bundle = ResourceBundle.getBundle("BundleUI");
	private TabSheet tabsheet = new TabSheet();
	private TextArea areaOutput;
	private TextArea areaOutputJson;

	private Logger logger = Logger.getLogger(JsonViewer.class);

	public JsonViewer(String defaultTitle) {
		super();
		this.defaultTitle = defaultTitle;
	}


	@Override
	public Component getComponent() {
		areaOutput = new TextArea();
		areaOutput.setReadOnly(true);
		areaOutput.setSizeFull();

		areaOutputJson = new TextArea();
		areaOutputJson.setReadOnly(true);
		areaOutputJson.setSizeFull();

		tabsheet.addTab(areaOutput, defaultTitle);
		tabsheet.addTab(areaOutputJson, bundle.getString("Json"));

		tabsheet.setSizeFull();
		return tabsheet;
	}

	@Override
	public void selectionValue(String content) {

		areaOutput.setReadOnly(false);
		areaOutput.setValue(content);
		areaOutput.setReadOnly(true);

		areaOutputJson.setReadOnly(false);
		String message = NetworkReaderUtility.readContentResponse(content);
		String jsonContent = NetworkReaderUtility.getJsonContent(message);

		ObjectMapper mapper = new ObjectMapper();
		Object json;
		String formattedJson;
		try {
			json = mapper.readValue(jsonContent, Object.class);
			formattedJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
			areaOutputJson.setValue(formattedJson);
		} catch (Exception e) {
			logger.error("Unable to parse Json response", e);
			areaOutputJson.setValue(bundle.getString("ParseError"));
		}
		areaOutputJson.setReadOnly(true);
	}

}
