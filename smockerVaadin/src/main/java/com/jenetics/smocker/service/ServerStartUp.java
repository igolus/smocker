package com.jenetics.smocker.service;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;

import com.jenetics.smocker.rest.AliveEndPoint;

@Startup
@Singleton
public class ServerStartUp {
	@PostConstruct
	void init() {
		//AliveEndPoint.setInitialized(true);
	}
}
