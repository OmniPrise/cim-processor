package com.omniprise.cim.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SynchronousMachine extends ConductingEquipment {
	private static Logger log = LoggerFactory.getLogger(SynchronousMachine.class); 

	public SynchronousMachine() {
		log.debug("Created SynchronousMachine instance");
	}
	
	@Override
	protected String getDisplayName() { return "SynchronousMachine"; }
}
