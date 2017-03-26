package com.jenetics.resEasyAgent;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;

import org.jboss.logging.Logger;

import com.jenetics.resEasyAgent.dao.IDaoManager;


@Path("/connections")
@Produces("application/json")
@Consumes("application/json")
public abstract class GenericEndPoint {
	
	@Inject
	Logger logger;
	
	@PersistenceContext(unitName="smockerLocalData") 
	private EntityManager em;

	private IDaoManager daoManager;
	
	private Class targetClass;
	
	@POST
	public Response create(final EntityWithId entity) {
		em.getTransaction().begin();
		em.persist(entity);
		em.getTransaction().commit();
		return Response.created(
				UriBuilder.fromResource(GenericEndPoint.class).path(String.valueOf(entity.getId())).build())
				.build();
		
	}

	@GET
	@Path("/{id:[0-9][0-9]*}")
	public Response findById(@PathParam("id") final Long id) {
		//TODO: retrieve the connection 
		EntityWithId ret = em.find(targetClass, id);
		if (ret == null) {
			return Response.status(Status.NOT_FOUND).build();
		}
		return Response.ok(ret).build();
	}

//	@GET
//	public List<Connection> listAll(@QueryParam("start") final Integer startPosition,
//			@QueryParam("max") final Integer maxResult) {
//		//TODO: retrieve the connections 
//		List<Connection> connections = null;
//		
//		Connection connection = new Connection();
//		connection.setId((long) 0);
//		connection.setHost("toto");
//		connection.setPort(10);
//		connections = Arrays.asList(new Connection[] {connection});
//		
//		return connections;
//	}
//
//	@PUT
//	@Path("/{id:[0-9][0-9]*}")
//	public Response update(@PathParam("id") Long id, final EntityWithId connection) {
//		//TODO: process the given connection 
//		return Response.noContent().build();
//	}
//
//	@DELETE
//	@Path("/{id:[0-9][0-9]*}")
//	public Response deleteById(@PathParam("id") final Long id) {
//		//TODO: process the connection matching by the given id 
//		return Response.noContent().build();
//	}

}
