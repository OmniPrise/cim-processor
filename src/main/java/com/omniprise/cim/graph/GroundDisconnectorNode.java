package com.omniprise.cim.graph;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.omniprise.cim.model.Rdf;

public class GroundDisconnectorNode extends SwitchNode implements SingleTerminalEquipment {
	private static Logger log = LoggerFactory.getLogger(GroundDisconnectorNode.class); 

	public GroundDisconnectorNode(Rdf source, int model) {
		super(source, model);
		log.debug("Created GroundDisconnectorNode");
	}

	@Override
	public String getDisplayName() { return "GroundDisconnector"; }

	@Override
	public String getDiagramType() { return "gd"; }
}
