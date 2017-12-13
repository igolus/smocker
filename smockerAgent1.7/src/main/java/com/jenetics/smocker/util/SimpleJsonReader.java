package com.jenetics.smocker.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 
 * @author igolus
 * Utility to read json field at root level
 */
public class SimpleJsonReader {
	
	/**
	 * Read first value from key in one json string
	 * @param json
	 * @param key
	 * @return
	 */
	public static String readValue(String json, String key) {
		int startIndex = json.indexOf(key);
		if (startIndex != -1) {
			String after = json.substring(startIndex);
			int afterDoubleDot = after.indexOf(":");
			String afterSecond = after.substring(afterDoubleDot + 1);
			
			if (afterSecond.startsWith("\"")) {
				int firstQuoteIndex = 1;
				int secondQuoteIndex = afterSecond.substring(1).indexOf("\"") + 1;
				return afterSecond.substring(firstQuoteIndex,secondQuoteIndex);
			}
			else {
				int firstIndex = 0;
				int secondIndex = afterSecond.indexOf(",");
				if (secondIndex == -1) {
					secondIndex =  afterSecond.indexOf("}");
				}
				return afterSecond.substring(firstIndex,secondIndex);
			}
		}
		return null;
	}
	
	
	public static String[] readSubResources(String json, String key) {
		//connections":[
		int startIndex = json.indexOf(key + "\"" + ":[");
		if (startIndex != -1) {
			String after = json.substring(startIndex + key.length() + 3);
			String[] values = findInBracket(after);
			return values;
			
		}
		return null;
	}

	/**
	 * Find all the items in the list like [{item1}, {item2}]
	 * @param toScan
	 * @return
	 */
	private static String[] findInBracket(String toScan) {
		List<String> listRet = new ArrayList<String>();
		if (toScan.startsWith("]")) {
			return null;
		}
		
		byte[] bytesToScan = toScan.getBytes();
		int openBrackets = 0;
		int  startIndex = 0;
		int  index = 0;
		for (int i = 0; i < bytesToScan.length; i++) {
			char currentChar = (char) bytesToScan[i];
			if (currentChar == '{') {
				openBrackets++;
			}
			if (currentChar == '}') {
				openBrackets--;
			}
			index++;
			if (openBrackets == 0) {
				listRet.add(toScan.substring(startIndex, index));
				//the end
				if ((char) bytesToScan[i+1] == ',') {
					i++;
					startIndex = ++index;
					continue;
				}
				else {
					break;
				}
			}
		}
		String[] ret = new String[listRet.size()];
		ret = listRet.toArray(ret);
		return ret;
	}
}
