package com.jenetics.smocker.rest;

import java.util.List;

import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;

import org.jboss.logging.Logger;

import com.jenetics.smocker.dao.IDaoManager;
import com.jenetics.smocker.model.EntityWithId;
import com.jenetics.smocker.ui.SmockerUI;

public abstract class AbstractEndpoint<T extends EntityWithId> {

	@Inject
	Logger logger;

	@PersistenceContext(unitName = SmockerUI.PERSISTENCE_UNIT)
	private EntityManager em;

	@Inject
	protected Event<T> entityEventSrc;

	@Inject
	protected IDaoManager<T> daoManager;

	void init() {
		if (daoManager.getEm() == null) {
			daoManager.setEm(this.em);
		}
	}

	@POST
	public Response create(final T entity) {
		init();

		daoManager.create(entity);
		//notify the creation
		entityEventSrc.fire(entity);
		return Response
				.created(UriBuilder.fromResource(ConnectionEndpoint.class).path(String.valueOf(entity.getId())).build())
				.build();

	}

	@GET
	@Path("/{id:[0-9][0-9]*}")
	public Response findById(@PathParam("id") final Long id) {
		//TODO: retrieve the connection 
		T entity = daoManager.findById(id);
		if (entity == null) {
			return Response.status(Status.NOT_FOUND).build();
		}
		return Response.ok(entity).build();
	}

	@GET
	public List<T> listAll(@QueryParam("start") final Integer startPosition, @QueryParam("max") final Integer maxResult) {
		//TODO: retrieve the connections 

		return daoManager.listAll(startPosition, maxResult);
	}

	@PUT
	@Path("/{id:[0-9][0-9]*}")
	public Response update(@PathParam("id") Long id, final T entity) {
		//TODO: process the given connection 
		daoManager.update(id, entity);
		return Response.ok(entity).build();
	}

	@DELETE
	@Path("/{id:[0-9][0-9]*}")
	public Response deleteById(@PathParam("id") final Long id) {
		//TODO: process the connection matching by the given id 
		daoManager.deleteById(id);
		return Response.noContent().build();
	}

}
