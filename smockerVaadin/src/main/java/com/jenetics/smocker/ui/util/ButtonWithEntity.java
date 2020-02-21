package com.jenetics.smocker.ui.util;

import com.jenetics.smocker.model.EntityWithId;
import com.vaadin.ui.Button;

public class ButtonWithEntity<T extends EntityWithId> extends Button {

	private T entity = null;

	public ButtonWithEntity(T entity) {
		this.entity = entity;
	}

	public T getEntity() {
		return entity;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((entity == null) ? 0 : entity.hashCode());
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
		ButtonWithEntity other = (ButtonWithEntity) obj;
		if (entity == null) {
			if (other.entity != null)
				return false;
		} else if (!entity.equals(other.entity)) {
			return false;
		}
		return true;
	}
	
	

}
