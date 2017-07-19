package com.omniprise.cim.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomerLoad extends ConductingEquipment {
	private static Logger log = LoggerFactory.getLogger(CustomerLoad.class); 

	public CustomerLoad() {
		log.debug("Created CustomerLoad instance");
	}
	
	@Override
	protected String getDisplayName() { return "CustomerLoad"; }
}
