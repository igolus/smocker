package com.jenetics.smocker.ui.view;

import java.util.Set;

import org.vaadin.easyapp.ui.ViewWithToolBar;
import org.vaadin.easyapp.util.ActionContainer;
import org.vaadin.easyapp.util.ActionContainerBuilder;
import org.vaadin.easyapp.util.ActionContainer.InsertPosition;
import org.vaadin.easyapp.util.annotations.ContentView;

import com.jenetics.smocker.model.CommunicationMocked;
import com.jenetics.smocker.model.Connection;
import com.jenetics.smocker.model.ConnectionMocked;
import com.jenetics.smocker.model.EntityWithId;
import com.jenetics.smocker.model.JavaApplication;
import com.jenetics.smocker.model.JavaApplicationMocked;
import com.jenetics.smocker.ui.SmockerUI;
import com.jenetics.smocker.ui.component.ConnectionMockedDetailsView;
import com.jenetics.smocker.ui.dialog.Dialog;
import com.jenetics.smocker.ui.util.RefreshableView;
import com.jenetics.smocker.ui.util.StrandardTreeGridConnectionMockedData;
import com.jenetics.smocker.ui.util.TreeGridConnectionData;
import com.vaadin.annotations.Push;
import com.vaadin.event.selection.SelectionEvent;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.spring.annotation.ViewScope;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Window;

@SuppressWarnings("serial")
@Push
@ViewScope
@ContentView(sortingOrder = 1, viewName = "Mock View", icon = "icons/Java-icon.png", homeView = true, rootViewParent = ConnectionsRoot.class)
public class MockSpaceView extends AbstractConnectionTreeView2<JavaApplicationMocked, ConnectionMocked, CommunicationMocked> implements RefreshableView {

	@Override
	public void enterInView(ViewChangeEvent event) {
		fillTreeTable();
	}

	private static final String BUNDLE_NAME = "BundleUI";

	public MockSpaceView() {
		super(JavaApplicationMocked.class, ConnectionMocked.class, CommunicationMocked.class);
		treeGrid.addSelectionListener(this::treeSelectionChange);
	}
	
	@Override
	protected Set<ConnectionMocked> getJavaAppConnections(JavaApplicationMocked javaApplication) {
		return javaApplication.getConnections();
	}

	@Override
	protected JavaApplicationMocked getJavaAppFromConnection(ConnectionMocked connection) {
		return connection.getJavaApplication();
	}

	@Override
	protected ConnectionMocked getConnectionFromCommunication(CommunicationMocked comm) {
		return comm.getConnection();
	}

	@Override
	protected TreeGridConnectionData<JavaApplicationMocked, ConnectionMocked> createTreeGridFromJavaApplication(
			JavaApplicationMocked javaApplication) {
		return new StrandardTreeGridConnectionMockedData(javaApplication, null);
	}

	@Override
	protected TreeGridConnectionData<JavaApplicationMocked, ConnectionMocked> createTreeGridFromJConnection(ConnectionMocked connection) {
		return new StrandardTreeGridConnectionMockedData(null, connection);
	}

	@Override
	protected void addTreeMapping() {
		treeGrid.addColumn(item -> item.getApplication()).setCaption(APPLICATION);
		treeGrid.addColumn(item -> item.getAdress()).setCaption(ADRESS);
		treeGrid.addColumn(item -> item.getPort()).setCaption(PORT);
		treeGrid.addColumn(item -> item.getConnectionType()).setCaption(CONNECTION_TYPE);
	}
	
	public void treeSelectionChange(SelectionEvent<TreeGridConnectionData<JavaApplicationMocked, ConnectionMocked>> event) {
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
						, this::clean, org.vaadin.easyapp.util.ActionContainer.Position.LEFT, InsertPosition.AFTER)
				.addButton("ViewDetails_Button", VaadinIcons.EYE, null,  this::isConnectionSelected			
						, this::details, org.vaadin.easyapp.util.ActionContainer.Position.LEFT, InsertPosition.AFTER)
				.addButton("Refresh_Button", VaadinIcons.REFRESH, null,  this::always			
						, this::refresh, org.vaadin.easyapp.util.ActionContainer.Position.LEFT, InsertPosition.AFTER)
				;

		return builder.build();
	}
	
	public void clean(ClickEvent event) {
		if (isSelected()) {
			Dialog.ask(SmockerUI.getBundle().getString("RemoveQuestion"), null, this::delete, null);
		}
	}
	
	public void delete() {
		Set<TreeGridConnectionData<JavaApplicationMocked, ConnectionMocked>> selectedItems = treeGrid.getSelectedItems();
		for (TreeGridConnectionData<JavaApplicationMocked, ConnectionMocked> treeGridConnectionData : selectedItems) {
			if (treeGridConnectionData.isConnection()) {
				ConnectionMocked selectedConnection = treeGridConnectionData.getConnection();
				selectedConnection.getJavaApplication().getConnections().remove(selectedConnection);
				daoManagerJavaApplication.update(selectedConnection.getJavaApplication());
			}
			else if (treeGridConnectionData.isJavaApplication()) {
				JavaApplicationMocked selectedJavaApplication = treeGridConnectionData.getJavaApplication();
				daoManagerJavaApplication.deleteById(selectedJavaApplication.getId());
			}
			fillTreeTable();
		}
	}
	
	public void details(ClickEvent event) {
		if (isConnectionSelected()) {
			ConnectionMocked conn = treeGrid.getSelectedItems().iterator().next().getConnection();
			ConnectionMockedDetailsView connectionWithDetail = new ConnectionMockedDetailsView(conn);
			ViewWithToolBar view = new ViewWithToolBar(connectionWithDetail);
			connectionWithDetail.setSubWindow(SmockerUI.displayInSubWindow(bundle.getString("MockedCommunications"), view));
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
