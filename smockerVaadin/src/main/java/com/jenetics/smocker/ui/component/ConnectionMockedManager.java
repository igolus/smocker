package com.jenetics.smocker.ui.component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.jboss.logging.Logger;
import org.vaadin.easyapp.util.ActionContainer;
import org.vaadin.easyapp.util.ActionContainerBuilder;
import org.vaadin.easyapp.util.EasyAppLayout;
import org.vaadin.teemu.switchui.Switch;

import com.jenetics.smocker.dao.DaoManager;
import com.jenetics.smocker.dao.DaoManagerByModel;
import com.jenetics.smocker.model.CommunicationMocked;
import com.jenetics.smocker.model.ConnectionMocked;
import com.jenetics.smocker.model.Scenario;
import com.jenetics.smocker.ui.SmockerUI;
import com.jenetics.smocker.ui.dialog.Dialog;
import com.jenetics.smocker.ui.util.SwitchWithEntity;
import com.jenetics.smocker.ui.util.TreeGridMockedItem;
import com.vaadin.data.HasValue.ValueChangeEvent;
import com.vaadin.data.TreeData;
import com.vaadin.data.provider.TreeDataProvider;
import com.vaadin.event.selection.SingleSelectionEvent;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Grid.SelectionMode;
import com.vaadin.ui.TreeGrid;

public class ConnectionMockedManager extends EasyAppLayout {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final String CREATE_SCENARIO = "createScenario";
	private static final String RENAME_COMM = "renameComm";
	private static final String MOVE_COMM = "moveComm";
	private static final String DELETE_COMM = "deleteComm";
	private static final String DELETE_SCENARIO = "deleteScenario";
	private static final String MOVE_ALL_COMMS = "moveAllComms";
	private static final String REMOVE_COMMS = "removeComms";
	private static final String TURN_OFF_ALL = "turnOffAll";
	private static final String TURN_ON_ALL = "turnOnAll";
	private static final String RENAME_SCENARIO = "renameScenario";
	private static final String MOVE_UP = "moveUp";
	private static final String MOVE_DOWN = "moveDown";
	private static final String DUPLICATE_COMM = "duplicateComm";
	
	private TreeGrid<TreeGridMockedItem> treeGrid;
	private TreeData<TreeGridMockedItem> treeData;
	private TreeDataProvider<TreeGridMockedItem> treeDataProvider;
	private ConnectionMocked connectionMocked;
	private Consumer<CommunicationMocked> itemClicked;

	private transient DaoManager<Scenario> scenarioDaoManager = DaoManagerByModel.getDaoManager(Scenario.class);
	private transient DaoManager<CommunicationMocked> communicationDaoManager = DaoManagerByModel.getDaoManager(CommunicationMocked.class);
	private transient DaoManager<ConnectionMocked> connectionDaoManager = DaoManagerByModel.getDaoManager(ConnectionMocked.class);
	private ComboBox comboBox;
	
	private List<Scenario> listScenarios;
	private TreeGridMockedItem selectedTreeItem;
	private TreeGridMockedItem root;
	
	private Map<CommunicationMocked, TreeGridMockedItem> treeScenarioItemByComm = new HashMap<>();
	private Map<TreeGridMockedItem, List<TreeGridMockedItem>> commItemsByScenarioItem = new HashMap<>();
	private Map<CommunicationMocked, TreeGridMockedItem> treeCommItemByComm = new HashMap<>();
	private Map<Scenario, TreeGridMockedItem> treeScenarioItemByScenario = new LinkedHashMap<>();
	private List<TreeGridMockedItem> listItems = new ArrayList<>();

	private Logger logger = Logger.getLogger(ConnectionMockedManager.class);

	public ConnectionMockedManager(ConnectionMocked connectionMocked, 
			Consumer<CommunicationMocked> itemClicked, Runnable refreshClickable ) {
		super();
		this.refreshClickable = refreshClickable;
		this.itemClicked = itemClicked;
		this.connectionMocked = connectionMocked;
		fillCommunication();
		
		setSizeFull();
	}

