package com.jenetics.smocker.configuration.util;

public class ConnectionMockedBehavior {

	private ConnectionBehavior mode;
	
	public ConnectionMockedBehavior(ConnectionBehavior mode) {
		super();
		this.mode = mode;
	}

	public ConnectionBehavior getMode() {
		return mode;
	}

	public void setMode(ConnectionBehavior mode) {
		this.mode = mode;
	}
}
