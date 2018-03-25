package com.jenetics.smocker.ui;

import java.util.Collections;
import java.util.ResourceBundle;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Persistence;

import org.jboss.logging.Logger;
import org.vaadin.easyapp.EasyAppBuilder;
import org.vaadin.easyapp.EasyAppMainView;
import org.vaadin.easyapp.ui.ViewWithToolBar;
import org.vaadin.easyapp.util.MessageBuilder;

import com.jenetics.smocker.model.EntityWithId;
import com.jenetics.smocker.ui.util.RefreshableView;
import com.vaadin.annotations.Push;
import com.vaadin.annotations.Theme;
import com.vaadin.navigator.Navigator;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.ThemeResource;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.Image;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

@Push
@Theme("mytheme")
public class SmockerUI extends UI {

	private static final int SLEEP_TIME = 200;

	// @PersistenceContext(unitName = SmockerUI.PERSISTENCE_UNIT)
	private static EntityManager em;

	@Inject
	private Logger logger;
	
	public static final String BUNDLE_NAME = "BundleUI";
	
	private static ResourceBundle bundle = ResourceBundle.getBundle(BUNDLE_NAME);

	public static final String PERSISTENCE_UNIT = "smockerLocalData";

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
			em = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT).createEntityManager();
		}
		return em;
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

		easyAppMainView = new EasyAppBuilder(Collections.singletonList("com.jenetics.smocker.ui.view"))
				.withTopBarIcon(image).withTopBarStyle("topBannerBackGround")
				.withSearchCapabilities((searchValue) -> search(searchValue), FontAwesome.SEARCH).withBreadcrumb()
				.withBreadcrumbStyle("breadcrumbStyle")
				.withToolBar()
				.withNavigationIcon(image)
				// .withLoginPopupLoginStyle("propupStyle")
				.build(this);

		//easyAppMainView.setSplitPosition(93);

		layout.addComponents(easyAppMainView);

		//easyAppMainView.getTopBar().setStyleName("topBannerBackGround");

		setContent(layout);
		instance = this;
	}

	public enum EnumButton {
		REMOVE, STACK, ADD_TO_MOCK
	}

	private Object search(String searchValue) {
		return null;
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

}