	@Override
	public ActionContainer buildActionContainer() {

		comboBox = new ComboBox<>();
		comboBox.setTextInputAllowed(false);
		comboBox.addSelectionListener(this::comboSelected);
	
		ActionContainerBuilder builder = new ActionContainerBuilder(null)
				.withSingleComponent(comboBox);
		
		ActionContainer actionContainer = builder.build();
		return actionContainer;
	}

	
	private TreeGridMockedItem undefinedScenarioItem;
	
	public ComboBox getComboBox() {
		return comboBox;
	}
	
	
	public void fillCommunication() {
		if (treeGrid != null) {
			removeComponent(treeGrid);
		}
		treeGrid = new TreeGrid<>();
		treeGrid.setSelectionMode(SelectionMode.SINGLE);
		treeGrid.setSizeFull();

		treeGrid.addItemClickListener(this::treeItemClick);
		treeGrid.removeHeaderRow(0);
		treeGrid.setSizeFull();
		
		treeData = new TreeData<>();
		treeDataProvider = new TreeDataProvider<>(treeData);
		
		
		treeGrid.setItems(listItems);
		
		
		treeData.clear();
		root = new TreeGridMockedItem();
		listItems.add(root);
		
		treeData.addItem(null, root);

		Scenario undefinedScenario = DaoManagerByModel.getUNDEFINED_SCENARIO();
		undefinedScenarioItem = addScenarioToRoot(undefinedScenario);
		
		List<CommunicationMocked> communicationsMocked = getListCommOrdered(undefinedScenario);
		for (CommunicationMocked communicationMocked : communicationsMocked) {
			addCommunicationItemToScenario(undefinedScenarioItem, communicationMocked);
		}
		
		listScenarios = scenarioDaoManager.listAll();
		for (Scenario scenario : listScenarios) {
			if (!scenario.equals(DaoManagerByModel.getUNDEFINED_SCENARIO())) {
				TreeGridMockedItem scenarioItem = addScenarioToRootExceptUndifined(scenario);
				List<CommunicationMocked> communicationsMockedScenario = getListCommOrdered(scenario);
				for (CommunicationMocked communicationMocked : communicationsMockedScenario) {
					addCommunicationItemToScenario(scenarioItem, communicationMocked);
				}
			}
		}
		addTreeMapping();
		
		treeGrid.setDataProvider(treeDataProvider);
		addComponent(treeGrid);
		treeDataProvider.refreshAll();
	}
	
	private void addCommunicationItemToScenario(TreeGridMockedItem scenarioItem,
			CommunicationMocked communicationMocked) {
		if (communicationMocked.getConnection().getHost().equals(this.connectionMocked.getHost()) &&
				communicationMocked.getConnection().getPort() == this.connectionMocked.getPort()) {
			TreeGridMockedItem commItem = new TreeGridMockedItem(communicationMocked);
			listItems.add(commItem);
			treeScenarioItemByComm.put(communicationMocked, scenarioItem);
			treeCommItemByComm.put(communicationMocked, commItem);
			
			commItemsByScenarioItem.computeIfAbsent(
					scenarioItem, k -> new ArrayList<TreeGridMockedItem>()).add(commItem);
			
			
			treeData.addItem(scenarioItem, commItem);
			treeDataProvider.refreshItem(scenarioItem);
			treeDataProvider.refreshItem(commItem);
		}
	}
	
	private void removeCommunicationItemFromScenario(CommunicationMocked communicationMocked) {
		communicationMocked.getScenario().getCommunicationsMocked().remove(communicationMocked);
		scenarioDaoManager.update(communicationMocked.getScenario());
	}

	private TreeGridMockedItem addScenarioToRootExceptUndifined(Scenario scenario) {
		//undefined scenario already added
		if (!scenario.equals(DaoManagerByModel.getUNDEFINED_SCENARIO())) {
			return addScenarioToRoot(scenario);
		}
		return undefinedScenarioItem;
	}

