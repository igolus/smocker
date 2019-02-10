package com.jenetics.smocker.ui.view;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.jboss.logging.Logger;
import org.vaadin.easyapp.util.ActionContainer;
import org.vaadin.easyapp.util.ActionContainer.InsertPosition;
import org.vaadin.easyapp.util.ActionContainer.Position;
import org.vaadin.easyapp.util.ActionContainerBuilder;
import org.vaadin.easyapp.util.ButtonWithCheck;
import org.vaadin.easyapp.util.annotations.ContentView;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.jenetics.smocker.dao.DaoManagerByModel;
import com.jenetics.smocker.dao.IDaoManager;
import com.jenetics.smocker.model.CommunicationMocked;
import com.jenetics.smocker.model.ConnectionMocked;
import com.jenetics.smocker.model.EntityWithId;
import com.jenetics.smocker.model.JavaApplicationMocked;
import com.jenetics.smocker.model.Scenario;
import com.jenetics.smocker.ui.SmockerUI;
import com.jenetics.smocker.ui.component.ConnectionMockedDetailsView;
import com.jenetics.smocker.ui.component.ConnectionMockedManager;
import com.jenetics.smocker.ui.component.ScenarioUploader;
import com.jenetics.smocker.ui.component.UploadPanel;
import com.jenetics.smocker.ui.dialog.Dialog;
import com.jenetics.smocker.ui.util.JsonFileDownloader;
import com.jenetics.smocker.ui.util.JsonFileDownloader.OnDemandStreamResource;
import com.jenetics.smocker.ui.util.RefreshableView;
import com.jenetics.smocker.ui.util.StrandardTreeGridConnectionMockedData;
import com.jenetics.smocker.ui.util.TreeGridConnectionData;
import com.vaadin.annotations.Push;
import com.vaadin.event.selection.SelectionEvent;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.FileDownloader;
import com.vaadin.server.Resource;
import com.vaadin.server.StreamResource;
import com.vaadin.spring.annotation.ViewScope;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Upload;
import com.vaadin.ui.TabSheet.Tab;

