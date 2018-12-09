package com.jenetics.smocker.jseval;

import java.util.HashMap;

public class SmockerJsEnv {
	
	private static SmockerJsEnv instance = null;
	
	private HashMap<String, String> globalMap = new HashMap<>();
	
	private SmockerJsEnv() {
	}
	
	public static SmockerJsEnv getInstance() {
		if (instance == null) {
			instance = new SmockerJsEnv();
		}
		return instance;
	}
	
	public void addToMap(String key, String value) {
		globalMap.put(key, value);
	}
	
	public String getFromMap(String key) {
		return globalMap.get(key);
	}
	
	public void removeFromMap(String key) {
		globalMap.remove(key);
	}
	
	public void clear() {
		globalMap.clear();
	}
}
