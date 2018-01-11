package com.jenetics.smocker.ui.util;

import com.jenetics.smocker.model.EntityWithId;
import com.vaadin.ui.Button;
import com.vaadin.ui.ListSelect;

public class ListSelectWithId<T extends EntityWithId> extends ListSelect {

	private String uiId = null;
	private T entity = null;

	public ListSelectWithId() {
		super();
	}

	public ListSelectWithId(String title, String uiId, T entity) {
		super(title);
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
