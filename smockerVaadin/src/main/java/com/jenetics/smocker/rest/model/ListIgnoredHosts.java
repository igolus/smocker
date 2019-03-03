package com.jenetics.smocker.rest.model;

import java.util.ArrayList;
import java.util.List;

public class ListIgnoredHosts {
	private List<String> listIgnoredList = null;

	public ListIgnoredHosts(List<String> listIgnoredList) {
		super();
		this.listIgnoredList = listIgnoredList;
	}
	
	public ListIgnoredHosts() {
		super();
		this.listIgnoredList = new ArrayList<>();
	}
	
	public void addIgnoredHost(String host) {
		listIgnoredList.add(host);
	}

	public List<String> getListIgnoredList() {
		return listIgnoredList;
	}
	
	
	
}
