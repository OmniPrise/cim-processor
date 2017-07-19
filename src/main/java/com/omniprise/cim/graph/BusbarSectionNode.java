package com.omniprise.cim.graph;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.omniprise.cim.model.Rdf;

public class BusbarSectionNode extends ConductingEquipmentNode implements EquipmentContainerNode, SingleTerminalEquipment {
	private static Logger log = LoggerFactory.getLogger(BusbarSectionNode.class); 

	public BusbarSectionNode(Rdf source, int model) {
		super(source, model);
		log.debug("Created BusbarSectionNode");
	}

	@Override
	public String getDiagramType() { return "bs"; }

	@Override
	public String getDiagramName() { return super.getDiagramName() + "-BusbarSection"; }

	@Override
	public String toDiagram() { return "\t\tnode [shape=plaintext label=\"" + getDiagramLabel() + "\"] \"" + getDiagramName() + "\"; // BusbarSection"; }

	@Override
	public String getDisplayName() { return "BusbarSection"; }
}
