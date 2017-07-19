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
