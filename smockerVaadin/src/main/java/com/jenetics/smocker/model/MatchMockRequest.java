package com.jenetics.smocker.model;

public class MatchMockRequest {
	private String request;
	private String host;
	
	
	private MatchMockRequest() {
		super();
	}

	public MatchMockRequest(String request, String host) {
		super();
		this.request = request;
		this.host = host;
	}

	public String getRequest() {
		return request;
	}

	public void setRequest(String request) {
		this.request = request;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}
}
