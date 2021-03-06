package com.jenetics.smocker.transformers;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;

public class AbstractInterruptibleChannelTransformer {

	public byte[] transform(byte[] classfileBuffer)
			throws IOException, NotFoundException, CannotCompileException {
		byte[] byteCode;
		ClassPool classPool = ClassPool.getDefault();
		CtClass ctClass = classPool.makeClass(new ByteArrayInputStream(classfileBuffer));

		redefineClose(ctClass);

		byteCode = ctClass.toBytecode();
		ctClass.detach();
		ctClass.defrost();
		return byteCode;
	}

	private void redefineClose(CtClass ctClass) throws NotFoundException, CannotCompileException {

		CtMethod writeMethodInitial = ctClass.getDeclaredMethod("close");

		final String body = "{"
				+ " try{" 
				+ " 	com.jenetics.smocker.util.TransformerUtility.socketChannelClosed( $0 );"
				+ "} catch (Throwable t) "
				+ "{ "
				+ "     throw t; "
				+ "}" 
				+ "}";

		writeMethodInitial.insertAfter(body);
	}

}
