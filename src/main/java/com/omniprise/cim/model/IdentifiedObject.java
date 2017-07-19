package com.omniprise.cim.model;

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

import com.omniprise.cim.parse.model.Element;

public abstract class IdentifiedObject {
	private static Logger log = LoggerFactory.getLogger(IdentifiedObject.class); 
	private static long rdfSeqId = 0;

	// names are not guaranteed unique, even within the scope of a given class
	private String name;
	private long seqId;
	private String description = "";

	protected IdentifiedObject() {
		log.debug("Created IdentifiedObject instance");
		this.seqId = rdfSeqId++;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public long getSeqId() {
		return seqId;
	}

	public void setSeqId(long seqId) {
		this.seqId = seqId;
	}

	public void processAttribute(Element element, String value) {
		if (!handleAttribute(element,value)) {
			if ( ("cim".equals(element.getNamespace()) && "Terminal".equals(element.getClassName()) && "ConductingEquipment".equals(element.getName())) 
					|| ("cim".equals(element.getNamespace()) && "Terminal".equals(element.getClassName()) && "ConnectivityNode".equals(element.getName())) 
					|| ("cim".equals(element.getNamespace()) && "VoltageLevel".equals(element.getClassName()) && "BaseVoltage".equals(element.getName()))
					|| ("cim".equals(element.getNamespace()) && "VoltageLevel".equals(element.getClassName()) && "MemberOf_Substation".equals(element.getName()))
					|| ("cim".equals(element.getNamespace()) && "Equipment".equals(element.getClassName()) && "MemberOf_EquipmentContainer".equals(element.getName()))
					|| ("cim".equals(element.getNamespace()) && "ConductingEquipment".equals(element.getClassName()) && "BaseVoltage".equals(element.getName()))
					|| ("cim".equals(element.getNamespace()) && "TransformerWinding".equals(element.getClassName()) && "MemberOf_PowerTransformer".equals(element.getName()))
					|| ("cim".equals(element.getNamespace()) && "ConnectivityNode".equals(element.getClassName()) && "MemberOf_EquipmentContainer".equals(element.getName()))
					|| ("cim".equals(element.getNamespace()) && "ConnectivityNode".equals(element.getClassName()) && "ConnectivityNodeGroup".equals(element.getName()))
				) {
				log.info("Unhandled Attribute value " + element.getNamespace() + ":" + element.getClassName() + (element.getClassName().equals(element.getName()) ? "" : "." + element.getName() + " for " + this.getClass().getName()));
			}
		}
	}

	protected boolean handleAttribute(Element element, String value) {
		boolean handled = false;
		
		if ( ("cim".equals(element.getNamespace()) && "Terminal".equals(element.getClassName()) && "ConductingEquipment".equals(element.getName())) 
				|| ("cim".equals(element.getNamespace()) && "Terminal".equals(element.getClassName()) && "ConnectivityNode".equals(element.getName())) 
				|| ("cim".equals(element.getNamespace()) && "VoltageLevel".equals(element.getClassName()) && "BaseVoltage".equals(element.getName()))
				|| ("cim".equals(element.getNamespace()) && "VoltageLevel".equals(element.getClassName()) && "MemberOf_Substation".equals(element.getName()))
				|| ("cim".equals(element.getNamespace()) && "Equipment".equals(element.getClassName()) && "MemberOf_EquipmentContainer".equals(element.getName()))
				|| ("cim".equals(element.getNamespace()) && "ConductingEquipment".equals(element.getClassName()) && "BaseVoltage".equals(element.getName()))
				|| ("cim".equals(element.getNamespace()) && "TransformerWinding".equals(element.getClassName()) && "MemberOf_PowerTransformer".equals(element.getName()))
				|| ("cim".equals(element.getNamespace()) && "ConnectivityNode".equals(element.getClassName()) && "MemberOf_EquipmentContainer".equals(element.getName()))
				|| ("cim".equals(element.getNamespace()) && "ConnectivityNode".equals(element.getClassName()) && "ConnectivityNodeGroup".equals(element.getName()))
			) {
			log.debug("handleAttribute: " + element.getNamespace() + ":" + element.getClassName() + (element.getClassName().equals(element.getName()) ? "" : "." + element.getName()) + " <--? " + value);
		}
		
		return handled;
	}
	
	public void processElement(Element element, String value) {
		if (!handleElement(element,value)) {
			if ( ("cim".equals(element.getNamespace()) && "IdentifiedObject".equals(element.getClassName()) && "name".equals(element.getName()))
					|| ("cim".equals(element.getNamespace()) && "IdentifiedObject".equals(element.getClassName()) && "description".equals(element.getName())) 
					|| ("etx".equals(element.getNamespace()) && "PowerSystemResource".equals(element.getClassName()) && "teid".equals(element.getName()))
					|| ("etx".equals(element.getNamespace()) && "Analog".equals(element.getClassName()) && "teid".equals(element.getName()))
					|| ("etx".equals(element.getNamespace()) && "Discrete".equals(element.getClassName()) && "teid".equals(element.getName()))
					|| ("cim".equals(element.getNamespace()) && "Measurement".equals(element.getClassName()) && "Terminal".equals(element.getName()))
					|| ("cim".equals(element.getNamespace()) && "Switch".equals(element.getClassName()) && "normalOpen".equals(element.getName()))
					|| ("cim".equals(element.getNamespace()) && "BaseVoltage".equals(element.getClassName()) && "nominalVoltage".equals(element.getName()))
				) {
				log.info("Unhandled Element value " + element.getNamespace() + ":" + element.getClassName() + (element.getClassName().equals(element.getName()) ? "" : "." + element.getName() + " for " + this.getClass().getName()));
			}
		}
	}
	
	protected boolean handleElement(Element element, String value) {
		boolean handled = false;

		if ("cim".equals(element.getNamespace()) && "IdentifiedObject".equals(element.getClassName()) && "name".equals(element.getName())) {
			log.debug("handleElement: cim:IdentifiedObject.name = " + value);
			setName(value);
			handled = true;
		} else if ("cim".equals(element.getNamespace()) && "IdentifiedObject".equals(element.getClassName()) && "description".equals(element.getName())) {
			log.debug("handleElement: cim:IdentifiedObject.description = " + value);
			setDescription(value);
			handled = true;
		}
		
		if ( ("cim".equals(element.getNamespace()) && "IdentifiedObject".equals(element.getClassName()) && "name".equals(element.getName()))
				|| ("cim".equals(element.getNamespace()) && "IdentifiedObject".equals(element.getClassName()) && "description".equals(element.getName())) 
				|| ("etx".equals(element.getNamespace()) && "PowerSystemResource".equals(element.getClassName()) && "teid".equals(element.getName()))
				|| ("etx".equals(element.getNamespace()) && "Analog".equals(element.getClassName()) && "teid".equals(element.getName()))
				|| ("etx".equals(element.getNamespace()) && "Discrete".equals(element.getClassName()) && "teid".equals(element.getName()))
				|| ("cim".equals(element.getNamespace()) && "Measurement".equals(element.getClassName()) && "Terminal".equals(element.getName()))
				|| ("cim".equals(element.getNamespace()) && "Switch".equals(element.getClassName()) && "normalOpen".equals(element.getName()))
			) {
			log.debug("handleElement: " + element.getNamespace() + ":" + element.getClassName() + (element.getClassName().equals(element.getName()) ? "" : "." + element.getName()) + " <--? " + value);
		}
		
		return handled;
	}
	
	protected String getDisplayName() { return "IdentifiedObject"; }
	
	public String toString() { return "" + getDisplayName() + ",\"" + getName() + "\",\"" + getDescription() + "\""; }
}
