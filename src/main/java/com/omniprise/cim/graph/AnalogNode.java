package com.omniprise.cim.graph;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.omniprise.cim.model.Rdf;

public class AnalogNode extends MeasurementResourceNode {
	private static Logger log = LoggerFactory.getLogger(AnalogNode.class); 

	public AnalogNode(Rdf source, int model) {
		super(source, model);
		log.debug("Created AnalogNode");
	}

	@Override
	public String getDisplayName() { return "Analog"; }
}
