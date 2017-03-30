package com.jenetics.smocker.ui.util;

import com.vaadin.ui.Button;

public class ButtonWithOrderId extends Button {
	private int Sortingorder = 0;
	private boolean homeView = false;

	public ButtonWithOrderId(int order, String name, boolean homeView) {
		super(name);
		this.Sortingorder = order;
		this.homeView = homeView;
	}


	public int getSortingOrder() {
		return Sortingorder;
	}


	public boolean isHomeView() {
		return homeView;
	}
	
}
