package com.jenetics.smocker.ui.view;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.jboss.logging.Logger;
import org.vaadin.easyapp.util.ButtonDescriptor;
import org.vaadin.easyapp.util.annotations.ContentView;

import com.jenetics.smocker.dao.DaoManager;
import com.jenetics.smocker.dao.IDaoManager;
import com.jenetics.smocker.model.Communication;
import com.jenetics.smocker.model.Connection;
import com.jenetics.smocker.model.EntityWithId;
import com.jenetics.smocker.model.JavaApplication;
import com.jenetics.smocker.model.converter.MockConverter;
import com.jenetics.smocker.network.ClientCommunicator;
import com.jenetics.smocker.ui.SmockerUI;
import com.jenetics.smocker.ui.SmockerUI.EnumButton;
import com.jenetics.smocker.ui.dialog.Dialog;
import com.jenetics.smocker.ui.netdisplayer.ComponentWithDisplayChange;
import com.jenetics.smocker.ui.netdisplayer.NetDisplayerFactoryInput;
import com.jenetics.smocker.ui.netdisplayer.NetDisplayerFactoryOutput;
import com.jenetics.smocker.ui.util.ButtonWithId;
import com.jenetics.smocker.ui.util.CommunicationTreeItem;
import com.jenetics.smocker.util.NetworkReaderUtility;
import com.vaadin.annotations.Push;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.FontAwesome;
import com.vaadin.spring.annotation.ViewScope;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.Tree;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

@Push
@ViewScope
@ContentView(sortingOrder = 1, viewName = "Java Applications", icon = "icons/Java-icon.png", homeView = true, rootViewParent = ConnectionsRoot.class)
public class JavaApplicationsView extends AbstractConnectionTreeView<JavaApplication, Connection, Communication> {

	private static final long serialVersionUID = 1L;

	protected static final String NAME_PROPERTY = "Name";
	protected static final String HOURS_PROPERTY = "Hours done";
	protected static final String MODIFIED_PROPERTY = "Last Modified";
	
	protected Map<String, ButtonWithId<Connection>> buttonByUiId;

	@Inject
	private Logger logger;

	private final class WatchMuteClicked implements ClickListener {
		@Override
		public void buttonClick(ClickEvent event) {
			ButtonWithId<Connection> buttonWithId = (ButtonWithId<Connection>) event.getSource();
			if (buttonWithId.getEntity().getWatched() == null || !buttonWithId.getEntity().getWatched()) {
				buttonWithId.setCaption(bundle.getString("Mute_Button"));
				buttonWithId.getEntity().setWatched(true);
				buttonWithId.setEnabled(false);
				daoManagerConnection.update(buttonWithId.getEntity());
				buttonWithId.setEntity(daoManagerConnection.findById(buttonWithId.getEntity().getId()));
				ClientCommunicator.sendWatched(buttonWithId.getEntity());
				buttonWithId.setEnabled(true);
			} else {
				buttonWithId.setCaption(bundle.getString("Watch_Button"));
				buttonWithId.getEntity().setWatched(false);
				buttonWithId.setEnabled(false);
				daoManagerConnection.update(buttonWithId.getEntity());
				buttonWithId.setEntity(daoManagerConnection.findById(buttonWithId.getEntity().getId()));
				ClientCommunicator.sendUnWatched(buttonWithId.getEntity());
				buttonWithId.setEnabled(true);
			}
		}
	}

	public JavaApplicationsView() {
		super(JavaApplication.class, Connection.class, Communication.class);
	}
	
