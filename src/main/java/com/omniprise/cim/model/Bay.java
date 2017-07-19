package com.omniprise.cim.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Bay extends EquipmentContainer {
	private static Logger log = LoggerFactory.getLogger(Bay.class);

	Bay() {
		log.debug("Created Bay instance");
	}
	
	@Override
	protected String getDisplayName() { return "Bay"; }
}
