package com.omniprise.cim.graph;

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
