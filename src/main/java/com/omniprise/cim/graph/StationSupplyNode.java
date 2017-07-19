package com.omniprise.cim.graph;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.omniprise.cim.model.Rdf;

public class StationSupplyNode extends ConductingEquipmentNode implements SingleTerminalEquipment {
	private static Logger log = LoggerFactory.getLogger(StationSupplyNode.class); 

	public StationSupplyNode(Rdf source, int model) {
		super(source, model);
		log.debug("Created StationSupplyNode");
	}

	@Override
	public String getDisplayName() { return "StationSupply"; }

	@Override
	public String getDiagramType() { return "ss"; }
}
