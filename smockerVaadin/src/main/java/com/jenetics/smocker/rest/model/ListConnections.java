package com.jenetics.smocker.rest.model;

import java.util.ArrayList;
import java.util.List;

public class ListConnections {
	private List<String> listConnections = new ArrayList<>();

	public ListConnections() {
		super();
	}
	
	public void addConnection(String host, int port) {
		listConnections.add(host + ":" + port);
	}

	public List<String> getListConnections() {
		return listConnections;
	}
	
}
