package java.net;

import java.io.OutputStream;
import java.net.SocketImpl;
import java.security.PrivilegedExceptionAction;

public class PrivilegedExceptionActionForSocketOutputStream implements PrivilegedExceptionAction<OutputStream> {

	public PrivilegedExceptionActionForSocketOutputStream(SocketImpl impl) {
		super();
		this.impl = impl;
	}

	private SocketImpl impl;

	public OutputStream run() throws Exception {
		// TODO Auto-generated method stub
		return impl.getOutputStream();
	}

}




