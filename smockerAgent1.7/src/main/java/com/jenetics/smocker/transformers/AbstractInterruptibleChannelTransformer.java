package com.jenetics.smocker.transformers;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.Modifier;
import javassist.NotFoundException;

public class AbstractInterruptibleChannelTransformer {
	
	
	
	public byte[] transform(byte[] classfileBuffer)
			throws IOException, NotFoundException, CannotCompileException {
		byte[] byteCode;
		ClassPool classPool = ClassPool.getDefault();
		CtClass ctClass = classPool.makeClass(new ByteArrayInputStream(classfileBuffer));
		
		redefineClose(classPool, ctClass);
		
		byteCode = ctClass.toBytecode();
		ctClass.detach();
		ctClass.defrost();
		return byteCode;
	}
	
private void redefineClose(ClassPool classPool, CtClass ctClass) throws NotFoundException, CannotCompileException {
		
		CtMethod writeMethodInitial = ctClass.getDeclaredMethod("close");
		writeMethodInitial.setName("closeNew");	
		writeMethodInitial.setModifiers(Modifier.PUBLIC);
		ctClass.setModifiers(Modifier.PUBLIC);
		
		
		final String body = "public void close () {"
				+ " try{" 
				+ " 	com.jenetics.smocker.util.TransformerUtility.socketChannelClosed( $0 );"
				+ "} catch (Throwable t) "
				+ "{ "
				+ "     throw t; "
				+ "}" 
				+ "}";
		

		CtMethod closeMethod = CtNewMethod.make(body, ctClass);
		ctClass.addMethod(closeMethod);

	}

}
