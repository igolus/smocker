package com.jenetics.smocker.ui.view;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.vaadin.aceeditor.AceEditor;
import org.vaadin.aceeditor.AceMode;
import org.vaadin.aceeditor.AceTheme;
import org.vaadin.easyapp.util.ActionContainer;
import org.vaadin.easyapp.util.ActionContainer.InsertPosition;
import org.vaadin.easyapp.util.ActionContainerBuilder;
import org.vaadin.easyapp.util.EasyAppLayout;
import org.vaadin.easyapp.util.annotations.ContentView;

import com.jenetics.smocker.dao.DaoConfig;
import com.jenetics.smocker.dao.IDaoManager;
import com.jenetics.smocker.jseval.SmockerJsEnv;
import com.jenetics.smocker.model.config.SmockerConf;
import com.jenetics.smocker.ui.SmockerUI;
import com.jenetics.smocker.ui.component.DupHostEditor;
import com.jenetics.smocker.ui.dialog.Dialog;
import com.jenetics.smocker.ui.util.DuplicateHost;
import com.vaadin.annotations.Push;
import com.vaadin.data.TreeData;
import com.vaadin.data.HasValue.ValueChangeEvent;
import com.vaadin.data.provider.TreeDataProvider;
import com.vaadin.event.selection.SelectionEvent;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.Sizeable;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.spring.annotation.ViewScope;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Grid.SelectionMode;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.Tree;
import com.vaadin.ui.TreeGrid;
import com.vaadin.ui.VerticalLayout;

import de.steinwedel.messagebox.MessageBox;

@SuppressWarnings("serial")
@Push
@ViewScope
@ContentView(sortingOrder = 4, viewName = "ConfigView", icon = "icons/Settings-icon.png", homeView = true, rootViewParent = ConnectionsRoot.class)
public class ConfigView extends EasyAppLayout {

	private static final String SEP_IGNORED_HOST = ";";
	protected transient IDaoManager<SmockerConf> daoManagerSmockerConf = null;
	private SmockerConf singleConfig;
	protected final TabSheet tabSheet = new TabSheet();
	private AceEditor aceEditorGlobalFunctions = new AceEditor();
	private AceEditor aceEditorFilter = new AceEditor();
	private AceEditor aceEditorFormatDisplay = new AceEditor();
	private AceEditor aceEditorDefaultMockFunction = new AceEditor();
	private Button editButton;
	private DuplicateHost selectedDuplicateHost;
	private TreeData<DuplicateHost> treeDataDupHost;
	private TreeData<String> treeDataIgnoreHost;
	private TreeDataProvider<DuplicateHost> treeDataProviderDupHost;
	
	public ConfigView() {
		singleConfig = DaoConfig.getSingleConfig();
		tabSheet.setSizeFull();
		
		Component globalConfigPane = buildGlobalConfigPane();
		globalConfigPane.setCaption(SmockerUI.getBundleValue("globalConfigPane"));
		tabSheet.addTab(globalConfigPane);
		
		Component jsGlobalFunctionsPane = buildJSGlobalFunctionsPanel();
		jsGlobalFunctionsPane.setCaption(SmockerUI.getBundleValue("jsGlobalFunctionsPane"));
		tabSheet.addTab(jsGlobalFunctionsPane);
		
		Component jsFilterPane = buildJSFilterPanel();
		jsFilterPane.setCaption(SmockerUI.getBundleValue("jsFilterPane"));
		tabSheet.addTab(jsFilterPane);
		
		Component jsFormatDisplayPane = buildJSFormatDisplayPanel();
		jsFormatDisplayPane.setCaption(SmockerUI.getBundleValue("jsFormatDisplayPane"));
		tabSheet.addTab(jsFormatDisplayPane);
		
		Component jsDefaultMockFunctionPane = buildJSDefaultMockFunctionPanel();
		jsDefaultMockFunctionPane.setCaption(SmockerUI.getBundleValue("jsDefaultMockFunctionPane"));
		tabSheet.addTab(jsDefaultMockFunctionPane);
		
		addComponent(tabSheet);
		setMargin(true);
		
		setSizeFull();
	}
	
	@Override
	public ActionContainer buildActionContainer() {
		ActionContainerBuilder builder = new ActionContainerBuilder(SmockerUI.BUNDLE_NAME)
				.addButton("Save_Button", VaadinIcons.DISC, null,  this::canSave			
						, this::save, org.vaadin.easyapp.util.ActionContainer.Position.LEFT, InsertPosition.AFTER);
		return builder.build();
	}
	
