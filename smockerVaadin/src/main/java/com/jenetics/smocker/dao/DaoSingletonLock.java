package com.jenetics.smocker.dao;

import java.util.concurrent.locks.ReentrantLock;

public class DaoSingletonLock {
	
	
	private DaoSingletonLock() {
		super();
	}

	private static ReentrantLock lock = new ReentrantLock();

	public static ReentrantLock getLock() {
		return lock;
	}
	
	public static void lock() {
		lock.lock();
	}
	
	public static void unlock() {
		lock.unlock();
	}
	
}
