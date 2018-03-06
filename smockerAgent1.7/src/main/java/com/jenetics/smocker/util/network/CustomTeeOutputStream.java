package com.jenetics.smocker.util.network;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

import org.apache.commons.io.output.TeeOutputStream;

import com.jenetics.smocker.util.SmockerContainer;
import com.jenetics.smocker.util.TransformerUtility;

public class CustomTeeOutputStream extends TeeOutputStream {

	ByteArrayOutputStream baos = new ByteArrayOutputStream();
	private Socket source;
	
	public CustomTeeOutputStream(Socket source, OutputStream out, OutputStream branch) {
		super(out, branch);
		this.source = source;
	}

	@Override
	public synchronized void write(byte[] b) throws IOException {
		// TODO Auto-generated method stub
		super.write(b);
		baos.write(b);
	}

	@Override
	public synchronized void write(byte[] b, int off, int len) throws IOException {
		// TODO Auto-generated method stub
		//System.out.println(this + source.getRemoteSocketAddress().toString() + " write\n" + new String(b));
		String input = new String(b);
		String keyEncoding = "Accept-Encoding: gzip" + System.getProperty("line.separator");
		String contentWithoutEncoding = null;
		if (input.indexOf(keyEncoding) != -1) {
			contentWithoutEncoding = input.replaceFirst(keyEncoding , "");
			super.write(contentWithoutEncoding.getBytes(), off, len - keyEncoding.length());
		}
		else {
			super.write(input.getBytes(), off, len);
		}
		
	}

	@Override
	public synchronized void write(int b) throws IOException {
		// TODO Auto-generated method stub
	
		baos.write(b);
		super.write(b);
	}

	@Override
	public void flush() throws IOException {
		System.out.println(this + "flush");
		// TODO Auto-generated method stub
		String input = new String(baos.toByteArray());
		//super.write(input.getBytes(), 0, input.getBytes().length);
//		String contentWithoutEncoding = input.replaceFirst("Accept-Encoding: gzip" + System.getProperty("line.separator") , "");
//		super.write(contentWithoutEncoding.getBytes(), 0, contentWithoutEncoding.getBytes().length);
		//TransformerUtility.getSmockerContainer(source);
		SmockerContainer smockerContainer = TransformerUtility.getSmockerContainer(source);
		if (smockerContainer.getSmockerSocketInputStream() != null && 
				smockerContainer.getSmockerSocketInputStream().getSmockerOutputStreamData().getBuffer().size() > 0) {
			TransformerUtility.postCommuicationFromSource(source);
			smockerContainer.resetBuffer();
			
		}
		super.flush();
	}

	@Override
	public void close() throws IOException {
		SmockerContainer smockerContainer = TransformerUtility.getSmockerContainer(source);
		if (smockerContainer.getSmockerSocketInputStream() != null && 
				smockerContainer.getSmockerSocketInputStream().getSmockerOutputStreamData().getBuffer().size() > 0) {
			//TransformerUtility.postCommuicationFromSource(source);
			//smockerContainer.resetBuffer();
			
		}
		super.close();
	}
	
	
	
	
	
	

}
