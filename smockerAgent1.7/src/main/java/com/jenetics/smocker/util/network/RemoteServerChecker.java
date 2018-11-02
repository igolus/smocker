package com.jenetics.smocker.util.network;

import java.util.ArrayList;
import java.util.List;

import com.jenetics.smocker.util.MessageLogger;
import com.jenetics.smocker.util.SimpleJsonReader;


public class RemoteServerChecker {

	private static final int CHECKER_PERIOD = 3000;
	private static RemoteServerChecker instance;
	private static boolean remoteServerAlive = false;
	private static List<String> mockedHost = new ArrayList<>();
	
	private RemoteServerChecker() {
	}
	
	public static List<String> getMockedHost() {
		return mockedHost;
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


	public void startChecker() {

		Runnable checkerTask = new Runnable() {
			public void run() {
				while (true) {
					try {
						Thread.sleep(CHECKER_PERIOD);
						boolean alive = RestClientSmocker.getInstance().checkAlive();
						if (alive && !remoteServerAlive) {
							MessageLogger.logMessage("Remote server alive", RemoteServerChecker.class);
							remoteServerAlive = true;
						}
						else if (!alive && remoteServerAlive){
							MessageLogger.logMessage("Remote server down", RemoteServerChecker.class);
							remoteServerAlive = false;
						}
						
						if (remoteServerAlive) {
							String responseHost = RestClientSmocker.getInstance().getAllMockedConnection();
							if (responseHost != null) {
								mockedHost = SimpleJsonReader.readValues(responseHost, "activatedHosts");
							}
							else {
								remoteServerAlive = false;
							}
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
}
