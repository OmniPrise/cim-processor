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


import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.omniprise.cim.model.Rdf;

public class LineNode extends AbstractEquipmentContainerNode {
	private static Logger log = LoggerFactory.getLogger(LineNode.class); 
	
	private Map<String,ACLineSegmentNode> acLineSegments = new HashMap<String,ACLineSegmentNode>();

	public LineNode(Rdf source, int model) {
		super(source, model);
		log.debug("Created LineNode");
	}
	
	public void addLineSegment(ACLineSegmentNode acLineSegment) {
		if (!acLineSegments.containsKey(acLineSegment.getId())) acLineSegments.put(acLineSegment.getId(), acLineSegment);
	}
	
	public void removeLineSegment(ACLineSegmentNode acLineSegment) {
		acLineSegments.remove(acLineSegment.getId());
	}

	public Map<String,ACLineSegmentNode> getLineSegments() {
		return acLineSegments;
	}

	public static String getNodeName() { return "Line"; }

	@Override
	public String getDiagramName() { return getName(); }

	@Override
	public String getDiagramType() { return "lc"; }

	@Override
	public String getDisplayName() { return "Line"; }
}
