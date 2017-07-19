package com.omniprise.cim.graph;

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
