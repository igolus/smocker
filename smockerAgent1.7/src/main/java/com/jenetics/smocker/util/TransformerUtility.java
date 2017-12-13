package com.jenetics.smocker.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.input.TeeInputStream;
import org.apache.commons.io.output.TeeOutputStream;
//import com.jenetics.smocker.util.SmockerSocketInputStream;

import com.jenetics.smocker.configuration.util.ConnectionBehavior;
import com.jenetics.smocker.util.network.ResponseReader;
import com.jenetics.smocker.util.network.RestClientSmocker;

public class TransformerUtility {

	private static String callerApp = null;
	private static Long javaAppId = null;

	public static String getCallerApp() {
		return callerApp;
	}



	private static Hashtable<Socket, SmockerContainer> smockerContainerBySocket = new Hashtable<Socket, SmockerContainer>();
	private static Hashtable<Socket, Long> connectionIdBySocket = new Hashtable<Socket, Long>();

	public synchronized static InputStream manageInputStream(InputStream is, Socket source) {
		if (!filterSmockerBehavior()) {
			if (smockerContainerBySocket.containsKey(source)) {
				SmockerContainer smockerContainer = smockerContainerBySocket.get(source);
				if (smockerContainer.getSmockerSocketInputStream() == null) {
					SmockerSocketInputStream smockerInputStream = new SmockerSocketInputStream(source);
					smockerContainer.setSmockerSocketInputStream(smockerInputStream);
					TeeInputStream teeInputStream = new TeeInputStream(is, smockerInputStream, true);
					smockerContainer.setTeeInputStream(teeInputStream);
				}
				return smockerContainer.getTeeInputStream();
			}
			MessageLogger.logMessage("No socket key found in table", TransformerUtility.class);
		}
		return is;
	}

	public synchronized static OutputStream manageOutputStream(OutputStream os, Socket source) {
		// return create a tee only if not coming from SSL
		if (!filterSmockerBehavior()) {
			if (smockerContainerBySocket.containsKey(source)) {
				SmockerContainer smockerContainer = smockerContainerBySocket.get(source);
				if (smockerContainer.getSmockerSocketOutputStream() == null) {
					SmockerSocketOutputStream smockerOutputStream = new SmockerSocketOutputStream(source);
					smockerContainer.setSmockerSocketOutputStream(smockerOutputStream);
					TeeOutputStream teeOutputStream = new TeeOutputStream(os, smockerOutputStream);
					smockerContainer.setTeeOutputStream(teeOutputStream);
				}
				return smockerContainer.getTeeOutputStream();
			}
			MessageLogger.logMessage("No socket key found in table", TransformerUtility.class);
		}
		return os;
	}

	public synchronized static void socketClosed(Socket source) throws UnsupportedEncodingException {
		if (!filterSmockerBehavior()) {
			if (smockerContainerBySocket.get(source) != null) {
				if (connectionIdBySocket.get(source) != null && javaAppId != null) {
					Long idConnection = connectionIdBySocket.get(source);
					RestClientSmocker.getInstance().postCommunication(smockerContainerBySocket.get(source), javaAppId, idConnection);
				}
			}
		}
		// remove the container
		smockerContainerBySocket.remove(source);
	}

	private synchronized static void addSocketReference(Socket source, String host, int port) {
	if (callerApp == null) {
			String[] stackTraceAsArray = getStackTraceAsArray();
			callerApp = stackTraceAsArray[stackTraceAsArray.length - 1].split("\\(")[0];
			
		}
		try {
			if (!filterSmockerBehavior()) {
				String stackTrace = getStackTrace();
				SmockerContainer smockerContainer = new SmockerContainer(host, port, stackTrace);
				String allResponse = null;
				//only if the javaAppId was not found
				if (javaAppId == null) {
					//first get all the application from server
					allResponse = RestClientSmocker.getInstance().getAll();
					String existingId = ResponseReader.findExistingAppId(allResponse);
					if (existingId != null) {
						javaAppId = Long.valueOf(existingId);
					}
					else {
						String response = RestClientSmocker.getInstance().postJavaApp(smockerContainer);
						updateJavaAppId(response);
					}
					//fill the connection in the memory config
					Map<String, ConnectionBehavior> connectionsMap = ResponseReader.getConnections(allResponse);
						
				}
				if (javaAppId != null)  {
					String response = RestClientSmocker.getInstance().postConnection(smockerContainer, javaAppId);
					//check the status 
					String status = ResponseReader.readStatusCodeFromResponse(response);
					if (status.equals(ResponseReader.CONFLICT)) {
						allResponse = RestClientSmocker.getInstance().getAll();
						String existingConnectionId =  ResponseReader.findExistingConnectionId(allResponse, smockerContainer.getHost(), smockerContainer.getPort());
						connectionIdBySocket.put(source, Long.valueOf(existingConnectionId));
					}
					else {
						String idConnection = ResponseReader.readValueFromResponse(response, "id");
						if (idConnection != null) {
							connectionIdBySocket.put(source, Long.valueOf(idConnection));
						}
					}
				}
				smockerContainerBySocket.put(source, smockerContainer);
			}

		}
		catch (Exception ex) {
			MessageLogger.logThrowable(ex);
		} 

	}



