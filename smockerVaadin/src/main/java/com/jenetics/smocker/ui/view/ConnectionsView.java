package com.jenetics.smocker.ui.view;

import org.vaadin.easyapp.util.annotations.ContentView;

import com.jenetics.smocker.model.Connection;
import com.jenetics.smocker.model.EntityWithId;
import com.jenetics.smocker.ui.SmockerUI;
import com.jenetics.smocker.ui.util.EventManager;
import com.jenetics.smocker.ui.util.RefreshableView;
import com.vaadin.addon.jpacontainer.JPAContainer;
import com.vaadin.addon.jpacontainer.JPAContainerFactory;
import com.vaadin.annotations.Push;
import com.vaadin.data.Item;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.FontAwesome;
import com.vaadin.spring.annotation.ViewScope;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

@Push
@ViewScope
@ContentView(sortingOrder=2, viewName = "Connections", icon = "icons/Places-network-server-database-icon.png", rootViewParent=ConnectionsRoot.class)
public class ConnectionsView extends VerticalLayout implements RefreshableView {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ConnectionsView() {

		setMargin(true);
		connections = JPAContainerFactory.make(Connection.class, SmockerUI.PERSISTENCE_UNIT);
		connectionTable = new Table(null, connections);
		connectionTable.setSelectable(true);
		connectionTable.setSizeFull();
		connectionTable.setImmediate(true);
		
		connectionTable.addGeneratedColumn("Watch", new Table.ColumnGenerator() {
            public Component generateCell(Table source, Object itemId,
                    Object columnId) {
                //Item item = connectionTable.getItem(itemId);
                System.out.println(itemId);
            	Button button = new Button("Test");
                button.setIcon(FontAwesome.GLOBE);
                return button;
            }
        });
		setSizeFull();
		addComponent(connectionTable);
		setImmediate(true);
	}

	JPAContainer<Connection> connections = null;
	private Table connectionTable;

	@Override
	public void enter(ViewChangeEvent event) {

	}

	@Override
	public void refresh(EntityWithId entityWithId) {
		connections.refresh();
	}
}
