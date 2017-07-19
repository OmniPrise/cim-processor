package com.omniprise.cim.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.omniprise.cim.parse.model.Element;

public class ConductingEquipment extends Equipment {
	private static Logger log = LoggerFactory.getLogger(ConductingEquipment.class); 
	private static Map<String,ConductingEquipment> conductingEquipment = new HashMap<String,ConductingEquipment>();

	private String baseVoltage;

	public ConductingEquipment() {
		log.debug("Created ConductingEquipment instance");
	}
	
	@Override
	public void setId(String id) {
		if (getId() != null) {
			log.debug("setId: remove existing element id " + getId());
			conductingEquipment.remove(getId());
		}
		super.setId(id);
		conductingEquipment.put(getId(),this);
	}
	
	public String getBaseVoltage() {
		return baseVoltage;
	}
	
	public void setBaseVoltage(String baseVoltage) {
		this.baseVoltage = baseVoltage;
	}
	
	public static ConductingEquipment findConductingEquipmentById(String id) {
		return conductingEquipment.get(id);
	}
	
	public static Set<String> getConductingEquipmentIds() {
		return conductingEquipment.keySet();
	}

	@Override
	protected boolean handleAttribute(Element element, String value) {
		boolean handled = false;
		
		if (!(handled = super.handleAttribute(element, value))) {
			if ("cim".equals(element.getNamespace()) && "ConductingEquipment".equals(element.getClassName()) && "BaseVoltage".equals(element.getName())) {
				log.debug("handleAttribute cim:ConductingEquipment.BaseVoltage = " + value);
				setBaseVoltage(value);
				handled = true;
			}
		}
		
		return handled;
	}
	
	@Override
	protected String getDisplayName() { return "ConductingEquipment"; }
	
	@Override
	public String toString() { return super.toString() + "," + getBaseVoltage(); }
}
