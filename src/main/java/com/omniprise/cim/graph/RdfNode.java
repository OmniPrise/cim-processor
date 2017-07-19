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


import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.omniprise.cim.model.Rdf;

public abstract class RdfNode {
	private static Logger log = LoggerFactory.getLogger(RdfNode.class); 
	private static Map<Integer,Map<String,RdfNode>> rdfModelNodes = new HashMap<Integer,Map<String,RdfNode>>();
	private static long rdfSeqId = 0;

	private int model;
	private long seqId;
	private String id;
	private String name;
	private Rdf source;
	private boolean connected = false;
	private boolean visited = false;
	private int island = -1;
	private RdfNode path = null;
	private boolean diagramNodeOfInterest = false;
	private EquipmentNode equipmentContainer;

	public static void releaseModel(int model) {
		Map<String,RdfNode> rdfModel = rdfModelNodes.get(model);
		for (String rdfId : Rdf.getAllIds()) {
			RdfNode node = rdfModel.remove(rdfId);
			if (node != null) node.disconnect();
		}
	}
	
	private RdfNode() {
	}
	
	public RdfNode(Rdf source, int model) {
		this.model = model;
		this.seqId = rdfSeqId++;
		this.source = source;
		this.id = source.getId();
		this.name = source.getName();
		Map<String,RdfNode> rdfNodes = rdfModelNodes.get(model);
		if (rdfNodes == null) {
			rdfNodes = new HashMap<String,RdfNode>();
			rdfModelNodes.put(model, rdfNodes);
		}
		rdfNodes.put(source.getId(), this);
		log.debug("Created RdfNode");
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public boolean isVisited() {
		return visited;
	}

	public void setVisited(boolean visited) {
		this.visited = visited;
	}

	public Rdf getSource() {
		return source;
	}

	public void setSource(Rdf source) {
		this.source = source;
	}

	public int getIsland() {
		return island;
	}

	public void setIsland(int island) {
		this.island = island;
	}
	
	public boolean isConnectedElement() {
		return false;
	}

	public boolean isConnected() {
		return connected;
	}

	public void setConnected(boolean connected) {
		this.connected = connected;
	}
	
	public long getSeqId() {
		return seqId;
	}

	public void setSeqId(long seqId) {
		this.seqId = seqId;
	}

	public int getModel() {
		return model;
	}

	public void setModel(int model) {
		this.model = model;
	}

	public RdfNode getPath() {
		return path;
	}

	public void setPath(RdfNode path) {
		this.path = path;
	}

	public boolean isDiagramNodeOfInterest() {
		return diagramNodeOfInterest;
	}

	public void setDiagramNodeOfInterest(boolean diagramNodeOfInterest) {
		this.diagramNodeOfInterest = diagramNodeOfInterest;
	}

	public void setIsland(int island, Queue<RdfNode> queue, boolean assumeConnected) {
		if (!visited) {
			this.island = island;
			visited = true;
		}
	}
	
	public static Set<String> getRdfNodeIds(int model) {
		Set<String> result = null;
		Map<String,RdfNode> rdfNodes = rdfModelNodes.get(model);
		if (rdfNodes != null) result = rdfNodes.keySet();
		return result;
	}
	
	public static RdfNode findByNodeId(String rdfNodeId, int model) {
		RdfNode result = null;
		Map<String,RdfNode> rdfNodes = rdfModelNodes.get(model);
		if (rdfNodes != null) result = rdfNodes.get(rdfNodeId);
		return result;
	}
	
	public void connect() {
		if (equipmentContainer == null) {
			Rdf equipment = (Rdf)getSource();
			equipmentContainer = (AbstractEquipmentContainerNode)RdfNode.findByNodeId(equipment.getEquipmentContainer(), this.getModel());
			if (equipmentContainer != null) equipmentContainer.connect();
		}
	}
	
	public void disconnect() {
		if (equipmentContainer != null) {
			equipmentContainer.disconnect();
		}
		equipmentContainer = null;
	}

	public EquipmentNode getEquipmentContainer() {
		return equipmentContainer;
	}

	public SubstationNode getSubstation() {
		SubstationNode result = null;

		if (equipmentContainer != null) result = equipmentContainer.getSubstation();
		return result;
	}
	
	public String getDisplayName() { return "Rdf"; }
	
	public String getDiagramLabel() { return getName(); }
	
	public String getDiagramType() { return "rdf"; }

	public String getDiagramName() {
		String result = getDiagramType() + "-" + getName();
		
		if (equipmentContainer != null) result = equipmentContainer.getDiagramName() + "-" + result;
		
		return result; 
	}
	
	public String getDiagramBorderColor() { return (isDiagramNodeOfInterest() ? "cyan" : "black"); }
	
	public String getDiagramFillColor() { return "lightgrey"; }
	
	public String getDiagramStyle() { 
		return "\"filled" + (isDiagramNodeOfInterest() ? ",bold" : "") + "\""; 
	}
	
	public String getDiagramShape() { return "pentagon"; }
	
	public String toDiagram() { return "\t\tnode [fixedsize=shape shape=" + getDiagramShape() + " style=" + getDiagramStyle() + " color=" + getDiagramBorderColor() + " fillcolor=" + getDiagramFillColor() +" label=\"" + getDiagramLabel() + "\"] \"" + getDiagramName() + "\"; // "+ getDisplayName() + " " + getId(); }
	
	public String toString() { return toIdentifier() + ",\"" + source.getDescription() + "\""; }

	public String toIdentifier() { return getDisplayName() + "," + seqId + "," + getId() + ",\"" + getName() + "\""; }
}