	private TreeGridMockedItem addScenarioToRoot(Scenario scenario) {
		TreeGridMockedItem scenarioItem = new TreeGridMockedItem(scenario);
		listItems.add(scenarioItem);
		treeScenarioItemByScenario.put(scenario, scenarioItem);
		treeData.addItem(root, scenarioItem);
		return scenarioItem;
	}

	protected void addTreeMapping() {
		treeGrid.addColumn(item -> item.getDisplay());
		treeGrid.addComponentColumn(this::buildEnableButton);
	}
	
	private Map<CommunicationMocked, SwitchWithEntity<CommunicationMocked>> switchByCommunicationMap = new HashMap<>();

	private Switch buildEnableButton(TreeGridMockedItem item) {
		if (item.isCommunication()) {
			CommunicationMocked communication = item.getCommunication();
			SwitchWithEntity<CommunicationMocked> switchWithEntity = new SwitchWithEntity<>(communication);
			switchWithEntity.setValue(communication.isActivated());
			switchWithEntity.addValueChangeListener(this::switchButtonClickedCommunication);
			switchByCommunicationMap.put(communication, switchWithEntity);
			return switchWithEntity;
		}
		return null;
	}

	public void switchButtonClickedCommunication(ValueChangeEvent<Boolean> event) {
		SwitchWithEntity<CommunicationMocked> switchWithEntity = 
				(SwitchWithEntity<CommunicationMocked>) event.getComponent();
		CommunicationMocked communication = switchWithEntity.getEntity();
		switchCommActivation(event.getValue(), communication);
	}

	public void treeItemClick(com.vaadin.ui.Grid.ItemClick<TreeGridMockedItem> event) {
		CommunicationMocked comm = event.getItem().getCommunication();
		selectedTreeItem = event.getItem();
		itemClicked.accept(comm);
		updateComboBoxValues(event.getItem());
	}
	
	private TreeGridMockedItem selectedItem;
	private transient Runnable refreshClickable;

	public TreeGridMockedItem getSelectedItem() {
		return selectedItem;
	}

	private void updateComboBoxValues(TreeGridMockedItem treeItem) {
		selectedItem = treeItem;
		this.refreshClickable.run();
		comboBox.clear();
		comboBox.setEmptySelectionCaption("...");
		if (treeItem.isRoot()) {
			comboBox.setItems(SmockerUI.getBundleValue(CREATE_SCENARIO));
		}
		else if (treeItem.isScenario()) {
			Scenario scenario = treeItem.getScenario();
			List<String> lisItems = new ArrayList<>();
			
			if (!scenario.getCommunicationsMocked().isEmpty()) {
				lisItems.add(SmockerUI.getBundleValue(REMOVE_COMMS));
				lisItems.add(SmockerUI.getBundleValue(TURN_ON_ALL));
				lisItems.add(SmockerUI.getBundleValue(TURN_OFF_ALL));
				if (listScenarios.size() >= 2) {
					lisItems.add(SmockerUI.getBundleValue(MOVE_ALL_COMMS));
				}
			}
			
			if (!scenario.equals(DaoManagerByModel.getUNDEFINED_SCENARIO())) {
				lisItems.add(SmockerUI.getBundleValue(RENAME_SCENARIO));
				lisItems.add(SmockerUI.getBundleValue(DELETE_SCENARIO));
			}
			
			comboBox.setItems(lisItems);
		}
		else if (treeItem.isCommunication()) {
			List<String> listItems = new ArrayList<>(); 
			listItems.addAll(Arrays.asList(SmockerUI.getBundleValue(MOVE_COMM), 
					SmockerUI.getBundleValue(RENAME_COMM), 
					SmockerUI.getBundleValue(DELETE_COMM),
					SmockerUI.getBundleValue(DUPLICATE_COMM)));
			List<CommunicationMocked> commsOfScenario = getListCommOrdered(treeItem.getCommunication().getScenario());
			int indeoxOfComm = commsOfScenario.indexOf(treeItem.getCommunication());
			if (indeoxOfComm < commsOfScenario.size() - 1) {
				listItems.add(SmockerUI.getBundleValue(MOVE_DOWN));
			}
			
			if (indeoxOfComm > 0) {
				listItems.add(SmockerUI.getBundleValue(MOVE_UP));
			}
			comboBox.setItems(listItems);
		}
	}


