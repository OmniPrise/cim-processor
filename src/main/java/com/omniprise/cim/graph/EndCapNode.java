package com.omniprise.cim.graph;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.omniprise.cim.model.Rdf;

public class EndCapNode extends ConductingEquipmentNode implements SingleTerminalEquipment {
	private static Logger log = LoggerFactory.getLogger(EndCapNode.class); 

	public EndCapNode(Rdf source, int model) {
		super(source, model);
		log.debug("Created EndCapNode");
	}

	@Override
	public String getDisplayName() { return "EndCap"; }

	@Override
	public String getDiagramType() { return "ec"; }
}
