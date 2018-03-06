package com.jenetics.smocker.util.network;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.jenetics.smocker.util.MessageLogger;


/**
 * @author Riccardo Merolla
 *         Created on 13/01/15.
 */
public class RESTClient {

    public static final String PATH = "/rest";
    private InetAddress inetAddress;
    private int port;
    private String path;
    
    public static final String POST = "POST";
    public static final String PUT = "PUT";
    
    public void setPath(String path) {
		this.path = path;
	}

	private static final Logger logger = Logger.getLogger(RESTClient.class.getName());
    public static final String HEADER_NAME_PREFIX = "six_";
    private static final int TIMEOUT = 1000;

    public RESTClient(String hostName, int port, String path) {
        try {
            this.inetAddress = InetAddress.getByName(hostName);
            this.port = port;
            this.path = path;
        } catch (UnknownHostException ex) {
            throw new IllegalArgumentException("Wrong: " + hostName + " Reason: " + ex, ex);
        }
    }
    
    public RESTClient(String hostName, int port) {
        try {
            this.inetAddress = InetAddress.getByName(hostName);
            this.port = port;
        } catch (UnknownHostException ex) {
            throw new IllegalArgumentException("Wrong: " + hostName + " Reason: " + ex, ex);
        }
    }

    public RESTClient(URL url) {
        this(url.getHost(), url.getPort(), url.getPath());
    }

    public RESTClient() {
        this("localhost", 8080, PATH);
    }

    public String put(String content, Map<String,String> headers) {
        Socket socket = null;
        BufferedWriter wr = null;
        InputStreamReader is = null;
        try {
            socket = new Socket(inetAddress, port);
            socket.setSoTimeout(TIMEOUT);
            wr = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF8"));
            is = new InputStreamReader(socket.getInputStream());
            wr.write("PUT " + path + " HTTP/1.0\r\n");
            wr.write(getFormattedHeader("Content-Length","" + content.length()));
            wr.write(getFormattedHeader("Content-Type", "text/plain"));
            for (Map.Entry<String, String> header : headers.entrySet()) {
                wr.write(getFormattedHeader(HEADER_NAME_PREFIX + header.getKey(),header.getValue()));
            }
            wr.write("\r\n");
            wr.write(content);
            wr.flush();
            char[] buffer = new char[1024];
            StringWriter stringWriter = new StringWriter();
            while(is.read(buffer)!=-1){
                stringWriter.write(buffer);
            }
            return stringWriter.toString();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Problem communicating with SIX services: {0}", e);
            return "--";
        }finally{
            try {
                wr.close();
            } catch (IOException ex) {
            }
            try {
                is.close();
            } catch (IOException ex) {
            }
            try {
                socket.close();
            } catch (IOException ex) {
            }

        }

    }

    public String post(String content, Map<String,String> headers, String method) throws Exception {
        return post(content, headers, TIMEOUT, method);
    }

    public String post(String content, Map<String,String> headers, int timeout, String method) throws Exception {
        Socket socket = null;
        BufferedWriter wr = null;
        InputStreamReader is = null;
        try {
            socket = new Socket(inetAddress, port);
            socket.setSoTimeout(timeout + 1000);
            wr = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF8"));
            //wr = new BufferedWriter(new OutputStreamWriter(System.out));
            
            is = new InputStreamReader(socket.getInputStream());
            wr.write(method + " " + path + " HTTP/1.0\r\n");
            wr.write(getFormattedHeader("Content-Length","" + content.length()));
            wr.write(getFormattedHeader("Content-Type", "application/json"));
            if (headers != null) {
            	for (Map.Entry<String, String> header : headers.entrySet()) {
                    wr.write(getFormattedHeader(HEADER_NAME_PREFIX + header.getKey(),header.getValue()));
                }
            }
            wr.write("\r\n");
            wr.write(content);
            wr.flush();
            char[] buffer = new char[1024];
            StringWriter stringWriter = new StringWriter();
            while(is.read(buffer)!=-1){
                stringWriter.write(buffer);
            }
            return stringWriter.toString();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Problem communicating", e);
            throw e;
        }finally{
            try {
                wr.close();
            } catch (IOException ex) {
            }
            try {
                is.close();
            } catch (IOException ex) {
            }
            try {
                socket.close();
            } catch (IOException ex) {
            }

        }

    }

    public String get(String content, Map<String,String> headers) {
        return get(content, headers, TIMEOUT);
    }
    
    public String get() {
        return get(null, null, TIMEOUT);
    }

    public String get(String content, Map<String,String> headers, int timeout) {
        Socket socket = null;
        BufferedWriter wr = null;
        InputStreamReader is = null;
        try {
            socket = new Socket(inetAddress, port);
            socket.setSoTimeout(timeout);
            wr = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF8"));
            is = new InputStreamReader(socket.getInputStream());
            wr.write("GET " + path + " HTTP/1.0\r\n");
            if (content != null) {
            	wr.write(getFormattedHeader("Content-Length","" + content.length()));
                wr.write(getFormattedHeader("Content-Type", "application/json"));

            }
            if (headers != null) {
            	for (Map.Entry<String, String> header : headers.entrySet()) {
                    wr.write(getFormattedHeader(HEADER_NAME_PREFIX + header.getKey(),header.getValue()));
                }
            }
            wr.write("\r\n");
            if (content != null) {
            	wr.write(content);
            }
            wr.flush();
            
            
            BufferedReader inFromServer = new BufferedReader(is);
            String response;
            StringWriter stringWriter = new StringWriter();
            while((response = inFromServer.readLine()) != null){
            	stringWriter.write(response);
            	stringWriter.write(System.lineSeparator());
            }
            return stringWriter.toString();
        } catch (Exception e) {
        	MessageLogger.logError("Unable to communicate with smocker App", getClass());
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

    public String getPath() {
        return path;
    }

    public int getPort() {
        return port;
    }
}