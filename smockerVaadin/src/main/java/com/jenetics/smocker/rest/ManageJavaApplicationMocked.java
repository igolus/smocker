package com.jenetics.smocker.rest;

import java.util.Date;
import java.util.Optional;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.jboss.logging.Logger;

import com.jenetics.smocker.dao.IDaoManager;
import com.jenetics.smocker.injector.Dao;
import com.jenetics.smocker.model.CommunicationMocked;
import com.jenetics.smocker.model.ConnectionMocked;
import com.jenetics.smocker.model.JavaApplicationMocked;
import com.jenetics.smocker.ui.SmockerUI;

@RequestScoped
@Path("/manageJavaApplicationMocked/")
@Produces({ "application/json" })
@Consumes({ "application/json" })
public class ManageJavaApplicationMocked {

	@PersistenceContext(unitName = SmockerUI.PERSISTENCE_UNIT)
	private EntityManager em;

	@Inject
	protected Event<JavaApplicationMocked> javaApplicationEventSrc;

	@Inject
	protected Event<CommunicationMocked> communicationEventSrc;

	@Inject
	protected Event<ConnectionMocked> connectionEventSrc;

	@Inject
	@Dao
	protected IDaoManager<JavaApplicationMocked> daoManager;

	@Inject
	private Logger logger;

	@PUT
	@Path("/addConnection/{javaApplicationId}")
	public Response create(@PathParam("javaApplicationId") Long javaApplicationId, ConnectionMocked conn) {
		JavaApplicationMocked target = daoManager.findById(javaApplicationId);
		if (target == null) {
			return Response.status(Status.NOT_FOUND).build();
		}
		for (ConnectionMocked connIte : target.getConnections()) {
			if (connIte.getHost().equals(conn.getHost()) && connIte.getPort().equals(conn.getPort())) {
				return Response.status(Status.CONFLICT).build();
			}
		}
		conn.setJavaApplication(target);
		conn.setWatched(true);
		target.getConnections().add(conn);
		daoManager.update(target);
		// notify the creation
		ConnectionMocked[] conns = new ConnectionMocked[target.getConnections().size()];
		target.getConnections().toArray(conns);
		ConnectionMocked connUpdated = conns[conns.length - 1];
		connectionEventSrc.fire(connUpdated);
		return Response.ok().entity(connUpdated).build();
	}

	@DELETE
	@Path("/deleteConnection/{javaApplicationId}/{connectionId}")
	public Response create(@PathParam("javaApplicationId") Long javaApplicationId,
			@PathParam("connectionId") Long connectionId) {
		JavaApplicationMocked target = daoManager.findById(javaApplicationId);
		if (target == null) {
			return Response.status(Status.NOT_FOUND)
					.entity("javaApplication with Id : " + javaApplicationId + " not found").build();
		}
		Optional<ConnectionMocked> targetConn = target.getConnections().stream()
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
			@PathParam("connectionId") Long connectionId, CommunicationMocked comm) {
		// set the dateTime to now
		comm.setDateTime(new Date());

		JavaApplicationMocked target = daoManager.findById(javaApplicationId);
		if (target == null) {
			return Response.status(Status.NOT_FOUND).build();
		}

		Optional<ConnectionMocked> connection = target.getConnections().stream()
				.filter(x -> x.getId().equals(connectionId)).findFirst();

		if (!connection.isPresent()) {
			return Response.status(Status.NOT_FOUND).build();
		}
		if (connection.get().getWatched() != null && connection.get().getWatched()) {
			comm.setConnection(connection.get());
			connection.get().getCommunications().add(comm);
			daoManager.update(target);

			// notify the creation
			communicationEventSrc.fire(comm);
			return Response.ok().entity(comm).build();
		}
		return Response.status(Status.FORBIDDEN).build();

	}

	@DELETE
	@Path("/deleteCommunication/{javaApplicationId}/{connectionId}/{communicationId}")
	public Response create(@PathParam("javaApplicationId") Long javaApplicationId,
			@PathParam("connectionId") Long connectionId, @PathParam("communicationId") Long communicationId) {
		JavaApplicationMocked target = daoManager.findById(javaApplicationId);
		if (target == null) {
			logger.warn("javaApplication with Id : " + javaApplicationId + " not found");
			return Response.status(Status.NOT_FOUND).build();
		}
		Optional<ConnectionMocked> targetConn = target.getConnections().stream()
				.filter(conn -> conn.getId().equals(connectionId)).findFirst();
		if (!targetConn.isPresent()) {
			logger.warn("Connection with Id : " + targetConn + " not found in javaApplication with Id : "
					+ javaApplicationId);
			return Response.status(Status.NOT_FOUND).build();
		}
		Optional<CommunicationMocked> targetCommunication = targetConn.get().getCommunications().stream()
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
