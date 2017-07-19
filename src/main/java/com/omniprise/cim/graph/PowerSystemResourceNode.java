package com.omniprise.cim.graph;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.omniprise.cim.model.Rdf;

public class PowerSystemResourceNode extends OutageResourceNode {
	private static Logger log = LoggerFactory.getLogger(PowerSystemResourceNode.class); 

	public PowerSystemResourceNode(Rdf source, int model) {
		super(source, model);
		log.debug("Created PowerSystemResourceNode");
	}

	@Override
	public String getDisplayName() { return "PowerSystemResource"; }
}
