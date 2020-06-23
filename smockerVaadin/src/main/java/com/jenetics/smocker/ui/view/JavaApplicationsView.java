package com.jenetics.smocker.ui.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.vaadin.easyapp.util.ActionContainer;
import org.vaadin.easyapp.util.ActionContainer.InsertPosition;
import org.vaadin.easyapp.util.ActionContainerBuilder;
import org.vaadin.easyapp.util.annotations.ContentView;
import org.vaadin.teemu.switchui.Switch;

import com.jenetics.smocker.dao.DaoConfig;
import com.jenetics.smocker.dao.DaoManager;
import com.jenetics.smocker.dao.DaoManagerByModel;
import com.jenetics.smocker.jseval.JSEvaluator;
import com.jenetics.smocker.model.Communication;
import com.jenetics.smocker.model.Connection;
import com.jenetics.smocker.model.EntityWithId;
import com.jenetics.smocker.model.JavaApplication;
import com.jenetics.smocker.model.config.JsFilterAndDisplay;
import com.jenetics.smocker.model.converter.MockConverter;
import com.jenetics.smocker.model.event.CommunicationsRemoved;
import com.jenetics.smocker.ui.SmockerUI;
import com.jenetics.smocker.ui.component.ConnectionDetailsView;
import com.jenetics.smocker.ui.dialog.Dialog;
import com.jenetics.smocker.ui.util.ButtonWithEntity;
import com.jenetics.smocker.ui.util.RefreshableView;
import com.jenetics.smocker.ui.util.SearcheableView;
import com.jenetics.smocker.ui.util.StrandardTreeGridConnectionData;
import com.jenetics.smocker.ui.util.SwitchWithEntity;
import com.jenetics.smocker.ui.util.TreeGridConnectionData;
import com.jenetics.smocker.util.SmockerException;
import com.jenetics.smocker.util.SmockerUtility;
import com.vaadin.annotations.Push;
import com.vaadin.data.HasValue.ValueChangeEvent;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.event.ShortcutAction.ModifierKey;
import com.vaadin.event.selection.SelectionEvent;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.spring.annotation.ViewScope;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.TabSheet.Tab;

