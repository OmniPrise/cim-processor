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

public class BayNode extends AbstractEquipmentContainerNode {
	private static Logger log = LoggerFactory.getLogger(BayNode.class); 
	
	public BayNode(Rdf source, int model) {
		super(source, model);
		log.debug("Created BayNode");
	}

	@Override
	public String getDiagramType() { return "bay"; }

	@Override
	public String toDiagram() { return "\t\t# node [fixedsize=true shape=" + getDiagramShape() + " style=" + getDiagramStyle() + " color=" + getDiagramBorderColor() + " fillcolor=" + getDiagramFillColor() +" label=\"" + getDiagramLabel() + "\"] \"" + getDiagramName() + "\"; // "+ getDisplayName(); }

	@Override
	public String getDisplayName() { return "Bay"; }
}
