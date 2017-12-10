package com.jenetics.smocker.configuration;

import com.jenetics.smocker.util.MessageLogger;

/**
 * Used to parse special system properties
 * @author igolus
 *
 */
public class SystemPropertyConfiguration {
	private static final String SMOCKER_TARGET_HOST = "SMOCKER_TARGET_HOST";
	private static final String SMOCKER_TARGET_PORT = "SMOCKER_TARGET_PORT";
	private static final String SMOCKER_COMM_PORT = "SMOCKER_COMM_PORT";
	
	private static final String SMOCKER_DEFAULT_TARGET_HOST = "localhost";
	private static final int SMOCKER_DEFAULT_TARGET_PORT = 8080;
	private static final int SMOCKER_DEFAULT_COMM_PORT = 2024;

	public static String getTargetHost() {
		String targetHost = System.getProperty(SMOCKER_TARGET_HOST);
		return targetHost == null ? SMOCKER_DEFAULT_TARGET_HOST : targetHost;
	}
	
	public static int getTargetPort() {
		String targetPort = System.getProperty(SMOCKER_TARGET_PORT);
		int targetPortInt = SMOCKER_DEFAULT_COMM_PORT;
		if (targetPort != null) {
			try {
				targetPortInt = Integer.parseInt(targetPort);
			}
			catch (NumberFormatException e) {
				MessageLogger.logErrorWithMessage(
						"Bad format for property SMOCKER_TARGET_HOST, should be an integer", e, SystemPropertyConfiguration.class);
				
			}
		}
		return targetPortInt;
	}
	
	public static int getCommPort() {
		String commPort = System.getProperty(SMOCKER_COMM_PORT);
		int targetCommPortInt = SMOCKER_DEFAULT_COMM_PORT;
		if (commPort != null) {
			try {
				targetCommPortInt = Integer.parseInt(commPort);
			}
			catch (NumberFormatException e) {
				MessageLogger.logErrorWithMessage(
						"Bad format for property SMOCKER_DEFAULT_COMM_PORT, should be an integer", e,  SystemPropertyConfiguration.class);
				
			}
		}
		return targetCommPortInt;
	}
}
