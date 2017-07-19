package com.omniprise.cim.graph;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.omniprise.cim.model.Rdf;

public class DisconnectorNode extends SwitchNode implements DualTerminalEquipment {
	private static Logger log = LoggerFactory.getLogger(DisconnectorNode.class); 

	public DisconnectorNode(Rdf source, int model) {
		super(source, model);
		log.debug("Created DisconnectorNode");
	}

	@Override
	public String getDiagramType() { return "dsc"; }
	
	@Override
	public String getDiagramShape() { return "diamond"; }

	@Override
	public String getDisplayName() { return "Disconnector"; }
}
