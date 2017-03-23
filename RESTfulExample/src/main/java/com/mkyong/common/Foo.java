package com.mkyong.common;

public class Foo {
	public static void main(String args[]) {
		Foo f = new Foo();
	    f.doIt();
	  }
	 
	  void doIt() {
	    try {
	      System.out.println("ran doIt() method");
	      Thread.sleep(5000);
	    }
	    catch (Exception e) {
		  e.printStackTrace();
		}
	  }
}
