package com.omniprise.cim.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Breaker extends Switch {
	private static Logger log = LoggerFactory.getLogger(Breaker.class); 

	public Breaker() {
		log.debug("Created Breaker instance");
	}
	
	@Override
	protected String getDisplayName() { return "Breaker"; }
}
