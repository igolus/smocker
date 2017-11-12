package com.jenetics.smocker.ui.view;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.vaadin.easyapp.util.ButtonDescriptor;
import org.vaadin.easyapp.util.annotations.ContentView;

import com.jenetics.smocker.model.EntityWithId;
import com.jenetics.smocker.ui.SmockerUI.EnumButton;
import com.vaadin.annotations.Push;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.FontAwesome;
import com.vaadin.spring.annotation.ViewScope;
import com.vaadin.ui.Button.ClickListener;

@Push
@ViewScope
@ContentView(sortingOrder=1, viewName = "Mock Space", icon = "icons/Java-icon.png", homeView=false, rootViewParent=ConnectionsRoot.class)
public class MockSpaceView extends AbstractConnectionTreeView {

	public MockSpaceView() {
		super();
	}

	@Override
	public ClickListener getClickListener(String key) {
		return null;
	}

	@Override
	public List<ButtonDescriptor> getButtons() {
		return Arrays.asList( new ButtonDescriptor [] {
				new ButtonDescriptor(bundle.getString("remove"), bundle.getString("removeToolTip"), 
						FontAwesome.REMOVE, EnumButton.REMOVE.toString()),
		});
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
	protected void initDao() {
		// TODO Auto-generated method stub
		
	}

	protected Map<String, Class<?>> getColumnMap() {
		Map<String, Class<?>> ret = new HashMap<>();
		ret.put(APPLICATION, String.class);
		ret.put(ADRESS, String.class);
		ret.put(PORT, String.class);
		ret.put(CONNECTION_TYPE, String.class);
		return ret;
	}

	@Override
	protected void addColumnToTreeTable() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void fillTreeTable() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void treeTableItemClicked(ItemClickEvent itemClickEvent) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void updateTree(EntityWithId entityWithId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void refreshEntity(EntityWithId entityWithId) {
		// TODO Auto-generated method stub
		
	}


}
