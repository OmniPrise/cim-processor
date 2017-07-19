package com.omniprise.cim.graph;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.omniprise.cim.model.Rdf;

public class EquipmentNode extends PowerSystemResourceNode {
	private static Logger log = LoggerFactory.getLogger(EquipmentNode.class); 
	
	public EquipmentNode(Rdf source, int model) {
		super(source, model);
		log.debug("Created EquipmentNode");
	}
	
	@Override
	public String getDisplayName() { return "Equipment"; }
}
