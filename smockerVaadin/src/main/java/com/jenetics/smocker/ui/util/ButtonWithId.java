package com.jenetics.smocker.ui.util;

import com.jenetics.smocker.model.EntityWithId;
import com.vaadin.ui.Button;

public class ButtonWithId<T extends EntityWithId> extends Button {

	private String uiId = null;
	private T entity = null;

	public ButtonWithId() {
		super();
	}

	public ButtonWithId(String uiId, T entity) {
		super();
		this.uiId = uiId;
		this.entity = entity;
	}

	public T getEntity() {
		return entity;
	}

	public void setEntity(T entity) {
		this.entity = entity;
	}

	public String getUiId() {
		return uiId;
	}

	public void setUiId(String uiId) {
		this.uiId = uiId;
	}
	
	

}
