package com.jenetics.smocker.rest;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.stream.Collectors;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import com.jenetics.smocker.dao.IDaoManager;
import com.jenetics.smocker.injector.Dao;
import com.jenetics.smocker.jseval.JSEvaluator;
import com.jenetics.smocker.model.BooleanResponse;
import com.jenetics.smocker.model.CommunicationMocked;
import com.jenetics.smocker.model.Connection;
import com.jenetics.smocker.model.ListHostResponse;
import com.jenetics.smocker.model.MatchMockRequest;
import com.jenetics.smocker.model.MatchMockResponse;
import com.jenetics.smocker.ui.SmockerUI;
import com.jenetics.smocker.util.NetworkReaderUtility;
import com.jenetics.smocker.util.SmockerException;

@Stateless
@Path("/mocks")
@Produces("application/json")
@Consumes("application/json")
public class MockedRepliesEndPoint  {
	
	private static final String SEP = ":";
	private static final String NO_MATCH = "NO_MATCH";
	@Inject
	@Dao
	protected IDaoManager<CommunicationMocked> daoManagerCommunications;
	
	@GET
	@Path("/atLeastOneMockeActivated/{host}")
	public Response atLeastOneMockeActicated(@PathParam("host") String host) {
		final String decodedHost;
		try {
			decodedHost = URLDecoder.decode(host, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			return Response.serverError().build();
		}
		boolean isThere = daoManagerCommunications.listAll().stream()
			.filter( comm -> comm.isActivated() && comm.getConnection().getHost().equals(decodedHost))
			.findAny()
			.isPresent();
		return Response.ok(new BooleanResponse(isThere)).build();
	}
	
	@GET
	@Path("/listHostActivated")
	public Response listActivated() {
		try {
			List<String> listActivatedHost = daoManagerCommunications.listAll().stream()
					.filter(CommunicationMocked::isActivated)
					.map( comm -> comm.getConnection().getHost() + SEP + comm.getConnection().getPort())
					.distinct()
					.collect(Collectors.toList());
				
				ListHostResponse hostResponses = new ListHostResponse();
				listActivatedHost.stream().forEach( host -> hostResponses.getActivatedHosts().add(host));
				
				return Response.ok(hostResponses).build();
		}
		catch (Throwable e) {
			e.printStackTrace();
		}
		return null;
		
	}
	
	@POST
	@Path("/checkMatch") 
	public Response create(final MatchMockRequest request) {
		List<CommunicationMocked> listComms = daoManagerCommunications.listAll().stream().filter(Objects::nonNull)
			.filter( CommunicationMocked::isActivated)
			.filter( comm -> comm.getConnection().getHost().equals(request.getHost()) )
			.sorted( (a, b) -> Long.compare(a.getIndex(), b.getIndex()))
			.collect(Collectors.toList());
					
		for (CommunicationMocked communicationMocked : listComms) {
			try {
				String decodedInput = NetworkReaderUtility.decode(request.getRequest());
				String[] result = JSEvaluator.runScript(request.getRequest(), decodedInput, communicationMocked, null, null, null);
				if (result != null && result[1] != null) {
					MatchMockResponse response = new MatchMockResponse(NetworkReaderUtility.encode(result[1]));
					SmockerUI.log(Level.INFO, "Found mock match for " + 
							communicationMocked.getConnection().getHost() + SEP + communicationMocked.getConnection().getPort());
					return Response.ok(response).build();
				}
			} catch (SmockerException e) {
				SmockerUI.log(Level.SEVERE, "Unable To evaluate Script", e);
			}
		}	
		MatchMockResponse response = new MatchMockResponse(NO_MATCH);
		return Response.ok(response).build();
	}
	
}
