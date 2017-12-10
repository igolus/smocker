package com.jenetics.smocker.ui.util;

import com.jenetics.smocker.model.CommunicationMocked;

public class CommunicationMockedTreeItem {

	private CommunicationMocked communication;

	public CommunicationMockedTreeItem(CommunicationMocked communication) {
		this.communication = communication;
	}

	@Override
	public String toString() {
		return communication.getDateTime().toString();
	}

	public CommunicationMocked getCommunication() {
		return communication;
	}

}
