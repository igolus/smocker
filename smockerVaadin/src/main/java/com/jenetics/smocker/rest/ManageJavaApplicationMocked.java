package com.jenetics.smocker.rest;

import java.util.Date;
import java.util.Optional;
import java.util.logging.Level;

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

	@PersistenceContext(unitName = SmockerUI.PERSISTENCE_UNIT_MEMORY)
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
}
