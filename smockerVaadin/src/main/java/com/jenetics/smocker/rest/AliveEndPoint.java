package com.jenetics.smocker.rest;

import javax.ejb.Stateless;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import com.jenetics.smocker.model.BooleanResponse;



@Stateless
@Path("/alive")
@Produces("application/json")
@Consumes("application/json")
public class AliveEndPoint  {
	
	private static boolean initialized = false;
	
	public static boolean isInitialized() {
		return initialized;
	}

	public static void setInitialized(boolean initialized) {
		AliveEndPoint.initialized = initialized;
	}

	@GET
	public Response alive() {
		return Response.ok(new BooleanResponse(initialized)).build();
	}
	
}
