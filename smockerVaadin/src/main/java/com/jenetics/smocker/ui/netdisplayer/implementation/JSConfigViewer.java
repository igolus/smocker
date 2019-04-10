package com.jenetics.smocker.ui.netdisplayer.implementation;

import com.jenetics.smocker.jseval.JSEvaluator;
import com.jenetics.smocker.model.config.JsFilterAndDisplay;
import com.jenetics.smocker.ui.SmockerUI;
import com.jenetics.smocker.ui.netdisplayer.ComponentWithDisplayChange;
import com.jenetics.smocker.util.SmockerException;
import com.jenetics.smocker.util.SmockerUtility;
import com.vaadin.ui.Component;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.VerticalLayout;

public class JSConfigViewer implements ComponentWithDisplayChange {

	private String defaultTitle;

	private TabSheet tabsheet = new TabSheet();
	private TextArea areaOutput;
	private TextArea areaOutputJsDisplay;

	private JsFilterAndDisplay jsDisplayAndFilter;

	private boolean input;

	public JSConfigViewer(String defaultTitle, JsFilterAndDisplay jsDisplayAndFilter, boolean input) {
		super();
		this.jsDisplayAndFilter = jsDisplayAndFilter;
		this.defaultTitle = defaultTitle;
		this.input = input;
	}


	@Override
	public Component getComponent() {
		areaOutput = new TextArea();
		areaOutput.setWordWrap(false);
		areaOutput.setReadOnly(true);
		areaOutput.setSizeFull();

		areaOutputJsDisplay = new TextArea();
		areaOutputJsDisplay.setWordWrap(false);
		areaOutputJsDisplay.setReadOnly(true);
		areaOutputJsDisplay.setSizeFull();

		tabsheet.addTab(areaOutput, defaultTitle);
		VerticalLayout vlayout = new VerticalLayout();
		vlayout.addComponent(areaOutputJsDisplay);
		tabsheet.addTab(areaOutputJsDisplay, SmockerUI.getBundleValue("jsDisplay"));

		tabsheet.setSizeFull();
		return tabsheet;
	}

	@Override
	public void selectionValue(String content) {

		areaOutput.setValue(content);
		String functionDisplay = null;
		if (input) {
			functionDisplay = jsDisplayAndFilter.getFunctionInputDisplay();
		}
		else {
			functionDisplay = jsDisplayAndFilter.getFunctionOutputDisplay();
		}
		
		String formattedDisplay;
		try {
			formattedDisplay = JSEvaluator.formatAndDisplay(functionDisplay, content);
			areaOutputJsDisplay.setValue(formattedDisplay);
		} catch (SmockerException e) {
			areaOutputJsDisplay.setValue(SmockerUtility.getStackTrace(e));
		}
		
	}

}
