package com.omniprise.cim.graph;

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

import com.omniprise.cim.model.BaseVoltage;
import com.omniprise.cim.model.Rdf;

public class BaseVoltageNode extends RdfNode {
	private static Logger log = LoggerFactory.getLogger(BaseVoltageNode.class); 
	
	private String nominalVoltage = null;

	public BaseVoltageNode(Rdf source, int model) {
		super(source, model);
		BaseVoltage baseVoltage = (BaseVoltage)source;
		nominalVoltage = baseVoltage.getNominalVoltage();
		log.debug("Created BaseVoltageNode");
	}

	public String getNominalVoltage() {
		return nominalVoltage;
	}

	public void setNominalVoltage(String nominalVoltage) {
		this.nominalVoltage = nominalVoltage;
	}

	@Override
	public String getDisplayName() { return "BaseVoltage"; }
	
	@Override
	public String toString() { 
		BaseVoltage baseVoltage = (BaseVoltage)getSource();
		return super.toString() + ",\"" + baseVoltage.getNominalVoltage() + "\"";
	}
}
