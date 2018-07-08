package com.mkyong.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.SocketChannel;

import org.apache.http.client.ClientProtocolException;
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
		while (true) {	
			try {	
				BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
				String line = bufferedReader.readLine();

				line = bufferedReader.readLine();
				callGoogle();
				callYahoo();
			}

			catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	private static void callYahoo() throws IOException, ClientProtocolException {
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
	}
	
	private static void callGoogleSocketChannell() throws UnknownHostException, IOException {
//		InetAddress addr = InetAddress.getByName("www.google.com");
//		Socket socket = new Socket(addr, 80);
//		boolean autoflush = true;
//		PrintWriter out = new PrintWriter(socket.getOutputStream(), autoflush);
		
		SocketChannel socketChannel = SocketChannel.open(new InetSocketAddress("www.google.com", 80));
		
//		SocketChannel socketChannel = SocketChannel.open();
//		socketChannel.socket().connect(new InetSocketAddress("www.google.com", 80), 1000);
		
		Channels.newOutputStream(socketChannel);
		
		StringBuffer bf = new StringBuffer();
		
		String lineSeparator = System.getProperty("line.separator");
		bf.append("GET / HTTP/1.1").append(lineSeparator);
		bf.append("Host: www.google.com:80").append(lineSeparator);
		bf.append("Connection: Close").append(lineSeparator);
		bf.append(lineSeparator);
		
		ByteBuffer buf = ByteBuffer.wrap(bf.toString().getBytes());
		socketChannel.write(buf);
		buf.clear();
		socketChannel.read(buf);
		socketChannel.close();
		System.out.println(new String(buf.array()));
		
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
