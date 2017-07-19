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

import com.omniprise.cim.model.OutageResource;
import com.omniprise.cim.model.Rdf;

public class OutageResourceNode extends RdfNode {
	private static Logger log = LoggerFactory.getLogger(OutageResourceNode.class); 

	private boolean outage = false;
	private Integer teid = null;

	public OutageResourceNode(Rdf source, int model) {
		super(source, model);
		OutageResource outageResource = (OutageResource)source;
		if (outageResource.getTeid() != null) teid = Integer.parseInt(outageResource.getTeid());
		log.debug("Created OutageResourceNode");
	}

	public boolean isOutage() {
		return outage;
	}

	public void setOutage(boolean outage) {
		this.outage = outage;
	}
	
	public void resetOutage() {
		this.outage = false;
	}

	public Integer getTeid() {
		return teid;
	}

	public void setTeid(Integer teid) {
		this.teid = teid;
	}

	@Override
	public String getDisplayName() { return "OutageResource"; }
	
	public String getDiagramBorderColor() { return (isDiagramNodeOfInterest() || isOutage() ? "cyan" : "black"); }
	
	@Override
	public String getDiagramFillColor() { return (isOutage() ? "green" : "red"); }
	
	@Override
	public String getDiagramStyle() { return "\"filled" + (isDiagramNodeOfInterest() || isOutage() ? ",bold" : "") + (isOutage() ? ",dashed" : "") + "\""; }
}
