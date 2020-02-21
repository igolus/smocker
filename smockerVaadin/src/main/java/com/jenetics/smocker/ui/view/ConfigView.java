package com.jenetics.smocker.ui.view;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.jboss.logging.Logger;
import org.vaadin.aceeditor.AceEditor;
import org.vaadin.aceeditor.AceMode;
import org.vaadin.aceeditor.AceTheme;
import org.vaadin.easyapp.util.ActionContainer;
import org.vaadin.easyapp.util.ActionContainer.InsertPosition;
import org.vaadin.easyapp.util.ActionContainer.Position;
import org.vaadin.easyapp.util.ActionContainerBuilder;
import org.vaadin.easyapp.util.ButtonWithCheck;
import org.vaadin.easyapp.util.EasyAppLayout;
import org.vaadin.easyapp.util.annotations.ContentView;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.jenetics.smocker.dao.DaoConfig;
import com.jenetics.smocker.dao.IDaoManager;
import com.jenetics.smocker.jseval.SmockerJsEnv;
import com.jenetics.smocker.model.config.SmockerConf;
import com.jenetics.smocker.ui.SmockerUI;
import com.jenetics.smocker.ui.component.DupHostEditor;
import com.jenetics.smocker.ui.component.HostAndPortEditor;
import com.jenetics.smocker.ui.dialog.Dialog;
import com.jenetics.smocker.ui.util.ConfigUploader;
import com.jenetics.smocker.ui.util.DuplicateHost;
import com.jenetics.smocker.ui.util.HostAndPortRange;
import com.jenetics.smocker.ui.util.JsonFileDownloader;
import com.jenetics.smocker.ui.util.JsonFileDownloader.OnDemandStreamResource;
import com.vaadin.annotations.Push;
import com.vaadin.data.HasValue.ValueChangeEvent;
import com.vaadin.data.TreeData;
import com.vaadin.data.provider.TreeDataProvider;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.event.ShortcutAction.ModifierKey;
import com.vaadin.event.selection.SelectionEvent;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.spring.annotation.ViewScope;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Grid.SelectionMode;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.Tab;
import com.vaadin.ui.TreeGrid;
import com.vaadin.ui.Upload;
import com.vaadin.ui.VerticalLayout;

import de.steinwedel.messagebox.MessageBox;

@SuppressWarnings("serial")
@Push
@ViewScope
@ContentView(sortingOrder = 4, viewName = "ConfigView", icon = "icons/Settings-icon.png", homeView = true, rootViewParent = ConnectionsRoot.class)
public class ConfigView extends EasyAppLayout {

	@Inject
	private static Logger logger;

	private static final String SEP_IGNORED_HOST = ";";
	protected transient IDaoManager<SmockerConf> daoManagerSmockerConf = null;
	private SmockerConf singleConfig;
	protected final TabSheet tabSheet = new TabSheet();
	private AceEditor aceEditorGlobalFunctions = new AceEditor();
	private AceEditor aceEditorFilter = new AceEditor();
	private AceEditor aceEditorFormatDisplay = new AceEditor();
	private AceEditor aceEditorTraceFunction = new AceEditor();
	private AceEditor aceEditorDefaultMockFunction = new AceEditor();

	private Button removeDupHostHostButton;
	private transient DuplicateHost selectedDuplicateHost;
	private TreeData<DuplicateHost> treeDataDupHost;
	private TreeDataProvider<DuplicateHost> treeDataProviderDupHost;
	private transient List<DuplicateHost> duplicateHosts = 
			DuplicateHost.getListFromDbValue(DaoConfig.getSingleConfig().getDuplicateHosts());

	private transient List<HostAndPortRange> excludedHosts = new ArrayList<>();
	private TreeDataProvider<HostAndPortRange> treeDataProviderExcludedHost;
	private transient HostAndPortRange selectedExcludedHost;
	private Button removeExcludedHostButton;
	private TreeData<HostAndPortRange> treeDataExcludedHost;


	private transient List<HostAndPortRange> includedHosts = new ArrayList<>();
	private TreeDataProvider<HostAndPortRange> treeDataProviderIncludedHost;
	private transient HostAndPortRange selectedIncludedHost;
	private Button removeIncludedHostButton;
	private TreeData<HostAndPortRange> treeDataIncludedHost;


