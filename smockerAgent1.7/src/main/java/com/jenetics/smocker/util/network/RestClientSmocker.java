package com.jenetics.smocker.util.network;

import java.net.InetAddress;
import javax.xml.bind.DatatypeConverter;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import com.jenetics.smocker.configuration.MemoryConfiguration;
import com.jenetics.smocker.configuration.SystemPropertyConfiguration;
import com.jenetics.smocker.util.MessageLogger;
import com.jenetics.smocker.util.SmockerContainer;
import com.jenetics.smocker.util.TransformerUtility;

public class RestClientSmocker extends RESTClient {

	private static final String SMOCKER_REST_PATH = "/smocker/rest";
	private static final String SMOCKER_JAVAAPP_PATH = "/javaapplications";
	private static final String SMOCKER_ADDCONN = "/manageJavaApplication/addConnection";
	private static final String SMOCKER_ADDCOMM = "/manageJavaApplication/addCommunication";
	private static final String SMOCKER_GETMOCKEDCONN = "/connectionsMocked";
	private static final String SMOCKER_GET_LIST_MOCKED_HOST= "/mocks/listHostActivated";
	private static final String SMOCKER_CHECK_MATCH = "/mocks/checkMatch";
	private static final String SMOCKER_ALIVE= "/alive";
	//"http://admin:admin@localhost:9990/management/deployment/smocker-1.0-SNAPSHOT.war?operation=attribute&name=status"
	private static RestClientSmocker instance;

	public static synchronized RestClientSmocker getInstance() {
		if (instance == null) {
			instance = new RestClientSmocker();
		}
		return instance;
	}

	private RestClientSmocker() {
		super(SystemPropertyConfiguration.getTargetHost(), 
				SystemPropertyConfiguration.getTargetPort());

	}

	public String getAll() {
		return get(SMOCKER_REST_PATH + SMOCKER_JAVAAPP_PATH);
	}

	public String getAllMockedConnection() {
		return get(SMOCKER_REST_PATH + SMOCKER_GET_LIST_MOCKED_HOST);
	}

	public String getMockedConnections() {
		return get(SMOCKER_REST_PATH + SMOCKER_GETMOCKEDCONN);
	}

	public boolean checkAlive() {
		String resp = get(SMOCKER_REST_PATH + SMOCKER_ALIVE);
		String value = ResponseReader.readValueFromResponse(resp, "response");
		return Boolean.parseBoolean(value);
	}

	public String postConnection(SmockerContainer smockerContainer, Long javaAppId) {
		StringBuffer buffer = new StringBuffer();
		Map<String, String> headers = buildHeader();
		buffer.append("{\"id\": 0, \"version\": 0,  \"host\": \"")
		.append(smockerContainer.getHost())
		.append("\",  \"port\":")
		.append(smockerContainer.getPort())
		.append("}");
		String path = SMOCKER_REST_PATH + SMOCKER_ADDCONN + "/" + javaAppId;
		try {
			return put(buffer.toString(), path, null);
		} catch (Exception e) {
			MessageLogger.logThrowable(e);
		}
		return null;
	}


	//	public boolean checkRemoteAppReady() {
	//		getAdmin("")
	//	}


	public String postCommunication(SmockerContainer smockerContainer, Long javaAppId, Long connectionId) {

		String host = smockerContainer.getHost();
		int port = smockerContainer.getPort();

		if (!MemoryConfiguration.isConnecctionThere(host, port)) {
			MemoryConfiguration.setConnecctionWatched(host, port);	
		}
		//send only if the connection is watched

		if (MemoryConfiguration.isConnecctionWatched(host, port)) {
			StringBuffer buffer = new StringBuffer();
			Map<String, String> headers = buildHeader();
			if (
					smockerContainer.getSmockerSocketOutputStream() != null &&
					smockerContainer.getSmockerSocketOutputStream().getSmockerOutputStreamData() != null &&
					smockerContainer.getSmockerSocketInputStream() != null &&
					smockerContainer.getSmockerSocketInputStream().getSmockerOutputStreamData() != null
					)
				try {
					buffer.append("{\"id\": 0, \"request\":\"")
					.append(encode(smockerContainer.getSmockerSocketOutputStream().getSmockerOutputStreamData().getString()))
					.append("\",  \"response\":\"")
					.append(encode(smockerContainer.getSmockerSocketInputStream().getSmockerOutputStreamData().getString()))
					.append("\",  \"callerStack\":\"")
					.append(encode(smockerContainer.getStackTrace()))
					.append("\"}");
					String path = SMOCKER_REST_PATH + SMOCKER_ADDCOMM + "/" + javaAppId + "/" + connectionId;
					return put(buffer.toString(), path, headers);
				} catch (Exception e) {
					MessageLogger.logThrowable(e);
				}
		}
		return null;
	}


	public static String encode(String source) {
		return Base64.getEncoder().encodeToString(source.getBytes());
	}
	
	public static String decode(String source) {
		return new String(Base64.getDecoder().decode(source));
	}

	public String postCheckMatch(String content, String host) {
		StringBuffer buffer = new StringBuffer();
		Map<String, String> headers = buildHeader();
		buffer.append("{\"request\": \"" + encode(content) + "\",")
		.append("\"host\":\"" + host ).append("\"}");
		String path = SMOCKER_REST_PATH + SMOCKER_CHECK_MATCH;
		try {
			return post(buffer.toString(), path, headers);
		} catch (Exception e) {
			MessageLogger.logErrorWithMessage("Unable to communicate with target host", e, getClass());
		}
		return null;
	}

	public String postJavaApp(SmockerContainer smockerContainer) {
		StringBuffer buffer = new StringBuffer();
		Map<String, String> headers = buildHeader();
		String host = null;
		String ip = null;
		try {
			host = InetAddress.getLocalHost().getHostName();
			ip = InetAddress.getLocalHost().getHostAddress();
		} catch (Exception e) {
			MessageLogger.logErrorWithMessage("Unable to get current host", e, getClass());
			return null;
		}

		buffer.append("{\"id\": 0, \"version\": 0,  \"classQualifiedName\": \"")
		.append(TransformerUtility.getCallerApp())
		.append("\",  \"sourceHost\":\"")
		.append(host)
		.append("\",  \"sourceIp\":\"")
		.append(ip)
		.append("\",  \"sourcePort\":\"")
		.append(SystemPropertyConfiguration.getCommPort())
		.append("\"}");
		String path = SMOCKER_REST_PATH + SMOCKER_JAVAAPP_PATH;
		try {
			return post(buffer.toString(), path, headers);
		} catch (Exception e) {
			MessageLogger.logErrorWithMessage("Unable to communicate with target host", e, getClass());
		}
		return null;
	}

	private Map<String, String> buildHeader() {
		Map<String,String> headers = new HashMap<String,String>();
		//headers.put("Content-Type", "application/json");
		return headers;
	}


}
