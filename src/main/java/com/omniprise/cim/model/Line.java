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

public class Line extends EquipmentContainer {
	private static Logger log = LoggerFactory.getLogger(Line.class);
	private static Map<String,Line> lines = new HashMap<String,Line>();

	Line() {
		log.debug("Created Line instance");
	}
	
	@Override
	public void setName(String name) {
		log.debug("setName: set name " + name);
		if (getName() != null) {
			log.debug("setName: remove existing Line " + getName());
			lines.remove(getName());
		}
		super.setName(name);
		lines.put(getName(),this);
	}
	
	public static Line findByName(String lineName) {
		Line result = null;
		
		result = lines.get(lineName);
		
		return result;
	}
	
	@Override
	protected String getDisplayName() { return "Line"; }
}
