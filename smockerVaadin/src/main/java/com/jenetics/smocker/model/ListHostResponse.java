package com.jenetics.smocker.model;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class ListHostResponse implements Serializable {
	
	private Set<String> activatedHosts = new HashSet<>();
	
	public ListHostResponse() {
		super();
	}

	public Set<String> getActivatedHosts() {
		return activatedHosts;
	}

	public void setActivatedHosts(Set<String> activatedHost) {
		this.activatedHosts = activatedHost;
	}
	
}
