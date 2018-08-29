package com.jenetics.smocker.ui.view;

import java.util.Set;

import org.apache.commons.lang3.BooleanUtils;
import org.vaadin.easyapp.util.ActionContainer;
import org.vaadin.easyapp.util.ActionContainer.InsertPosition;
import org.vaadin.easyapp.util.ActionContainerBuilder;
import org.vaadin.easyapp.util.EasyAppLayout;
import org.vaadin.easyapp.util.annotations.ContentView;

import com.jenetics.smocker.model.Communication;
import com.jenetics.smocker.model.Connection;
import com.jenetics.smocker.model.EntityWithId;
import com.jenetics.smocker.model.JavaApplication;
import com.jenetics.smocker.model.converter.MockConverter;
import com.jenetics.smocker.network.ClientCommunicator;
import com.jenetics.smocker.ui.SmockerUI;
import com.jenetics.smocker.ui.component.AbstractConnectionDetails;
import com.jenetics.smocker.ui.component.ConnectionDetailsView;
import com.jenetics.smocker.ui.dialog.Dialog;
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

@SuppressWarnings("serial")
@Push
@ViewScope
@ContentView(sortingOrder = 1, viewName = "JavaAppView", icon = "icons/java-43-569305.png", 
homeView = true, rootViewParent = ConnectionsRoot.class, bundle=SmockerUI.BUNDLE_NAME)
public class JavaApplicationsView extends AbstractConnectionTreeView<JavaApplication, Connection, Communication, ConnectionDetailsView> implements RefreshableView {

	
	public JavaApplicationsView() {
		super(JavaApplication.class, Connection.class, Communication.class);
		treeGrid.addSelectionListener(this::treeSelectionChange);
		setSizeFull();
	}
	
	
//	@Override
//	protected Component getInnerComponent() {
//		treeGrid.setSizeFull();
//		VerticalLayout tabmainlayout = new VerticalLayout();
//		tabmainlayout.setSizeFull();
//		tabmainlayout.setCaption("Main");
//		tabmainlayout.addComponent(treeGrid);
//		tabSheet.setSizeFull();
//		tabSheet.addTab(tabmainlayout);
//		tabSheet.addSelectedTabChangeListener(this::tabChanged);
//		
//		tabSheet.setCloseHandler(this::tabClosed);
//		//tabSheet.add
//		return tabSheet;
//	}
//	public void tabClosed(TabSheet tabsheet, Component tabContent) {
//		Tab tab = tabsheet.getTab(tabContent);
//		Map<Tab, String> connectionByTab = 
//				tabByConnectionKey.entrySet()
//			       .stream()
//			       .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
//		
//		String connKey = connectionByTab.get(tab);
//		tabByConnectionKey.remove(connKey);
//		
//		tabsheet.removeTab(tab);
//		refreshClickable();
//	}


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
		ActionContainerBuilder builder = new ActionContainerBuilder(SmockerUI.BUNDLE_NAME)
				
				.addButton("ViewDetails_Button", VaadinIcons.EYE, null,  this::isConnectionSelected			
						, this::details, org.vaadin.easyapp.util.ActionContainer.Position.LEFT, InsertPosition.AFTER)
				.addButton("Refresh_Button", VaadinIcons.REFRESH, null,  this::always			
						, this::refresh, org.vaadin.easyapp.util.ActionContainer.Position.LEFT, InsertPosition.AFTER)
				.addButton("AddToMock_Button", VaadinIcons.PLUS, "AddToMock_ToolTip",  this::canAddToMock			
						, this::addToMock, org.vaadin.easyapp.util.ActionContainer.Position.LEFT, InsertPosition.AFTER)
				.addButton("Clean_Button", VaadinIcons.MINUS, "Clean_ToolTip",  this::isSelected			
						, this::clean, org.vaadin.easyapp.util.ActionContainer.Position.LEFT, InsertPosition.AFTER)
				.addButton("CleanAll_Button", VaadinIcons.MINUS, "CleanAll_ToolTip",  this::canCleanAll			
						, this::cleanAll, org.vaadin.easyapp.util.ActionContainer.Position.LEFT, InsertPosition.AFTER)
				.addButton("StackTrace", VaadinIcons.ALIGN_JUSTIFY, "StackTraceToolTip",  this::canDisplayStack		
						, this::displayStack, org.vaadin.easyapp.util.ActionContainer.Position.LEFT, InsertPosition.AFTER)
				;

		return builder.build();
	}
	
	public void addToMock(ClickEvent event) {
		ConnectionDetailsView connectionDetailsView = getSelectedDetailView();
		MockConverter.convertcommunication(connectionDetailsView.getSelectedCommunication());
		SmockerUI.getInstance().getEasyAppMainView().getScanner().navigateTo(MockSpaceView.class);
	}
	
	public boolean canAddToMock() {
		ConnectionDetailsView connectionDetailsView = getSelectedDetailView();
		return (connectionDetailsView != null && !isMainTabSelected() && connectionDetailsView.isSelected());
	}
	
	
	public void clean(ClickEvent event) {
		if (isSelected()) {
			Dialog.ask(SmockerUI.getBundle().getString("RemoveQuestion"), null, this::delete, null);
		}
	}
	
	public void delete() {
		Set<TreeGridConnectionData<JavaApplication, Connection>> selectedItems = treeGrid.getSelectedItems();
		for (TreeGridConnectionData<JavaApplication, Connection> treeGridConnectionData : selectedItems) {
			if (treeGridConnectionData.isConnection()) {
				Connection selectedConnection = treeGridConnectionData.getConnection();
				selectedConnection.getJavaApplication().getConnections().remove(selectedConnection);
				daoManagerJavaApplication.update(selectedConnection.getJavaApplication());
			}
			else if (treeGridConnectionData.isJavaApplication()) {
				JavaApplication selectedJavaApplication = treeGridConnectionData.getJavaApplication();
				daoManagerJavaApplication.deleteById(selectedJavaApplication.getId());
			}
			fillTreeTable();
		}
	}
	
	public void cleanAll(ClickEvent event) {
		Notification.show("CleanAll");
	}
	
	public boolean canCleanAll() {
		return true;
	}
	
	public void displayStack(ClickEvent event) {		
		getSelectedDetailView().displayStack();
		//Notification.show("CleanAll");
	}
	
	public boolean canDisplayStack() {
		return !isMainTabSelected() && getSelectedDetailView() != null && getSelectedDetailView().isSelected();
	}
	
	public boolean isSelected() {
		return treeGrid.getSelectedItems().size() == 1;
	}
	
	public boolean isConnectionSelected() {
		return isMainTabSelected() && 
				treeGrid.getSelectedItems().size() == 1 &&				
				treeGrid.getSelectedItems().iterator().next().isConnection();
	}
	
	public void search(String searchValue) {
		Notification.show("Search for:" + searchValue);
	}

	
	@Override
	protected String getConnectionKey(Connection conn) {
		return conn.getHost() + ":" + conn.getPort();
	}
	
	
	@Override
	protected ConnectionDetailsView getConnectionDetailsLayout(Connection conn) {
		ConnectionDetailsView connectionDetailsView = new ConnectionDetailsView(conn);
		connectionDetailsView.setRefreshClickableAction(this::refreshClickable);
		return connectionDetailsView;
	}

}
