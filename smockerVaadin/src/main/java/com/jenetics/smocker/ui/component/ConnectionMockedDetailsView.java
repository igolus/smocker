package com.jenetics.smocker.ui.component;

import org.vaadin.easyapp.ui.ViewWithToolBar;

import com.jenetics.smocker.dao.DaoManagerByModel;
import com.jenetics.smocker.dao.IDaoManager;
import com.jenetics.smocker.model.CommunicationMocked;
import com.jenetics.smocker.model.ConnectionMocked;
import com.jenetics.smocker.ui.SmockerUI;
import com.jenetics.smocker.ui.component.javascript.JsEditor;
import com.jenetics.smocker.ui.component.javascript.JsTesterPanel;
import com.jenetics.smocker.ui.util.ButtonWithIEntity;
import com.jenetics.smocker.ui.util.TreeGridMockedItem;
import com.jenetics.smocker.util.NetworkReaderUtility;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.SelectedTabChangeEvent;
import com.vaadin.ui.TreeGrid;


@SuppressWarnings("serial")
public class ConnectionMockedDetailsView extends AbstractConnectionDetails {
	
	//private TreeGrid<TreeGridMockedItem> treeGrid;
	private CommunicationMocked selectedCommunication = null;
	private HorizontalSplitPanel mainLayout = null;
	
	protected transient IDaoManager<ConnectionMocked> daoManagerConnection =  DaoManagerByModel.getDaoManager(ConnectionMocked.class);
	protected transient IDaoManager<CommunicationMocked> daoManagerCommunicationMocked = DaoManagerByModel.getDaoManager(CommunicationMocked.class);

	private TabSheet tabSheet;
	private JsEditor tabJs;
	private TextPanel selectedRequestPane;
	private TextPanel selectedResponsePane;
	private JsTesterPanel jsTesterPanel;
	private ConnectionMockedManager connectionMockedManager;

	public ConnectionMockedDetailsView(ConnectionMocked connectionMocked,  Runnable refreshClickable ) {
		super();
		this.refreshClickable = refreshClickable;
		mainLayout = new HorizontalSplitPanel();
	
		connectionMockedManager = new ConnectionMockedManager(
				connectionMocked, this::commSelected, this.refreshClickable);
		
		ViewWithToolBar view = new ViewWithToolBar(connectionMockedManager);
		
		mainLayout.setFirstComponent(view);
		mainLayout.setSplitPosition(23);
		mainLayout.setSizeFull();
		
		addComponent(mainLayout);

		setSizeFull();
	}
	
	public ConnectionMockedManager getConnectionMockedManager() {
		return connectionMockedManager;
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
	
//	protected void addTreeMapping() {
//		treeGrid.addColumn(item -> item.getDisplay());
//		treeGrid.addComponentColumn(this::buildEnableButton);
//	}
	
	
//	private Button buildEnableButton(TreeGridMockedItem item) {
//		if (!item.isRoot()) {
//			ButtonWithIEntity<CommunicationMocked> buttonWithId = new ButtonWithIEntity<>(item.getCommunication());
//			buttonWithId.setCaption("Enable");
//			buttonWithId.addClickListener(this::enableButtonClicked);
//			return buttonWithId;
//		}
//		return null;
//    }
	
//	public void enableButtonClicked(ClickEvent event) {
//		ButtonWithIEntity<CommunicationMocked> buttonWithEntity = 
//				(ButtonWithIEntity<CommunicationMocked>) event.getSource();
//	}

	public void commSelected(CommunicationMocked comm) {
		if (comm != null) {
			String request = NetworkReaderUtility.decode(comm.getRequest());
			String response = NetworkReaderUtility.decode(comm.getResponse());
			
			tabSheet = new TabSheet();
			tabSheet.setSizeFull();
			
			selectedRequestPane =  addTextAreaToTabSheet(request, "Input", tabSheet);
			selectedResponsePane = addTextAreaToTabSheet(response, "Output", tabSheet);	
			
			tabJs = new JsEditor(comm, selectedRequestPane, selectedResponsePane);
			tabJs.setSizeFull();
			tabSheet.addTab(tabJs, SmockerUI.getBundleValue("NodeEditor"));
			
			jsTesterPanel = new JsTesterPanel(request, tabJs, comm);
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
		else {
			selectedCommunication = null;
			mainLayout.setSecondComponent(null);
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
	
	
	private void clearTabs() {
		mainLayout.setSecondComponent(null);
	}

	public void cleanAll(ClickEvent event) {
		Notification.show("Clean");
	}
	

	public void save() {
		CommunicationMocked comm = getSelectedCommunication();
		comm.setSourceJs(tabJs.getJSSource());
		comm.setRequest(NetworkReaderUtility.encode(selectedRequestPane.getText()));
		comm.setResponse(NetworkReaderUtility.encode(selectedResponsePane.getText()));
		comm.setInputForTest(NetworkReaderUtility.encode(jsTesterPanel.getSourceInput()));
		
		daoManagerCommunicationMocked.update(comm);
	}
	
	public void refresh() {
		connectionMockedManager.fillCommunication();
	}
	
	public boolean isSelected() {
		return selectedCommunication != null;
	}
	
	public boolean always() {
		return true;
	}
	
	public void search(String searchValue) {
		Notification.show("Search for:" + searchValue);
	}

	public boolean isJSTabSelected() {
		return  tabSheet != null &&
				tabSheet.getSelectedTab() != null && 
				tabSheet.getSelectedTab() == tabJs;
	}

	public void communicationMockedCreated(CommunicationMocked communicationMocked) {
		connectionMockedManager.communicationMockedCreated(communicationMocked);
	}
	
}
