package com.jenetics.smocker.rest;

import java.util.List;
import java.util.Set;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import com.jenetics.smocker.dao.DaoManager;
import com.jenetics.smocker.dao.IDaoManager;
import com.jenetics.smocker.model.CommunicationMocked;
import com.jenetics.smocker.model.Connection;
import com.jenetics.smocker.model.ConnectionMocked;
import com.jenetics.smocker.model.JavaApplicationMocked;
import com.jenetics.smocker.model.MockRequest;
import com.jenetics.smocker.model.MockResponse;
import com.jenetics.smocker.ui.SmockerUI;
import com.jenetics.smocker.util.NetworkReaderUtility;

@Stateless
@Path("/findMock")
@Produces("application/json")
@Consumes("application/json")
public class FindMockEndpoint {
	
	@PersistenceContext(unitName = SmockerUI.PERSISTENCE_UNIT)
	private EntityManager em;
	
	private static IDaoManager<JavaApplicationMocked> daoManagerJavaApplicationMocked = 
			new DaoManager<>(JavaApplicationMocked.class, SmockerUI.getEm());
	
	
	@POST
	@Consumes("text/plain")
	public void postClichedMessage(String message) {
	    System.out.println(message);
	}
	
	@POST
	public Response findMock(MockRequest mockRequest) {
		
		String match = null;
		
		List<JavaApplicationMocked> javaApplicationsMocked = 
				daoManagerJavaApplicationMocked.findByColumn("classQualifiedName", mockRequest.getClassName());
		
		JavaApplicationMocked javaApplicationMocked = null;
		//consider the first one
		if (javaApplicationsMocked.size() > 0) {
			javaApplicationMocked = javaApplicationsMocked.get(0);
		}
		if (javaApplicationMocked != null) {
			Set<ConnectionMocked> connections = javaApplicationMocked.getConnections();
			for (ConnectionMocked connectionMocked : connections) {
				if (connectionMocked.getHost().equals(mockRequest.getHost()) && connectionMocked.getPort() == mockRequest.getPort()) {
					Set<CommunicationMocked> communications = connectionMocked.getCommunications();
					for (CommunicationMocked communicationMocked : communications) {
						match = checkMatch(mockRequest.getRequest(), communicationMocked);
					}
				}
			}
		}
		
		
		if (match != null) {
			MockResponse response = new MockResponse();
			response.setResponse(match);
			return Response.ok(response).build();
		}
		else {
			return Response.noContent().build();
		}
	
	}

	/**
	 * Check if the communication match
	 * @param input
	 * @param match
	 * @param communicationMocked
	 * @return
	 */
	private String checkMatch(String input, CommunicationMocked communicationMocked) {
		if (communicationMocked.getRequest().equals(input)) {
			return communicationMocked.getResponse();
		}
		return null;
	}
}
