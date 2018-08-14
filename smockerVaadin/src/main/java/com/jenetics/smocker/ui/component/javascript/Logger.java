package com.jenetics.smocker.ui.component.javascript;

import com.eclipsesource.v8.JavaVoidCallback;
import com.eclipsesource.v8.V8Array;
import com.eclipsesource.v8.V8Object;

public class Logger implements JavaVoidCallback {
	
	private StringBuffer sb = new StringBuffer();
	
	public Logger() {
		super();
	}

	@Override
	public void invoke(V8Object receiver, V8Array parameters) {
		if (parameters.length() > 0) {
			sb.append(parameters.get(0)).append(System.lineSeparator());
		}
	}

	@Override
	public String toString() {
		return sb.toString();
	}
	
	

}