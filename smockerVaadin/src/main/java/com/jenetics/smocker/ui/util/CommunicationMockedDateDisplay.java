package com.jenetics.smocker.ui.util;

import com.jenetics.smocker.model.CommunicationMocked;

public class CommunicationMockedDateDisplay {
	private CommunicationMocked communicationMocked;

	public CommunicationMockedDateDisplay(CommunicationMocked communicationMocked) {
		super();
		this.communicationMocked = communicationMocked;
	}

	@Override
	public String toString() {
		return communicationMocked.getDateTime().toString();
	}

	public CommunicationMocked getCommunication() {
		return communicationMocked;
	}
}
