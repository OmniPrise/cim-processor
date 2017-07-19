package com.omniprise.cim.graph;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.omniprise.cim.model.Rdf;

public class SeriesCompensatorNode extends SwitchNode implements DualTerminalEquipment {
	private static Logger log = LoggerFactory.getLogger(SeriesCompensatorNode.class); 

	public SeriesCompensatorNode(Rdf source, int model) {
		super(source, model);
		log.debug("Created SeriesCompensatorNode");
	}

	@Override
	public String getName() {
		String result = super.getName();
		if (getEquipmentContainer() != null) result = getEquipmentContainer().getName() + result;			
		
		return result;
	}

	@Override
	public String getDiagramType() { return "sc"; }

	@Override
	public String getDisplayName() { return "SeriesCompensator"; }
}