	public void save(ClickEvent event) {
		singleConfig.setGlobalJsFunction(aceEditorGlobalFunctions.getValue());
		singleConfig.setFilterJsFunction(aceEditorFilter.getValue());
		singleConfig.setFormatDisplayJsFunction(aceEditorFormatDisplay.getValue());
		singleConfig.setDefaultMockFunction(aceEditorDefaultMockFunction.getValue());
		DaoConfig.saveConfig();
	}
	
	public boolean canSave() {
		return true;
	}
	
	private Component buildJSGlobalFunctionsPanel() {
		aceEditorGlobalFunctions = new AceEditor();
		aceEditorGlobalFunctions.setMode(AceMode.javascript);
		aceEditorGlobalFunctions.setTheme(AceTheme.eclipse);
		aceEditorGlobalFunctions.setSizeFull();
		if (singleConfig.getGlobalJsFunction() != null) {
			aceEditorGlobalFunctions.setValue(singleConfig.getGlobalJsFunction());
		}
		return aceEditorGlobalFunctions;
	}
	
	private Component buildJSFilterPanel() {
		aceEditorFilter = new AceEditor();
		aceEditorFilter.setMode(AceMode.javascript);
		aceEditorFilter.setTheme(AceTheme.eclipse);
		aceEditorFilter.setSizeFull();
		if (singleConfig.getFilterJsFunction() != null) {
			aceEditorFilter.setValue(singleConfig.getFilterJsFunction());
		}
		return aceEditorFilter;
	}
	
	private Component buildJSDefaultMockFunctionPanel() {
		aceEditorDefaultMockFunction.setMode(AceMode.javascript);
		aceEditorDefaultMockFunction.setTheme(AceTheme.eclipse);
		aceEditorDefaultMockFunction.setSizeFull();
		if (singleConfig.getDefaultMockFunction() != null) {
			aceEditorDefaultMockFunction.setValue(singleConfig.getDefaultMockFunction());
		}
		return aceEditorDefaultMockFunction;
	}
	
	private Component buildJSFormatDisplayPanel() {
		aceEditorFormatDisplay.setMode(AceMode.javascript);
		aceEditorFormatDisplay.setTheme(AceTheme.eclipse);
		aceEditorFormatDisplay.setSizeFull();
		if (singleConfig.getFormatDisplayJsFunction() != null) {
			aceEditorFormatDisplay.setValue(singleConfig.getFormatDisplayJsFunction());
		}
		return aceEditorFormatDisplay;
	}


	private Component buildGlobalConfigPane() {
		VerticalLayout layout = new VerticalLayout();
		layout.setSpacing(false);

		VerticalLayout fillerRight = new VerticalLayout();
		
		fillerRight.setWidth(0, Unit.PIXELS);
		
		CheckBox autoRefreshBox = createCheckBoxAutoRefresh();
		GridLayout gridAutoRefresh = createInLabelBox(SmockerUI.getBundleValue("UI_Conf"), autoRefreshBox);
		layout.addComponent(gridAutoRefresh);
		layout.setExpandRatio(gridAutoRefresh, 1.0f);
		
		Button clearGlobalVar = new Button(SmockerUI.getBundleValue("Clear_Global_Var_Button"));
		
		clearGlobalVar.addClickListener(this::clearVar);
		GridLayout gridClearGlobalVar = createInLabelBox(SmockerUI.getBundleValue("Manage_Env"), clearGlobalVar);
		layout.addComponent(gridClearGlobalVar);
		layout.setExpandRatio(gridClearGlobalVar, 1.0f);
		
		
		Component dupHost = buildDupHostGrid();
				
		GridLayout gridDupHost = createInLabelBox(SmockerUI.getBundleValue("Manage_Dup_Host"), dupHost);
		layout.addComponent(gridDupHost);
		layout.setExpandRatio(gridDupHost, 1.0f);
		
		
		Component ignoreHost = buildIgnoreHostListGrid();
		
		GridLayout gridIgnoreHost = createInLabelBox(SmockerUI.getBundleValue("Manage_Ignored_Host"), ignoreHost);
		layout.addComponent(gridIgnoreHost);
		layout.setExpandRatio(gridIgnoreHost, 1.0f);
		
		layout.addComponent(fillerRight);
		layout.setWidth("100%");
		return layout;
	}
	
