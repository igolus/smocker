package com.jenetics.smocker.ui.view;

import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
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
import org.vaadin.easyapp.util.ButtonDescriptor;
import org.vaadin.easyapp.util.annotations.ContentView;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.jenetics.smocker.dao.DaoManager;
import com.jenetics.smocker.dao.IDaoManager;
import com.jenetics.smocker.injector.BundleUI;
import com.jenetics.smocker.model.Communication;
import com.jenetics.smocker.model.Connection;
import com.jenetics.smocker.model.EntityWithId;
import com.jenetics.smocker.model.JavaApplication;
import com.jenetics.smocker.ui.SmockerUI;
import com.jenetics.smocker.ui.SmockerUI.EnumButton;
import com.jenetics.smocker.ui.util.ButtonWithId;
import com.jenetics.smocker.ui.util.CommunicationTreeItem;
import com.jenetics.smocker.ui.util.RefreshableView;
import com.vaadin.addon.jpacontainer.JPAContainer;
import com.vaadin.addon.jpacontainer.JPAContainerFactory;
import com.vaadin.annotations.Push;
import com.vaadin.data.Item;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.shared.Position;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.spring.annotation.ViewScope;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.PopupView;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.Tree;
import com.vaadin.ui.TreeTable;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.VerticalSplitPanel;
import com.vaadin.ui.Window;

@Push
@ViewScope
@ContentView(sortingOrder=1, viewName = "Java Applications", icon = "icons/Java-icon.png", homeView=true, rootViewParent=ConnectionsRoot.class)
public class JavaApplicationsView extends VerticalSplitPanel implements RefreshableView {


	private static final String SEP_CONN = ":";

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

	private VerticalLayout second;

	private Map<Long ,Object > applicationItemById = new HashMap<>();
	private Map<String ,ButtonWithId> buttonByUiId =  new HashMap<>();
	private Map<String ,Long > applicationIdIByAdressAndPort =  new HashMap<>();
	private Map<Long, Object> connectionTreeItemByConnectionId = new HashMap<>();

	private TextArea areaInput;

	private TextArea areaOutput;

	protected Item selectedTreeItem;

	protected Communication selectedCommunication = null;

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
		buildTreeTable();
		buildSecondArea();
		fillTreeTable();
		treetable.setWidth("100%");;
		treetable.setHeight("40%");


		treetable.setSizeFull();
		setFirstComponent(treetable);
		setSecondComponent(second);
		setSplitPosition(150, Unit.PIXELS);

