package com.jenetics.smocker.rest;

import java.io.Serializable;

import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.POST;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.jboss.logging.Logger;

import com.jenetics.smocker.dao.DaoManager;
import com.jenetics.smocker.dao.IDaoManager;
import com.jenetics.smocker.model.Connection;
import com.jenetics.smocker.model.EntityWithId;
import com.jenetics.smocker.ui.SmockerUI;

public abstract class AbstractConnectionEndpoint<T extends EntityWithId> {
	
	@Inject
	Logger logger;
	
	@PersistenceContext(unitName=SmockerUI.PERSISTENCE_UNIT) 
	private EntityManager em;

	@Inject
    private Event<T> connectionEventSrc;
	
	@Inject
	IDaoManager<T> daoManager;
	
	void init() {
		if (daoManager.getEm() == null) {
			daoManager.setEm(this.em);
		}
	}
	
	@POST
	public Response create(final T entity) {
		init();
		
		connectionEventSrc.fire(entity);
		daoManager.create(entity);
		return Response.created(
				UriBuilder.fromResource(ConnectionEndpoint.class).path(String.valueOf(entity.getId())).build())
				.build();
	}

}
