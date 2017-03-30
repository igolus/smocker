package com.jenetics.smocker.ui.util;

public class ViewAndIconContainer {
	private RefreshableView refreshableView;
	private String icon;
	private int sortingOrder;
	private boolean homeView;
	
	
	public ViewAndIconContainer(RefreshableView refreshableView, String icon, int order, boolean homeView) {
		super();
		this.refreshableView = refreshableView;
		this.icon = icon;
		this.sortingOrder = order;
		this.homeView = homeView;
	}

	public RefreshableView getRefreshableView() {
		return refreshableView;
	}

	public String getIcon() {
		return icon;
	}

	public int getOrder() {
		return sortingOrder;
	}

	public boolean isHomeView() {
		return homeView;
	}
	
	
	
}
