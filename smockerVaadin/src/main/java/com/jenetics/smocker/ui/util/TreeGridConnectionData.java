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
	
//	protected static final String CONNECTION_TYPE = bundle.getString("ConnectionType");
//	protected static final String PORT = bundle.getString("Port");
//	protected static final String ADRESS = bundle.getString("Adress");
//	protected static final String APPLICATION = bundle.getString("Application");
//	protected static final String ALL = "all";
	
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
	
//	public TreeGridConnectionData(U connection) {
//		super();
//		this.connection = connection;
//	} 
	

}
