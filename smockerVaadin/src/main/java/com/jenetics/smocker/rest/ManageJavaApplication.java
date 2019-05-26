package com.jenetics.smocker.rest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang3.StringUtils;
import org.jboss.logging.Logger;

import com.jenetics.smocker.dao.DaoConfig;
import com.jenetics.smocker.dao.DaoSingletonLock;
import com.jenetics.smocker.dao.IDaoManager;
import com.jenetics.smocker.injector.Dao;
import com.jenetics.smocker.jseval.JSEvaluator;
import com.jenetics.smocker.model.Communication;
import com.jenetics.smocker.model.Connection;
import com.jenetics.smocker.model.JavaApplication;
import com.jenetics.smocker.model.config.JsFilterAndDisplay;
import com.jenetics.smocker.model.event.CommunicationsRemoved;
import com.jenetics.smocker.rest.container.AddCommunicationContainer;
import com.jenetics.smocker.rest.model.ListConnections;
import com.jenetics.smocker.threading.ExecutorBean;
import com.jenetics.smocker.ui.SmockerUI;
import com.jenetics.smocker.util.NetworkReaderUtility;
import com.jenetics.smocker.util.SmockerException;
import com.vaadin.ui.UI;

@RequestScoped
@Path("/manageJavaApplication/")
@Produces({ "application/json" })
@Consumes({ "application/json" })
public class ManageJavaApplication {

	private static final int FREQUENCY_CLEAN = 50;

	private static final int SIZE_FOR_CLEAN = 3500000;

	@PersistenceContext(unitName = SmockerUI.PERSISTENCE_UNIT_MEMORY)
	private EntityManager em;

	@Inject
	protected Event<JavaApplication> javaApplicationEventSrc;

	@Inject
	protected Event<Communication> communicationEventSrc;

	@Inject
	protected Event<Connection> connectionEventSrc;
	
	@Inject
	protected Event<CommunicationsRemoved> communicationsRemovedEventSrc;

	@Inject
	@Dao
	protected IDaoManager<JavaApplication> daoManager;

	@Inject
	@Dao
	protected IDaoManager<Connection> daoManagerConnection;

	@Inject
	@Dao
	protected IDaoManager<Communication> daoManagerCommunication;

	@Inject
	private Logger logger;

	@GET
	@Path("/{javaApplicationId}/listConnections")
	public Response listConnection(@PathParam("javaApplicationId") Long javaApplicationId) {
		JavaApplication target = daoManager.findById(javaApplicationId);
		ListConnections listConn = new ListConnections();
		if (target != null) {
			target.getConnections().stream().forEach( item -> listConn.addConnection(item.getHost(), item.getPort(), item.getId()));
			return Response.ok().entity(listConn).build();
		}
		return Response.ok().build();
	}

	@PUT
	@Path("/addConnection/{javaApplicationId}")
	public Response create(@PathParam("javaApplicationId") Long javaApplicationId, Connection conn) {
		DaoSingletonLock.lock();
		JavaApplication target = daoManager.findById(javaApplicationId);
		if (target == null) {
			DaoSingletonLock.unlock();
			return Response.status(Status.NOT_FOUND).build();
		}
		for (Connection connIte : target.getConnections()) {
			if (connIte.getHost().equals(conn.getHost()) && connIte.getPort().equals(conn.getPort())) {
				DaoSingletonLock.unlock();
				return Response.status(Status.CONFLICT).build();
			}
		}
		conn.setJavaApplication(target);
		conn.setWatched(true);
		target.getConnections().add(conn);
		conn = daoManagerConnection.create(conn);
		daoManager.update(target);
		// notify the creation
		Connection[] conns = new Connection[target.getConnections().size()];
		target.getConnections().toArray(conns);
		Connection connUpdated = conns[conns.length - 1];
		connectionEventSrc.fire(connUpdated);
		DaoSingletonLock.unlock();
		return Response.ok().entity(connUpdated).build();
	}

