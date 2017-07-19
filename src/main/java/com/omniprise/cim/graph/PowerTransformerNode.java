package com.omniprise.cim.graph;

/*-
 * #%L
 * ERCOT CIM Processor
 * %%
 * Copyright (C) 2017 OmniPrise, LLC
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */



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
