package com.jenetics.smocker.ui.netdisplayer.implementation;

import java.util.ResourceBundle;

import org.jboss.logging.Logger;

import com.jenetics.smocker.ui.netdisplayer.ComponentWithDisplayChange;
import com.vaadin.ui.Component;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TextArea;

public class DefaultViewer implements ComponentWithDisplayChange {
	
	private String defaultTitle;

	public DefaultViewer(String defaultTitle) {
		this.defaultTitle = defaultTitle;
	}

	private static ResourceBundle bundle = ResourceBundle.getBundle("BundleUI");
	private TabSheet tabsheet = new TabSheet();
	private TextArea areaOutput;

	private Logger logger = Logger.getLogger(DefaultViewer.class);
	
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
