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
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.Response.Status;

import com.jenetics.smocker.dao.DaoManager;
import com.jenetics.smocker.dao.IDaoManager;
import com.jenetics.smocker.injector.Dao;
import com.jenetics.smocker.model.Communication;
import com.jenetics.smocker.model.Connection;
import com.jenetics.smocker.model.JavaApplication;
import com.jenetics.smocker.ui.SmockerUI;

@RequestScoped
@Path("/manageJavaApplication/")
@Produces({ "application/json" })
@Consumes({ "application/json" })
public class ManageJavaApplication {
	
	@PersistenceContext(unitName = SmockerUI.PERSISTENCE_UNIT)
	private EntityManager em;
	
	@Inject
	protected Event<JavaApplication> javaApplicationEventSrc;
	
	@Inject
	protected Event<Communication> communicationEventSrc;
	
	@Inject
	protected Event<Connection> connectionEventSrc;
	
	@Inject @Dao
	protected IDaoManager<JavaApplication> daoManager;
	
	@PUT
	@Path("/addConnection/{javaApplicationId}")
	public Response create(@PathParam("javaApplicationId") Long javaApplicationId, Connection conn) {
		JavaApplication target = daoManager.findById(javaApplicationId);
		if (target == null) {
			return Response.status(Status.NOT_FOUND).build();
		}
		for (Connection connIte : target.getConnections()) {
			if (connIte.getHost().equals(conn.getHost()) && connIte.getPort().equals(conn.getPort())) {
				return Response.status(Status.CONFLICT).build();
			}
		}
		conn.setJavaApplication(target);
		target.getConnections().add(conn);
		daoManager.update(target);
		//notify the creation
		Connection[] conns = new Connection[target.getConnections().size()];
		target.getConnections().toArray(conns);
		connectionEventSrc.fire(conns[conns.length - 1]);
		return Response.ok().entity(conn).build();
	}
	
	@DELETE
	@Path("/deleteConnection/{javaApplicationId}/{connectionId}")
	public Response create(@PathParam("javaApplicationId") Long javaApplicationId, @PathParam("connectionId") Long connectionId) {
		JavaApplication target = daoManager.findById(javaApplicationId);
		if (target == null) {
			return Response.status(Status.NOT_FOUND).entity("javaApplication with Id : " + javaApplicationId + " not found").build();
		}
		Optional<Connection> targetConn = target.getConnections().stream().filter(conn -> conn.getId().equals(connectionId)).findFirst();
		if (!targetConn.isPresent()) {
			return Response.status(Status.NOT_FOUND).entity("Connection with Id : " + targetConn +
					" not found in javaApplication with Id : " + javaApplicationId ).build();
		}
		
		target.getConnections().remove(targetConn.get());
		daoManager.update(target);
		//notify the creation
		javaApplicationEventSrc.fire(target);
		return Response.ok().build();
	}
	
	@PUT
	@Path("/addCommunication/{javaApplicationId}/{connectionId}")
	public Response create(@PathParam("javaApplicationId") Long javaApplicationId, @PathParam("connectionId") Long connectionId, Communication comm) {
		//set the dateTime to now
		comm.setDateTime(new Date());
		
		JavaApplication target = daoManager.findById(javaApplicationId);
		if (target == null) {
			return Response.status(Status.NOT_FOUND).build();
		}
		
		Optional<Connection> connection = target.getConnections().stream().filter(x -> x.getId().equals(connectionId)).findFirst();
		
		if (!connection.isPresent()) {
			return Response.status(Status.NOT_FOUND).entity("Connection with Id : " + connectionId +
					" not found in javaApplication with Id : " + javaApplicationId ).build();
		}
		comm.setConnection(connection.get());
		connection.get().getCommunications().add(comm);
		daoManager.update(target);
		
		//notify the creation
		communicationEventSrc.fire(comm);
		//javaApplicationEventSrc.fire(target);
		
		return Response.ok().entity(comm).build();
	}
	
	@DELETE
	@Path("/deleteCommunication/{javaApplicationId}/{connectionId}/{communicationId}")
	public Response create(@PathParam("javaApplicationId") Long javaApplicationId, @PathParam("connectionId") Long connectionId, @PathParam("communicationId") Long communicationId) {
		JavaApplication target = daoManager.findById(javaApplicationId);
		if (target == null) {
			return Response.status(Status.NOT_FOUND).entity("javaApplication with Id : " + javaApplicationId + " not found").build();
		}
		Optional<Connection> targetConn = target.getConnections().stream().filter(conn -> conn.getId().equals(connectionId)).findFirst();
		if (!targetConn.isPresent()) {
			return Response.status(Status.NOT_FOUND).entity("Connection with Id : " + targetConn +
					" not found in javaApplication with Id : " + javaApplicationId ).build();
		}
		Optional<Communication> targetCommunication = targetConn.get().getCommunications().stream().filter(x -> x.getId().equals(communicationId)).findFirst();
		if (!targetCommunication.isPresent()) {
			return Response.status(Status.NOT_FOUND).entity("Communication with Id : " + targetCommunication +
					" not found in javaApplication/Connection with Id : " + javaApplicationId + "/" + connectionId).build();
		}
		
		
		targetConn.get().getCommunications().remove(targetCommunication.get());
		daoManager.update(target);
		javaApplicationEventSrc.fire(target);
		return Response.ok().build();
	}
	

}
