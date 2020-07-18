package com.jenetics.smocker.jseval.callBack;

import com.eclipsesource.v8.JavaCallback;
import com.eclipsesource.v8.V8Array;
import com.eclipsesource.v8.V8Object;

public class SmockerJSONParse implements JavaCallback {

	public SmockerJSONParse() {
		super();
	}

	@Override
	public Object invoke(V8Object receiver, V8Array parameters) {
		V8Object json = receiver.getObject("JSON");
		V8Object jsonObject = json.executeObjectFunction("parse", parameters);
		return jsonObject;
	}
}