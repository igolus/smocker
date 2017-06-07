package com.jenetics.smocker.ui.view;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import javax.inject.Inject;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.jboss.logging.Logger;
import org.vaadin.easyapp.util.annotations.ContentView;

import com.jenetics.smocker.dao.DaoManager;
import com.jenetics.smocker.dao.IDaoManager;
import com.jenetics.smocker.injector.BundleUI;
import com.jenetics.smocker.injector.Dao;
import com.jenetics.smocker.model.Connection;
import com.jenetics.smocker.model.EntityWithId;
import com.jenetics.smocker.model.JavaApplication;
import com.jenetics.smocker.ui.SmockerUI;
import com.jenetics.smocker.ui.SmockerUI.EnumButton;
import com.jenetics.smocker.ui.util.ButtonWithId;
import com.jenetics.smocker.ui.util.EventManager;
import com.jenetics.smocker.ui.util.RefreshableView;
import com.jenetics.smocker.ui.util.VerticalSplitWithButton;
import com.vaadin.addon.jpacontainer.JPAContainer;
import com.vaadin.addon.jpacontainer.JPAContainerFactory;
import com.vaadin.annotations.Push;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.server.ThemeResource;
import com.vaadin.shared.Position;
import com.vaadin.spring.annotation.ViewScope;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Table;
import com.vaadin.ui.TreeTable;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;

@Push
@ViewScope
@ContentView(sortingOrder=1, viewName = "Java Applications", icon = "icons/Java-icon.png", homeView=true, rootViewParent=ConnectionsRoot.class)
public class JavaApplicationsView extends VerticalLayout implements RefreshableView {


	private static final int PORT_ARRAY_LOC = 2;

	//@Inject
	//private static ResourceBundle bundle;
	private static ResourceBundle bundle = ResourceBundle.getBundle("BundleUI");

	private static final String CONNECTION_TYPE = bundle.getString("ConnectionType");

	private static final String PORT = bundle.getString("Port");

	private static final String ADRESS = bundle.getString("Adress");

	private static final String APPLICATION = bundle.getString("Application");

	protected IDaoManager<Connection> daoManagerConnection = null;
	protected IDaoManager<JavaApplication> daoManagerJavaApplication = null;

	@Inject
	private Logger logger;

	protected static final String NAME_PROPERTY = "Name";
	protected static final String HOURS_PROPERTY = "Hours done";
	protected static final String MODIFIED_PROPERTY = "Last Modified";


	private TreeTable treetable= null;
	private JPAContainer<JavaApplication> jpaJavaApplication;
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public JavaApplicationsView() {
		super();
		VerticalLayout mainLayout = new VerticalLayout();
		
		jpaJavaApplication = JPAContainerFactory.make(JavaApplication.class, SmockerUI.getEm());
		daoManagerConnection = new DaoManager<Connection>(Connection.class, SmockerUI.getEm()) ;
		daoManagerJavaApplication = new DaoManager<JavaApplication>(JavaApplication.class, SmockerUI.getEm()) ;

		mainLayout.setMargin(true);
		treetable = new TreeTable();
		treetable.setSelectable(true);
		treetable.addContainerProperty(APPLICATION, String.class, "");
		treetable.addContainerProperty(ADRESS, String.class, "");
		treetable.addContainerProperty(PORT, String.class, "");
		treetable.addContainerProperty(CONNECTION_TYPE, String.class, "");


		treetable.addGeneratedColumn("Watch", new Table.ColumnGenerator() {
			public Component generateCell(Table source, Object itemId,  Object columnId) {
				if (!treetable.getItem(itemId).getItemProperty(ADRESS).getValue().toString().isEmpty() &&
						!treetable.getItem(itemId).getItemProperty(PORT).getValue().toString().isEmpty()) {
					String UiId = treetable.getItem(itemId).getItemProperty(ADRESS).getValue().toString() + 
							treetable.getItem(itemId).getItemProperty(PORT).getValue().toString();
					Button button = buttonByUiId.get(UiId);
					
					if (button != null && button.getListeners(ClickEvent.class).isEmpty()) {
						button.addClickListener(new ClickListener() {
							@Override
							public void buttonClick(ClickEvent event) {
								ButtonWithId<Connection> buttonWithId = (ButtonWithId<Connection>)event.getSource();
								if (buttonWithId.getEntity().getWatched() == null || !buttonWithId.getEntity().getWatched()) {
									buttonWithId.setCaption(bundle.getString("UnWatch_Button"));
									buttonWithId.getEntity().setWatched(true);
									daoManagerConnection.update(buttonWithId.getEntity());
								}
								else {
									buttonWithId.setCaption(bundle.getString("Watch_Button"));
									buttonWithId.getEntity().setWatched(false);
									daoManagerConnection.update(buttonWithId.getEntity());
								}
							}
						});
					} 
					return button;
				}
				return null;
			}
		});


		fillTreeTable();
		treetable.setSizeFull();
		addComponent(treetable);
		setSizeFull();
	}

