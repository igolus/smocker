package com.jenetics.smocker.util;

import java.io.PrintWriter;
import java.io.StringWriter;

public class SmockerUtility {
	
	public static String getStackTrace(final Throwable throwable) {
	     final StringWriter sw = new StringWriter();
	     final PrintWriter pw = new PrintWriter(sw, true);
	     throwable.printStackTrace(pw);
	     return sw.getBuffer().toString();
	}
}
