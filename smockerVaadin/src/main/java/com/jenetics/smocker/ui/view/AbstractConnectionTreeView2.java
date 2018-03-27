package com.jenetics.smocker.ui.view;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.jboss.logging.Logger;
import org.vaadin.easyapp.util.EasyAppLayout;

import com.jenetics.smocker.dao.DaoManager;
import com.jenetics.smocker.dao.IDaoManager;
import com.jenetics.smocker.model.Communication;
import com.jenetics.smocker.model.Connection;
import com.jenetics.smocker.model.EntityWithId;
import com.jenetics.smocker.model.JavaApplication;
import com.jenetics.smocker.ui.SmockerUI;
import com.jenetics.smocker.ui.util.ButtonWithId;
import com.jenetics.smocker.ui.util.RefreshableView;
import com.jenetics.smocker.ui.util.TreeGridConnectionData;
import com.vaadin.addon.jpacontainer.JPAContainer;
import com.vaadin.addon.jpacontainer.JPAContainerFactory;
import com.vaadin.data.TreeData;
import com.vaadin.data.provider.TreeDataProvider;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.shared.Position;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.TreeGrid;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.VerticalSplitPanel;

public abstract class AbstractConnectionTreeView2<T extends Serializable, U extends Serializable, V extends Serializable> 
extends EasyAppLayout  {

	/**
	 * Tree Item object by javaApplication id used to find the associated UI
	 * Item from an Application id
	 */


	protected TreeGrid<TreeGridConnectionData<T, U>> treeGrid = null;

	static ResourceBundle bundle = ResourceBundle.getBundle("BundleUI");
	protected static final String CONNECTION_TYPE = bundle.getString("ConnectionType");
	protected static final String PORT = bundle.getString("Port");
	protected static final String ADRESS = bundle.getString("Adress");
	protected static final String APPLICATION = bundle.getString("Application");
	protected static final String ALL = "all";
	protected static final String SEP_CONN = ":";

	protected T selectedJavaApplication = null;
	protected U selectedConnection = null;
	protected V selectedCommunication = null;
	protected boolean allSelected;

	protected IDaoManager<T> daoManagerJavaApplication = null;
	protected IDaoManager<U> daoManagerConnection = null;

	private Class<T> tParameterClass = null;
	private Class<U> uParameterClass = null;
	private Class<V> vParameterClass = null;
	//protected Item selectedTreeItem;
	protected JPAContainer<T> jpaJavaApplication;


	protected TreeData<TreeGridConnectionData<T, U>> treeData = null;
	protected TreeDataProvider<TreeGridConnectionData<T, U>> treeDataProvider = null;

	@Inject
	private Logger logger;

	public AbstractConnectionTreeView2(Class<T> tParameterClass, Class<U> uParameterClass, Class<V> vParameterClass) {
		super();
		this.tParameterClass = tParameterClass;
		this.uParameterClass = uParameterClass;
		this.vParameterClass = vParameterClass;

		daoManagerJavaApplication = new DaoManager<>(tParameterClass, SmockerUI.getEm());
		daoManagerConnection = new DaoManager<>(uParameterClass, SmockerUI.getEm());

		initDao();

		setMargin(true);
		buildTreeTable();
		fillTreeTable();
		
		treeGrid.setSizeFull();
		addComponent(treeGrid);

		setSizeFull();
	}


	/**
	 * Update the tree
	 * 
	 * @param entityWithId
	 */
	protected void updateTree(EntityWithId entityWithId) {
		if (tParameterClass.isAssignableFrom(entityWithId.getClass())) {
			T javaApplication = (T) entityWithId;
			addJavaApplicationToTree(javaApplication);
		} else if (uParameterClass.isAssignableFrom(entityWithId.getClass())) {
			U conn = (U) entityWithId;
			addConnectionToTree(getJavaAppFromConnection(conn), conn);
		} 
		treeDataProvider.refreshAll();
	}

	protected abstract Set<U> getJavaAppConnections(T javaApplication);
	protected abstract T getJavaAppFromConnection(U connection);
	protected abstract U getConnectionFromCommunication(V comm);
	protected abstract TreeGridConnectionData<T, U> createTreeGridFromJavaApplication(T javaApplication);
	protected abstract TreeGridConnectionData<T, U> createTreeGridFromJConnection(U connection);



	protected void refreshEntity(EntityWithId entityWithId) {
		Notification notif = new Notification("Refreshing", Type.ASSISTIVE_NOTIFICATION);

		// Customize it
		notif.setDelayMsec(100);
		notif.setPosition(Position.BOTTOM_RIGHT);
		notif.setIcon(FontAwesome.SPINNER);

		// Show it in the page
		notif.show(Page.getCurrent());

		treeGrid.setEnabled(true);

		updateTree(entityWithId);
	}


	/**
	 * Rebuild the treeTable
	 */
	protected void fillTreeTable() {
		treeData.clear();
		List<T> listAllJavaApplications = daoManagerJavaApplication.listAll();
		for (T javaApplication : listAllJavaApplications) {
			addJavaApplicationToTree(javaApplication);
		}
		treeDataProvider.refreshAll();
	}
	
	Map<T, TreeGridConnectionData<T, U>> treeItemByJavaApplication = new Hashtable<T, TreeGridConnectionData<T, U>>();
	/**
	 * Add a Java application item to the tree
	 * @param javaApplication
	 */
	private void addJavaApplicationToTree(T javaApplication) {
		TreeGridConnectionData<T, U> treeGridConnectionJavaApp = createTreeGridFromJavaApplication(javaApplication);
		treeItemByJavaApplication.put(javaApplication, treeGridConnectionJavaApp);
		treeData.addItem(null, treeGridConnectionJavaApp);
		Set<U> javaAppConnections = getJavaAppConnections(javaApplication);
		for (U connection : javaAppConnections) {
			addConnectionToTree(javaApplication, connection);
		}
	}


	private void addConnectionToTree(T javaApplication, U connection) {
		TreeGridConnectionData<T, U> treeGridConnection = createTreeGridFromJConnection(connection);
		if (treeItemByJavaApplication.containsKey(javaApplication)) {
			treeData.addItem(treeItemByJavaApplication.get(javaApplication), treeGridConnection);
		}
	}

	protected void buildTreeTable() {
		treeGrid = new TreeGrid<TreeGridConnectionData<T, U>>();

		treeData = new TreeData<>();
		treeDataProvider = new TreeDataProvider<>(treeData);
		
		addTreeMapping();

		treeGrid.setDataProvider(treeDataProvider);
	}



	protected abstract void addTreeMapping();


	protected void initDao() {
		jpaJavaApplication = JPAContainerFactory.make(tParameterClass, SmockerUI.getEm());
	}


	public boolean always() {
		return true;
	}


	public void refresh(ClickEvent event) {
		fillTreeTable();
	}

}
