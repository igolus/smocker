package com.jenetics.smocker.transformers;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.NotFoundException;

public class SocketInputStreamtTransformer {
	
	
	
	public byte[] transform(byte[] classfileBuffer)
			throws IOException, NotFoundException, CannotCompileException {
		byte[] byteCode;
		ClassPool classPool = ClassPool.getDefault();
		CtClass ctClass = classPool.makeClass(new ByteArrayInputStream(classfileBuffer));
		
		//redefineGetOutputStream(classPool, ctClass);
		//redefineGetInputStream(classPool, ctClass);
		//redefineClose(classPool, ctClass);
		redefineConstructors(classPool, ctClass);
		
		byteCode = ctClass.toBytecode();
		ctClass.detach();
		ctClass.defrost();
		return byteCode;
	}

	private void redefineConstructors(ClassPool classPool, CtClass ctClass) {
		String body = " try{"
				+ " 	com.jenetics.smocker.util.TransformerUtility.socketInputStreamCreated( $$, $0.impl );"
				+ "} catch (Throwable t) " + "{ " + "     throw t; " + "}";
		
		CtConstructor[] constructors = ctClass.getDeclaredConstructors();
		for (CtConstructor ctConstructor : constructors) {
			try {
				ctConstructor.insertAfter(body);
			}
			catch (Throwable t) {
				t.printStackTrace();
			}
		}
		
	}
}
