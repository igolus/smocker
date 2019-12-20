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
		
		//ctClass.getDeclaredMethod("publicRegister").setModifiers(Modifier.PUBLIC);

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
		
//		CtMethod writeMethodInitial = ctClass.getDeclaredMethod("selectNow");
//		writeMethodInitial.setName("selectNowNew");
//		writeMethodInitial.setModifiers(Modifier.PUBLIC);
//		ctClass.setModifiers(Modifier.PUBLIC);
		
		
		final String selectNowBody = "public int selectNowS() {"
		//+ " try{" 
		//+ " 	return com.jenetics.smocker.util.TransformerUtility.selectNow($0);"
		+ " 	return 0;"
		//+ "} catch (Throwable t) "
		//+ "{ "
		//+ "     throw t; "
		//+ "}" 
		+ "}";

//		CtMethod selectNowNew = CtNewMethod.make(selectNowBody, ctClass);
//		ctClass.addMethod(selectNowNew);
		
//		ctClass.getDeclaredMethods("register")[0].setModifiers(Modifier.PUBLIC);

//		final String getselectedKeysBody = "public Object getSelectedKeys () {"
//				+ " try{" 
//				+ " 	return $0.selectedKeys;"
//				+ "} catch (Throwable t) "
//				+ "{ "
//				+ "     t.printStackTrace(); "
//				+ "     throw t; "
//				+ "}" 
//				+ "}";
//		
//		CtMethod getselectedKeys = CtNewMethod.make(getselectedKeysBody, ctClass);
//		ctClass.addMethod(getselectedKeys);
	}

	

}
