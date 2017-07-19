package com.omniprise.cim.graph;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.omniprise.cim.model.Rdf;

public class DiscreteNode extends MeasurementResourceNode {
	private static Logger log = LoggerFactory.getLogger(DiscreteNode.class); 

	public DiscreteNode(Rdf source, int model) {
		super(source, model);
		log.debug("Created DiscreteNode");
	}

	@Override
	public String getDisplayName() { return "Discrete"; }
}
