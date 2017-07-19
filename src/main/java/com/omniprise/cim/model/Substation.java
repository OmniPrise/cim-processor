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


import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Substation extends EquipmentContainer {
	private static Logger log = LoggerFactory.getLogger(Substation.class);
	private static Map<String,Substation> substations = new HashMap<String,Substation>();
	
	Substation() {
		log.debug("Created Substation instance");
	}
	
	@Override
	public void setName(String name) {
		log.debug("setName: set name " + name);
		if (getName() != null) {
			log.debug("setName: remove existing Substation " + getName());
			substations.remove(getName());
		}
		super.setName(name);
		substations.put(getName(),this);
	}
	
	public static Substation findByName(String substationName) {
		Substation result = null;
		
		result = substations.get(substationName);
		
		return result;
	}
	
	@Override
	protected String getDisplayName() { return "Substation"; }
}
