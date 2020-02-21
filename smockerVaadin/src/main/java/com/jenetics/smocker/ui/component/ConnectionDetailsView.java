package com.jenetics.smocker.ui.component;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.stream.Collectors;

import com.jenetics.smocker.dao.DaoManagerByModel;
import com.jenetics.smocker.dao.DaoSingletonLock;
import com.jenetics.smocker.dao.IDaoManager;
import com.jenetics.smocker.lucene.LuceneIndexer;
import com.jenetics.smocker.model.Communication;
import com.jenetics.smocker.model.Connection;
import com.jenetics.smocker.model.event.CommunicationsRemoved;
import com.jenetics.smocker.ui.SmockerUI;
import com.jenetics.smocker.ui.component.seach.CommunicationItemsResults;
import com.jenetics.smocker.ui.dialog.Dialog;
import com.jenetics.smocker.ui.netdisplayer.ComponentWithDisplayChange;
import com.jenetics.smocker.ui.netdisplayer.NetDisplayerFactoryInput;
import com.jenetics.smocker.ui.netdisplayer.NetDisplayerFactoryOutput;
import com.jenetics.smocker.ui.util.CommunicationDateDisplay;
import com.jenetics.smocker.util.NetworkReaderUtility;
import com.vaadin.data.TreeData;
import com.vaadin.data.provider.TreeDataProvider;
import com.vaadin.event.selection.SelectionEvent;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.Grid.SelectionMode;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Tree;
import com.vaadin.ui.Tree.ItemClick;
import com.vaadin.ui.components.grid.SingleSelectionModel;
import com.vaadin.ui.Window;


@SuppressWarnings("serial")
public class ConnectionDetailsView extends AbstractConnectionDetails {

	private Connection connection;
	private Tree<CommunicationDateDisplay> menu;
	private TreeData<CommunicationDateDisplay> treeData;
	private TreeDataProvider<CommunicationDateDisplay> treeDataProvider;
	private Communication selectedCommunication = null;
	private transient Map<Communication, CommunicationDateDisplay> commDisplayByComm = new HashMap<>();
	private Map<Communication, String[]> decodedCommunications = new HashMap<>();

	protected transient IDaoManager<Connection> daoManagerConnection = DaoManagerByModel.getDaoManager(Connection.class);
	private Set<Communication> communications;
	private HorizontalSplitPanel mainLayout = new HorizontalSplitPanel();


	public ConnectionDetailsView(Connection connection) {
		super();

		this.connection = connection;

		menu = new Tree<>();
		menu.setStyleName("NoSelect");


		menu.setSelectionMode(SelectionMode.SINGLE);
		treeData = new TreeData<>();
		treeDataProvider = new TreeDataProvider<>(treeData);
		menu.setDataProvider(treeDataProvider);

		menu.addSelectionListener(this::menuSelectionChange);

		fillCommunication();

		menu.addItemClickListener(this::treeItemClick);
		treeDataProvider.refreshAll();

		menu.setSizeFull();
		((SingleSelectionModel)menu.getSelectionModel()).setDeselectAllowed(false);

		mainLayout.setFirstComponent(menu);
		mainLayout.setSplitPosition(23);
		mainLayout.setSizeFull();
		addComponent(mainLayout);
		setSizeFull();
	}

	private String[] getDecodedCommunication(Communication comm) {
		return decodedCommunications.computeIfAbsent(comm, ConnectionDetailsView::decodeCommunication);
	}

	private static String[] decodeCommunication(Communication comm) {
		return new String[] {
				NetworkReaderUtility.decode(comm.getRequest()),
				NetworkReaderUtility.decode(comm.getResponse())		
		};
	}

	public Connection getConnection() {
		return connection;
	}


	public void menuSelectionChange(SelectionEvent<CommunicationDateDisplay> event) {
		if (refreshClickable != null) {
			refreshClickable.run();
		}
	}

	public Communication getSelectedCommunication() {
		return selectedCommunication;
	}

	public Set<Communication> getCommunications () {
		return communications;
	}

	private void fillCommunication() {
		treeData.clear();
		DaoSingletonLock.lock();
		try {
			communications = connection.getCommunications();
			for (Communication communication : communications) {
				CommunicationDateDisplay commDateDisplay = new CommunicationDateDisplay(communication);
				commDisplayByComm.put(communication, commDateDisplay);
				treeData.addItem(null, commDateDisplay);
			}
			treeDataProvider.refreshAll();
		}
		finally {
			DaoSingletonLock.unlock();
		}

	}

	public void treeItemClick(ItemClick<CommunicationDateDisplay> event) {
		Communication comm = event.getItem().getCommunication();
		selectCommunication(comm);
	}

