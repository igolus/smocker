package com.jenetics.smocker.rest;

import javax.ejb.Stateless;
import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import com.jenetics.smocker.model.Connection;
import com.jenetics.smocker.model.config.SmockerConf;

@Stateless
@Path("/configurations")
@Produces("application/json")
@Consumes("application/json")
public class SmockerConfEndpoint extends AbstractEndpoint<SmockerConf> {

}
