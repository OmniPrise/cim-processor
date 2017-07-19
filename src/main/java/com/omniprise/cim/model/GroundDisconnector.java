package com.omniprise.cim.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GroundDisconnector extends Switch {
	private static Logger log = LoggerFactory.getLogger(GroundDisconnector.class); 

	public GroundDisconnector() {
		log.debug("Created GroundDisconnector instance");
	}
	
	@Override
	protected String getDisplayName() { return "GroundDisconnector"; }
}
