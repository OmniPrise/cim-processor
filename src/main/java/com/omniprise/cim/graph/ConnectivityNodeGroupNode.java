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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.omniprise.cim.model.ConnectivityNodeGroup;
import com.omniprise.cim.model.Rdf;

public class ConnectivityNodeGroupNode extends PowerSystemResourceNode {
	private static Logger log = LoggerFactory.getLogger(ConnectivityNodeGroupNode.class); 

	private Map<String,ConnectivityElementNode> connectivityNodes = new HashMap<String,ConnectivityElementNode>();
	private String psseBusName;
	private String psseBusNumber;

	public ConnectivityNodeGroupNode(Rdf source, int model) {
		super(source, model);
		ConnectivityNodeGroup connectivityNodeGroup = (ConnectivityNodeGroup)source;
		psseBusName = connectivityNodeGroup.getPsseBusName();
		psseBusNumber = connectivityNodeGroup.getPsseBusNumber();
		log.debug("Created ConnectivityNodeGroupNode");
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
	
	public void addConnectivityNode(ConnectivityElementNode connectivityNode) {
		if (!connectivityNodes.containsKey(connectivityNode.getId())) connectivityNodes.put(connectivityNode.getId(), connectivityNode);
	}
	
	public void removeConnectivityNode(ConnectivityElementNode connectivityNode) {
		connectivityNodes.remove(connectivityNode.getId());
	}
	
	public Map<String,ConnectivityElementNode> getConnectivityNodes() {
		return connectivityNodes;
	}

	@Override
	public String getDisplayName() { return "ConnectivityNodeGroupNode"; }
}
