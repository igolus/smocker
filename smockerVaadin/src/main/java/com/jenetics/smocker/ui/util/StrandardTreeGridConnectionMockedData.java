package com.jenetics.smocker.ui.util;

import com.jenetics.smocker.model.ConnectionMocked;
import com.jenetics.smocker.model.JavaApplicationMocked;

public class StrandardTreeGridConnectionMockedData extends TreeGridConnectionData<JavaApplicationMocked, ConnectionMocked> {

	public StrandardTreeGridConnectionMockedData(JavaApplicationMocked javaApplication, ConnectionMocked connection) {
		super(javaApplication, connection);
	}

	@Override
	public String getConnectionType() {
		return null;
	}

	@Override
	public String getPort() {
		if (isConnection()) {
			return connection.getPort().toString();
		}
		else {
			return null;
		}
	}

	@Override
	public String getAdress() {
		if (isConnection()) {
			return connection.getHost();
		}
		else {
			return null;
		}
	}

	@Override
	public String getApplication() {
		if (isJavaApplication()) {
			return javaApplication.getClassQualifiedName();
		}
		else {
			return null;
		}
	}

	@Override
	public String getApplicationId() {
		return null;
	}
	
	

}
