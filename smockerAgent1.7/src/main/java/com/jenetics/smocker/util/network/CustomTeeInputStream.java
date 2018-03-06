package com.jenetics.smocker.util.network;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import org.apache.commons.io.input.TeeInputStream;

import com.jenetics.smocker.util.TransformerUtility;

public class CustomTeeInputStream extends TeeInputStream {

	private Socket source;

	public CustomTeeInputStream(Socket source, InputStream input, OutputStream branch) {
		super(input, branch, true);
		this.source = source;
		// TODO Auto-generated constructor stub
	}

	@Override
	public int read() throws IOException {
		// TODO Auto-generated method stub
		//System.out.println(this + source.getRemoteSocketAddress().toString() + " readSingle\n");
		return super.read();
	}

	@Override
	public int read(byte[] bts, int st, int end) throws IOException {
		// TODO Auto-generated method stub
		//System.out.println(this + source.getRemoteSocketAddress().toString() + " read2\n" + new String(bts) + "\n st" + st + " end " + end);
		
		int ret = super.read(bts, st, end);
		TransformerUtility.getSmockerContainer(source);
		return ret;
	}

	@Override
	public int read(byte[] bts) throws IOException {
		// TODO Auto-generated method stub
		//System.out.println(this + source.getRemoteSocketAddress().toString() + " read3\n");
		return super.read(bts);
	}

	@Override
	public int available() throws IOException {
		// TODO Auto-generated method stub
		return super.available();
	}
	
	
	

}
