package com.jenetics.smocker.annotation;

import java.lang.annotation.Retention;

import javax.enterprise.inject.Default;

import com.vaadin.server.FontAwesome;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
public @interface ContentView {

	public static final String NOT_SET = "NOT_SET";
	
	
	String viewName();
	String icon() default NOT_SET;
	int sortingOrder() default 0;
	boolean homeView() default false;
	String accordeonParent();
	boolean useFontAwasome() default false;
	String componentParent() default NOT_SET;
}



