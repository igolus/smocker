package com.jenetics.smocker.util;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;

public class SmockerSocketOutputStreamData {
	private ByteArrayOutputStream buffer;

	public SmockerSocketOutputStreamData(ByteArrayOutputStream buffer) {
		this.buffer = buffer;
	}
	
	public String getString () throws UnsupportedEncodingException {
		return new String(buffer.toByteArray(), "UTF-8");
	}
	
	public byte[] getBytes() {
		return this.buffer.toByteArray();
	}

	public ByteArrayOutputStream getBuffer() {
		return buffer;
	}
	
}