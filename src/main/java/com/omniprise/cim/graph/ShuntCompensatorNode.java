package com.omniprise.cim.graph;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.omniprise.cim.model.Rdf;

public class ShuntCompensatorNode extends SwitchNode implements SingleTerminalEquipment {
	private static Logger log = LoggerFactory.getLogger(ShuntCompensatorNode.class); 

	public ShuntCompensatorNode(Rdf source, int model) {
		super(source, model);
		log.debug("Created ShuntCompensatorNode");
	}

	@Override
	public String getDisplayName() { return "ShuntCompensator"; }

	@Override
	public String getDiagramType() { return "sh"; }
}
