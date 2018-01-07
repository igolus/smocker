package com.jenetics.smocker.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import com.jenetics.smocker.util.network.RestClientSmocker;
import static com.jenetics.smocker.util.network.Base64Util.*;

public class SmockerSocketOutputStreamData {
	public ByteArrayOutputStream buffer;
	private int index = 0;

	public SmockerSocketOutputStreamData(ByteArrayOutputStream buffer) {
		this.buffer = buffer;
	}
	
	public SmockerSocketOutputStreamData(String content) throws IOException {
		this.buffer = new ByteArrayOutputStream();
		buffer.write(content.getBytes());
	}
	
	public String getString () throws UnsupportedEncodingException {
		return new String(buffer.toByteArray(), "UTF-8");
	}
	
	public String getStringBase64() throws UnsupportedEncodingException {
		String ret = new String(buffer.toByteArray(), "UTF-8");
		return encode(ret);
	}
	
	public int read() {
		return index < buffer.size() ? buffer.toByteArray()[index++] : -1;
	}

	public int available() {
		// TODO Auto-generated method stub
		return buffer.size() - index;
	}
}