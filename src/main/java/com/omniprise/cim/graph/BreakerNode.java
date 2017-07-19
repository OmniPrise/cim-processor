package com.omniprise.cim.graph;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.omniprise.cim.model.Rdf;

public class BreakerNode extends SwitchNode implements DualTerminalEquipment {
	private static Logger log = LoggerFactory.getLogger(BreakerNode.class); 

	public BreakerNode(Rdf source, int model) {
		super(source, model);
		log.debug("Created BreakerNode");
	}

	@Override
	public String getDiagramType() { return "cb"; }
	
	@Override
	public String getDiagramShape() { return "rectangle"; }

	@Override
	public String getDisplayName() { return "Breaker"; }
}
