package com.jenetics.smocker.rest;

import java.util.Arrays;
import java.util.List;

import javax.ejb.Stateless;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.jenetics.smocker.model.Connection;

@Stateless
@Path("/connections")
@Produces("application/json")
@Consumes("application/json")
public class ConnectionEndpoint extends AbstractConnectionEndpoint<Connection>{
	
	

	@GET
	@Path("/{id:[0-9][0-9]*}")
	public Response findById(@PathParam("id") final Long id) {
		//TODO: retrieve the connection 
		Connection connection = daoManager.findById(id);
		if (connection == null) {
			return Response.status(Status.NOT_FOUND).build();
		}
		return Response.ok(connection).build();
	}

	@GET
	public List<Connection> listAll(@QueryParam("start") final Integer startPosition,
			@QueryParam("max") final Integer maxResult) {
		//TODO: retrieve the connections 
		List<Connection> connections = null;
		
		Connection connection = new Connection();
		connection.setId((long) 0);
		connection.setHost("toto");
		connection.setPort(10);
		connections = Arrays.asList(new Connection[] {connection});
		
		return connections;
	}

	@PUT
	@Path("/{id:[0-9][0-9]*}")
	public Response update(@PathParam("id") Long id, final Connection connection) {
		//TODO: process the given connection 
		return Response.noContent().build();
	}

	@DELETE
	@Path("/{id:[0-9][0-9]*}")
	public Response deleteById(@PathParam("id") final Long id) {
		//TODO: process the connection matching by the given id 
		return Response.noContent().build();
	}

}
