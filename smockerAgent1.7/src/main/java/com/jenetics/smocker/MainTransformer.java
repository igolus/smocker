package com.jenetics.smocker;

import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

import com.jenetics.smocker.transformers.AbstractInterruptibleChannelTransformer;
import com.jenetics.smocker.transformers.SSLSocketImplTransformer;
import com.jenetics.smocker.transformers.SocketChannelImplTransformer;
import com.jenetics.smocker.transformers.SocketAdaptorImplTransformer;
import com.jenetics.smocker.transformers.SocketImplTransformer;
import com.jenetics.smocker.transformers.SocketInputStreamtTransformer;
import com.jenetics.smocker.transformers.SocketTransformer;
import com.jenetics.smocker.util.RessourceLoader;
import com.jenetics.smocker.util.network.SmockerServer;

/**
 * Main transformer
 * @author igolus
 *
 */
public class MainTransformer implements ClassFileTransformer {
	
	
	public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
			ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
		// TODO Auto-generated method stub
		
		
		byte[] byteCode = classfileBuffer;
		try {
			if (className != null && className.equals("sun/security/ssl/SSLSocketImpl")) {
				byteCode = new SSLSocketImplTransformer().transform(classfileBuffer);
			};
			
			if (className != null && className.equals("java/net/Socket")) {
				byteCode = new SocketTransformer().transform(classfileBuffer);
			}
			
//			if (className != null && className.equals("java/net/SocketInputStream")) {
//				byteCode = new SocketInputStreamtTransformer().transform(classfileBuffer);
//			}
			
//			if (className != null && className.startsWith("java/net/SocksSocketImpl")) {
//				byteCode = new SocketImplTransformer().transform(classfileBuffer);
//			}
//			
//			if (className != null && className.startsWith("sun/nio/ch/SocketAdaptor")) {
//				//System.out.println();
//				byteCode = new SocketAdaptorImplTransformer().transform(classfileBuffer);
//			}
			
			if (className != null && className.equals("sun/nio/ch/SocketChannelImpl")) {
				//System.out.println();
				byteCode = new SocketChannelImplTransformer().transform(classfileBuffer);
			}
			
			if (className != null && className.equals("java/nio/channels/spi/AbstractInterruptibleChannel")) {
				//System.out.println();
				byteCode = new AbstractInterruptibleChannelTransformer().transform(classfileBuffer);
			}
			
			
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return byteCode;
	}
}
