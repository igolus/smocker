package com.jenetics.smocker.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class SmockerSocketOutputStream extends OutputStream {
	
	SmockerSocketOutputStreamData smockerSocketOutputStreamData = new SmockerSocketOutputStreamData(new ByteArrayOutputStream());
	
	@Override
	public void write(int b) throws IOException {
		smockerSocketOutputStreamData.buffer.write(b);
	}

	public SmockerSocketOutputStreamData getSmockerOutputStreamData() {
		return smockerSocketOutputStreamData;
	}

	public void resetBuffer() {
		smockerSocketOutputStreamData = new SmockerSocketOutputStreamData(new ByteArrayOutputStream());
	}

}
