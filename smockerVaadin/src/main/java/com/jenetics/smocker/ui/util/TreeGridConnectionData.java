package com.jenetics.smocker.ui.util;

public abstract class TreeGridConnectionData<T, U> {
	
	protected T javaApplication; 
	protected U connection;
	
	public TreeGridConnectionData() {
		super();
	} 
	
	public TreeGridConnectionData(T javaApplication, U connection) {
		super();
		this.javaApplication = javaApplication;
		this.connection = connection;
	} 
	
	public boolean isConnection() {
		return this.connection != null;
	}  
	
	public boolean isJavaApplication() {
		return this.javaApplication != null;
	}  
	
	public T getJavaApplication() {
		return javaApplication;
	}

	public U getConnection() {
		return connection;
	}

	public abstract String getConnectionType();
	public abstract String getPort();
	public abstract String getAdress();
	public abstract String getApplication();


}
