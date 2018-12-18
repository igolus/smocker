package com.jenetics.smocker.jseval.callBack;

import com.eclipsesource.v8.JavaVoidCallback;
import com.eclipsesource.v8.V8Array;
import com.eclipsesource.v8.V8Object;
import com.jenetics.smocker.jseval.SmockerJsEnv;

public class SmockerJsEnvCallBackRemove implements JavaVoidCallback {

	public SmockerJsEnvCallBackRemove() {
		super();
	}

	@Override
	public void invoke(V8Object receiver, V8Array parameters) {
		SmockerJsEnv.getInstance().removeFromMap(parameters.getString(0));
	}
}