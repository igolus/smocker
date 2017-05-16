package com.jenetics.smocker.ui.util;

import com.vaadin.server.Sizeable.Unit;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalSplitPanel;

public class VerticalSplitWithButton extends VerticalSplitPanel{

	private HorizontalLayout buttonLayout;

	public VerticalSplitWithButton() {
		super();
		buttonLayout = new HorizontalLayout();
		setFirstComponent(buttonLayout);
		setSplitPosition(50, Unit.PIXELS);
		setLocked(true);
		addStyleName("mainramhsplitpanel"); 
	}
	
	public VerticalSplitWithButton(Component downComponent) {
		super();
		buttonLayout = new HorizontalLayout();
		setFirstComponent(buttonLayout);
		setSecondComponent(downComponent);
		addStyleName("mainramhsplitpanel"); 
	}
	
	public void addButton(Button but) {
		buttonLayout.addComponent(but);
		but.addStyleName("spacey-button");
		buttonLayout.setComponentAlignment(but, Alignment.MIDDLE_LEFT);
	}
	
}
