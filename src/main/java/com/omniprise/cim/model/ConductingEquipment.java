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


import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.omniprise.cim.parse.model.Element;

public class ConductingEquipment extends Equipment {
	private static Logger log = LoggerFactory.getLogger(ConductingEquipment.class); 
	private static Map<String,ConductingEquipment> conductingEquipment = new HashMap<String,ConductingEquipment>();

	private String baseVoltage;

	public ConductingEquipment() {
		log.debug("Created ConductingEquipment instance");
	}
	
	@Override
	public void setId(String id) {
		if (getId() != null) {
			log.debug("setId: remove existing element id " + getId());
			conductingEquipment.remove(getId());
		}
		super.setId(id);
		conductingEquipment.put(getId(),this);
	}
	
	public String getBaseVoltage() {
		return baseVoltage;
	}
	
	public void setBaseVoltage(String baseVoltage) {
		this.baseVoltage = baseVoltage;
	}
	
	public static ConductingEquipment findConductingEquipmentById(String id) {
		return conductingEquipment.get(id);
	}
	
	public static Set<String> getConductingEquipmentIds() {
		return conductingEquipment.keySet();
	}

	@Override
	protected boolean handleAttribute(Element element, String value) {
		boolean handled = false;
		
		if (!(handled = super.handleAttribute(element, value))) {
			if ("cim".equals(element.getNamespace()) && "ConductingEquipment".equals(element.getClassName()) && "BaseVoltage".equals(element.getName())) {
				log.debug("handleAttribute cim:ConductingEquipment.BaseVoltage = " + value);
				setBaseVoltage(value);
				handled = true;
			}
		}
		
		return handled;
	}
	
	@Override
	protected String getDisplayName() { return "ConductingEquipment"; }
	
	@Override
	public String toString() { return super.toString() + "," + getBaseVoltage(); }
}
