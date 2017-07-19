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

public class BaseVoltage extends Rdf {
	private static Logger log = LoggerFactory.getLogger(BaseVoltage.class); 
	
	private String nominalVoltage;

	public BaseVoltage() {
		log.debug("Created BaseVoltage instance");
	}

	public String getNominalVoltage() {
		return nominalVoltage;
	}

	public void setNominalVoltage(String nominalVoltage) {
		this.nominalVoltage = nominalVoltage;
	}

	@Override
	protected boolean handleElement(Element element, String value) {
		boolean handled = false;
		
		if (!(handled = super.handleElement(element, value))) {
			if ("cim".equals(element.getNamespace()) && "BaseVoltage".equals(element.getClassName()) && "nominalVoltage".equals(element.getName())) {
				log.debug("handleAttribute cim:BaseVoltage.nominalVoltage = " + value);
				setNominalVoltage(value);
				handled = true;
			}
		}
		
		return handled;
	}
	
	@Override
	protected String getDisplayName() { return "BaseVoltage"; }
	
	@Override
	public String toString() { return super.toString() + "," + getNominalVoltage(); }
}
