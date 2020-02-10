package com.jenetics.smocker.transformers;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.Modifier;
import javassist.NotFoundException;

import java.io.ByteArrayInputStream;
import java.io.IOException;

public class SelectorImplTransformer {



	public byte[] transform(byte[] classfileBuffer)
			throws IOException, NotFoundException, CannotCompileException {
		byte[] byteCode;
		ClassPool classPool = ClassPool.getDefault();
		CtClass ctClass = classPool.makeClass(new ByteArrayInputStream(classfileBuffer));

		defineNewMethod(ctClass);

		byteCode = ctClass.toBytecode();
		ctClass.detach();
		ctClass.defrost();
		return byteCode;
	}

	private void defineNewMethod(CtClass ctClass) throws CannotCompileException, NotFoundException {
		ctClass.setModifiers(Modifier.PUBLIC);

		final String publicRegisterBody = "public Object publicRegister (nio.channels.spi.AbstractSelectableChannel channel, int ops, Object attachment) {"
				+ " try{" 
				+ " 	return $0.register(channel, ops, attachment);"
				+ "} catch (Throwable t) "
				+ "{ "
				+ "     t.printStackTrace(); "
				+ "     throw t; "
				+ "}" 
				+ "}";
		
		CtMethod publicRegister = CtNewMethod.make(publicRegisterBody, ctClass);
		ctClass.addMethod(publicRegister);
		
		
		CtMethod newmethod = CtNewMethod.make("public void testPrint() { System.out.println(); }",ctClass);
		ctClass.addMethod(newmethod);


	}

	

}
