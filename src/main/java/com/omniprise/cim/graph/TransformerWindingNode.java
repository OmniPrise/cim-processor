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


import java.util.Queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.omniprise.cim.model.Rdf;
import com.omniprise.cim.model.TransformerWinding;

public class TransformerWindingNode extends ConductingEquipmentNode implements SingleTerminalEquipment, ContainerConnectedEquipment {
	private static Logger log = LoggerFactory.getLogger(TransformerWindingNode.class); 

	private PowerTransformerNode powerTransformer;
	
	public TransformerWindingNode(Rdf source, int model) {
		super(source, model);
		log.debug("Created TransformerWindingNode");
	}
	
	public PowerTransformerNode getPowerTransformer() {
		return powerTransformer;
	}

	@Override
	public void connect() {
		super.connect();
		TransformerWinding transformerWinding = (TransformerWinding)getSource();
		if (powerTransformer == null) powerTransformer = (PowerTransformerNode)RdfNode.findByNodeId(transformerWinding.getPowerTransformer(), getModel());
		if (powerTransformer != null) {
			powerTransformer.addTransformerWinding(this);
		}
	}

	@Override
	public void disconnect() {
		if (powerTransformer != null) {
			powerTransformer.removeTransformerWinding(this);
		}
		super.disconnect();
		powerTransformer = null;
	}

	@Override
	public void addTerminal(TerminalNode terminal) {
		super.addTerminal(terminal);
	}

	@Override
	public void setIsland(int island, Queue<RdfNode> queue, boolean assumeConnected) {
		if (!isVisited()) {
			super.setIsland(island, queue, assumeConnected);
			if (powerTransformer != null) queue.add(powerTransformer);
		}
	}

	@Override
	public SubstationNode getSubstation() {
		SubstationNode result = null;

		if (powerTransformer != null) result = powerTransformer.getSubstation();
		return result;
	}

	@Override
	public String getDisplayName() { return "TransformerWinding"; }

	@Override
	public String getDiagramName() {
		String result = super.getDiagramName();
		
		if (powerTransformer != null) result = powerTransformer.getDiagramName() + "-" + result;
		if (getBaseVoltage() != null) result = result + "-" + getBaseVoltage().getNominalVoltage();
		
		return result; 
	}

	@Override
	public String getDiagramType() { return "tw"; }
	
	@Override
	public String getDiagramShape() { return "doublecircle"; }
	
	@Override
	public String toString() { 
		String result = super.toString();
		TransformerWinding transformerWinding = (TransformerWinding)getSource();
		//RdfNode powerTransformer = RdfNode.findByNodeId(transformerWinding.getPowerTransformer(),getModel());
		if (getPowerTransformer() == null) {
			result += ",PowerTransformer," + transformerWinding.getPowerTransformer() + "-NotPresentInRdfModel";
		} else {
			result += "," + getPowerTransformer().toIdentifier();
		}
		return result;
	}
}
