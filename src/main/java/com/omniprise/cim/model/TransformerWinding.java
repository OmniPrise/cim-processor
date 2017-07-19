package com.omniprise.cim.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.omniprise.cim.parse.model.Element;

public class TransformerWinding extends ConductingEquipment {
	private static Logger log = LoggerFactory.getLogger(TransformerWinding.class); 

	private String powerTransformer;

	public TransformerWinding() {
		log.debug("Created TransformerWinding instance");
	}
	
	public String getPowerTransformer() {
		return powerTransformer;
	}
	
	public void setPowerTransformer(String powerTransformer) {
		this.powerTransformer = powerTransformer;
	}
	
	@Override
	protected boolean handleAttribute(Element element, String value) {
		boolean handled = false;
		
		if (!(handled = super.handleAttribute(element, value))) {
			if ("cim".equals(element.getNamespace()) && "TransformerWinding".equals(element.getClassName()) && "MemberOf_PowerTransformer".equals(element.getName())) {
				log.debug("handleAttribute cim:TransformerWinding.MemberOf_PowerTransformer = " + value);
				setPowerTransformer(value);
				handled = true;
			}
		}
		
		return handled;
	}
	
	@Override
	protected String getDisplayName() { return "TransformerWinding"; }
	
	@Override
	public String toString() { return super.toString() + "," + getPowerTransformer(); }
}
