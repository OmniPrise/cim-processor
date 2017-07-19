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

public class ConnectivityNode extends Rdf {
	private static Logger log = LoggerFactory.getLogger(ConnectivityNode.class); 
	private static Map<String,ConnectivityNode> connectivityNodes = new HashMap<String,ConnectivityNode>();

	private String connectivityNodeGroup;
	private String psseBusName;
	private String psseBusNumber;

	public ConnectivityNode() {
		log.debug("Created ConnectivityNode instance");
	}

	public String getPsseBusName() {
		return psseBusName;
	}

	public void setPsseBusName(String psseBusName) {
		this.psseBusName = psseBusName;
	}

	public String getPsseBusNumber() {
		return psseBusNumber;
	}

	public void setPsseBusNumber(String psseBusNumber) {
		this.psseBusNumber = psseBusNumber;
	}

	@Override
	public void setId(String id) {
		if (getId() != null) {
			log.debug("setId: remove existing element id " + getId());
			connectivityNodes.remove(getId());
		}
		super.setId(id);
		connectivityNodes.put(getId(),this);
	}

	public String getConnectivityNodeGroup() {
		return connectivityNodeGroup;
	}

	public void setConnectivityNodeGroup(String connectivityNodeGroup) {
		this.connectivityNodeGroup = connectivityNodeGroup;
	}
	
	@Override
	protected boolean handleElement(Element element, String value) {
		boolean handled = false;
		
		if (!(handled = super.handleElement(element, value))) {
			if ("etx".equals(element.getNamespace()) && "ConnectivityNode".equals(element.getClassName()) && "PSSEBusName".equals(element.getName())) {
				log.debug("handleElement etx:ConnectivityNode.PSSEBusName = " + value);
				setPsseBusName(value);
				handled = true;
			} else if ("etx".equals(element.getNamespace()) && "ConnectivityNode".equals(element.getClassName()) && "PSSEBusNumber".equals(element.getName())) {
				log.debug("handleElement etx:ConnectivityNode.PSSEBusName = " + value);
				setPsseBusNumber(value);
				handled = true;
			}
		}
		
		return handled;
	}
	
	@Override
	protected boolean handleAttribute(Element element, String value) {
		boolean handled = false;
		
		if (!(handled = super.handleAttribute(element, value))) {
			if ("etx".equals(element.getNamespace()) && "ConnectivityNode".equals(element.getClassName()) && "ConnectivityNodeGroup".equals(element.getName())) {
				log.debug("handleAttribute etx:ConnectivityNode.ConnectivityNodeGroup = " + value);
				setConnectivityNodeGroup(value);
				handled = true;
			}
		}
		
		return handled;
	}

	public static ConnectivityNode findConnectivityNodeById(String id) {
		return connectivityNodes.get(id);
	}

	public static Set<String> getConnectivityNodeIds() {
		return connectivityNodes.keySet();
	}
	
	@Override
	protected String getDisplayName() { return "ConnectivityNode"; }
	
	@Override
	public String toString() { return super.toString() + "," + getEquipmentContainer() + "," + getConnectivityNodeGroup(); }
}
