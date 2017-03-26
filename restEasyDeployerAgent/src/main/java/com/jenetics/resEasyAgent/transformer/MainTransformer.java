package com.jenetics.resEasyAgent.transformer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtPrimitiveType;

/**
 * Main transformer
 * 
 * @author igolus
 *
 */
public class MainTransformer implements ClassFileTransformer {

	private static boolean done = false;

	public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
			ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
		// TODO Auto-generated method stub

		byte[] byteCode = classfileBuffer;
		if (!done) {
			
			
			
			try {
				ClassPool classPool = ClassPool.getDefault();
				
				
				CtClass endPoint = classPool.makeClass("GenericConnection");
				
				
				
				CtClass ctClass = classPool.makeClass(new ByteArrayInputStream(classfileBuffer));

				ClassPool pool = ClassPool.getDefault();
				CtClass cc = pool.makeClass("Point");
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return byteCode;
	}
}