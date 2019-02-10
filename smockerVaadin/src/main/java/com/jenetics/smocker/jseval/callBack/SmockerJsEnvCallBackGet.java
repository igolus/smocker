package com.jenetics.smocker.jseval.callBack;

import com.eclipsesource.v8.JavaVoidCallback;
import com.eclipsesource.v8.V8Array;
import com.eclipsesource.v8.V8Object;
import com.jenetics.smocker.jseval.SmockerJsEnv;

public class SmockerJsEnvCallBackGet implements JavaVoidCallback {

	public SmockerJsEnvCallBackGet() {
		super();
	}

	@Override
	public void invoke(V8Object receiver, V8Array parameters) {
		String fromMap = SmockerJsEnv.getInstance().getFromMap(parameters.get(0));
		receiver.add("result", fromMap);
	}
}