package com.jenetics.smocker.ui.view;

import java.util.Collection;
import java.util.Set;

import com.jenetics.smocker.annotation.ContentView;
import com.jenetics.smocker.model.Connection;
import com.jenetics.smocker.model.EntityWithId;
import com.jenetics.smocker.model.JavaApplication;
import com.jenetics.smocker.ui.SmockerUI;
import com.jenetics.smocker.ui.util.EventManager;
import com.jenetics.smocker.ui.util.RefreshableView;
import com.vaadin.addon.jpacontainer.JPAContainer;
import com.vaadin.addon.jpacontainer.JPAContainerFactory;
import com.vaadin.annotations.Push;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.spring.annotation.ViewScope;
import com.vaadin.ui.TreeTable;
import com.vaadin.ui.VerticalLayout;

@Push
@ViewScope
@ContentView(sortingOrder=1, viewName = "Java Applications", icon = "icons/Java-icon.png", accordeonParent=EventManager.CONNECTIONS)
public class JavaApplicationsView extends VerticalLayout implements RefreshableView {
	
	
	protected static final String NAME_PROPERTY = "Name";
    protected static final String HOURS_PROPERTY = "Hours done";
    protected static final String MODIFIED_PROPERTY = "Last Modified";
    
    //@Inject
	//protected IDaoManager<JavaApplication> daoManager;
    //protected IDaoManager<JavaApplication> daoManager = new DaoManager<JavaApplication>(JavaApplication.class);
    
    private TreeTable treetable= null;
	private JPAContainer<JavaApplication> jpaJavaApplication;
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public JavaApplicationsView() {
		//daoManager.setEm(Persistence.createEntityManagerFactory(SmockerUI.PERSISTENCE_UNIT));
		
		jpaJavaApplication = JPAContainerFactory.make(JavaApplication.class, SmockerUI.PERSISTENCE_UNIT);
		setMargin(true);
		
		treetable = new TreeTable();
		treetable.setSelectable(true);
		//treetable.addContainerProperty("Root", String.class, "");
		treetable.addContainerProperty("Application", String.class, "");
		//treetable.addContainerProperty("Id", Long.class, "");
		treetable.addContainerProperty("Adress", String.class, "");
		treetable.addContainerProperty("Port", String.class, "");
		treetable.addContainerProperty("ConnectionType", String.class, "");
		treetable.setSizeFull();
		fillTreeTable();
		addComponent(treetable);
        setSizeFull();
	}


	private void fillTreeTable() {
		Collection<Object> itemIds = jpaJavaApplication.getItemIds();
		
		Object[] root = new Object[] { "all", "", "", "" };
		Object rootTreeItem = treetable.addItem(root, null);
		for (Object id : itemIds) {
			JavaApplication javaApplication = jpaJavaApplication.getItem(id).getEntity();
			Object[] javaApplicationItem = new Object[] { javaApplication.getClassQualifiedName(),  "", "", "" };
			Object javaApplicationTreeItem = treetable.addItem(javaApplicationItem, null);
			treetable.setParent(javaApplicationTreeItem, rootTreeItem);
			Set<Connection> connections = javaApplication.getConnections();
			
			for (Connection connection : connections) {
				Object[] itemConnection = new Object[] { javaApplication.getClassQualifiedName(),  connection.getHost(), connection.getPort().toString(), ""};
				Object connectionTreeItem = treetable.addItem(itemConnection, null);
				treetable.setParent(connectionTreeItem, javaApplicationTreeItem);
				treetable.setChildrenAllowed(connectionTreeItem, false);
			}
			if (connections.size() == 0) {
				treetable.setChildrenAllowed(javaApplicationTreeItem, false);
			}
		}
	}


	@Override
	public void enter(ViewChangeEvent event) {
		
	}

	@Override
	public void refresh(EntityWithId entityWithId) {
		//jpaJavaApplication.refreshItem();
		treetable.removeAllItems();
		fillTreeTable();
	}
}
