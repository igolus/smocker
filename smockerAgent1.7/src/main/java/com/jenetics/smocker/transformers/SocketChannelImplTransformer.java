package com.jenetics.smocker.transformers;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtMethod;
import javassist.NotFoundException;

public class SocketChannelImplTransformer {
	
	
	
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
		String body = " try{"
				+ " 	com.jenetics.smocker.util.TransformerUtility.socketChannelWrite( $0, $0.socket, $$ );"
				+ "} catch (Throwable t) " + "{ " + "     throw t; " + "}";
		
		CtMethod writeMethod = ctClass.getDeclaredMethod("write", new CtClass[] {classPool.getCtClass("java.nio.ByteBuffer")});
		writeMethod.insertAfter(body.toString());
		
	}
	
	private void redefineRead(ClassPool classPool, CtClass ctClass) throws NotFoundException, CannotCompileException {
		String body = " try{"
				+ " 	com.jenetics.smocker.util.TransformerUtility.socketChannelRead( $0, $0.socket, $$ );"
				+ "} catch (Throwable t) " + "{ " + "     throw t; " + "}";
		
		CtMethod writeMethod = ctClass.getDeclaredMethod("read", new CtClass[] {classPool.getCtClass("java.nio.ByteBuffer")});
		writeMethod.insertAfter(body.toString());
		
	}
}
