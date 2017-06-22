package com.jenetics.smocker.ui.util;

import com.jenetics.smocker.model.Communication;

public class CommunicationTreeItem {

	private Communication communication;

	public CommunicationTreeItem(Communication communication) {
		this.communication = communication;
	}

	@Override
	public String toString() {
		return communication.getDateTime().toString();
	}

	public Communication getCommunication() {
		return communication;
	}
	
	

}
