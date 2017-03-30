package com.jenetics.smocker.ui;

import java.util.HashMap;

import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.jboss.logging.Logger;

import com.jenetics.smocker.model.Connection;
import com.jenetics.smocker.ui.util.AnnotationScanner;
import com.jenetics.smocker.ui.util.ViewAndIconContainer;
import com.jenetics.smocker.ui.view.ConnectionsView;
import com.vaadin.annotations.Push;
import com.vaadin.annotations.Theme;
import com.vaadin.event.UIEvents;
import com.vaadin.navigator.Navigator;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.Button;
import com.vaadin.ui.UI;

@Push
@Theme("smocker")
public class SmockerUI extends UI {

	private static final int SLEEP_TIME = 100;

	@Inject
	private Logger logger;
	
	public static final String PERSISTENCE_UNIT = "smockerLocalData";
	
	Navigator navigator;
    protected static final String MAINVIEW = "main";
	
    private static SmockerUI instance = null;
    
	public static SmockerUI getInstance() {
		return instance;
	}
	
	private SmockerMainView mainContent = null;
	
	private HashMap<String, ViewAndIconContainer> viewMap; 

	public HashMap<String, ViewAndIconContainer> getViewMap() {
		return viewMap;
	}


	@Override
	protected void init(VaadinRequest request) {
		instance = this;
		getPage().setTitle("Smocker");
		
		try {
			viewMap = AnnotationScanner.getViewMap();
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e ) {
			logger.error("Unable to get the view map", e);
		} 
		
		mainContent = new SmockerMainView();
		setContent(mainContent);
	}
	
	
	public void newConnection(String viewName) {
	access(new Runnable() {
	    @Override
	    public void run() {
	    	
	    	try {
				Thread.sleep(SLEEP_TIME);
				viewMap.get(viewName).getRefreshableView().refresh();
				//content.refresh();
			} catch (InterruptedException e) {
				logger.error("Unable to get the view map", e);
			}
	    }
	});
}
	


}
