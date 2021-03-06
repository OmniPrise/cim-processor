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

import com.omniprise.cim.model.ConductingEquipment;
import com.omniprise.cim.model.Rdf;

public class ConductingEquipmentNode extends EquipmentNode {
	private static Logger log = LoggerFactory.getLogger(ConductingEquipmentNode.class); 

	private List<TerminalNode> terminals = new LinkedList<TerminalNode>();
	private BaseVoltageNode baseVoltage = null;

	public ConductingEquipmentNode(Rdf source, int model) {
		super(source, model);
		log.debug("Created ConductingEquipmentNode");
	}
	
	public BaseVoltageNode getBaseVoltage() {
		return baseVoltage;
	}

	public void setBaseVoltage(BaseVoltageNode baseVoltage) {
		this.baseVoltage = baseVoltage;
	}

	public void addTerminal(TerminalNode terminal) {
		connect();
		terminals.add(terminal);
		setConnected(true);
	}

	public void removeTerminal(TerminalNode terminal) {
		terminals.remove(terminal);
		disconnect();
	}
	
	public List<TerminalNode> getTerminals() {
		return terminals;
	}
	
	@Override
	public void connect() {
		super.connect();
		ConductingEquipment conductingEquipment = (ConductingEquipment)getSource();
		if (baseVoltage == null && conductingEquipment.getBaseVoltage() != null) baseVoltage = (BaseVoltageNode)RdfNode.findByNodeId(conductingEquipment.getBaseVoltage(), getModel());
	}
	
	@Override
	public void disconnect() {
		super.disconnect();
		baseVoltage = null;
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
				for (TerminalNode node : terminals) {
					queue.add(node);
				}
			}
		}
	}

	@Override
	public String getDisplayName() { return "ConductingEquipment"; }
	
	@Override
	public String toString() { 
		String result = super.toString();
		ConductingEquipment conductingEquipment = (ConductingEquipment)getSource();
		//RdfNode equipmentContainer = RdfNode.findByNodeId(conductingEquipment.getEquipmentContainer(),getModel());
		//RdfNode baseVoltage = RdfNode.findByNodeId(conductingEquipment.getBaseVoltage(), getModel());
		if (getEquipmentContainer() != null && VoltageLevelNode.class.isInstance(getEquipmentContainer())) {
			// toString() won't recurse
			result += "," + getEquipmentContainer().toString();
		}
		if (getBaseVoltage() == null) {
			result += ",BaseVoltage," + conductingEquipment.getBaseVoltage() + "-NotPresentInRdfModel";
		} else {
			result += "," + getBaseVoltage().toIdentifier();
		}
		for (TerminalNode terminal : terminals) {
			result += "," + terminal.toIdentifier();
		}
		return result;
	}
}