	@DELETE
	@Path("/deleteConnection/{javaApplicationId}/{connectionId}")
	public Response create(@PathParam("javaApplicationId") Long javaApplicationId,
			@PathParam("connectionId") Long connectionId) {
		JavaApplication target = daoManager.findById(javaApplicationId);
		if (target == null) {
			return Response.status(Status.NOT_FOUND)
					.entity("javaApplication with Id : " + javaApplicationId + " not found").build();
		}
		Optional<Connection> targetConn = target.getConnections().stream()
				.filter(conn -> conn.getId().equals(connectionId)).findFirst();
		if (!targetConn.isPresent()) {
			return Response.status(Status.NOT_FOUND).entity("Connection with Id : " + targetConn
					+ " not found in javaApplication with Id : " + javaApplicationId).build();
		}

		target.getConnections().remove(targetConn.get());
		daoManager.update(target);
		// notify the creation
		javaApplicationEventSrc.fire(target);
		return Response.ok().build();
	}

	@PUT
	@Path("/addCommunication/{javaApplicationId}/{connectionId}")
	public Response create(@PathParam("javaApplicationId") Long javaApplicationId,
			@PathParam("connectionId") Long connectionId, Communication comm) {

		AddCommunicationContainer addCommunicationContainer = new AddCommunicationContainer(javaApplicationId,  
				connectionId, comm);
		
		//final UI ui = getCurrent();
		ExecutorBean.executeAsync(addCommunicationContainer, this::createAsync);
		return Response.ok().build();
	}
	
	private static int countcreateComm;
	
	public Response createAsync(AddCommunicationContainer addCommunicationContainer) {
		DaoSingletonLock.lock();
		try {
			if (countcreateComm++ == FREQUENCY_CLEAN) {
				countcreateComm = 0;
				List<Communication> listAll = daoManagerCommunication.listAll();
				Communication[] commToRemove = filterElementToRemoveForMemory(listAll);
				if (commToRemove != null) {
					deleteComms(commToRemove);
					SmockerUI.getInstance().displayNotif(SmockerUI.getBundleValue("CleaningCommMemory"), 0);
					CommunicationsRemoved commRemoved = new CommunicationsRemoved(Arrays.asList(commToRemove));
					SmockerUI.getInstance().remove(commRemoved);
				}
			}
			
			Communication comm = addCommunicationContainer.getComm();
			Long connectionId = addCommunicationContainer.getConnectionId();
			Long javaApplicationId = addCommunicationContainer.getJavaApplicationId();

			// set the dateTime to now
			if (comm.getDateTime() == null) {
				comm.setDateTime(new Date());
			}

			JavaApplication target = daoManager.findById(javaApplicationId);
			if (target == null) {
				DaoSingletonLock.unlock();
				return Response.status(Status.NOT_FOUND).build();
			}

			Optional<Connection> connection = target.getConnections().stream().filter(x -> x.getId().equals(connectionId))
					.findFirst();

			if (!connection.isPresent()) {
				DaoSingletonLock.unlock();
				return Response.status(Status.NOT_FOUND).build();
			}

			if (connection.get().getWatched() != null && connection.get().getWatched()) {
				//check filter
				JsFilterAndDisplay jsDisplayAndFilter = DaoConfig.findJsDisplayAndFilter(connection.get());
				String input = NetworkReaderUtility.decode(comm.getRequest());
				String output = NetworkReaderUtility.decode(comm.getResponse());
				String filterJsFunction = jsDisplayAndFilter.getFunctionFilter();

				if (!StringUtils.isEmpty(filterJsFunction)) {
					try {
						if (JSEvaluator.filter(filterJsFunction, input, output)) {
							trace(comm);
							return Response.ok().build();
						}
					} catch (SmockerException e) {
						//continue
					}
				}

				comm.setConnection(connection.get());
				comm = daoManagerCommunication.create(comm);
				connection.get().getCommunications().add(comm);
				daoManager.update(target);

				// notify the creation
				communicationEventSrc.fire(comm);

				StringBuilder builder = new StringBuilder();
				builder.append("Communication added to ")
				.append(comm.getConnection().getHost())
				.append(":")
				.append(comm.getConnection().getPort());

				SmockerUI.getInstance().log(Level.INFO, builder.toString());
				DaoSingletonLock.unlock();
				trace(comm);
				return Response.ok().build();
			}
			DaoSingletonLock.unlock();
			return Response.status(Status.FORBIDDEN).build();
		}
		catch (Exception e) {
			logger.error("unable to add communication", e);
		}
		finally {
			DaoSingletonLock.unlock();
		}
		return null;
	}

