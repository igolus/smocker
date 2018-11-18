package com.jenetics.smocker.model.config;

import com.vaadin.annotations.PropertyId;

public class ConfigBean {
	
	@PropertyId("Test auto")
	private boolean autoRefresh = true;

	public boolean isAutoRefresh() {
		return autoRefresh;
	}

	public void setAutoRefresh(boolean autoRefresh) {
		this.autoRefresh = autoRefresh;
	}
}
