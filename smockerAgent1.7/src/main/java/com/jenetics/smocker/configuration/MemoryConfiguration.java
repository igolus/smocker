package com.jenetics.smocker.configuration;

import java.util.HashMap;
import java.util.Map;

import com.jenetics.smocker.configuration.util.ConnectionBehavior;
import com.jenetics.smocker.configuration.util.ConnectionMockedBehavior;
import com.jenetics.smocker.configuration.util.MockCconnectionMode;
import com.jenetics.smocker.util.MessageLogger;

public class MemoryConfiguration {
  
	public static final String SEP = "%*%";
	private static Map<String, ConnectionBehavior> connectionsWatched = new HashMap<String, ConnectionBehavior>();
	private static Map<String, MockCconnectionMode> connectionsMockedModes = new HashMap<String, MockCconnectionMode>();
	private static Map<String, String> headerReplace = new HashMap<String, String>();
	
	
	private static boolean replayMode;
	
	public static boolean isReplayMode() {
		return replayMode;
	}

	public static void addHeaderReplace (String regExp, String replace) {
		headerReplace.put(regExp, replace);
	}
	
	public static Map<String, String> getHeaderReplace() {
		return headerReplace;
	}

	public static void setConnecctionWatched(String host, int port) {
		//if (connectionWatched.contains(o))
		String key = getKey(host, port);
		if (connectionsWatched.get(key) == null) {
			connectionsWatched.put(key, new ConnectionBehavior(true));
		}
		else {
			connectionsWatched.get(key).setWatched(true);
		}
	} 
	
	
	public static void setConnecctionMute(String host, int port) {
		String key = getKey(host, port);
		if (connectionsWatched.get(key) == null) {
			connectionsWatched.put(key, new ConnectionBehavior(false));
		}
		else {
			connectionsWatched.get(key).setWatched(false);
		}
	}
	
	public static void addMap(Map<String, ConnectionBehavior> mapConnections) {
		for (Map.Entry<String, ConnectionBehavior> entry : mapConnections.entrySet()) {
			if (connectionsWatched.get(entry.getKey()) != null) {
				connectionsWatched.remove(entry.getKey());
			}
			connectionsWatched.put(entry.getKey(), entry.getValue());
		}
	} 


	private static String getKey(String host, int port) {
		return host + SEP + port;
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

	public static MockCconnectionMode getConnectionMode(String host, int port) {
		String key = getKey(host, port);
		return connectionsMockedModes.get(key);
	}

	public static void setConnectionMode(String host, int port, String mode) {
		String key = getKey(host, port);
		if (connectionsMockedModes.get(key) == null) {
			MockCconnectionMode connectionMockedBehavior = getConnectionMockedBehavior(mode);
			connectionsMockedModes.put(key, connectionMockedBehavior);
		}
	}

	private static MockCconnectionMode getConnectionMockedBehavior(String mode) {
		MockCconnectionMode modeEnum =  MockCconnectionMode.valueOf(mode);
		if (modeEnum == MockCconnectionMode.DISABLED) {
			connectionsMockedModes.remove(modeEnum);
		}
		else {
			return modeEnum;
		}
		return null;
	} 
	
  }
