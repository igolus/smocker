package com.jenetics.smocker.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.inject.Inject;

import org.jboss.logging.Logger;

import com.jenetics.smocker.model.Connection;
import com.jenetics.smocker.ui.util.AnnotationScanner;
import com.jenetics.smocker.ui.util.ButtonWithOrderId;
import com.jenetics.smocker.ui.util.ViewAndIconContainer;
import com.vaadin.addon.jpacontainer.JPAContainer;
import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.navigator.ViewDisplay;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Image;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Table;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.VerticalSplitPanel;

public class SmockerMainView extends VerticalSplitPanel implements View {
	
	@Inject 
	private Logger logger;
	
	JPAContainer<Connection> connections = null;
	private Table connectionTable;
	private Navigator navigator;
	
	
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
		setSplitPosition(60, Unit.PIXELS);
		
		
		setSizeFull();
		setLocked(true);
		instance = this;
		
	}

	private Component buildTopBar() {
		HorizontalLayout horizontalLayout = new HorizontalLayout();
		Image image = new Image(null, new ThemeResource("icons/smocker_small.png"));
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
		
		VerticalLayout navigationLayout = new VerticalLayout();
		navigationLayout.setImmediate(true);
        setFirstComponent(navigationLayout);
        navigationLayout.addStyleName("mainBackGround");
		
        ViewDisplay viewDisplay = new Navigator.ComponentContainerViewDisplay(target);
		navigator = new Navigator(SmockerUI.getInstance(), viewDisplay);
        
		ArrayList<ButtonWithOrderId> buttons = new ArrayList<>();
		
        HashMap<String, ViewAndIconContainer> viewMap = SmockerUI.getInstance().getViewMap();
        for ( Map.Entry<String, ViewAndIconContainer> entry : viewMap.entrySet() ) {
            String viewName = entry.getKey();
            ViewAndIconContainer viewAndIconContainer = entry.getValue();
            
            navigator.addView(viewAndIconContainer.isHomeView() ? "" : viewName, viewAndIconContainer.getRefreshableView());
            
            ButtonWithOrderId buttonWithIcon = new ButtonWithOrderId(viewAndIconContainer.getOrder(), viewName, viewAndIconContainer.isHomeView());
            buttonWithIcon.setIcon(new ThemeResource(viewAndIconContainer.getIcon()));
            buttonWithIcon.addStyleName("button-with-right-alligned-icon");
            buttonWithIcon.setWidth(navigationLayout.getWidth(), navigationLayout.getWidthUnits());
			
			buttonWithIcon.addClickListener(new ClickListener() {
				@Override
				public void buttonClick(ClickEvent event) {
		            if (((ButtonWithOrderId)event.getButton()).isHomeView()) {
		            	navigator.navigateTo("");
		            }
		            else {
		            	navigator.navigateTo(viewName);
		            }
				}
            });
			
			buttons.add(buttonWithIcon);
        }
        
        buttons.sort((a, b) -> a.getSortingOrder() - b.getSortingOrder());
        
        for (ButtonWithOrderId buttonWithOrderId : buttons) {
        	navigationLayout.addComponent(buttonWithOrderId);
		}
        
        
        parentLayout.setSizeFull();
        parentLayout.addComponent(navigationLayout);
        
        return parentLayout;
	}

	@Override
	public void enter(ViewChangeEvent event) {
		Notification.show("test enter");
	}
}
