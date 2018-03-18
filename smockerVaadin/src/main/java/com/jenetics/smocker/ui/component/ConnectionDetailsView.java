package com.jenetics.smocker.ui.component;

import java.util.Set;

import org.vaadin.easyapp.util.EasyAppLayout;

import com.jenetics.smocker.model.Communication;
import com.jenetics.smocker.model.Connection;
import com.jenetics.smocker.ui.netdisplayer.ComponentWithDisplayChange;
import com.jenetics.smocker.ui.netdisplayer.NetDisplayerFactoryInput;
import com.jenetics.smocker.ui.netdisplayer.NetDisplayerFactoryOutput;
import com.jenetics.smocker.util.NetworkReaderUtility;
import com.vaadin.data.TreeData;
import com.vaadin.data.provider.TreeDataProvider;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Tree;
import com.vaadin.ui.Tree.ItemClick;


public class ConnectionDetailsView extends EasyAppLayout {

	private Connection connection;
	private GridLayout grid = null;
	
	public ConnectionDetailsView(Connection connection) {
		super();
		HorizontalSplitPanel mainLayout = new HorizontalSplitPanel();
		
		this.connection = connection;
		
		Set<Communication> communications = connection.getCommunications();

		Tree<Communication> menu = new Tree<>();
		TreeData<Communication> treeData = new TreeData<>();
		TreeDataProvider<Communication> treeDataProvider = new TreeDataProvider<>(treeData);
		menu.setDataProvider(treeDataProvider);
		
		for (Communication communication : communications) {
			treeData.addItem(null, communication);
		}
		
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
		
	}
	
	public void treeItemClick(ItemClick<Communication> event) {
		Communication comm = event.getItem();
		String response = NetworkReaderUtility.decode(comm.getResponse());
		ComponentWithDisplayChange outputComponent = NetDisplayerFactoryOutput.getComponent(response);
		grid.removeComponent(1, 0);
		outputComponent.getComponent().setSizeFull();
		grid.addComponent(outputComponent.getComponent(), 1, 0);
		outputComponent.selectionValue(response);
		
		String request = NetworkReaderUtility.decode(comm.getRequest());
		ComponentWithDisplayChange inputComponent = NetDisplayerFactoryInput.getComponent(request);
		inputComponent.getComponent().setSizeFull();
		grid.removeComponent(0, 0);
		grid.addComponent(inputComponent.getComponent(), 0, 0);
		inputComponent.selectionValue(request);
	}
}
