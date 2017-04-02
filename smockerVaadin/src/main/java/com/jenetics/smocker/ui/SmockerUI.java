package com.jenetics.smocker.ui;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.jboss.logging.Logger;

import com.jenetics.smocker.ui.util.AnnotationScanner;
import com.jenetics.smocker.ui.util.TreeWithIcon;
import com.jenetics.smocker.ui.util.ViewAndIconContainer;
import com.vaadin.annotations.Push;
import com.vaadin.annotations.Theme;
import com.vaadin.navigator.Navigator;
import com.vaadin.server.VaadinRequest;
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
	private Map<String, TreeWithIcon> treeMap; 

	public Map<String, ViewAndIconContainer> getViewMap() {
		return viewMap;
	}


	@Override
	protected void init(VaadinRequest request) {
		instance = this;
		getPage().setTitle("Smocker");
		
//		try {
//			treeMap = AnnotationScanner.getTreeMap();
//		} catch (InstantiationException | IllegalAccessException e ) {
//			logger.error("Unable to get the view map", e);
//		} 
		
		mainContent = new SmockerMainView();
		setContent(mainContent);
	}
	
	
	public void refreshView(String viewName) {
	access(new Runnable() {
	    @Override
	    public void run() {
	    	try {
				Thread.sleep(SLEEP_TIME);
				viewMap.get(viewName).getRefreshableView().refresh();
			} catch (InterruptedException e) {
				logger.error("Unable to get the view map", e);
			}
	    }
	});
}


	public Map<String, TreeWithIcon> getTreeMap(Navigator navigator) throws InstantiationException, IllegalAccessException {
		return AnnotationScanner.getTreeMap(navigator);
	}
	


}
