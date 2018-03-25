package com.jenetics.smocker.ui.view;

import java.util.Set;

import org.vaadin.easyapp.ui.ViewWithToolBar;
import org.vaadin.easyapp.util.ActionContainer;
import org.vaadin.easyapp.util.ActionContainerBuilder;
import org.vaadin.easyapp.util.annotations.ContentView;

import com.jenetics.smocker.model.Communication;
import com.jenetics.smocker.model.Connection;
import com.jenetics.smocker.model.EntityWithId;
import com.jenetics.smocker.model.JavaApplication;
import com.jenetics.smocker.network.ClientCommunicator;
import com.jenetics.smocker.ui.SmockerUI;
import com.jenetics.smocker.ui.component.ConnectionDetailsView;
import com.jenetics.smocker.ui.util.ButtonWithIEntity;
import com.jenetics.smocker.ui.util.RefreshableView;
import com.jenetics.smocker.ui.util.StrandardTreeGridConnectionData;
import com.jenetics.smocker.ui.util.TreeGridConnectionData;
import com.vaadin.annotations.Push;
import com.vaadin.event.selection.SelectionEvent;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.spring.annotation.ViewScope;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Window;

@SuppressWarnings("serial")
@Push
@ViewScope
@ContentView(sortingOrder = 1, viewName = "Java Applications", icon = "icons/Java-icon.png", homeView = true, rootViewParent = ConnectionsRoot.class)
public class JavaApplicationView2 extends AbstractConnectionTreeView2<JavaApplication, Connection, Communication> implements RefreshableView {

	private static final String BUNDLE_NAME = "BundleUI";

	public JavaApplicationView2() {
		super(JavaApplication.class, Connection.class, Communication.class);
		treeGrid.addSelectionListener(this::treeSelectionChange);
	}
	
	@Override
	protected Set<Connection> getJavaAppConnections(JavaApplication javaApplication) {
		return javaApplication.getConnections();
	}

	@Override
	protected JavaApplication getJavaAppFromConnection(Connection connection) {
		return connection.getJavaApplication();
	}

	@Override
	protected Connection getConnectionFromCommunication(Communication comm) {
		return comm.getConnection();
	}

	@Override
	protected TreeGridConnectionData<JavaApplication, Connection> createTreeGridFromJavaApplication(
			JavaApplication javaApplication) {
		return new StrandardTreeGridConnectionData(javaApplication, null);
	}

	@Override
	protected TreeGridConnectionData<JavaApplication, Connection> createTreeGridFromJConnection(Connection connection) {
		return new StrandardTreeGridConnectionData(null, connection);
	}

	@Override
	protected void addTreeMapping() {
		treeGrid.addColumn(item -> item.getApplication()).setCaption(APPLICATION);
		treeGrid.addColumn(item -> item.getAdress()).setCaption(ADRESS);
		treeGrid.addColumn(item -> item.getPort()).setCaption(PORT);
		treeGrid.addColumn(item -> item.getConnectionType()).setCaption(CONNECTION_TYPE);
		treeGrid.addComponentColumn(this::buildWatchButton);
	}
	
	private Button buildWatchButton(TreeGridConnectionData<JavaApplication, Connection> item) {
		if (item.isConnection()) {
			String buttonString;
			Connection connection = item.getConnection();
			if (connection.getWatched() == null || connection.getWatched()) {
				buttonString = bundle.getString("Mute_Button");
			} else {
				buttonString = bundle.getString("Watch_Button");
			}
			ButtonWithIEntity<Connection> buttonWithId = new ButtonWithIEntity<Connection>(connection);
			buttonWithId.setCaption(buttonString);
			buttonWithId.addClickListener(this::watchButtonClicked);
			//buttonByUiId.put(buttonWithId.getUiId(), buttonWithId);
			return buttonWithId;
		}
		return null;
    }
	
	public void watchButtonClicked(ClickEvent event) {
		ButtonWithIEntity<Connection> buttonWithEntity = (ButtonWithIEntity<Connection>) event.getSource();
		if (buttonWithEntity.getEntity().getWatched() == null || !buttonWithEntity.getEntity().getWatched()) {
			buttonWithEntity.setCaption(bundle.getString("Mute_Button"));
			buttonWithEntity.getEntity().setWatched(true);
			buttonWithEntity.setEnabled(false);
			daoManagerConnection.update(buttonWithEntity.getEntity());
			ClientCommunicator.sendWatched(buttonWithEntity.getEntity());
			buttonWithEntity.setEnabled(true);
		} else {
			buttonWithEntity.setCaption(bundle.getString("Watch_Button"));
			buttonWithEntity.getEntity().setWatched(false);
			buttonWithEntity.setEnabled(false);
			daoManagerConnection.update(buttonWithEntity.getEntity());
			ClientCommunicator.sendUnWatched(buttonWithEntity.getEntity());
			buttonWithEntity.setEnabled(true);
		}
	}
	
	
	public void treeSelectionChange(SelectionEvent<TreeGridConnectionData<JavaApplication, Connection>> event) {
		refreshClickable();
	}
	
	@Override
	public void refresh(EntityWithId entityWithId) {
		refreshEntity(entityWithId);
	}
	
	@Override
	public ActionContainer buildActionContainer() {
		ActionContainerBuilder builder = new ActionContainerBuilder(BUNDLE_NAME)
				.addButton("Clean_Button", VaadinIcons.MINUS, null,  this::isSelected			
						, this::clean)
				.addButton("ViewDetails_Button", VaadinIcons.EYE, null,  this::isConnectionSelected			
						, this::details)
				.addButton("Refresh_Button", VaadinIcons.REFRESH, null,  this::always			
						, this::refresh)
				.setSearch(this::search);

		return builder.build();
	}
	
	public void clean(ClickEvent event) {
		Notification.show("Clean");
	}
	
	public void details(ClickEvent event) {
		if (isConnectionSelected()) {
			Connection conn = treeGrid.getSelectedItems().iterator().next().getConnection();
			Window subWindow = new Window(bundle.getString("Communications"));
			subWindow.setModal(true);
			ConnectionDetailsView connectionWithDetail = new ConnectionDetailsView(conn, subWindow);
			ViewWithToolBar view = new ViewWithToolBar(connectionWithDetail);
			subWindow.setContent(view);
			subWindow.center();
			SmockerUI.getInstance().addWindow(subWindow);
			subWindow.setHeight("800px");
			subWindow.setWidth("600px");
			subWindow.setSizeFull();
		}
	}
	
	public boolean isSelected() {
		return treeGrid.getSelectedItems().size() == 1;
	}
	
	public boolean isConnectionSelected() {
		return treeGrid.getSelectedItems().size() == 1 && 
				treeGrid.getSelectedItems().iterator().next().isConnection();
	}
	
	public void search(String searchValue) {
		Notification.show("Search for:" + searchValue);
	}

}
