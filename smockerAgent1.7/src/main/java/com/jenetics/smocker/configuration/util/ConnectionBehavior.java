package com.jenetics.smocker.configuration.util;

public class ConnectionBehavior {

	private boolean watched;
	
	public ConnectionBehavior(boolean watched) {
		super();
		this.watched = watched;
	}

	public boolean isWatched() {
		return watched;
	}

	public void setWatched(boolean watched) {
		this.watched = watched;
	}
	
	
	
}
