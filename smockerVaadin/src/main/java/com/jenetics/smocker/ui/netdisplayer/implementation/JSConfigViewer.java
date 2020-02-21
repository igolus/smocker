package com.jenetics.smocker.ui.netdisplayer.implementation;

import java.util.logging.Level;

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
import com.vaadin.ui.TabSheet.SelectedTabChangeEvent;

public class JSConfigViewer implements ComponentWithDisplayChange {

	private String defaultTitle;

	private TabSheet tabsheet = new TabSheet();
	private TextArea areaOutput;
	private TextArea areaOutputJsDisplay;

	private JsFilterAndDisplay jsDisplayAndFilter;

	private boolean input;

	private String content;

	private String formattedDisplay;

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
		tabsheet.addSelectedTabChangeListener(this::tabbedChanged);

		tabsheet.setSizeFull();
		return tabsheet;
	}

	private void tabbedChanged(SelectedTabChangeEvent event) {
		//format the display only once
		SmockerUI.getCurrent().access(() -> {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			try {
				if (formattedDisplay != null) {
					return;
				}
				Component selected = tabsheet.getSelectedTab();
				if (areaOutputJsDisplay == selected) {
					String functionDisplay = null;
					if (input) {
						functionDisplay = jsDisplayAndFilter.getFunctionInputDisplay();
					}
					else {
						functionDisplay = jsDisplayAndFilter.getFunctionOutputDisplay();
					}
					formattedDisplay = JSEvaluator.formatAndDisplay(functionDisplay, content);
					areaOutputJsDisplay.setValue(formattedDisplay);
				}
			} catch (Throwable e) {
				//display error in tab
				areaOutputJsDisplay.setValue(SmockerUtility.getStackTrace(e));
			}
		});
	}

	@Override
	public void selectionValue(String content) {
		SmockerUI.doInBackGround(() -> {
			this.content = content;
			areaOutput.setValue(content);
		}, 
		100);
	}

}