@SuppressWarnings("serial")
@Push
@ViewScope
@ContentView(sortingOrder = 2, viewName = "MockView", icon = "icons/952776-200.png", homeView = true, rootViewParent = ConnectionsRoot.class)
public class MockSpaceView 
extends AbstractConnectionTreeView<JavaApplicationMocked, ConnectionMocked, CommunicationMocked, ConnectionMockedDetailsView> 
implements RefreshableView {
	
	
	private IDaoManager<Scenario> daoManagerScenario = DaoManagerByModel.getDaoManager(Scenario.class);
	
	@Inject
	private Logger logger;

	public MockSpaceView() {
		super(JavaApplicationMocked.class, ConnectionMocked.class, CommunicationMocked.class);
		treeGrid.addSelectionListener(this::treeSelectionChange);
		setSizeFull();
	}

	@Override
	public void enterInView(ViewChangeEvent event) {
		super.enterInView(event);
		fillTreeTable();
	}

	private static final String BUNDLE_NAME = "BundleUI";


//	@Override
//	protected Set<ConnectionMocked> getJavaAppConnections(JavaApplicationMocked javaApplication) {
//		return javaApplication.getConnections();
//	}
//
//	@Override
//	protected JavaApplicationMocked getJavaAppFromConnection(ConnectionMocked connection) {
//		return connection.getJavaApplication();
//	}

	@Override
	protected ConnectionMocked getConnectionFromCommunication(CommunicationMocked comm) {
		return comm.getConnection();
	}

	@Override
	protected TreeGridConnectionData<JavaApplicationMocked, ConnectionMocked> createTreeGridFromJavaApplication(
			JavaApplicationMocked javaApplication) {
		return new StrandardTreeGridConnectionMockedData(javaApplication, null);
	}

	@Override
	protected TreeGridConnectionData<JavaApplicationMocked, ConnectionMocked> createTreeGridFromJConnection(ConnectionMocked connection) {
		return new StrandardTreeGridConnectionMockedData(null, connection);
	}

	@Override
	protected void addTreeMapping() {
		//treeGrid.addColumn(item -> item.getApplication()).setCaption(APPLICATION);
		treeGrid.addColumn(item -> item.getAdress()).setCaption(ADRESS);
		treeGrid.addColumn(item -> item.getPort()).setCaption(PORT);
		treeGrid.addColumn(item -> item.getConnectionType()).setCaption(CONNECTION_TYPE);
	}

	public void treeSelectionChange(SelectionEvent<TreeGridConnectionData<JavaApplicationMocked, ConnectionMocked>> event) {
		refreshClickable();
	}

	@Override
	public void refresh(EntityWithId entityWithId) {
		refreshEntity(entityWithId);
	}
	
	/**
	 * Rebuild the treeTable
	 */
	protected void fillTreeTable() {
		treeData.clear();
		List<ConnectionMocked> listConnections = daoManagerConnection.listAll();
		for (ConnectionMocked connectionMocked : listConnections) {
			addConnectionToTree(null, connectionMocked);
		}
		treeDataProvider.refreshAll();
	}

	@Override
	public ActionContainer buildActionContainer() {
		ActionContainerBuilder builder = new ActionContainerBuilder(BUNDLE_NAME)
				.addButton("Clean_Button", VaadinIcons.MINUS, null,  this::isMainTabSelected			
						, this::clean, org.vaadin.easyapp.util.ActionContainer.Position.LEFT, InsertPosition.AFTER)
				.addButton("ViewDetails_Button", VaadinIcons.EYE, null,  this::canViewDetails			
						, this::details, org.vaadin.easyapp.util.ActionContainer.Position.LEFT, InsertPosition.AFTER)
				.addButton("Refresh_Button", VaadinIcons.REFRESH, null,  this::always
						, this::refresh, org.vaadin.easyapp.util.ActionContainer.Position.LEFT, InsertPosition.AFTER)
				.addButton("Save_Button", VaadinIcons.DISC, null,  this::canSave			
						, this::save, org.vaadin.easyapp.util.ActionContainer.Position.LEFT, InsertPosition.AFTER)
				.addButton("Export_Button", VaadinIcons.SHARE, "ExportToolTip",  this::canExport			
						, null, org.vaadin.easyapp.util.ActionContainer.Position.LEFT, InsertPosition.AFTER);

		ScenarioUploader uploader = new ScenarioUploader();
		Upload upload = new Upload(null, uploader);
		upload.setButtonCaption(SmockerUI.getBundleValue("Import_Button"));
		upload.setDescription(SmockerUI.getBundleValue("ImportToolTip"));
		upload.addSucceededListener(uploader);
		builder.addComponent(upload, Position.LEFT, InsertPosition.AFTER);
		
		ActionContainer actionContainer = builder.build();
		List<ButtonWithCheck> listButtonWithCheck = actionContainer.getListButtonWithCheck();
		ButtonWithCheck exportButton = listButtonWithCheck.get(listButtonWithCheck.size() - 1);

		//OnDemandStreamResource myResource = createResource();

		JsonFileDownloader downloader = new JsonFileDownloader(createResource());
		downloader.extend(exportButton);

		return actionContainer;
	}

	public boolean canExport() {
		if (getSelectedTab() != null && 
				getSelectedTab() instanceof ConnectionMockedDetailsView) {
			ConnectionMockedDetailsView details = (ConnectionMockedDetailsView) getSelectedTab();
			ConnectionMockedManager connectionMockedManager = details.getConnectionMockedManager();
			if (connectionMockedManager.getSelectedItem() != null) {
				return connectionMockedManager.getSelectedItem().isScenario() && 
						!connectionMockedManager.getSelectedItem().getScenario().equals(DaoManagerByModel.getUNDEFINED_SCENARIO());
			}
		}
		return false;
	}

	private OnDemandStreamResource createResource() {

		return new OnDemandStreamResource() {
			@Override
			public InputStream getStream() {
				try {
					Scenario scenario = getSelectedScenario();
					if (scenario != null) {
						ObjectMapper mapper = new ObjectMapper();
						mapper.enable(SerializationFeature.INDENT_OUTPUT);
						mapper.configure(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE, false);
						mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
						String jsonObjSTring = 
								mapper.writerWithDefaultPrettyPrinter().writeValueAsString(scenario);
						return new ByteArrayInputStream(jsonObjSTring.getBytes("UTF-8"));
					}
				} catch (IOException e) {
					logger.error("Unable to export scenario", e);
				}
				return null;
			}

			@Override
			public String getFilename() {
				return getSelectedScenario().getName() + ".json";
			}

		};
	}

	private Scenario getSelectedScenario() {
		ConnectionMockedDetailsView details = (ConnectionMockedDetailsView) getSelectedTab();
		ConnectionMockedManager connectionMockedManager = details.getConnectionMockedManager();
		if (connectionMockedManager.getSelectedItem() != null 
				&& connectionMockedManager.getSelectedItem().isScenario()) {
			return connectionMockedManager.getSelectedItem().getScenario();
		}
		return null;
	}

	public void importScenario(ClickEvent event) {
		SmockerUI.displayInSubWindowCustomSize("Import", new UploadPanel(), "200px", "200px");
	}

	public boolean canSave() {
		return !isMainTabSelected() && selectedDetailView.getSelectedCommunication() != null;
	}

	public void save(ClickEvent event) {
		getSelectedDetailView().save();
	}

	public void clean(ClickEvent event) {
		if (isSelected()) {
			Dialog.ask(SmockerUI.getBundle().getString("RemoveQuestion"), null, this::delete, null);
		}
	}

	public void delete() {
		if (isMainTabSelected()) {
			Set<TreeGridConnectionData<JavaApplicationMocked, ConnectionMocked>> selectedItems = treeGrid.getSelectedItems();
			for (TreeGridConnectionData<JavaApplicationMocked, ConnectionMocked> treeGridConnectionData : selectedItems) {
				if (treeGridConnectionData.isConnection()) {
					ConnectionMocked selectedConnection = treeGridConnectionData.getConnection();
					//selectedConnection.getJavaApplication().getConnections().remove(selectedConnection);
					removeTabConn(selectedConnection);
					cleanScenario(selectedConnection);
					daoManagerConnection.delete(selectedConnection);
					//daoManagerJavaApplication.update(selectedConnection.getJavaApplication());
				}
//				else if (treeGridConnectionData.isJavaApplication()) {
//					JavaApplicationMocked selectedJavaApplication = treeGridConnectionData.getJavaApplication();
//					selectedJavaApplication.getConnections().stream().forEach(this::removeTabConn);
//					cleanScenario(selectedJavaApplication);
//					daoManagerJavaApplication.deleteById(selectedJavaApplication.getId());
//				}
				fillTreeTable();
			}
		}
	}

//	private void cleanScenario(JavaApplicationMocked javaApplication) {
//		for (ConnectionMocked conn : javaApplication.getConnections()) {
//			cleanScenario(conn);
//		}
//	}

	private void cleanScenario(ConnectionMocked connection) {
		for (CommunicationMocked comm : connection.getCommunications()) {
			comm.getScenario().getCommunicationsMocked().remove(comm);
			daoManagerScenario.update(comm.getScenario());
		}
	}

	private void removeTabConn(ConnectionMocked selectedConnection) {
		Tab tabForConn = tabByConnection.get(selectedConnection);
		if (tabForConn != null) {
			detailsViewByTab.remove(tabForConn);
			tabByConnectionKey.remove( getConnectionKey(selectedConnection));
			tabSheet.removeTab(tabForConn);
		}
	}

	public boolean isSelected() {
		return treeGrid.getSelectedItems().size() == 1;
	}
	
	public boolean canViewDetails() {
		return isConnectionSelected() && isMainTabSelected();
	}

	public boolean isConnectionSelected() {
		return treeGrid.getSelectedItems().size() == 1 && 
				treeGrid.getSelectedItems().iterator().next().isConnection();
	}

	public void search(String searchValue) {
		Notification.show("Search for:" + searchValue);
	}

	@Override
	protected String getConnectionKey(ConnectionMocked conn) {
		return conn.getHost() + ":" + conn.getPort();
	}

	@Override
	protected ConnectionMockedDetailsView getConnectionDetailsLayout(ConnectionMocked conn) {
		ConnectionMockedDetailsView connectionMockedDetailsView = new ConnectionMockedDetailsView(conn, this::refreshClickable);
		return connectionMockedDetailsView;
	}

	@Override
	public boolean canSearch() {
		return false;
	}

	public void communicationMockedCreated(CommunicationMocked communicationMocked) {
		String connectionKey = getConnectionKey(communicationMocked.getConnection());
		Tab tabForConnection = tabByConnectionKey.get(connectionKey);
		if (tabForConnection != null) {
			ConnectionMockedDetailsView detailsView = (ConnectionMockedDetailsView) tabForConnection.getComponent();
			detailsView.communicationMockedCreated(communicationMocked);
		}

	}

	@Override
	protected Set<ConnectionMocked> getJavaAppConnections(JavaApplicationMocked javaApplication) {
		return null;
	}

	@Override
	protected JavaApplicationMocked getJavaAppFromConnection(ConnectionMocked connection) {
		// TODO Auto-generated method stub
		return null;
	}
}
