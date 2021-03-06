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

public class Switch extends ConductingEquipment {
	private static Logger log = LoggerFactory.getLogger(Switch.class); 
	
	private boolean normalOpen = false;
	private boolean zbr = false;

	public Switch() {
		log.debug("Created Switch instance");
	}

	public boolean isNormalOpen() {
		return normalOpen;
	}

	public void setNormalOpen(boolean normalOpen) {
		this.normalOpen = normalOpen;
	}
	
	public boolean isZbr() {
		return zbr;
	}

	public void setZbr(boolean zbr) {
		this.zbr = zbr;
	}

	@Override
	protected boolean handleElement(Element element, String value) {
		boolean handled = false;
		
		if (!(handled = super.handleElement(element, value))) {
			if ("cim".equals(element.getNamespace()) && "Switch".equals(element.getClassName()) && "normalOpen".equals(element.getName())) {
				log.debug("handleElement cim:Switch.normalOpen = " + value);
				setNormalOpen(Boolean.valueOf(value));
				handled = true;
			} else if ("etx".equals(element.getNamespace()) && "Switch".equals(element.getClassName()) && "ZBR".equals(element.getName())) {
				log.debug("handleElement etx:Switch.ZBR = " + value);
				setZbr(Boolean.valueOf(value));
				handled = true;
			}
		}
		
		return handled;
	}
	
	@Override
	protected String getDisplayName() { return "Switch"; }
	
	@Override
	public String toString() { return super.toString() + "," + isNormalOpen(); }
}
