package com.jenetics.smocker.annotation;

import java.lang.annotation.Retention;

import javax.enterprise.inject.Default;

import com.vaadin.server.FontAwesome;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
public @interface RootView {
	public static final String NOT_SET = "NOT_SET";
	
	
	String viewName();
	String icon() default NOT_SET;
	boolean useFontAwasome() default false;
}
