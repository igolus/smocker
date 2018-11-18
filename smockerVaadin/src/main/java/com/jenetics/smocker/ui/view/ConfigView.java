package com.jenetics.smocker.ui.view;

import java.util.List;
import java.util.logging.Level;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.vaadin.easyapp.util.ActionContainer;
import org.vaadin.easyapp.util.ActionContainerBuilder;
import org.vaadin.easyapp.util.EasyAppLayout;
import org.vaadin.easyapp.util.ActionContainer.InsertPosition;
import org.vaadin.easyapp.util.annotations.ContentView;
import org.vaadin.easybinder.data.AutoBinder;

import com.jenetics.smocker.dao.IDaoManager;
import com.jenetics.smocker.model.Communication;
import com.jenetics.smocker.model.config.ConfigBean;
import com.jenetics.smocker.model.config.FormConfig;
import com.jenetics.smocker.model.config.SmockerConf;
import com.jenetics.smocker.ui.SmockerUI;
import com.jenetics.smocker.ui.component.LoggerPanel;
import com.jenetics.smocker.ui.dialog.Dialog;
import com.vaadin.annotations.Push;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.spring.annotation.ViewScope;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;

@SuppressWarnings("serial")
@Push
@ViewScope
@ContentView(sortingOrder = 4, viewName = "LogsView", icon = "icons/log-file-1-504262.png", homeView = true, rootViewParent = ConnectionsRoot.class)
public class ConfigView extends EasyAppLayout {

	protected IDaoManager<SmockerConf> daoManagerSmockerConf = null;
	private static SmockerConf smockerConfSingleItem = null;
	
	public ConfigView() {
		
		
		GridLayout grid = new GridLayout(1, 1);
		grid.setStyleName("Config");
		grid.setSizeFull();
		CheckBox autoRefreshBox = new CheckBox(SmockerUI.getBundleValue("autoRefreshCheckBox"));
		
		grid.addComponent(autoRefreshBox, 0, 0);
		addComponent(grid);
		setExpandRatio(grid, 1.0f);
		setMargin(true);
		setSizeFull();
	}
}
