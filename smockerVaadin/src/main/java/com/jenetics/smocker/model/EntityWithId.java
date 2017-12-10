package com.jenetics.smocker.model;

import java.io.Serializable;

@FunctionalInterface
public interface EntityWithId extends Serializable {

	/**
	 * entity should have a getId method foe the generic
	 * 
	 * @return
	 */
	Long getId();

}