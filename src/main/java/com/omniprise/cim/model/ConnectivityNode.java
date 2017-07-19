package com.omniprise.cim.model;

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
