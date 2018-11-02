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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import com.jenetics.smocker.util.SmockerContainer;
import com.jenetics.smocker.util.SmockerSocketInputStream;

/**
 * InputStream proxy that transparently writes a copy of all bytes read
 * from the proxied stream to a given OutputStream. Using {@link #skip(long)}
 * or {@link #mark(int)}/{@link #reset()} on the stream will result on some
 * bytes from the input stream being skipped or duplicated in the output
 * stream.
 * <p>
 * The proxied input stream is closed when the {@link #close()} method is
 * called on this proxy. It is configurable whether the associated output
 * stream will also closed.
 *
 * @version $Id: TeeInputStream.java 1586350 2014-04-10 15:57:20Z ggregory $
 * @since 1.4
 */
public class TeeInputStream extends ProxyInputStream {
	
	private boolean applyMock;
	private String host;
	private SmockerContainer smockerContainer;
	
    /**
     * The output stream that will receive a copy of all bytes read from the
     * proxied input stream.
     */
    private SmockerSocketInputStream branch;

    /**
     * Flag for closing also the associated output stream when this
     * stream is closed.
     */
    private final boolean closeBranch;
	private ByteArrayInputStream mockBis;

    /**
     * Creates a TeeInputStream that proxies the given {@link InputStream}
     * and copies all read bytes to the given {@link OutputStream}. The given
     * output stream will not be closed when this stream gets closed.
     *
     * @param input input stream to be proxied
     * @param branch output stream that will receive a copy of all bytes read
     */
    public TeeInputStream(final InputStream input, SmockerSocketInputStream branch, SmockerContainer smockerContainer) {
        this(input, branch, false, smockerContainer);
    }

    /**
     * Creates a TeeInputStream that proxies the given {@link InputStream}
     * and copies all read bytes to the given {@link OutputStream}. The given
     * output stream will be closed when this stream gets closed if the
     * closeBranch parameter is {@code true}.
     *
     * @param input input stream to be proxied
     * @param branch output stream that will receive a copy of all bytes read
     * @param closeBranch flag for closing also the output stream when this
     *                    stream is closed
     */
    public TeeInputStream(
            final InputStream input, SmockerSocketInputStream branch, final boolean closeBranch, SmockerContainer smockerContainer) {
        super(input);
        this.branch = branch;
        this.closeBranch = closeBranch;
        this.smockerContainer = smockerContainer;
    }
    
    public void resetBranch() {
    	branch = new SmockerSocketInputStream();
    }
    
    

    public SmockerSocketInputStream getBranch() {
		return branch;
	}

	/**
     * Closes the proxied input stream and, if so configured, the associated
     * output stream. An exception thrown from one stream will not prevent
     * closing of the other stream.
     *
     * @throws IOException if either of the streams could not be closed
     */
    @Override
    public void close() throws IOException {
        try {
            super.close();
        } finally {
            if (closeBranch) {
                branch.close();
            }
        }
    }
    
    public void initiateMockedResponse() {
    	if (smockerContainer != null && smockerContainer.getResponseMocked() != null && mockBis == null) {
    		this.mockBis = new ByteArrayInputStream(smockerContainer.getResponseMocked().getBytes());
    	}
    }
    
    public void resetMockBis()  {
    	this.mockBis = null;
    }

    /**
     * Reads a single byte from the proxied input stream and writes it to
     * the associated output stream.
     *
     * @return next byte from the stream, or -1 if the stream has ended
     * @throws IOException if the stream could not be read (or written) 
     */
    @Override
    public int read() throws IOException {
    	initiateMockedResponse();
    	final int ch = super.read();
        if (ch != EOF && mockBis == null) {
            branch.write(ch);
        }
        if (mockBis != null) {
        	return mockBis.read(); 
        }
        if (ch == -1) {
        	postCommunication();
        }
        return ch;
    }

    private void postCommunication() throws UnsupportedEncodingException {
		if (getSmockerContainer().isPostAtNextRead()) {
			getSmockerContainer().postCommunication();
			getSmockerContainer().setPostAtNextRead(false);
		}
	}

	@Override
	public int available() throws IOException {
    	initiateMockedResponse();
    	if (mockBis != null) {
        	return mockBis.available(); 
        }
		return super.available();
	}

	/**
     * Reads bytes from the proxied input stream and writes the read bytes
     * to the associated output stream.
     *
     * @param bts byte buffer
     * @param st start offset within the buffer
     * @param end maximum number of bytes to read
     * @return number of bytes read, or -1 if the stream has ended
     * @throws IOException if the stream could not be read (or written) 
     */
    @Override
    public int read(final byte[] bts, final int st, final int end) throws IOException {
    	initiateMockedResponse();
    	
    	if (mockBis != null) {
        	return mockBis.read(bts, st, end);
        }
    	final int n = super.read(bts, st, end);
        if (n != -1 && mockBis == null) {
            branch.write(bts, st, n);
        }
        if (n == -1) {
        	postCommunication();
        }
        return n;
    }

    /**
     * Reads bytes from the proxied input stream and writes the read bytes
     * to the associated output stream.
     *
     * @param bts byte buffer
     * @return number of bytes read, or -1 if the stream has ended
     * @throws IOException if the stream could not be read (or written) 
     */
    @Override
    public int read(final byte[] bts) throws IOException {
    	initiateMockedResponse();
    	final int n = super.read(bts);
        if (n != EOF && mockBis == null) {
            branch.write(bts, 0, n);
        }
        if (mockBis != null) {
        	return mockBis.read(bts);
        }
        return n;
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
