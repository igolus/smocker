package com.jenetics.smocker.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 
 * @author igolus
 * Utility to read json field at root level
 */
public class SimpleJsonReader {
	
	private SimpleJsonReader() {
		super();
	}

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
			int afterDoubleDot = after.indexOf(':');
			String afterSecond = after.substring(afterDoubleDot + 1);
			
			if (afterSecond.startsWith("\"")) {
				int firstQuoteIndex = 1;
				int secondQuoteIndex = afterSecond.substring(1).indexOf('\"') + 1;
				return afterSecond.substring(firstQuoteIndex,secondQuoteIndex);
			}
			else {
				int firstIndex = 0;
				int secondIndex = afterSecond.indexOf(',');
				if (secondIndex == -1) {
					secondIndex =  afterSecond.indexOf('}');
				}
				return afterSecond.substring(firstIndex,secondIndex);
			}
		}
		return null;
	}
	
	public static Map<String, String> readMap(String json, String rootKey) {
		Map<String, String> ret = new HashMap<>();
		int startIndex = json.indexOf(rootKey);
		if (startIndex != -1) {
			String after = json.substring(startIndex);
			int afterDoubleDot = after.indexOf(':');
			String afterSecond = after.substring(afterDoubleDot + 1);
			String values = afterSecond.substring(afterSecond.indexOf('{') + 1, afterSecond.indexOf("}}"));
			if (values.equals("")) {
				return ret;
			}
			String[] allValues = values.split(",");
			for (int i = 0; i < allValues.length; i++) {
				String[] valuesArray = allValues[i].split("\":");
				if (valuesArray.length == 2) {
					String key = valuesArray[0];
					String value = valuesArray[1];
					
					if (key.length() >= 2) {
						key = key.substring(1, key.length());
					}
					ret.put(key, value);
				}
			}
			
			
		}
		return ret;
	}
	
	/**
	 * Read first value from key in one json string
	 * @param json
	 * @param key
	 * @return
	 */
	public static List<String> readValues(String json, String key) {
		int startIndex = json.indexOf(key);
		List<String> retValues = new ArrayList<>();
		if (startIndex != -1) {
			String after = json.substring(startIndex);
			int afterDoubleDot = after.indexOf(':');
			String afterSecond = after.substring(afterDoubleDot + 1);
			String values = afterSecond.substring(afterSecond.indexOf('[') + 1, afterSecond.indexOf(']'));
			if (values.equals("")) {
				return retValues;
			}
			String[] valuesArray = values.split(",");
			for (int i = 0; i < valuesArray.length; i++) {
				retValues.add(valuesArray[i].substring(1, valuesArray[i].length() - 1));
			}
			return retValues;
		}
		return new ArrayList<>();
	}
	
	public static List<List<String>> readValuesDoubleList(String json, String secondKey) {
		List<List<String>> ret = new ArrayList<>();
		List<String> readListValuesDoubleList = readListValuesDoubleList(json);
		for (String jsonList : readListValuesDoubleList) {
			ret.add(readValues(jsonList, secondKey));
		}
		return ret; 
	}

	public static List<String> readListValuesDoubleList(String json) {
		int startIndex = json.indexOf('[');
		int endIndex = json.lastIndexOf(']');
		
		List<String> retValues = new ArrayList<>();
		if (startIndex != -1 && endIndex != -1) {
			String listValues = json.substring(startIndex + 1, endIndex);
			String[] splittedValues = listValues.split("},");
			for (int i = 0; i < splittedValues.length - 1; i++) {
				retValues.add(splittedValues[i] + "}");
			}
			retValues.add(splittedValues[splittedValues.length - 1]);
			return retValues;
		}
		return new ArrayList<>();
	}
	

	
	public static String[] readSubResources(String json, String key) {
		//connections":[
		int startIndex = json.indexOf(key + '\"' + ":[");
		if (startIndex != -1) {
			String after = json.substring(startIndex + key.length() + 3);
			String[] values = findInBracket(after);
			return values;
			
		}
		return new String[] {};
	}

	/**
	 * Find all the items in the list like [{item1}, {item2}]
	 * @param toScan
	 * @return
	 */
	private static String[] findInBracket(String toScan) {
		List<String> listRet = new ArrayList<>();
		if (toScan.startsWith("]")) {
			return new String[] {};
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
