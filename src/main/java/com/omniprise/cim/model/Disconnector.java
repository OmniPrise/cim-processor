package com.omniprise.cim.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Disconnector extends Switch {
	private static Logger log = LoggerFactory.getLogger(Disconnector.class); 

	public Disconnector() {
		log.debug("Created Disconnector instance");
	}
	
	@Override
	protected String getDisplayName() { return "Disconnector"; }
}
