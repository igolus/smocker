package com.jenetics.smocker.ui.component;

import java.util.Set;

import org.vaadin.easyapp.util.ActionContainer;
import org.vaadin.easyapp.util.ActionContainerBuilder;
import org.vaadin.easyapp.util.EasyAppLayout;

import com.jenetics.smocker.model.Communication;
import com.jenetics.smocker.model.Connection;
import com.jenetics.smocker.ui.netdisplayer.ComponentWithDisplayChange;
import com.jenetics.smocker.ui.netdisplayer.NetDisplayerFactoryInput;
import com.jenetics.smocker.ui.netdisplayer.NetDisplayerFactoryOutput;
import com.jenetics.smocker.util.NetworkReaderUtility;
import com.vaadin.data.TreeData;
import com.vaadin.data.provider.TreeDataProvider;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Tree;
import com.vaadin.ui.Tree.ItemClick;


@SuppressWarnings("serial")
public class ConnectionDetailsView extends EasyAppLayout {
	
	private static final String BUNDLE_NAME = "BundleUI";
	private Connection connection;
	private GridLayout grid = null;
	private Tree<Communication> menu;
	private TreeData<Communication> treeData;
	private TreeDataProvider<Communication> treeDataProvider;
	
	public ConnectionDetailsView(Connection connection) {
		super();
		HorizontalSplitPanel mainLayout = new HorizontalSplitPanel();
		
		this.connection = connection;

		menu = new Tree<>();
		treeData = new TreeData<>();
		treeDataProvider = new TreeDataProvider<>(treeData);
		menu.setDataProvider(treeDataProvider);
		
		fillCommunication();
		
		menu.addItemClickListener(this::treeItemClick);
		treeDataProvider.refreshAll();
		
		
		// Show all leaf nodes as disabled
		menu.setStyleGenerator(item -> {
		        return item.getDateTime().toString();
		    }
		);
		menu.setSizeFull();
		
		grid = new GridLayout(2, 1);
		grid.setSizeFull();
		grid.setSizeFull();


		mainLayout.setFirstComponent(menu);
		mainLayout.setSecondComponent(grid);
		mainLayout.setSplitPosition(20);
		mainLayout.setSizeFull();
		addComponent(mainLayout);
		setSizeFull();
		
	}

	private void fillCommunication() {
		treeData.clear();
		Set<Communication> communications = connection.getCommunications();
		for (Communication communication : communications) {
			treeData.addItem(null, communication);
		}
		treeDataProvider.refreshAll();
	}
	
	public void treeItemClick(ItemClick<Communication> event) {
		Communication comm = event.getItem();
		
		String request = NetworkReaderUtility.decode(comm.getRequest());
		ComponentWithDisplayChange componentWithDisplayChangeInput = NetDisplayerFactoryInput.getComponent(request);
		Component inputComponent = componentWithDisplayChangeInput.getComponent();
		inputComponent.setSizeFull();
		grid.removeComponent(0, 0);
		grid.addComponent(inputComponent, 0, 0);
		componentWithDisplayChangeInput.selectionValue(request);
		
		
		String response = NetworkReaderUtility.decode(comm.getResponse());
		ComponentWithDisplayChange componentWithDisplayChangeOutput = NetDisplayerFactoryOutput.getComponent(response);
		Component outputComponent = componentWithDisplayChangeOutput.getComponent();
		outputComponent.setSizeFull();
		grid.removeComponent(1, 0);
		grid.addComponent(outputComponent, 1, 0);
		componentWithDisplayChangeOutput.selectionValue(response);

	}
	
	public ActionContainer buildActionContainer() {
		ActionContainerBuilder builder = new ActionContainerBuilder(BUNDLE_NAME)
				.addButton("AddToMock_Button", VaadinIcons.PLUS, "AddToMock_ToolTip",  this::isSelected			
						, this::addToMock)
				.addButton("Clean_Button", VaadinIcons.MINUS, "Clean_ToolTip",  this::isSelected			
						, this::clean)
				.addButton("CleanAll_Button", VaadinIcons.MINUS, "CleanAll_ToolTip",  this::atLeastOneItem			
						, this::cleanAll)
				.addButton("Refresh_Button", VaadinIcons.REFRESH, "CleanAll_ToolTip",  this::always			
						, this::refresh)
				
				.setSearch(this::search);

		return builder.build();
	}
	
	public void addToMock(ClickEvent event) {
		Notification.show("Clean");
	}
	
	public void clean(ClickEvent event) {
		Notification.show("Clean");
	}
	
	public void cleanAll(ClickEvent event) {
		Notification.show("Clean");
	}
	
	public void refresh(ClickEvent event) {
		fillCommunication();
	}
	
	public boolean isSelected() {
		return menu.getSelectedItems().size() == 1;
	}
	
	public boolean always() {
		return true;
	}
	
	public boolean atLeastOneItem() {
		return treeData.getRootItems().size() > 1;
	}
	
	public void search(String searchValue) {
		Notification.show("Search for:" + searchValue);
	}
	
}
