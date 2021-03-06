package com.jenetics.smocker.ui.component;

import org.vaadin.easyapp.util.EasyAppLayout;

public abstract class AbstractConnectionDetails extends EasyAppLayout {

	protected transient Runnable refreshClickable;

	public void setRefreshClickableAction(Runnable refreshClickable) {
		this.refreshClickable = refreshClickable;
	}

	public abstract void refresh();
}
