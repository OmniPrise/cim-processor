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

public class TransformerWinding extends ConductingEquipment {
	private static Logger log = LoggerFactory.getLogger(TransformerWinding.class); 

	private String powerTransformer;

	public TransformerWinding() {
		log.debug("Created TransformerWinding instance");
	}
	
	public String getPowerTransformer() {
		return powerTransformer;
	}
	
	public void setPowerTransformer(String powerTransformer) {
		this.powerTransformer = powerTransformer;
	}
	
	@Override
	protected boolean handleAttribute(Element element, String value) {
		boolean handled = false;
		
		if (!(handled = super.handleAttribute(element, value))) {
			if ("cim".equals(element.getNamespace()) && "TransformerWinding".equals(element.getClassName()) && "MemberOf_PowerTransformer".equals(element.getName())) {
				log.debug("handleAttribute cim:TransformerWinding.MemberOf_PowerTransformer = " + value);
				setPowerTransformer(value);
				handled = true;
			}
		}
		
		return handled;
	}
	
	@Override
	protected String getDisplayName() { return "TransformerWinding"; }
	
	@Override
	public String toString() { return super.toString() + "," + getPowerTransformer(); }
}
