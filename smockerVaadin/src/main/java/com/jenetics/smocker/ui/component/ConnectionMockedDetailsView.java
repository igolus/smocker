package com.jenetics.smocker.ui.component;

import java.util.Set;
import java.util.logging.Level;

import org.apache.commons.collections4.CollectionUtils;
import org.vaadin.aceeditor.AceEditor;
import org.vaadin.aceeditor.AceMode;
import org.vaadin.aceeditor.AceTheme;
import org.vaadin.easyapp.util.ActionContainer;
import org.vaadin.easyapp.util.ActionContainer.InsertPosition;
import org.vaadin.easyapp.util.ActionContainerBuilder;
import org.vaadin.easyapp.util.EasyAppLayout;

import com.jenetics.smocker.dao.DaoManager;
import com.jenetics.smocker.dao.IDaoManager;
import com.jenetics.smocker.injector.BundleUI;
import com.jenetics.smocker.model.CommunicationMocked;
import com.jenetics.smocker.model.ConnectionMocked;
import com.jenetics.smocker.ui.SmockerUI;
import com.jenetics.smocker.ui.component.javascript.JsEditor;
import com.jenetics.smocker.ui.dialog.Dialog;
import com.jenetics.smocker.ui.util.CommunicationMockedDateDisplay;
import com.jenetics.smocker.util.NetworkReaderUtility;
import com.jenetics.smocker.util.SmockerUtility;
import com.vaadin.data.TreeData;
import com.vaadin.data.provider.TreeDataProvider;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.Sizeable;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Grid.SelectionMode;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.Tree;
import com.vaadin.ui.Tree.ItemClick;
import com.vaadin.ui.VerticalSplitPanel;
import com.vaadin.ui.Window;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;


@SuppressWarnings("serial")
public class ConnectionMockedDetailsView extends EasyAppLayout {
	
	private ConnectionMocked connectionMocked;
	private Tree<CommunicationMockedDateDisplay> menu;
	private TreeData<CommunicationMockedDateDisplay> treeData;
	private TreeDataProvider<CommunicationMockedDateDisplay> treeDataProvider;
	private CommunicationMocked selectedCommunication = null;
	private HorizontalSplitPanel mainLayout = null;
	//private AceEditor aceEditor = null;
	private JsEditor jsEditor = null;
	
	protected IDaoManager<ConnectionMocked> daoManagerConnection = new DaoManager<ConnectionMocked>(ConnectionMocked.class, SmockerUI.getEm());
	
	private Window subWindow;
	
	public Window getSubWindow() {
		return subWindow;
	}

	public void setSubWindow(Window subWindow) {
		this.subWindow = subWindow;
	}
	
	public void close() {
		if (subWindow != null) {
			subWindow.close();
		}
	}

	public ConnectionMockedDetailsView(ConnectionMocked connectionMocked) {
		super();
		mainLayout = new HorizontalSplitPanel();
		
		this.connectionMocked = connectionMocked;
		
		jsEditor = new JsEditor();
		jsEditor.setSizeFull();
		
		menu = new Tree<>();
		menu.setSelectionMode(SelectionMode.SINGLE);
		menu.setSizeFull();
		
		treeData = new TreeData<>();
		treeDataProvider = new TreeDataProvider<>(treeData);
		menu.setDataProvider(treeDataProvider);
		
		fillCommunication();
		menu.addItemClickListener(this::treeItemClick);

		mainLayout.setFirstComponent(menu);
		
		mainLayout.setSplitPosition(23);
		mainLayout.setSizeFull();
		
		addComponent(mainLayout);
		setSizeFull();
		
	}

	private void fillCommunication() {
		treeData.clear();
		Set<CommunicationMocked> communications = connectionMocked.getCommunications();
		for (CommunicationMocked communication : communications) {
			treeData.addItem(null, new CommunicationMockedDateDisplay(communication));
		}
		treeDataProvider.refreshAll();
	}
	
