package com.mkyong.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.jboss.resteasy.spi.HttpResponse;

/**
 * Hello world!
 *
 */
public class App 
{
	public static void main(String[] args) {
	    	
	    try {	
		    DefaultHttpClient httpClient = new DefaultHttpClient();
			HttpGet getRequest = new HttpGet(
				"https://query.yahooapis.com/v1/public/yql?q=select%20*%20from%20weather.forecast%20where%20woeid%20in%20(select%20woeid%20from%20geo.places(1)%20where%20text%3D%22nome%2C%20ak%22)&format=json&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys");
			getRequest.addHeader("accept", "application/json");

			org.apache.http.HttpResponse response = httpClient.execute(getRequest);

			if (response.getStatusLine().getStatusCode() != 200) {
				throw new RuntimeException("Failed : HTTP error code : "
				   + response.getStatusLine().getStatusCode());
			}

			BufferedReader br = new BufferedReader(
	                         new InputStreamReader((response.getEntity().getContent())));

			String output;
			System.out.println("Output from Server .... \n");
			while ((output = br.readLine()) != null) {
				System.out.println(output);
			}

			httpClient.getConnectionManager().shutdown();
			
			//google
			//callGoogle();
	    }
	    catch (Exception ex) {
	    	ex.printStackTrace();
	    }
	}

	private static void callGoogle() throws UnknownHostException, IOException {
		InetAddress addr = InetAddress.getByName("www.google.com");
		Socket socket = new Socket(addr, 80);
		boolean autoflush = true;
		PrintWriter out = new PrintWriter(socket.getOutputStream(), autoflush);
		BufferedReader in = new BufferedReader(

		new InputStreamReader(socket.getInputStream()));
		// send an HTTP request to the web server
		out.println("GET / HTTP/1.1");
		out.println("Host: www.google.com:80");
		out.println("Connection: Close");
		out.println();

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
