package com.jenetics.smocker.ui.util;

import com.jenetics.smocker.model.Communication;

public class TTreeItem<T> {
	private Communication communication;
	
	private T inner;

	public TTreeItem(T inner) {
		this.inner = inner;
	}

	@Override
	public String toString() {
		return communication.getDateTime().toString();
	}

	public T getInner() {
		return inner;
	}
}
