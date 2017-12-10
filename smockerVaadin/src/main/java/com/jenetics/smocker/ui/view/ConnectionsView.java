package com.jenetics.smocker.ui.view;

import java.util.ArrayList;
import java.util.List;

import org.vaadin.easyapp.util.ButtonDescriptor;

import com.jenetics.smocker.model.Connection;
import com.jenetics.smocker.model.EntityWithId;
import com.jenetics.smocker.ui.SmockerUI;
import com.jenetics.smocker.ui.util.RefreshableView;
import com.vaadin.addon.jpacontainer.JPAContainer;
import com.vaadin.addon.jpacontainer.JPAContainerFactory;
import com.vaadin.annotations.Push;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.FontAwesome;
import com.vaadin.spring.annotation.ViewScope;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

@Push
@ViewScope
// @ContentView(sortingOrder=2, viewName = "Connections", icon =
// "icons/Places-network-server-database-icon.png",
// rootViewParent=ConnectionsRoot.class)
public class ConnectionsView extends VerticalLayout implements RefreshableView {

	private static final long serialVersionUID = 1L;

	JPAContainer<Connection> connections = null;
	private Table connectionTable;

	public ConnectionsView() {

		setMargin(true);
		connections = JPAContainerFactory.make(Connection.class, SmockerUI.PERSISTENCE_UNIT);
		connectionTable = new Table(null, connections);
		connectionTable.setSelectable(true);
		connectionTable.setSizeFull();
		connectionTable.setImmediate(true);

		connectionTable.addGeneratedColumn("Watch", (source, itemId, columnId) -> {
			Button button = new Button("Test");
			button.setIcon(FontAwesome.GLOBE);
			return button;
		});
		setSizeFull();
		addComponent(connectionTable);
		setImmediate(true);
	}


	@Override
	public void enter(ViewChangeEvent event) {
		//do nothing
	}

	@Override
	public void refresh(EntityWithId entityWithId) {
		connections.refresh();
	}

	@Override
	public ClickListener getClickListener(String key) {
		return null;
	}

	@Override
	public List<ButtonDescriptor> getButtons() {
		return new ArrayList<>();
	}

	@Override
	public boolean isClickable(String key) {
		return false;
	}
}
