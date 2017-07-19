package com.omniprise.cim.graph;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.omniprise.cim.model.Rdf;

public class CustomerLoadNode extends ConductingEquipmentNode implements SingleTerminalEquipment {
	private static Logger log = LoggerFactory.getLogger(CustomerLoadNode.class); 

	public CustomerLoadNode(Rdf source, int model) {
		super(source, model);
		log.debug("Created CustomerLoadNode");
	}

	@Override
	public String getDiagramType() { return "ld"; }
	
	@Override
	public String getDiagramShape() { return "invtriangle"; }

	@Override
	public String getDisplayName() { return "CustomerLoad"; }
}
