package com.jenetics.smocker.ui.view;

import java.util.List;

import org.vaadin.easyapp.util.ButtonDescriptor;
import org.vaadin.easyapp.util.annotations.ContentView;

import com.jenetics.smocker.model.EntityWithId;
import com.jenetics.smocker.ui.util.RefreshableView;
import com.vaadin.annotations.Push;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.spring.annotation.ViewScope;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.VerticalSplitPanel;

@Push
@ViewScope
@ContentView(sortingOrder=1, viewName = "Mock Space", icon = "icons/Java-icon.png", homeView=false, rootViewParent=ConnectionsRoot.class)
public class MockSpaceView extends VerticalSplitPanel implements RefreshableView {

	public MockSpaceView() {
		super();
		VerticalLayout mainLayout = new VerticalLayout();
		setSizeFull();
	}
	
	@Override
	public ClickListener getClickListener(String key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<ButtonDescriptor> getButtons() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isClickable(String key) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void enter(ViewChangeEvent event) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void refresh(EntityWithId entityWithId) {
		// TODO Auto-generated method stub
		
	}

}
