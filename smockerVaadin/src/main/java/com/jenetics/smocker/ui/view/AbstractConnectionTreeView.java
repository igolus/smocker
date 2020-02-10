package com.jenetics.smocker.ui.view;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.jboss.logging.Logger;
import org.vaadin.easyapp.ui.ViewWithToolBar;
import org.vaadin.easyapp.util.AnnotationScanner;
import org.vaadin.easyapp.util.EasyAppLayout;

import com.jenetics.smocker.dao.DaoManagerByModel;
import com.jenetics.smocker.dao.IDaoManager;
import com.jenetics.smocker.model.EntityWithId;
import com.jenetics.smocker.ui.SmockerUI;
import com.jenetics.smocker.ui.component.AbstractConnectionDetails;
import com.jenetics.smocker.ui.util.SearcheableView;
import com.jenetics.smocker.ui.util.TreeGridConnectionData;
import com.vaadin.addon.jpacontainer.JPAContainer;
import com.vaadin.addon.jpacontainer.JPAContainerFactory;
import com.vaadin.data.TreeData;
import com.vaadin.data.provider.TreeDataProvider;
import com.vaadin.event.selection.SelectionEvent;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.Grid.SelectionMode;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.SelectedTabChangeEvent;
import com.vaadin.ui.TabSheet.Tab;
import com.vaadin.ui.TreeGrid;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.components.grid.SingleSelectionModel;

/**
 * 
 * @author igolus 
 *
 * @param <T> Application
 * @param <U> Connection
 * @param <V> Communication
 * @param <W> Connection details view Details
 */
