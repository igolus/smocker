package com.jenetics.smocker.ui.util;

import com.jenetics.smocker.model.CommunicationMocked;
import com.jenetics.smocker.model.Scenario;
import com.jenetics.smocker.ui.SmockerUI;

public class TreeGridMockedItem {

	private CommunicationMocked communicationMocked;
	private Scenario scenario;
	
	public TreeGridMockedItem() {
		super();
	}
	
	public TreeGridMockedItem(CommunicationMocked communicationMocked) {
		super();
		this.communicationMocked = communicationMocked;
	}
	
	public TreeGridMockedItem(Scenario scenario) {
		super();
		this.scenario = scenario;
	}
	
	public boolean isRoot() {
		return scenario == null && communicationMocked == null;
	}
	
	public boolean isScenario() {
		return scenario != null;
	}
	
	public boolean isCommunication() {
		return communicationMocked != null;
	}
	
	public String getDisplay() {
		if (isRoot()) {
			return SmockerUI.getBundleValue("root");
		}
		if (isScenario()) {
			return scenario.getName();
		}
		if (isCommunication()) {
			return getValueElseUnamed(communicationMocked.getName());
		}
		return null;
	}
	
	private String getValueElseUnamed (String name) {
		return name == null ? SmockerUI.getBundleValue("unamed") : name;
	}
	
	public CommunicationMocked getCommunication() {
		return communicationMocked;
	}
	
	public Scenario getScenario() {
		return scenario;
	}
}
