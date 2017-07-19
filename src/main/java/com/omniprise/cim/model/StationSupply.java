package com.omniprise.cim.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StationSupply extends ConductingEquipment {
	private static Logger log = LoggerFactory.getLogger(StationSupply.class); 

	public StationSupply() {
		log.debug("Created StationSupply instance");
	}
	
	@Override
	protected String getDisplayName() { return "StationSupply"; }
}
