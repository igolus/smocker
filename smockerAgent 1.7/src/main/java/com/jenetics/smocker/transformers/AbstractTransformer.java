package com.jenetics.smocker.transformers;

import java.io.IOException;

import javassist.CannotCompileException;
import javassist.NotFoundException;

public abstract class AbstractTransformer {
	public abstract byte[] transform(byte[] classfileBuffer) throws IOException, NotFoundException, CannotCompileException;
}

