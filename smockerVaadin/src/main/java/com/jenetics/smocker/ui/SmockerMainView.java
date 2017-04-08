package com.jenetics.smocker.ui;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.inject.Inject;

import org.jboss.logging.Logger;

import com.jenetics.smocker.annotation.RootView;
import com.jenetics.smocker.model.Connection;
import com.jenetics.smocker.ui.util.AnnotationScanner;
import com.jenetics.smocker.ui.util.ButtonWithOrderId;
import com.jenetics.smocker.ui.util.TreeWithIcon;
import com.jenetics.smocker.ui.util.ViewAndIconContainer;
import com.jenetics.smocker.ui.view.ConnectionsView;
import com.vaadin.addon.jpacontainer.JPAContainer;
import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.navigator.ViewDisplay;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Resource;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Accordion;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TabSheet.Tab;
import com.vaadin.ui.Table;
import com.vaadin.ui.Tree;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.VerticalSplitPanel;
import com.vaadin.ui.themes.Reindeer;

public class SmockerMainView extends VerticalSplitPanel implements View {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Inject 
	private Logger logger;
	
	JPAContainer<Connection> connections = null;
	private Navigator navigator;

	private Accordion accordion;

	private VerticalLayout navigationLayout;
	
	
	public Navigator getNavigator() {
		return navigator;
	}

	private static SmockerMainView instance = null;
	
	
	public static SmockerMainView getInstance() {
		return instance;
	}

	public SmockerMainView() {
		super();
		
		HorizontalSplitPanel down = new HorizontalSplitPanel();
		addStyleName(Reindeer.SPLITPANEL_SMALL);
		ComponentContainer mainArea = buildMainArea();
		down.setSecondComponent(mainArea);
		try {
			down.setFirstComponent(buildNavigation(mainArea));
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
			logger.error("Unable to build navigation area", e);
		} 
		down.setSplitPosition(300, Unit.PIXELS);
		down.setSizeFull();
		
		setSecondComponent(down);
		setFirstComponent(buildTopBar());
		setSplitPosition(55, Unit.PIXELS);
		down.setLocked(true);
		down.addStyleName(Reindeer.SPLITPANEL_SMALL);
		setSizeFull();
		setLocked(true);
		instance = this;
		
	}

	private Component buildTopBar() {
		HorizontalLayout horizontalLayout = new HorizontalLayout();
		Image image = new Image(null, new ThemeResource("icons/smockerLogo2.png"));
		horizontalLayout.addComponent(image);
		horizontalLayout.setSizeFull();
		horizontalLayout.setStyleName("topBannerBackGround");
		return horizontalLayout;
		
	}

	private ComponentContainer buildMainArea() {
		
		VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setSizeFull();
        verticalLayout.setStyleName("mainBackGround");
        return verticalLayout;
	}

	private ComponentContainer buildNavigation(ComponentContainer target) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
	
		VerticalLayout parentLayout = new VerticalLayout();
		
		navigationLayout = new VerticalLayout();
		navigationLayout.setImmediate(true);
        setFirstComponent(navigationLayout);
        navigationLayout.addStyleName("mainBackGround");
        
        
        ViewDisplay viewDisplay = new Navigator.ComponentContainerViewDisplay(target);
		navigator = new Navigator(SmockerUI.getInstance(), viewDisplay);
        
        parentLayout.setSizeFull();
        parentLayout.addComponent(navigationLayout);
        
        return parentLayout;
	}

	public void buildAccordion() {
		Map<String, TreeWithIcon> treeByAccordeonItem = SmockerUI.getInstance().getTreeMap(navigator);
		accordion = new Accordion();
		treeByAccordeonItem.forEach((name, treeWithIcon)-> {
			Resource icon = null;
			if (treeWithIcon.isFontAwesome()) {
				Field fontAwasomeField = null;
				try {
					fontAwasomeField = FontAwesome.class.getDeclaredField(treeWithIcon.getIcon());
					if (fontAwasomeField != null && !treeWithIcon.getIcon().equals(RootView.NOT_SET)) {
						icon = (Resource) fontAwasomeField.get(null);
					}
					else if (!treeWithIcon.getIcon().equals(RootView.NOT_SET)){
						icon = new ThemeResource(treeWithIcon.getIcon());
					}	
				} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
					logger.error("Unable to get field :" + treeWithIcon.getIcon() + " in FontAwesome", e);
				}
			}
			accordion.addTab(treeWithIcon.getTree(), name, icon).setStyleName("smocker");
		});
		
		navigator.addView("", new ConnectionsView());
		//navigator.navigateTo("");
        
        
        accordion.setSizeFull();
        navigationLayout.addComponent(accordion);
	}

	@Override
	public void enter(ViewChangeEvent event) {
		Notification.show("test enter");
	}
}
