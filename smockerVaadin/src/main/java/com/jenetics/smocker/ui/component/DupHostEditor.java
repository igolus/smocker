package com.jenetics.smocker.ui.component;

import java.util.Optional;

import com.jenetics.smocker.ui.SmockerUI;
import com.jenetics.smocker.ui.dialog.Dialog;
import com.jenetics.smocker.ui.util.DuplicateHost;
import com.vaadin.data.TreeData;
import com.vaadin.data.provider.TreeDataProvider;
import com.vaadin.event.selection.SelectionEvent;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.Grid.SelectionMode;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Tree;
import com.vaadin.ui.VerticalLayout;

import de.steinwedel.messagebox.ButtonType;
import de.steinwedel.messagebox.MessageBox;

public class DupHostEditor extends VerticalLayout implements BoxableItem<DuplicateHost> {
	private DuplicateHost duplicateHost = null;
	private TreeData<String> treeData;
	private TreeDataProvider<String> treeDataProvider;
	private String selectedHost;
	private MessageBox displayComponentBox;

	public DupHostEditor(DuplicateHost duplicateHost) {
		super();
		this.duplicateHost = duplicateHost;
		buildComponent();
	}

	private void buildComponent() {
		GridLayout gridLayout = new GridLayout(1, 2);

		HorizontalLayout layoutButtons = new HorizontalLayout();
		Button addButton = new Button(VaadinIcons.PLUS);
		addButton.setDescription(SmockerUI.getBundleValue("Add_Host_toolTip"));
		addButton.addClickListener(this::addHost);
		layoutButtons.addComponent(addButton);

		Button removeButton = new Button(VaadinIcons.MINUS);
		removeButton.setDescription(SmockerUI.getBundleValue("Remove_Host_toolTip"));
		removeButton.addClickListener(this::removeHost);
		layoutButtons.addComponent(removeButton);

		gridLayout.addComponent(layoutButtons, 0, 0);

		Tree<String> listHostTree = new Tree<>();
		listHostTree.setSelectionMode(SelectionMode.SINGLE);
		treeData = new TreeData<>();
		treeDataProvider = new TreeDataProvider<>(treeData);
		listHostTree.setDataProvider(treeDataProvider);
		listHostTree.addSelectionListener(this::hostSelected);

		refreshTree();
		listHostTree.setHeight("200px");
		gridLayout.addComponent(listHostTree, 0, 1);
		gridLayout.setRowExpandRatio(1, 1);

		gridLayout.setSizeFull();

		addComponent(gridLayout);
	}
	
	private void hostSelected(SelectionEvent<String> dupHost) {
		Optional<String> firstSelectedItem = dupHost.getFirstSelectedItem();
		if (firstSelectedItem.isPresent()) {
			selectedHost = firstSelectedItem.get();
		}
	}	

	private void addHost(ClickEvent event) {
		Dialog.displayCreateStringBox(SmockerUI.getBundleValue("New_Host"), hostName -> {
			duplicateHost.addHost(hostName);
			refreshTree();
		});
	}

	private void removeHost(ClickEvent event) {
		duplicateHost.removeHost(selectedHost);
		refreshTree();
	}

	private void refreshTree() {
		treeData.clear();
		for (String host : duplicateHost.getListDupHost()) {
			treeData.addItem(null, host);
		}
		treeDataProvider.refreshAll();
		manageOkButton();
	}

	@Override
	public DuplicateHost getItem() {
		return duplicateHost;
	}

	@Override
	public Component getComponent() {
		return this;
	}

	public void setBox(MessageBox displayComponentBox) {
		this.displayComponentBox = displayComponentBox;
		manageOkButton();
	}

	private void manageOkButton() {
		if (displayComponentBox != null && duplicateHost.getListDupHost().size() > 1) {
			displayComponentBox.getButton(ButtonType.OK).setEnabled(true);
		}
		else if (displayComponentBox != null) {
			displayComponentBox.getButton(ButtonType.OK).setEnabled(false);
		}
	}
}