	private Object rootTreeItem = null;

	private void fillTreeTable() {
		//treetable.clear();
		treetable.removeAllItems();
		jpaJavaApplication = JPAContainerFactory.make(JavaApplication.class, SmockerUI.getEm());
		//treetable.getContainerDataSource().removeAllItems();
		Collection<Object> itemIds = jpaJavaApplication.getItemIds();

		Object[] root = new Object[] { "all", "", "", "" };
		rootTreeItem = treetable.addItem(root, null);
		for (Object id : itemIds) {
			JavaApplication javaApplication = jpaJavaApplication.getItem(id).getEntity();
			Object javaApplicationTreeItem = buildJavaApplicationTreeItem(javaApplication);
			Set<Connection> connections = javaApplication.getConnections();

			for (Connection connection : connections) {
				buildConnectionTreeItem(connection);
			}
			if (connections.size() == 0) {
				treetable.setChildrenAllowed(javaApplicationTreeItem, false);
			}
		}
	}

	private Map<Object ,Object > applicationItemById = new HashMap<>();
	private Map<String ,ButtonWithId> buttonByUiId = new HashMap<>();

	/**
	 * Update the tree add new items (JavaConnection or Connection) 
	 * @param entityWithId
	 */
	private void updateTree(EntityWithId entityWithId) {
		if (entityWithId instanceof JavaApplication) {
			JavaApplication javaApplication = (JavaApplication) entityWithId;
			buildJavaApplicationTreeItem(javaApplication);
		}
		else if (entityWithId instanceof Connection)
		{
			Connection conn = (Connection) entityWithId;
			buildConnectionTreeItem(conn);

		}
	}


	private void buildConnectionTreeItem(Connection conn) {

		Object javaApplicationTreeItem = applicationItemById.get(conn.getJavaApplication().getId());
		if (javaApplicationTreeItem != null) {
			
			String buttonString = null;
			if (conn.getWatched() == null || conn.getWatched()) {
				buttonString = bundle.getString("Watch_Button");
			}
			else {
				buttonString = bundle.getString("UnWatch_Button");
			}
			
			ButtonWithId<Connection> buttonWithId = new ButtonWithId<Connection>(conn.getHost() + conn.getPort().toString(), conn);
			buttonWithId.setCaption(buttonString);
			buttonWithId.setIcon(FontAwesome.GLOBE);
			buttonByUiId.put(buttonWithId.getUiId(), buttonWithId);
			
			Object[] itemConnection = new Object[] { conn.getJavaApplication().getClassQualifiedName(),  conn.getHost(), conn.getPort().toString(), ""};
			Object connectionTreeItem = treetable.addItem(itemConnection, null);
			treetable.setChildrenAllowed(javaApplicationTreeItem, true);
			treetable.setParent(connectionTreeItem, javaApplicationTreeItem);
			treetable.setChildrenAllowed(connectionTreeItem, false);
		}
		else {
			logger.warn("Unable to find javaApplicationTreeItem");
		}
	}


	private Object buildJavaApplicationTreeItem(JavaApplication javaApplication) {
		Object[] javaApplicationItem = new Object[] { javaApplication.getClassQualifiedName(),  "", "", "" };
		Object javaApplicationTreeItem = treetable.addItem(javaApplicationItem, null);
		treetable.setParent(javaApplicationTreeItem, rootTreeItem);
		treetable.setChildrenAllowed(javaApplicationTreeItem, false);
		applicationItemById.put(javaApplication.getId(), javaApplicationTreeItem);
		return javaApplicationTreeItem;
	}


	@Override
	public void enter(ViewChangeEvent event) {

	}

	@Override
	public void refresh(EntityWithId entityWithId) {
		Notification notif = new Notification(
				"Warning",
				"Area of reindeer husbandry",
				Notification.TYPE_WARNING_MESSAGE);

		// Customize it
		notif.setDelayMsec(100);
		notif.setPosition(Position.BOTTOM_RIGHT);
		notif.setIcon(FontAwesome.SPINNER);

		// Show it in the page
		notif.show(Page.getCurrent());

		treetable.setEnabled(true);

		jpaJavaApplication.refreshItem(entityWithId.getId());
		updateTree(entityWithId);
	}


	@Override
	public ClickListener getClickListener(String key) {
		// TODO Auto-generated method stub
		if (key.equals(EnumButton.CLEAN_ALL.toString())) {
			return new ClickListener() {
				@Override
				public void buttonClick(ClickEvent event) {
					daoManagerJavaApplication.deleteAll();
					fillTreeTable();
				}
			};
		}
		return null;
		
	}
}
