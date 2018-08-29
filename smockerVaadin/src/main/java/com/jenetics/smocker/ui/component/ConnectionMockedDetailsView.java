package com.jenetics.smocker.ui.component;

import java.util.Set;
import java.util.logging.Level;

import org.apache.commons.collections4.CollectionUtils;
import org.vaadin.aceeditor.AceEditor;
import org.vaadin.aceeditor.AceMode;
import org.vaadin.aceeditor.AceTheme;
import org.vaadin.easyapp.ui.ViewWithToolBar;
import org.vaadin.easyapp.util.ActionContainer;
import org.vaadin.easyapp.util.ActionContainer.InsertPosition;
import org.vaadin.easyapp.util.ActionContainerBuilder;
import org.vaadin.easyapp.util.EasyAppLayout;

import com.jenetics.smocker.dao.DaoManager;
import com.jenetics.smocker.dao.DaoManagerByModel;
import com.jenetics.smocker.dao.IDaoManager;
import com.jenetics.smocker.injector.BundleUI;
import com.jenetics.smocker.model.CommunicationMocked;
import com.jenetics.smocker.model.Connection;
import com.jenetics.smocker.model.ConnectionMocked;
import com.jenetics.smocker.model.JavaApplication;
import com.jenetics.smocker.ui.SmockerUI;
import com.jenetics.smocker.ui.component.javascript.JsEditor;
import com.jenetics.smocker.ui.component.javascript.JsTesterPanel;
import com.jenetics.smocker.ui.dialog.Dialog;
import com.jenetics.smocker.ui.util.ButtonWithIEntity;
import com.jenetics.smocker.ui.util.CommunicationMockedDateDisplay;
import com.jenetics.smocker.ui.util.TreeGridConnectionData;
import com.jenetics.smocker.ui.util.TreeGridMockedItem;
import com.jenetics.smocker.util.NetworkReaderUtility;
import com.jenetics.smocker.util.SmockerUtility;
import com.vaadin.data.TreeData;
import com.vaadin.data.provider.TreeDataProvider;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.Sizeable;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Grid.SelectionMode;
import com.vaadin.ui.TabSheet.SelectedTabChangeEvent;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.Tree;
import com.vaadin.ui.Tree.ItemClick;
import com.vaadin.ui.components.grid.ItemClickListener;
import com.vaadin.ui.TreeGrid;
import com.vaadin.ui.VerticalSplitPanel;
import com.vaadin.ui.Window;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;


@SuppressWarnings("serial")
public class ConnectionMockedDetailsView extends AbstractConnectionDetails {
	
	private ConnectionMocked connectionMocked;
	private TreeGrid<TreeGridMockedItem> treeGrid;
	private TreeData<TreeGridMockedItem> treeData;
	private TreeDataProvider<TreeGridMockedItem> treeDataProvider;
	private CommunicationMocked selectedCommunication = null;
	private HorizontalSplitPanel mainLayout = null;
	
	protected IDaoManager<ConnectionMocked> daoManagerConnection =  DaoManagerByModel.getDaoManager(ConnectionMocked.class);
	protected IDaoManager<CommunicationMocked> daoManagerCommunicationMocked = DaoManagerByModel.getDaoManager(CommunicationMocked.class);

	private TabSheet tabSheet;
	private JsEditor tabJs;
	private TextPanel selectedRequestPane;
	private TextPanel selectedResponsePane;
	private JsTesterPanel jsTesterPanel;

	public ConnectionMockedDetailsView(ConnectionMocked connectionMocked) {
		super();
		mainLayout = new HorizontalSplitPanel();
		
		this.connectionMocked = connectionMocked;
	
		ConnectionMockedManager connectionMockedManager = new ConnectionMockedManager(connectionMocked, this::commSelected);
		
		ViewWithToolBar view = new ViewWithToolBar(connectionMockedManager);
		mainLayout.setFirstComponent(view);
		mainLayout.setSplitPosition(23);
		mainLayout.setSizeFull();
		
		addComponent(mainLayout);

		setSizeFull();
	}
	
	public TextPanel getSelectedResponsePane() {
		return selectedResponsePane;
	}
	
	public CommunicationMocked getSelectedCommunication() {
		return selectedCommunication;
	}
	
	public TextPanel getSelectedRequestPane() {
		return selectedRequestPane;
	}
	
	protected void addTreeMapping() {
		treeGrid.addColumn(item -> item.getName());
		treeGrid.addComponentColumn(this::buildEnableButton);
	}
	
	
	private Button buildEnableButton(TreeGridMockedItem item) {
		if (!item.isRoot()) {
			ButtonWithIEntity<CommunicationMocked> buttonWithId = new ButtonWithIEntity<>(item.getCommunication());
			buttonWithId.setCaption("Enable");
			buttonWithId.addClickListener(this::enableButtonClicked);
			return buttonWithId;
		}
		return null;
    }
	