	public void comboSelected(SingleSelectionEvent<String> event) {
		
		List<String> listScenarioName = listScenarios.stream()
				.map(Scenario::getName)
				.collect(Collectors.toList());
		
		if (event.getValue() != null && event.getValue().equals(SmockerUI.getBundleValue(MOVE_COMM))){
			CommunicationMocked selectedComm = selectedTreeItem.getCommunication();
			if (selectedComm != null) {
				listScenarioName.remove(selectedComm.getScenario().getName());
			}
			Dialog.displaySelectableListBox(SmockerUI.getBundleValue(MOVE_COMM), listScenarioName, this::scenarioMovSelected);
		}
		else if (event.getValue() != null && event.getValue().equals(SmockerUI.getBundleValue(MOVE_UP))){
			CommunicationMocked selectedComm = selectedTreeItem.getCommunication();
			if (selectedComm != null) {
				moveUp(selectedComm);
			}
		}
		else if (event.getValue() != null && event.getValue().equals(SmockerUI.getBundleValue(MOVE_DOWN))){
			CommunicationMocked selectedComm = selectedTreeItem.getCommunication();
			if (selectedComm != null) {
				moveDown(selectedComm);
			}
		}
		else if (event.getValue() != null && event.getValue().equals(SmockerUI.getBundleValue(DUPLICATE_COMM))){
			CommunicationMocked selectedComm = selectedTreeItem.getCommunication();
			duplicateComm(selectedComm);
		}
		else if (event.getValue() != null && event.getValue().equals(SmockerUI.getBundleValue(CREATE_SCENARIO))){
			Dialog.displayCreateStringBox(SmockerUI.getBundleValue(CREATE_SCENARIO), this::scenarioCreated);
		}
		else if (event.getValue() != null && event.getValue().equals(SmockerUI.getBundleValue(RENAME_SCENARIO))){
			Dialog.displayCreateStringBox(SmockerUI.getBundleValue(RENAME_SCENARIO), this::scenarioRenamed);
		}
		else if (event.getValue() != null && event.getValue().equals(SmockerUI.getBundleValue(DELETE_SCENARIO))){
			Dialog.ask(SmockerUI.getBundleValue("deleteScenarioQuestion"), SmockerUI.getBundleValue(DELETE_SCENARIO), this::scenarioDeleted, null);
		}
		else if (event.getValue() != null && event.getValue().equals(SmockerUI.getBundleValue(REMOVE_COMMS))){
			Dialog.ask(SmockerUI.getBundleValue(REMOVE_COMMS), SmockerUI.getBundleValue(REMOVE_COMMS), this::removeComms, null);
		}
		else if (event.getValue() != null && event.getValue().equals(SmockerUI.getBundleValue(MOVE_ALL_COMMS))){
			Scenario selectedScenario = selectedTreeItem.getScenario();
			if (selectedScenario != null) {
				listScenarioName.remove(selectedScenario.getName());
			}
			Dialog.displaySelectableListBox(SmockerUI.getBundleValue(MOVE_ALL_COMMS), listScenarioName, this::moveAllComms);
		}
		
		
		else if (event.getValue() != null && event.getValue().equals(SmockerUI.getBundleValue(RENAME_COMM))){
			Dialog.displayCreateStringBox(SmockerUI.getBundleValue(RENAME_COMM), this::commRenamed);
		}
		else if (event.getValue() != null && event.getValue().equals(SmockerUI.getBundleValue(DELETE_COMM))){
			Dialog.ask(SmockerUI.getBundleValue("deleteCommunicationQuestion"), 
					SmockerUI.getBundleValue(DELETE_COMM), this::commDeleted, null);
		}
		else if (event.getValue() != null && event.getValue().equals(SmockerUI.getBundleValue(TURN_ON_ALL))){
			switchAllCommunicationInsideScenario(true);
		}
		else if (event.getValue() != null && event.getValue().equals(SmockerUI.getBundleValue(TURN_OFF_ALL))){
			switchAllCommunicationInsideScenario(false);
		}
	}
	