	private List<DuplicateHost> duplicateHosts = 
			DuplicateHost.getListFromDbValue(DaoConfig.getSingleConfig().getDuplicateHosts());
	private List<String> ignoredHosts;
	
	private TreeDataProvider<String> treeDataProviderIgnoreHost;
	private String selectedIgnoredHost;
	private Button removeIgnoreHostButton;

	public List<DuplicateHost> getDuplicateHosts() {
		return duplicateHosts;
	}

	public List<String> getIgnoredHosts() {
		return ignoredHosts;
	}

	private Component buildDupHostGrid() {
		
		GridLayout gridInteration  = new GridLayout(1, 2);
		gridInteration.setWidth("100%");
		
		HorizontalLayout layoutButtons = new HorizontalLayout();
		editButton = new Button(VaadinIcons.PENCIL);
		editButton.setDescription(SmockerUI.getBundleValue("Edit_Dup_Host_toolTip"));
		editButton.setEnabled(false);
		editButton.addClickListener(this::editDupHost);
		layoutButtons.addComponent(editButton);
		
		Button addButton = new Button(VaadinIcons.PLUS);
		
		addButton.setDescription(SmockerUI.getBundleValue("Add_Dup_Host_toolTip"));
		addButton.addClickListener(this::addDupHost);
		layoutButtons.addComponent(addButton);
		
		
		gridInteration.addComponent(layoutButtons, 0, 0);
		
		TreeGrid<DuplicateHost> dupHostGrid = new TreeGrid<>();
		
		dupHostGrid.setSelectionMode(SelectionMode.SINGLE);
		dupHostGrid.addColumn(DuplicateHost::toString).setCaption("Dup host");
		treeDataDupHost = new TreeData<>();
		treeDataProviderDupHost = new TreeDataProvider<>(treeDataDupHost);
		dupHostGrid.setDataProvider(treeDataProviderDupHost);
		dupHostGrid.setWidth("100%");
		
		refreshDupHost();
		gridInteration.addComponent(dupHostGrid, 0, 1);
		
		
		dupHostGrid.addSelectionListener(this::gridDupHostSelected);
		
		return gridInteration;
	}
	
	


	private Component buildIgnoreHostListGrid() {
		GridLayout gridInteration  = new GridLayout(1, 2);
		gridInteration.setWidth("100%");
		
		HorizontalLayout layoutButtons = new HorizontalLayout();
		
		
		removeIgnoreHostButton = new Button(VaadinIcons.MINUS);
		removeIgnoreHostButton.setDescription(SmockerUI.getBundleValue("Remove_Ignore_Host_toolTip"));
		removeIgnoreHostButton.setEnabled(false);
		removeIgnoreHostButton.addClickListener(this::removeIgnoredHost);
		layoutButtons.addComponent(removeIgnoreHostButton);
		
		Button addButton = new Button(VaadinIcons.PLUS);
		
		addButton.setDescription(SmockerUI.getBundleValue("Add_Ignore_Host_toolTip"));
		addButton.addClickListener(this::addIgnoreHost);
		layoutButtons.addComponent(addButton);
		
		
		gridInteration.addComponent(layoutButtons, 0, 0);
		
		TreeGrid<String> ignoreHostGrid = new TreeGrid<>();
		
		ignoreHostGrid.setSelectionMode(SelectionMode.SINGLE);
		ignoreHostGrid.addColumn(String::toString).setCaption(SmockerUI.getBundleValue("Ignore_Host_Column"));
		treeDataIgnoreHost = new TreeData<>();
		treeDataProviderIgnoreHost = new TreeDataProvider<>(treeDataIgnoreHost);
		ignoreHostGrid.setDataProvider(treeDataProviderIgnoreHost);
		ignoreHostGrid.setWidth("100%");
		
		refreshDupHost();
		gridInteration.addComponent(ignoreHostGrid, 0, 1);
		
		
		ignoreHostGrid.addSelectionListener(this::gridIgnoreHostSelected);
		getIgnoredListFromDb(DaoConfig.getSingleConfig().getIgnoredHosts());
		return gridInteration;
	}
	
	
	private void refreshDupHost() {
		treeDataDupHost.clear();
		for (DuplicateHost dupHost : duplicateHosts) {
			treeDataDupHost.addItem(null, dupHost);
		}
		treeDataProviderDupHost.refreshAll();
	}
	
