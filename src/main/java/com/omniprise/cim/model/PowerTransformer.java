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
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PowerTransformer extends EquipmentContainer {
	private static Logger log = LoggerFactory.getLogger(PowerTransformer.class); 
	
	private static Map<String,PowerTransformer> powerTransformers = new HashMap<String,PowerTransformer>();

	public PowerTransformer() {
		log.debug("Created PowerTransformer instance");
	}

	public static Set<String> getPowerTransformerIds() {
		return powerTransformers.keySet();
	}
	
	public static PowerTransformer findByPowerTransformerId(String powerTransformerId) {
		return powerTransformers.get(powerTransformerId);
	}
	
	@Override
	public void setId(String id) {
		if (getId() != null) {
			log.debug("setId: remove existing element id " + getId());
			powerTransformers.remove(getId());
		}
		super.setId(id);
		powerTransformers.put(getId(),this);
	}
	
	@Override
	protected String getDisplayName() { return "PowerTransformer"; }
}
