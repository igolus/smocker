package com.jenetics.smocker.transformers;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import com.jenetics.smocker.util.RessourceLoader;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtMethod;
import javassist.Modifier;
import javassist.NotFoundException;

public class SocketTransformer extends AbstractTransformer {
     
	private static final String SocketGetOutputStreamSRCFile = "java.net.Socket_getOutputStreamNew.txt";
	private static final String SocketGetInputStreamSRCFile = "java.net.Socket_getInputStreamNew.txt";
	
	public byte[] transform(byte[] classfileBuffer)
			throws IOException, NotFoundException, CannotCompileException {
		byte[] byteCode;
		ClassPool classPool = ClassPool.getDefault();
		CtClass ctClass = classPool.makeClass(new ByteArrayInputStream(classfileBuffer));
		
		redefineGetOutputStream(classPool, ctClass);
		redefineGetInputStream(classPool, ctClass);
		redefineClose(classPool, ctClass);
		
		byteCode = ctClass.toBytecode();
		ctClass.detach();
		ctClass.defrost();
		return byteCode;
	}

	private void redefineClose(ClassPool classPool, CtClass ctClass) throws NotFoundException, CannotCompileException {
		CtMethod closeMethod = ctClass.getDeclaredMethod("close");

		String body = "{" 
					+ " try{"
					+ " 	com.jenetics.smocker.util.TransformerUtility.socketClosed( $0 );"
					+ "} catch (Throwable t) { " 
					+ "    throw t; "
					+ "}" 
					+ "}";

		closeMethod.insertAfter(body);
	}

	private void redefineGetInputStream(ClassPool classPool, CtClass ctClass) throws NotFoundException, CannotCompileException {
		String bodyGetInputStreamCopy = RessourceLoader.loadJavassistSource(SocketGetInputStreamSRCFile);
		CtMethod getInputStreamMethodNew = new CtMethod(classPool.get("java.io.InputStream"),
				"getInputStreamCopy", null, ctClass);
		
		getInputStreamMethodNew.setModifiers(ctClass.getModifiers() & Modifier.SYNCHRONIZED );
		ctClass.addMethod(getInputStreamMethodNew);
		getInputStreamMethodNew.setBody(bodyGetInputStreamCopy);
		ctClass.setModifiers(ctClass.getModifiers() & ~Modifier.ABSTRACT);
		
		String body = "{"
					+ " try{" 
				    + "     java.io.InputStream in = getInputStreamCopy($$);"
					+ " 	in = com.jenetics.smocker.util.TransformerUtility.manageInputStream( in, $0 );"
					+ " 	return in;\n" 
					+ "} catch (Throwable t) "
					+ "{ "
					+ "     throw t; "
					+ "}" 
					+ "}";
		
		CtMethod getOutputStreamMethod = ctClass.getDeclaredMethod("getInputStream");
		getOutputStreamMethod.setBody(body.toString());
		
	}

	private void redefineGetOutputStream(ClassPool classPool, CtClass ctClass)
			throws NotFoundException, CannotCompileException {
		String bodyGetOutputStreamCopy = RessourceLoader.loadJavassistSource(SocketGetOutputStreamSRCFile);
		CtMethod getOutputStreamMethodNew = new CtMethod(classPool.get("java.io.OutputStream"),
				"getOutputStreamCopy", null, ctClass);
		
		getOutputStreamMethodNew.setModifiers(ctClass.getModifiers() & Modifier.SYNCHRONIZED );
		ctClass.addMethod(getOutputStreamMethodNew);
		getOutputStreamMethodNew.setBody(bodyGetOutputStreamCopy);
		ctClass.setModifiers(ctClass.getModifiers() & ~Modifier.ABSTRACT);
		
		String body = "{"
					+ " try{" 
				    + "     java.io.OutputStream out = getOutputStreamCopy($$);"
					+ " 	out = com.jenetics.smocker.util.TransformerUtility.manageOutputStream( out, $0 );"
					+ " 	return out;\n" 
					+ "} catch (Throwable t) "
					+ "{ "
					+ "     throw t; "
					+ "}" 
					+ "}\n";
		
		CtMethod getOutputStreamMethod = ctClass.getDeclaredMethod("getOutputStream");
		getOutputStreamMethod.setBody(body.toString());
	}

	protected void redefineConstructors(ClassPool classPool, CtClass ctClass) throws NotFoundException, CannotCompileException {
//		CtConstructor constructor = ctClass.getDeclaredConstructor(new CtClass[] {
//				classPool.get("java.net.SocketAddress"), 
//				classPool.get("java.net.SocketAddress"),
//				CtClass.booleanType
//				});
		
		
//		CtConstructor[] constructors = ctClass.getDeclaredConstructors();
//		for (CtConstructor ctConstructor : constructors) {
//			try {
//				ctConstructor.insertAfter(body);
//			}
//			catch (Throwable t) {
//				t.printStackTrace();
//			}
//		}
//		
//		CtField smockerAdressField = new CtField(classPool.get("java.net.SocketAddress"), "smockerAdress", ctClass);
//		smockerAdressField.setModifiers(Modifier.PUBLIC);
//		ctClass.addField(smockerAdressField);
//	
//		CtField smockerLocalAdressField = new CtField(classPool.get("java.net.SocketAddress"), "smockerLocalAdress", ctClass);
//		smockerLocalAdressField.setModifiers(Modifier.PUBLIC);
//		ctClass.addField(smockerLocalAdressField);
//		
//		CtField smockerStreamField = new CtField(CtClass.booleanType, "smockerStream", ctClass);
//		smockerStreamField.setModifiers(Modifier.PUBLIC);
//		ctClass.addField(smockerStreamField);
		
		String body = " try{"
				+ " 	com.jenetics.smocker.util.TransformerUtility.socketCreated( $$ , $0 );"
				+ "} catch (Throwable t) " + "{ " + "     throw t; " + "}";
		
		CtConstructor[] constructors = ctClass.getDeclaredConstructors();
		for (CtConstructor ctConstructor : constructors) {
			try {
				ctConstructor.insertAfter(body);
			}
			catch (Throwable t) {
				t.printStackTrace();
			}
		}
	
//		String body = 
//					  " try{" 
//					+ " 	com.jenetics.smocker.util.TransformerUtility.socketCreated( (java.net.InetSocketAddress)$0.smockerAdress, "
//					+ "(java.net.InetSocketAddress)$0.smockerLocalAdress, $0.smockerStream, $0 );"
//					+ "} catch (Throwable t) "
//					+ "{ "
//					+ "     throw t; "
//					+ "}";
//		constructor.insertAfter(body);
		
	}
}
