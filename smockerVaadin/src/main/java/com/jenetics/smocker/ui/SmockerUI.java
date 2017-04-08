package com.jenetics.smocker.ui;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.jboss.logging.Logger;
import org.vaadin.easyapp.util.MessageBuilder;

import com.jenetics.smocker.model.EntityWithId;
import com.jenetics.smocker.ui.util.AnnotationScanner;
import com.jenetics.smocker.ui.util.RefreshableView;
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

	@PersistenceContext(unitName = SmockerUI.PERSISTENCE_UNIT)
	private EntityManager em;

	@Inject
	private Logger logger;

	public static final String PERSISTENCE_UNIT = "smockerLocalData";

	Navigator navigator;
	protected static final String MAINVIEW = "main";

	private static SmockerUI instance = null;

	public EntityManager getEm() {
		return em;
	}


	public static SmockerUI getInstance() {
		return instance;
	}

	private SmockerMainView mainContent = null;

	private HashMap<String, ViewAndIconContainer> viewMap; 
	private Map<String, TreeWithIcon> treeMap;

	private AnnotationScanner scanner; 

	public Map<String, ViewAndIconContainer> getViewMap() {
		return viewMap;
	}


	@Override
	protected void init(VaadinRequest request) {
		instance = this;
		getPage().setTitle("Smocker");
		mainContent = new SmockerMainView();
		setContent(mainContent);
		try {
			scanner = new AnnotationScanner(mainContent.getNavigator());
		} catch (InstantiationException | IllegalAccessException e) {
			logger.error(MessageBuilder.getEasyAppMessage("Unable to get the view map"), e);
		}
		mainContent.buildAccordion();
	}


	public void refreshView(String viewName, EntityWithId entityWithId) {
		access(new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(SLEEP_TIME);
					((RefreshableView)scanner.getViewMap().get(viewName)).refresh(entityWithId);
				} catch (InterruptedException e) {
					logger.error(MessageBuilder.getEasyAppMessage("Unable to get the view map"), e);
				}
			}
		});
	}


	public Map<String, TreeWithIcon> getTreeMap(Navigator navigator) {
		return scanner.getTreeMap();
	}

}
