package com.jenetics.smocker.rest.model;

import java.util.ArrayList;
import java.util.List;

import com.jenetics.smocker.ui.util.DuplicateHost;

public class ListDuplicatedHosts {
	private List<DuplicateHost> duplicateHostList = null;

	public ListDuplicatedHosts(List<DuplicateHost> duplicateHostList) {
		super();
		this.duplicateHostList = duplicateHostList;
	}
	
	public ListDuplicatedHosts() {
		super();
		this.duplicateHostList = new ArrayList();
	}
	
	public void addDuplicatedHost (DuplicateHost duplicateHost) {
		duplicateHostList.add(duplicateHost);
	}

	public List<DuplicateHost> getDuplicateHostList() {
		return duplicateHostList;
	}
}
