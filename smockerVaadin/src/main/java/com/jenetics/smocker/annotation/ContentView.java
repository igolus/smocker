package com.jenetics.smocker.annotation;

import java.lang.annotation.Retention;

import javax.enterprise.inject.Default;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
public @interface ContentView {

	String viewName();
	String icon();
	int sortingOrder() default 0;
	boolean homeView() default false;
}
