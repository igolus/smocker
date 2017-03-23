package com.jenetics.smocker.ui;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.enterprise.event.Observes;
import javax.validation.Payload;

import org.jsoup.nodes.Element;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.vaadin.aceeditor.AceEditor;

import com.jenetics.smocker.annotation.ContentView;
import com.jenetics.smocker.model.Connection;
import com.jenetics.smocker.ui.util.SmockerSingleComponentContainer;
import com.jenetics.smocker.ui.view.ConnectionsView;
import com.vaadin.addon.jpacontainer.JPAContainer;
import com.vaadin.addon.jpacontainer.JPAContainerFactory;
import com.vaadin.data.Item;
import com.vaadin.demo.jpaaddressbook.JpaAddressbookUI;
import com.vaadin.demo.jpaaddressbook.domain.Person;
import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.ClientMethodInvocation;
import com.vaadin.server.ErrorHandler;
import com.vaadin.server.Extension;
import com.vaadin.server.FileResource;
import com.vaadin.server.Resource;
import com.vaadin.server.ServerRpcManager;
import com.vaadin.server.Sizeable;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinResponse;
import com.vaadin.server.ClassResource;
import com.vaadin.server.ClientConnector.AttachListener;
import com.vaadin.server.ClientConnector.DetachListener;
import com.vaadin.server.Sizeable.Unit;
import com.vaadin.server.ThemeResource;
import com.vaadin.shared.communication.SharedState;
import com.vaadin.navigator.ViewDisplay;
import com.vaadin.ui.AbstractComponentContainer;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.HasComponents;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.Command;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.declarative.DesignContext;

import elemental.json.JsonObject;

import com.vaadin.ui.Notification;
import com.vaadin.ui.SingleComponentContainer;
import com.vaadin.ui.Table;
import com.vaadin.ui.Tree;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Component.Listener;
import com.vaadin.ui.HasComponents.ComponentAttachListener;
import com.vaadin.ui.HasComponents.ComponentDetachListener;

public class SmockerMainView extends HorizontalSplitPanel implements View {
	
	JPAContainer<Connection> connections = null;
	private Table connectionTable;
	private Navigator navigator;
	private Tree tree;
	
	
	public SmockerMainView() {
		super();
		
		//SmockerSingleComponentContainer singleView = new SmockerSingleComponentContainer();
		
		
		VerticalLayout layoutTarget = new VerticalLayout();
		layoutTarget.addStyleName("backColorGrey");
		
		ViewDisplay viewDisplay = new Navigator.ComponentContainerViewDisplay(layoutTarget);
		setSecondComponent(layoutTarget);
		
		// Create a navigator to control the views
        navigator = new Navigator(UI.getCurrent(), viewDisplay);

        // Create and register the views
        //navigator.addView("", new StartView());
        navigator.addView("home", new StartView(navigator));
        navigator.addView("Connection", new ConnectionsView());
		
		
		connections = JPAContainerFactory.make(Connection.class,
	            SmockerUI.PERSISTENCE_UNIT);
		//buildMainArea();
		buildNavigation();
		navigator.navigateTo("home");
		
		setSplitPosition(300, Unit.PIXELS);
		setLocked(true);
		
	}

	private void buildMainArea() {
		setSizeFull();
		VerticalLayout verticalLayout = new VerticalLayout();
        setSecondComponent(verticalLayout);
        connectionTable = new Table(null, connections);
        connectionTable.setSelectable(true);
        connectionTable.setSizeFull();
        verticalLayout.setSizeFull();
        verticalLayout.addComponent(connectionTable);
        setSplitPosition(30);
	}

	private void buildNavigation() {
//		tree = new Tree("Smocker");
//        addComponent(tree);
//        setFirstComponent(tree);
//        
//        tree.addItem("Smocker");
//        tree.addItem("Connection");
//        
//        tree.setParent("Connection", "Smocker");
//        
//        tree.addValueChangeListener(event -> { 
//            if (event.getProperty() != null &&
//                event.getProperty().getValue() != null) {
//           		//System.out.println(event.getProperty().getValue());
//           		navigator.navigateTo((String) event.getProperty().getValue());
//            }
//        });
		
		VerticalLayout verticalLayout = new VerticalLayout();
        setFirstComponent(verticalLayout);
		
		Button testButton = new Button("test");
		testButton.setIcon(new ClassResource("/WEB-INF/icons/Web-icon.png"));
        
		verticalLayout.addComponent(testButton);
        
		
		
		
//		MenuBar menu = new MenuBar();
//		menu.addItem("test", new Command() {
//
//			@Override
//			public void menuSelected(MenuItem selectedItem) {
//				navigator.navigateTo("Connection");
//				
//			}});
//		
//		menu.addItem("home", new Command() {
//
//			@Override
//			public void menuSelected(MenuItem selectedItem) {
//				navigator.navigateTo("home");
//				
//			}});
//		
//		setFirstComponent(menu);
	}

	@Override
	public void enter(ViewChangeEvent event) {
		Notification.show("test enter");
	}
}