@SuppressWarnings("serial")
@Push
@ViewScope
@ContentView(sortingOrder = 1, viewName = "JavaAppView", icon = "icons/java-43-569305.png", 
homeView = true, rootViewParent = ConnectionsRoot.class, bundle=SmockerUI.BUNDLE_NAME)
public class JavaApplicationsView extends AbstractConnectionTreeView<JavaApplication, Connection, Communication, ConnectionDetailsView> 
implements RefreshableView, SearcheableView {

	public static final DaoManager<JsFilterAndDisplay> daoManagerJsFilterAndDisplay = DaoManagerByModel.getDaoManager(JsFilterAndDisplay.class);

	private transient HashMap<JavaApplication, List<SwitchWithEntity<Connection>>> switchButtonsByJavaApp = new HashMap<>();
	private transient HashMap<Connection, SwitchWithEntity<Connection>> switchWithEntityByConnection = new HashMap<>();

	public JavaApplicationsView() {
		super(JavaApplication.class, Connection.class);
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
		treeGrid.addColumn(item -> 
		item.getApplication() != null ? 
				item.getApplicationId() +  "_" + item.getApplication()
				: null).setCaption(APPLICATION);
		treeGrid.addColumn(item -> item.getAdress()).setCaption(ADRESS);
		treeGrid.addColumn(item -> item.getPort()).setCaption(PORT);
		treeGrid.addComponentColumn(this::buildWatchButton).setCaption(WATCH);
		treeGrid.addComponentColumn(this::buildConfigureButton).setCaption(CONFIGURE);
	}


	private Switch buildWatchButton(TreeGridConnectionData<JavaApplication, Connection> item) {
		if (item.isConnection()) {
			Connection connection = item.getConnection();
			SwitchWithEntity<Connection> switchConnection = new SwitchWithEntity<>(connection);
			switchConnection.setValue(connection.getWatched());
			switchConnection.addValueChangeListener(this::watchButtonClicked);
			switchButtonsByJavaApp.computeIfAbsent(connection.getJavaApplication(), key -> new ArrayList<SwitchWithEntity<Connection>>())
			.add(switchConnection);

			switchWithEntityByConnection.put(connection, switchConnection);
			return switchConnection;
		}
		else if (item.isJavaApplication()) {
			JavaApplication javaApplication = item.getJavaApplication();
			SwitchWithEntity<JavaApplication> switchConnection = new SwitchWithEntity<>(javaApplication);
			switchConnection.setValue(javaApplication.getWatched());
			switchConnection.addValueChangeListener(this::watchButtonJavaAppClicked);
			return switchConnection;
		}
		return null;
	}

	private Component buildConfigureButton(TreeGridConnectionData<JavaApplication, Connection> item) {
		if (item.isConnection()) {
			Connection connection = item.getConnection();
			ButtonWithEntity<Connection> filterButton = new ButtonWithEntity<>(connection);
			filterButton.setHeight("100%");
			filterButton.setCaption(SmockerUI.getBundleValue(CONFIGURE));
			filterButton.setDescription(SmockerUI.getBundleValue("Configure_ToolTip"));
			filterButton.addClickListener(this::configureClicked);
			return filterButton;
		}
		return null;
	}

	private Button buildFilterButton(Connection connection) {
		ButtonWithEntity<Connection> filterButton = new ButtonWithEntity<>(connection);
		filterButton.setHeight("100%");
		filterButton.setCaption(SmockerUI.getBundleValue("Filter_Button"));
		filterButton.setDescription(SmockerUI.getBundleValue("Filter_ToolTip"));
		filterButton.addClickListener(this::filterClicked);
		return filterButton;
	}

	private Button buildFormatInputButton(Connection connection) {
		ButtonWithEntity<Connection> formatButton = new ButtonWithEntity<>(connection);
		formatButton.setCaption(SmockerUI.getBundleValue("Format_Display_Input_Button"));
		formatButton.setHeight("100%");
		formatButton.setDescription(SmockerUI.getBundleValue("Format_Display_Input_ToolTip"));
		formatButton.addClickListener(this::formatInputClicked);
		return formatButton;
	}

	private Button buildFormatMockInputButton(Connection connection) {
		ButtonWithEntity<Connection> formatButton = new ButtonWithEntity<>(connection);
		formatButton.setCaption(SmockerUI.getBundleValue("Format_Display_Mock_Input_Button"));
		formatButton.setHeight("100%");
		formatButton.setDescription(SmockerUI.getBundleValue("Format_Display_Mock_Input_Button"));
		formatButton.addClickListener(this::formatMockInputClicked);
		return formatButton;
	}


	private Button buildFormatOutputButton(Connection connection) {
		ButtonWithEntity<Connection> formatButton = new ButtonWithEntity<>(connection);
		formatButton.setCaption(SmockerUI.getBundleValue("Format_Display_Output_Button"));
		formatButton.setHeight("100%");
		formatButton.setDescription(SmockerUI.getBundleValue("Format_Display_Output_ToolTip"));
		formatButton.addClickListener(this::formatOutputClicked);
		return formatButton;
	}

	private Button buildFormatMockOutputButton(Connection connection) {
		ButtonWithEntity<Connection> formatButton = new ButtonWithEntity<>(connection);
		formatButton.setCaption(SmockerUI.getBundleValue("Format_Display_Mock_Output_Button"));
		formatButton.setHeight("100%");
		formatButton.setDescription(SmockerUI.getBundleValue("Format_Display_Mock_Output_ToolTip"));
		formatButton.addClickListener(this::formatMockOutputClicked);
		return formatButton;
	}

	private Button buildTraceButton(Connection connection) {
		ButtonWithEntity<Connection> traceButton = new ButtonWithEntity<>(connection);
		traceButton.setCaption(SmockerUI.getBundleValue("Trace_Function_Button"));
		traceButton.setHeight("100%");
		traceButton.setDescription(SmockerUI.getBundleValue("Trace_Function_ToolTip"));
		traceButton.addClickListener(this::traceFunctionClicked);
		return traceButton;
	}
	
	private Button buildCommNaneButton(Connection connection) {
		ButtonWithEntity<Connection> traceButton = new ButtonWithEntity<>(connection);
		traceButton.setCaption(SmockerUI.getBundleValue("ComName_Function_Button"));
		traceButton.setHeight("100%");
		traceButton.setDescription(SmockerUI.getBundleValue("ComName_Function_ToolTip"));
		traceButton.addClickListener(this::comNameFunctionClicked);
		return traceButton;
	}

	public void filterClicked(ClickEvent event) {
		ButtonWithEntity<Connection>  button = (ButtonWithEntity<Connection>) event.getButton();
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
						Dialog.warning(SmockerUI.getBundleValue("Bad_JS_Filter_Function_Warning") + checkValue);
					}
				}, first.getFunctionFilter());
	}

	public void configureClicked(ClickEvent event) {
		ButtonWithEntity<Connection>  button = (ButtonWithEntity<Connection>) event.getButton();
		Connection conn = button.getEntity();

		Dialog.displayComponentInVLayoutBox(SmockerUI.getBundleValue(CONFIGURE), 
				new Label(conn.getHost() + ":" + conn.getPort()),
				buildFilterButton(conn), 
				buildFormatInputButton(conn), 
				buildFormatOutputButton(conn),
				buildFormatMockInputButton(conn), 
				buildFormatMockOutputButton(conn),
				buildCommNaneButton(conn),
				buildTraceButton(conn));
	}



	public void formatInputClicked(ClickEvent event) {
		ButtonWithEntity<Connection>  button = (ButtonWithEntity<Connection>) event.getButton();
		Connection conn = button.getEntity();
		final JsFilterAndDisplay first = DaoConfig.findJsDisplayAndFilter(conn);
		Dialog.displayCreateStringBox(SmockerUI.getBundleValue("format_function_input_display"), 
				selectedFunction -> {
					first.setFunctionInputDisplay(selectedFunction);
					String checkValue = checkValidFunctionDisplay(selectedFunction);
					if (checkValue == null) {
						daoManagerJsFilterAndDisplay.update(first);
					}
					else {
						first.setFunctionInputDisplay("");
						Dialog.warning("Bad Javascript function should be of "
								+ "function(string) returning string " + checkValue);
					}
				}, first.getFunctionInputDisplay());
	}

	public void traceFunctionClicked(ClickEvent event) {
		ButtonWithEntity<Connection>  button = (ButtonWithEntity<Connection>) event.getButton();
		Connection conn = button.getEntity();
		final JsFilterAndDisplay first = DaoConfig.findJsDisplayAndFilter(conn);
		Dialog.displayCreateStringBox(SmockerUI.getBundleValue("format_function_output_display"), 
				selectedFunction -> {
					first.setFunctionTrace(selectedFunction);
					String checkValue = checkValidFunctionTrace(selectedFunction);
					if (checkValue == null) {
						daoManagerJsFilterAndDisplay.update(first);
					}
					else {
						first.setFunctionOutputDisplay("");
						Dialog.warning("Bad Javascript function should be of "
								+ "function(string) returning void " + checkValue);
					}
				}, first.getFunctionTrace());
	}
	
	public void comNameFunctionClicked(ClickEvent event) {
		ButtonWithEntity<Connection>  button = (ButtonWithEntity<Connection>) event.getButton();
		Connection conn = button.getEntity();
		final JsFilterAndDisplay first = DaoConfig.findJsDisplayAndFilter(conn);
		Dialog.displayCreateStringBox(SmockerUI.getBundleValue("ComName_Function_Button"), 
				selectedFunction -> {
					first.setFunctionComName(selectedFunction);
					boolean checkValue = checkValidComNameFunction(selectedFunction);
					if (!checkValue) {
						first.setFunctionOutputDisplay("");
						Dialog.warning("Bad Javascript function should be of "
								+ "function(string) returning string " + checkValue);
						
					}
					else {
						daoManagerJsFilterAndDisplay.update(first);
					}
				}, first.getFunctionComName());
	}


	public void formatOutputClicked(ClickEvent event) {
		ButtonWithEntity<Connection>  button = (ButtonWithEntity<Connection>) event.getButton();
		Connection conn = button.getEntity();
		final JsFilterAndDisplay first = DaoConfig.findJsDisplayAndFilter(conn);
		Dialog.displayCreateStringBox(SmockerUI.getBundleValue("format_function_output_display"), 
				selectedFunction -> {
					first.setFunctionOutputDisplay(selectedFunction);
					String checkValue = checkValidFunctionDisplay(selectedFunction);
					if (checkValue == null) {
						daoManagerJsFilterAndDisplay.update(first);
					}
					else {
						first.setFunctionOutputDisplay("");
						Dialog.warning("Bad Javascript function should be of "
								+ "function(string) returning boolean " + checkValue);
					}
				}, first.getFunctionOutputDisplay());
	}


	public void formatMockInputClicked(ClickEvent event) {
		ButtonWithEntity<Connection>  button = (ButtonWithEntity<Connection>) event.getButton();
		Connection conn = button.getEntity();
		final JsFilterAndDisplay first = DaoConfig.findJsDisplayAndFilter(conn);
		Dialog.displayCreateStringBox(SmockerUI.getBundleValue("format_function_mock_input_display"), 
				selectedFunction -> {
					first.setFunctionMockInputDisplay(selectedFunction);
					String checkValue = checkValidFunctionDisplay(selectedFunction);
					if (checkValue == null) {
						daoManagerJsFilterAndDisplay.update(first);
					}
					else {
						first.setFunctionMockInputDisplay("");
						Dialog.warning("Bad Javascript function should be of "
								+ "function(string) returning boolean " + checkValue);
					}
				}, first.getFunctionMockInputDisplay());
	}

	public void formatMockOutputClicked(ClickEvent event) {
		ButtonWithEntity<Connection>  button = (ButtonWithEntity<Connection>) event.getButton();
		Connection conn = button.getEntity();
		final JsFilterAndDisplay first = DaoConfig.findJsDisplayAndFilter(conn);
		Dialog.displayCreateStringBox(SmockerUI.getBundleValue("format_function_mock_output_display"), 
				selectedFunction -> {
					first.setFunctionMockOutputDisplay(selectedFunction);
					String checkValue = checkValidFunctionDisplay(selectedFunction);
					if (checkValue == null) {
						daoManagerJsFilterAndDisplay.update(first);
					}
					else {
						first.setFunctionMockOutputDisplay("");
						Dialog.warning("Bad Javascript function should be of "
								+ "function(string) returning boolean " + checkValue);
					}
				}, first.getFunctionMockOutputDisplay());
	}




	private String checkValidFunctionFilter(String selectedFunction) {
		if (StringUtils.isEmpty(selectedFunction)) {
			return null;
		}
		try {
			JSEvaluator.filter(selectedFunction, "", "");
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
	
	private String checkValidFunctionTrace(String selectedFunction) {
		if (StringUtils.isEmpty(selectedFunction)) {
			return null;
		}
		try {
			JSEvaluator.trace(selectedFunction, "", "");
		} catch (SmockerException e) {
			return SmockerUtility.getStackTrace(e, 400);
		}
		return null;
	}
	
	private boolean checkValidComNameFunction(String selectedFunction) {
		if (StringUtils.isEmpty(selectedFunction)) {
			return true;
		}
		try {
			JSEvaluator.commName(selectedFunction, "");
			return true;
		} catch (SmockerException e) {
			return false;
		}
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

	public void watchButtonJavaAppClicked(ValueChangeEvent<Boolean> event) {
		SwitchWithEntity<JavaApplication> switchWithEntity = (SwitchWithEntity<JavaApplication>) event.getSource();
		List<SwitchWithEntity<Connection>> targetSwitchButton = switchButtonsByJavaApp.get(switchWithEntity.getEntity());
		if (targetSwitchButton != null) {
			targetSwitchButton.stream()
			.forEach( switchButton -> switchButton.setValue(switchWithEntity.getValue()));
		}
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
				.addButtonWithShotCut("ViewDetails_Button", VaadinIcons.EYE, "ViewDetails_Button_ToolTip",  this::isConnectionSelected			
						, this::details, org.vaadin.easyapp.util.ActionContainer.Position.LEFT, InsertPosition.AFTER, KeyCode.E, 
						ModifierKey.CTRL)
				.addButtonWithShotCut("Refresh_Button", VaadinIcons.REFRESH, "Refresh_Button_ToolTip",  this::always			
						, this::refresh, org.vaadin.easyapp.util.ActionContainer.Position.LEFT, InsertPosition.AFTER, KeyCode.R, 
						ModifierKey.CTRL)
				.addButtonWithShotCut("Clean_Button", VaadinIcons.MINUS, "Clean_Button_ToolTip",  this::isSelected			
						, this::clean, org.vaadin.easyapp.util.ActionContainer.Position.LEFT, InsertPosition.AFTER, KeyCode.D, 
						ModifierKey.CTRL);
		if (!isMainTabSelected()) {
			builder.addButton("AddToMock_Button", VaadinIcons.PLUS, "AddToMock_ToolTip",  this::canAddToMock			
					, this::addToMock, org.vaadin.easyapp.util.ActionContainer.Position.LEFT, InsertPosition.AFTER)
			.addButton("AddAllToMock_Button", VaadinIcons.PLUS, "AddAllToMock_ToolTip",  this::canAddAllToMock			
					, this::addAllToMock, org.vaadin.easyapp.util.ActionContainer.Position.LEFT, InsertPosition.AFTER)
			.addButton("CleanAll_Button", VaadinIcons.MINUS, "CleanAll_ToolTip",  this::canCleanAll			
					, this::cleanAll, org.vaadin.easyapp.util.ActionContainer.Position.LEFT, InsertPosition.AFTER)
			.addButton("Sort_Button", VaadinIcons.SORT, "Sort_ToolTip",  this::canSort			
					, this::sort, org.vaadin.easyapp.util.ActionContainer.Position.LEFT, InsertPosition.AFTER)
			.addButton("StackTrace", VaadinIcons.ALIGN_JUSTIFY, "StackTraceToolTip",  this::canDisplayStack		
					, this::displayStack, org.vaadin.easyapp.util.ActionContainer.Position.LEFT, InsertPosition.AFTER);
		}
		return builder.build();
	}

	public void addToMock(ClickEvent event) {
		ConnectionDetailsView connectionDetailsView = getSelectedDetailView();
		MockConverter.convertcommunication(connectionDetailsView.getSelectedCommunication());
		SmockerUI.getInstance().displayNotif(SmockerUI.getBundleValue("Notif_CommMocked_Added"), 0);
	}

	public void addAllToMock(ClickEvent event) {
		ConnectionDetailsView connectionDetailsView = getSelectedDetailView();
		for (Communication comm : connectionDetailsView.getCommunications()) {
			MockConverter.convertcommunication(comm);
		}
		SmockerUI.getInstance().displayNotif(SmockerUI.getBundleValue("Notif_CommsMocked_Added"), 0);
	}


	public boolean canAddToMock() {
		ConnectionDetailsView connectionDetailsView = getSelectedDetailView();
		return (connectionDetailsView != null && !isMainTabSelected() && connectionDetailsView.isSelected());
	}

	public boolean canAddAllToMock() {
		ConnectionDetailsView connectionDetailsView = getSelectedDetailView();
		if (connectionDetailsView != null) {
			return !connectionDetailsView.getCommunications().isEmpty();
		}
		return false;
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
					switchButtonsByJavaApp.get(selectedConnection.getJavaApplication()).remove(switchWithEntityByConnection.get(selectedConnection));
					switchWithEntityByConnection.remove(selectedConnection);
					refreshClickable();
				}
				else if (treeGridConnectionData.isJavaApplication()) {
					JavaApplication selectedJavaApplication = treeGridConnectionData.getJavaApplication();
					selectedJavaApplication.getConnections().stream().forEach(this::removeTabConn);
					daoManagerJavaApplication.delete(selectedJavaApplication);
					switchButtonsByJavaApp.remove(selectedJavaApplication);
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

	public void sort(ClickEvent event) {
		if (!isMainTabSelected()) {
			getSelectedDetailView().sort();
		}
	}

	public boolean canCleanAll() {
		return !isMainTabSelected() && !getSelectedDetailView().getConnection().getCommunications().isEmpty();
	}

	public boolean canSort() {
		return true;
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
		return conn.getJavaApplication().getId() + "_" + conn.getHost() + ":" + conn.getPort();
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
			selectedView.search(searchQuery, this::refreshClickable);
		}
	}

	public void remove(CommunicationsRemoved item) {
		Iterator<Component> iter = tabSheet.iterator();
		while (iter.hasNext()) {
			Component current = iter.next();
			if (current instanceof ConnectionDetailsView) {
				((ConnectionDetailsView)current).deleteCommunications(item);
			}

		}
	}


}
