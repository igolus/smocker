package com.jenetics.smocker.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class SmockerSocketOutputStream extends BaseSmockerSocketHandler {
	
	SmockerSocketOutputStreamData smockerSocketOutputStreamData = new SmockerSocketOutputStreamData(new ByteArrayOutputStream());
	
	@Override
	public void write(int b) throws IOException {
		smockerSocketOutputStreamData.getBuffer().write(b);
	}

	public SmockerSocketOutputStreamData getSmockerOutputStreamData() {
		return smockerSocketOutputStreamData;
	}

	public byte[] getBytes() {
		return smockerSocketOutputStreamData.getBytes();
	}

}
