package com.jenetics.smocker.util;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.input.TeeInputStream;
import org.apache.commons.io.output.TeeOutputStream;
//import com.jenetics.smocker.util.SmockerSocketInputStream;

import com.jenetics.smocker.configuration.MemoryConfiguration;
import com.jenetics.smocker.configuration.util.ConnectionBehavior;
import com.jenetics.smocker.configuration.util.MockCconnectionMode;
import com.jenetics.smocker.util.network.CustomTeeInputStream;
import com.jenetics.smocker.util.network.CustomTeeOutputStream;
import com.jenetics.smocker.util.network.ResponseReader;
import com.jenetics.smocker.util.network.RestClientSmocker;

import sun.nio.ch.SocketAdaptor;

public class TransformerUtility {

	private static String callerApp = null;
	private static Long javaAppId = null;

	private static Hashtable<Object, SmockerContainer> smockerContainerBySocket = new Hashtable<Object, SmockerContainer>();
	private static Hashtable<Object, Long> connectionIdBySocket = new Hashtable<Object, Long>();
	
	public static SmockerContainer getSmockerContainer(Object source) {
		return smockerContainerBySocket.get(source);
	}
	
	
	public static String getCallerApp() {
		if (callerApp == null) {
			String[] stackTraceAsArray = getStackTraceAsArray();
			callerApp = stackTraceAsArray[stackTraceAsArray.length - 1].split("\\(")[0];
		}
		return callerApp;
	}
	
	public synchronized static OutputStream manageOutputStream(OutputStream os, Socket source) throws IOException {
		// return create a tee only if not coming from SSL
		if (!filterSmockerBehavior()) {
			source.setSoTimeout(10000);
			SmockerContainer container = null;
			if (!smockerContainerBySocket.containsKey(source)) {
				container = addSmockerContainer(source, source.getInetAddress().getHostName(),
						source.getPort());
			}
			else {
				container = smockerContainerBySocket.get(source);
			}

			SmockerSocketOutputStream smockerOutputStream = new SmockerSocketOutputStream();
			container.setSmockerSocketOutputStream(smockerOutputStream);
			TeeOutputStream teeOutputStream = new CustomTeeOutputStream(source, os, smockerOutputStream);
			//TeeOutputStream teeOutputStream = new TeeOutputStream(os, smockerOutputStream);
			container.setTeeOutputStream(teeOutputStream);
			SmockerContainer smockerContainer = smockerContainerBySocket.get(source);
			return smockerContainer.getTeeOutputStream();
			// MessageLogger.logMessage("No socket key found in table",
			// TransformerUtility.class);
		}
		return os;
	}


	public synchronized static InputStream manageInputStream(InputStream is, Socket source) throws IOException {
		
		if (!filterSmockerBehavior()) {
			
			String host = source.getInetAddress().getHostName();
			int port = source.getPort();
			
			if (!smockerContainerBySocket.containsKey(source)) {
				addSmockerContainer(source, host, port);
				// smockerContainerBySocket.put(source, value)
			}
			SmockerContainer smockerContainer = smockerContainerBySocket.get(source);
			if (smockerContainer.getSmockerSocketInputStream() == null) {
				MockCconnectionMode mode = MemoryConfiguration.getConnectionMode(host, port);
				if (mode == MockCconnectionMode.DISABLED || mode == null) { 
					SmockerSocketOutputStream smockerInputStream = new SmockerSocketOutputStream();
					smockerContainer.setSmockerSocketInputStream(smockerInputStream);
					CustomTeeInputStream teeInputStream = new CustomTeeInputStream(source, is, smockerInputStream);
					smockerContainer.setTeeInputStream(teeInputStream);
					return smockerContainer.getTeeInputStream();
					
				}
				else if (mode == MockCconnectionMode.STRICT) {
					InputStream mockIs = new MockInputStream(is, smockerContainerBySocket, source);
					return mockIs;	
				}
			}
		}
		return is;
	}
	
	public synchronized static void socketInputStreamCreated(Object o, Object impl) {
		System.out.println();
	}
	
	public synchronized static void manageOutputStreamNio (Object o) {
		System.out.println();
	}
	
