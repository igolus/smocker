package com.jenetics.smocker.ui.util;

public class ViewAndIconContainer {
	private RefreshableView refreshableView;
	private String icon;
	
	public ViewAndIconContainer(RefreshableView refreshableView, String icon) {
		super();
		this.refreshableView = refreshableView;
		this.icon = icon;
	}

	public RefreshableView getRefreshableView() {
		return refreshableView;
	}

	public String getIcon() {
		return icon;
	}
	
	
}
