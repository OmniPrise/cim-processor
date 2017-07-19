package com.omniprise.cim.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.omniprise.cim.parse.model.Element;

public class Switch extends ConductingEquipment {
	private static Logger log = LoggerFactory.getLogger(Switch.class); 
	
	private boolean normalOpen = false;
	private boolean zbr = false;

	public Switch() {
		log.debug("Created Switch instance");
	}

	public boolean isNormalOpen() {
		return normalOpen;
	}

	public void setNormalOpen(boolean normalOpen) {
		this.normalOpen = normalOpen;
	}
	
	public boolean isZbr() {
		return zbr;
	}

	public void setZbr(boolean zbr) {
		this.zbr = zbr;
	}

	@Override
	protected boolean handleElement(Element element, String value) {
		boolean handled = false;
		
		if (!(handled = super.handleElement(element, value))) {
			if ("cim".equals(element.getNamespace()) && "Switch".equals(element.getClassName()) && "normalOpen".equals(element.getName())) {
				log.debug("handleElement cim:Switch.normalOpen = " + value);
				setNormalOpen(Boolean.valueOf(value));
				handled = true;
			} else if ("etx".equals(element.getNamespace()) && "Switch".equals(element.getClassName()) && "ZBR".equals(element.getName())) {
				log.debug("handleElement etx:Switch.ZBR = " + value);
				setZbr(Boolean.valueOf(value));
				handled = true;
			}
		}
		
		return handled;
	}
	
	@Override
	protected String getDisplayName() { return "Switch"; }
	
	@Override
	public String toString() { return super.toString() + "," + isNormalOpen(); }
}
