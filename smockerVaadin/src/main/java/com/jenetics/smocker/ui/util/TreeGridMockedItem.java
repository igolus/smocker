package com.jenetics.smocker.ui.util;

import com.jenetics.smocker.model.CommunicationMocked;

public class TreeGridMockedItem {
	private boolean root;
	private String name;
	private CommunicationMocked communicationMocked;
	
	public TreeGridMockedItem(boolean root, String name, CommunicationMocked communicationMocked) {
		super();
		this.root = root;
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
		// TODO Auto-generated method stub
		return communicationMocked;
	}
}
