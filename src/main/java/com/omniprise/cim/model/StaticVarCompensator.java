package com.omniprise.cim.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StaticVarCompensator extends Switch {
	private static Logger log = LoggerFactory.getLogger(StaticVarCompensator.class); 

	public StaticVarCompensator() {
		log.debug("Created StationSupply instance");
	}
	
	@Override
	protected String getDisplayName() { return "StaticVarCompensator"; }
}
