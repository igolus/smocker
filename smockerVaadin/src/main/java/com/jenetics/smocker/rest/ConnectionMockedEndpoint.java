package com.jenetics.smocker.rest;

import javax.ejb.Stateless;
import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import com.jenetics.smocker.model.ConnectionMocked;

@Stateless
@Path("/connectionsMocked")
@Produces("application/json")
@Consumes("application/json")
public class ConnectionMockedEndpoint extends AbstractEndpoint<ConnectionMocked>{

}
