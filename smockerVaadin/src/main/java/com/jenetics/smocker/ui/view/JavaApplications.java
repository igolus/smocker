package com.jenetics.smocker.ui.view;

import java.util.Calendar;
import java.util.Date;

import com.jenetics.smocker.annotation.ContentView;
import com.jenetics.smocker.model.Connection;
import com.jenetics.smocker.ui.SmockerUI;
import com.jenetics.smocker.ui.util.EventManager;
import com.jenetics.smocker.ui.util.RefreshableView;
import com.vaadin.addon.jpacontainer.JPAContainer;
import com.vaadin.addon.jpacontainer.JPAContainerFactory;
import com.vaadin.annotations.Push;
import com.vaadin.data.Item;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.spring.annotation.ViewScope;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.Table;
import com.vaadin.ui.Tree;
import com.vaadin.ui.TreeTable;
import com.vaadin.ui.VerticalLayout;

@Push
@ViewScope
@ContentView(sortingOrder=1, viewName = "Java Applications", icon = "icons/Java-icon.png", accordeonParent=EventManager.CONNECTIONS)
public class JavaApplications extends VerticalLayout implements RefreshableView {
	
	
	protected static final String NAME_PROPERTY = "Name";
    protected static final String HOURS_PROPERTY = "Hours done";
    protected static final String MODIFIED_PROPERTY = "Last Modified";
    
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public JavaApplications() {

		setMargin(true);
		
//		applications = JPAContainerFactory.make(JavaApplications.class, SmockerUI.PERSISTENCE_UNIT);
//		applicationsTable = new Table(null, applications);
//		applicationsTable = new Table();
//		applicationsTable.setSelectable(true);
//		applicationsTable.setSizeFull();
//		applicationsTable.setImmediate(true);
//		
//		applicationsTable.addGeneratedColumn("Watch", new Table.ColumnGenerator() {
//            public Component generateCell(Table source, Object itemId,
//                    Object columnId) {
//                //Item item = connectionTable.getItem(itemId);
//                Button button = new Button("Watch");
//                return button;
//            }
//        });
//		setSizeFull();
//		addComponent(applicationsTable);
//		setImmediate(true);
		
		Calendar cal = Calendar.getInstance();
        cal.set(2011, 10, 30, 14, 40, 26);

        // Create the treetable
        TreeTable treetable = new TreeTable();
        treetable.setWidth("100%");

        addComponent(treetable);

        // Add Table columns
        treetable.addContainerProperty(NAME_PROPERTY, String.class, "");
        treetable.addContainerProperty(HOURS_PROPERTY, Integer.class, 0);
        treetable.addContainerProperty(MODIFIED_PROPERTY, Date.class,
                cal.getTime());

        // Populate table
        Object allProjects = treetable.addItem(new Object[] { "All Projects",
                18, cal.getTime() }, null);
        Object year2010 = treetable.addItem(
                new Object[] { "Year 2010", 18, cal.getTime() }, null);
        Object customerProject1 = treetable.addItem(new Object[] {
                "Customer Project 1", 13, cal.getTime() }, null);
        Object customerProject1Implementation = treetable.addItem(new Object[] {
                "Implementation", 5, cal.getTime() }, null);
        Object customerProject1Planning = treetable.addItem(new Object[] {
                "Planning", 2, cal.getTime() }, null);
        Object customerProject1Prototype = treetable.addItem(new Object[] {
                "Prototype", 5, cal.getTime() }, null);
        Object customerProject2 = treetable.addItem(new Object[] {
                "Customer Project 2", 5, cal.getTime() }, null);
        Object customerProject2Planning = treetable.addItem(new Object[] {
                "Planning", 5, cal.getTime() }, null);

        // Set hierarchy
        treetable.setParent(year2010, allProjects);
        treetable.setParent(customerProject1, year2010);
        treetable.setParent(customerProject1Implementation, customerProject1);
        treetable.setParent(customerProject1Planning, customerProject1);
        treetable.setParent(customerProject1Prototype, customerProject1);
        treetable.setParent(customerProject2, year2010);
        treetable.setParent(customerProject2Planning, customerProject2);

        // Disallow children from leaves
        treetable.setChildrenAllowed(customerProject1Implementation, false);
        treetable.setChildrenAllowed(customerProject1Planning, false);
        treetable.setChildrenAllowed(customerProject1Prototype, false);
        treetable.setChildrenAllowed(customerProject2Planning, false);

        // Expand all
        treetable.setCollapsed(allProjects, false);
        treetable.setCollapsed(year2010, false);
        treetable.setCollapsed(customerProject1, false);
        treetable.setCollapsed(customerProject2, false);
        addComponent(treetable);
        
        
        Tree tree = new Tree("My Tree");
        
        // Create the tree nodes
        tree.addItem("UI");
        tree.addItem("Branch 1");
        tree.addItem("Branch 2");
        tree.addItem("Leaf 1");
        tree.addItem("Leaf 2");
        tree.addItem("Leaf 3");
        tree.addItem("Leaf 4");
        
        // Set the hierarchy
        tree.setParent("Branch 1", "UI");
        tree.setParent("Branch 2", "UI");
        tree.setParent("Leaf 1", "Branch 1");
        tree.setParent("Leaf 2", "Branch 1");
        tree.setParent("Leaf 3", "Branch 2");
        tree.setParent("Leaf 4", "Branch 2");

        // Disallow children for leaves
        tree.setChildrenAllowed("Leaf 1", false);
        tree.setChildrenAllowed("Leaf 2", false);
        tree.setChildrenAllowed("Leaf 3", false);
        tree.setChildrenAllowed("Leaf 4", false);
        
        addComponent(tree);
	}

	JPAContainer<JavaApplications> applications = null;
	private Table applicationsTable;

	@Override
	public void enter(ViewChangeEvent event) {

	}

	@Override
	public void refresh() {
		applications.refresh();
	}
}
