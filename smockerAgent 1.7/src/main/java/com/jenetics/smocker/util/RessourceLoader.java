package com.jenetics.smocker.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;


public class RessourceLoader {
	
	private static final String SOURCE_COPY = "sourceCopy";

	/**
	 * Load a piece of javassit source code from resources/sourceCopy
	 * @param resFileName
	 * @return
	 */
	public static String loadJavassistSource(String resFileName) {
		return getSourceFromRessources(SOURCE_COPY, resFileName);
	}
	
	public static String readFile(String fileName) throws IOException {
		java.io.InputStream is = RessourceLoader.class.getResourceAsStream("/" + SOURCE_COPY + "/" + fileName);
	    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
	    String         line = null;
	    StringBuilder  stringBuilder = new StringBuilder();
	    String         ls = System.getProperty("line.separator");

	    try {
	        while((line = reader.readLine()) != null) {
	            stringBuilder.append(line);
	            stringBuilder.append(ls);
	        }

	        return stringBuilder.toString();
	    } finally {
	        reader.close();
	    }
	}
	
	private static String getSourceFromRessources(String folder, String fileName) {
		java.io.InputStream is = RessourceLoader.class.getResourceAsStream("/" + folder + "/" + fileName);
		return getStringFromInputStream(is);
	}

	// convert InputStream to String
	private static String getStringFromInputStream(java.io.InputStream is) {

		BufferedReader br = null;
		StringBuilder sb = new StringBuilder();

		String line;
		try {

			br = new BufferedReader(new InputStreamReader(is));
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return sb.toString();

	}
}
