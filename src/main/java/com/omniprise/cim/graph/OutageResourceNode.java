package com.omniprise.cim.graph;

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
