package com.jenetics.smocker.ui.view;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.jboss.logging.Logger;

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
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.TreeGrid;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.VerticalSplitPanel;

public abstract class AbstractConnectionTreeView<T extends Serializable, U extends Serializable, V extends Serializable> 
	extends VerticalSplitPanel implements RefreshableView {
	
	/**
	 * Tree Item object by javaApplication id used to find the associated UI
	 * Item from an Application id
	 */
	protected transient Map<Long, Object> applicationItemById = new HashMap<>();
	protected transient Map<String, ButtonWithId> buttonByUiId = new HashMap<>();
	protected transient Map<String, Long> applicationIdIByAdressAndPort = new HashMap<>();
	protected transient Map<String, Long> applicationIdIByApplicationClass = new HashMap<>();
	protected transient Map<Long, Object> connectionTreeItemByConnectionId = new HashMap<>();
	protected transient Object rootTreeItem;
	

	protected TreeGrid<TreeGridConnectionData<T, U>> treeGrid = null;
	protected VerticalLayout second;
	
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

	public AbstractConnectionTreeView(Class<T> tParameterClass, Class<U> uParameterClass, Class<V> vParameterClass) {
		super();
		this.tParameterClass = tParameterClass;
		this.uParameterClass = uParameterClass;
		this.vParameterClass = vParameterClass;
		
		daoManagerJavaApplication = new DaoManager<>(tParameterClass, SmockerUI.getEm());
		daoManagerConnection = new DaoManager<>(uParameterClass, SmockerUI.getEm());
		
		VerticalLayout mainLayout = new VerticalLayout();
		initDao();

		mainLayout.setMargin(true);
		buildTreeTable();
		fillTreeTable();
		treeGrid.setWidth("100%");
		treeGrid.setHeight("40%");

		treeGrid.setSizeFull();
		setFirstComponent(treeGrid);
		setSplitPosition(150, Unit.PIXELS);

		setSizeFull();
		checkToolBar();
	}
	
	protected abstract Map<String, Class<?>> getColumnMap();
	
	protected abstract void addColumnToTreeTable();
	
	/**
	 * Update the tree
	 * 
	 * @param entityWithId
	 */
	protected void updateTree(EntityWithId entityWithId) {
		if (tParameterClass.isAssignableFrom(entityWithId.getClass())) {
			T javaApplication = (T) entityWithId;
			fillJavaApplicationTreeItem(javaApplication, false);
		} else if (uParameterClass.isAssignableFrom(entityWithId.getClass())) {
			U conn = (U) entityWithId;
			addConnectionItemToTreeTable(getJavaAppFromConnection(conn), conn);
		} else if (vParameterClass.isAssignableFrom(entityWithId.getClass())) {
			V comm = (V) entityWithId;
			fillCommunications( getConnectionFromCommunication( comm), true);
		}
	}

	
	protected void refreshEntity(EntityWithId entityWithId) {
		jpaJavaApplication.refreshItem(entityWithId.getId());
	}
	
	
	/**
	 * Rebuild the treeTable
	 */
	protected void fillTreeTable() {
		clearAssociationMaps();

		//data.clear();
		//treeGrid.removeAllItems();
		//treeGrid.getDataProvider().get
		second.removeAllComponents();

		List<T> listAllJavaApplications = daoManagerJavaApplication.listAll();

		Object[] root = new Object[] { ALL, "", "", "" };
		//this.rootTreeItem = treeGrid.addItem(root, null);

		for (T javaApplication : listAllJavaApplications) {
			fillJavaApplicationTreeItem(javaApplication, true);
		}
	}
	
	/**
	 * Rebuild the UI for the JavaApplications
	 * 
	 * @param javaApplication
	 * @param rebuild
	 *            true when the Full UI is redisplayed
	 */
	protected void fillJavaApplicationTreeItem(T javaApplication, boolean rebuild) {

		if (applicationItemById.get(getJavaAppId(javaApplication)) == null || rebuild) {
			createJavaApplicationItem(getJavaAppClassQualifiedName(javaApplication), getJavaAppId(javaApplication));
		}
		Set<U> connections = getJavaAppConnections(javaApplication);
		rebuildConnectionsTreeItem(connections, javaApplication);

		if (connections.isEmpty()) {
			//treeGrid.setChildrenAllowed(applicationItemById.get(getJavaAppId(javaApplication)), false);
		}
	}
	
	/**
	 * Add a connection item to the tree table
	 * @param javaApplication
	 * @param connection
	 */
	protected void addConnectionItemToTreeTable(T javaApplication, U connection) {

		Object javaApplicationTreeItem = applicationItemById.get(getJavaAppId(javaApplication));
		if (javaApplicationTreeItem != null) {
			manageSpecialUIBehaviourInJavaApplication(connection);
			Object[] itemConnection = new Object[] { getJavaAppClassQualifiedName(javaApplication), getConnectionHost(connection),
					getConnectionPort(connection).toString(), "" };
			//Object connectionTreeItem = treeGrid.addItem(itemConnection, null);
			//connectionTreeItemByConnectionId.remove(getConnectionId(connection));
			//connectionTreeItemByConnectionId.put(getConnectionId(connection), connectionTreeItem);

			//treeGrid.setChildrenAllowed(javaApplicationTreeItem, true);
			//treeGrid.setParent(connectionTreeItem, javaApplicationTreeItem);
			//treeGrid.setChildrenAllowed(connectionTreeItem, false);

			applicationIdIByAdressAndPort.remove(getConnectionHost(connection) + SEP_CONN + getConnectionHost(connection));
			applicationIdIByAdressAndPort.put(getConnectionHost(connection) + SEP_CONN + getConnectionPort(connection), 
					getJavaAppId(getJavaAppFromConnection(connection)));
			fillCommunications(connection, true);
		} else {
			logger.warn("Unable to find javaApplicationTreeItem");
		}
	}
	
	private void rebuildConnectionsTreeItem(Set<U> connections, T javaApplication) {

		Object applicationTreeItemId = applicationItemById.get(getJavaAppId(javaApplication));
		// remove all the items
//		if (treeGrid.getChildren(applicationTreeItemId) != null) {
//			for (Object child : new HashSet<Object>(treeGrid.getChildren(applicationTreeItemId))) {
//				treeGrid.removeItem(child);
//				connectionTreeItemByConnectionId.values().remove(child);
//			}
//		}

		for (Iterator iterator = connections.iterator(); iterator.hasNext();) {
			U connection = (U) iterator.next();
			addConnectionItemToTreeTable(javaApplication, connection);
		}
	}
	
	protected abstract Long getJavaAppId(T javaApplication);
	protected abstract Long getConnectionId(U connection);
	protected abstract String getJavaAppClassQualifiedName(T javaApplication);
	protected abstract Set<U> getJavaAppConnections(T javaApplication);
	protected abstract void manageSpecialUIBehaviourInJavaApplication(U connection);
	protected abstract String getConnectionHost(U connection);
	protected abstract Integer getConnectionPort(U connection);
	protected abstract T getJavaAppFromConnection(U connection);
	protected abstract U getConnectionFromCommunication(V comm);



	/**
	 * Clear all the reference maps
	 */
	public void clearAssociationMaps() {
		applicationItemById.clear();
		buttonByUiId.clear();
		applicationIdIByAdressAndPort.clear();
		applicationIdIByApplicationClass.clear();
		connectionTreeItemByConnectionId.clear();
	}

	protected void buildTreeTable() {
		treeGrid = new TreeGrid<TreeGridConnectionData<T, U>>();
		
		treeData = new TreeData<>();
		treeDataProvider = new TreeDataProvider<>(treeData);
		
		treeGrid.addColumn(item -> item.getApplication()).setCaption("APP");
		treeGrid.addColumn(item -> item.getAdress()).setCaption("ADD");
		treeGrid.addColumn(item -> item.getPort()).setCaption("PORT");

		
		treeGrid.setDataProvider(treeDataProvider);
		
//		for (Map.Entry<String, Class<?>> entry : getColumnMap().entrySet()) {
//			treeGrid.addContainerProperty(entry.getKey(), entry.getValue(), "");
//		}
//		treeGrid.setSelectable(true);
//		addColumnToTreeTable();
//		treeGrid.addItemClickListener(this::treeTableItemClicked);
	}

	protected VerticalLayout buildSecondArea() {
		second = new VerticalLayout();
		second.setSizeFull();
		return second;
	}

	protected void checkToolBar() {
//		if (SmockerUI.getInstance() != null) {
//			SmockerUI.getInstance().getEasyAppMainView().getToolBar().checkClickable(this);
//		}
	}

	@Override
	public void refresh(EntityWithId entityWithId) {
		Notification notif = new Notification("Refreshing", Type.ASSISTIVE_NOTIFICATION);

		// Customize it
		notif.setDelayMsec(100);
		notif.setPosition(Position.BOTTOM_RIGHT);
		notif.setIcon(FontAwesome.SPINNER);

		// Show it in the page
		notif.show(Page.getCurrent());

		treeGrid.setEnabled(true);

		refreshEntity(entityWithId);
		updateTree(entityWithId);
	}

	/**
	 * Add Java Application to tree view
	 * 
	 * @param className
	 * @param javaApplicationId
	 */
	protected void createJavaApplicationItem(String className, Long javaApplicationId) {
		Object[] javaApplicationItem = new Object[] { className, "", "", "" };
		//Object javaApplicationTreeItem = treeGrid.addItem(javaApplicationItem, null);
		//treeGrid.setParent(javaApplicationTreeItem, rootTreeItem);
		//treeGrid.setChildrenAllowed(javaApplicationTreeItem, false);
		//applicationItemById.put(javaApplicationId, javaApplicationTreeItem);
		applicationIdIByApplicationClass.put(className, javaApplicationId);
	}

	protected void setSelection(T javaApplication, U connection, boolean allSelected) {
		selectedJavaApplication = javaApplication;
		selectedConnection = connection;
		this.allSelected = allSelected;
		checkToolBar();
	}

//	protected void treeTableItemClicked(ItemClickEvent itemClickEvent) {
//		selectedTreeItem = itemClickEvent.getItem();
//		checkToolBar();
//	
//		if (!StringUtils.isEmpty(itemClickEvent.getItem().getItemProperty(ADRESS).toString())
//				&& !StringUtils.isEmpty(itemClickEvent.getItem().getItemProperty(PORT).toString())) {
//			String host = itemClickEvent.getItem().getItemProperty(ADRESS).toString();
//			String port = itemClickEvent.getItem().getItemProperty(PORT).toString();
//			String key = host + SEP_CONN + port;
//			Long appId = applicationIdIByAdressAndPort.get(key);
//			if (appId != null) {
//				refreshCommunications(host, port, appId);
//			}
//		} else {
//			String application = itemClickEvent.getItem().getItemProperty(APPLICATION).toString();
//			if (application.equals(ALL)) {
//				setSelection(null, null, true);
//			} else if (!StringUtils.isEmpty(application)) {
//				long selectedApplicationId = applicationIdIByApplicationClass.get(application);
//				T selectedApplication = jpaJavaApplication.getItem(selectedApplicationId).getEntity();
//				setSelection(selectedApplication, null, false);
//			}
//		}
//		checkToolBar();
//	}

	/**
	 * refresh the communications
	 * @param host
	 * @param port
	 * @param appId
	 */
	void refreshCommunications(String host, String port, Long appId) {
		jpaJavaApplication.refresh();
		T javaApplication = jpaJavaApplication.getItem(appId).getEntity();
		U connection = getCorrespondingConnection(host, port, javaApplication);
		
		if (connection != null) {
			setSelection(null, connection, false);
			fillCommunications(connection, false);
		}
	}
	
	protected abstract void fillCommunications(U conn, boolean checkSelected);
	
	/**
	 * get the corresponding connection
	 * @param host
	 * @param port
	 * @return
	 */
	protected abstract U getCorrespondingConnection(String host, String port, T javaApplication);

	protected void initDao() {
		jpaJavaApplication = JPAContainerFactory.make(tParameterClass, SmockerUI.getEm());
	}

}
