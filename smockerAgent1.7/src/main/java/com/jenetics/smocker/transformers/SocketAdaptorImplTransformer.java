package com.jenetics.smocker.transformers;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;

public class SocketAdaptorImplTransformer extends AbstractTransformer {
     
	
	public byte[] transform(byte[] classfileBuffer)
			throws IOException, NotFoundException, CannotCompileException {
		byte[] byteCode;
		ClassPool classPool = ClassPool.getDefault();
		CtClass ctClass = classPool.makeClass(new ByteArrayInputStream(classfileBuffer));
		
		redefineGetOutputStream(ctClass);
		
		byteCode = ctClass.toBytecode();
		ctClass.detach();
		ctClass.defrost();
		return byteCode;
	}

	private void redefineGetOutputStream(CtClass ctClass)
			throws NotFoundException, CannotCompileException {
		String body = "{" +
				      " try{" +
				      " 	com.jenetics.smocker.util.TransformerUtility.manageOutputStreamNio( $0 );" +
				      "} catch (Throwable t) " +
				      "{ " +
				      "     throw t; " +
				      "}" +
				      "}\n";

		CtMethod getOutputStreamMethod = ctClass.getDeclaredMethod("getOutputStream");
		getOutputStreamMethod.insertAfter(body);
		
		
		
	}
}

