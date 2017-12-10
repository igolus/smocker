package com.jenetics.smocker.ui.netdisplayer.implementation;

import com.jenetics.smocker.ui.netdisplayer.ComponentWithDisplayChange;
import com.vaadin.ui.Component;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TextArea;

public class DefaultViewer implements ComponentWithDisplayChange {

	private String defaultTitle;
	private TabSheet tabsheet = new TabSheet();
	private TextArea areaOutput;

	public DefaultViewer(String defaultTitle) {
		this.defaultTitle = defaultTitle;
	}


	@Override
	public Component getComponent() {
		areaOutput = new TextArea();
		areaOutput.setReadOnly(true);
		areaOutput.setSizeFull();

		tabsheet.addTab(areaOutput, defaultTitle);

		tabsheet.setSizeFull();
		return tabsheet;
	}

	@Override
	public void selectionValue(String content) {
		areaOutput.setReadOnly(false);
		areaOutput.setValue(content);
		areaOutput.setReadOnly(true);
	}
}
