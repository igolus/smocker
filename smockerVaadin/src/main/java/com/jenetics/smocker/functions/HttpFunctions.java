package com.jenetics.smocker.functions;

import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.ParseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.jenetics.smocker.jseval.SmockerFunctionClass;
import com.jenetics.smocker.jseval.SmockerMethod;

@SmockerFunctionClass
public class HttpFunctions {

	@SmockerMethod
	public static String smockerHttpGet(String targetUrl, String headerLine) 
			throws ParseException, IOException 
	{
		HttpGet request = new HttpGet(targetUrl);
		CloseableHttpClient httpClient = HttpClients.createDefault();
		// add request headers
		if (headerLine != null) {
			String[] headers = headerLine.split("/");
			for (int i = 0; headers != null && i < headers.length; i++) {
				String[] headerInfo = headerLine.split(":");
				if (headerInfo.length == 2) {
					request.addHeader(headerInfo[0], headerInfo[1]);
				}
			}
		}
		
		CloseableHttpResponse response = httpClient.execute(request);
		
		StringBuffer sb = new StringBuffer();
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
}