	private void trace(Communication comm) throws SmockerException {
		//trace function
		JsFilterAndDisplay first = DaoConfig.findJsDisplayAndFilter(comm.getConnection());
		if (first.getFunctionTrace() != null) {
			JSEvaluator.trace(first.getFunctionTrace(), 
					NetworkReaderUtility.decode(comm.getRequest()), NetworkReaderUtility.decode(comm.getResponse()));
		}
	}
	
	private void deleteComms(Communication[] commToRemove) {
		List<Connection> listAllConn = new ArrayList<>();
		for (Communication comm : commToRemove) {
			if (!listAllConn.contains(comm.getConnection())) {
				listAllConn.add(comm.getConnection());
			}
		}
		
		for (Connection conn : listAllConn) {
			conn.getCommunications().removeAll(Arrays.asList(commToRemove));
			DaoSingletonLock.lock();
			try {
				daoManagerConnection.update(conn);
			}
			catch (Exception e) {
				logger.error("Unable to clean memory", e);
			}
			finally {
				DaoSingletonLock.unlock();
			}
		}
	}

	/**
	 * Filter element to remove for memory limit
	 * @param listAll
	 * @return null if nothing to clean
	 */
	private Communication[] filterElementToRemoveForMemory(List<Communication> listAll) {
		int size = 0;
		Communication[] communicationArr = new Communication[listAll.size()];
		communicationArr = listAll.toArray(communicationArr);
		for (int i = communicationArr.length - 1; i >= 0; i--) {
			size += communicationArr[i].getRequest().length() + communicationArr[i].getResponse().length();
			if (size > SIZE_FOR_CLEAN) {
				Communication[] ret = new Communication[i + 1];
				System.arraycopy(communicationArr, 0, ret, 0, i + 1);
				return ret;
			}
		}
		return null;
	}

	@DELETE
	@Path("/deleteCommunication/{javaApplicationId}/{connectionId}/{communicationId}")
	public Response create(@PathParam("javaApplicationId") Long javaApplicationId,
			@PathParam("connectionId") Long connectionId, @PathParam("communicationId") Long communicationId) {
		JavaApplication target = daoManager.findById(javaApplicationId);
		if (target == null) {
			logger.warn("javaApplication with Id : " + javaApplicationId + " not found");
			return Response.status(Status.NOT_FOUND).build();
		}
		Optional<Connection> targetConn = target.getConnections().stream()
				.filter(conn -> conn.getId().equals(connectionId)).findFirst();
		if (!targetConn.isPresent()) {
			logger.warn("Connection with Id : " + targetConn + " not found in javaApplication with Id : "
					+ javaApplicationId);
			return Response.status(Status.NOT_FOUND).build();
		}
		Optional<Communication> targetCommunication = targetConn.get().getCommunications().stream()
				.filter(x -> x.getId().equals(communicationId)).findFirst();
		if (!targetCommunication.isPresent()) {
			logger.warn("Communication with Id : " + targetCommunication
					+ "not found in javaApplication/Connection with Id : " + javaApplicationId + "/" + connectionId);
			return Response.status(Status.NOT_FOUND).build();
		}

		targetConn.get().getCommunications().remove(targetCommunication.get());
		daoManager.update(target);
		javaApplicationEventSrc.fire(target);
		return Response.ok().build();
	}

}
