package com.omniprise.cim.psse.model;

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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.omniprise.cim.graph.ConnectivityElementNode;
import com.omniprise.cim.graph.ConnectivityNodeGroupNode;

public class Bus extends PsseElement {
	private static Logger log = LoggerFactory.getLogger(Bus.class); 
	
	private static Map<Integer,Set<Integer>> availableBusNumbers = new HashMap<Integer,Set<Integer>>();
	private static Map<Integer,Map<String,Bus>> busNames = new HashMap<Integer,Map<String,Bus>>();
	private static Map<Integer,Map<String,Bus>> busNumbers = new HashMap<Integer,Map<String,Bus>>();
	private static Map<Integer,Map<String,Bus>> connectivityNodeBus = new HashMap<Integer,Map<String,Bus>>();
	private static Map<Integer,Map<String,Bus>> connectivityNodeGroupBus = new HashMap<Integer,Map<String,Bus>>();

	public static void initialize(int model) {
		Set<Integer> modelAvailableBusNumbers = new HashSet<Integer>();
		availableBusNumbers.put(model, modelAvailableBusNumbers);
		for (int i = 1; i <= 99997 ; i++) modelAvailableBusNumbers.add(i);
		busNames.put(model, new HashMap<String,Bus>());
		busNumbers.put(model, new HashMap<String,Bus>());
		connectivityNodeBus.put(model, new HashMap<String,Bus>());
		connectivityNodeGroupBus.put(model, new HashMap<String,Bus>());
	}
	
	public Bus(int model) {
		super(model);
		log.debug("Created Bus instance");
	}
	
	@Override
	public void setName(String name) {
		if (busNames.get(getModel()).containsKey(name)) {
			int index = 0;
			while (busNames.get(getModel()).containsKey(name + ":" + index)) index++;
			name += ":" + index;
			log.debug("Duplicate bus name replaced with indexed value " + name);
		}
		super.setName(name);
		busNames.get(getModel()).put(name, this);
	}
	
	@Override
	public void setNumber(String number) {
		if (busNumbers.get(getModel()).containsKey(number)) {
			int index = 0;
			while (busNumbers.get(getModel()).containsKey(number + ":" + index)) index++;
			number += ":" + index;
			log.info("Duplicate bus number replaced with indexed value " + number);
		} else {
			availableBusNumbers.get(getModel()).remove(Integer.parseInt(number));
		}
		super.setNumber(number);
		busNumbers.get(getModel()).put(number, this);
	}
	
	public void addConnectivityNodeGroup(ConnectivityNodeGroupNode connectivityNodeGroup) {
		if (connectivityNodeGroupBus.get(getModel()).containsKey(connectivityNodeGroup.getId())) {
			log.info("Bus ConnectivityNodeGroup map for model " + getModel() + " already contains ConnectivityNodeGroup " + connectivityNodeGroup.getId());
		}
		connectivityNodeGroupBus.get(getModel()).put(connectivityNodeGroup.getId(), this);
		Map<String,ConnectivityElementNode> connectivityNodes = connectivityNodeGroup.getConnectivityNodes();
		for (String connectivityNodeId : connectivityNodes.keySet()) {
			ConnectivityElementNode connectivityNode = connectivityNodes.get(connectivityNodeId);
			addConnectivityNode(connectivityNode);
		}
	}
	
	public void addConnectivityNode(ConnectivityElementNode connectivityNode) {
		if (connectivityNodeBus.get(getModel()).containsKey(connectivityNode.getId())) {
			log.info("Bus ConnectivityNode map for model " + getModel() + " already contains ConnectivityNode " + connectivityNode.getId());
		}
		connectivityNodeBus.get(getModel()).put(connectivityNode.getId(), this);
		addPsseEquipment(connectivityNode);
	}

	public static Map<String,Bus> getBusNames(int model) {
		return busNames.get(model);
	}

	public static Map<String,Bus> getBusNumbers(int model) {
		return busNumbers.get(model);
	}

	public static Map<String,Bus> getConnectivityNodeBusses(int model) {
		return connectivityNodeBus.get(model);
	}
}
