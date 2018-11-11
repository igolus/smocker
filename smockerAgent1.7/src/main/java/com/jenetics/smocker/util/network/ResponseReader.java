package com.jenetics.smocker.util.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Map;

import com.jenetics.smocker.configuration.MemoryConfiguration;
import com.jenetics.smocker.configuration.util.ConnectionBehavior;
import com.jenetics.smocker.util.SimpleJsonReader;
import com.jenetics.smocker.util.TransformerUtility;

/**
 * Used to parse the response
 * @author igolus
 *
 */
public class ResponseReader {
	
	public static final String OK_STATUS = "200";
	public static final String CONFLICT = "409";

	public static String readValueFromResponse(String response, String key)  {
		try {
			String status = readStatusCodeFromResponse(response);
			if (status != null && status.equals(OK_STATUS)) {
				BufferedReader reader = new BufferedReader(new StringReader(response));
				String line;
				line = reader.readLine();
				while (!line.isEmpty()) {
					line = reader.readLine();
				}
				String jsonResp = reader.readLine();
				return SimpleJsonReader.readValue(jsonResp, key);
			}
		} catch (IOException e) {
			return null;
		}
		return null;
	}
	
	public static String readValuesFromResponse(String response, String key)  {
		try {
			String status = readStatusCodeFromResponse(response);
			if (status != null && status.equals(OK_STATUS)) {
				BufferedReader reader = new BufferedReader(new StringReader(response));
				String line;
				line = reader.readLine();
				while (!line.isEmpty()) {
					line = reader.readLine();
				}
				String jsonResp = reader.readLine();
				return SimpleJsonReader.readValue(jsonResp, key);
			}
		} catch (IOException e) {
			return null;
		}
		return null;
	}
	
	public static String getJsonString(String response) throws IOException {
		String status = readStatusCodeFromResponse(response);
		if (status != null && status.equals(OK_STATUS)) {
			BufferedReader reader = new BufferedReader(new StringReader(response));
			String line = reader.readLine();
			while (!line.isEmpty()) {
				line = reader.readLine();
			}
			String jsonResp = reader.readLine();
			return jsonResp;
		}
		return null;
	}
	
	public static String readStatusCodeFromResponse(String response) {
		String status = null;
		try {
			if (response != null) {
				BufferedReader reader = new BufferedReader(new StringReader(response));
				String line = reader.readLine();

				if (!line.isEmpty() && line.split(" ").length >= 3) {
					status = line.split(" ")[1];
				}
				return status;
			}
		} catch (IOException e) {
			return null;
		}
		return null;
	}
	
	/**
	 * Find the Id of the app in the existing apps
	 * @param allResponse
	 * @return
	 */
	public static String findExistingAppId(String allResponse) {
		int indexApp =  allResponse.indexOf(TransformerUtility.getCallerApp());
		if (indexApp != -1) {
			String subStr = allResponse.substring(0, indexApp);
			int idAppIndex = subStr.lastIndexOf("id");
			String remaining = allResponse.substring(idAppIndex + 4);
			int idAppIndexEnd = remaining.indexOf(",");
			String idStr = remaining.substring(0, idAppIndexEnd);
			return idStr;
		}
		return null;
	}
	

	/**
	 * Read the reponse to find all the connections
	 * @param allResponse
	 * @return
	 */
//	public static  Map<String, ConnectionBehavior> getConnections(String allResponse) {
//		String[] connections = SimpleJsonReader.readSubResources(allResponse, "connections");
//		for (int i = 0; connections != null && i < connections.length; i++) {
//			String connectionString = connections[i];
//			String host = SimpleJsonReader.readValue(connectionString, "host");
//			String port = SimpleJsonReader.readValue(connectionString, "port");
//			String watched = SimpleJsonReader.readValue(connectionString, "watched");
//			
//			int portInt = Integer.valueOf(port).intValue();
//			boolean watchedBool = Boolean.valueOf(watched).booleanValue();
//			
//			if (watchedBool) {
//				MemoryConfiguration.setConnecctionWatched(host, portInt);
//			}
//			else {
//				MemoryConfiguration.setConnecctionMute(host, portInt);
//			}
//		}
//		return null;
//	}

	public static String findExistingConnectionId(String allResponse, String host, int port) {
		int indexApp =  allResponse.indexOf("\"host\":\"" + host +  "\",\"port\":" + port);
		if (indexApp != -1) {
			String subStr = allResponse.substring(0, indexApp);
			int idAppIndex = subStr.lastIndexOf("id");
			String remaining = allResponse.substring(idAppIndex + 4);
			int idAppIndexEnd = remaining.indexOf(",");
			String idStr = remaining.substring(0, idAppIndexEnd);
			return idStr;
		}
		return null;
	}
	
	
	
}
