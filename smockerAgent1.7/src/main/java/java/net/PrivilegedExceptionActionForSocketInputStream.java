package java.net;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketImpl;
import java.security.PrivilegedExceptionAction;

public class PrivilegedExceptionActionForSocketInputStream implements PrivilegedExceptionAction<InputStream> {

	public PrivilegedExceptionActionForSocketInputStream(SocketImpl impl) {
		super();
		this.impl = impl;
	}

	private SocketImpl impl;

	public InputStream run() throws Exception {
		return impl.getInputStream();
	}
}
