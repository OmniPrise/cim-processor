package com.omniprise.cim.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PowerSystemResource extends OutageResource {
	private static Logger log = LoggerFactory.getLogger(PowerSystemResource.class); 
	public PowerSystemResource() {
		log.debug("Created PowerSystemResource instance");
	}
	
	@Override
	protected String getDisplayName() { return "PowerSystemResource"; }
}