	public synchronized static void socketChannelWrite (SocketChannel socketChannel, Object socketAdaptorObject, ByteBuffer b) throws IOException {
		System.out.println(new String(b.array()));
		if (socketChannel != null) {
			//SocketAdaptor socketAdaptor = (SocketAdaptor) socketAdaptorObject;
			SmockerContainer smockerContainer = smockerContainerBySocket.get(socketChannel);
			if (!smockerContainerBySocket.containsKey(socketChannel)) {
				InetSocketAddress remoteAddress = (InetSocketAddress) socketChannel.getRemoteAddress();
				smockerContainer = addSmockerContainer(socketChannel, remoteAddress.getHostName(), remoteAddress.getPort());
				SmockerSocketOutputStream smockerOutputStream = new SmockerSocketOutputStream();
				smockerContainer.setSmockerSocketOutputStream(smockerOutputStream);
			}
			smockerContainer.getSmockerSocketOutputStream().write(b.array());
		}
	}
	
	public synchronized static void socketChannelRead (SocketChannel socketChannel, Object socketAdaptorObject, ByteBuffer b) throws IOException {
		System.out.println(new String(b.array()));
		if (socketChannel != null) {
			SmockerContainer smockerContainer = smockerContainerBySocket.get(socketChannel);
			if (smockerContainer != null) {
				smockerContainer = smockerContainerBySocket.get(socketChannel);
				SmockerSocketOutputStream smockerInputStream = new SmockerSocketOutputStream();
				smockerContainer.setSmockerSocketInputStream(smockerInputStream);
			}
			smockerContainer.getSmockerSocketInputStream().write(b.array());
		}
	}
	
	public synchronized static void socketChannelClosed (Object o) throws IOException {
		socketClosed(o);
	}
	
	
	
	

	public synchronized static void socketClosed(Object source) throws UnsupportedEncodingException {
		//postCommuicationFromSource(source);
		// remove the container
		smockerContainerBySocket.remove(source);
	}


	public static synchronized void postCommuicationFromSource(Object source) {
		if (!filterSmockerBehavior()) {
			if (smockerContainerBySocket.get(source) != null) {
				if (connectionIdBySocket.get(source) != null && javaAppId != null) {
					Long idConnection = connectionIdBySocket.get(source);
					RestClientSmocker.getInstance().postCommunication(smockerContainerBySocket.get(source), javaAppId,
							idConnection);
				}
			}
		}
	}

//	private synchronized static void addSocketReference(Socket source, String host, int port) {
//		if (callerApp == null) {
//			String[] stackTraceAsArray = getStackTraceAsArray();
//			callerApp = stackTraceAsArray[stackTraceAsArray.length - 1].split("\\(")[0];
//
//		}
//		try {
//			if (!filterSmockerBehavior()) {
//				addSmockerContainer(source, source.getInetAddress().getHostName(),
//						source.getPort());
//			}
//
//		} catch (Exception ex) {
//			MessageLogger.logThrowable(ex);
//		}
//
//	}

	private static synchronized SmockerContainer addSmockerContainer(Object source, String host, int port) throws IOException {
		String stackTrace = getStackTrace();
		SmockerContainer smockerContainer = new SmockerContainer(host,port, stackTrace);
		String allResponse = null;
		// only if the javaAppId was not found
		if (javaAppId == null) {
			// first get all the application from server
			allResponse = RestClientSmocker.getInstance().getAll();
			if (allResponse != null) {
				String existingId = ResponseReader.findExistingAppId(allResponse);
				System.out.println("existingId ++++" + existingId);
				System.out.println("ALL RESPONSE ++++" + allResponse);
				
				
				
				if (existingId != null) {
					javaAppId = Long.valueOf(existingId);
				} else {
					updateJavaAppId(smockerContainer);
				}
				// fill the connection in the memory config
				//Map<String, ConnectionBehavior> connectionsMap = ResponseReader.getConnections(allResponse);
			}
		}
		if (javaAppId != null) {
			String idConnection = null;
			String response = RestClientSmocker.getInstance().postConnection(smockerContainer, javaAppId);
			// check the status
			String status = ResponseReader.readStatusCodeFromResponse(response);
			if (status.equals(ResponseReader.NOT_FOUND)) {
				//if no found post again the container
				updateJavaAppId(smockerContainer);
				//post again 
				response = RestClientSmocker.getInstance().postConnection(smockerContainer, javaAppId);
			}
			else if (status.equals(ResponseReader.CONFLICT)) {
				allResponse = RestClientSmocker.getInstance().getAll();
				idConnection = ResponseReader.findExistingConnectionId(allResponse,
						smockerContainer.getHost(), smockerContainer.getPort());
				//connectionIdBySocket.put(source, Long.valueOf(existingConnectionId));
			}
			if (idConnection == null) {
				idConnection = ResponseReader.readValueFromResponse(response, "id");
				
			}
			if (idConnection != null) {
				connectionIdBySocket.put(source, Long.valueOf(idConnection));
			}
		}
		smockerContainerBySocket.put(source, smockerContainer);
		return smockerContainer;
	}

