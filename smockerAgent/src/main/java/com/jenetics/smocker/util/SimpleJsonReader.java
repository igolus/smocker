package com.jenetics.smocker.util;

/**
 * 
 * @author igolus
 * Utility to read json field at root level
 */
public class SimpleJsonReader {
	public static String readValue(String json, String key) {
		int startIndex = json.indexOf(key);
		if (startIndex != -1) {
			String after = json.substring(startIndex);
			int endIndex =  after.indexOf(",");
			return json.substring(startIndex + 4, endIndex + 2);
		}
		return null;
	}
}
