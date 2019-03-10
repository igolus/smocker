package com.jenetics.smocker.util.network;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jenetics.smocker.util.MessageLogger;
import com.jenetics.smocker.util.SimpleJsonReader;


public class RemoteServerChecker {

	private static final String SEP = ":";
	private static final int CHECKER_PERIOD = 1000;
	private static RemoteServerChecker instance;
	private static boolean remoteServerAlive = false;
	private static List<String> mockedHosts = new ArrayList<>();
	private static List<String>	ignoredHosts = new ArrayList<>();
	private static List<List<String>> duplicatedHosts = new ArrayList<>();
	private static List<String> unWatchedHost = new ArrayList<>();
	private static Map<String, String> listConnectionsReferenced = new HashMap<>();
	
	private static long javaAppId = -1;
	private static String existingId = null;
	
	private RemoteServerChecker() {
		super();
	}
	
	public static List<String> getMockedHosts() {
		return mockedHosts;
	}
	
	public static List<String> getIgnoredHosts() {
		return ignoredHosts;
	}

	public static List<List<String>> getDuplicatedHosts() {
		return duplicatedHosts;
	}

	public static boolean isJavaAppIdentified() {
		return javaAppId != -1;
	}
	
	public static Long idByConnectionRererenced(String host, int port) {
		if (listConnectionsReferenced != null) {
			String value = listConnectionsReferenced.get(host + ":" + port);
			return value == null ? null : Long.valueOf(value);
		}
		return null;
	}
	
	public static long getJavaAppId() {
		return javaAppId;
	}

	public static List<String> getUnWatchedHost() {
		return unWatchedHost;
	}

	public static boolean isRemoteServerAlive() {
		return remoteServerAlive;
	}

	public static synchronized RemoteServerChecker getInstance() {
		if (instance == null) {
			instance = new RemoteServerChecker();
			instance.startChecker();
		}
		return instance;
	}
	
	public static boolean isConnectionWatched(String host, int port) {
		boolean isHostIgnored = ignoredHosts == null ? false : ignoredHosts.contains(host);
		return !isHostIgnored && (unWatchedHost == null || !unWatchedHost.contains(host + SEP + port));
	}


	public void startChecker() {
		Runnable checkerTask = new Runnable() {
			public void run() {
				while (true) {
					try {
						Thread.sleep(CHECKER_PERIOD);
						boolean alive = RestClientSmocker.getInstance().checkAlive();
						if (alive) {
							String responseHost = RestClientSmocker.getInstance().getAllMockedConnection();
							if (responseHost != null) {
								mockedHosts = SimpleJsonReader.readValues(responseHost, "activatedHosts");
							}
							else {
								remoteServerAlive = false;
							}
							
							String responseIgnoredHosts = RestClientSmocker.getInstance().getAllIgnoredHosts();
							if (responseIgnoredHosts != null) {
								ignoredHosts = SimpleJsonReader.readValues(responseIgnoredHosts, "listIgnoredList");
							} 
							
							String responseDupHosts = RestClientSmocker.getInstance().getAllDupHosts();
							if (responseDupHosts != null) {
								duplicatedHosts = SimpleJsonReader.readValuesDoubleList(responseDupHosts, "listDupHost");
							}
							
							String listHostWatched = RestClientSmocker.getInstance().getAllUnWachedConnections();
							if (listHostWatched != null) {
								unWatchedHost = SimpleJsonReader.readValues(listHostWatched, "activatedHosts");
							}
							
							String allResponse = RestClientSmocker.getInstance().getAll();
							if (allResponse != null) {
								existingId = ResponseReader.findExistingAppId(allResponse);
							}
							
							if (existingId != null) {
								javaAppId = Long.valueOf(existingId);
							}
							
							if (existingId == null || javaAppId == -1) {
								updateJavaAppId();
							}
							listConnectionsReferenced = 
									RestClientSmocker.getInstance().getListConnection(javaAppId);
						}
						if (alive && !remoteServerAlive) {
							MessageLogger.logMessage("Remote server alive", RemoteServerChecker.class);
							remoteServerAlive = true;
						}
						else if (!alive && remoteServerAlive){
							MessageLogger.logMessage("Remote server down", RemoteServerChecker.class);
							remoteServerAlive = false;
						}
					} catch (Exception e) {
						MessageLogger.logErrorWithMessage("Unable to check remote server", e, RemoteServerChecker.class);
					}
				}
			}
		};
		Thread checkerThread = new Thread(checkerTask);
		checkerThread.start();
	}
	
	private synchronized static void updateJavaAppId() {
		String response = RestClientSmocker.getInstance().postJavaApp();
		if (response != null) {
			String id = ResponseReader.readValueFromResponse(response, "id");
			if (id != null) {
				javaAppId = Long.parseLong(id);
			}
			listConnectionsReferenced.clear();
		}
	}

	public synchronized void addConnectionRef(Long idConnection, String host, int port) {
		if (listConnectionsReferenced == null) {
			listConnectionsReferenced = new HashMap<>();
		}
		listConnectionsReferenced.put(host + ":" + port, idConnection.toString());	
	}

}
