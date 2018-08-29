package com.jenetics.smocker.ui.util;

import com.jenetics.smocker.model.CommunicationMocked;

public class TreeGridMockedItem {
	private boolean root;
	private String name;
	private CommunicationMocked communicationMocked;
	private boolean scenario;
	
	public TreeGridMockedItem(boolean root, boolean scenario, String name, CommunicationMocked communicationMocked) {
		super();
		this.root = root;
		this.scenario = scenario;
		this.name = name;
		this.communicationMocked = communicationMocked;
	}
	
	public boolean isRoot() {
		return root;
	}
	public String getName() {
		return name;
	}
	public CommunicationMocked getCommunication() {
		return communicationMocked;
	}
}
