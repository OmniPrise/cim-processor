package com.omniprise.cim.graph;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.omniprise.cim.model.Rdf;

public class SynchronousMachineNode extends ConductingEquipmentNode implements SingleTerminalEquipment {
	private static Logger log = LoggerFactory.getLogger(SynchronousMachineNode.class); 

	public SynchronousMachineNode(Rdf source, int model) {
		super(source, model);
		log.debug("Created SynchronousMachineNode");
	}

	@Override
	public String getDisplayName() { return "SynchronousMachine"; }

	@Override
	public String getDiagramType() { return "gn"; }
}