	public void enableButtonClicked(ClickEvent event) {
		ButtonWithIEntity<CommunicationMocked> buttonWithEntity = (ButtonWithIEntity<CommunicationMocked>) event.getSource();
		CommunicationMocked communicationMockedSelected = buttonWithEntity.getEntity();
	}

	private void fillCommunication() {
		treeData.clear();
		TreeGridMockedItem root = new TreeGridMockedItem(true, "scenario1", null);
		treeData.addItem(null, root);
		
		Set<CommunicationMocked> communications = connectionMocked.getCommunications();
		for (CommunicationMocked communication : communications) {
			treeData.addItem(root, new TreeGridMockedItem(false, "unamed", communication));
		}
		treeDataProvider.refreshAll();
	}
	
	public void commSelected(CommunicationMocked comm) {
		if (comm != null) {
			String request = NetworkReaderUtility.decode(comm.getRequest());
			String response = NetworkReaderUtility.decode(comm.getResponse());
			String sourceJS = comm.getSourceJs();
			
			tabSheet = new TabSheet();
			tabSheet.setSizeFull();
			
			selectedRequestPane =  addTextAreaToTabSheet(request, "Input", tabSheet);
			selectedResponsePane = addTextAreaToTabSheet(response, "Output", tabSheet);	
			
			tabJs = new JsEditor(comm, selectedRequestPane, selectedResponsePane);
			if (sourceJS != null) {
				tabJs.setJSSource(sourceJS);
			}
			
			tabJs.setSizeFull();
			tabSheet.addTab(tabJs, SmockerUI.getBundleValue("NodeEditor"));
			
			jsTesterPanel = new JsTesterPanel(request, tabJs, comm.getDateTime());
			String inputForTest = comm.getInputForTest();
			if (inputForTest != null) {
				jsTesterPanel.setSourceInput(NetworkReaderUtility.decode(inputForTest));
			}
			
			ViewWithToolBar jsView = new ViewWithToolBar(jsTesterPanel);
			jsView.setSizeFull();
			tabSheet.addTab(jsView, SmockerUI.getBundleValue("TesterPanel"));
			
			
			tabSheet.addSelectedTabChangeListener(this::tabChanged);
			
			mainLayout.setSecondComponent(tabSheet);
			
			selectedCommunication = comm;
		}
		refreshClickable();
	}
	
	public void tabChanged(SelectedTabChangeEvent event) {
		if (refreshClickable != null) {
			refreshClickable.run();
		}
	}

	private TextPanel addTextAreaToTabSheet(String request, String locString, TabSheet tabSheet) {
		TextPanel textPanel = new TextPanel(request, false);
		tabSheet.addTab(textPanel, SmockerUI.getBundleValue(locString));
		return textPanel;
	}
	
	public void clean(ClickEvent event) {
		if (isSelected()) {
			Dialog.ask(SmockerUI.getBundle().getString("RemoveQuestion"), null, this::delete, null);
		}
	}
	
	public void delete() {
		selectedCommunication.getConnection().getCommunications().remove(selectedCommunication);
		daoManagerConnection.update(selectedCommunication.getConnection());

		if (CollectionUtils.isEmpty(selectedCommunication.getConnection().getCommunications())) {
			clearTabs();
		}
		fillCommunication();
	}
	
	private void clearTabs() {
		mainLayout.setSecondComponent(null);
	}

	public void cleanAll(ClickEvent event) {
		Notification.show("Clean");
	}
	
//	public void test() {
//		tabJs.runScript();
//	}
	
	public void save() {
		CommunicationMocked comm = getSelectedCommunication();
		comm.setSourceJs(tabJs.getJSSource());
		comm.setRequest(NetworkReaderUtility.encode(selectedRequestPane.getText()));
		comm.setResponse(NetworkReaderUtility.encode(selectedResponsePane.getText()));
		comm.setInputForTest(NetworkReaderUtility.encode(jsTesterPanel.getSourceInput()));
		
		daoManagerCommunicationMocked.update(comm);
	}
	
	public void refresh(ClickEvent event) {
		fillCommunication();
	}
	
	public boolean isSelected() {
		return selectedCommunication != null;
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

	public boolean isJSTabSelected() {
		return  tabSheet != null &&
				tabSheet.getSelectedTab() != null && 
				tabSheet.getSelectedTab() == tabJs;
	}


	
}
