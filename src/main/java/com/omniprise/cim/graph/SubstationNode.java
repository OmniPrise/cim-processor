package com.omniprise.cim.graph;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.omniprise.cim.model.Rdf;

public class SubstationNode extends AbstractEquipmentContainerNode {
	private static Logger log = LoggerFactory.getLogger(SubstationNode.class); 

	public SubstationNode(Rdf source, int model) {
		super(source, model);
		log.debug("Created SubstationNode");
	}

	@Override
	public String getDiagramName() { return getName(); }

	@Override
	public String getDiagramType() { return "su"; }

	@Override
	public String getDisplayName() { return "Substation"; }
}
