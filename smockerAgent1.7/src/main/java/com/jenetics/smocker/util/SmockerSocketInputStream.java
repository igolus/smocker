package com.jenetics.smocker.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;

public class SmockerSocketInputStream extends OutputStream {

	SmockerSocketOutputStreamData smockerOutputStreamData = new SmockerSocketOutputStreamData(new ByteArrayOutputStream());
	

	public SmockerSocketOutputStreamData getSmockerOutputStreamData() {
		return smockerOutputStreamData;
	}


	@Override
	public void write(int b) throws IOException {
		
		//System.out.print(new String(new byte[] {(byte) b},  "UTF-8"));
		//System.out.println(b);
		smockerOutputStreamData.buffer.write(b);
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
//		//flushOut(smockerOutputStreamData.buffer.toByteArray());
//	}

}
