package com.jenetics.smocker.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

import javax.inject.Inject;

import org.jboss.logging.Logger;

import com.jenetics.smocker.model.Connection;

public class ClientCommunicator {
	
	private static final String SEP = ":";
	private static final String WATCH = "WATCH";
	private static final String MUTE = "MUTE";
	
	@Inject
	private static Logger logger;
	
	public static boolean sendWatched(Connection conn) {
		String message = WATCH + " " + conn.getHost() + SEP + conn.getPort();
		try {
			sendMessageToClient(conn.getJavaApplication().getSourceHost(), conn.getJavaApplication().getSourcePort(), message);
		} catch (Exception e) {
			return false;
		}
		return true;
	}
	
	public static boolean sendUnWatched(Connection conn) {
		String message = MUTE + " " + conn.getHost() + SEP + conn.getPort();
		try {
			sendMessageToClient(conn.getJavaApplication().getSourceHost(), conn.getJavaApplication().getSourcePort(), message);
		} catch (Exception e) {
			return false;
		}
		return true;
	}
	
	private static String sendMessageToClient(String host, int port, String message) throws Exception {
		Socket socket = null;
		try {
			InetAddress addr = InetAddress.getByName( host);
		    socket = new Socket(addr, port);
		    boolean autoflush = true;
		    PrintWriter out = new PrintWriter(socket.getOutputStream(), autoflush);
		    BufferedReader in = new BufferedReader(

		    new InputStreamReader(socket.getInputStream()));
		    // send an HTTP request to the web server
		    out.println(message);
		    

		    // read the response
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
		    return sb.toString();
		}
		catch (Exception e) {
			logger.error("Unable to communicate with client", e);
			throw e;
		}
		finally {
			if (socket != null) {
				try {
					socket.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					logger.error("Unable to close socket", e);
				}
			}
			
		}
	}
}
