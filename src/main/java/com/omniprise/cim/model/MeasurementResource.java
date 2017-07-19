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

public class MeasurementResource extends OutageResource {
	private static Logger log = LoggerFactory.getLogger(MeasurementResource.class); 

	private String measurementTerminal;

	public MeasurementResource() {
		log.debug("Created MeasurementResource instance");
	}
	
	public String getMeasurementTerminal() {
		return measurementTerminal;
	}

	public void setMeasurementTerminal(String measurementTerminal) {
		this.measurementTerminal = measurementTerminal;
	}

	@Override
	protected boolean handleElement(Element element, String value) {
		boolean handled = false;
		
		if (!(handled = super.handleElement(element, value))) {
			if ("cim".equals(element.getNamespace()) && "Measurement".equals(element.getClassName()) && "Terminal".equals(element.getName())) {
				log.debug("handleElement: cim:PowerSystemResource.teid = " + value);
				setMeasurementTerminal(value);
				handled = true;
			}
		}
		
		return handled;
	}
	
	@Override
	protected String getDisplayName() { return "MeasurementResource"; }
}
