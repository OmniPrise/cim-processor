package com.omniprise.cim.graph;

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
