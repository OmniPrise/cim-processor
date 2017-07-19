package com.omniprise.cim.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShuntCompensator extends Switch {
	private static Logger log = LoggerFactory.getLogger(ShuntCompensator.class); 

	public ShuntCompensator() {
		log.debug("Created ACLineSegment instance");
	}
	
	@Override
	protected String getDisplayName() { return "ShuntCompensator"; }
}
