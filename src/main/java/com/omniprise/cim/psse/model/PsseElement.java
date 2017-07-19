package com.omniprise.cim.psse.model;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.omniprise.cim.graph.ConnectivityElementNode;
import com.omniprise.cim.graph.ConnectivityNodeGroupNode;
import com.omniprise.cim.graph.RdfNode;

public abstract class PsseElement {
	private static Logger log = LoggerFactory.getLogger(PsseElement.class); 

	private int model = 0;
	private Map<String,RdfNode> psseEquipment = new HashMap<String,RdfNode>();
	private String name = null;
	private String number = null;
	private boolean outOfService;

	private PsseElement() {
	}
	
	public PsseElement(int model) {
		this.model = model;
		log.debug("Created PsseElement instance");
	}
	
	public boolean isOutOfService() {
		return outOfService;
	}

	public void setOutOfService(boolean outOfService) {
		this.outOfService = outOfService;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}
	
	public int getModel() {
		return model;
	}

	public void addPsseEquipment(RdfNode node) {
		if (!psseEquipment.containsKey(node.getId())) psseEquipment.put(node.getId(),node);
	}
	
	public Map<String,RdfNode> getPsseEquipment() {
		return psseEquipment;
	}
}
