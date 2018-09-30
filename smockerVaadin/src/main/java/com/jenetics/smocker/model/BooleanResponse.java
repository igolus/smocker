package com.jenetics.smocker.model;

import java.io.Serializable;

public class BooleanResponse implements Serializable {
	
	
	public BooleanResponse(boolean response) {
		super();
		this.response = response;
	}

	private boolean response;

	public boolean isResponse() {
		return response;
	}

	public void setResponse(boolean response) {
		this.response = response;
	}
	
	

}
