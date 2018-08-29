package com.jenetics.smocker.ui.view;

import java.util.Set;

import org.vaadin.easyapp.ui.ViewWithToolBar;
import org.vaadin.easyapp.util.ActionContainer;
import org.vaadin.easyapp.util.ActionContainerBuilder;
import org.vaadin.easyapp.util.EasyAppLayout;
import org.vaadin.easyapp.util.EasyAppView;
import org.vaadin.easyapp.util.ActionContainer.InsertPosition;
import org.vaadin.easyapp.util.annotations.ContentView;

import com.jenetics.smocker.dao.DaoManagerByModel;
import com.jenetics.smocker.dao.IDaoManager;
import com.jenetics.smocker.model.CommunicationMocked;
import com.jenetics.smocker.model.Connection;
import com.jenetics.smocker.model.ConnectionMocked;
import com.jenetics.smocker.model.EntityWithId;
import com.jenetics.smocker.model.JavaApplication;
import com.jenetics.smocker.model.JavaApplicationMocked;
import com.jenetics.smocker.ui.SmockerUI;
import com.jenetics.smocker.ui.component.ConnectionMockedDetailsView;
import com.jenetics.smocker.ui.component.TextPanel;
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
import com.vaadin.ui.Component;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.SelectedTabChangeEvent;
import com.vaadin.ui.Window;

@SuppressWarnings("serial")
@Push
@ViewScope
@ContentView(sortingOrder = 2, viewName = "MockView", icon = "icons/952776-200.png", homeView = true, rootViewParent = ConnectionsRoot.class)
public class MockSpaceView 
	extends AbstractConnectionTreeView<JavaApplicationMocked, ConnectionMocked, CommunicationMocked, ConnectionMockedDetailsView> 
	implements RefreshableView {
	
	public MockSpaceView() {
		super(JavaApplicationMocked.class, ConnectionMocked.class, CommunicationMocked.class);
		treeGrid.addSelectionListener(this::treeSelectionChange);
		setSizeFull();
	}
	
	@Override
	public void enterInView(ViewChangeEvent event) {
		fillTreeTable();
	}

	private static final String BUNDLE_NAME = "BundleUI";

	
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
//				.addButton("Play_Button", VaadinIcons.PLAY, null,  this::canTest			
//						, this::test, org.vaadin.easyapp.util.ActionContainer.Position.LEFT, InsertPosition.AFTER)
				.addButton("Save_Button", VaadinIcons.DISC, null,  this::canSave			
						, this::save, org.vaadin.easyapp.util.ActionContainer.Position.LEFT, InsertPosition.AFTER)
				;

		return builder.build();
	}
	
//	public boolean canTest() {
//		return !isMainTabSelected() && getSelectedDetailView().isJSTabSelected();
//	}
	
//	public void test(ClickEvent event) {
//		TextPanel textPanelInput = new TextPanel(false);
//		textPanelInput.setText(getSelectedDetailView().getSelectedRequestPane().getText());
//		
//		EasyAppView container = new EasyAppView() {
//
//			sqsq
//			
//		};
//		
//		SmockerUI.displayInSubWindowMidSize(SmockerUI.getBundleValue("InputForTest"), textPanelInput);
//		
//		//getSelectedDetailView().test();
//	}
	
	public boolean canSave() {
		return !isMainTabSelected();
	}
	
	public void save(ClickEvent event) {
		getSelectedDetailView().save();
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

	@Override
	protected String getConnectionKey(ConnectionMocked conn) {
		return conn.getHost() + ":" + conn.getPort();
	}

	@Override
	protected ConnectionMockedDetailsView getConnectionDetailsLayout(ConnectionMocked conn) {
		ConnectionMockedDetailsView connectionMockedDetailsView = new ConnectionMockedDetailsView(conn);
		connectionMockedDetailsView.setRefreshClickableAction(this::refreshClickable);
		return connectionMockedDetailsView;
	}
	
	
}