	/**
	 * Trigger when communication is selected
	 * @param comm
	 */
	private void selectCommunication(Communication comm) {
		if (selectedCommunication == comm) {
			return;
		}
		String[] decodedCommunication = getDecodedCommunication(comm);
		//recreate the grid and fill it

		GridLayout grid = new GridLayout(2, 1);
		grid.setSizeFull();


		String request = NetworkReaderUtility.decode(comm.getRequest());
		ComponentWithDisplayChange componentWithDisplayChangeInput = NetDisplayerFactoryInput.getComponent(request, comm);
		Component inputComponent = componentWithDisplayChangeInput.getComponent();
		componentWithDisplayChangeInput.selectionValue(decodedCommunication[0]);
		inputComponent.setSizeFull();
		grid.addComponent(inputComponent, 0, 0);

		String response = NetworkReaderUtility.decode(comm.getResponse());
		ComponentWithDisplayChange componentWithDisplayChangeOutput = NetDisplayerFactoryOutput.getComponent(response, comm);
		Component outputComponent = componentWithDisplayChangeOutput.getComponent();

		componentWithDisplayChangeOutput.selectionValue(decodedCommunication[1]);
		outputComponent.setSizeFull();

		grid.addComponent(outputComponent, 1, 0);
		selectedCommunication = comm;
		mainLayout.setSecondComponent(grid);
		refreshClickable();
	}

	public void clean() {
		if (isSelected()) {
			Dialog.ask(SmockerUI.getBundle().getString("RemoveQuestion"), null, this::delete, null);
		}
	}

	public void cleanAll() {
		Dialog.ask(SmockerUI.getBundle().getString("RemoveAllQuestion"), null, this::deleteAll, null);
	}

	public void delete() {
		deleteCommunication(selectedCommunication);
		commDisplayByComm.remove(selectedCommunication);
		postDeleteUiUpdate();
	}


	private void postDeleteUiUpdate() {
		fillCommunication();
		refreshClickable();
	}

	private void deleteCommunication(Communication communication) {
		communication.getConnection().getCommunications().remove(communication);
		daoManagerConnection.update(communication.getConnection());
		removeCommFromTree(communication);
		treeDataProvider.refreshAll();
	}

	private void removeCommFromTree(Communication communication) {
		CommunicationDateDisplay communicationDateDisplay = commDisplayByComm.get(communication);
		if (communicationDateDisplay != null) {
			treeData.removeItem(communicationDateDisplay);
		}
	}

	public void deleteCommunications(CommunicationsRemoved communicationsRemoved) {
		List<Communication> commList = communicationsRemoved.getCommList();
		for (Communication comm : commList) {
			removeCommFromTree(comm);
		}
		treeDataProvider.refreshAll();
	}

	private void deleteAll() {
		commDisplayByComm.clear();
		connection.getCommunications().clear();
		daoManagerConnection.update(connection);
		mainLayout.setSecondComponent(null);
		postDeleteUiUpdate();
	}

	public void cleanAll(ClickEvent event) {
		Notification.show("Clean");
	}

	public void refresh() {
		fillCommunication();
		resetSelected();
		refreshClickable();
	}

	public boolean isSelected() {
		return selectedCommunication != null;
	}

	public void resetSelected() {
		selectedCommunication = null;
	}

	public boolean always() {
		return true;
	}

	public boolean atLeastOneItem() {
		return treeData.getRootItems().size() > 1;
	}

	public void search(String searchQuery, Runnable callBack) {
		Set<Communication> communnications = getCommunications();
		LuceneIndexer lucenIndexer = new LuceneIndexer();
		communnications.stream().forEach( comm -> lucenIndexer.addEntity(comm));
		List<Communication> foundComms = lucenIndexer.search(searchQuery);
		if (foundComms != null) {
			CommunicationItemsResults commResult = 
					new CommunicationItemsResults(callBack, foundComms, searchQuery, this::selectedCommFromSearch);
			Window searchWindow = SmockerUI.displayInSubWindowMidSize(SmockerUI.getBundleValue("search_result"), commResult);
			commResult.setWindowContainer(searchWindow);
		}
		else {
			Notification.show(SmockerUI.getBundleValue("nothing_Found"));
		}
	}

	private void selectedCommFromSearch(Communication comm) {
		menu.select(commDisplayByComm.get(comm));
		selectCommunication(comm);
	}

	public void displayStack() {
		if (selectedCommunication != null) {
			SmockerUI.displayInSubWindowMidSize(SmockerUI.getBundle().getString("StackTrace"), 
					new TextPanel(NetworkReaderUtility.decode(selectedCommunication.getCallerStack()), true));
		}
	}

	public void sort() {
		treeData.clear();
		Set<Communication> communicationsSet = connection.getCommunications();
		List<Communication> sortedList = communicationsSet.stream().sorted(
				Comparator.comparing(Communication::getDateTime)).collect(Collectors.toList());
		for (Communication communication : sortedList) {
			CommunicationDateDisplay commDateDisplay = new CommunicationDateDisplay(communication);
			commDisplayByComm.put(communication, commDateDisplay);
			treeData.addItem(null, commDateDisplay);
		}
		treeDataProvider.refreshAll();
	}

}
