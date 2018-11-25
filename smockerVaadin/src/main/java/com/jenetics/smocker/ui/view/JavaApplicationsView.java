package com.jenetics.smocker.ui.view;

import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.vaadin.easyapp.util.ActionContainer;
import org.vaadin.easyapp.util.ActionContainer.InsertPosition;
import org.vaadin.easyapp.util.ActionContainerBuilder;
import org.vaadin.easyapp.util.EasyAppLayout;
import org.vaadin.easyapp.util.annotations.ContentView;
import org.vaadin.teemu.switchui.Switch;

import com.jenetics.smocker.dao.DaoConfig;
import com.jenetics.smocker.dao.DaoManager;
import com.jenetics.smocker.dao.DaoManagerByModel;
import com.jenetics.smocker.jseval.JSEvaluator;
import com.jenetics.smocker.model.Communication;
import com.jenetics.smocker.model.CommunicationMocked;
import com.jenetics.smocker.model.Connection;
import com.jenetics.smocker.model.EntityWithId;
import com.jenetics.smocker.model.JavaApplication;
import com.jenetics.smocker.model.config.JsFilterAndDisplay;
import com.jenetics.smocker.model.config.SmockerConf;
import com.jenetics.smocker.model.converter.MockConverter;
import com.jenetics.smocker.network.ClientCommunicator;
import com.jenetics.smocker.ui.SmockerUI;
import com.jenetics.smocker.ui.component.AbstractConnectionDetails;
import com.jenetics.smocker.ui.component.ConnectionDetailsView;
import com.jenetics.smocker.ui.component.seach.CommunicationItemsResults;
import com.jenetics.smocker.ui.dialog.Dialog;
import com.jenetics.smocker.ui.util.ButtonWithIEntity;
import com.jenetics.smocker.ui.util.RefreshableView;
import com.jenetics.smocker.ui.util.SearcheableView;
import com.jenetics.smocker.ui.util.StrandardTreeGridConnectionData;
import com.jenetics.smocker.ui.util.SwitchWithEntity;
import com.jenetics.smocker.ui.util.TreeGridConnectionData;
import com.jenetics.smocker.util.SmockerException;
import com.jenetics.smocker.util.SmockerUtility;
import com.vaadin.annotations.Push;
import com.vaadin.data.HasValue.ValueChangeEvent;
import com.vaadin.event.selection.SelectionEvent;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.spring.annotation.ViewScope;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TabSheet.Tab;

