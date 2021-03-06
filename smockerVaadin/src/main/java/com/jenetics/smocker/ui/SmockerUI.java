package com.jenetics.smocker.ui;

import java.util.Collections;
import java.util.Map;
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

import com.jenetics.smocker.dao.DaoConfigUpdaterThread;
import com.jenetics.smocker.lucene.LuceneIndexer;
import com.jenetics.smocker.model.EntityWithId;
import com.jenetics.smocker.model.JavaApplication;
import com.jenetics.smocker.model.event.CommunicationsRemoved;
import com.jenetics.smocker.rest.AliveEndPoint;
import com.jenetics.smocker.ui.util.RefreshableView;
import com.jenetics.smocker.ui.util.SearcheableView;
import com.jenetics.smocker.ui.view.JavaApplicationsView;
import com.jenetics.smocker.ui.view.LogView;
import com.jenetics.smocker.util.lang.ReflectionUtil;
import com.vaadin.annotations.Push;
import com.vaadin.annotations.Theme;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.View;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.server.ThemeResource;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.Component;
import com.vaadin.ui.Image;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Notification.Type;

@Push
@Theme("mytheme")
public class SmockerUI extends UI {
	

	private static final int DISPLAY_NOTIF_DELAY = 1000;
	
	private static final String SUB_WINDOW_DEFAULT_WIDTH = "600px";

	private static final String SUB_WINDOW_DEFAULT_HEIGHT = "800px";

	private static final int SLEEP_TIME = 100;

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
	
	private ActionContainerBuilder actionContainerBuilder = null;
	
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

	private ViewWithToolBar mainViewWithToolBar;

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
		
		actionContainerBuilder = new ActionContainerBuilder(null);
		actionContainerBuilder.addImageIcon(image,  Position.LEFT, null);
		actionContainerBuilder.addSearch(this::searchTriggered, Position.RIGHT, null);
		
		easyAppBuilder.withActionContainer(actionContainerBuilder.build());
		
		mainViewWithToolBar = easyAppBuilder.build(this);
		layout.addComponents(mainViewWithToolBar);
		easyAppMainView = easyAppBuilder.getMainView();

