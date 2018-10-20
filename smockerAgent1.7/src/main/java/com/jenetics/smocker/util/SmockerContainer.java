package com.jenetics.smocker.util;

import com.jenetics.smocker.util.io.TeeInputStream;
import com.jenetics.smocker.util.io.TeeOutputStream;

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
	
	public String getStackTrace() {
		return stackTrace;
	}


	public String getHost() {
		return host;
	}


	public int getPort() {
		return port;
	}


	public SmockerContainer(String host, int port, String stackTrace) {
		this.host = host;
		this.port = port;
		this.stackTrace = stackTrace;
	}


	public SmockerContainer(SmockerSocketInputStream smockerSocketInputStream,
			SmockerSocketOutputStream smockerSocketOutputStream) {
		super();
		this.smockerSocketInputStream = smockerSocketInputStream;
		this.smockerSocketOutputStream = smockerSocketOutputStream;
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
	
	
}
