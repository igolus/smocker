package com.jenetics.smocker.ui.component;

import java.util.Set;

import javax.inject.Inject;

import org.vaadin.aceeditor.AceEditor;
import org.vaadin.aceeditor.AceMode;
import org.vaadin.aceeditor.AceTheme;
import org.vaadin.easyapp.util.ActionContainer;
import org.vaadin.easyapp.util.ActionContainerBuilder;
import org.vaadin.easyapp.util.EasyAppLayout;

import com.jenetics.smocker.dao.DaoManager;
import com.jenetics.smocker.dao.IDaoManager;
import com.jenetics.smocker.injector.BundleUI;
import com.jenetics.smocker.injector.Dao;
import com.jenetics.smocker.model.Communication;
import com.jenetics.smocker.model.CommunicationMocked;
import com.jenetics.smocker.model.Connection;
import com.jenetics.smocker.model.ConnectionMocked;
import com.jenetics.smocker.model.converter.MockConverter;
import com.jenetics.smocker.ui.SmockerUI;
import com.jenetics.smocker.ui.dialog.Dialog;
import com.jenetics.smocker.ui.netdisplayer.ComponentWithDisplayChange;
import com.jenetics.smocker.ui.netdisplayer.NetDisplayerFactoryInput;
import com.jenetics.smocker.ui.netdisplayer.NetDisplayerFactoryOutput;
import com.jenetics.smocker.ui.util.CommunicationDateDisplay;
import com.jenetics.smocker.ui.util.CommunicationMockedDateDisplay;
import com.jenetics.smocker.ui.view.JavaApplicationView2;
import com.jenetics.smocker.ui.view.MockSpaceView;
import com.jenetics.smocker.util.NetworkReaderUtility;
import com.vaadin.data.TreeData;
import com.vaadin.data.provider.TreeDataProvider;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.Grid.SelectionMode;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.Tree;
import com.vaadin.ui.Tree.ItemClick;
import com.vaadin.ui.Window;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;


@SuppressWarnings("serial")
public class ConnectionMockedDetailsView extends EasyAppLayout {
	
	private ConnectionMocked connectionMocked;
	private TabSheet tabSheet = null;
	private Tree<CommunicationMockedDateDisplay> menu;
	private TreeData<CommunicationMockedDateDisplay> treeData;
	private TreeDataProvider<CommunicationMockedDateDisplay> treeDataProvider;
	private CommunicationMocked selectedCommunication = null;
	private  AceEditor aceEditor = null;
	
	protected IDaoManager<ConnectionMocked> daoManagerConnection = new DaoManager<ConnectionMocked>(ConnectionMocked.class, SmockerUI.getEm());
	private Window subWindow;
	
	public ConnectionMockedDetailsView(ConnectionMocked connectionMocked, Window subWindow) {
		super();
		HorizontalSplitPanel mainLayout = new HorizontalSplitPanel();
		
		this.subWindow = subWindow;
		this.connectionMocked = connectionMocked;
		
		aceEditor = new AceEditor();
		
		menu = new Tree<>();
		menu.setSelectionMode(SelectionMode.SINGLE);
		treeData = new TreeData<>();
		treeDataProvider = new TreeDataProvider<>(treeData);
		menu.setDataProvider(treeDataProvider);
		
		fillCommunication();
		menu.addItemClickListener(this::treeItemClick);
		
		tabSheet = new TabSheet();
		tabSheet.setSizeFull();
		mainLayout.setFirstComponent(menu);
		mainLayout.setSecondComponent(tabSheet);
		mainLayout.setSplitPosition(20);
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
	
	public void treeItemClick(ItemClick<CommunicationMockedDateDisplay> event) {
		CommunicationMocked comm = event.getItem().getCommunication();
		
		String request = NetworkReaderUtility.decode(comm.getRequest());
		String response = NetworkReaderUtility.decode(comm.getResponse());
		
		addTextAreaToTabSheet(request, "Input");
		addTextAreaToTabSheet(response, "Output");		
		
		aceEditor.setMode(AceMode.groovy);
		aceEditor.setTheme(AceTheme.eclipse);
		aceEditor.setSizeFull();
		tabSheet.addTab(aceEditor, "GroovyEditor");
	
		selectedCommunication = comm;
		refreshClickable();
	}

	private void addTextAreaToTabSheet(String request, String locString) {
		TextArea areaOutput = new TextArea();
		areaOutput.setWordWrap(false);
		areaOutput.setReadOnly(true);
		areaOutput.setSizeFull();
		areaOutput.setValue(request);
		tabSheet.addTab(areaOutput, SmockerUI.getBundleValue(locString));
	}
	
	public ActionContainer buildActionContainer() {
		ActionContainerBuilder builder = new ActionContainerBuilder(SmockerUI.BUNDLE_NAME)
				.addButton("Clean_Button", VaadinIcons.MINUS, "Clean_ToolTip",  this::isSelected			
						, this::clean)
				.addButton("CleanAll_Button", VaadinIcons.MINUS, "CleanAll_ToolTip",  this::atLeastOneItem			
						, this::cleanAll)
				.addButton("Refresh_Button", VaadinIcons.REFRESH, "Refresh_ToolTip",  this::always			
						, this::refresh)
				.addButton("Eval groovy", VaadinIcons.PLAY, "CleanAll_ToolTip",  this::always			
						, this::runGrrovy)
				
				.setSearch(this::search);

		return builder.build();
	}
	
	public void clean(ClickEvent event) {
		if (isSelected()) {
			Dialog.ask(SmockerUI.getBundle().getString("RemoveQuestion"), null, this::delete, null);
		}
	}
	
	public void delete() {
		selectedCommunication.getConnection().getCommunications().remove(selectedCommunication);
		daoManagerConnection.update(selectedCommunication.getConnection());
		fillCommunication();
	}
	
	public void cleanAll(ClickEvent event) {
		Notification.show("Clean");
	}
	
	public void runGrrovy(ClickEvent event) {
		Binding binding = new Binding();
		GroovyShell shell = new GroovyShell(binding);
		shell.evaluate(aceEditor.getValue());
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
	
}
