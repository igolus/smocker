package com.jenetics.smocker.rest;

import javax.ejb.Stateless;
import javax.enterprise.context.RequestScoped;
import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import com.jenetics.smocker.dao.DaoManager;
import com.jenetics.smocker.model.Connection;
import com.jenetics.smocker.model.JavaApplication;

@Stateless
@Path("/javaapplications")
@Produces("application/json")
@Consumes("application/json")
public class JavaApplicationEndpoint extends AbstractEndpoint<JavaApplication>{


}
