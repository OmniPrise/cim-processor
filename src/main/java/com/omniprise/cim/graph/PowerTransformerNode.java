package com.omniprise.cim.graph;


import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.omniprise.cim.model.Rdf;

public class PowerTransformerNode extends AbstractEquipmentContainerNode {
	private static Logger log = LoggerFactory.getLogger(PowerTransformerNode.class); 

	private List<TransformerWindingNode> transformerWindings = new LinkedList<TransformerWindingNode>();
	
	public PowerTransformerNode(Rdf source, int model) {
		super(source, model);
		log.debug("Created PowerTransformerNode");
	}

	public void addTransformerWinding(TransformerWindingNode transformerWinding) {
		connect();
		transformerWindings.add(transformerWinding);
		setConnected(true);
	}

	public void removeTransformerWinding(TransformerWindingNode transformerWinding) {
		transformerWindings.remove(transformerWinding);
		disconnect();
	}
	
	public List<TransformerWindingNode> getTransformerWindings() {
		return transformerWindings;
	}

	@Override
	public SubstationNode getSubstation() {
		SubstationNode result = null;

		AbstractEquipmentContainerNode equipmentContainer = (AbstractEquipmentContainerNode)getEquipmentContainer();
		if (equipmentContainer != null) result = (SubstationNode)equipmentContainer;
		return result;
	}
	
	@Override
	public boolean isConnectedElement() {
		return true;
	}

	@Override
	public void setIsland(int island, Queue<RdfNode> queue, boolean assumeConnected) {
		if (!isVisited()) {
			super.setIsland(island, queue, assumeConnected);
			if (assumeConnected || !isOutage()) {
				for (TransformerWindingNode node : transformerWindings) {
					queue.add(node);
				}
			}
		}
	}

	public static String getNodeName() { return "PowerTransformer"; }

	@Override
	public String getDiagramType() { return "xf"; }
	
	@Override
	public String getDiagramShape() { return "doubleoctagon"; }

	@Override
	public String getDisplayName() { return "PowerTransformer"; }
	
	@Override
	public String toString() { 
		String result = super.toString();
		for (TransformerWindingNode transformerWinding: transformerWindings) {
			result += "," + transformerWinding.toIdentifier();
		}
		return result;
	}
}
