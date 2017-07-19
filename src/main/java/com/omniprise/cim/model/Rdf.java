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

public abstract class Rdf extends IdentifiedObject {
	private static Logger log = LoggerFactory.getLogger(Rdf.class); 
	
	private static Map<String,Rdf> allRdfElements = new HashMap<String,Rdf>();
	private static Map<String,List<Rdf>> containerContent = new HashMap<String,List<Rdf>>();
	
	private String id = null;
	private String equipmentContainer;

	protected Rdf() {
		log.debug("Created Rdf instance");
	}
	
	public String getId() {
		return id;
	}

	public String getEquipmentContainer() {
		return equipmentContainer;
	}
	
	public void setEquipmentContainer(String equipmentContainer) {
		this.equipmentContainer = equipmentContainer;
	}

	public void setId(String id) {
		log.debug("setId: element id " + id);
		if (this.id != null) {
			log.debug("setId: remove existing element id " + this.id);
			allRdfElements.remove(this.id);
		}
		this.id = id;
		allRdfElements.put(this.id,this);
	}

	public static Set<String> getAllIds() {
		return allRdfElements.keySet();
	}
	
	public static Rdf findById(String id) {
		Rdf result = null;
		log.debug("findById: find RDF element with id " + id);
		result = allRdfElements.get(id);
		if (result == null) log.debug("findById: element not found");
		return result;
	}
	
	public static Set<String> getContainerIds() {
		return containerContent.keySet();
	}
	
	public static List<Rdf> findRdfByContainer(String id) {
		return containerContent.get(id);
	}
	
	@Override
	protected boolean handleAttribute(Element element, String value) {
		boolean handled = false;
		
		if (!(handled = super.handleAttribute(element, value))) {
			if ("cim".equals(element.getNamespace()) && "ConnectivityNode".equals(element.getClassName()) && "MemberOf_EquipmentContainer".equals(element.getName())) {
				log.debug("handleAttribute cim:ConnectivityNode.MemberOf_EquipmentContainer = " + value);
				setEquipmentContainer(value);
				List<Rdf> content = containerContent.get(value);
				if (content == null ) {
					content = new LinkedList<Rdf>();
					containerContent.put(value, content);
				}
				content.add(this);
				handled = true;
			} else if ("cim".equals(element.getNamespace()) && "Equipment".equals(element.getClassName()) && "MemberOf_EquipmentContainer".equals(element.getName())) {
				log.debug("handleAttribute cim:Equipment.MemberOf_EquipmentContainer = " + value);
				setEquipmentContainer(value);
				List<Rdf> content = containerContent.get(value);
				if (content == null ) {
					content = new LinkedList<Rdf>();
					containerContent.put(value, content);
				}
				content.add(this);
				handled = true;
			} else if ("cim".equals(element.getNamespace()) && "Bay".equals(element.getClassName()) && "MemberOf_VoltageLevel".equals(element.getName())) {
				log.debug("handleAttribute cim:Bay.MemberOf_VoltageLevel = " + value);
				setEquipmentContainer(value);
				List<Rdf> content = containerContent.get(value);
				if (content == null ) {
					content = new LinkedList<Rdf>();
					containerContent.put(value, content);
				}
				content.add(this);
				handled = true;
			}
		}
		
		return handled;
	}
	
	@Override
	protected String getDisplayName() { return "Rdf"; }
	
	@Override
	public String toString() { return super.toString() + "," + getId(); }
}
