package com.omniprise.cim.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.omniprise.cim.parse.model.Element;

public class ConnectivityNodeGroup extends PowerSystemResource {
	private static Logger log = LoggerFactory.getLogger(ConnectivityNodeGroup.class); 

	private String psseBusName;
	private String psseBusNumber;

	public ConnectivityNodeGroup() {
		log.debug("Created ConnectivityNodeGroup instance");
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
	protected boolean handleElement(Element element, String value) {
		boolean handled = false;
		
		if (!(handled = super.handleElement(element, value))) {
			if ("etx".equals(element.getNamespace()) && "ConnectivityNodeGroup".equals(element.getClassName()) && "PSSEBusName".equals(element.getName())) {
				log.debug("handleElement etx:ConnectivityNodeGroup.PSSEBusName = " + value);
				setPsseBusName(value);
				handled = true;
			} else if ("etx".equals(element.getNamespace()) && "ConnectivityNodeGroup".equals(element.getClassName()) && "PSSEBusNumber".equals(element.getName())) {
				log.debug("handleElement etx:ConnectivityNodeGroup.PSSEBusName = " + value);
				setPsseBusNumber(value);
				handled = true;
			}
		}
		
		return handled;
	}
	
	@Override
	protected String getDisplayName() { return "ConnectivityNodeGroup"; }
	
	// TODO: ConnectivityNodeGroup has a PlanningBay rather than an EquipmentContainer
	@Override
	//public String toString() { return super.toString() + "," + getEquipmentContainer(); }
	public String toString() { return super.toString(); }
}
