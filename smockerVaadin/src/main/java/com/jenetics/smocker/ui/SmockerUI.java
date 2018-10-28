package com.jenetics.smocker.ui;

import java.util.Collections;
import java.util.ResourceBundle;
import java.util.logging.Level;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Persistence;

import org.jboss.logging.Logger;
import org.vaadin.easyapp.EasyAppBuilder;
import org.vaadin.easyapp.EasyAppMainView;
import org.vaadin.easyapp.ui.ViewWithToolBar;
import org.vaadin.easyapp.util.ActionContainerBuilder;
import org.vaadin.easyapp.util.MessageBuilder;
import org.vaadin.easyapp.util.ActionContainer.Position;

import com.jenetics.smocker.model.EntityWithId;
import com.jenetics.smocker.rest.AliveEndPoint;
import com.jenetics.smocker.ui.util.RefreshableView;
import com.jenetics.smocker.ui.view.LogView;
import com.vaadin.annotations.Push;
import com.vaadin.annotations.Theme;
import com.vaadin.navigator.Navigator;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.ThemeResource;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.Component;
import com.vaadin.ui.Image;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

@Push
@Theme("mytheme")
public class SmockerUI extends UI {

	private static final String SUB_WINDOW_DEFAULT_WIDTH = "600px";

	private static final String SUB_WINDOW_DEFAULT_HEIGHT = "800px";

	private static final int SLEEP_TIME = 200;

	private static EntityManager em;
	
	private static EntityManager emPersitant;

	@Inject
	private Logger logger;
	
	public static final String BUNDLE_NAME = "BundleUI";
	
	private static ResourceBundle bundle = ResourceBundle.getBundle(BUNDLE_NAME);

	public static final String PERSISTENCE_UNIT_MEMORY = "smockerLocalData";
	public static final String PERSISTENCE_UNIT_DB = "smockerPersistantData";

	Navigator navigator;
	protected static final String MAINVIEW = "main";

	private static SmockerUI instance = null;
	
	public static ResourceBundle getBundle() {
		return bundle;
	}
	
	/**
	 * Try to get the bundle value 
	 * return value if not found
	 */
	public static String getBundleValue(String value) {
		if (bundle.containsKey(value)) {
			return bundle.getString(value);
		}
		return value;
	}