	private boolean unsaved=false;


	public ConfigView() {
		singleConfig = DaoConfig.getSingleConfig();
		tabSheet.setSizeFull();

		Component globalConfigPane = buildGlobalConfigPane();
		globalConfigPane.setCaption(SmockerUI.getBundleValue("globalConfigPane"));
		tabSheet.addTab(globalConfigPane);

		Component traceFunctionPane = buildTraceFunctionPane();
		traceFunctionPane.setCaption(SmockerUI.getBundleValue("traceFunctionPane"));
		tabSheet.addTab(traceFunctionPane);

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
	}

	private void changedEditor(Component source) {
		Tab tabEditor = tabSheet.getTab(source);
		if (tabEditor != null && tabEditor.getCaption().indexOf(" *") == -1) {
			tabEditor.setCaption(tabEditor.getCaption() + " *");
		}
	}


	private void removeUnsavedState() {
		removedUnsavedStateInTab(aceEditorGlobalFunctions);
		removedUnsavedStateInTab(aceEditorFilter);
		removedUnsavedStateInTab(aceEditorFormatDisplay);
		removedUnsavedStateInTab(aceEditorTraceFunction);
		removedUnsavedStateInTab(aceEditorDefaultMockFunction);
	} 
	
	private void removedUnsavedStateInTab(Component component) {
		Tab tabEditor = tabSheet.getTab(component);
		tabEditor.setCaption(tabEditor.getCaption().substring(0, tabEditor.getCaption().length() - 1));
	} 
	
	@Override
	public ActionContainer buildActionContainer() {
		ActionContainerBuilder builder = new ActionContainerBuilder(SmockerUI.BUNDLE_NAME)
				.addButtonWithShotCut("Save_Button", VaadinIcons.DISC, "Save_Button_ToolTip",  this::canSave			
						, this::save, org.vaadin.easyapp.util.ActionContainer.Position.LEFT, InsertPosition.AFTER, KeyCode.S, 
						ModifierKey.CTRL)
				.addButton("Export_Button", VaadinIcons.SHARE, "ExportConfigToolTip",  () -> true			
						, null, org.vaadin.easyapp.util.ActionContainer.Position.LEFT, InsertPosition.AFTER);

		ConfigUploader uploader = new ConfigUploader();
		Upload upload = new Upload(null, uploader);
		upload.setButtonCaption(SmockerUI.getBundleValue("Import_Button"));
		upload.setDescription(SmockerUI.getBundleValue("ImportConfigToolTip"));
		upload.addSucceededListener(uploader);
		builder.addComponent(upload, Position.LEFT, InsertPosition.AFTER);


		builder.addButton("Refresh_Button", VaadinIcons.REFRESH, null,  () -> true
				, this::refresh, org.vaadin.easyapp.util.ActionContainer.Position.LEFT, InsertPosition.AFTER);

		ActionContainer actionContainer = builder.build(); 

		List<ButtonWithCheck> listButtonWithCheck = actionContainer.getListButtonWithCheck();
		ButtonWithCheck exportButton = listButtonWithCheck.get(listButtonWithCheck.size() - 2);

		JsonFileDownloader downloader = new JsonFileDownloader(createResource());

		downloader.extend(exportButton);
		return actionContainer;

	}

	private OnDemandStreamResource createResource() {

		return new OnDemandStreamResource() {
			@Override
			public InputStream getStream() {
				try {
					SmockerConf conf = DaoConfig.getSingleConfig();
					if (conf != null) {
						ObjectMapper mapper = new ObjectMapper();
						mapper.enable(SerializationFeature.INDENT_OUTPUT);
						mapper.configure(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE, false);
						mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
						String jsonObjSTring = 
								mapper.writerWithDefaultPrettyPrinter().writeValueAsString(conf);
						return new ByteArrayInputStream(jsonObjSTring.getBytes("UTF-8"));
					}
				} catch (IOException e) {
					logger.error("Unable to export scenario", e);
				}
				return null;
			}

			@Override
			public String getFilename() {
				return "mySmockerConfig.json";
			}

		};
	}