@SuppressWarnings("serial")
@Push
@ViewScope
@ContentView(sortingOrder = 1, viewName = "JavaAppView", icon = "icons/java-43-569305.png", 
homeView = true, rootViewParent = ConnectionsRoot.class, bundle=SmockerUI.BUNDLE_NAME)
public class JavaApplicationsView extends AbstractConnectionTreeView<JavaApplication, Connection, Communication, ConnectionDetailsView> 
implements RefreshableView, SearcheableView {

	public static DaoManager<JsFilterAndDisplay> daoManagerJsFilterAndDisplay = DaoManagerByModel.getDaoManager(JsFilterAndDisplay.class);

	public JavaApplicationsView() {
		super(JavaApplication.class, Connection.class, Communication.class);
		treeGrid.addSelectionListener(this::treeSelectionChange);
		setSizeFull();
		tabSheet.addSelectedTabChangeListener(this::tabChanged);
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
		treeGrid.addComponentColumn(this::buildWatchButton).setCaption(WATCH);
		treeGrid.addComponentColumn(this::buildFilterButton).setCaption(FITLER);
		treeGrid.addComponentColumn(this::buildFormatButton).setCaption(FORMAT_DISPLAY);
	}

	private Switch buildWatchButton(TreeGridConnectionData<JavaApplication, Connection> item) {
		if (item.isConnection()) {
			Connection connection = item.getConnection();
			SwitchWithEntity<Connection> switchConnection = new SwitchWithEntity<>(connection);
			switchConnection.setValue(connection.getWatched());
			switchConnection.addValueChangeListener(this::watchButtonClicked);
			return switchConnection;
		}
		return null;
	}

	private Component buildFilterButton(TreeGridConnectionData<JavaApplication, Connection> item) {
		if (item.isConnection()) {
			Connection connection = item.getConnection();
			ButtonWithIEntity<Connection> filterButton = new ButtonWithIEntity<>(connection);
			filterButton.setHeight("100%");
			filterButton.setCaption(SmockerUI.getBundleValue(FITLER));
			filterButton.setDescription(SmockerUI.getBundleValue("Filter_TootTip"));
			filterButton.addClickListener(this::filterClicked);
			return filterButton;
		}
		return null;
	}

	private Component buildFormatButton(TreeGridConnectionData<JavaApplication, Connection> item) {
		if (item.isConnection()) {
			Connection connection = item.getConnection();
			ButtonWithIEntity<Connection> formatButton = new ButtonWithIEntity<>(connection);
			formatButton.setCaption(SmockerUI.getBundleValue(FORMAT_DISPLAY));
			formatButton.setHeight("100%");
			formatButton.setDescription(SmockerUI.getBundleValue("Format_Display_TootTip"));
			formatButton.addClickListener(this::formatClicked);
			return formatButton;
		}
		return null;
	}

	public void filterClicked(ClickEvent event) {
		ButtonWithIEntity<Connection>  button = (ButtonWithIEntity<Connection>) event.getButton();
		Connection conn = button.getEntity();

		final JsFilterAndDisplay first = DaoConfig.findJsDisplayAndFilter(conn);
		Dialog.displayCreateStringBox(SmockerUI.getBundleValue("filter_function_select"), 
				selectedFunction -> {
					first.setFunctionFilter(selectedFunction);
					String checkValue = checkValidFunctionFilter(selectedFunction);
					if (checkValue == null) {
						daoManagerJsFilterAndDisplay.update(first);
					}
					else {
						first.setFunctionFilter("");
						Dialog.warning("Bad Javascript function should be of function(string) returning boolean " + checkValue);
					}
				}, first.getFunctionFilter());
	}



	public void formatClicked(ClickEvent event) {
		ButtonWithIEntity<Connection>  button = (ButtonWithIEntity<Connection>) event.getButton();
		Connection conn = button.getEntity();
		final JsFilterAndDisplay first = DaoConfig.findJsDisplayAndFilter(conn);
		Dialog.displayCreateStringBox(SmockerUI.getBundleValue("format_function_display"), 
				selectedFunction -> {
					first.setFunctionDisplay(selectedFunction);
					String checkValue = checkValidFunctionDisplay(selectedFunction);
					if (checkValue == null) {
						daoManagerJsFilterAndDisplay.update(first);
					}
					else {
						first.setFunctionDisplay("");
						Dialog.warning("Bad Javascript function should be of "
								+ "function(string) returning boolean " + checkValue);
					}
				}, first.getFunctionDisplay());
	}

	private String checkValidFunctionFilter(String selectedFunction) {
		if (StringUtils.isEmpty(selectedFunction)) {
			return null;
		}
		try {
			JSEvaluator.filter(selectedFunction, "");
		} catch (SmockerException e) {
			return SmockerUtility.getStackTrace(e, 400);
		}
		return null;
	}

	private String checkValidFunctionDisplay(String selectedFunction) {
		if (StringUtils.isEmpty(selectedFunction)) {
			return null;
		}
		try {
			JSEvaluator.formatAndDisplay(selectedFunction, "");
		} catch (SmockerException e) {
			return SmockerUtility.getStackTrace(e, 400);
		}
		return null;
	}



	public void watchButtonClicked(ValueChangeEvent<Boolean> event) {
		SwitchWithEntity<Connection> switchWithEntity = (SwitchWithEntity<Connection>) event.getSource();
		if (event.getValue()) {
			switchWithEntity.getEntity().setWatched(true);
		}
		else {
			switchWithEntity.getEntity().setWatched(false);
		}
		daoManagerConnection.update(switchWithEntity.getEntity());	
	}


	public void treeSelectionChange(SelectionEvent<TreeGridConnectionData<JavaApplication, Connection>> event) {
		refreshClickable();
	}

	@Override
	public void refresh(EntityWithId entityWithId) {
		if (isMainTabSelected()) {
			refreshEntity(entityWithId);
		}
		else {
			getSelectedDetailView().refresh();
		}
	}

	@Override
	public ActionContainer buildActionContainer() {
		ActionContainerBuilder builder = new ActionContainerBuilder(SmockerUI.BUNDLE_NAME)
				.addButton("ViewDetails_Button", VaadinIcons.EYE, null,  this::isConnectionSelected			
						, this::details, org.vaadin.easyapp.util.ActionContainer.Position.LEFT, InsertPosition.AFTER)
				.addButton("Refresh_Button", VaadinIcons.REFRESH, null,  this::always			
						, this::refresh, org.vaadin.easyapp.util.ActionContainer.Position.LEFT, InsertPosition.AFTER)
				.addButton("Clean_Button", VaadinIcons.MINUS, "Clean_ToolTip",  this::isSelected			
						, this::clean, org.vaadin.easyapp.util.ActionContainer.Position.LEFT, InsertPosition.AFTER);
		if (!isMainTabSelected()) {
			builder.addButton("AddToMock_Button", VaadinIcons.PLUS, "AddToMock_ToolTip",  this::canAddToMock			
					, this::addToMock, org.vaadin.easyapp.util.ActionContainer.Position.LEFT, InsertPosition.AFTER)
			.addButton("CleanAll_Button", VaadinIcons.MINUS, "CleanAll_ToolTip",  this::canCleanAll			
					, this::cleanAll, org.vaadin.easyapp.util.ActionContainer.Position.LEFT, InsertPosition.AFTER)
			.addButton("StackTrace", VaadinIcons.ALIGN_JUSTIFY, "StackTraceToolTip",  this::canDisplayStack		
					, this::displayStack, org.vaadin.easyapp.util.ActionContainer.Position.LEFT, InsertPosition.AFTER);
		}
		return builder.build();
	}

	public void addToMock(ClickEvent event) {
		ConnectionDetailsView connectionDetailsView = getSelectedDetailView();
		MockConverter.convertcommunication(connectionDetailsView.getSelectedCommunication());
	}

	public boolean canAddToMock() {
		ConnectionDetailsView connectionDetailsView = getSelectedDetailView();
		return (connectionDetailsView != null && !isMainTabSelected() && connectionDetailsView.isSelected());
	}


	public void clean(ClickEvent event) {
		if (isMainTabSelected()) {
			Dialog.ask(SmockerUI.getBundle().getString("RemoveQuestion"), null, this::delete, null);
		}
		else {
			delete();
		}
	}

	public void delete() {
		if (isMainTabSelected()) {
			Set<TreeGridConnectionData<JavaApplication, Connection>> selectedItems = treeGrid.getSelectedItems();
			for (TreeGridConnectionData<JavaApplication, Connection> treeGridConnectionData : selectedItems) {
				if (treeGridConnectionData.isConnection()) {
					Connection selectedConnection = treeGridConnectionData.getConnection();
					selectedConnection.getJavaApplication().getConnections().remove(selectedConnection);
					daoManagerJavaApplication.update(selectedConnection.getJavaApplication());
					removeTabConn(selectedConnection);
					refreshClickable();
				}
				else if (treeGridConnectionData.isJavaApplication()) {
					JavaApplication selectedJavaApplication = treeGridConnectionData.getJavaApplication();
					selectedJavaApplication.getConnections().stream().forEach(this::removeTabConn);
					daoManagerJavaApplication.deleteById(selectedJavaApplication.getId());
				}
				fillTreeTable();
			}
		}
		else {
			ConnectionDetailsView connectionDetailsView = getSelectedDetailView();
			connectionDetailsView.clean();
		}
	}

	private void removeTabConn(Connection selectedConnection) {
		Tab tabForConn = tabByConnection.get(selectedConnection);
		if (tabForConn != null) {
			detailsViewByTab.remove(tabForConn);
			tabByConnectionKey.remove(getConnectionKey(selectedConnection));
			tabSheet.removeTab(tabForConn);
		}
	}

	public void cleanAll(ClickEvent event) {
		if (!isMainTabSelected()) {
			ConnectionDetailsView connectionDetailsView = getSelectedDetailView();
			connectionDetailsView.cleanAll();
		}
	}

	public boolean canCleanAll() {
		return !isMainTabSelected() && getSelectedDetailView().getConnection().getCommunications().size() > 0;
	}

	public void displayStack(ClickEvent event) {		
		getSelectedDetailView().displayStack();
	}

	public boolean canDisplayStack() {
		return !isMainTabSelected() && getSelectedDetailView() != null && getSelectedDetailView().isSelected();
	}

	public boolean isSelected() {
		if (isMainTabSelected()) {
			return treeGrid.getSelectedItems().size() == 1;
		}
		else {
			ConnectionDetailsView connectionDetailsView = getSelectedDetailView();
			return connectionDetailsView.isSelected();
		}
	}

	public boolean isConnectionSelected() {
		return isMainTabSelected() && 
				treeGrid.getSelectedItems().size() == 1 &&				
				treeGrid.getSelectedItems().iterator().next().isConnection();
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


	@Override
	public void search(String searchQuery) {
		Component selectedComponent = tabSheet.getSelectedTab();
		if (selectedComponent instanceof ConnectionDetailsView && !StringUtils.isEmpty(searchQuery)) {
			ConnectionDetailsView selectedView = (ConnectionDetailsView) selectedComponent;
			selectedView.search(searchQuery);
		}
	}
}
