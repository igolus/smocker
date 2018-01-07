package com.jenetics.smocker.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.util.Hashtable;

import static com.jenetics.smocker.util.network.ResponseReader.*;
import static com.jenetics.smocker.util.network.Base64Util.*;

import com.jenetics.smocker.util.network.RestClientSmocker;

public class MockInputStream extends InputStream {

	private InputStream sourceIs;
	private Hashtable<Object, SmockerContainer> smockerContainerBySocket;
	private Socket sourceSocket;
	private boolean lookedIntoMock = false;
	private SmockerSocketOutputStreamData toReturn;
	
	public MockInputStream(InputStream is, Hashtable<Object, SmockerContainer> smockerContainerBySocket,
			Socket source) {
		this.sourceIs = is;
		this.smockerContainerBySocket = smockerContainerBySocket;
		this.sourceSocket = source;
	}

	@Override
	public int read() throws IOException {
		if (!lookedIntoMock) {
			toReturn = findMock();
		}
		if (toReturn != null) {
			return toReturn.read();
		}
		else {
			return sourceIs.read();
		}
	}

	private SmockerSocketOutputStreamData findMock() {
		SmockerContainer smockerContainer = smockerContainerBySocket.get(sourceIs);
		if (smockerContainer != null && 
				smockerContainer.getSmockerSocketOutputStream() != null && 
				smockerContainer.getSmockerSocketOutputStream().getSmockerOutputStreamData() != null ) {
			try {
				String request = smockerContainer.getSmockerSocketOutputStream().getSmockerOutputStreamData().getStringBase64();
				String mockResponse = RestClientSmocker.getInstance().findMock(request, TransformerUtility.getCallerApp(), 
						smockerContainer.getHost(), smockerContainer.getPort());
				if (readStatusCodeFromResponse(mockResponse).equals(OK_STATUS)) {
					String response = readValueFromResponse(mockResponse, "response");
					return new SmockerSocketOutputStreamData(decode(response));
				}
			} catch (Exception e) {
				MessageLogger.logErrorWithMessage("Unable to get any mock response", e, MockInputStream.class);
			} 
		}
		return null;
	}

}
