package com.jenetics.smocker;

import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

import com.jenetics.smocker.transformers.SSLSocketImplTransformer;
import com.jenetics.smocker.transformers.SocketTransformer;
import com.jenetics.smocker.util.RessourceLoader;
import com.jenetics.smocker.util.network.SmockerServer;

/**
 * Main transformer
 * @author igolus
 *
 */
public class MainTransformer implements ClassFileTransformer {
	
	
	private static final String SSLSocketImplGetOutputStreamSRCFile = "sun.security.ssl.SSLSocketImpl_getOutputStream.txt";
		
	private static SmockerServer smockerServer = null;
	
	public static SmockerServer getSmockerServer() {
		return smockerServer;
	}

	public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
			ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
		// TODO Auto-generated method stub
		if (smockerServer == null) {
			smockerServer = new SmockerServer();
			smockerServer.startServer();
		}
		
		
		byte[] byteCode = classfileBuffer;
		try {
			if (className != null && className.equals("sun/security/ssl/SSLSocketImpl")) {
				byteCode = new SSLSocketImplTransformer().transform(classfileBuffer);
			};
			
			if (className != null && className.equals("java/net/Socket")) {
				byteCode = new SocketTransformer().transform(classfileBuffer);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return byteCode;
	}
}
