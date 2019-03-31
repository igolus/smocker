package com.jenetics.smocker.util;

import java.util.logging.Level;
import java.util.logging.Logger;

public class MessageLogger {
	private static final String SMOCKER_HEAD = "SMOCKER -- ";

	private MessageLogger() {
		super();
	}

	/**
	 * Log exception
	 * @param t
	 */
	public static void logThrowable (Throwable t, Class<?> source) {
		Logger logger = Logger.getLogger(source.getName());
		logger.log(Level.SEVERE, SMOCKER_HEAD, t);
	}
	
	/**
	 * Log exception
	 * @param t
	 */
	public static void logMessage (String message, Class<?> source) {
		Logger logger = Logger.getLogger(source.getName());
		logger.log(Level.INFO, SMOCKER_HEAD + message);
	}
	
	/**
	 * Log exception
	 * @param t
	 */
	public static void logError (String message, Class<?> source) {
		Logger logger = Logger.getLogger(source.getName());
		logger.log(Level.SEVERE, SMOCKER_HEAD + message);
	}
	
	/**
	 * Log exception
	 * @param t
	 */
	public static void logErrorWithMessage (String message, Throwable t, Class<?> source) {
		Logger logger = Logger.getLogger(source.getName());
		logger.log(Level.SEVERE, SMOCKER_HEAD + message, t);
	}
}
