package com.jenetics.smocker.functions;

import java.io.IOException;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.ParseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.jenetics.smocker.jseval.SmockerFunctionClass;
import com.jenetics.smocker.jseval.SmockerMethod;

@SmockerFunctionClass
public class HttpFunctions {

	@SmockerMethod
	public static String smockerHttpGet(String targetUrl, String headerLine) 
			throws ParseException, IOException 
	{
		HttpGet request = new HttpGet(targetUrl);
		try (CloseableHttpClient httpClient = HttpClients.createDefault()){
			// add request headers
			addHeaders(headerLine, request);
			CloseableHttpResponse response = httpClient.execute(request);
			return getStringFromResponse(response);
		}
	}

	@SmockerMethod
	public static String smockerHttpPostJson(String url, String headerLine, String body) throws IOException {
		try (CloseableHttpClient httpClient = HttpClients.createDefault()){
			HttpPost post = new HttpPost(url);
			addHeaders(headerLine, post);
			StringBuilder json = new StringBuilder();
			json.append(body);

			post.setEntity(new StringEntity(json.toString()));
			CloseableHttpResponse response = httpClient.execute(post);
			httpClient.close();
			return getStringFromResponse(response);
		}
	}


	private static String getStringFromResponse(CloseableHttpResponse response) throws IOException {
		StringBuilder sb = new StringBuilder();
		Header[] allHeaders = response.getAllHeaders();
		for (int i = 0; i < allHeaders.length; i++) {
			sb.append(allHeaders[i].toString()).append(System.lineSeparator());
		}
		sb.append(System.lineSeparator());

		HttpEntity entity = response.getEntity();
		if (entity != null) {
			// return it as a String
			sb.append(EntityUtils.toString(entity));

			return sb.toString();
		}
		return null;
	}


	private static void addHeaders(String headerLine, HttpRequestBase request) {
		if (headerLine != null) {
			String[] headers = headerLine.split("|");
			for (int i = 0; headers != null && i < headers.length; i++) {
				String[] headerInfo = headerLine.split(":");
				if (headerInfo.length == 2) {
					request.addHeader(headerInfo[0], headerInfo[1]);
				}
			}
		}
	}



}
