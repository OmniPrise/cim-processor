package com.omniprise.cim.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.omniprise.cim.parse.model.Element;

public class BaseVoltage extends Rdf {
	private static Logger log = LoggerFactory.getLogger(BaseVoltage.class); 
	
	private String nominalVoltage;

	public BaseVoltage() {
		log.debug("Created BaseVoltage instance");
	}

	public String getNominalVoltage() {
		return nominalVoltage;
	}

	public void setNominalVoltage(String nominalVoltage) {
		this.nominalVoltage = nominalVoltage;
	}

	@Override
	protected boolean handleElement(Element element, String value) {
		boolean handled = false;
		
		if (!(handled = super.handleElement(element, value))) {
			if ("cim".equals(element.getNamespace()) && "BaseVoltage".equals(element.getClassName()) && "nominalVoltage".equals(element.getName())) {
				log.debug("handleAttribute cim:BaseVoltage.nominalVoltage = " + value);
				setNominalVoltage(value);
				handled = true;
			}
		}
		
		return handled;
	}
	
	@Override
	protected String getDisplayName() { return "BaseVoltage"; }
	
	@Override
	public String toString() { return super.toString() + "," + getNominalVoltage(); }
}