	private synchronized static void updateJavaAppId(SmockerContainer smockerContainer) throws IOException {
		String response = RestClientSmocker.getInstance().postJavaApp(smockerContainer);
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

	// public static void socketCreated(InetSocketAddress adress,
	// InetSocketAddress localAdress, boolean stream,
	// Socket source) {
	// System.out.println();
	// addSocketReference(source, source.getInetAddress().getHostName(),
	// source.getPort());
	// }
	//
	// public static void socketCreated(Proxy proxy, Socket source) {
	// System.out.println();
	// addSocketReference(source, source.getInetAddress().getHostName(),
	// source.getPort());
	// }
	//
	// public static void socketCreated(Socket socket, Socket source) {
	// System.out.println();
	// addSocketReference(source, source.getInetAddress().getHostName(),
	// source.getPort());
	// }
	//
	//
	// public static void sslSocketCreated(Object o, InetAddress address, int
	// port, Socket source) {
	// //System.out.println();
	// // addSocketReference(source);
	// }
	//
	// public static void sslSocketCreated(Object o, String address, int port,
	// Socket source) {
	// //System.out.println();
	// // addSocketReference(source);
	// }
	//
	// public static void sslSocketCreated(Object o, String address, int port,
	// InetAddress inetAddress, Socket source) {
	// //System.out.println();
	// // addSocketReference(source);
	// }
	//
	// public static void sslSocketCreated(Object o, String address, int port,
	// InetAddress inetAddress, int localPort,
	// Socket source) {
	// //System.out.println();
	// // addSocketReference(source);
	// }
	//
	// public static void sslSocketCreated(Object o, InetAddress host, int port,
	// InetAddress localAddr, int localPort,
	// Socket source) {
	// // addSocketReference(source);
	// }
	//
	// public static void sslSocketCreated(Object o, Socket sock, String host,
	// int port, boolean autoClose,
	// Socket source) {
	// addSocketReference(source, host, port);
	// }
	//
	// public static void sslSocketCreated(Object o, boolean serverMode, Object
	// suites, byte clientAuth,
	// boolean sessionCreation, Object protocols, String identificationProtocol,
	// Object algorithmConstraints,
	// Object sniMatchers, boolean preferLocalCipherSuites, Socket source) {
	// // addSocketReference(source);
	// }
	//
	// public static void sslSocketCreated(Object o, boolean serverMode, Object
	// suites, byte clientAuth,
	// boolean sessionCreation, Object protocols, String identificationProtocol,
	// Object algorithmConstraints) {
	// // addSocketReference(source);
	// }
	//
	// public static void sslSocketCreated(Object o, boolean serverMode, Object
	// suites, byte clientAuth,
	// boolean sessionCreation, Object protocols, String identificationProtocol,
	// Object algorithmConstraints, Object sSLSocketImpl) {
	// // addSocketReference(source);
	// }
	//
	//// public static void sslSocketCreated(Object sSLContextImpl, boolean
	// serverMode ,Object cipherSuiteList,
	//// byte clientAuth, boolean sessionCreation ,Object protocols,String
	// identificationProtocol,
	//// Object algorithmConstraints)
	//
	// public static void sslSocketCreated(Object o, Socket sock, InputStream
	// consumed, boolean autoClose, Socket source) {
	// // addSocketReference(source);
	// }
	//
	// public static void sslSocketCreated(Object o, Object context) {
	// // System.out.println("Socket Created 9");
	// }

	private static boolean filterSmockerBehavior() {
		return inSocketFromSSL() || inStack("com.jenetics.smocker.util.network.RestClientSmocker")
				|| inStack("com.jenetics.smocker.util.network.SmockerServer$ClientTask");
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