	private void refreshIgnoredHost() {
		treeDataIgnoreHost.clear();
		for (String host : ignoredHosts) {
			treeDataIgnoreHost.addItem(null, host);
		}
		treeDataProviderIgnoreHost.refreshAll();
	}

	private void editDupHost(ClickEvent event) {
		DupHostEditor dupHostEditor = new DupHostEditor(selectedDuplicateHost);
		Dialog.displayComponentBox(SmockerUI.getBundleValue("Dup_Host"), this::dupHostUpdated, dupHostEditor);
	}
	
	private void dupHostUpdated(DuplicateHost dupHost) {
		updateDupHostInDB();
	}
	
	private void addDupHost(ClickEvent event) {
		DuplicateHost dupHost = new DuplicateHost();
		DupHostEditor dupHostEditor = new DupHostEditor(dupHost);
		MessageBox displayComponentBox = 
				Dialog.displayComponentBox(SmockerUI.getBundleValue("Dup_Host"), this::dupHostCreated, dupHostEditor);
		dupHostEditor.setBox(displayComponentBox);
	}
	
	private void addIgnoreHost(ClickEvent event) {
		Dialog.displayCreateStringBox(SmockerUI.getBundleValue("Dup_Host"), this::ignoreHostCreated);
	}
	
	
	private void removeIgnoredHost(ClickEvent event) {
		ignoredHosts.remove(selectedIgnoredHost);
		updateIgnoredHostInDB();
	}
	
	
	private void dupHostCreated(DuplicateHost dupHost) {
		duplicateHosts.add(dupHost);
		updateDupHostInDB();
	}
	
	private void ignoreHostCreated(String host) {
		ignoredHosts.add(host);
		updateIgnoredHostInDB();
	}

	private void updateDupHostInDB() {
		DaoConfig.getSingleConfig().setDuplicateHosts(DuplicateHost.getDbValueFromList(duplicateHosts));
		DaoConfig.saveConfig();
		refreshDupHost();
	}
	
	private void updateIgnoredHostInDB() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < ignoredHosts.size(); i++) {
			sb.append(ignoredHosts.get(i));
			if (i < ignoredHosts.size() - 1) {
				 sb.append(SEP_IGNORED_HOST);
			}
		}
		DaoConfig.getSingleConfig().setIgnoredHosts(sb.toString());
		DaoConfig.saveConfig();
		refreshIgnoredHost();
	}
	
	private void getIgnoredListFromDb(String ignoredHostString) {
		ignoredHosts = new ArrayList<>(Arrays.asList(ignoredHostString.split(SEP_IGNORED_HOST)));
		refreshIgnoredHost();
	}
	
	private void gridDupHostSelected(SelectionEvent<DuplicateHost> dupHost) {
		selectedDuplicateHost = dupHost.getFirstSelectedItem().get();
		editButton.setEnabled(true);
	}
	
	private void gridIgnoreHostSelected(SelectionEvent<String> dupHost) {
		selectedIgnoredHost = dupHost.getFirstSelectedItem().get();
		removeIgnoreHostButton.setEnabled(true);
	}
	
	private GridLayout createInLabelBox (String textDesc, Component component) {
		Label labelInteration = new Label("<b>" + textDesc + "</b>", ContentMode.HTML);
		GridLayout gridInteration  = new GridLayout(1, 2);
		gridInteration.setWidth("100%");
		gridInteration.setStyleName("Config");
		gridInteration.addComponent(labelInteration, 0, 0);
		
		gridInteration.addComponent(component, 0, 1);
		return gridInteration;
		
	}
	
	private void clearVar(ClickEvent event) {
		SmockerJsEnv.getInstance().clear();
	}

	private CheckBox createCheckBoxAutoRefresh() {
		CheckBox autoRefreshBox = new CheckBox(SmockerUI.getBundleValue("autoRefreshCheckBox"));
		autoRefreshBox.setValue(singleConfig.isAutorefesh());
		autoRefreshBox.addValueChangeListener(this::autoRefreshChange);
		return autoRefreshBox;
	}
	
	private void autoRefreshChange (ValueChangeEvent<Boolean> event) {
		singleConfig.setAutorefesh(event.getValue());
		DaoConfig.saveConfig();
	}
}
