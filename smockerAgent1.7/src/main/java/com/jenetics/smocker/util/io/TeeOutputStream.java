/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jenetics.smocker.util.io;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import com.jenetics.smocker.util.SmockerContainer;
import com.jenetics.smocker.util.SmockerSocketOutputStream;
import com.jenetics.smocker.util.network.RestClientSmocker;

/**
 * Classic splitter of OutputStream. Named after the unix 'tee' 
 * command. It allows a stream to be branched off so there 
 * are now two streams.
 *
 * @version $Id: TeeOutputStream.java 1686503 2015-06-19 21:32:13Z sebb $
 */
public class TeeOutputStream extends ProxyOutputStream {

	private boolean applyMock;
	private String host;
	private SmockerContainer smockerContainer;
	private boolean forwardToRealStream = true;

	/** the second OutputStream to write to */
	private SmockerSocketOutputStream branch;

	/**
	 * Constructs a TeeOutputStream.
	 * @param out the main OutputStream
	 * @param branch the second OutputStream
	 */
	public TeeOutputStream(final OutputStream out, final SmockerSocketOutputStream branch) {
		super(out);
		this.branch = branch;
	}

	public OutputStream getRealOutputSTream() {
		return out;
	}

	/**
	 * Write the bytes to both streams.
	 * @param b the bytes to write
	 * @throws IOException if an I/O error occurs
	 */
	@Override
	public synchronized void write(final byte[] b) throws IOException {
		if (forwardToRealStream) {
			super.write(b);
		}
		this.branch.write(b);
	}

	public void resetBranch() {
		branch = new SmockerSocketOutputStream();
	}

	public void setForwardToRealStream(boolean forwardToRealStream) {
		this.forwardToRealStream = forwardToRealStream;
	}

	public SmockerSocketOutputStream getBranch() {
		return branch;
	}

	/**
	 * Write the specified bytes to both streams.
	 * @param b the bytes to write
	 * @param off The start offset
	 * @param len The number of bytes to write
	 * @throws IOException if an I/O error occurs
	 */
	@Override
	public synchronized void write(final byte[] b, final int off, final int len) throws IOException {
		if (forwardToRealStream) {
			super.write(b, off, len);
		}
		this.branch.write(b, off, len);	
	}

	/**
	 * Write a byte to both streams.
	 * @param b the byte to write
	 * @throws IOException if an I/O error occurs
	 */
	@Override
	public synchronized void write(final int b) throws IOException {
		if (forwardToRealStream) {
			super.write(b);
		}
		this.branch.write(b);
	}

	/**
	 * Flushes both streams.
	 * @throws IOException if an I/O error occurs
	 */
	@Override
	public void flush() throws IOException {
		
		if (getSmockerContainer().isApplyMock())
		{
			byte[] matchMock = getSmockerContainer().getMatchMock();
			if (matchMock != null) {
				//getSmockerContainer().setResponseMocked(matchMock);
				return;
			}
			//send the initial flow to real stream
			else {
				out.write(getSmockerContainer().getSmockerSocketOutputStream().getBytes());
				out.flush();
			}
		}
		else {
			getSmockerContainer().setApplyMock(false);
		}
		this.branch.flush();
		super.flush();
		
		getSmockerContainer().setOutputToBesend(getSmockerContainer().getSmockerSocketOutputStream().getSmockerOutputStreamData().getString());
		if (getSmockerContainer().getSmockerSocketInputStream() != null && 
				!getSmockerContainer().getSmockerSocketInputStream().getSmockerOutputStreamData().getString().isEmpty()) {
			resetBranch();
		}
	}

	private void postCommunication() throws UnsupportedEncodingException {
		String lastReaden = null;
		if (getSmockerContainer().getSmockerSocketInputStream() != null) {
			lastReaden = getSmockerContainer().getSmockerSocketInputStream().getSmockerOutputStreamData().getString();
		}

		if (lastReaden != null && getSmockerContainer().getOutputToBesend() != null) {
			RestClientSmocker.getInstance().postCommunication(getSmockerContainer());
		}
	}

	/**
	 * Closes both output streams.
	 * 
	 * If closing the main output stream throws an exception, attempt to close the branch output stream.
	 * 
	 * If closing the main and branch output streams both throw exceptions, which exceptions is thrown by this method is
	 * currently unspecified and subject to change.
	 * 
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	@Override
	public void close() throws IOException {
		if (!applyMock) {
			postCommunication();
		}
		try {
			super.close();
		} finally {
			this.branch.close();
		}
	}

	public boolean isApplyMock() {
		return applyMock;
	}

	public void setApplyMock(boolean applyMock) {
		this.applyMock = applyMock;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public SmockerContainer getSmockerContainer() {
		return smockerContainer;
	}

	public void setSmockerContainer(SmockerContainer smockerContainer) {
		this.smockerContainer = smockerContainer;
	}
}
