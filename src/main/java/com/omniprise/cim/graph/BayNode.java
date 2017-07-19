package com.omniprise.cim.graph;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.omniprise.cim.model.Rdf;

public class BayNode extends AbstractEquipmentContainerNode {
	private static Logger log = LoggerFactory.getLogger(BayNode.class); 
	
	public BayNode(Rdf source, int model) {
		super(source, model);
		log.debug("Created BayNode");
	}

	@Override
	public String getDiagramType() { return "bay"; }

	@Override
	public String toDiagram() { return "\t\t# node [fixedsize=shape shape=" + getDiagramShape() + " style=" + getDiagramStyle() + " color=" + getDiagramBorderColor() + " fillcolor=" + getDiagramFillColor() +" label=\"" + getDiagramLabel() + "\"] \"" + getDiagramName() + "\"; // "+ getDisplayName(); }

	@Override
	public String getDisplayName() { return "Bay"; }
}
