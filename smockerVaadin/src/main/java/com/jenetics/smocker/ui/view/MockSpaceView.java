package com.jenetics.smocker.ui.view;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.jboss.logging.Logger;
import org.vaadin.aceeditor.AceEditor;
import org.vaadin.aceeditor.AceMode;
import org.vaadin.aceeditor.AceTheme;
import org.vaadin.easyapp.util.ButtonDescriptor;
import org.vaadin.easyapp.util.annotations.ContentView;

import com.jenetics.smocker.dao.DaoManager;
import com.jenetics.smocker.dao.IDaoManager;
import com.jenetics.smocker.model.Communication;
import com.jenetics.smocker.model.CommunicationMocked;
import com.jenetics.smocker.model.Connection;
import com.jenetics.smocker.model.ConnectionMocked;
import com.jenetics.smocker.model.EntityWithId;
import com.jenetics.smocker.model.JavaApplication;
import com.jenetics.smocker.model.JavaApplicationMocked;
import com.jenetics.smocker.ui.SmockerUI;
import com.jenetics.smocker.ui.SmockerUI.EnumButton;
import com.jenetics.smocker.ui.netdisplayer.ComponentWithDisplayChange;
import com.jenetics.smocker.ui.netdisplayer.NetDisplayerFactoryInput;
import com.jenetics.smocker.ui.netdisplayer.NetDisplayerFactoryOutput;
import com.jenetics.smocker.ui.util.ButtonWithId;
import com.jenetics.smocker.ui.util.ComboWithId;
import com.jenetics.smocker.ui.util.CommunicationMockedTreeItem;
import com.jenetics.smocker.ui.util.CommunicationTreeItem;
import com.jenetics.smocker.ui.util.ListSelectWithId;
import com.jenetics.smocker.util.NetworkReaderUtility;
import com.vaadin.annotations.Push;
import com.vaadin.data.Container.ItemSetChangeEvent;
import com.vaadin.data.Container.ItemSetChangeListener;
import com.vaadin.data.Container.PropertySetChangeEvent;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.FontAwesome;
import com.vaadin.spring.annotation.ViewScope;
import com.vaadin.ui.Button;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Label;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.Table;
import com.vaadin.ui.Tree;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;

@Push
@ViewScope
@ContentView(sortingOrder = 1, viewName = "Mock Space", icon = "icons/Java-icon.png", homeView = false, rootViewParent = ConnectionsRoot.class)
public class MockSpaceView extends AbstractConnectionTreeView<JavaApplicationMocked, ConnectionMocked, CommunicationMocked> {
	
	protected transient Map<String, ComboWithId<ConnectionMocked>> listComboByUiId;
	
	@Inject
	private Logger logger;

	public MockSpaceView() {
		super(JavaApplicationMocked.class, ConnectionMocked.class, CommunicationMocked.class);
	}

	@Override
	public ClickListener getClickListener(String key) {
		if (key.equals(EnumButton.REMOVE.toString())) {
			return (ClickEvent event) -> deleteSelectedItem();
		} 
		return null;

	}


	private Object deleteSelectedItem() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isClickable(String key) {
		return false;
	}

	@Override
	public void enter(ViewChangeEvent event) {
		//do nothing
	}
	
	@Override
	protected Map<String, Class<?>> getColumnMap() {
		Map<String, Class<?>> ret = new HashMap<>();
		ret.put(APPLICATION, String.class);
		ret.put(ADRESS, String.class);
		ret.put(PORT, String.class);
		ret.put(CONNECTION_TYPE, String.class);
		return ret;
	}
	

