package com.jenetics.smocker.ui.component;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import org.vaadin.easyapp.util.ActionContainer;
import org.vaadin.easyapp.util.ActionContainer.InsertPosition;
import org.vaadin.easyapp.util.ActionContainerBuilder;
import org.vaadin.easyapp.util.EasyAppLayout;

import com.jenetics.smocker.dao.DaoManager;
import com.jenetics.smocker.dao.DaoManagerByModel;
import com.jenetics.smocker.dao.IDaoManager;
import com.jenetics.smocker.lucene.LuceneIndexer;
import com.jenetics.smocker.model.Communication;
import com.jenetics.smocker.model.Connection;
import com.jenetics.smocker.model.converter.MockConverter;
import com.jenetics.smocker.ui.SmockerUI;
import com.jenetics.smocker.ui.component.seach.CommunicationItemsResults;
import com.jenetics.smocker.ui.dialog.Dialog;
import com.jenetics.smocker.ui.netdisplayer.ComponentWithDisplayChange;
import com.jenetics.smocker.ui.netdisplayer.NetDisplayerFactoryInput;
import com.jenetics.smocker.ui.netdisplayer.NetDisplayerFactoryOutput;
import com.jenetics.smocker.ui.util.CommunicationDateDisplay;
import com.jenetics.smocker.ui.view.MockSpaceView;
import com.jenetics.smocker.util.NetworkReaderUtility;
import com.vaadin.data.TreeData;
import com.vaadin.data.provider.TreeDataProvider;
import com.vaadin.event.selection.SelectionEvent;
import com.vaadin.event.selection.SelectionListener;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.Grid.SelectionMode;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Tree;
import com.vaadin.ui.Tree.ItemClick;
import com.vaadin.ui.Window;


@SuppressWarnings("serial")
public class ConnectionDetailsView extends AbstractConnectionDetails {

	private Connection connection;
	private GridLayout grid = null;
	private Tree<CommunicationDateDisplay> menu;
	private TreeData<CommunicationDateDisplay> treeData;
	private TreeDataProvider<CommunicationDateDisplay> treeDataProvider;
	private Communication selectedCommunication = null;;
	private Hashtable<Communication, CommunicationDateDisplay> commDisplayByComm = new Hashtable<>();
	private Hashtable<Communication, String[]> decodedCommunications = new Hashtable<>();

	protected IDaoManager<Connection> daoManagerConnection = DaoManagerByModel.getDaoManager(Connection.class);

	public ConnectionDetailsView(Connection connection) {
		super();
		HorizontalSplitPanel mainLayout = new HorizontalSplitPanel();

		this.connection = connection;

		menu = new Tree<>();
		menu.setSelectionMode(SelectionMode.SINGLE);
		treeData = new TreeData<>();
		treeDataProvider = new TreeDataProvider<>(treeData);
		menu.setDataProvider(treeDataProvider);
		menu.addSelectionListener(this::menuSelectionChange);

		fillCommunication();

		menu.addItemClickListener(this::treeItemClick);
		treeDataProvider.refreshAll();

		menu.setSizeFull();

		grid = new GridLayout(2, 1);
		grid.setSizeFull();

		mainLayout.setFirstComponent(menu);
		mainLayout.setSecondComponent(grid);
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
		return connection.getCommunications();
	}

	private void fillCommunication() {
		treeData.clear();
		Set<Communication> communications = connection.getCommunications();
		for (Communication communication : communications) {
			CommunicationDateDisplay commDateDisplay = new CommunicationDateDisplay(communication);
			commDisplayByComm.put(communication, commDateDisplay);
			treeData.addItem(null, commDateDisplay);
		}
		treeDataProvider.refreshAll();
	}

	public void treeItemClick(ItemClick<CommunicationDateDisplay> event) {
		Communication comm = event.getItem().getCommunication();
		selectCommunication(comm);
	}

	private void selectCommunication(Communication comm) {
		String[] decodedCommunication = getDecodedCommunication(comm);
		
		
		String request = NetworkReaderUtility.decode(comm.getRequest());
		ComponentWithDisplayChange componentWithDisplayChangeInput = NetDisplayerFactoryInput.getComponent(request);
		Component inputComponent = componentWithDisplayChangeInput.getComponent();
		inputComponent.setSizeFull();
		grid.removeComponent(0, 0);
		grid.addComponent(inputComponent, 0, 0);
		componentWithDisplayChangeInput.selectionValue(decodedCommunication[0]);


		String response = NetworkReaderUtility.decode(comm.getResponse());
		ComponentWithDisplayChange componentWithDisplayChangeOutput = NetDisplayerFactoryOutput.getComponent(response);
		Component outputComponent = componentWithDisplayChangeOutput.getComponent();
		outputComponent.setSizeFull();
		grid.removeComponent(1, 0);
		grid.addComponent(outputComponent, 1, 0);
		componentWithDisplayChangeOutput.selectionValue(decodedCommunication[1]);

		selectedCommunication = comm;
		refreshClickable();
	}

	public void cleanGrid() {
		grid.removeComponent(0, 0);
		grid.removeComponent(1, 0);
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
		cleanGrid();
		fillCommunication();
		refreshClickable();
	}

	private void deleteCommunication(Communication communication) {
		communication.getConnection().getCommunications().remove(communication);
		daoManagerConnection.update(communication.getConnection());
	}

	private void deleteAll() {
		commDisplayByComm.clear();
		connection.getCommunications().clear();
		daoManagerConnection.update(connection);
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

	public void search(String searchQuery) {
		Set<Communication> communnications = getCommunications();
		LuceneIndexer lucenIndexer = new LuceneIndexer();
		communnications.stream().forEach( comm -> lucenIndexer.addEntity(comm));
		List<Communication> foundComms = lucenIndexer.search(searchQuery);
		if (foundComms != null) {
			CommunicationItemsResults commResult = new CommunicationItemsResults(foundComms, searchQuery, 
					this::selectedCommFromSearch);
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
		Communication selectedCommunication = getSelectedCommunication();
		if (selectedCommunication != null) {
			SmockerUI.displayInSubWindowMidSize(SmockerUI.getBundle().getString("StackTrace"), 
					new TextPanel(NetworkReaderUtility.decode(selectedCommunication.getCallerStack()), true));
		}
	}

}
