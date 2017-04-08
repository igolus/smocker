package com.jenetics.smocker.ui.view;

import com.jenetics.smocker.annotation.ContentView;
import com.jenetics.smocker.model.EntityWithId;
import com.jenetics.smocker.ui.util.RefreshableView;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.spring.annotation.ViewScope;
import com.vaadin.ui.VerticalLayout;

@ViewScope
@ContentView (sortingOrder=0, homeView=true, viewName = "Dashboard", icon = "icons/home-icon.png" , accordeonParent="Home")
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
	public void refresh(EntityWithId item) {
		// TODO Auto-generated method stub
		
	}

}
