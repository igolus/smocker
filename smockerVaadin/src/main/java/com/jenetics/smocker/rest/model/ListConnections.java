package com.jenetics.smocker.rest.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ListConnections {
	private Map<String, Long> listConnections = new HashMap<>();

	public ListConnections() {
		super();
	}
	
	public void addConnection(String host, int port, long id) {
		listConnections.put(host + ":" + port, id);
	}

	public Map<String, Long> getListConnections() {
		return listConnections;
	}
	
}
