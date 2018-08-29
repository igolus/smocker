package com.jenetics.smocker.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Base64;

import org.jboss.logging.Logger;

public class NetworkReaderUtility {

	private NetworkReaderUtility() {
		super();
		// TODO Auto-generated constructor stub
	}

	private static Logger logger = Logger.getLogger(NetworkReaderUtility.class);

	public final static String HEADER_CONTENT_TYPE = "Content-Type";
	public final static String CONTENT_TYPE_JSON = "application/json";

	public static String decode(String code) {
		String ret = null;
		if (code != null) {
			ret = new String(Base64.getDecoder().decode(code));
		}
		return ret;
	}
	
	public static String encode(String code) {
		String ret = null;
		if (code != null) {
			ret = new String(Base64.getEncoder().encode(code.getBytes()));
		}
		return ret;
	}

	/**
	 * Get the content in the response
	 * 
	 * @param response
	 * @return
	 */
	public static String readContentResponse(String response) {

		int indexSep = response.indexOf(System.getProperty("line.separator") + System.getProperty("line.separator"));
		if (indexSep != -1) {
			return response.substring(indexSep + 2);
		}
		return null;
	}

	/**
	 * Get the content in the response
	 * 
	 * @param response
	 * @return
	 */
	public static String getJsonContent(String content) {

		byte[] bytesContent = content.getBytes();
		int openBracketNb = 0;
		boolean inJson = false;
		int indexStart = 0;
		int indexEnd = 0;
		for (int i = 0; i < bytesContent.length; i++) {

			if (((char) bytesContent[i]) == '{') {
				openBracketNb++;
				if (!inJson) {
					indexStart = i;
					inJson = true;
				}

			} else if (((char) bytesContent[i]) == '}') {
				openBracketNb--;
			}
			if (inJson && openBracketNb == 0) {
				indexEnd = i;
			}
		}
		if (indexEnd > 0) {
			return content.substring(indexStart, indexEnd - 1);
		}
		return null;
	}

	/**
	 * Read a value from header
	 * 
	 * @param response
	 * @param key
	 * @return
	 */
	public static String readHeaderValue(String response, String key) {
		BufferedReader reader = new BufferedReader(new StringReader(response));
		try {
			return readLineWithKey(key, reader);
		} catch (IOException e) {
			logger.error("Unable to read header", e);
		}
		return null;
	}

	private static String readLineWithKey(String key, BufferedReader reader) throws IOException {
		String line;
		while ((line = reader.readLine()) != null && line != "") {
			if (line.startsWith(key)) {
				String[] splitdot = line.split(":");
				if (splitdot.length > 1) {
					return splitdot[1];
				}
			}
		}
		return null;
	}

}
