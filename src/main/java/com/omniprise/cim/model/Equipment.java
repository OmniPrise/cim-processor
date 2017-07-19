package com.omniprise.cim.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Equipment extends PowerSystemResource {
	private static Logger log = LoggerFactory.getLogger(Equipment.class); 


	public Equipment() {
		log.debug("Created Equipment instance");
	}
	
	@Override
	protected String getDisplayName() { return "Equipment"; }
	
	@Override
	public String toString() { return super.toString() + "," + getEquipmentContainer(); }
}