		setContent(layout);
		instance = this;
		AliveEndPoint.setInitialized(true);
		DaoConfigUpdaterThread.getInstance();
	}

	public enum EnumButton {
		REMOVE, STACK, ADD_TO_MOCK
	}

	private Object search(String searchValue) {
		return null;
	}
	
	public ViewWithToolBar getMainViewWithToolBar() {
		return mainViewWithToolBar;
	}

	/**
	 * Display a subwindow from any component
	 * @param component
	 */
	public static Window displayInSubWindow(String title, Component component) {
		return displayInSubWindow(title, component, 
				SUB_WINDOW_DEFAULT_WIDTH, SUB_WINDOW_DEFAULT_HEIGHT, true);
	}
	
	public static Window displayInSubWindowMidSize(String title, Component component) {
		return displayInSubWindow(title, component, 
				SUB_WINDOW_DEFAULT_WIDTH, SUB_WINDOW_DEFAULT_HEIGHT, false);
	}
	
	public static Window displayInSubWindowCustomSize(String title, Component component, 
			String width, String height) {
		return displayInSubWindow(title, component, 
				width, height, false);
	}
	
	/**
	 * Display a subwindow from any component
	 * @param component
	 */
	private static Window displayInSubWindow(String title, Component component, 
			String width, String height, boolean fullSize) {
		Window subWindow = new Window(title);
		subWindow.setModal(true);
		subWindow.setContent(component);
		subWindow.center();
		

		subWindow.setHeight(height);
		subWindow.setWidth(width);
		
		if (fullSize) {
			subWindow.setSizeFull();
		}
		getInstance().addWindow(subWindow);
		return subWindow;
	}
	
	public static void log(Level level, String message) {
		LogView logView = (LogView) getInstance().getEasyAppMainView().getScanner().getViewMap().get(LogView.class.toString());
		if (logView != null) {
			logView.appendMessage(level, message);
		}
	}
	
	public static void log(Level level, String message, Exception ex) {
		LogView logView = (LogView) getInstance().getEasyAppMainView().getScanner().getViewMap().get(LogView.class.toString());
		if (logView != null) {
			logView.appendMessage(level, message, ex);
		}
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
	
	public void remove(CommunicationsRemoved communicationsRemoved) {
		access( ()  -> 
		{
			
			try {
				Thread.sleep(SLEEP_TIME);
				Component javaApp = 
						getInstance().getEasyAppMainView().getScanner().getViewMap().get(JavaApplicationsView.class.toString());
				if (javaApp != null) {
					JavaApplicationsView javaAppView = (JavaApplicationsView)javaApp;
					javaAppView.remove(communicationsRemoved);
				}
			} catch (InterruptedException e) {
				logger.error(MessageBuilder.getEasyAppMessage("Unable to remove item"), e);
				Thread.currentThread().interrupt();
			}
		});
	}
	

	public void refreshView(EntityWithId entityWithId) {
		access( ()  -> 
			{
				try {
					Thread.sleep(SLEEP_TIME);
					
					View selectedView = easyAppMainView.getCurrentView();
					if (selectedView == null) {
						selectedView =  easyAppMainView.getNavigator().getCurrentView();
					}
					Class<?> currentViewClass =  selectedView.getClass();
					RefreshableView targetView = null;
					if (ViewWithToolBar.class.isAssignableFrom(currentViewClass)) {
						ViewWithToolBar viewWithToolBar =  (ViewWithToolBar)selectedView;
						if (viewWithToolBar.getInnerComponent() != null && RefreshableView.class.isAssignableFrom(viewWithToolBar.getInnerComponent().getClass())) {
							targetView = (RefreshableView) viewWithToolBar.getInnerComponent();
						}
					}
					else if (RefreshableView.class.isAssignableFrom(currentViewClass)) {
						targetView = ((RefreshableView) selectedView);
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
	
	public void searchTriggered(String searchQuery) {
		SearcheableView searcheableView = getSearcheableView();
		if (searcheableView != null) {
			searcheableView.search(searchQuery);
		}
	}
	
	public void enableSearch(boolean value) {
		if (value) {
			actionContainerBuilder.enableSarch();
		}
		else {
			actionContainerBuilder.disableSarch();
		}
	}
	
	public void checkEnableSearch() {
		SearcheableView searcheableView = getSearcheableView();
		if (searcheableView == null || !searcheableView.canSearch()) {
			enableSearch(false);
		}
		else {
			enableSearch(true);
		}
	}

	private SearcheableView getSearcheableView() {
		if (easyAppMainView.getNavigator() != null && easyAppMainView.getNavigator().getCurrentView() != null) {
			Class<?> currentViewClass =  easyAppMainView.getNavigator().getCurrentView().getClass();
			if (ViewWithToolBar.class.isAssignableFrom(currentViewClass)) {
				ViewWithToolBar viewWithToolBar =  (ViewWithToolBar)easyAppMainView.getNavigator().getCurrentView();
				if (viewWithToolBar.getInnerComponent() != null && SearcheableView.class.isAssignableFrom(viewWithToolBar.getInnerComponent().getClass())) {
					return (SearcheableView) viewWithToolBar.getInnerComponent();
				}
			}

		}
		return null;
	}

	public void displayNotif(String text, int delay) {
		Notification notif = new Notification(text, Type.ASSISTIVE_NOTIFICATION);
		// Customize it
		notif.setDelayMsec(delay == 0 ? DISPLAY_NOTIF_DELAY : delay);
		notif.setPosition(com.vaadin.shared.Position.BOTTOM_RIGHT);
		notif.setIcon(VaadinIcons.SPINNER);
		
		// Show it in the page
		if (SmockerUI.getInstance().getPage() != null) {
			notif.show(SmockerUI.getInstance().getPage());
		}
		
	}
	
	public static void doInBackGround(Runnable runnable, long sleepTime) {
		instance.access(() -> {
			try {
				Thread.sleep(sleepTime);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				SmockerUI.log(Level.SEVERE, "Unable to wait", e);
			}
			runnable.run();
		});
		
	}


}