	@Override
	protected void addColumnToTreeTable() {
		treetable.addGeneratedColumn("Watch", (Table source, Object itemId, Object columnId) -> {
			if (!treetable.getItem(itemId).getItemProperty(ADRESS).getValue().toString().isEmpty()
					&& !treetable.getItem(itemId).getItemProperty(PORT).getValue().toString().isEmpty()) {
				String uiId = treetable.getItem(itemId).getItemProperty(ADRESS).getValue().toString()
						+ treetable.getItem(itemId).getItemProperty(PORT).getValue().toString();
				ComboBox box = listComboByUiId.get(uiId);
				
				box.addItemSetChangeListener(new ItemSetChangeListener() {
					
					@Override
					public void containerItemSetChange(ItemSetChangeEvent event) {
						System.out.println("");
					}
				});
				return box;
			}
			return null;
		});
	}
	
	
	protected void fillCommunications(ConnectionMocked conn, boolean checkSelected) {
		// clean communication panel
		second.removeAllComponents();

		// only if the connection is selected and if there are some
		// communications items exccept if if comes from click event
		Object connectionItem = connectionTreeItemByConnectionId.get(conn.getId());

		if (connectionItem != null && (treetable.isSelected(connectionItem) || !checkSelected)
				&& !conn.getCommunications().isEmpty()) {
			Set<CommunicationMocked> communications = conn.getCommunications();

			Tree menu = new Tree();
			for (CommunicationMocked communication : communications) {
				CommunicationMockedTreeItem commTreeItem = new CommunicationMockedTreeItem(communication);
				menu.addItem(commTreeItem);
				menu.setChildrenAllowed(commTreeItem, false);
			}

			menu.setSizeFull();
			GridLayout grid = new GridLayout(2, 1);
			grid.setSizeFull();

			menu.addItemClickListener((ItemClickEvent event) -> {
				CommunicationMocked comm = ((CommunicationMockedTreeItem) event.getItemId()).getCommunication();
				// remove selection in the table
				treetable.select(null);
				selectedCommunication = comm;

//				String response = NetworkReaderUtility.decode(comm.getResponse());
//				ComponentWithDisplayChange outputComponent = NetDisplayerFactoryOutput.getComponent(response);
//				grid.removeComponent(1, 0);
//				grid.addComponent(outputComponent.getComponent(), 1, 0);
//				outputComponent.selectionValue(response);
				
				AceEditor editor = new AceEditor();
				editor.setThemePath("/static/ace");	
				editor.setMode(AceMode.java);
				editor.setTheme(AceTheme.eclipse);

				// Use worker (if available for the current mode)
				editor.setUseWorker(true);
				editor.setSizeFull();
				editor.setValue("Hello world!");
				grid.removeComponent(1, 0);
				grid.addComponent(editor, 1, 0);
				
				String request = NetworkReaderUtility.decode(comm.getRequest());
				ComponentWithDisplayChange inputComponent = NetDisplayerFactoryInput.getComponent(request);
				grid.removeComponent(0, 0);
				grid.addComponent(inputComponent.getComponent(), 0, 0);
				inputComponent.selectionValue(request);
				checkToolBar();
			});

			HorizontalSplitPanel hsplitPane = new HorizontalSplitPanel();

			hsplitPane.setFirstComponent(menu);
			hsplitPane.setSecondComponent(grid);
			hsplitPane.setSplitPosition(20);

			second.addComponent(hsplitPane);
		} else if (conn.getCommunications().isEmpty() && !checkSelected) {
			selectedCommunication = null;
		}
	}


	@Override
	protected Long getJavaAppId(JavaApplicationMocked javaApplication) {
		return javaApplication.getId();
	}

	@Override
	protected Long getConnectionId(ConnectionMocked connection) {
		return connection.getId();
	}

	@Override
	protected String getJavaAppClassQualifiedName(JavaApplicationMocked javaApplication) {
		return javaApplication.getClassQualifiedName();
	}

	@Override
	protected Set<ConnectionMocked> getJavaAppConnections(JavaApplicationMocked javaApplication) {
		return javaApplication.getConnections();
	}

	@Override
	protected void manageSpecialUIBehaviourInJavaApplication(ConnectionMocked connection) {
		ListSelectWithId<ConnectionMocked> listSelectWithId = new ListSelectWithId<>(
				null, connection.getHost() + connection.getPort().toString(), connection);
		ComboWithId<ConnectionMocked> box = new ComboWithId();
		box.setScrollToSelectedItem(false);
		box.setTextInputAllowed(false);
		
		
		box.addItems(
				bundle.getString("mock_mode_disabled"), 
				bundle.getString("mock_mode_mock_or_call"),
				bundle.getString("mock_mode_strict"));
		listComboByUiId.put(listSelectWithId.getUiId(), box);
	}

	@Override
	protected String getConnectionHost(ConnectionMocked connection) {
		return connection.getHost();
	}

	@Override
	protected Integer getConnectionPort(ConnectionMocked connection) {
		return connection.getPort();
	}

	@Override
	protected ConnectionMocked getCorrespondingConnection(String host, String port,
			JavaApplicationMocked javaApplication) {
		Set<ConnectionMocked> connections = javaApplication.getConnections();
		Optional<ConnectionMocked> connection = connections.stream().filter(
				x -> StringUtils.equals(host, x.getHost()) && StringUtils.equals(port, x.getPort().toString()))
				.findFirst();
		return connection.isPresent() ? connection.get() : null;
	}

	@Override
	protected JavaApplicationMocked getJavaAppFromConnection(ConnectionMocked connection) {
		return connection.getJavaApplication();
	}

	@Override
	protected ConnectionMocked getConnectionFromCommunication(CommunicationMocked comm) {
		return comm.getConnection();
	}
	
	@Override
	public List<ButtonDescriptor> getButtons() {
		return Arrays.asList(new ButtonDescriptor[] { new ButtonDescriptor(bundle.getString("remove"),
				bundle.getString("removeToolTip"), FontAwesome.REMOVE, EnumButton.REMOVE.toString())});
	}

	@Override
	protected void instanciateMaps() {
		listComboByUiId = new HashMap<>();
	}

}
