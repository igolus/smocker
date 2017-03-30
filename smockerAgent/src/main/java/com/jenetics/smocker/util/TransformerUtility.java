package com.jenetics.smocker.util;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Hashtable;

import org.apache.commons.io.input.TeeInputStream;
import org.apache.commons.io.output.TeeOutputStream;
//import com.jenetics.smocker.util.SmockerSocketInputStream;

import com.jenetics.smocker.util.network.RestClientSmocker;

import comm.SocketClient;

public class TransformerUtility {

	private static Hashtable<Socket, SmockerContainer> smockerContainerBySocket = new Hashtable<Socket, SmockerContainer>();

	public static InputStream manageInputStream(InputStream is, Socket source) {
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
			ExceptionLogger.logMessage("No socket key found in table");
		}
		return is;
	}

	public static OutputStream manageOutputStream(OutputStream os, Socket source) {
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
			ExceptionLogger.logMessage("No socket key found in table");
		}
		return os;
	}

	public synchronized static void socketClosed(Socket source) throws UnsupportedEncodingException {
		if (!filterSmockerBehavior()) {
			if (smockerContainerBySocket.get(source) != null) {
				SocketClient.getSocketClient().sendConnectionClosed(smockerContainerBySocket.get(source));
			}
		}
		// remove the container
		smockerContainerBySocket.remove(source);
	}

	private static void addSocketReference(Socket source, String host, int port) {
		if (!filterSmockerBehavior()) {
			SmockerContainer smockerContainer = new SmockerContainer(host, port);
			smockerContainerBySocket.put(source, smockerContainer);
			RestClientSmocker.getInstance().postConnection(smockerContainer);
		}
	}

	public static void socketCreated(InetSocketAddress adress, InetSocketAddress localAdress, boolean stream,
			Socket source) {
		// addSocketReference(source);
	}

	public static void sslSocketCreated(Object o, InetAddress address, int port, Socket source) {
		// addSocketReference(source);
	}

	public static void sslSocketCreated(Object o, String address, int port, Socket source) {
		// addSocketReference(source);
	}

	public static void sslSocketCreated(Object o, String address, int port, InetAddress inetAddress, Socket source) {
		// addSocketReference(source);
	}

	public static void sslSocketCreated(Object o, String address, int port, InetAddress inetAddress, int localPort,
			Socket source) {
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
		return inSocketFromSSL() || inStack("com.jenetics.smocker.util.network.RestClientSmocker");
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
