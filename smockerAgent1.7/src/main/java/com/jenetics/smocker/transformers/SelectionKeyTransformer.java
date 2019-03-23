package com.jenetics.smocker.transformers;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.invoke.VolatileCallSite;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.Modifier;
import javassist.NotFoundException;

public class SelectionKeyTransformer {

	public byte[] transform(byte[] classfileBuffer)
			throws IOException, NotFoundException, CannotCompileException {
		byte[] byteCode;
		ClassPool classPool = ClassPool.getDefault();
		CtClass ctClass = classPool.makeClass(new ByteArrayInputStream(classfileBuffer));

		defineNewMethod(classPool, ctClass);

		byteCode = ctClass.toBytecode();
		ctClass.detach();
		ctClass.defrost();
		return byteCode;
	}

	private void defineNewMethod(ClassPool classPool, CtClass ctClass) throws CannotCompileException, NotFoundException {
		//ctClass.setModifiers(Modifier.PUBLIC);
//		ctClass.getDeclaredField("attachment").setModifiers(Modifier.PUBLIC & Modifier.VOLATILE);
		
		final String body = "public void setAttachment (Object attachment) {"
				+ " try{" 
				+ " 	$0.attachment = attachment;"
				+ "} catch (Throwable t) "
				+ "{ "
				+ "     t.printStackTrace(); "
				+ "     throw t; "
				+ "}" 
				+ "}";
		
		CtMethod writeMethod = CtNewMethod.make(body, ctClass);
		ctClass.addMethod(writeMethod);
		
		final String getAttachmentBody = "public Object getAttachment () {"
				+ " try{" 
				+ " 	return $0.attachment;"
				+ "} catch (Throwable t) "
				+ "{ "
				+ "     t.printStackTrace(); "
				+ "     throw t; "
				+ "}" 
				+ "}";
		
		CtMethod getAttachment = CtNewMethod.make(getAttachmentBody, ctClass);
		ctClass.addMethod(getAttachment);
	}

	

}
