package com.jenetics.smocker.ui.view;

import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import com.jenetics.smocker.model.EntityWithId;
import com.jenetics.smocker.ui.SmockerUI;
import com.jenetics.smocker.ui.util.ButtonWithId;
import com.jenetics.smocker.ui.util.RefreshableView;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.server.Sizeable.Unit;
import com.vaadin.shared.Position;
import com.vaadin.ui.Component;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TreeTable;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.VerticalSplitPanel;
import com.vaadin.ui.Notification.Type;

public abstract class AbstractConnectionTreeView extends VerticalSplitPanel implements RefreshableView {
	

	static ResourceBundle bundle = ResourceBundle.getBundle("BundleUI");
	protected static final String CONNECTION_TYPE = bundle.getString("ConnectionType");
	protected static final String PORT = bundle.getString("Port");
	protected static final String ADRESS = bundle.getString("Adress");
	protected static final String APPLICATION = bundle.getString("Application");
	
	
	protected TreeTable treetable = null;
	protected VerticalLayout second;

	protected abstract void initDao();
	protected abstract Map<String, Class<?>> getColumnMap();
	
	protected transient Map<Long ,Object > applicationItemById = new HashMap<>();
	protected transient Map<String ,ButtonWithId> buttonByUiId =  new HashMap<>();
	protected transient Map<String ,Long > applicationIdIByAdressAndPort =  new HashMap<>();
	protected transient Map<String ,Long > applicationIdIByApplicationClass =  new HashMap<>();
	protected transient Map<Long, Object> connectionTreeItemByConnectionId = new HashMap<>();

	public AbstractConnectionTreeView() {
		super();
		VerticalLayout mainLayout = new VerticalLayout();
		initDao();

		mainLayout.setMargin(true);
		buildTreeTable();
		buildSecondArea();
		fillTreeTable();
		treetable.setWidth("100%");
		treetable.setHeight("40%");


		treetable.setSizeFull();
		setFirstComponent(treetable);
		setSecondComponent(second);
		setSplitPosition(150, Unit.PIXELS);

		setSizeFull();
		checkToolBar();
	}

	protected void buildTreeTable() {
		treetable = new TreeTable();
		for (Map.Entry<String, Class<?>> entry : getColumnMap().entrySet()) {
			treetable.addContainerProperty(entry.getKey(), entry.getValue(), "");
		}
		treetable.setSelectable(true);
		addColumnToTreeTable();
		treetable.addItemClickListener(this::treeTableItemClicked);
	}
	
	protected abstract void addColumnToTreeTable();
	protected abstract void fillTreeTable();
	protected abstract  void treeTableItemClicked(ItemClickEvent itemClickEvent);
	
	protected VerticalLayout buildSecondArea() {
		second = new VerticalLayout();
		second.setSizeFull();
		return second;
	}
	
	protected void checkToolBar() {
		if (SmockerUI.getInstance()!=null) {
			SmockerUI.getInstance().getEasyAppMainView().getToolBar().checkClickable(this);
		}
	}
	
	@Override
	public void refresh(EntityWithId entityWithId) {
		Notification notif = new Notification(
				"Refreshing",
				Type.ASSISTIVE_NOTIFICATION);

		// Customize it
		notif.setDelayMsec(100);
		notif.setPosition(Position.BOTTOM_RIGHT);
		notif.setIcon(FontAwesome.SPINNER);

		// Show it in the page
		notif.show(Page.getCurrent());

		treetable.setEnabled(true);

		refreshEntity(entityWithId);
		updateTree(entityWithId);
	}
	
	/**
	 * Update the tree 
	 * @param entityWithId
	 */
	protected abstract void updateTree(EntityWithId entityWithId);
	
	protected abstract void refreshEntity(EntityWithId entityWithId);
	
	
}