	private LoggerPanel loggerPanel = null;
	
	public void treeItemClick(ItemClick<CommunicationMockedDateDisplay> event) {
		CommunicationMocked comm = event.getItem().getCommunication();
		
		String request = NetworkReaderUtility.decode(comm.getRequest());
		String response = NetworkReaderUtility.decode(comm.getResponse());
		
//		aceEditor.setMode(AceMode.groovy);
//		aceEditor.setTheme(AceTheme.eclipse);
//		aceEditor.setSizeFull();
		
		TabSheet tabSheet = new TabSheet();
		tabSheet.setSizeFull();
		
		addTextAreaToTabSheet(request, "Input", tabSheet);
		addTextAreaToTabSheet(response, "Output", tabSheet);	
		
//		loggerPanel = new LoggerPanel();
		
//		VerticalSplitPanel vsplit = new VerticalSplitPanel();
//		vsplit.setSplitPosition(75, Unit.PERCENTAGE);
//		vsplit.setFirstComponent(loggerPanel);
//		vsplit.setSecondComponent(aceEditor);
		
		tabSheet.addTab(jsEditor, SmockerUI.getBundleValue("GroovyEditor"));
		
		mainLayout.setSecondComponent(tabSheet);
		
		selectedCommunication = comm;
		refreshClickable();
	}

	private void addTextAreaToTabSheet(String request, String locString, TabSheet tabSheet) {
		TextArea areaOutput = new TextArea();
		areaOutput.setWordWrap(false);
		areaOutput.setReadOnly(true);
		areaOutput.setSizeFull();
		areaOutput.setValue(request);
		tabSheet.addTab(areaOutput, SmockerUI.getBundleValue(locString));
	}
	
//	public ActionContainer buildActionContainer() {
//		ActionContainerBuilder builder = new ActionContainerBuilder(SmockerUI.BUNDLE_NAME)
//				.addButton("Clean_Button", VaadinIcons.MINUS, "Clean_ToolTip",  this::isSelected			
//						, this::clean, org.vaadin.easyapp.util.ActionContainer.Position.LEFT, InsertPosition.AFTER)
//				.addButton("CleanAll_Button", VaadinIcons.MINUS, "CleanAll_ToolTip",  this::atLeastOneItem			
//						, this::cleanAll, org.vaadin.easyapp.util.ActionContainer.Position.LEFT, InsertPosition.AFTER)
//				.addButton("Refresh_Button", VaadinIcons.REFRESH, "Refresh_ToolTip",  this::always			
//						, this::refresh, org.vaadin.easyapp.util.ActionContainer.Position.LEFT, InsertPosition.AFTER)
//				.addButton("Play Button", VaadinIcons.PLAY, "Play_ToolTip",  this::isSelected		
//						, this::play, org.vaadin.easyapp.util.ActionContainer.Position.LEFT, InsertPosition.AFTER);
//
//		return builder.build();
//	}
	
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
	
	public void play() {
		jsEditor.runScript();
//		if (evaluateAndShowGroovyErrors()) {
//			selectedCommunication.setSourceGroovy(aceEditor.getValue());
//			Binding binding = new Binding();
//			GroovyShell shell = new GroovyShell(binding);
//			shell.evaluate(aceEditor.getValue());
//		}
	}

//	private boolean evaluateAndShowGroovyErrors() {
//		try {
//		    new GroovyShell().evaluate(aceEditor.getValue());
//		} catch(Exception e) {
//		    //SmockerUI.displayMessageInSubWindow(SmockerUI.getBundleValue("GroovyErrors"), SmockerUtility.getStackTrace(e));
//			loggerPanel.appendMessage(Level.SEVERE, SmockerUtility.getStackTrace(e));
//		    //SmockerUI.log(Level.SEVERE, SmockerUtility.getStackTrace(e));
//		    return false;
//		}
//		return true;
//	}
	
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
	
}
