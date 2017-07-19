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
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.omniprise.cim.parse.model.Element;

public class Terminal extends Rdf {
	private static Logger log = LoggerFactory.getLogger(Terminal.class); 
	
	private static Map<String,List<Terminal>> conductingEquipmentTerminals = new HashMap<String,List<Terminal>>();
	private static Map<String,List<Terminal>> connectivityNodeTerminals = new HashMap<String,List<Terminal>>();
	private static Map<String,Terminal> terminals = new HashMap<String,Terminal>();

	private String connectivityNode;
	private String conductingEquipment;
	
	public Terminal() {
		log.debug("Created Terminal instance");
	}

	@Override
	public void setId(String id) {
		if (getId() != null) {
			log.debug("setId: remove existing element id " + getId());
			terminals.remove(getId());
		}
		super.setId(id);
		terminals.put(getId(),this);
	}
	
	public String getConnectivityNode() {
		return connectivityNode;
	}
	
	public void setConnectivityNode(String connectivityNode) {
		this.connectivityNode = connectivityNode;
	}
	
	public String getConductingEquipment() {
		return conductingEquipment;
	}
	
	public void setConductingEquipment(String conductingEquipment) {
		this.conductingEquipment = conductingEquipment;
	}
	
	public static List<Terminal> findElementTerminals(String id) {
		List<Terminal> result = null;
		log.debug("findElementTerminals: find terminals for id " + id);
		result = conductingEquipmentTerminals.get(id);
		if (result == null) log.debug("findElementTerminals: element not found");
		return result;
	}
	
	public static List<Terminal> findTerminalsByConductingEquipmentId(String id) {
		return conductingEquipmentTerminals.get(id);
	}
	
	public static List<Terminal> findTerminalsByConnectivityNodeId(String id) {
		return connectivityNodeTerminals.get(id);
	}
	
	@Override
	protected boolean handleAttribute(Element element, String value) {
		boolean handled = false;
		
		if (!(handled = super.handleAttribute(element, value))) {
			if ("cim".equals(element.getNamespace()) && "Terminal".equals(element.getClassName()) && "ConductingEquipment".equals(element.getName())) {
				log.debug("handleAttribute cim:Terminal.ConductingEquipment = " + value);
				setConductingEquipment(value);
				List<Terminal> conductingEquipmentTerminalList = conductingEquipmentTerminals.get(value);
				if (conductingEquipmentTerminalList == null) {
					conductingEquipmentTerminalList = new LinkedList<Terminal>();
					conductingEquipmentTerminals.put(value,conductingEquipmentTerminalList);
				}
				conductingEquipmentTerminalList.add(this);
				handled = true;
			} else if ("cim".equals(element.getNamespace()) && "Terminal".equals(element.getClassName()) && "ConnectivityNode".equals(element.getName())) {
				log.debug("handleAttribute cim:Terminal.ConnectivityNode = " + value);
				setConnectivityNode(value);
				List<Terminal> connectivityNodeTerminalList = connectivityNodeTerminals.get(value);
				if (connectivityNodeTerminalList == null) {
					connectivityNodeTerminalList = new LinkedList<Terminal>();
					connectivityNodeTerminals.put(value,connectivityNodeTerminalList);
				}
				connectivityNodeTerminalList.add(this);
				handled = true;
			}
		}
		
		return handled;
	}
	
	public static Set<String> getTerimalIds() {
		return terminals.keySet();
	}
	
	public static Terminal findByTermialId(String terminalId) {
		return terminals.get(terminalId);
	}
	
	@Override
	protected String getDisplayName() { return "Terminal"; }
	
	@Override
	public String toString() { return super.toString() + "," + getConnectivityNode() + "," + getConductingEquipment(); }
}