	public void save(ClickEvent event) {
		singleConfig.setGlobalJsFunction(aceEditorGlobalFunctions.getValue());
		singleConfig.setFilterJsFunction(aceEditorFilter.getValue());
		singleConfig.setFormatDisplayJsFunction(aceEditorFormatDisplay.getValue());
		singleConfig.setDefaultMockFunction(aceEditorDefaultMockFunction.getValue());
		singleConfig.setTraceFunctionJsFunction(aceEditorTraceFunction.getValue());
		DaoConfig.saveConfig();
		removeUnsavedState();
	}

	public void refresh(ClickEvent event) {
		excludedHosts = getListFromDb(DaoConfig.getSingleConfig().getExcludedHosts());
		includedHosts = getListFromDb(DaoConfig.getSingleConfig().getIncludedHosts());
		duplicateHosts = DuplicateHost.getListFromDbValue(DaoConfig.getSingleConfig().getDuplicateHosts());


		refreshDupHost();
		refreshHost(excludedHosts, treeDataProviderExcludedHost, treeDataExcludedHost);
		refreshHost(includedHosts, treeDataProviderIncludedHost, treeDataIncludedHost);

		if (singleConfig.getGlobalJsFunction() != null) {
			aceEditorGlobalFunctions.setValue(singleConfig.getGlobalJsFunction());
		}
		if (singleConfig.getFilterJsFunction() != null) {
			aceEditorFilter.setValue(singleConfig.getFilterJsFunction());
		}
		if (singleConfig.getFormatDisplayJsFunction() != null) {
			aceEditorFormatDisplay.setValue(singleConfig.getFormatDisplayJsFunction());
		}
		if (singleConfig.getTraceFunctionJsFunction() != null) {
			aceEditorTraceFunction.setValue(singleConfig.getTraceFunctionJsFunction());
		}
		if (singleConfig.getDefaultMockFunction() != null) {
			aceEditorDefaultMockFunction.setValue(singleConfig.getDefaultMockFunction());
		}
	}

	public boolean canSave() {
		return true;
	}

	private Component buildJSGlobalFunctionsPanel() {
		customizeAceEditor(aceEditorGlobalFunctions);
		aceEditorGlobalFunctions.setSizeFull();
		if (singleConfig.getGlobalJsFunction() != null) {
			aceEditorGlobalFunctions.setValue(singleConfig.getGlobalJsFunction());
		}
		return aceEditorGlobalFunctions;
	}

	private Component buildJSFilterPanel() {
		customizeAceEditor(aceEditorFilter);
		if (singleConfig.getFilterJsFunction() != null) {
			aceEditorFilter.setValue(singleConfig.getFilterJsFunction());
		}
		return aceEditorFilter;
	}

	private Component buildJSDefaultMockFunctionPanel() {
		customizeAceEditor(aceEditorDefaultMockFunction);
		if (singleConfig.getDefaultMockFunction() != null) {
			aceEditorDefaultMockFunction.setValue(singleConfig.getDefaultMockFunction());
		}
		return aceEditorDefaultMockFunction;
	}

	private Component buildJSFormatDisplayPanel() {
		customizeAceEditor(aceEditorFormatDisplay);
		if (singleConfig.getFormatDisplayJsFunction() != null) {
			aceEditorFormatDisplay.setValue(singleConfig.getFormatDisplayJsFunction());
		}
		return aceEditorFormatDisplay;
	}

	private Component buildTraceFunctionPane() {
		customizeAceEditor(aceEditorTraceFunction);
		if (singleConfig.getTraceFunctionJsFunction() != null) {
			aceEditorTraceFunction.setValue(singleConfig.getTraceFunctionJsFunction());
		}
		return aceEditorTraceFunction;
	}

	private void customizeAceEditor (AceEditor aceEditor) {
		aceEditor.setMode(AceMode.javascript);
		aceEditor.setTheme(AceTheme.eclipse);
		aceEditor.addValueChangeListener( e -> this.changedEditor(aceEditor));
		aceEditor.setSizeFull();
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


		Component excludedHost = buildExcludedHostListGrid();

		GridLayout gridExcludedHost = createInLabelBox(SmockerUI.getBundleValue("Manage_Excluded_Host"), excludedHost);
		layout.addComponent(gridExcludedHost);
		layout.setExpandRatio(gridExcludedHost, 1.0f);

		Component includedHost = buildIncludedHostListGrid();
		GridLayout gridIncludedHost = createInLabelBox(SmockerUI.getBundleValue("Manage_Included_Host"), includedHost);
		layout.addComponent(gridIncludedHost);
		layout.setExpandRatio(gridIncludedHost, 1.0f);

		layout.addComponent(fillerRight);
		layout.setWidth("100%");
		return layout;
	}



