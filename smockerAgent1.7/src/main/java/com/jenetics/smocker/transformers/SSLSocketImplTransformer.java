package com.jenetics.smocker.transformers;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import com.jenetics.smocker.util.MessageLogger;
import com.jenetics.smocker.util.RessourceLoader;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtMethod;
import javassist.Modifier;
import javassist.NotFoundException;

public class SSLSocketImplTransformer extends AbstractTransformer {

	private static final String SSLSocketImplGetOutputStreamSRCFile = "sun.security.ssl.SSLSocketImpl_getOutputStream.txt";
	private static final String SSLSocketImplGetInputStreamSRCFile = "sun.security.ssl.SSLSocketImpl_getInputStream.txt";

	public byte[] transform(byte[] classfileBuffer) throws IOException, NotFoundException, CannotCompileException {
		byte[] byteCode;
		ClassPool classPool = ClassPool.getDefault();
		CtClass ctClass = classPool.makeClass(new ByteArrayInputStream(classfileBuffer));
		redefineGetOutputStream(classPool, ctClass);
		redefineGetInputStream(classPool, ctClass);
		redefineClose(ctClass);
		byteCode = ctClass.toBytecode();
		ctClass.detach();
		ctClass.defrost();
		return byteCode;
	}


	private void redefineClose(CtClass ctClass) throws NotFoundException, CannotCompileException {
		CtMethod closeMethod = ctClass.getDeclaredMethod("close");

		String body = "{" + 
					  " try{" +
					  " 	com.jenetics.smocker.util.TransformerUtility.socketClosed( $0 );" + 
					  "} catch (Throwable t) { " +
					  "    throw t; " +
					  "}" +
					  "}";

		closeMethod.insertAfter(body);

	}

	private void redefineGetInputStream(ClassPool classPool, CtClass ctClass)
			throws NotFoundException, CannotCompileException {
		String bodyGetInputStreamCopy = RessourceLoader.loadJavassistSource(SSLSocketImplGetInputStreamSRCFile);
		CtMethod getInputStreamMethodNew = new CtMethod(classPool.get("java.io.InputStream"), "getInputStreamCopy",
				null, ctClass);

		getInputStreamMethodNew.setModifiers(ctClass.getModifiers() & Modifier.SYNCHRONIZED);
		ctClass.addMethod(getInputStreamMethodNew);
		getInputStreamMethodNew.setBody(bodyGetInputStreamCopy);
		ctClass.setModifiers(ctClass.getModifiers() & ~Modifier.ABSTRACT);

		String body = "{" +
					  " try{" +
					  "     java.io.InputStream in = getInputStreamCopy($$);" +
					  " 	in = com.jenetics.smocker.util.TransformerUtility.manageInputStream( in, $0 );"	+
					  " 	return in;\n" +
					  "} catch (Throwable t) { " +
					  "    throw t; " +
					  "}" +
					  "}";

		CtMethod getOutputStreamMethod = ctClass.getDeclaredMethod("getInputStream");
		getOutputStreamMethod.setBody(body);
	}

	private void redefineGetOutputStream(ClassPool classPool, CtClass ctClass)
			throws NotFoundException, CannotCompileException {
		String bodyGetOutputStreamCopy = RessourceLoader.loadJavassistSource(SSLSocketImplGetOutputStreamSRCFile);
		CtMethod getOutputStreamMethodNew = new CtMethod(classPool.get("java.io.OutputStream"), "getOutputStreamCopy",
				null, ctClass);

		getOutputStreamMethodNew.setModifiers(ctClass.getModifiers() & Modifier.SYNCHRONIZED);
		ctClass.addMethod(getOutputStreamMethodNew);
		getOutputStreamMethodNew.setBody(bodyGetOutputStreamCopy);
		ctClass.setModifiers(ctClass.getModifiers() & ~Modifier.ABSTRACT);

		String body = "{" +
					  " try{" +
					  "     java.io.OutputStream out = getOutputStreamCopy($$);" +
					  " 	out = com.jenetics.smocker.util.TransformerUtility.manageOutputStream( out, $0 );" +
					  " 	return out;" +
					  "} catch (Throwable t) " +
					  "{ " +
					  "     throw t; " +
					  "}" +
					  "}";

		CtMethod getOutputStreamMethod = ctClass.getDeclaredMethod("getOutputStream");
		getOutputStreamMethod.setBody(body);
	}

	protected void redefineConstructors(CtClass ctClass)
			throws CannotCompileException {
		String body = " try{" +
					  " 	com.jenetics.smocker.util.TransformerUtility.sslSocketCreated( $$ , $0 );" +
					  "} catch (Throwable t) " +
					  "{ " +
					  "     throw t; " +
					  "}";
		
		CtConstructor[] constructors = ctClass.getDeclaredConstructors();
		for (CtConstructor ctConstructor : constructors) {
			try {
				ctConstructor.insertAfter(body);
			}
			catch (Exception e) {
				MessageLogger.logThrowable(e, getClass());
			}
		}



	}
}
