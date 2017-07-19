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


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.omniprise.cim.model.Rdf;
import com.omniprise.cim.model.VoltageLevel;

public class VoltageLevelNode extends AbstractEquipmentContainerNode {
	private static Logger log = LoggerFactory.getLogger(VoltageLevelNode.class); 

	private SubstationNode substation = null;
	private BaseVoltageNode baseVoltage = null;

	public VoltageLevelNode(Rdf source, int model) {
		super(source, model);
		log.debug("Created VoltageLevelNode");
	}
	
	public BaseVoltageNode getBaseVoltage() {
		return baseVoltage;
	}

	public void setBaseVoltage(BaseVoltageNode baseVoltage) {
		this.baseVoltage = baseVoltage;
	}

	@Override
	public void connect() {
		super.connect();
		VoltageLevel voltageLevel = (VoltageLevel)getSource();
		if (baseVoltage == null && voltageLevel.getBaseVoltage() != null) baseVoltage = (BaseVoltageNode)RdfNode.findByNodeId(voltageLevel.getBaseVoltage(), getModel());
		if (substation == null) substation = (SubstationNode)RdfNode.findByNodeId(voltageLevel.getSubstation(), getModel());
	}

	@Override
	public void disconnect() {
		super.disconnect();
		baseVoltage = null;
		substation = null;
	}
	
	@Override
	public SubstationNode getSubstation() {
		return substation;
	}

	@Override
	public String getDiagramType() { return "vl"; }

	@Override
	public String getDiagramName() {
		String result = super.getDiagramName();
		
		if (substation != null) result = substation.getDiagramLabel() + "-" + result;
		
		return result; 
	}

	@Override
	public String getDisplayName() { return "VoltageLevel"; }

	@Override
	public String toString() { 
		String result = super.toString();
		VoltageLevel voltageLevel = (VoltageLevel)getSource();
		//RdfNode substation = RdfNode.findByNodeId(voltageLevel.getSubstation(), getModel());
		//RdfNode baseVoltage = RdfNode.findByNodeId(voltageLevel.getBaseVoltage(), getModel());
		if (getSubstation() == null) {
			result += ",Substation," + voltageLevel.getSubstation() + "-NotPresentInRdfModel";
		} else {
			result += "," + getSubstation().toIdentifier();
		}
		if (getBaseVoltage() == null) {
			result += ",BaseVoltage," + voltageLevel.getBaseVoltage() + "-NotPresentInRdfModel";
		} else {
			result += "," + getBaseVoltage().toIdentifier();
		}
		return result;
	}
}
