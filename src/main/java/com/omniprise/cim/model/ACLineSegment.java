package com.omniprise.cim.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ACLineSegment extends ConductingEquipment {
	private static Logger log = LoggerFactory.getLogger(ACLineSegment.class); 

	public ACLineSegment() {
		log.debug("Created ACLineSegment instance");
	}
	
	@Override
	protected String getDisplayName() { return "ACLineSegment"; }
}
