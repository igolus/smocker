package com.jenetics.smocker.rest;

import javax.ejb.Stateless;
import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import com.jenetics.smocker.model.Connection;

@Stateless
@Path("/connections")
@Produces("application/json")
@Consumes("application/json")
public class ConnectionEndpoint extends AbstractEndpoint<Connection> {

}