	private void duplicateComm(CommunicationMocked selectedComm) {
		TreeGridMockedItem scenarioItem = treeScenarioItemByComm.get(selectedComm);
		Scenario scenario = selectedComm.getScenario();
		CommunicationMocked cloneCommunication = ScenarioUploader.cloneCommunication(selectedComm);
		cloneCommunication.setScenario(scenario);
		cloneCommunication.setConnection(selectedComm.getConnection());
		cloneCommunication = communicationDaoManager.create(cloneCommunication);
		selectedComm.getConnection().getCommunications().add(cloneCommunication);
		connectionDaoManager.update(selectedComm.getConnection());
		scenario.getCommunicationsMocked().add(cloneCommunication);
		scenarioDaoManager.update(scenario);
		addCommunicationItemToScenario(scenarioItem, cloneCommunication);
		treeDataProvider.refreshAll();
	}

	private void moveDown(CommunicationMocked selectedComm) {
		refreshConnectionMocked();
		
		List<CommunicationMocked> commsOfScenario = getListCommOrdered(selectedComm.getScenario());
		if (commsOfScenario.size() >= 2) {
			int indexOfSelected = commsOfScenario.indexOf(selectedComm);
			if (indexOfSelected < commsOfScenario.size()) {
				//swap index 
				CommunicationMocked nextComm = commsOfScenario.get(indexOfSelected + 1);
				long indexNext = nextComm.getIndex();
				nextComm.setIndex(selectedComm.getIndex());
				selectedComm.setIndex(indexNext);
				communicationDaoManager.update(selectedComm);
				communicationDaoManager.update(nextComm);
				
				reorderScenarioItem(selectedComm, selectedComm.getScenario());	
			}
		}
		treeDataProvider.refreshAll();
	}

	private void reorderScenarioItem(CommunicationMocked selectedComm, Scenario scenario) {
		TreeGridMockedItem treeGridMockedScenarioItem = treeScenarioItemByScenario.get(scenario);
		List<TreeGridMockedItem> children = treeData.getChildren(treeGridMockedScenarioItem);
		List<TreeGridMockedItem> itemsToRemove = new ArrayList<>();
		for (TreeGridMockedItem treeGridMockedItem : children) {
			itemsToRemove.add(treeGridMockedItem);
		}
		for (TreeGridMockedItem treeGridMockedItem : itemsToRemove) {
			treeData.removeItem(treeGridMockedItem);
		}
		List<CommunicationMocked> listCommOrdered = getListCommOrdered(selectedComm.getScenario());
		for (CommunicationMocked communicationMocked : listCommOrdered) {
			addCommunicationItemToScenario(treeGridMockedScenarioItem, communicationMocked);
		}
	}
	
	
	private List<CommunicationMocked> getListCommOrdered(Scenario scenario) {
		return scenario.getCommunicationsMocked()
				.stream().sorted( 
						(a, b) -> Long.compare(a.getIndex(), b.getIndex())).collect(Collectors.toList());
	}

	private void refreshConnectionMocked() {
		connectionMocked = connectionDaoManager.findById(connectionMocked.getId());
	}

	private void moveUp(CommunicationMocked selectedComm) {
		refreshConnectionMocked();

		List<CommunicationMocked> commsOfScenario = getListCommOrdered(selectedComm.getScenario());
		if (commsOfScenario.size() >= 2) {
			int indexOfSelected = commsOfScenario.indexOf(selectedComm);
			if (indexOfSelected > 0) {
				//swap index 
				CommunicationMocked previousComm = commsOfScenario.get(indexOfSelected - 1);
				long indexPrevious = previousComm.getIndex();
				previousComm.setIndex(selectedComm.getIndex());
				selectedComm.setIndex(indexPrevious);
				communicationDaoManager.update(selectedComm);
				communicationDaoManager.update(previousComm);

				reorderScenarioItem(selectedComm, selectedComm.getScenario());	
			}
		}
		treeDataProvider.refreshAll();
	}

