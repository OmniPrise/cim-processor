package com.omniprise.cim.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EquipmentContainer extends Equipment {
	private static Logger log = LoggerFactory.getLogger(EquipmentContainer.class); 
	
	public EquipmentContainer() {
		log.debug("Created EquipmentContainer instance");
	}
	
	@Override
	protected String getDisplayName() { return "EquipmentContainer"; }
}
