package com.jenetics.smocker.rest;

import javax.ejb.Stateless;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.jenetics.smocker.jseval.SmockerJsEnv;
import com.jenetics.smocker.model.Connection;
import com.jenetics.smocker.model.JavaApplication;
import com.jenetics.smocker.model.config.SmockerConf;

@Stateless
@Path("/configurations")
@Produces("application/json")
@Consumes("application/json")
public class SmockerConfEndpoint extends AbstractEndpoint<SmockerConf> {
	
	@POST
	@Path("/clearEnv")
	public Response clear() {
		SmockerJsEnv.getInstance().clear();
		return Response.ok().build();
	}
}