	public List<DuplicateHost> getDuplicateHosts() {
		return duplicateHosts;
	}

	public List<HostAndPortRange> getExcludedHosts() {
		return excludedHosts;
	}

	public List<HostAndPortRange> getIncludedHosts() {
		return includedHosts;
	}

	private Component buildDupHostGrid() {

		GridLayout gridInteration  = new GridLayout(1, 2);
		gridInteration.setWidth("100%");

		HorizontalLayout layoutButtons = new HorizontalLayout();
		Button editButton = new Button(VaadinIcons.PENCIL);
		editButton.setDescription(SmockerUI.getBundleValue("Edit_Dup_Host_toolTip"));
		editButton.setEnabled(false);
		editButton.addClickListener(this::editDupHost);
		layoutButtons.addComponent(editButton);

		removeDupHostHostButton = new Button(VaadinIcons.MINUS);
		removeDupHostHostButton.setDescription(SmockerUI.getBundleValue("Remove_Excluded_DupHost_toolTip"));
		removeDupHostHostButton.setEnabled(false);
		removeDupHostHostButton.addClickListener(this::removeDuplicateHostList);
		layoutButtons.addComponent(removeDupHostHostButton);



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




	private Component buildExcludedHostListGrid() {

		GridLayout gridInteration  = new GridLayout(1, 2);
		gridInteration.setWidth("100%");

		HorizontalLayout layoutButtons = new HorizontalLayout();


		removeExcludedHostButton = new Button(VaadinIcons.MINUS);
		removeExcludedHostButton.setDescription(SmockerUI.getBundleValue("Remove_Excluded_Host_toolTip"));
		removeExcludedHostButton.setEnabled(false);
		removeExcludedHostButton.addClickListener(this::removeExludedHost);
		layoutButtons.addComponent(removeExcludedHostButton);

		Button addButton = new Button(VaadinIcons.PLUS);

		addButton.setDescription(SmockerUI.getBundleValue("Add_Excluded_Host_toolTip"));
		addButton.addClickListener(this::addExcludedHost);
		layoutButtons.addComponent(addButton);


		gridInteration.addComponent(layoutButtons, 0, 0);

		TreeGrid<HostAndPortRange> exludedHostGrid = new TreeGrid<>();

		exludedHostGrid.setSelectionMode(SelectionMode.SINGLE);
		exludedHostGrid.addColumn(HostAndPortRange::toString).setCaption(SmockerUI.getBundleValue("Excluded_Host_Column"));
		treeDataExcludedHost = new TreeData<>();
		treeDataProviderExcludedHost = new TreeDataProvider<>(treeDataExcludedHost);
		exludedHostGrid.setDataProvider(treeDataProviderExcludedHost);
		exludedHostGrid.setWidth("100%");

		refreshDupHost();
		gridInteration.addComponent(exludedHostGrid, 0, 1);

		exludedHostGrid.addSelectionListener(this::gridExcludedHostSelected);
		excludedHosts = getListFromDb(DaoConfig.getSingleConfig().getExcludedHosts());
		refreshHost(excludedHosts, treeDataProviderExcludedHost, treeDataExcludedHost);
		return gridInteration;
	}


	private Component buildIncludedHostListGrid() {
		GridLayout gridInteration  = new GridLayout(1, 2);
		gridInteration.setWidth("100%");

		HorizontalLayout layoutButtons = new HorizontalLayout();


		removeIncludedHostButton = new Button(VaadinIcons.MINUS);
		removeIncludedHostButton.setDescription(SmockerUI.getBundleValue("Remove_Included_Host_toolTip"));
		removeIncludedHostButton.setEnabled(false);
		removeIncludedHostButton.addClickListener(this::removeIncludedHost);
		layoutButtons.addComponent(removeIncludedHostButton);

		Button addButton = new Button(VaadinIcons.PLUS);

		addButton.setDescription(SmockerUI.getBundleValue("Add_Included_Host_toolTip"));
		addButton.addClickListener(this::addIncludedHost);
		layoutButtons.addComponent(addButton);

		gridInteration.addComponent(layoutButtons, 0, 0);

		TreeGrid<HostAndPortRange> includedHostGrid = new TreeGrid<>();

		includedHostGrid.setSelectionMode(SelectionMode.SINGLE);
		includedHostGrid.addColumn(HostAndPortRange::toString).setCaption(SmockerUI.getBundleValue("Included_Host_Column"));
		treeDataIncludedHost = new TreeData<>();
		treeDataProviderIncludedHost = new TreeDataProvider<>(treeDataIncludedHost);
		includedHostGrid.setDataProvider(treeDataProviderIncludedHost);
		includedHostGrid.setWidth("100%");

		refreshDupHost();
		gridInteration.addComponent(includedHostGrid, 0, 1);

		includedHostGrid.addSelectionListener(this::gridIncludedHostSelected);


		includedHosts = getListFromDb(DaoConfig.getSingleConfig().getIncludedHosts());
		refreshHost(includedHosts, treeDataProviderIncludedHost, treeDataIncludedHost);
		return gridInteration;
	}


	private void refreshDupHost() {
		treeDataDupHost.clear();
		for (DuplicateHost dupHost : duplicateHosts) {
			treeDataDupHost.addItem(null, dupHost);
		}
		treeDataProviderDupHost.refreshAll();
	}

	private void refreshHost(List<HostAndPortRange> listHost, TreeDataProvider<HostAndPortRange> dataProvider,
			TreeData<HostAndPortRange> treeData) {
		treeData.clear();
		if (listHost != null) {
			for (HostAndPortRange host : listHost) {
				treeData.addItem(null, host);
			}
			dataProvider.refreshAll();
		}
	}



	private void editDupHost(ClickEvent event) {
		DupHostEditor dupHostEditor = new DupHostEditor(selectedDuplicateHost);
		MessageBox displayComponentBox = 
				Dialog.displayComponentBox(SmockerUI.getBundleValue("Dup_Host"), 
						this::dupHostUpdated, dupHostEditor, true, true);
		dupHostEditor.setBox(displayComponentBox);
	}

	private void dupHostUpdated(DuplicateHost dupHost) {
		updateDupHostInDB();
	}

	private void addDupHost(ClickEvent event) {
		DuplicateHost dupHost = new DuplicateHost();
		DupHostEditor dupHostEditor = new DupHostEditor(dupHost);
		MessageBox displayComponentBox = 
				Dialog.displayComponentBox(SmockerUI.getBundleValue("Dup_Host"), 
						this::dupHostCreated, dupHostEditor, true, true);
		dupHostEditor.setBox(displayComponentBox);
	}

	private void addExcludedHost(ClickEvent event) {
		HostAndPortRange hostAndPort = new HostAndPortRange("", "");
		HostAndPortEditor hostAndPortEditor = new HostAndPortEditor(hostAndPort);
		hostAndPort.setSource(hostAndPortEditor);
		MessageBox displayComponentBox = Dialog.displayComponentBox(
				SmockerUI.getBundleValue("Excluded_Host"), null, hostAndPortEditor, false, false);
		hostAndPortEditor.setBox(displayComponentBox);
		hostAndPortEditor.setCallBAckAfterCheck(this::excludedHostCreated);
	}

	private void removeDuplicateHostList(ClickEvent event) {
		duplicateHosts.remove(selectedDuplicateHost);
		updateDupHostInDB();
	}

	private void removeExludedHost(ClickEvent event) {
		excludedHosts.remove(selectedExcludedHost);
		updateExcludedHostInDB();
	}

	private void addIncludedHost(ClickEvent event) {
		HostAndPortRange hostAndPort = new HostAndPortRange("", "");
		HostAndPortEditor hostAndPortEditor = new HostAndPortEditor(hostAndPort);
		hostAndPort.setSource(hostAndPortEditor);
		MessageBox displayComponentBox = Dialog.displayComponentBox(
				SmockerUI.getBundleValue("Included_Host"), null, hostAndPortEditor, false, false);
		hostAndPortEditor.setBox(displayComponentBox);
		hostAndPortEditor.setCallBAckAfterCheck(this::includedHostCreated);
	}


	private void removeIncludedHost(ClickEvent event) {
		includedHosts.remove(selectedIncludedHost);
		updateIncludedHostInDB();
	}


	private void dupHostCreated(DuplicateHost dupHost) {
		duplicateHosts.add(dupHost);
		updateDupHostInDB();
	}

	private void excludedHostCreated(HostAndPortRange hostAndPort) {
		excludedHosts.add(hostAndPort);
		updateExcludedHostInDB();
	}

	private void includedHostCreated(HostAndPortRange hostAndPort) {
		includedHosts.add(hostAndPort);
		updateIncludedHostInDB();
	}

	private void updateDupHostInDB() {
		DaoConfig.getSingleConfig().setDuplicateHosts(DuplicateHost.getDbValueFromList(duplicateHosts));
		DaoConfig.saveConfig();
		refreshDupHost();
	}

	private void updateExcludedHostInDB() {
		String listInString = buildStringWithSepFromList(excludedHosts);
		DaoConfig.getSingleConfig().setExcludedHosts(listInString);
		DaoConfig.saveConfig();
		refreshHost(excludedHosts, treeDataProviderExcludedHost, treeDataExcludedHost);
	}

	private void updateIncludedHostInDB() {
		String listInString = buildStringWithSepFromList(includedHosts);
		DaoConfig.getSingleConfig().setIncludedHosts(listInString);
		DaoConfig.saveConfig();
		refreshHost(includedHosts, treeDataProviderIncludedHost, treeDataIncludedHost);
	}

	private String buildStringWithSepFromList(List<HostAndPortRange> listHost) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < listHost.size(); i++) {
			sb.append(listHost.get(i));
			if (i < listHost.size() - 1) {
				sb.append(SEP_IGNORED_HOST);
			}
		}
		return sb.toString();
	}

	private List<HostAndPortRange> getListFromDb(String dbString) {
		List<HostAndPortRange> ret = new ArrayList<>();
		if (dbString != null) {
			List<String> listHostAndPort = Arrays.asList(dbString.split(SEP_IGNORED_HOST));
			for (String hostAndPortString : listHostAndPort) {
				String[] splitHostAnPort = hostAndPortString.split(" ");
				if (splitHostAnPort.length == 2) {
					ret.add(new HostAndPortRange(splitHostAnPort[0], splitHostAnPort[1]));
				}
			}
		}
		return ret;

	}

	private void gridDupHostSelected(SelectionEvent<DuplicateHost> dupHost) {
		if (dupHost.getFirstSelectedItem().isPresent()) {
			selectedDuplicateHost = dupHost.getFirstSelectedItem().orElse(null);
			removeDupHostHostButton.setEnabled(true);
		}
		else {
			removeDupHostHostButton.setEnabled(false);
		}

	}
	
	@Override
	public void enterInView(ViewChangeEvent event) {
		SmockerUI.getInstance().checkEnableSearch();
	}

	private void gridExcludedHostSelected(SelectionEvent<HostAndPortRange> host) {
		Optional<HostAndPortRange> firstSelectedItem = host.getFirstSelectedItem();
		if (firstSelectedItem.isPresent()) {
			selectedExcludedHost = firstSelectedItem.get();
			removeExcludedHostButton.setEnabled(true);
		}
		else {
			selectedExcludedHost = null;
			removeExcludedHostButton.setEnabled(false);
		}
	}

	private void gridIncludedHostSelected(SelectionEvent<HostAndPortRange> host) {
		Optional<HostAndPortRange> firstSelectedItem = host.getFirstSelectedItem();
		if (firstSelectedItem.isPresent()) {
			selectedIncludedHost = firstSelectedItem.get();
			removeIncludedHostButton.setEnabled(true);
		}
		else {
			selectedIncludedHost = null;
			removeIncludedHostButton.setEnabled(false);
		}
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
		SmockerUI.getInstance().displayNotif(SmockerUI.getBundleValue("Global_Var_Cleaned"), 0);
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
