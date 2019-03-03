package com.jenetics.smocker.ui.component;

import com.vaadin.ui.Component;

public interface BoxableItem<T> {
	public T getItem();
	public Component getComponent();
}
