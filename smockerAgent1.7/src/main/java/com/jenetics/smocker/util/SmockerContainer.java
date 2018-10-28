package com.jenetics.smocker.util;

import java.io.UnsupportedEncodingException;
import java.net.Socket;

import com.jenetics.smocker.util.io.TeeInputStream;
import com.jenetics.smocker.util.io.TeeOutputStream;
import com.jenetics.smocker.util.network.RestClientSmocker;

public class SmockerContainer {
	private SmockerSocketInputStream smockerSocketInputStream = null;
	private SmockerSocketOutputStream smockerSocketOutputStream = null;
	private String host = null;
	private int port = 0;
	private boolean ssl;
	private TeeInputStream teeInputStream;
	private TeeOutputStream teeOutputStream;
	private String stackTrace;
	private String responseMocked; 
	
	private String outputToBesend;
	private String inputToBesend;
	private Object source;
	private boolean postAtNextRead = false;
	
	public String getOutputToBesend() {
		return outputToBesend;
	}


	public void setOutputToBesend(String outputToBesend) {
		this.outputToBesend = outputToBesend;
	}


	public String getInputToBesend() {
		return inputToBesend;
	}


	public void setInputToBesend(String inputToBesend) {
		this.inputToBesend = inputToBesend;
	}


	public String getStackTrace() {
		return stackTrace;
	}


	public String getHost() {
		return host;
	}


	public int getPort() {
		return port;
	}


	public SmockerContainer(String host, int port, String stackTrace, Object source) {
		this.host = host;
		this.port = port;
		this.stackTrace = stackTrace;
		this.source = source;
	}


	public SmockerContainer(SmockerSocketInputStream smockerSocketInputStream,
			SmockerSocketOutputStream smockerSocketOutputStream) {
		super();
		this.smockerSocketInputStream = smockerSocketInputStream;
		this.smockerSocketOutputStream = smockerSocketOutputStream;
	}


	public Object getSource() {
		return source;
	}


	public SmockerSocketInputStream getSmockerSocketInputStream() {
		return smockerSocketInputStream;
	}


	public void setSmockerSocketInputStream(SmockerSocketInputStream smockerSocketInputStream) {
		this.smockerSocketInputStream = smockerSocketInputStream;
	}


	public SmockerSocketOutputStream getSmockerSocketOutputStream() {
		return smockerSocketOutputStream;
	}


	public void setSmockerSocketOutputStream(SmockerSocketOutputStream smockerSocketOutputStream) {
		this.smockerSocketOutputStream = smockerSocketOutputStream;
	}


	public boolean isSsl() {
		// TODO Auto-generated method stub
		return ssl;
	}

	public String getResponseMocked() {
		return responseMocked;
	}


	public void setResponseMocked(String responseMocked) {
		this.responseMocked = responseMocked;
	}


	public TeeInputStream getTeeInputStream() {
		return teeInputStream;
	}


	public void setTeeInputStream(TeeInputStream teeInputStream) {
		this.teeInputStream = teeInputStream;
	}


	public TeeOutputStream getTeeOutputStream() {
		return teeOutputStream;
	}


	public void setTeeOutputStream(TeeOutputStream teeOutputStream) {
		this.teeOutputStream = teeOutputStream;
	}
	
	public boolean isPostAtNextRead() {
		return postAtNextRead;
	}


	public void setPostAtNextRead(boolean postAtNextRead) {
		this.postAtNextRead = postAtNextRead;
	}


	public void postCommunication() throws UnsupportedEncodingException {
		String lastReaden = null;
		if (getSmockerSocketInputStream() != null) {
			lastReaden = getSmockerSocketInputStream().getSmockerOutputStreamData().getString();
		}
		
		if (lastReaden != null && !lastReaden.isEmpty() &&  getOutputToBesend() != null) {
			RestClientSmocker.getInstance().postCommunication(this, getOutputToBesend(), lastReaden);
		}
	}
	
	
}
