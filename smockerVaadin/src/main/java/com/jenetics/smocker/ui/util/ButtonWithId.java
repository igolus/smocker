package com.jenetics.smocker.ui.util;

import com.jenetics.smocker.model.EntityWithId;
import com.vaadin.ui.Button;

public class ButtonWithId<T extends EntityWithId> extends Button {

	private String UiId = null;
	private T entity = null;
	
	public T getEntity() {
		return entity;
	}

	public void setEntity(T entity) {
		this.entity = entity;
	}

	public ButtonWithId(String uiId, T entity ) {
		super();
		UiId = uiId;
		this.entity = entity;
	}

	public ButtonWithId() {
		super();
		// TODO Auto-generated constructor stub
	}


	public String getUiId() {
		return UiId;
	}

	public void setUiId(String uiId) {
		UiId = uiId;
	}
	
}