	@Override
	protected Map<String, Class<?>> getColumnMap() {
		Map<String, Class<?>> ret = new LinkedHashMap<>();
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
				Button button = buttonByUiId.get(uiId);

				if (button != null && button.getListeners(ClickEvent.class).isEmpty()) {
					button.addClickListener(new WatchMuteClicked());
				}
				return button;
			}
			return null;
		});
	}

	@Override
	protected void fillCommunications(Connection conn, boolean checkSelected) {
		second.removeAllComponents();

		// only if the connection is selected and if there are some
		// communications items exccept if if comes from click event
		Object connectionItem = connectionTreeItemByConnectionId.get(conn.getId());

		if (connectionItem != null && (treetable.isSelected(connectionItem) || !checkSelected)
				&& !conn.getCommunications().isEmpty()) {
			Set<Communication> communications = conn.getCommunications();

			Tree menu = new Tree();
			for (Communication communication : communications) {
				CommunicationTreeItem commTreeItem = new CommunicationTreeItem(communication);
				menu.addItem(commTreeItem);
				menu.setChildrenAllowed(commTreeItem, false);
			}

			menu.setSizeFull();
			GridLayout grid = new GridLayout(2, 1);
			grid.setSizeFull();

			menu.addItemClickListener((ItemClickEvent event) -> {
				Communication comm = ((CommunicationTreeItem) event.getItemId()).getCommunication();
				// remove selection in the table
				treetable.select(null);
				selectedCommunication = comm;

				String response = NetworkReaderUtility.decode(comm.getResponse());
				ComponentWithDisplayChange outputComponent = NetDisplayerFactoryOutput.getComponent(response);
				grid.removeComponent(1, 0);
				grid.addComponent(outputComponent.getComponent(), 1, 0);
				outputComponent.selectionValue(response);

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

//	/**
//	 * Rebuild the treeTable
//	 */
//	@Override
//	protected void fillTreeTable() {
//		clearAssociationMaps();
//
//		treetable.removeAllItems();
//		second.removeAllComponents();
//
//		List<JavaApplication> listAllJavaApplications = daoManagerJavaApplication.listAll();
//
//		Object[] root = new Object[] { ALL, "", "", "" };
//		this.rootTreeItem = treetable.addItem(root, null);
//
//		for (JavaApplication javaApplication : listAllJavaApplications) {
//			fillJavaApplicationTreeItem(javaApplication, true);
//		}
//	}


	@Override
	public void clearAssociationMaps() {
		super.clearAssociationMaps();
		if (buttonByUiId != null) {
			buttonByUiId.clear();
		}
	}

	@Override
	protected void manageSpecialUIBehaviourInJavaApplication(Connection connection) {
		String buttonString;
		if (connection.getWatched() == null || connection.getWatched()) {
			buttonString = bundle.getString("Mute_Button");
		} else {
			buttonString = bundle.getString("Watch_Button");
		}
		ButtonWithId<Connection> buttonWithId = new ButtonWithId<>(
				connection.getHost() + connection.getPort().toString(), connection);
		buttonWithId.setCaption(buttonString);
		buttonWithId.setIcon(FontAwesome.GLOBE);
		buttonByUiId.put(buttonWithId.getUiId(), buttonWithId);
	}

	
	

	@Override
	public ClickListener getClickListener(String key) {
		if (key.equals(EnumButton.REMOVE.toString())) {
			return (ClickEvent event) -> deleteSelectedItem();
		} else if (key.equals(EnumButton.STACK.toString())) {
			return (ClickEvent event) -> {
				if (selectedCommunication != null) {

					Window subwindow = new Window(bundle.getString("StackTrace"));
					subwindow.setWidth("800px");
					subwindow.setHeight("600px");
					subwindow.setModal(true);

					VerticalLayout subContent = new VerticalLayout();
					subContent.setMargin(true);
					subContent.setSpacing(true);
					subwindow.setContent(subContent);

					Label message = new Label(NetworkReaderUtility.decode(selectedCommunication.getCallerStack()));
					message.setSizeFull();
					subContent.addComponent(message);
					subwindow.center();

					SmockerUI.getInstance().addWindow(subwindow);
				}
			};
		} else if (key.equals(EnumButton.ADD_TO_MOCK.toString())) {
			return (ClickEvent event) -> addItemToMock();
		}

		return null;

	}

	/**
	 * Add item to mock space
	 */
	private void addItemToMock() {
		if (selectedConnection != null) {
			MockConverter.convertConnection(selectedConnection);
		}
		else if (selectedJavaApplication != null) {
			MockConverter.convertJavaApplication(selectedJavaApplication);
		}
		SmockerUI.getInstance().getEasyAppMainView().getScanner().navigateTo(MockSpaceView.class);
		MockSpaceView MockSpaceView = (MockSpaceView)SmockerUI.getInstance().getEasyAppMainView().getScanner().getViewMap().get(MockSpaceView.class.toString());
		MockSpaceView.fillTreeTable();
	}

	/**
	 * Delete the selected Item, Could be JavaApplication, Connection or
	 * communications
	 */
	private void deleteSelectedItem() {
		Dialog.ask(bundle.getString("RemoveQuestion"), null, () -> {
			if (selectedConnection != null) {
				Set<Communication> communications = selectedConnection.getCommunications();
				for (Communication communication : communications) {
					selectedConnection.getCommunications().remove(communication);
				}
				daoManagerConnection.update(selectedConnection);

				selectedConnection.getJavaApplication().getConnections().remove(selectedConnection);
				daoManagerJavaApplication.update(selectedConnection.getJavaApplication());
			} else if (selectedJavaApplication != null) {
				daoManagerJavaApplication.deleteById(selectedJavaApplication.getId());
			} else if (selectedCommunication != null) {
				selectedCommunication.getConnection().getCommunications().remove(selectedCommunication);
				daoManagerConnection.update(selectedCommunication.getConnection());
			}

			fillTreeTable();
		}, null);
	}

	@Override
	public List<ButtonDescriptor> getButtons() {
		return Arrays.asList(new ButtonDescriptor[] { new ButtonDescriptor(bundle.getString("remove"),
				bundle.getString("removeToolTip"), FontAwesome.REMOVE, EnumButton.REMOVE.toString()),

				new ButtonDescriptor(bundle.getString("StackTrace"), bundle.getString("StackTraceToolTip"),
						FontAwesome.BARS, EnumButton.STACK.toString()),

				new ButtonDescriptor(bundle.getString("addToMock"), bundle.getString("StackTraceToolTip"),
						FontAwesome.PLUS, EnumButton.ADD_TO_MOCK.toString()) });
	}

	@Override
	public boolean isClickable(String key) {
		if (key.equals(EnumButton.REMOVE.toString())) {
			return true;
		}
		if (key.equals(EnumButton.STACK.toString())) {
			// enable only if connection is selected
			return selectedCommunication != null;
		}
		if (key.equals(EnumButton.ADD_TO_MOCK.toString())) {
			return true;
		}
		return false;
	}

	@Override
	public void enter(ViewChangeEvent event) {
		// nothing to show

	}
	
	@Override
	protected Connection getCorrespondingConnection(String host, String port, JavaApplication javaApplication) {
		Set<Connection> connections = javaApplication.getConnections();
		Optional<Connection> connection = connections.stream().filter(
				x -> StringUtils.equals(host, x.getHost()) && StringUtils.equals(port, x.getPort().toString()))
				.findFirst();
		return connection.isPresent() ? connection.get() : null;
	}

	@Override
	protected Long getJavaAppId(JavaApplication javaApplication) {
		return javaApplication.getId();
	}

	@Override
	protected Long getConnectionId(Connection connection) {
		return connection.getId();
	}

	@Override
	protected String getJavaAppClassQualifiedName(JavaApplication javaApplication) {
		return javaApplication.getClassQualifiedName();
	}

	@Override
	protected Set<Connection> getJavaAppConnections(JavaApplication javaApplication) {
		return javaApplication.getConnections();
	}

	@Override
	protected String getConnectionHost(Connection connection) {
		return connection.getHost();
	}

	@Override
	protected Integer getConnectionPort(Connection connection) {
		return connection.getPort();
	}

	@Override
	protected JavaApplication getJavaAppFromConnection(Connection connection) {
		return connection.getJavaApplication();
	}

	@Override
	protected Connection getConnectionFromCommunication(Communication comm) {
		return comm.getConnection();
	}

	@Override
	protected void instanciateMaps() {
		buttonByUiId = new HashMap<>();
	}

}
