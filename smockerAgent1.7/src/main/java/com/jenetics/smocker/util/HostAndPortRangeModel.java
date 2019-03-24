package com.jenetics.smocker.util;

public class HostAndPortRangeModel {
	private String host;
	private int minPort;
	private int maxPort;
	
	public HostAndPortRangeModel(String host, int minPort, int maxPort) {
		super();
		this.host = host;
		this.minPort = minPort;
		this.maxPort = maxPort;
	}
	
	public HostAndPortRangeModel(String host) {
		super();
		this.host = host;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getMinPort() {
		return minPort;
	}

	public void setMinPort(int minPort) {
		this.minPort = minPort;
	}

	public int getMaxPort() {
		return maxPort;
	}

	public void setMaxPort(int maxPort) {
		this.maxPort = maxPort;
	}
	
	
	
	
}
