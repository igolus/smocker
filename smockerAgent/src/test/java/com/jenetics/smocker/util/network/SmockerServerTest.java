package com.jenetics.smocker.util.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;


public class SmockerServerTest {
	@Test
	public void testConnection() throws IOException {
		InetAddress addr = InetAddress.getByName("localhost");
	    Socket socket = new Socket(addr, 2024);
	    boolean autoflush = true;
	    PrintWriter out = new PrintWriter(socket.getOutputStream(), autoflush);
	    BufferedReader in = new BufferedReader(

	    new InputStreamReader(socket.getInputStream()));
	    // send an HTTP request to the web server
	    out.println("TEST");
	    

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
	    System.out.println(sb.toString());
	    socket.close();
	}
}