	public void switchAllCommunicationInsideScenario(boolean value) {
		if (selectedTreeItem != null && selectedTreeItem.isScenario()) {
			Scenario scenario = selectedTreeItem.getScenario();
			List<CommunicationMocked> communicationsMocked = scenario.getCommunicationsMocked();
			for (CommunicationMocked communicationMocked : communicationsMocked) {
				switchCommActivation(value, communicationMocked);
				SwitchWithEntity<CommunicationMocked> switchWithEntity = switchByCommunicationMap.get(communicationMocked);
				if (switchWithEntity != null) {
					switchWithEntity.setValue(value);
				}
			}
		}
		treeDataProvider.refreshAll();
	}
	
	private void switchCommActivation(Boolean activated, CommunicationMocked communication) {
		if (communication.isActivated() ^ activated) {
			communication.setActivated(activated);
			communicationDaoManager.update(communication);
		}
	}

	
	public void scenarioMovSelected(String scenarioName) {
		List<Scenario> listScenariosToReturn = getScenarioByName(scenarioName);
		if (!listScenariosToReturn.isEmpty()) {
			Scenario targetScenario = listScenariosToReturn.get(0);
			CommunicationMocked communication = selectedTreeItem.getCommunication();
			moveCommToScenario(targetScenario, communication);
			treeDataProvider.refreshAll();
		}
		else {
			logger.error("Not able to find scenario " + scenarioName);
		}
	}

	private List<Scenario> getScenarioByName(String scenarioName) {
		return scenarioDaoManager.queryList("SELECT s FROM Scenario s WHERE s.name = '" + scenarioName +"'");
	}
	
	public void moveAllComms(String scenarioName) {
		List<Scenario> listScenariosFromQuery = getScenarioByName(scenarioName);
		if (!listScenariosFromQuery.isEmpty()) {
			Scenario targetScenario = listScenariosFromQuery.get(0);
			Scenario actualScenario = selectedTreeItem.getScenario();
			List<CommunicationMocked> communicationsMocked = actualScenario.getCommunicationsMocked();
			List<CommunicationMocked> newList = new ArrayList<>(communicationsMocked);
			for (CommunicationMocked communicationMocked : newList) {
				moveCommToScenario(targetScenario, communicationMocked);
			}
			treeDataProvider.refreshAll();
		}
		else {
			logger.error("Not able to find scenario " + scenarioName);
		}
	}

	private void moveCommToScenario(Scenario targetScenario, CommunicationMocked communication) {
		//remove previous assoc
		Scenario initialScenario = communication.getScenario();
		if (initialScenario != targetScenario) {
			List<CommunicationMocked> communicationsMockedInOldScenario = initialScenario.getCommunicationsMocked();
			if (communicationsMockedInOldScenario.contains(communication)) {
				communicationsMockedInOldScenario.remove(communication);
			}
			
			List<CommunicationMocked> communicationsMocked = targetScenario.getCommunicationsMocked();
			if (!communicationsMocked.contains(communication)) {
				communicationsMocked.add(communication);
				communication.setScenario(targetScenario);
				//targetScenario.setCommunicationsMocked(communicationsMocked);
				
			}
			
			targetScenario = scenarioDaoManager.update(targetScenario);
			communicationDaoManager.update(communication);

			TreeGridMockedItem commItem = treeCommItemByComm.get(communication);
			TreeGridMockedItem scenarioItemTarget = treeScenarioItemByScenario.get(targetScenario);

			if (commItem != null && scenarioItemTarget != null) {
				treeData.removeItem(commItem);
				addCommunicationItemToScenario(scenarioItemTarget, communication);
			}
		}
	}

	public void scenarioCreated(String scenarioName) {
		if (scenarioName.equals(SmockerUI.getBundleValue("undefined"))) {
			Dialog.warning(SmockerUI.getBundleValue("warning_invalid_Scenario_name"));
			return;
		}
		Scenario scenario = new Scenario();
		scenario.setName(scenarioName);
		scenario.setHost(connectionMocked.getHost());
		scenario.setPort(connectionMocked.getPort());
		//scenario.setClassQualifiedName(connectionMocked.getJavaApplication().getClassQualifiedName());
		scenario = scenarioDaoManager.create(scenario);
		listScenarios.add(scenario);
		addScenarioToRootExceptUndifined(scenario);
		treeDataProvider.refreshAll();
	}

