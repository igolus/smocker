package com.jenetics.smocker.util.network;


import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import javax.xml.bind.DatatypeConverter;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import com.jenetics.smocker.configuration.MemoryConfiguration;
import com.jenetics.smocker.configuration.SystemPropertyConfiguration;
import com.jenetics.smocker.util.MessageLogger;
import com.jenetics.smocker.util.SmockerContainer;
import com.jenetics.smocker.util.SmockerSocketOutputStreamData;
import com.jenetics.smocker.util.TransformerUtility;

import static com.jenetics.smocker.util.network.Base64Util.*;

public class RestClientSmocker extends RESTClient {

	private static final String SMOCKER_REST_PATH = "/smocker/rest";
	private static final String SMOCKER_JAVAAPP_PATH = "/javaapplications";
	private static final String SMOCKER_ADDCONN = "/manageJavaApplication/addConnection";
	private static final String SMOCKER_ADDCOMM = "/manageJavaApplication/addCommunication";
	private static final String SMOCKER_FINDMOCKS = "/findMock";
	
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
	
	public synchronized String getAll() {
		setPath(SMOCKER_REST_PATH + SMOCKER_JAVAAPP_PATH);
		return get();
	}
	
	public synchronized String findMock(String request, String className, String host, int port) {
		StringBuffer buffer = new StringBuffer();
		Map<String, String> headers = buildHeader();
		
		buffer
		.append("{\"id\": 0, \"version\": 0,  \"request\":\"")
		.append(request)
		.append("\",  \"className\":\"" )
		.append(className)
		.append("\",  \"host\":\"")
		.append(host)
		.append("\",  \"port\":")
		.append(port)
		.append("}");
		setPath(SMOCKER_REST_PATH + SMOCKER_FINDMOCKS);
		try {
			return post(buffer.toString(), null, POST);
		} catch (Exception e) {
			MessageLogger.logThrowable(e);
		}
		return null;
	}
	
	public synchronized String postConnection(SmockerContainer smockerContainer, Long javaAppId) {
		StringBuffer buffer = new StringBuffer();
		Map<String, String> headers = buildHeader();
		buffer.append("{\"id\": 0, \"version\": 0,  \"host\": \"")
			.append(smockerContainer.getHost())
			.append("\",  \"port\":")
			.append(smockerContainer.getPort())
			.append("}");
		setPath(SMOCKER_REST_PATH + SMOCKER_ADDCONN + "/" + javaAppId);
		try {
			System.out.println("********** POST ***************");
			System.out.println(buffer.toString());
			System.out.println("********** POST ***************");
			return post(buffer.toString(), headers, PUT);
		} catch (Exception e) {
			MessageLogger.logThrowable(e);
		}
		return null;
	}
	
	public synchronized String postCommunication(SmockerContainer smockerContainer, Long javaAppId, Long connectionId) {
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
					setPath(SMOCKER_REST_PATH + SMOCKER_ADDCOMM + "/" + javaAppId + "/" + connectionId);
					return post(buffer.toString(), headers, PUT);
				} catch (Exception e) {
					MessageLogger.logThrowable(e);
				}
		}
		return null;
	}
	



	private String decompressResponse(SmockerSocketOutputStreamData smockerOutputStreamData) {
		ByteArrayOutputStream buffer = smockerOutputStreamData.getBuffer();
		String content = readContentResponse(new String(buffer.toByteArray()));
		try {
			return decompress(content);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	private static String decompress(String str) throws Exception {
        if (str == null || str.length() == 0) {
            return str;
        }
        System.out.println("Input String length : " + str.length());
        GZIPInputStream gis = new GZIPInputStream(new ByteArrayInputStream(str.getBytes()));
        BufferedReader bf = new BufferedReader(new InputStreamReader(gis));
        String outStr = "";
        String line;
        while ((line=bf.readLine())!=null) {
          outStr += line;
        }
        System.out.println("Output String lenght : " + outStr.length());
        return outStr;
     }
	
	
	public static String readContentResponse(String response) {

		int indexSep = response.indexOf(System.getProperty("line.separator") + System.getProperty("line.separator"));
		if (indexSep != -1) {
			return response.substring(indexSep + System.getProperty("line.separator").length() * 2);
		}
		return null;
	}

	public synchronized String postJavaApp(SmockerContainer smockerContainer) {
		System.out.println("*****   Post Java App "  );
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
		setPath(SMOCKER_REST_PATH + SMOCKER_JAVAAPP_PATH);
		
		try {
			return post(buffer.toString(), headers, POST);
		} catch (Exception e) {
			MessageLogger.logErrorWithMessage("Unable to communicate with target host", e, getClass());
		}
		return null;
	}

	private Map<String, String> buildHeader() {
		Map<String,String> headers = new HashMap<String,String>();
		headers.put("Content-Type", "application/json");
		return headers;
	}


}
