package com.jenetics.smocker.ui.util;

import com.jenetics.smocker.model.Connection;
import com.jenetics.smocker.model.JavaApplication;

public class StrandardTreeGridConnectionData extends TreeGridConnectionData<JavaApplication, Connection> {

	public StrandardTreeGridConnectionData(JavaApplication javaApplication, Connection connection) {
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

}
