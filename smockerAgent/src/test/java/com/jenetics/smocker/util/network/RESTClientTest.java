package com.jenetics.smocker.util.network;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class RESTClientTest {
	@Test
	public void testPostConnection() {
		RESTClient restClient = new RESTClient("localhost", 8080, "/smocker/rest/connections");
		Map<String,String> headers = new HashMap<String,String>();
		headers.put("Content-Type", "application/json");
		String post = null;
		try {
			post = restClient.post("{\"id\": 0, \"version\": 0, \"host\": \"toto\",  \"port\": 10}", headers, RESTClient.POST);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(post);
	}
}
