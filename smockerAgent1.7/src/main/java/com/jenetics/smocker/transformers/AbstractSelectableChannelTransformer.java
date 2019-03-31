package com.jenetics.smocker.transformers;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtNewMethod;

public class AbstractSelectableChannelTransformer {

	public byte[] transform(byte[] classfileBuffer)
			throws IOException, CannotCompileException {
		byte[] byteCode;
		ClassPool classPool = ClassPool.getDefault();
		CtClass ctClass = classPool.makeClass(new ByteArrayInputStream(classfileBuffer));

		defineModifyKey(ctClass);

		byteCode = ctClass.toBytecode();
		ctClass.detach();
		ctClass.defrost();
		return byteCode;
	}

	private void defineModifyKey(CtClass ctClass) throws CannotCompileException {
		
		final String body = "public void modifyFirstKeyRead () {"
				+ " try{" 
				+ " 	System.out.println($0.getClass());"
				+ " 	System.out.println($0.keys[0].getClass());"
				+ " 	System.out.println($0.keys);" 
				+ " 	sun.nio.ch.SelectionKeyImpl selectionKeyImpl = (sun.nio.ch.SelectionKeyImpl)$0.keys[0];"
				+ "} catch (Throwable t) "
				+ "{ "
				+ "     t.printStackTrace(); "
				+ "     throw t; "
				+ "}" 
				+ "}";
		
		CtMethod writeMethod = CtNewMethod.make(body, ctClass);
		ctClass.addMethod(writeMethod);
		
		final String bodygetkeys = "public sun.nio.ch.SelectionKeyImpl[] getKeys () {"
				+ " try{" 
				+ " 	return  $0.keys;"
				+ "} catch (Throwable t) "
				+ "{ "
				+ "     throw t; "
				+ "}" 
				+ "}";

		
			
		CtMethod getKeyMethod = CtNewMethod.make(bodygetkeys, ctClass);
		ctClass.addMethod(getKeyMethod);
		
	}
}
