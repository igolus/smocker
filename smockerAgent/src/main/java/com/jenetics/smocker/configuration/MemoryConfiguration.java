package com.jenetics.smocker.configuration;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.jenetics.smocker.configuration.util.ConnectionBehavior;
import com.jenetics.smocker.util.MessageLogger;

public class MemoryConfiguration {
  
	public static final String SEP_CONNECTION = ".";
	private static Map<String, ConnectionBehavior> connectionsWatched = new HashMap<>();
	
	public static void setConnecctionWatched(String host, int port) {
		//if (connectionWatched.contains(o))
		String key = getKey(host, port);
		ConnectionBehavior connectionBehavior = new ConnectionBehavior(true);
		
		if (connectionsWatched.get(key) == null) {
			connectionsWatched.put(key, new ConnectionBehavior(true));
		}
		else {
			connectionsWatched.get(key).setWatched(true);
		}

	} 
	
	public static void setConnecctionMute(String host, int port) {
		String key = getKey(host, port);
		if (connectionsWatched.get(key) != null) {
			connectionsWatched.get(key).setWatched(false);
		}
		else {
			MessageLogger.logError("Call first setConnecctionWatched", MemoryConfiguration.class);
		}
	}

	private static String getKey(String host, int port) {
		return host + SEP_CONNECTION + port;
	} 
	
	public static boolean isConnecctionThere(String host, int port) {
		String key = getKey(host, port);
		return connectionsWatched.get(key) != null;
	}

	
	public static boolean isConnecctionWatched(String host, int port) {
		String key = getKey(host, port);
		if (connectionsWatched.get(key) != null) {
			return connectionsWatched.get(key).isWatched(); 
		}
		return false;
	} 
	
  }
