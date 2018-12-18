package com.jenetics.smocker.jseval.callBack;

import com.eclipsesource.v8.JavaVoidCallback;
import com.eclipsesource.v8.V8Array;
import com.eclipsesource.v8.V8Object;
import com.jenetics.smocker.jseval.SmockerJsEnv;

public class SmockerJsEnvCallBackAdd implements JavaVoidCallback {

	public SmockerJsEnvCallBackAdd() {
		super();
	}

	@Override
	public void invoke(V8Object receiver, V8Array parameters) {
		String key = parameters.getString(0);
		String value = parameters.getString(1);
		SmockerJsEnv.getInstance().addToMap(key, value);
	}
}