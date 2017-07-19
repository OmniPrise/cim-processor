package com.omniprise.cim.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EndCap extends ConductingEquipment {
	private static Logger log = LoggerFactory.getLogger(EndCap.class); 

	public EndCap() {
		log.debug("Created EndCap instance");
	}
	
	@Override
	protected String getDisplayName() { return "EndCap"; }
}
