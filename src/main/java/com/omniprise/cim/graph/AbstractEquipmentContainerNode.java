package com.omniprise.cim.graph;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.omniprise.cim.model.Rdf;

public abstract class AbstractEquipmentContainerNode extends EquipmentNode implements EquipmentContainerNode {
	private static Logger log = LoggerFactory.getLogger(AbstractEquipmentContainerNode.class); 

	public AbstractEquipmentContainerNode(Rdf source, int model) {
		super(source, model);
		log.debug("Created EquipmentContainerNode");
	}

	@Override
	public String getDisplayName() { return "EquipmentCDontainer"; }
}
