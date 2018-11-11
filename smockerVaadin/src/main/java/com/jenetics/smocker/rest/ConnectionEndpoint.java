package com.jenetics.smocker.rest;

import java.util.List;
import java.util.stream.Collectors;

import javax.ejb.Stateless;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import com.jenetics.smocker.model.CommunicationMocked;
import com.jenetics.smocker.model.Connection;
import com.jenetics.smocker.model.ListHostResponse;

@Stateless
@Path("/connections")
@Produces("application/json")
@Consumes("application/json")
public class ConnectionEndpoint extends AbstractEndpoint<Connection> {
	
	@GET
	@Path("/listHostUnWatched")
	public Response listActivated() {
		try {
			List<String> listActivatedHost = daoManager.listAll().stream()
					.filter(conn -> !conn.getWatched())
					.map( con -> con.getHost() + ":" + con.getPort())
					.distinct()
					.collect(Collectors.toList());
				
				ListHostResponse hostResponses = new ListHostResponse();
				listActivatedHost.stream().forEach( value -> hostResponses.getActivatedHosts().add(value));
				
				return Response.ok(hostResponses).build();
		}
		catch (Throwable e) {
			e.printStackTrace();
		}
		return null;
		
	}
}
