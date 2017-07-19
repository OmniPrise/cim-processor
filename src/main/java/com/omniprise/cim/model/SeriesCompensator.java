package com.omniprise.cim.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SeriesCompensator extends Switch {
	private static Logger log = LoggerFactory.getLogger(SeriesCompensator.class); 

	public SeriesCompensator() {
		log.debug("Created SeriesCompensator instance");
	}
	
	@Override
	protected String getDisplayName() { return "SeriesCompensator"; }
}
