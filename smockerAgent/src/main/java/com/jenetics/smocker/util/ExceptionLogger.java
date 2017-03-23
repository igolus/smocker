package com.jenetics.smocker.util;

public class ExceptionLogger {
	
	/**
	 * Log exception
	 * @param t
	 */
	public static void logThrowable (Throwable t) {
		t.printStackTrace();
	}
	
	/**
	 * Log exception
	 * @param t
	 */
	public static void logMessage (String message) {
		System.out.println("Smocker : " + message);
	}
}
