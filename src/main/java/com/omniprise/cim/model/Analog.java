package com.omniprise.cim.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Analog extends MeasurementResource {
	private static Logger log = LoggerFactory.getLogger(Analog.class); 

	public Analog() {
		log.debug("Created Analog instance");
	}
	
	@Override
	protected String getDisplayName() { return "Analog"; }
}
