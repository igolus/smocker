package com.jenetics.smocker.util.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import com.jenetics.smocker.configuration.SystemPropertyConfiguration;

public class SocketServer {

	private ServerSocket server;
	private Socket client;
	private BufferedReader in;
	private PrintWriter out;

	public void createSocketServer() {
		try{
			server = new ServerSocket(SystemPropertyConfiguration.getCommPort()); 
		} catch (IOException e) {
			System.out.println("Could not listen on port 4321");
			System.exit(-1);
		}

		try{
			client = server.accept();
		} catch (IOException e) {
			System.out.println("Accept failed: " + SystemPropertyConfiguration.getCommPort());
			System.exit(-1);
		}

		try{
			in = new BufferedReader(new InputStreamReader(client.getInputStream()));
			out = new PrintWriter(client.getOutputStream(), true);
		} catch (IOException e) {
			System.out.println("Read failed");
			System.exit(-1);
		}
		
		while(true){
			try{
				String line = in.readLine();
				System.out.println(line);
				out.println(line);
				client.close();
			} catch (IOException e) {
				System.out.println("Read failed");
				System.exit(-1);
			}
		}

	}
}
