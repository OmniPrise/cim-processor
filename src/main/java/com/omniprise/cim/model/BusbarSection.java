package com.omniprise.cim.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BusbarSection extends ConductingEquipment {
	private static Logger log = LoggerFactory.getLogger(BusbarSection.class); 

	public BusbarSection() {
		log.debug("Created BusbarSection instance");
	}
	
	@Override
	protected String getDisplayName() { return "BusbarSection"; }
}
