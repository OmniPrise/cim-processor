package com.omniprise.cim.psse.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.omniprise.cim.graph.ConnectivityElementNode;
import com.omniprise.cim.graph.ConnectivityNodeGroupNode;
import com.omniprise.cim.graph.RdfNode;

public class PsseModel {
	private static Logger log = LoggerFactory.getLogger(PsseModel.class); 
	private static int psseModel = -1;
	
	public static int initialize() {
		psseModel += 1;
		Bus.initialize(psseModel);
		Branch.initialize(psseModel);
		
		return psseModel;
	}
	
	public static Bus createBus(ConnectivityNodeGroupNode connectivityNodeGroup, int model) {
		Bus result = new Bus(model);
		
		result.setName(connectivityNodeGroup.getPsseBusName());
		result.setNumber(connectivityNodeGroup.getPsseBusNumber());
		result.addConnectivityNodeGroup(connectivityNodeGroup);
		
		return result;
	}
	
	public static Bus createBus(ConnectivityElementNode connectivityNode, int model) {
		Bus result = Bus.getBusNumbers(model).get(connectivityNode.getPsseBusNumber());
		if (result == null) {
			result = new Bus(model);
			result.setName(connectivityNode.getPsseBusName());
			result.setNumber(connectivityNode.getPsseBusNumber());
			if (!result.getName().equals(connectivityNode.getPsseBusName()) || !result.getNumber().equals(connectivityNode.getPsseBusNumber())) {
				log.debug("Bus name or number modified " + result.getName() + "-" + connectivityNode.getPsseBusName() + " " + result.getNumber() + "-" + connectivityNode.getPsseBusNumber());
			}
		} else {
			RdfNode node = result.getPsseEquipment().get(result.getPsseEquipment().keySet().iterator().next());
			if (!node.getSubstation().equals(connectivityNode.getSubstation())) {
				log.info("Bus equipment substations do not match...");
				result = new Bus(model);
				result.setName(connectivityNode.getPsseBusName());
				result.setNumber(connectivityNode.getPsseBusNumber());
				if (!result.getName().equals(connectivityNode.getPsseBusName()) || !result.getNumber().equals(connectivityNode.getPsseBusNumber())) {
					ConnectivityElementNode guess = (ConnectivityElementNode)node;
					log.debug("Bus name or number modified " + result.getName() + "-" + connectivityNode.getPsseBusName() + "-" + guess.getPsseBusName() + " " + result.getNumber() + "-" + connectivityNode.getPsseBusNumber() + "-" + guess.getPsseBusNumber());
				}
			}
		}
		if (!result.getName().equals(connectivityNode.getPsseBusName())) {
			log.debug("Bus name and number do not match " + result.getName() + "-" + connectivityNode.getPsseBusName() + " " + result.getNumber() + "-" + connectivityNode.getPsseBusNumber());
		}
		result.addConnectivityNode(connectivityNode);
		
		return result;
	}
}
