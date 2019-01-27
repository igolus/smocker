package com.jenetics.smocker.util;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.Socket;

import com.jenetics.smocker.util.io.TeeInputStream;
import com.jenetics.smocker.util.io.TeeOutputStream;
import com.jenetics.smocker.util.network.RemoteServerChecker;
import com.jenetics.smocker.util.network.ResponseReader;
import com.jenetics.smocker.util.network.RestClientSmocker;

public class SmockerContainer {
	private SmockerSocketInputStream smockerSocketInputStream = null;
	private SmockerSocketOutputStream smockerSocketOutputStream = null;
	private ByteArrayOutputStream bosforMock = new ByteArrayOutputStream();
	
	private String host = null;
	private int port = 0;
	private boolean ssl;
	private TeeInputStream teeInputStream;
	private TeeOutputStream teeOutputStream;
	private String stackTrace;
	private String responseMocked; 
	
	private String outputToBesend;
	private String inputToBesend;
	private Object source;
	//private boolean postAtNextRead = false;
	//private boolean postAtNextWrite = false;
	private boolean applyMock;
	private boolean streamResent = false;
	private int indexForArrayCopy = 0;
	
	public int getIndexForArrayCopy() {
		return indexForArrayCopy;
	}

//	public boolean isPostAtNextWrite() {
//		return postAtNextWrite;
//	}
//
//	public void setPostAtNextWrite(boolean postAtNextWrite) {
//		this.postAtNextWrite = postAtNextWrite;
//	}

	public void setIndexForArrayCopy(int indexForArrayCopy) {
		this.indexForArrayCopy = indexForArrayCopy;
	}

	public boolean isStreamResent() {
		return streamResent;
	}

	public void setStreamResent(boolean streamResent) {
		this.streamResent = streamResent;
	}

	public boolean isApplyMock() {
		return applyMock;
	}

	public void setApplyMock(boolean applyMock) {
		this.applyMock = applyMock;
	}

	public String getOutputToBesend() {
		return outputToBesend;
	}


	public void setOutputToBesend(String outputToBesend) {
		this.outputToBesend = outputToBesend;
	}


	public String getInputToBesend() {
		return inputToBesend;
	}


	public void setInputToBesend(String inputToBesend) {
		this.inputToBesend = inputToBesend;
	}


	public String getStackTrace() {
		return stackTrace;
	}


	public String getHost() {
		return host;
	}


	public int getPort() {
		return port;
	}


	public SmockerContainer(String host, int port, String stackTrace, Object source) {
		this.host = host;
		this.port = port;
		this.stackTrace = stackTrace;
		this.source = source;
		
		this.applyMock = RemoteServerChecker.getMockedHost().contains(host);
	}


	public SmockerContainer(SmockerSocketInputStream smockerSocketInputStream,
			SmockerSocketOutputStream smockerSocketOutputStream) {
		super();
		this.smockerSocketInputStream = smockerSocketInputStream;
		this.smockerSocketOutputStream = smockerSocketOutputStream;
	}

	public ByteArrayOutputStream getBosforMock() {
		return bosforMock;
	}
	
	public ByteArrayOutputStream resetBosforMock() {
		ByteArrayOutputStream byteArrayOutputStream = bosforMock = new ByteArrayOutputStream();
		return byteArrayOutputStream;
	}

	public void setBosforMock(ByteArrayOutputStream bosforMock) {
		this.bosforMock = bosforMock;
	}


	public Object getSource() {
		return source;
	}


	public SmockerSocketInputStream getSmockerSocketInputStream() {
		return smockerSocketInputStream;
	}


	public void setSmockerSocketInputStream(SmockerSocketInputStream smockerSocketInputStream) {
		this.smockerSocketInputStream = smockerSocketInputStream;
	}
	
	public void resetSmockerSocketInputStream() {
		this.smockerSocketInputStream = new SmockerSocketInputStream();
	}

	public SmockerSocketOutputStream getSmockerSocketOutputStream() {
		return smockerSocketOutputStream;
	}

	public void setSmockerSocketOutputStream(SmockerSocketOutputStream smockerSocketOutputStream) {
		this.smockerSocketOutputStream = smockerSocketOutputStream;
	}
	
	public void resetSmockerSocketOutputStream() {
		this.smockerSocketOutputStream = new SmockerSocketOutputStream();
	}
	
	public void resetAll() {
		resetSmockerSocketInputStream();
		resetSmockerSocketOutputStream();
		applyMock = false;
		reseNextWrite = false;
		streamResent = false;
		matchOutput = null;
		applyMock = RemoteServerChecker.getMockedHost().contains(host);
		indexForArrayCopy = 0;
	}

	public boolean isSsl() {
		return ssl;
	}

	public String getResponseMocked() {
		return responseMocked;
	}


	public void setResponseMocked(String responseMocked) {
		this.responseMocked = responseMocked;
	}


	public TeeInputStream getTeeInputStream() {
		return teeInputStream;
	}


	public void setTeeInputStream(TeeInputStream teeInputStream) {
		this.teeInputStream = teeInputStream;
	}


	public TeeOutputStream getTeeOutputStream() {
		return teeOutputStream;
	}


	public void setTeeOutputStream(TeeOutputStream teeOutputStream) {
		this.teeOutputStream = teeOutputStream;
		this.teeOutputStream.setHost(getHost());
		this.teeOutputStream.setSmockerContainer(this);
	}
	

	public void postCommunication() throws UnsupportedEncodingException {
		String lastReaden = null;
		if (getSmockerSocketInputStream() != null) {
			lastReaden = getSmockerSocketInputStream().getSmockerOutputStreamData().getString();
		}
		
		if (lastReaden != null && !lastReaden.isEmpty() &&  getSmockerSocketOutputStream() != null) {
			RestClientSmocker.getInstance().postCommunication(this);
		}
	}
	
	private String matchOutput = null;
	
	public String getMatchMock () throws UnsupportedEncodingException {
		if (matchOutput != null && matchOutput.equals("NO_MATCH")) {
			return null;
		}
		if (matchOutput != null) {
			return matchOutput;
		}
		
		String inputToCheck = getSmockerSocketOutputStream().getSmockerOutputStreamData().getString();
		String match = RestClientSmocker.getInstance().postCheckMatch(inputToCheck, host);
    	String matchResponse = ResponseReader.readValueFromResponse(match, "outputResponse");
    	matchOutput = matchResponse;
    	if (matchOutput == null || matchResponse.equals("NO_MATCH")) {
    		return null;
    	}
    	else {
    		matchOutput = RestClientSmocker.decode(matchResponse);
    		return matchOutput;
    	}
	}
	
//	private boolean resetMatchNextWrite = false;
//
//	public boolean isResetMatchNextWrite() {
//		return resetMatchNextWrite;
//	}
//
//	public void setResetMatchNextWrite(boolean resetMatchNextWrite) {
//		this.resetMatchNextWrite = resetMatchNextWrite;
//	}
	
	private boolean reseNextWrite = false;

	public boolean isReseNextWrite() {
		return reseNextWrite;
	}

	public void setReseNextWrite(boolean reseNextWrite) {
		this.reseNextWrite = reseNextWrite;
	}
	
}
