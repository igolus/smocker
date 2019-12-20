package com.jenetics.smocker.transformers;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import com.jenetics.smocker.util.NioCustomSelectionKey;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
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
		try {
			ctClass.setModifiers(Modifier.PUBLIC);

			ctClass.getField("selectedKeys").setModifiers(Modifier.PUBLIC);

			ctClass.getField("fdToKey").setModifiers(Modifier.PUBLIC);
			
			ctClass.getField("interruptLock").setModifiers(Modifier.PUBLIC);

			CtClass nioCustomSelectionKey = ClassPool.getDefault().get("com.jenetics.smocker.util.NioCustomSelectionKey");
			CtField nioCustomSelectionKeyField = new CtField(nioCustomSelectionKey, "selectedKey", ctClass);
			nioCustomSelectionKeyField.setModifiers(Modifier.PUBLIC);
			ctClass.addField(nioCustomSelectionKeyField);


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

			final String getKeysBody = "public Object getKeys () {"
					+ " try{" 
					+ " 	return $0.keys;"
					+ "} catch (Throwable t) "
					+ "{ "
					+ "     t.printStackTrace(); "
					+ "     throw t; "
					+ "}" 
					+ "}";

			CtMethod getKeys = CtNewMethod.make(getKeysBody, ctClass);
			ctClass.addMethod(getKeys);

			final String publicRegisterBody = "public Object publicRegister (java.nio.channels.spi.AbstractSelectableChannel channel, Object attachment) {"
					+ " try{" 
					+ " 	return $0.register(channel, 1, attachment);"
					+ "} catch (Throwable t) "
					+ "{ "
					+ "     t.printStackTrace(); "
					+ "     throw t; "
					+ "}" 
					+ "}";

			CtMethod publicRegister = CtNewMethod.make(publicRegisterBody, ctClass);
			ctClass.addMethod(publicRegister);
			
			
					
		CtMethod selectMethodInitial = ctClass.getDeclaredMethod("doSelect");
		selectMethodInitial.setName("doSelectNew");
		selectMethodInitial.setModifiers(Modifier.PUBLIC);
		ctClass.setModifiers(Modifier.PUBLIC);
			
			
					final String selectNowBody = "public int doSelect(long timeout) {"
		+ " try{" 
		+ " 	return com.jenetics.smocker.util.TransformerUtility.doSelect($0, $1);"
		+ "} catch (Throwable t) "
		+ "{ "
		+ "     throw t; "
		+ "}" 
		+ "}";

		CtMethod selectNowNew = CtNewMethod.make(selectNowBody, ctClass);
		ctClass.addMethod(selectNowNew);
			
		} catch (NotFoundException e) {
			e.printStackTrace();
		} catch (CannotCompileException e) {
			e.printStackTrace();
		}
	}

	

}
