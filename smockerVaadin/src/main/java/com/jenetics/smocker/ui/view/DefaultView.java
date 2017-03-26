package com.jenetics.smocker.ui.view;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.jenetics.smocker.annotation.ContentView;
import com.jenetics.smocker.ui.util.RefreshableView;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.UIScope;
import com.vaadin.spring.annotation.ViewScope;
import com.vaadin.ui.VerticalLayout;

@ViewScope
@ContentView (viewName = "", icon = "icons/home-icon.png")
public class DefaultView extends VerticalLayout implements RefreshableView {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public void enter(ViewChangeEvent event) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void refresh() {
		// TODO Auto-generated method stub
		
	}

}