	private synchronized static void updateJavaAppId(String response) throws IOException {
		String id = ResponseReader.readValueFromResponse(response, "id");
		if (id != null) {
			javaAppId = Long.parseLong(id);
		}
	}



	private static String getStackTrace() {
		StringBuffer sb = new StringBuffer();
		String[] stackTraceAsArray = getStackTraceAsArray();
		for (int i = 0; i < stackTraceAsArray.length; i++) {
			sb.append(stackTraceAsArray[i]);
			if (i < stackTraceAsArray.length - 1) {
				sb.append(System.getProperty("line.separator"));
			}
		}
		return sb.toString();
	}

	public static String[] getStackTraceAsArray() {
		List<String> arrRet = new ArrayList<String>();
		for (StackTraceElement ste : Thread.currentThread().getStackTrace()) {
			arrRet.add(ste.toString());
		}
		String[] ret = new String[arrRet.size()];
		ret = arrRet.toArray(ret);
		return ret;
	}

	public static void socketCreated(InetSocketAddress adress, InetSocketAddress localAdress, boolean stream,
			Socket source) {
		System.out.println();
		addSocketReference(source, source.getInetAddress().getHostName(), source.getPort());
	}

	public static void sslSocketCreated(Object o, InetAddress address, int port, Socket source) {
		//System.out.println();
		// addSocketReference(source);
	}

	public static void sslSocketCreated(Object o, String address, int port, Socket source) {
		//System.out.println();
		// addSocketReference(source);
	}

	public static void sslSocketCreated(Object o, String address, int port, InetAddress inetAddress, Socket source) {
		//System.out.println();
		// addSocketReference(source);
	}

	public static void sslSocketCreated(Object o, String address, int port, InetAddress inetAddress, int localPort,
			Socket source) {
		//System.out.println();
		// addSocketReference(source);
	}

	public static void sslSocketCreated(Object o, InetAddress host, int port, InetAddress localAddr, int localPort,
			Socket source) {
		// addSocketReference(source);
	}

	public static void sslSocketCreated(Object o, Socket sock, String host, int port, boolean autoClose,
			Socket source) {
		addSocketReference(source, host, port);
	}

	public static void sslSocketCreated(Object o, boolean serverMode, Object suites, byte clientAuth,
			boolean sessionCreation, Object protocols, String identificationProtocol, Object algorithmConstraints,
			Object sniMatchers, boolean preferLocalCipherSuites, Socket source) {
		// addSocketReference(source);
	}

	public static void sslSocketCreated(Object o, Socket sock, InputStream consumed, boolean autoClose, Socket source) {
		// addSocketReference(source);
	}

	public static void sslSocketCreated(Object o, Object context) {
		// System.out.println("Socket Created 9");
	}


	private static boolean filterSmockerBehavior() {
		return inSocketFromSSL() || 
				inStack("com.jenetics.smocker.util.network.RestClientSmocker") || 
				inStack("com.jenetics.smocker.util.network.SmockerServer$ClientTask");
	}

	protected static boolean inStack(String className) {
		StackTraceElement[] stackTrace = new Throwable().getStackTrace();
		for (int i = 1; i < stackTrace.length; i++) {
			if (stackTrace[i].getClassName().equals(className)) {
				return true;
			}
		}
		return false;
	}

	protected static boolean inSocketFromSSL() {
		StackTraceElement[] stackTrace = new Throwable().getStackTrace();
		// we are in socket coming from SSLSocket socket
		// if (stackTrace.length > 1 &&
		// stackTrace[2].getClassName().equals("java.net.Socket")) {
		boolean socketTrace = false;
		for (int i = 1; i < stackTrace.length; i++) {
			if (!socketTrace) {
				socketTrace = stackTrace[i].getClassName().equals("java.net.Socket");
			}
			if (!socketTrace && stackTrace[i].getClassName().equals("sun.security.ssl.SSLSocketImpl")) {
				return false;
			} else if (socketTrace && stackTrace[i].getClassName().equals("sun.security.ssl.SSLSocketImpl")) {
				return true;
			}
		}
		return false;
	}
}
