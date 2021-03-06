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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.omniprise.cim.parse.model.Element;

public class VoltageLevel extends EquipmentContainer {
	private static Logger log = LoggerFactory.getLogger(VoltageLevel.class); 
	private static Map<String,List<VoltageLevel>> substations = new HashMap<String,List<VoltageLevel>>();
	
	private String substation;
	private String baseVoltage;
	
	public VoltageLevel() {
		log.debug("Created VoltageLevel instance");
	}

	public String getSubstation() {
		return substation;
	}
	
	public void setSubstation(String substation) {
		this.substation = substation;
	}
	
	public String getBaseVoltage() {
		return baseVoltage;
	}
	
	public void setBaseVoltage(String baseVoltage) {
		this.baseVoltage = baseVoltage;
	}
	
	public static List<VoltageLevel> findVoltageLevels(String id) {
		List<VoltageLevel> result = null;
		log.debug("findVoltageLevels: find voltage levels for substations " + id);
		result = substations.get(id);
		if (result == null) log.debug("findVoltageLevels:  not found");
		return result;
	}
	
	@Override
	protected boolean handleAttribute(Element element, String value) {
		boolean handled = false;
		
		if (!(handled = super.handleAttribute(element, value))) {
			if ("cim".equals(element.getNamespace()) && "VoltageLevel".equals(element.getClassName()) && "BaseVoltage".equals(element.getName())) {
				log.debug("handleAttribute cim:VoltageLevel.BaseVoltage = " + value);
				setBaseVoltage(value);
				handled = true;
			} else if ("cim".equals(element.getNamespace()) && "VoltageLevel".equals(element.getClassName()) && "MemberOf_Substation".equals(element.getName())) {
				log.debug("handleAttribute cim:VoltageLevel.MemberOf_Substation = " + value);
				setSubstation(value);
				List<VoltageLevel> voltageLevels = substations.get(substation);
				if (voltageLevels == null) {
					voltageLevels = new LinkedList<VoltageLevel>();
					substations.put(substation,voltageLevels);
				}
				voltageLevels.add(this);
				handled = true;
			}
		}
		
		return handled;
	}
	
	@Override
	protected String getDisplayName() { return "VoltageLevel"; }
	
	@Override
	public String toString() { return super.toString() + "," + getSubstation() + "," + getBaseVoltage(); }
}
