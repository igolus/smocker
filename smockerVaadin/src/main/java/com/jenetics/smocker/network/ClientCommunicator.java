package com.jenetics.smocker.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

import javax.inject.Inject;

import org.eclipse.persistence.jaxb.javamodel.JavaPackage;
import org.jboss.logging.Logger;

import com.jenetics.smocker.model.Connection;
import com.jenetics.smocker.model.JavaApplication;
import com.jenetics.smocker.util.SmockerException;

public class ClientCommunicator {

	private static final String SEP = ":";
	private static final String WATCH = "WATCH";
	private static final String MUTE = "MUTE";
	private static final String MODE = "MODE";
	
	@Inject
	private static Logger logger;

	private ClientCommunicator() {
		super();
	}


	public static boolean sendWatched(Connection conn) {
		String message = WATCH + " " + conn.getHost() + SEP + conn.getPort();
		try {
			sendMessageToClient(conn.getJavaApplication().getSourceHost(), conn.getJavaApplication().getSourcePort(),
					message);
		} catch (Exception e) {
			logger.error(e);
			return false;
		}
		return true;
	}
	
	public static boolean sendMode(String mode, JavaApplication javaApplication) {
		String message = MODE + " " + mode;
		try {
			sendMessageToClient(javaApplication.getSourceHost(), javaApplication.getSourcePort(),
					message);
		} catch (Exception e) {
			logger.error(e);
			return false;
		}
		return true;
	}

	public static boolean sendUnWatched(Connection conn) {
		String message = MUTE + " " + conn.getHost() + SEP + conn.getPort();
		try {
			sendMessageToClient(conn.getJavaApplication().getSourceHost(), conn.getJavaApplication().getSourcePort(),
					message);
		} catch (Exception e) {
			logger.error(e);
			return false;
		}
		return true;
	}

	private static String sendMessageToClient(String host, int port, String message) throws SmockerException {
		try (Socket socket = new Socket(InetAddress.getByName(host), port);) {
			boolean autoflush = true;
			PrintWriter out = new PrintWriter(socket.getOutputStream(), autoflush);
			BufferedReader in = new BufferedReader(

					new InputStreamReader(socket.getInputStream()));
			// send an HTTP request to the web server
			out.println(message);

			// read the response
			StringBuilder sb = readResponse(in);
			return sb.toString();
		} catch (Exception e) {
			throw new SmockerException("Unable to communicate with client", e);
		}
	}
	
	/**
	 * read response from reader
	 * @param in
	 * @return
	 * @throws IOException
	 */
	private static StringBuilder readResponse(BufferedReader in) throws IOException {
		boolean loop = true;
		StringBuilder sb = new StringBuilder(8096);
		while (loop) {
			if (in.ready()) {
				int i = 0;
				while (i != -1) {
					i = in.read();
					sb.append((char) i);
				}
				loop = false;
			}
		}
		return sb;
	}
}
