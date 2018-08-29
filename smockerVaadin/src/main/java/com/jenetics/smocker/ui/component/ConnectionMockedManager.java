package com.jenetics.smocker.ui.component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import org.vaadin.easyapp.util.ActionContainer;
import org.vaadin.easyapp.util.ActionContainer.InsertPosition;
import org.vaadin.easyapp.util.ActionContainer.Position;
import org.vaadin.easyapp.util.ActionContainerBuilder;
import org.vaadin.easyapp.util.EasyAppLayout;

import com.jenetics.smocker.dao.DaoManager;
import com.jenetics.smocker.dao.DaoManagerByModel;
import com.jenetics.smocker.model.CommunicationMocked;
import com.jenetics.smocker.model.ConnectionMocked;
import com.jenetics.smocker.model.Scenario;
import com.jenetics.smocker.ui.SmockerUI;
import com.jenetics.smocker.ui.util.ButtonWithIEntity;
import com.jenetics.smocker.ui.util.TreeGridMockedItem;
import com.vaadin.data.TreeData;
import com.vaadin.data.provider.TreeDataProvider;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.ui.Button;
import com.vaadin.ui.TreeGrid;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Grid.SelectionMode;

public class ConnectionMockedManager extends EasyAppLayout {

	private TreeGrid<TreeGridMockedItem> treeGrid;
	private TreeData<TreeGridMockedItem> treeData;
	private TreeDataProvider<TreeGridMockedItem> treeDataProvider;
	private ConnectionMocked connectionMocked;
	private Consumer<CommunicationMocked> itemClicked;
	
	private DaoManager<Scenario> scenarioDaoManager = DaoManagerByModel.getDaoManager(Scenario.class);
	private DaoManager<CommunicationMocked> communicationDaoManager = DaoManagerByModel.getDaoManager(CommunicationMocked.class);
	private ComboBox comboBox;
	
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
		
		comboBox.setEmptySelectionAllowed(false);
		comboBox.setEmptySelectionCaption("...");
		comboBox.setItems("Mozilla Firefox", "Opera",
		        "Apple Safari", "Microsoft Edge");
		
		//comboBox.setNullSelectionAllowed(false);
		
		ActionContainerBuilder builder = new ActionContainerBuilder(null)
				.addButton(VaadinIcons.ERASER, this::buttonClick, Position.RIGHT)
				.addComponent(comboBox, Position.RIGHT, InsertPosition.AFTER);
		return builder.build();
	}
	
	private Map<Long, TreeGridMockedItem> scenarioItemByScenarioId = new HashMap<>();
	private Map<Scenario, List<CommunicationMocked>> commsByScenario = new HashMap<>();
	
	private void fillCommunication() {
		treeData.clear();
		TreeGridMockedItem root = new TreeGridMockedItem(true, SmockerUI.getBundleValue("root"), null);
		treeData.addItem(null, root);
		
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
		    TreeGridMockedItem scenarioItem = new TreeGridMockedItem(true, scenario.getName(), null);
		    treeData.addItem(root, scenarioItem);
		    for (CommunicationMocked communicationMocked : associatedComms) {
		    	TreeGridMockedItem commItem = new TreeGridMockedItem(false, SmockerUI.getBundleValue("unamed"), communicationMocked);
		    	treeData.addItem(scenarioItem, commItem);
			}
		}
		
//		
		//add remaining scenario
		List<Scenario> listScenarios = scenarioDaoManager.listAll();
		for (Scenario scenario : listScenarios) {
			if (!commsByScenario.containsKey(scenario)) {
				TreeGridMockedItem scenarioItem = new TreeGridMockedItem(true, scenario.getName(), null);
				scenarioItemByScenarioId.put(scenario.getId(), scenarioItem);
				treeData.addItem(root, scenarioItem);
			}
		}
//		
//		Set<CommunicationMocked> communications = connectionMocked.getCommunications();
//		for (CommunicationMocked communication : communications) {
//			Long scenarioId;
//			if (communication.getScenario() != null) {
//				scenarioId = communication.getScenario().getId();
//			}
//			communication.getScenario().getId();
//			treeData.addItem(root, new TreeGridMockedItem(false, SmockerUI.getBundleValue("unamed"), communication));
//		}
		treeDataProvider.refreshAll();
	}
	
	protected void addTreeMapping() {
		treeGrid.addColumn(item -> item.getName());
		treeGrid.addComponentColumn(this::buildEnableButton);
	}
	
	
	private Button buildEnableButton(TreeGridMockedItem item) {
		if (!item.isRoot()) {
			ButtonWithIEntity<CommunicationMocked> buttonWithId = new ButtonWithIEntity<>(item.getCommunication());
			buttonWithId.setIcon(VaadinIcons.PLAY);
			buttonWithId.setDescription(SmockerUI.getBundleValue("enable"));
			buttonWithId.addClickListener(this::enableButtonClicked);
			return buttonWithId;
		}
		return null;
    }
	
	public void enableButtonClicked(ClickEvent event) {
		ButtonWithIEntity<CommunicationMocked> buttonWithEntity = (ButtonWithIEntity<CommunicationMocked>) event.getSource();
		CommunicationMocked communicationMockedSelected = buttonWithEntity.getEntity();
	}
	
	 public void buttonClick(ClickEvent event) {
		 System.out.println("click");
	 }
	 
	 public void treeItemClick(com.vaadin.ui.Grid.ItemClick<TreeGridMockedItem> event) {
		CommunicationMocked comm = event.getItem().getCommunication();
		itemClicked.accept(comm);
	 }
}
