package com.jenetics.smocker.ui.view;

import java.util.List;
import java.util.logging.Level;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.vaadin.aceeditor.AceEditor;
import org.vaadin.aceeditor.AceMode;
import org.vaadin.aceeditor.AceTheme;
import org.vaadin.easyapp.util.ActionContainer;
import org.vaadin.easyapp.util.ActionContainerBuilder;
import org.vaadin.easyapp.util.EasyAppLayout;
import org.vaadin.easyapp.util.ActionContainer.InsertPosition;
import org.vaadin.easyapp.util.annotations.ContentView;
import org.vaadin.easybinder.data.AutoBinder;

import com.jenetics.smocker.dao.DaoConfig;
import com.jenetics.smocker.dao.IDaoManager;
import com.jenetics.smocker.model.Communication;
import com.jenetics.smocker.model.config.ConfigBean;
import com.jenetics.smocker.model.config.FormConfig;
import com.jenetics.smocker.model.config.SmockerConf;
import com.jenetics.smocker.ui.SmockerUI;
import com.jenetics.smocker.ui.component.LoggerPanel;
import com.jenetics.smocker.ui.dialog.Dialog;
import com.vaadin.annotations.Push;
import com.vaadin.data.HasValue.ValueChangeEvent;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.spring.annotation.ViewScope;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TabSheet;

@SuppressWarnings("serial")
@Push
@ViewScope
@ContentView(sortingOrder = 4, viewName = "ConfigView", icon = "icons/Settings-icon.png", homeView = true, rootViewParent = ConnectionsRoot.class)
public class ConfigView extends EasyAppLayout {

	protected IDaoManager<SmockerConf> daoManagerSmockerConf = null;
	private SmockerConf singleConfig;
	private static SmockerConf smockerConfSingleItem = null;
	protected final TabSheet tabSheet = new TabSheet();
	private AceEditor aceEditorGlobalFunctions = new AceEditor();
	private AceEditor aceEditorFilter = new AceEditor();
	private AceEditor aceEditorFormatDisplay = new AceEditor();
	private AceEditor aceEditorDefaultMockFunction = new AceEditor();
	
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
		HorizontalLayout layout = new HorizontalLayout();
		VerticalLayout fillerRight = new VerticalLayout();
		
		fillerRight.setWidth(0, Unit.PIXELS);
		
		Label label = new Label("<b>UI Conf</b>", ContentMode.HTML);
		//addComponent(label);
		
		GridLayout grid = new GridLayout(1, 2);
		grid.setWidth("100%");
		grid.setStyleName("Config");
		grid.addComponent(label, 0, 0);
		
		CheckBox autoRefreshBox = createCheckBoxAutoRefresh();
		grid.addComponent(autoRefreshBox, 0, 1);
		
		layout.addComponent(grid);
		layout.setExpandRatio(grid, 1.0f);
		layout.addComponent(fillerRight);
		layout.setSizeFull();
		layout.setSizeFull();
		layout.setWidth("100%");
		return layout;
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
