package com.jenetics.smocker.functions;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.internal.filter.ValueNode.JsonNode;
import com.jenetics.smocker.jseval.SmockerFunctionClass;
import com.jenetics.smocker.jseval.SmockerJsEnv;
import com.jenetics.smocker.jseval.SmockerMethod;
import com.jenetics.smocker.util.NetworkReaderUtility;

@SmockerFunctionClass
public class UsefullFunctions {
	
	@SmockerMethod
	public static String smockerXpath(String input, String xpathExpression) 
			throws ParserConfigurationException, XPathExpressionException, SAXException, IOException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document doc = builder.parse(new InputSource(new StringReader(input)));
		XPathFactory xPathfactory = XPathFactory.newInstance();
		XPath xpath = xPathfactory.newXPath();
		XPathExpression expr = xpath.compile(xpathExpression);
		String res = null;
		return expr.evaluate(doc);
	}
	
	@SmockerMethod
	public static String smockerJsonPath(String input, String jsonPath) {
		Object path = JsonPath.read(input, jsonPath);
		return path.toString();
	}
	
	/**
	 * Add a key value to the smocker env map
	 * @param key
	 * @param value
	 */
	@SmockerMethod
	public static void smockerAddKeyValueMap (String key, String value) {
		SmockerJsEnv.getInstance().addToMap(key, value);
	}
	
	/**
	 * Get map value in smocker env map from key
	 * @param key
	 * @return
	 */
	@SmockerMethod
	public static String smockerGetValueMap (String key) {
		return SmockerJsEnv.getInstance().getFromMap(key);
	}
	
	/**
	 * REmove entry in smocker env map from key
	 * @param key
	 */
	@SmockerMethod
	public static void smockerRemoveEntryMap (String key) {
		SmockerJsEnv.getInstance().removeFromMap(key);
	}
	
	/**
	 * encode in base 64
	 * @param clear string value
	 * @return encoded value
	 */
	@SmockerMethod
	public static String smockerBtoa (String value) {
		return NetworkReaderUtility.encode(value);
	}
	
	/**
	 * decode in base 64
	 * @param value encoded string value
	 * @return decoded value
	 */
	@SmockerMethod
	public static String smockerAtob (String key) {
		return NetworkReaderUtility.decode(key);
	}
	
	/**
	 * Format json string
	 * @param json
	 * @return formatted json
	 * @throws IOException 
	 */
	@SmockerMethod
	public static String smockerFormatJson (String json) throws IOException {
		if (json.isEmpty()) {
			return "";
		}
		ObjectMapper mapper = new ObjectMapper();
		Object jsonObj = mapper.readValue(json, Object.class);
		return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonObj);
	}
	
	/**
	 * format Xml
	 * @param xml
	 * @return formatted xml
	 * @throws TransformerException 
	 */
	@SmockerMethod
	public static String smockerFormatXML (String xml, int indent) throws TransformerException {
		if (xml.isEmpty()) {
			return "";
		}
		Source xmlInput = new StreamSource(new StringReader(xml));
        StringWriter stringWriter = new StringWriter();
        StreamResult xmlOutput = new StreamResult(stringWriter);
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        transformerFactory.setAttribute("indent-number", indent);
        Transformer transformer = transformerFactory.newTransformer(); 
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.transform(xmlInput, xmlOutput);
        return xmlOutput.getWriter().toString();
	}
	
	/**
	 * remove header content
	 * @param fullConntent
	 * @return
	 */
	@SmockerMethod
	public static String smockerRemoveHeader (String fullContent) {
		int indexDoubleRet = fullContent.indexOf("\r\n\r\n");
		if (indexDoubleRet != -1) {
			return fullContent.substring(indexDoubleRet + 4);
		}
		indexDoubleRet = fullContent.indexOf("\n\n");
		if (indexDoubleRet != -1) {
			return fullContent.substring(indexDoubleRet + 2);
		}
		return fullContent;
	}
	
	/**
	 * get header content
	 * @param fullConntent
	 * @return
	 */
	@SmockerMethod
	public static String smockerGetHeader (String fullContent) {
		int indexDoubleRet = fullContent.indexOf("\r\n\r\n");
		if (indexDoubleRet != -1) {
			return fullContent.substring(0, indexDoubleRet + 4);
		}
		indexDoubleRet = fullContent.indexOf("\n\n");
		if (indexDoubleRet != -1) {
			return fullContent.substring(0, indexDoubleRet + 2);
		}
		return fullContent;
	}
	
	
	/**
	 * adjust header content length
	 * @param fullConntent
	 * @return
	 */
	@SmockerMethod
	public static String smockerAdjustContentLength (String fullContent) {
		return smockerReplaceHeader(fullContent, "Content-Length",String.valueOf(smockerContentLength(fullContent)));
	}
	
	
	/**
	 * remove header content
	 * @param fullConntent
	 * @return
	 */
	@SmockerMethod
	public static int smockerContentLength (String fullContent) {
		return smockerRemoveHeader(fullContent).length();
	}
	
	/**
	 * remove header content
	 * @param fullConntent
	 * @return
	 */
	@SmockerMethod
	public static String smockerReplaceHeader (String fullContent, String headerName, String value) {
		if (fullContent.indexOf(headerName) != -1) {
			String regExp = headerName + ".+";
			fullContent = fullContent.replaceFirst(regExp, headerName + ": " + value);
		}
		return fullContent;
	}
	
	/**
	 * remove header content
	 * @param fullConntent
	 * @return
	 * @throws IOException 
	 */
	@SmockerMethod
	public static void smockerWriteToDisk (String content, String filePath) throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(filePath));
	    writer.write(content);
	    writer.close();
	}
	
	
	/**
	 * get the value of a query parameter
	 * @param fullConntent
	 * @return
	 * @throws IOException 
	 * @throws URISyntaxException 
	 */
	@SmockerMethod
	public static String smockerGetQueryParam (String input, String paramName) throws IOException, URISyntaxException {
		String[] split = input.split(" ");
		if (split.length > 1) {
			String url = split[1];
			List<NameValuePair> params = URLEncodedUtils.parse(new URI(url), "UTF-8");
			return params.stream().filter(p -> p.getName().equals(paramName)).map(NameValuePair::getValue).findFirst().orElse(null);
		}
		return null;
	}
	
}