	public void scenarioRenamed(String scenarioName) {
		if (selectedTreeItem != null && selectedTreeItem.isScenario()) {
			selectedTreeItem.getScenario().setName(scenarioName);
			scenarioDaoManager.update(selectedTreeItem.getScenario());
			treeDataProvider.refreshAll();
		}
	}
	
	public void scenarioDeleted() {
		if (selectedTreeItem != null && selectedTreeItem.isScenario()) {
			Scenario scenarioSelected = selectedTreeItem.getScenario();
			
			
			List<CommunicationMocked> communicationsMocked = scenarioSelected.getCommunicationsMocked();
			List<CommunicationMocked> newList = new ArrayList<>(communicationsMocked);

			scenarioSelected.getCommunicationsMocked().clear();
			scenarioDaoManager.update(scenarioSelected);
			
			for (CommunicationMocked communicationMocked : newList) {
				communicationMocked.setScenario(null);
				communicationDaoManager.update(communicationMocked);
			}

			scenarioDaoManager.delete(scenarioSelected);
			
			for (CommunicationMocked communicationMocked : newList) {
				communicationMocked.getConnection().getCommunications().remove(communicationMocked);
				connectionDaoManager.update(communicationMocked.getConnection());
			}
			
//			for (CommunicationMocked communicationMocked : newList) {
//				communicationDaoManager.delete(communicationMocked);
//			}
			
			TreeGridMockedItem scenarioItemTarget = treeScenarioItemByScenario.get(scenarioSelected);
			treeData.removeItem(scenarioItemTarget);
			treeScenarioItemByScenario.remove(scenarioSelected);
			treeDataProvider.refreshAll();
		}
	}
	
	public void removeComms() {
		if (selectedTreeItem != null && selectedTreeItem.isScenario()) {
			List<CommunicationMocked> communicationsMocked = selectedTreeItem.getScenario().getCommunicationsMocked();
			List<CommunicationMocked> listCommsToRemove = new ArrayList<>();
			
			for (CommunicationMocked communicationMocked : communicationsMocked) {
				TreeGridMockedItem commItem = treeCommItemByComm.get(communicationMocked);
				if (commItem != null) {
					listCommsToRemove.add(communicationMocked);
					treeData.removeItem(commItem);
				}
			}
			
			selectedTreeItem.getScenario().getCommunicationsMocked().clear();
			scenarioDaoManager.update(selectedTreeItem.getScenario());
			
			//all comm are in the same connection
			if (listCommsToRemove.size() > 0) {
				ConnectionMocked connection = listCommsToRemove.get(0).getConnection();
				connection.getCommunications().clear();
				connectionDaoManager.update(connection);
			}
			
			
			treeDataProvider.refreshAll();
		}
	}
	
	public void commRenamed(String commName) {
		if (selectedTreeItem != null && selectedTreeItem.isCommunication()) {
			selectedTreeItem.getCommunication().setName(commName);
			communicationDaoManager.update(selectedTreeItem.getCommunication());
			treeDataProvider.refreshAll();
		}
	}
	
	public void commDeleted () {
		if (selectedTreeItem != null && selectedTreeItem.isCommunication()) {
			treeData.removeItem(selectedTreeItem);
			
			CommunicationMocked communication = selectedTreeItem.getCommunication();
			communication = communicationDaoManager.findById(communication.getId());
			removeCommunicationItemFromScenario(communication);
			connectionMocked.getCommunications().remove(communication);
			connectionDaoManager.update(connectionMocked);
			treeDataProvider.refreshAll();
		}
	}

	public void communicationMockedCreated(CommunicationMocked communicationMocked) {
		TreeGridMockedItem treeGridMockedItem = treeScenarioItemByScenario.get(communicationMocked.getScenario());
		addCommunicationItemToScenario(treeGridMockedItem, communicationMocked);
	}
}
