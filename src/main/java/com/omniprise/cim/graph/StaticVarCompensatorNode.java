package com.omniprise.cim.graph;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.omniprise.cim.model.Rdf;

public class StaticVarCompensatorNode extends SwitchNode implements SingleTerminalEquipment {
	private static Logger log = LoggerFactory.getLogger(StaticVarCompensatorNode.class); 

	public StaticVarCompensatorNode(Rdf source, int model) {
		super(source, model);
		log.debug("Created StaticVarCompensatorNode");
	}

	@Override
	public String getDisplayName() { return "StaticVarCompensator"; }

	@Override
	public String getDiagramType() { return "svc"; }
}
