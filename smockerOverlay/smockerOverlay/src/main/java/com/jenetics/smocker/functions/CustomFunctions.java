package com.jenetics.smocker.functions;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.xml.sax.SAXException;

import com.jenetics.smocker.jseval.SmockerFunctionClass;
import com.jenetics.smocker.jseval.SmockerMethod;

@SmockerFunctionClass
public class CustomFunctions {
	
	@SmockerMethod
	public static String test(String input) 
			throws ParserConfigurationException, XPathExpressionException, SAXException, IOException {
		return "test";
	}
}
