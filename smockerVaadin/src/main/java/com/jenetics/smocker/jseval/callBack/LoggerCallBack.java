package com.jenetics.smocker.jseval.callBack;

import org.apache.commons.lang3.StringUtils;

import com.eclipsesource.v8.JavaVoidCallback;
import com.eclipsesource.v8.V8Array;
import com.eclipsesource.v8.V8Object;

public class LoggerCallBack implements JavaVoidCallback {
	
	private StringBuffer sb = new StringBuffer();
	
	public LoggerCallBack() {
		super();
	}

	@Override
	public void invoke(V8Object receiver, V8Array parameters) {
		if (parameters.length() > 0) {
			if (!StringUtils.isEmpty(sb.toString())) {
				sb.append(System.lineSeparator());
			}
			sb.append(parameters.get(0));
		}
	}

	@Override
	public String toString() {
		return sb.toString();
	}
	
	public void reset() {
		sb = new StringBuffer();
	}

}