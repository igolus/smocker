package com.jenetics.smocker.util.network;

import java.util.HashMap;
import java.util.Map;

import com.jenetics.smocker.util.SmockerContainer;

public class RestClientSmocker extends RESTClient {

	private static final String CONNECTIONS = "connections";
	private static final String SMOCKER_REST_PATH = "/smocker/rest/";
	private static RestClientSmocker instance;
	
	public static synchronized RestClientSmocker getInstance() {
		if (instance == null) {
			instance = new RestClientSmocker();
		}
		return instance;
	}

	private RestClientSmocker() {
		super("localhost", 8080);
		// TODO Auto-generated constructor stub
	}
	
	public void postConnection(SmockerContainer smockerContainer) {
		StringBuffer buffer = new StringBuffer();
		Map<String, String> headers = buildHeader();
		
		//"{\"id\": 0, \"version\": 0, \"host\": \"toto\",  \"port\": 10}"
		buffer.append("{\"id\": 0, \"version\": 0,  \"host\": \"")
			.append(smockerContainer.getHost())
			.append("\",  \"port\":")
			.append(smockerContainer.getPort())
			.append("}");
		setPath(SMOCKER_REST_PATH + CONNECTIONS);
		try {
			post(buffer.toString(), headers);
		} catch (Exception e) {
			// TODO COOL ERROR LOGGING
			e.printStackTrace();
		}
	}

	private Map<String, String> buildHeader() {
		Map<String,String> headers = new HashMap<String,String>();
		headers.put("Content-Type", "application/json");
		return headers;
	}

}
