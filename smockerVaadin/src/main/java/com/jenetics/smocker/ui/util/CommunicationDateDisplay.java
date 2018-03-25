package com.jenetics.smocker.ui.util;

import com.jenetics.smocker.model.Communication;

public class CommunicationDateDisplay {
	private Communication communication;

	public CommunicationDateDisplay(Communication communication) {
		super();
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
