package com.jenetics.smocker.rest.container;

import com.jenetics.smocker.model.Communication;

public class AddCommunicationContainer {
	private Long javaApplicationId;
	private Long connectionId;
	private Communication comm;
	
	
	public AddCommunicationContainer(Long javaApplicationId, Long connectionId, Communication comm) {
		super();
		this.javaApplicationId = javaApplicationId;
		this.connectionId = connectionId;
		this.comm = comm;
	}


	public Long getJavaApplicationId() {
		return javaApplicationId;
	}


	public void setJavaApplicationId(Long javaApplicationId) {
		this.javaApplicationId = javaApplicationId;
	}


	public Long getConnectionId() {
		return connectionId;
	}


	public void setConnectionId(Long connectionId) {
		this.connectionId = connectionId;
	}


	public Communication getComm() {
		return comm;
	}


	public void setComm(Communication comm) {
		this.comm = comm;
	}
	
}