	public static EntityManager getEm() {
		if (em == null) {
			em = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_MEMORY).createEntityManager();
		}
		return em;
	}
	
	

	public static EntityManager getEmPersitant() {
		if (emPersitant == null) {
			emPersitant = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_DB).createEntityManager();
		}
		return emPersitant;
	}

	public static SmockerUI getInstance() {
		return instance;
	}
	
	private EasyAppMainView easyAppMainView;

	public EasyAppMainView getEasyAppMainView() {
		return easyAppMainView;
	}

	@Override
	protected void init(VaadinRequest request) {

		final VerticalLayout layout = new VerticalLayout();
		layout.setSizeFull();

		Image image = new Image(null, new ThemeResource("smocker_small.png"));

		EasyAppBuilder easyAppBuilder = new EasyAppBuilder(Collections.singletonList("com.jenetics.smocker.ui.view"));
		
		easyAppBuilder.withNavigationIcon(image);
		easyAppBuilder.withTopBarIcon(image);
		easyAppBuilder.withRessourceBundle(null);
		easyAppBuilder.withNavigatorStyle("Nav");
		easyAppBuilder.withNavigationButtonSelectedStyle("Selected");
		easyAppBuilder.withContentStyle("Content");
		easyAppBuilder.withActionContainerStyle("Container");
		easyAppBuilder.withNavigatorSplitPosition(300);
		easyAppBuilder.withMenuCollapsable();
		easyAppBuilder.withTopBarStyle("TopBar");
		easyAppBuilder.withContextualTextLabelStyle("Contextual");
		easyAppBuilder.withNavigatorSplitPosition(200);
		easyAppBuilder.withRessourceBundle(getBundle());
		
		easyAppBuilder.withMenuCollapsable();
		
		ActionContainerBuilder actionContainerBuilder = new ActionContainerBuilder(null);
		actionContainerBuilder.addImageIcon(image,  Position.LEFT, null);
		actionContainerBuilder.addSearch(this::searchTriggered, Position.RIGHT, null);
		
		easyAppBuilder.withActionContainer(actionContainerBuilder.build());
		
		layout.addComponents(easyAppBuilder.build(this));
		easyAppMainView = easyAppBuilder.getMainView();

		setContent(layout);
		instance = this;
		AliveEndPoint.setInitialized(true);
	}

	public enum EnumButton {
		REMOVE, STACK, ADD_TO_MOCK
	}

	private Object search(String searchValue) {
		return null;
	}
	
	/**
	 * Display a subwindow from any component
	 * @param component
	 */
	public static Window displayInSubWindow(String title, Component component) {
		return displayInSubWindow(title, component, true);
	}
	
	public static Window displayInSubWindowMidSize(String title, Component component) {
		return displayInSubWindow(title, component, false);
	}
	
	/**
	 * Display a subwindow from any component
	 * @param component
	 */
	private static Window displayInSubWindow(String title, Component component, boolean fullSize) {
		Window subWindow = new Window(title);
		subWindow.setModal(true);
		subWindow.setContent(component);
		subWindow.center();
		
		subWindow.setHeight(SUB_WINDOW_DEFAULT_HEIGHT);
		subWindow.setWidth(SUB_WINDOW_DEFAULT_WIDTH);
		if (fullSize) {
			subWindow.setSizeFull();
		}
		getInstance().addWindow(subWindow);
		return subWindow;
	}
	
	public static void log(Level level, String message) {
		LogView logView = (LogView) getInstance().getEasyAppMainView().getScanner().getViewMap().get(LogView.class.toString());
		logView.appendMessage(level, message);
	}
	
	public static void log(Level level, String message, Exception ex) {
		LogView logView = (LogView) getInstance().getEasyAppMainView().getScanner().getViewMap().get(LogView.class.toString());
		logView.appendMessage(level, message, ex);
	}
	
	/**
	 * Display a subwindow from any component
	 * @param component
	 */
	public static void displayMessageInSubWindow(String title, String message) {
		Window subWindow = new Window(title);
		subWindow.setModal(true);
		TextArea textArea = new TextArea(message);
		textArea.setSizeFull();
		subWindow.setContent(textArea);
		subWindow.center();
		getInstance().addWindow(subWindow);
		subWindow.setHeight(SUB_WINDOW_DEFAULT_HEIGHT);
		subWindow.setWidth(SUB_WINDOW_DEFAULT_WIDTH);
		subWindow.setSizeFull();
	}
	

	public void refreshView(EntityWithId entityWithId) {
		access( ()  -> 
			{
				try {
					Thread.sleep(SLEEP_TIME);
					Class<?> currentViewClass =  easyAppMainView.getNavigator().getCurrentView().getClass();
					RefreshableView targetView = null;
					if (ViewWithToolBar.class.isAssignableFrom(currentViewClass)) {
						ViewWithToolBar viewWithToolBar =  (ViewWithToolBar)easyAppMainView.getNavigator().getCurrentView();
						if (RefreshableView.class.isAssignableFrom(viewWithToolBar.getInnerComponent().getClass())) {
							targetView = (RefreshableView) viewWithToolBar.getInnerComponent();
						}
					}
					else if (RefreshableView.class.isAssignableFrom(currentViewClass)) {
						targetView = ((RefreshableView) easyAppMainView.getNavigator().getCurrentView());
					}
					if (targetView != null) {
						targetView.refresh(entityWithId);
					}
				} catch (InterruptedException e) {
					logger.error(MessageBuilder.getEasyAppMessage("Unable to get the view map"), e);
					Thread.currentThread().interrupt();
				}
			});
	}
	
	public void searchTriggered(String search) {
		Notification.show("Search to be implemented");
	}

}
