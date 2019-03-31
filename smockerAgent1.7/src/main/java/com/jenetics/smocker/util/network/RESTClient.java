package com.jenetics.smocker.util.network;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Map;


/**
 * @author Riccardo Merolla
 *         Created on 13/01/15.
 */
public class RESTClient {

	private static final int SIZE_BUFFER = 1024;
	public static final String PATH = "/rest";
	private InetAddress inetAddress;
	private int port;

	public static final String METHOD_POST = "POST";
	public static final String METHOD_PUT = "PUT";
	public static final String METHOD_GET = "GET";
	
	public static final String CONTENT_TYPE_JSON = "application/json";

	private static final int TIMEOUT = 1000;

	public RESTClient(String hostName, int port) {
		try {
			this.inetAddress = InetAddress.getByName(hostName);
			this.port = port;
		} catch (UnknownHostException ex) {
			throw new IllegalArgumentException("Wrong: " + hostName + " Reason: " + ex, ex);
		}
	}

	public String post(String content, String path, Map<String,String> headers) {
		return sendMethod(content, CONTENT_TYPE_JSON, path, headers, TIMEOUT, METHOD_POST);
	}
	
	public String put(String content, String path, Map<String,String> headers) {
		return sendMethod(content, CONTENT_TYPE_JSON, path, headers, TIMEOUT, METHOD_PUT);
	}
	
	public String get(String contentPath, Map<String,String> headers) {
		return get(contentPath, headers, TIMEOUT);
	}

	public String get(String path) {
		return get(path, null, TIMEOUT);
	}

	public String get(String path, Map<String,String> headers, int timeout) {
		return sendMethod(null, CONTENT_TYPE_JSON, path, headers, timeout, METHOD_GET);
	}

	public String sendMethod(String content, String contentType, String path, 
			Map<String,String> headers, int timeout, String method) {
		Socket socket = null;
		BufferedWriter wr = null;
		InputStreamReader is = null;
		try {
			socket = new Socket(inetAddress, port);
			socket.setSoTimeout(timeout);
			wr = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF8"));
			InputStream inputStream = socket.getInputStream();
			is = new InputStreamReader(inputStream);
			wr.write(method + " " + path + " HTTP/1.1\r\n");
			if (content != null) {
				wr.write(getFormattedHeader("Content-Length","" + content.length()));
			}
			if (contentType != null) {
				wr.write(getFormattedHeader("Content-Type", contentType));
			}
			
			//required headers
			wr.write(getFormattedHeader("Host", inetAddress.getHostName() + ":" + port));
			wr.write(getFormattedHeader("Connection", "Close"));
			if (headers != null) {
				for (Map.Entry<String, String> header : headers.entrySet()) {
					wr.write(getFormattedHeader(header.getKey(),header.getValue()));
				}
			}
			
			wr.write("\r\n");
			if (content != null) {
				wr.write(content);
			}
			else {
				wr.write("\r\n");
			}
			wr.flush();
			char[] buffer = new char[SIZE_BUFFER];
			
			StringWriter stringWriter = new StringWriter();

		    for (int len = is.read(buffer); len != -1; len = is.read(buffer)) { 
		    	stringWriter.write(buffer, 0, len);
		    }
			return stringWriter.toString();
		} catch (Exception e) {
			return null;
		}finally{
			try {
				if (wr != null) {
					wr.close();
				}
			} catch (IOException ex) {
			}
			try {
				if (is != null) {
					is.close();
				}
			} catch (IOException ex) {
			}
			try {
				if (socket != null) {
					socket.close();
				}
			} catch (IOException ex) {
			}

		}

	}

	String getFormattedHeader(String key,String value){
		return key + ": " + value + "\r\n";
	}

	public InetAddress getInetAddress() {
		return inetAddress;
	}

	public int getPort() {
		return port;
	}
}