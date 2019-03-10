package com.jenetics.smocker.util;

public class SmockerRuntimeException extends RuntimeException {

	public SmockerRuntimeException() {
		super();
	}

	public SmockerRuntimeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public SmockerRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public SmockerRuntimeException(String message) {
		super(message);
	}

	public SmockerRuntimeException(Throwable cause) {
		super(cause);
	}
	

}
