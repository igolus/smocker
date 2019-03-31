package com.jenetics.smocker.util;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;

import com.jenetics.smocker.util.io.TeeInputStream;
import com.jenetics.smocker.util.io.TeeOutputStream;
import com.jenetics.smocker.util.network.RemoteServerChecker;
import com.jenetics.smocker.util.network.ResponseReader;
import com.jenetics.smocker.util.network.RestClientSmocker;

public class SmockerContainer {
	private static final String SEP = ":";
	private SmockerSocketInputStream smockerSocketInputStream = null;
	private SmockerSocketOutputStream smockerSocketOutputStream = null;
	private ByteArrayOutputStream bosforMock = new ByteArrayOutputStream();
	
	private String ip = null;
	private String host = null;
	private int port = 0;
	private boolean ssl;
	private TeeInputStream teeInputStream;
	private TeeOutputStream teeOutputStream;
	private String stackTrace;
	
	private String outputToBesend;
	private String inputToBesend;
	private Object source;
	private boolean applyMock;
	private boolean streamResent = false;
	private int indexForArrayCopy = 0;
	
	public int getIndexForArrayCopy() {
		return indexForArrayCopy;
	}

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


	public String getIp() {
		return ip;
	}
	
	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}
	

	public SmockerContainer(String ip, String host, int port, String stackTrace, Object source) {
		this.ip = ip;
		this.host = checkDuplicateHost(host);
		this.port = port;
		this.stackTrace = stackTrace;
		this.source = source;
		
		this.applyMock = RemoteServerChecker.getMockedHosts().contains(host + ":" + port);
	}


	private String checkDuplicateHost(String host) {
		List<List<String>> duplicatedHosts = RemoteServerChecker.getDuplicatedHosts();
		if (duplicatedHosts == null) {
			return host;
		}
		for (List<String> listDup : duplicatedHosts) {
			if (listDup != null && listDup.contains(host)) {
				return listDup.get(0);
			}
		}
		return host;
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
		noMatch = false;
		matchOutput = null;
		applyMock = RemoteServerChecker.getMockedHosts().contains(host + SEP + port);
		indexForArrayCopy = 0;
	}

	public boolean isSsl() {
		return ssl;
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
		this.teeOutputStream.setHost(getIp());
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
	
	private byte[] matchOutput = null;
	private boolean noMatch = false;
	
	public byte[] getMatchMock () throws UnsupportedEncodingException {
		if (noMatch) {
			return null;
		}
		if (matchOutput != null) {
			return matchOutput;
		}
		
		String inputToCheck = getSmockerSocketOutputStream().getSmockerOutputStreamData().getString();
		String match = RestClientSmocker.getInstance().postCheckMatch(inputToCheck, host);
    	String matchResponse = ResponseReader.readValueFromResponse(match, "outputResponse");
    	if ( matchResponse == null || matchResponse.equals("NO_MATCH")) {
    		noMatch = true;
    		return null;
    	}
    	try {
    		matchOutput = RestClientSmocker.decodeByte(matchResponse); 
        	return matchOutput;
    	}
    	catch (Exception e) {
			MessageLogger.logThrowable(e, getClass());
		}
    	return null;
	}
	
	private boolean reseNextWrite = false;

	public boolean isReseNextWrite() {
		return reseNextWrite;
	}

	public void setReseNextWrite(boolean reseNextWrite) {
		this.reseNextWrite = reseNextWrite;
	}
	
}
