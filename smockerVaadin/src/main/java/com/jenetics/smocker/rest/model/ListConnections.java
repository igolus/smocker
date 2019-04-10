package com.jenetics.smocker.rest.model;

import java.util.HashMap;
import java.util.Map;

public class ListConnections {
	private Map<String, Long> connectionMap = new HashMap<>();

	public ListConnections() {
		super();
	}
	
	public void addConnection(String host, int port, long id) {
		connectionMap.put(host + ":" + port, id);
	}

	public Map<String, Long> getListConnections() {
		return connectionMap;
	}
	
}
