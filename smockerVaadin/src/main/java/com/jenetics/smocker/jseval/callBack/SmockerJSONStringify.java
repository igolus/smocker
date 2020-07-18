package com.jenetics.smocker.jseval.callBack;

import com.eclipsesource.v8.JavaCallback;
import com.eclipsesource.v8.V8Array;
import com.eclipsesource.v8.V8Object;

public class SmockerJSONStringify implements JavaCallback {

	public SmockerJSONStringify() {
		super();
	}

	@Override
	public Object invoke(V8Object receiver, V8Array parameters) {
		V8Object json = receiver.getObject("JSON");
		String resJson = json.executeStringFunction("stringify", parameters);
		return resJson;
	}

}