public abstract class AbstractConnectionTreeView<T extends EntityWithId, U extends EntityWithId, V extends Serializable, W extends AbstractConnectionDetails> 
	extends EasyAppLayout implements SearcheableView  {

	/**
	 * Tree Item object by javaApplication id used to find the associated UI
	 * Item from an Application id
	 */
	protected TreeGrid<TreeGridConnectionData<T, U>> treeGrid = null;
	
	protected final TabSheet tabSheet = new TabSheet();

	static ResourceBundle bundle = ResourceBundle.getBundle("BundleUI");
	protected static final String CONNECTION_TYPE =SmockerUI.getBundleValue("ConnectionType");
	protected static final String PORT = SmockerUI.getBundleValue("Port");
	protected static final String ID = SmockerUI.getBundleValue("IdJavaApp");
	protected static final String ADRESS = SmockerUI.getBundleValue("Adress");
	protected static final String APPLICATION = SmockerUI.getBundleValue("Application");
	protected static final String WATCH = SmockerUI.getBundleValue("Watch_Button");
	
	protected static final String CONFIGURE = SmockerUI.getBundleValue("Configure_Button");
	
	protected static final String ALL = "all";
	protected static final String SEP_CONN = ":";

	protected T selectedJavaApplication = null;
	protected U selectedConnection = null;
	protected V selectedCommunication = null;
	protected boolean allSelected;

	protected transient IDaoManager<T> daoManagerJavaApplication = null;
	protected transient IDaoManager<U> daoManagerConnection = null;

	private Class<T> tParameterClass = null;
	private Class<U> uParameterClass = null;
	
	protected JPAContainer<T> jpaJavaApplication;


	protected TreeData<TreeGridConnectionData<T, U>> treeData = null;
	protected TreeDataProvider<TreeGridConnectionData<T, U>> treeDataProvider = null;
	
	protected transient Map<String, Tab> tabByConnectionKey = new HashMap<>();
	
	protected transient  Map<Tab, W> detailsViewByTab = new HashMap<>();
	protected transient Map<U, Tab> tabByConnection = new HashMap<>();
	
	protected W selectedDetailView = null;

	@Inject
	private Logger logger;

	private VerticalLayout tabmainlayout;

	public AbstractConnectionTreeView(Class<T> tParameterClass, Class<U> uParameterClass) {
		super();
		this.tParameterClass = tParameterClass;
		this.uParameterClass = uParameterClass;
		
		
		daoManagerJavaApplication = DaoManagerByModel.getDaoManager(tParameterClass);
		daoManagerConnection = DaoManagerByModel.getDaoManager(uParameterClass);

		initDao();

		setMargin(true);
		buildTreeTable();
		fillTreeTable();

		setSizeFull();
		addComponent(getInnerComponent());
		
		treeGrid.addSelectionListener(this::treeChanged);
		((SingleSelectionModel) treeGrid.getSelectionModel()).setDeselectAllowed(false);
		treeGrid.setStyleName("NoSelect");
		treeGrid.setSizeFull();
		setSizeFull();
	}
	
	protected W getSelectedDetailView() {
		return selectedDetailView;
	}
	
	/**
	 * return the main component to add
	 * @return
	 */
	protected Component getInnerComponent() {
		treeGrid.setSizeFull();
		tabmainlayout = new VerticalLayout();
		tabmainlayout.setSizeFull();
		tabmainlayout.setCaption(SmockerUI.getBundleValue("MainTab"));
		
		treeGrid.getColumns().stream().forEach(c -> {
			c.setWidthUndefined();
			c.setResizable(false);
		});
		
		tabmainlayout.addComponent(treeGrid);
		tabSheet.setSizeFull();
		tabSheet.addTab(tabmainlayout);
		tabSheet.addSelectedTabChangeListener(this::tabChanged);
		tabSheet.setCloseHandler(this::tabClosed);
		return tabSheet;
	}
	
	public boolean isMainTabSelected () {
		return tabSheet.getSelectedTab() == tabmainlayout;
	}
	
	public Component getSelectedTab () {
		return tabSheet.getSelectedTab();
	}
	
	public void tabChanged(SelectedTabChangeEvent event) {
		SmockerUI.getInstance().checkEnableSearch();
		ViewWithToolBar currentView = AnnotationScanner.getViewWithToolBarFromLayout(this);
		//detailsViewByTab = detailsViewByTab.get(event.getSource())
		Component selectedTab = tabSheet.getSelectedTab();
		if (selectedTab != tabmainlayout ) {
			selectedDetailView = (W) selectedTab;
		}
		else {
			selectedDetailView = null;
		}
		if (currentView != null) {
			currentView.refreshViewToolBar();
		}
		refreshClickable();
	}
	
	public void treeChanged(SelectionEvent<TreeGridConnectionData<T, U>> event) {
		refreshClickable();
	}
	
	public void details(ClickEvent event) {
		if (isConnectionSelected()) {
			U conn = treeGrid.getSelectedItems().iterator().next().getConnection();
			W connectionWithDetail = getConnectionDetailsLayout(conn);
			selectedDetailView = connectionWithDetail;
			String connectionKey = getConnectionKey(conn);
			Tab tabForConnection = tabByConnectionKey.get(connectionKey);
			if (tabForConnection != null) {
				tabSheet.setSelectedTab(tabForConnection);
			}
			else {
				tabForConnection = addTabConnectionDetails(connectionWithDetail, conn);
				detailsViewByTab.put(tabForConnection, connectionWithDetail);
				tabByConnection.put(conn, tabForConnection);
			}
			selectedDetailView = detailsViewByTab.get(tabForConnection);
		}
	}
	
	protected abstract String getConnectionKey(U conn);

	protected abstract W getConnectionDetailsLayout(U conn);

	protected Tab addTabConnectionDetails (EasyAppLayout connectionWithDetail, U conn) {
		connectionWithDetail.setSizeFull();
		Tab connectionWithDetailTab = tabSheet.addTab(connectionWithDetail);
		
		connectionWithDetailTab.setClosable(true);
		String connectionKey = getConnectionKey(conn);
		connectionWithDetailTab.setCaption(connectionKey);
		tabByConnectionKey.put(connectionKey, connectionWithDetailTab);
		
		removeAllComponents();
        addComponent(tabSheet);
        tabSheet.setSelectedTab(connectionWithDetailTab);
        
        return connectionWithDetailTab;
	}
	
	protected abstract boolean isConnectionSelected();

	public void tabClosed(TabSheet tabsheet, Component tabContent) {
		Tab tab = tabsheet.getTab(tabContent);
		Map<Tab, String> connectionByTab = 
				tabByConnectionKey.entrySet()
				.stream()
				.collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));

		String connKey = connectionByTab.get(tab);
		tabByConnectionKey.remove(connKey);
		detailsViewByTab.remove(tab);

		tabsheet.removeTab(tab);
		refreshClickable();
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
		treeGrid.setEnabled(true);
		treeGrid.setSelectionMode(SelectionMode.SINGLE);
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
	
	private transient Map<T, TreeGridConnectionData<T, U>> treeItemByJavaApplication = new HashMap<>();
	/**
	 * Add a Java application item to the tree
	 * @param javaApplication
	 */
	protected void addJavaApplicationToTree(T javaApplication) {
		TreeGridConnectionData<T, U> treeGridConnectionJavaApp = createTreeGridFromJavaApplication(javaApplication);
		treeItemByJavaApplication.put(javaApplication, treeGridConnectionJavaApp);
		treeData.addItem(null, treeGridConnectionJavaApp);
		Set<U> javaAppConnections = getJavaAppConnections(javaApplication);
		for (U connection : javaAppConnections) {
			addConnectionToTree(javaApplication, connection);
		}
	}


	protected void addConnectionToTree(T javaApplication, U connection) {
		TreeGridConnectionData<T, U> treeGridConnection = createTreeGridFromJConnection(connection);
		if (javaApplication != null && treeItemByJavaApplication.containsKey(javaApplication)) {
			treeData.addItem(treeItemByJavaApplication.get(javaApplication), treeGridConnection);
		}
		else {
			treeData.addItem(null, treeGridConnection);
		}
	}

	protected void buildTreeTable() {
		treeGrid = new TreeGrid<>();
		treeGrid.addItemClickListener( e -> {
			if (e.getMouseEventDetails().isDoubleClick()) {
				details(null);
			}
		});
		
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
		if (isMainTabSelected()) {
			fillTreeTable();
		}
		else {
			getSelectedDetailView().refresh();
		}
	}
	
	@Override
	public void enterInView(ViewChangeEvent event) {
		SmockerUI.getInstance().checkEnableSearch();
	}
	
	
	@Override
	public boolean canSearch() {
		return !isMainTabSelected();
	}
}
