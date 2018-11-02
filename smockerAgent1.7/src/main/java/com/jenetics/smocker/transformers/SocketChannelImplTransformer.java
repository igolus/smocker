package com.jenetics.smocker.transformers;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import com.jenetics.smocker.util.RessourceLoader;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.Modifier;
import javassist.NotFoundException;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;

public class SocketChannelImplTransformer {

	private static final String chanellReadSRCFile = "sun.nio.ch.SocketChannelImpl_read.txt";

	public byte[] transform(byte[] classfileBuffer)
			throws IOException, NotFoundException, CannotCompileException {
		byte[] byteCode;
		ClassPool classPool = ClassPool.getDefault();
		CtClass ctClass = classPool.makeClass(new ByteArrayInputStream(classfileBuffer));

		//redefineGetOutputStream(classPool, ctClass);
		//redefineGetInputStream(classPool, ctClass);
		//redefineClose(classPool, ctClass);
		redefineWrite(classPool, ctClass);
		redefineRead(classPool, ctClass);
		//redefineClose(classPool, ctClass);

		byteCode = ctClass.toBytecode();
		ctClass.detach();
		ctClass.defrost();
		return byteCode;
	}

	private void redefineClose(ClassPool classPool, CtClass ctClass) throws NotFoundException, CannotCompileException {
		String body = " try{"
				+ " 	com.jenetics.smocker.util.TransformerUtility.socketChannelClosed( $0, $0.socket, $$ );"
				+ "} catch (Throwable t) " + "{ " + "     throw t; " + "}";

		CtMethod writeMethod = ctClass.getDeclaredMethod("close");
		writeMethod.insertAfter(body.toString());

	}

	private void redefineWrite(ClassPool classPool, CtClass ctClass) throws NotFoundException, CannotCompileException {
		
		CtMethod writeMethodInitial = ctClass.getDeclaredMethod("write", new CtClass[] {classPool.getCtClass("java.nio.ByteBuffer")});
		writeMethodInitial.setName("writeNew");	
		writeMethodInitial.setModifiers(Modifier.PUBLIC);
		ctClass.setModifiers(Modifier.PUBLIC);
		
		final String body = "public int write (java.nio.ByteBuffer buffer) {"
				+ " try{" 
				+ " 	return com.jenetics.smocker.util.TransformerUtility.socketChannelWrite( $0, buffer);"
				+ "} catch (Throwable t) "
				+ "{ "
				+ "     throw t; "
				+ "}" 
				+ "}";
		
		CtMethod writeMethod = CtNewMethod.make(body, ctClass);
		ctClass.addMethod(writeMethod);
	}

	private void redefineRead(ClassPool classPool, final CtClass ctClass) throws NotFoundException, CannotCompileException {

		CtMethod readMethodInitial = ctClass.getDeclaredMethod("read", new CtClass[] {classPool.getCtClass("java.nio.ByteBuffer")});
		readMethodInitial.setName("readNew");	
		readMethodInitial.setModifiers(Modifier.PUBLIC);
		ctClass.setModifiers(Modifier.PUBLIC);
		
		final String body = "public int read (java.nio.ByteBuffer buffer) {"
				+ " try{" 
				+ " 	return com.jenetics.smocker.util.TransformerUtility.socketChannelRead( $0, buffer);"
				+ "} catch (Throwable t) "
				+ "{ "
				+ "     throw t; "
				+ "}" 
				+ "}";
		
		CtMethod readMethod = CtNewMethod.make(body, ctClass);
		ctClass.addMethod(readMethod);

	}
}
