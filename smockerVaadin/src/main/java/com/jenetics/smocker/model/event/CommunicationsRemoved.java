package com.jenetics.smocker.model.event;

import java.util.ArrayList;
import java.util.List;

import com.jenetics.smocker.model.Communication;

public class CommunicationsRemoved {
	private List<Communication> commList = new ArrayList<Communication>();

	public CommunicationsRemoved(List<Communication> commList) {
		super();
		this.commList = commList;
	}

	public List<Communication> getCommList() {
		return commList;
	}
}
