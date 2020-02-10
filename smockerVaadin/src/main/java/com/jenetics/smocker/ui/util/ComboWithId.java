package com.jenetics.smocker.ui.util;

import com.jenetics.smocker.model.EntityWithId;
import com.vaadin.ui.ComboBox;

public class ComboWithId<T extends EntityWithId> extends ComboBox {

	private String uiId = null;
	private T entity = null;

	public ComboWithId(String title, String uiId, T entity) {
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((entity == null) ? 0 : entity.hashCode());
		result = prime * result + ((uiId == null) ? 0 : uiId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		ComboWithId other = (ComboWithId) obj;
		if (entity == null) {
			if (other.entity != null)
				return false;
		} 
		else if (!entity.equals(other.entity))
			return false;
		if (uiId == null) {
			if (other.uiId != null)
				return false;
		} 
		else if (!uiId.equals(other.uiId))
			return false;
		return true;
	}
	
	

}
