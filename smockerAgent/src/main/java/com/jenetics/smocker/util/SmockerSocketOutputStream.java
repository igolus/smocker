package com.jenetics.smocker.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.stream.Stream;

public class SmockerSocketOutputStream extends BaseSmockerSocketHandler {
	
	SmockerSocketOutputStreamData smockerSocketOutputStreamData = new SmockerSocketOutputStreamData(new ByteArrayOutputStream());
	
	private Socket socketSource;

	public SmockerSocketOutputStream(Socket source) {
		this.socketSource = source;
	}

	@Override
	public void write(int b) throws IOException {
		smockerSocketOutputStreamData.buffer.write(b);
	}

	public SmockerSocketOutputStreamData getSmockerOutputStreamData() {
		// TODO Auto-generated method stub
		return smockerSocketOutputStreamData;
	}

//	@Override
//	public void flush() throws IOException {
//		flushOut(data.buffer.toByteArray());
//	}
//
//	private void flushOut(byte[] byteArray) throws UnsupportedEncodingException {
//		System.out.println(new String(byteArray,  "UTF-8"));
//	}

//	@Override
//	public void close() throws IOException {
//		// TODO Auto-generated method stub
//		super.close();
//	}
	
	

}
