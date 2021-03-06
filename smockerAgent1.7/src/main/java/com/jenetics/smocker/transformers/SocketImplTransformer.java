package com.jenetics.smocker.transformers;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.Modifier;
import javassist.NotFoundException;

public class SocketImplTransformer extends AbstractTransformer {
     
	
	public byte[] transform(byte[] classfileBuffer)
			throws IOException, NotFoundException, CannotCompileException {
		byte[] byteCode;
		ClassPool classPool = ClassPool.getDefault();
		CtClass ctClass = classPool.makeClass(new ByteArrayInputStream(classfileBuffer));
		
		definePublicGetSocket(classPool, ctClass);
		
		byteCode = ctClass.toBytecode();
		ctClass.detach();
		ctClass.defrost();
		return byteCode;
	}

	private void definePublicGetSocket(ClassPool classPool, CtClass ctClass) throws NotFoundException, CannotCompileException {
		CtMethod publicGetSocket = new CtMethod(classPool.get("java.net.Socket"),
				"publicGetSocket", null, ctClass);

		String body = "{ return $0.socket; }";
		
		publicGetSocket.setModifiers(ctClass.getModifiers() & Modifier.PUBLIC );
		publicGetSocket.setBody(body, body, body);
		ctClass.addMethod(publicGetSocket);

	}
}
