package com.jenetics.smocker.functions;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.xml.sax.SAXException;

import com.jenetics.smocker.jseval.SmockerFunctionClass;
import com.jenetics.smocker.jseval.SmockerMethod;

@SmockerFunctionClass
public class CustomFunctions {
	
	@SmockerMethod
	public static String addHelloWordToHtml(String input, String host, int port) 
			throws ParserConfigurationException, XPathExpressionException, SAXException, IOException {
		if (input.indexOf("Accept: text/html") != -1) {
			Socket socket = new Socket(host, port);
			OutputStream output = socket.getOutputStream();
			output.write(input.getBytes());
			output.flush();
			
			InputStream inputStream = socket.getInputStream();
			InputStreamReader reader = new InputStreamReader(inputStream);
			
			int character;
            StringBuilder data = new StringBuilder();
 
            while ((character = reader.read()) != -1) {
                data.append((char) character);
            }
            
            String fullContent = data.toString();
            if (fullContent.indexOf("Content-Type: text/html") == -1) {
            	return null;
            }

            String content = UsefullFunctions.smockerRemoveHeader(fullContent);

            content = content.replaceFirst("<html>",   "<html><H1>HELLO FROM SMOCKER</H1>");
			String header = UsefullFunctions.smockerGetHeader(fullContent);
            
			fullContent = header + content;
			int length = UsefullFunctions.smockerContentLength(fullContent);
			fullContent = UsefullFunctions.smockerReplaceHeader(fullContent, "Content-Length", String.valueOf(length));
			socket.close();
			return fullContent;
		}
		
		return null;
	}
}
