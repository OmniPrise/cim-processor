package com.omniprise.cim.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Discrete extends MeasurementResource {
	private static Logger log = LoggerFactory.getLogger(Discrete.class); 

	public Discrete() {
		log.debug("Created Discrete instance");
	}
	
	@Override
	protected String getDisplayName() { return "Discrete"; }
}
