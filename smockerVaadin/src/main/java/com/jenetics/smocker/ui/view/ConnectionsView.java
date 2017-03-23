package com.jenetics.smocker.ui.view;

import com.jenetics.smocker.annotation.ContentView;
import com.jenetics.smocker.model.Connection;
import com.jenetics.smocker.ui.SmockerUI;
import com.jenetics.smocker.ui.util.RefreshableView;
import com.vaadin.addon.jpacontainer.JPAContainer;
import com.vaadin.addon.jpacontainer.JPAContainerFactory;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

@ContentView (viewName = "Connections")
public class ConnectionsView extends VerticalLayout implements RefreshableView {

	JPAContainer<Connection> connections = null;
	private Table connectionTable;
	
	
	@Override
	public void enter(ViewChangeEvent event) {
		// TODO Auto-generated method stub
		
		connections = JPAContainerFactory.make(Connection.class,
	            SmockerUI.PERSISTENCE_UNIT);
		connectionTable = new Table(null, connections);
        connectionTable.setSelectable(true);
        connectionTable.setSizeFull();
        setSizeFull();
        addComponent(connectionTable);
	}
	
	@Override
	public void refresh() {
		connections.refresh();
	}
}
