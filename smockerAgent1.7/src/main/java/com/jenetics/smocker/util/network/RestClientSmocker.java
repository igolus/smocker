package com.jenetics.smocker.util.network;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.text.SimpleDateFormat;

import javax.xml.bind.DatatypeConverter;

import java.util.Date;
//import java.util.Base64;
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
	private static final String SMOCKER_LIST_WATCHED_CONNECTIONS =  "/connections/listHostUnWatched";
	
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
	
	public String getAllUnWachedConnections() {
		return get(SMOCKER_REST_PATH + SMOCKER_LIST_WATCHED_CONNECTIONS);
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

	public String postCommunication(SmockerContainer smockerContainer) {

		String host = smockerContainer.getHost();
		int port = smockerContainer.getPort();
		
		smockerContainer.getSmockerSocketOutputStream().getSmockerOutputStreamData().getBytes();
    	Long idConnection = TransformerUtility.getConnectionIdBySocket().get(smockerContainer.getSource());
		Long javaAppId = TransformerUtility.getJavaAppId();
		if (RemoteServerChecker.isConnectionWatched(host, port) && 
				smockerContainer.getSmockerSocketOutputStream() != null && 
				smockerContainer.getSmockerSocketInputStream()!= null ) {
			
			byte[] outputBytes = smockerContainer.getSmockerSocketOutputStream().getSmockerOutputStreamData().getBytes();
			byte[] inputBytes = smockerContainer.getSmockerSocketInputStream().getSmockerOutputStreamData().getBytes();

			StringBuffer buffer = new StringBuffer();
			Map<String, String> headers = buildHeader();
				try {
					buffer.append("{\"id\": 0, \"request\":\"")
					.append(encode(outputBytes))
					.append("\",  \"response\":\"")
					.append(encode(inputBytes))
					.append("\",  \"callerStack\":\"")
					.append(encode(smockerContainer.getStackTrace()))
					.append("\",  \"dateTime\":\"")
					.append(getCurrentDate())
					.append("\"}");
					String path = SMOCKER_REST_PATH + SMOCKER_ADDCOMM + "/" + javaAppId + "/" + idConnection;
					return put(buffer.toString(), path, headers);
				} catch (Exception e) {
					MessageLogger.logThrowable(e);
				}
		}
		return null;
	}
	
	private String getCurrentDate() {
		//DateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");
		return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX").format(new Date());
	}

	public String postCommunication(SmockerContainer smockerContainer, Long javaAppId, Long connectionId) {

		String host = smockerContainer.getHost();
		int port = smockerContainer.getPort();

		if (RemoteServerChecker.isConnectionWatched(host, port)) {
			StringBuffer buffer = new StringBuffer();
			Map<String, String> headers = buildHeader();
				try {
					String input = "";
					if (smockerContainer.getTeeInputStream() != null) {
						input = smockerContainer.getTeeInputStream().getBranch().getSmockerOutputStreamData().getString();
					}
					buffer.append("{\"id\": 0, \"request\":\"")
					.append(encode(input))
					.append("\",  \"response\":\"")
					.append(encode(smockerContainer.getTeeOutputStream().getBranch().getSmockerOutputStreamData().getBytes()))
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
		byte[] message = null;
		try {
			message = source.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			MessageLogger.logErrorWithMessage("Unable to encode ", e,RestClientSmocker.class);
		}
		String encoded = DatatypeConverter.printBase64Binary(message);
		return encoded;
	}
	
	public static String decode(String source) {
		byte[] decoded = DatatypeConverter.parseBase64Binary(source);
		try {
			return new String(decoded, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			MessageLogger.logErrorWithMessage("Unable to decode ", e,RestClientSmocker.class);
		}
		return null;
	}
	
	public static String encode(byte[] source) {
		String encoded = DatatypeConverter.printBase64Binary(source);
		return encoded;
	}
	
	public static byte[] decodeByte(String source) {
		return DatatypeConverter.parseBase64Binary(source);
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

	public String postJavaApp() {
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
