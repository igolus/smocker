package com.jenetics.smocker.ui;

import java.util.Collections;
import java.util.ResourceBundle;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import javax.persistence.PersistenceContext;

import org.jboss.logging.Logger;
import org.vaadin.easyapp.EasyAppBuilder;
import org.vaadin.easyapp.EasyAppMainView;
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
import com.vaadin.ui.themes.BaseTheme;

@Push
@Theme("smocker")
public class SmockerUI extends UI {

	private static ResourceBundle bundle = ResourceBundle.getBundle("BundleUI");
	
	private static final int SLEEP_TIME = 200;

	//@PersistenceContext(unitName = SmockerUI.PERSISTENCE_UNIT)
	private static EntityManager em;

	@Inject
	private Logger logger;

	public static final String PERSISTENCE_UNIT = "smockerLocalData";

	Navigator navigator;
	protected static final String MAINVIEW = "main";

	private static SmockerUI instance = null;

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
        
        
        Image image = new Image(null, new ThemeResource("icons/smocker_small.png"));
		
		easyAppMainView = new EasyAppBuilder(Collections.singletonList("com.jenetics.smocker.ui.view"))
        	.withTopBarIcon(image)
        	.withTopBarStyle("topBannerBackGround")
        	.withSearchCapabilities( (searchValue) -> search(searchValue) , FontAwesome.SEARCH)
        	.withBreadcrumb()
        	.withBreadcrumbStyle("breadcrumbStyle")
        	.withButtonLinkStyleInBreadCrumb(BaseTheme.BUTTON_LINK)
        	.withToolBar()
        	//.withLoginPopupLoginStyle("propupStyle")
        	.build();
		
		easyAppMainView.setSplitPosition(93);
		
//		manageButtons();
		
		layout.addComponents(easyAppMainView);
        
		easyAppMainView.getTopBar().setStyleName("topBannerBackGround");
        
        setContent(layout);
        instance = this;
	}

	public enum EnumButton {
		CLEAN_ALL,
		STACK
	}
	
//	private void manageButtons() {
//		easyAppMainView.getToolBar().addButton(bundle.getString("CleanAll"), bundle.getString("CleanAllToolTip"), FontAwesome.REMOVE, EnumButton.CLEAN_ALL.toString());
//	}


	private Object search(String searchValue) {
		// TODO Auto-generated method stub
		return null;
	}


	public void refreshView(String viewName, EntityWithId entityWithId) {
		access(new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(SLEEP_TIME);
					if (RefreshableView.class.isAssignableFrom(easyAppMainView.getNavigator().getCurrentView().getClass()))
					{
						((RefreshableView)easyAppMainView.getNavigator().getCurrentView()).refresh(entityWithId);
					}
				} catch (InterruptedException e) {
					logger.error(MessageBuilder.getEasyAppMessage("Unable to get the view map"), e);
				}
			}
		});
	}

}
