package com.jenetics.smocker.transformers;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.Modifier;
import javassist.NotFoundException;

public class WindowsSelectorImplTransformer {



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
		
		ctClass.getField("selectedKeys").setModifiers(Modifier.PUBLIC);

		final String getselectedKeysBody = "public Object getSelectedKeys () {"
				+ " try{" 
				+ " 	return $0.selectedKeys;"
				+ "} catch (Throwable t) "
				+ "{ "
				+ "     t.printStackTrace(); "
				+ "     throw t; "
				+ "}" 
				+ "}";
		
		CtMethod getselectedKeys = CtNewMethod.make(getselectedKeysBody, ctClass);
		ctClass.addMethod(getselectedKeys);
	}

	

}
