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

import com.omniprise.cim.model.Rdf;
import com.omniprise.cim.model.Switch;

public class SwitchNode extends ConductingEquipmentNode implements DualTerminalEquipment {
	private static Logger log = LoggerFactory.getLogger(SwitchNode.class); 

	private boolean normalOpen = false;
	private boolean open = false;
	private boolean zbr = false;

	public SwitchNode(Rdf source, int model) {
		super(source, model);
		Switch rdfSwitch = (Switch)source;
		normalOpen = rdfSwitch.isNormalOpen();
		zbr = rdfSwitch.isZbr();
		open = normalOpen;
		log.debug("Created SwitchNode");
	}

	public boolean isOpen() {
		return open;
	}

	public void setOpen(boolean open) {
		this.open = open;
	}
	
	public boolean isZbr() {
		return zbr;
	}

	public void setZbr(boolean zbr) {
		this.zbr = zbr;
	}

	@Override
	public void setOutage(boolean outage) {
		super.setOutage(outage);
		open = !normalOpen;
	}

	@Override
	public void resetOutage() {
		super.resetOutage();
		open = normalOpen;
	}
	
	@Override
	public String getDisplayName() { return "Switch"; }
	
	@Override
	public String getDiagramFillColor() { return (isOpen() ? "green" : "red"); }
}
