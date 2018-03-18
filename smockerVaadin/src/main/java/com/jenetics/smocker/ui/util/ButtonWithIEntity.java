package com.jenetics.smocker.ui.util;

import com.jenetics.smocker.model.EntityWithId;
import com.vaadin.ui.Button;

public class ButtonWithIEntity<T extends EntityWithId> extends Button {

	private T entity = null;

	public ButtonWithIEntity(T entity) {
		this.entity = entity;
	}

	public T getEntity() {
		return entity;
	}

}
