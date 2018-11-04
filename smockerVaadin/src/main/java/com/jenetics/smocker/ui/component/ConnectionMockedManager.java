package com.jenetics.smocker.ui.component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

	private static final String CREATE_SCENARIO = "createScenario";
	private static final String RENAME_COMM = "renameComm";
	private static final String MOVE_COMM = "moveComm";
	private static final String DELETE_SCENARIO = "deleteScenario";
	private static final String MOVE_ALL_COMMS = "moveAllComms";
	private static final String REMOVE_COMMS = "removeComms";
	private static final String TURN_OFF_ALL = "turnOffAll";
	private static final String TURN_ON_ALL = "turnOnAll";
	private static final String RENAME_SCENARIO = "renameScenario";
	private TreeGrid<TreeGridMockedItem> treeGrid;
	private TreeData<TreeGridMockedItem> treeData;
	private TreeDataProvider<TreeGridMockedItem> treeDataProvider;
	private ConnectionMocked connectionMocked;
	private Consumer<CommunicationMocked> itemClicked;

	private transient DaoManager<Scenario> scenarioDaoManager = DaoManagerByModel.getDaoManager(Scenario.class);
	private transient DaoManager<CommunicationMocked> communicationDaoManager = DaoManagerByModel.getDaoManager(CommunicationMocked.class);
	private transient DaoManager<ConnectionMocked> connectionDaoManager = DaoManagerByModel.getDaoManager(ConnectionMocked.class);
	private ComboBox comboBox;


	@Inject
	private Logger logger;

	public ConnectionMockedManager(ConnectionMocked connectionMocked, Consumer<CommunicationMocked> itemClicked) {
		super();
		this.itemClicked = itemClicked;
		this.connectionMocked = connectionMocked;
		treeGrid = new TreeGrid<>();
		treeGrid.setSelectionMode(SelectionMode.SINGLE);
		treeGrid.setSizeFull();

		treeData = new TreeData<>();
		treeDataProvider = new TreeDataProvider<>(treeData);
		treeGrid.setDataProvider(treeDataProvider);

		treeGrid.addItemClickListener(this::treeItemClick);
		treeGrid.removeHeaderRow(0);
		treeGrid.setSizeFull();

		fillCommunication();
		addTreeMapping();

		addComponent(treeGrid);
		setSizeFull();
	}

	@Override
	public ActionContainer buildActionContainer() {

		comboBox = new ComboBox<>();
		comboBox.addSelectionListener(this::comboSelected);
	
		ActionContainerBuilder builder = new ActionContainerBuilder(null)
				.withSingleComponent(comboBox);
		
		ActionContainer actionContainer = builder.build();
		return actionContainer;
	}

	private Map<Scenario, List<CommunicationMocked>> commsByScenario = new HashMap<>();
	private List<Scenario> listScenarios;
	private TreeGridMockedItem selectedTreeItem;
	private TreeGridMockedItem root;
	
	private Map<CommunicationMocked, TreeGridMockedItem> treeScenarioItemByComm = new HashMap<>();
	private Map<CommunicationMocked, TreeGridMockedItem> treeCommItemByComm = new HashMap<>();
	private Map<Scenario, TreeGridMockedItem> treeScenarioItemByScenario = new HashMap<>();
	
	
	private TreeGridMockedItem undefinedScenarioItem;
	
	public ComboBox getComboBox() {
		return comboBox;
	}
	
	public void fillCommunication() {
		treeData.clear();
		commsByScenario.clear();
		root = new TreeGridMockedItem();
		treeData.addItem(null, root);

		Scenario undefinedScenario = DaoManagerByModel.getUNDEFINED_SCENARIO();
		undefinedScenarioItem = addScenarioToRoot(undefinedScenario);
		addScenarioToRootExceptUndifined(undefinedScenario);
		
		Set<CommunicationMocked> communications = connectionMocked.getCommunications();
		//fill the map
		for (CommunicationMocked communication : communications) {
			Scenario scenario = communication.getScenario();
			if (!commsByScenario.containsKey(scenario)) {
				commsByScenario.put(scenario, new ArrayList<>());
			}
			commsByScenario.get(scenario).add(communication);
		}

		for (Map.Entry<Scenario, List<CommunicationMocked>> entry : commsByScenario.entrySet()) {
			Scenario scenario = entry.getKey();
			List<CommunicationMocked> associatedComms = entry.getValue();
			TreeGridMockedItem scenarioItem = addScenarioToRootExceptUndifined(scenario);
			for (CommunicationMocked communicationMocked : associatedComms) {
				addCommunicationItemToScenario(scenarioItem, communicationMocked);
			}
		}

		//add remaining scenario
		listScenarios = scenarioDaoManager.listAll();
		for (Scenario scenario : listScenarios) {
			if (!commsByScenario.containsKey(scenario)) {
				addScenarioToRootExceptUndifined(scenario);
			}
		}
		treeDataProvider.refreshAll();
	}

	private void addCommunicationItemToScenario(TreeGridMockedItem scenarioItem,
			CommunicationMocked communicationMocked) {
		TreeGridMockedItem commItem = new TreeGridMockedItem(communicationMocked);
		treeScenarioItemByComm.put(communicationMocked, scenarioItem);
		treeCommItemByComm.put(communicationMocked, commItem);
		treeData.addItem(scenarioItem, commItem);
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

	private void updateComboBoxValues(TreeGridMockedItem treeItem) {
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
			comboBox.setItems(SmockerUI.getBundleValue(MOVE_COMM), 
					SmockerUI.getBundleValue(RENAME_COMM));
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
		else if (event.getValue() != null && event.getValue().equals(SmockerUI.getBundleValue(TURN_ON_ALL))){
			switchAllCommunicationInsideScenario(true);
		}
		else if (event.getValue() != null && event.getValue().equals(SmockerUI.getBundleValue(TURN_OFF_ALL))){
			switchAllCommunicationInsideScenario(false);
		}
	}
	
	public void switchAllCommunicationInsideScenario(boolean value) {
		if (selectedTreeItem != null && selectedTreeItem.isScenario()) {
			Scenario scenario = selectedTreeItem.getScenario();
			Set<CommunicationMocked> communicationsMocked = scenario.getCommunicationsMocked();
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
		List<Scenario> listScenariosRet = 
				scenarioDaoManager.queryList("SELECT s FROM Scenario s WHERE s.name = '" + scenarioName +"'");
		return listScenariosRet;
	}
	
	public void moveAllComms(String scenarioName) {
		List<Scenario> listScenariosFromQuery = getScenarioByName(scenarioName);
		if (!listScenariosFromQuery.isEmpty()) {
			Scenario targetScenario = listScenariosFromQuery.get(0);
			Scenario actualScenario = selectedTreeItem.getScenario();
			Set<CommunicationMocked> communicationsMocked = actualScenario.getCommunicationsMocked();
			for (CommunicationMocked communicationMocked : communicationsMocked) {
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
			Set<CommunicationMocked> communicationsMockedInOldScenario = initialScenario.getCommunicationsMocked();
			if (communicationsMockedInOldScenario.contains(communication)) {
				communicationsMockedInOldScenario.remove(communication);
			}
			
			Set<CommunicationMocked> communicationsMocked = targetScenario.getCommunicationsMocked();
			if (!communicationsMocked.contains(communication)) {
				communicationsMocked.add(communication);
				targetScenario.setCommunicationsMocked(communicationsMocked);
				communication.setScenario(targetScenario);
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
			Set<CommunicationMocked> communicationsMocked = scenarioSelected.getCommunicationsMocked();
			for (CommunicationMocked communicationMocked : communicationsMocked) {
				moveCommToScenario(DaoManagerByModel.getUNDEFINED_SCENARIO(), communicationMocked);
			}
			
			scenarioDaoManager.deleteById(scenarioSelected.getId());
			
			TreeGridMockedItem scenarioItemTarget = treeScenarioItemByScenario.get(scenarioSelected);
			treeData.removeItem(scenarioItemTarget);
			treeDataProvider.refreshAll();
			
			listScenarios = scenarioDaoManager.listAll();
		}
	}
	
	public void removeComms() {
		if (selectedTreeItem != null && selectedTreeItem.isScenario()) {
			Set<CommunicationMocked> communicationsMocked = selectedTreeItem.getScenario().getCommunicationsMocked();
			List<CommunicationMocked> listCommsToRemove = new ArrayList<>();
			
			for (CommunicationMocked communicationMocked : communicationsMocked) {
				TreeGridMockedItem commItem = treeCommItemByComm.get(communicationMocked);
				if (commItem != null) {
					listCommsToRemove.add(communicationMocked);
					treeData.removeItem(commItem);
				}
			}
			//all comm are in the same connection
			if (listCommsToRemove.size() > 0) {
				ConnectionMocked connection = listCommsToRemove.get(0).getConnection();
				connection.getCommunications().clear();
				connectionDaoManager.update(connection);
			}
			
			selectedTreeItem.getScenario().getCommunicationsMocked().clear();
			scenarioDaoManager.update(selectedTreeItem.getScenario());
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
}
