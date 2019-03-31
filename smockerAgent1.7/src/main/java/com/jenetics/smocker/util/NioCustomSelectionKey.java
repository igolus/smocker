package com.jenetics.smocker.util;

import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;

public class NioCustomSelectionKey extends SelectionKey {

	private Selector selectorNio;
	private SelectableChannel socketChannel;

	public NioCustomSelectionKey(Selector selectorNio, SelectableChannel socketChannel) {
		super();
		this.selectorNio = selectorNio;
		this.socketChannel = socketChannel;
	}

	@Override
	public Selector selector() {
		return selectorNio;
	}
	
	@Override
	public int readyOps() {
		return SelectionKey.OP_READ;
	}
	
	@Override
	public boolean isValid() {
		return true;
	}
	
	@Override
	public SelectionKey interestOps(int ops) {
		return this;
	}
	
	@Override
	public int interestOps() {
		return SelectionKey.OP_READ;
	}
	
	@Override
	public SelectableChannel channel() {
		return socketChannel;
	}

	@Override
	/**
	 * Nothing to do for cancel
	 */
	public void cancel() {
		
	}

}
