package com.jenetics.smocker.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class SmockerSocketInputStream extends OutputStream {

	SmockerSocketOutputStreamData smockerOutputStreamData = new SmockerSocketOutputStreamData(new ByteArrayOutputStream());
	

	public SmockerSocketOutputStreamData getSmockerOutputStreamData() {
		return smockerOutputStreamData;
	}


	@Override
	public void write(int b) throws IOException {
		smockerOutputStreamData.getBuffer().write(b);
	}

}
