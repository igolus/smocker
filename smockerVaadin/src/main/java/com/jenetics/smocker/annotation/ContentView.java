package com.jenetics.smocker.annotation;

import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
public @interface ContentView {

	String viewName();
	String icon();
}
