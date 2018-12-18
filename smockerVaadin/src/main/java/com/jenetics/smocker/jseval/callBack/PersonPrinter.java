package com.jenetics.smocker.jseval.callBack;

import com.eclipsesource.v8.JavaVoidCallback;
import com.eclipsesource.v8.V8Array;
import com.eclipsesource.v8.V8Object;

public class PersonPrinter implements JavaVoidCallback {
 @Override
 public void invoke(final V8Object receiver, final V8Array parameters) {
   System.out.println(receiver.getString("first") + parameters.get(0));
 }
}