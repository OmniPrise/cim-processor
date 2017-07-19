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

import com.omniprise.cim.model.MeasurementResource;
import com.omniprise.cim.model.Rdf;

public class MeasurementResourceNode extends OutageResourceNode {
	private static Logger log = LoggerFactory.getLogger(MeasurementResourceNode.class); 

	private TerminalNode measurementTerminal = null;
	
	public MeasurementResourceNode(Rdf source, int model) {
		super(source, model);
		log.debug("Created MeasurementNode");
	}
	
	@Override
	public void connect() {
		super.connect();
		MeasurementResource measurementResource = (MeasurementResource)getSource();
		if (measurementTerminal == null) {
			measurementTerminal = (TerminalNode)RdfNode.findByNodeId(measurementResource.getMeasurementTerminal(), this.getModel());
		}
	}
	
	@Override
	public void disconnect() {
		super.disconnect();
		measurementTerminal = null;
	}
	
	public TerminalNode getTerminal() {
		return measurementTerminal;
	}

	@Override
	public String getDisplayName() { return "MeasurementResource"; }

	@Override
	public String toString() { 
		String result = super.toString();
		MeasurementResource measurementResource = (MeasurementResource)getSource();
		//RdfNode terminal = RdfNode.findByNodeId(measurementResource.getMeasurementTerminal(), getModel());
		if (getTerminal() == null) {
			result += ",Terminal," + measurementResource.getId() + "-NotPresentInRdfModel";
		} else {
			result += "," + getTerminal().toIdentifier();
		}
		return result;
	}
}
