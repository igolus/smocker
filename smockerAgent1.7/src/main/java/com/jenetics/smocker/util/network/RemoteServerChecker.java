package com.jenetics.smocker.util.network;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jenetics.smocker.util.HostAndPortRangeModel;
import com.jenetics.smocker.util.MessageLogger;
import com.jenetics.smocker.util.SimpleJsonReader;


public class RemoteServerChecker {

	private static final String SEP = ":";
	private static final int CHECKER_PERIOD = 1000;
	private static RemoteServerChecker instance;
	private static boolean remoteServerAlive = false;
	private static List<String> mockedHosts = new ArrayList<>();
	private static List<HostAndPortRangeModel>	excludedHosts = new ArrayList<>();
	private static List<HostAndPortRangeModel>	includedHosts = new ArrayList<>();
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

	public static List<HostAndPortRangeModel> getExcludedHosts() {
		return excludedHosts;
	}

	public static List<HostAndPortRangeModel> getIncludedHosts() {
		return includedHosts;
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
		boolean doNotfilter = true;
		if (!includedHosts.isEmpty()) {
			doNotfilter = false;
			for (HostAndPortRangeModel hostAndPortRangeModel : includedHosts) {
				if (isMatching(hostAndPortRangeModel, host, port)) {
					doNotfilter = true;
					break;
				}
			}
		}
		
		if (!excludedHosts.isEmpty() && doNotfilter) {
			for (HostAndPortRangeModel hostAndPortRangeModel : excludedHosts) {
				if (isMatching(hostAndPortRangeModel, host, port)) {
					doNotfilter = false;
					break;
				}
			}
		}
		
		return doNotfilter && (unWatchedHost == null || !unWatchedHost.contains(host + SEP + port));
	}


	private static boolean isMatching(HostAndPortRangeModel hostAndPortRangeModel, String host, int port) {
		if (hostAndPortRangeModel.getMinPort() == -1) {
			return hostAndPortRangeModel.getHost().equals(host);
		}
		if (hostAndPortRangeModel.getMaxPort() == 0) {
			return hostAndPortRangeModel.getHost().equals(host) 
					&& hostAndPortRangeModel.getMinPort() == port;
		}
		else {
			return hostAndPortRangeModel.getHost().equals(host) 
					&& port >= hostAndPortRangeModel.getMinPort()
					&& port <= hostAndPortRangeModel.getMaxPort();
		}
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
							
							excludedHosts.clear();
							String responseExcludedHosts = RestClientSmocker.getInstance().getAllExcludedHosts();
							if (responseExcludedHosts != null) {
								List<String> listExcluded = SimpleJsonReader.readListValuesDoubleList(responseExcludedHosts);
								if (listExcluded != null) {
									for (String jsonSingle : listExcluded) {
										HostAndPortRangeModel hostAndPortRangeModel = readHostAndPortRangeModel(jsonSingle);
										if (hostAndPortRangeModel != null) {
											excludedHosts.add(hostAndPortRangeModel);
										}
									}
								}
							} 
							
							includedHosts.clear();
							String responseIncludedHosts = RestClientSmocker.getInstance().getAllIncludedHosts();
							if (responseIncludedHosts != null) {
								List<String> listIncluded = SimpleJsonReader.readListValuesDoubleList(responseIncludedHosts);
								if (listIncluded != null) {
									for (String jsonSingle : listIncluded) {
										HostAndPortRangeModel hostAndPortRangeModel = readHostAndPortRangeModel(jsonSingle);
										if (hostAndPortRangeModel != null) {
											includedHosts.add(hostAndPortRangeModel);
										}
									}
								}
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

			private HostAndPortRangeModel readHostAndPortRangeModel(String jsonSingle) {
				try {
					if (!jsonSingle.isEmpty()) {
						String host = SimpleJsonReader.readValue(jsonSingle, "host");
						int minPort = Integer.parseInt(SimpleJsonReader.readValue(jsonSingle, "minPort"));
						int maxPort = Integer.parseInt(SimpleJsonReader.readValue(jsonSingle, "maxPort"));
						
						HostAndPortRangeModel hostAndPortRangeModel = new HostAndPortRangeModel(host, minPort, maxPort);
						return hostAndPortRangeModel;
					}
				}
				catch (NumberFormatException ex) {
					MessageLogger.logThrowable(ex, getClass());
				}
				return null;
				
			}
		};
		Thread checkerThread = new Thread(checkerTask);
		checkerThread.start();
	}
	
	private static synchronized void updateJavaAppId() {
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
		if (idConnection != null) {
			listConnectionsReferenced.put(host + ":" + port, idConnection.toString());	
		}
		
	}

}