		setSizeFull();
		checkToolBar();
	}

	private void buildTreeTable() {
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

		treetable.addItemClickListener(new ItemClickEvent.ItemClickListener() {
			@Override
			public void itemClick(ItemClickEvent itemClickEvent) {
				selectedTreeItem = itemClickEvent.getItem();
				checkToolBar();
				
				if (!StringUtils.isEmpty(itemClickEvent.getItem().getItemProperty(ADRESS).toString()) && 
						!StringUtils.isEmpty(itemClickEvent.getItem().getItemProperty(PORT).toString())) {
					String host = itemClickEvent.getItem().getItemProperty(ADRESS).toString();
					String port = itemClickEvent.getItem().getItemProperty(PORT).toString();
					String key = host + SEP_CONN + port;
					Long appId = applicationIdIByAdressAndPort.get(key);
					if (appId != null) {
						jpaJavaApplication.refresh();
						JavaApplication javaApplication = jpaJavaApplication.getItem(appId).getEntity();
						Set<Connection> connections = javaApplication.getConnections();
						Optional<Connection> connection = connections.stream().
								filter(x -> (StringUtils.equals(host, x.getHost()) && StringUtils.equals(port, x.getPort().toString()))).findFirst();
						if (connection.isPresent()) {
							fillCommunications(connection.get(), false);
						}
					}
				}
				else {
					selectedCommunication = null;
				}
				checkToolBar();
			}

			
		});
	}
	
	private void checkToolBar() {
		if (SmockerUI.getInstance()!=null) {
			SmockerUI.getInstance().getEasyAppMainView().getToolBar().checkClickable(JavaApplicationsView.this);
		}
	}

	protected void fillCommunications(Connection conn, boolean checkSelected) {
		
		//only if the connection is selected and if there are some communications items exccept if if comes from click event
		Object connectionItem = connectionTreeItemByConnectionId.get(conn.getId());
		
		if (connectionItem != null && 
				(treetable.isSelected(connectionItem) || !checkSelected) && 
				conn.getCommunications().size() > 0) {
			Set<Communication> communications = conn.getCommunications();

			Tree menu = new Tree();
			for (Communication communication : communications) {
				CommunicationTreeItem commTreeItem = new CommunicationTreeItem(communication);
				menu.addItem(commTreeItem);
				menu.setChildrenAllowed(commTreeItem, false);
			}

			menu.setSizeFull();


//			Label richTextInput= new Label("<h1>" + bundle.getString("Input") + "</h1>");
//			richTextInput.setContentMode( ContentMode.HTML);
//			richTextInput.setSizeFull();


			areaInput = new TextArea(bundle.getString("Input"));
			areaInput.setReadOnly(true);
			areaInput.setSizeFull();

//			GridLayout gridInput = new GridLayout(1, 2);
//			gridInput.addComponent(richTextInput, 0, 0);
//			gridInput.addComponent(areaInput, 0, 1);
//			gridInput.setRowExpandRatio(0, 1);
//			gridInput.setRowExpandRatio(1, 10);
//			gridInput.setSizeFull();

//			Label richTextOutput= new Label("<h1>" + bundle.getString("Output") + "</h1>");
//			richTextOutput.setContentMode( ContentMode.HTML);
//			richTextOutput.setSizeFull();

			areaOutput = new TextArea(bundle.getString("Output"));
			areaOutput.setReadOnly(true);
			areaOutput.setSizeFull();

//			GridLayout gridOutput = new GridLayout(1, 2);
//			gridOutput.addComponent(richTextOutput, 0, 0);
//			gridOutput.addComponent(areaOutput, 0, 1);
//			gridOutput.setRowExpandRatio(0, 1);
//			gridOutput.setRowExpandRatio(1, 10);
//			gridOutput.setSizeFull();

			menu.addItemClickListener(new ItemClickListener() {
				@Override
				public void itemClick(ItemClickEvent event) {
					
					Communication comm =  ((CommunicationTreeItem) event.getItemId()).getCommunication();
					selectedCommunication = comm;
					areaInput.setReadOnly(false);
					areaOutput.setReadOnly(false);
					areaInput.setValue(decode(comm.getRequest()));
					areaOutput.setValue(decode(comm.getResponse()));
					areaInput.setReadOnly(true);
					areaOutput.setReadOnly(true);
					checkToolBar();
				}
			});

			GridLayout grid = new GridLayout(2, 1);
			grid.setSizeFull();
			grid.addComponent(areaInput, 0, 0);
			grid.addComponent(areaOutput, 1, 0);

			HorizontalSplitPanel hsplitPane = new HorizontalSplitPanel();

			hsplitPane.setFirstComponent(menu);
			hsplitPane.setSecondComponent(grid);
			hsplitPane.setSplitPosition(20);

			second.removeAllComponents();
			second.addComponent(hsplitPane);

		}
		else if (conn.getCommunications().size() == 0 && !checkSelected) {
			selectedCommunication = null;
			second.removeAllComponents();
		}

	}

	protected String decode(String code) {
		String ret = null;
		if (code != null) {
			ret = new String(Base64.getDecoder().decode(code)); 
		}
		return ret;
	}

	//**
	private VerticalLayout buildSecondArea() {
		second = new VerticalLayout();
		second.setSizeFull();
		return second;
	}

	private Object rootTreeItem = null;

	private void fillTreeTable() {
		treetable.removeAllItems();
		jpaJavaApplication = JPAContainerFactory.make(JavaApplication.class, SmockerUI.getEm());
		Collection<Object> itemIds = jpaJavaApplication.getItemIds();

		Object[] root = new Object[] { "all", "", "", "" };
		rootTreeItem = treetable.addItem(root, null);
		for (Object id : itemIds) {
			JavaApplication javaApplication = jpaJavaApplication.getItem(id).getEntity();
			buildJavaApplicationTreeItem(javaApplication);
		}
	}



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
			addConnectionItemToTreeTable(conn.getJavaApplication(), conn);
		}
		else if (entityWithId instanceof Communication) {
			Communication comm = (Communication) entityWithId;
			fillCommunications(comm.getConnection(), true);
		}
	}


	private void rebuildConnectionsTreeItem(Set<Connection> connections, JavaApplication javaApplication) {

		Object applicationTreeItemId = applicationItemById.get(javaApplication.getId());
		//remove all the items
		if (treetable.getChildren(applicationTreeItemId) != null) {
			for(Object child: new HashSet<Object>(treetable.getChildren(applicationTreeItemId))) {
				treetable.removeItem(child);
				connectionTreeItemByConnectionId.values().remove(child);
			}
		}

		for (Iterator iterator = connections.iterator(); iterator.hasNext();) {
			Connection connection = (Connection) iterator.next();
			addConnectionItemToTreeTable(javaApplication, connection);
		}
	}

	private void addConnectionItemToTreeTable(JavaApplication javaApplication, Connection connection) {
			
		Object javaApplicationTreeItem = applicationItemById.get(javaApplication.getId());

		if (javaApplicationTreeItem != null) {
			String buttonString;
			if (connection.getWatched() == null || connection.getWatched()) {
				buttonString = bundle.getString("Watch_Button");
			}
			else {
				buttonString = bundle.getString("UnWatch_Button");
			}
			ButtonWithId<Connection> buttonWithId = new ButtonWithId<Connection>(connection.getHost() + connection.getPort().toString(), connection);
			buttonWithId.setCaption(buttonString);
			buttonWithId.setIcon(FontAwesome.GLOBE);
			buttonByUiId.put(buttonWithId.getUiId(), buttonWithId);
			Object[] itemConnection = new Object[] { javaApplication.getClassQualifiedName(),  connection.getHost(), connection.getPort().toString(), ""};
			Object connectionTreeItem = treetable.addItem(itemConnection, null);
			connectionTreeItemByConnectionId.remove(connection.getId());
			connectionTreeItemByConnectionId.put(connection.getId(), connectionTreeItem);

			treetable.setChildrenAllowed(javaApplicationTreeItem, true);
			treetable.setParent(connectionTreeItem, javaApplicationTreeItem);
			treetable.setChildrenAllowed(connectionTreeItem, false);
			
			applicationIdIByAdressAndPort.remove(connection.getHost() + SEP_CONN + connection.getPort());
			applicationIdIByAdressAndPort.put(connection.getHost() + SEP_CONN + connection.getPort(), connection.getJavaApplication().getId());

			fillCommunications(connection, true);
		}
		else {
			logger.warn("Unable to find javaApplicationTreeItem");
		}

	}


	private void buildJavaApplicationTreeItem(JavaApplication javaApplication) {

		if (applicationItemById.get(javaApplication.getId()) == null) {
			Object[] javaApplicationItem = new Object[] { javaApplication.getClassQualifiedName(),  "", "", ""};
			Object javaApplicationTreeItem = treetable.addItem(javaApplicationItem, null);
			treetable.setParent(javaApplicationTreeItem, rootTreeItem);
			treetable.setChildrenAllowed(javaApplicationTreeItem, false);
			applicationItemById.put(javaApplication.getId(), javaApplicationTreeItem);
		}

		Set<Connection> connections = javaApplication.getConnections();

		rebuildConnectionsTreeItem(connections, javaApplication);

		if (connections.size() == 0) {
			treetable.setChildrenAllowed(applicationItemById.get(javaApplication.getId()), false);
		}
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
		if (key.equals(EnumButton.STACK.toString())) {
			return new ClickListener() {
				@Override
				public void buttonClick(ClickEvent event) {
					if (selectedCommunication != null) {
						
						Window subwindow = new Window(bundle.getString("StackTrace"));
						subwindow.setWidth("800px");
						subwindow.setHeight("600px");;
						subwindow.setModal(true);
						
						VerticalLayout subContent = new VerticalLayout();
						subContent.setMargin(true);
						subContent.setSpacing(true);
						subwindow.setContent(subContent);


				        Label message = new Label(decode(selectedCommunication.getCallerStack()));
				        message.setSizeFull();
				        subContent.addComponent(message);
				        subwindow.center();
				        
				        SmockerUI.getInstance().addWindow(subwindow);
					}
				}
			};
		}
		return null;

	}

	@Override
	public List<ButtonDescriptor> getButtons() {
		// TODO Auto-generated method stub
		return Arrays.asList( new ButtonDescriptor [] {
			new ButtonDescriptor(bundle.getString("CleanAll"), bundle.getString("CleanAllToolTip"), 
					FontAwesome.REMOVE, EnumButton.CLEAN_ALL.toString()),
			
			new ButtonDescriptor(bundle.getString("StackTrace"), bundle.getString("StackTraceToolTip"), 
					FontAwesome.REMOVE, EnumButton.STACK.toString())
			});
	}

	@Override
	public boolean isClickable(String key) {
		if (key.equals(EnumButton.CLEAN_ALL.toString())) {
			return true;
		}
		if (key.equals(EnumButton.STACK.toString())) {
			//enable only if connection is selected
			return selectedCommunication != null;
		}
		return false;
	}
}
