package com.jenetics.smocker.ui.util;

import org.vaadin.teemu.switchui.Switch;

import com.jenetics.smocker.model.EntityWithId;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;

public class SwitchWithEntity<T extends EntityWithId> extends Switch  {

	private T entity = null;

	public SwitchWithEntity(T entity) {
		this.entity = entity;
	}

	public T getEntity() {
		return entity;
	}

}
