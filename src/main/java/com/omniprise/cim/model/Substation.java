package com.omniprise.cim.model;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Substation extends EquipmentContainer {
	private static Logger log = LoggerFactory.getLogger(Substation.class);
	private static Map<String,Substation> substations = new HashMap<String,Substation>();
	
	Substation() {
		log.debug("Created Substation instance");
	}
	
	@Override
	public void setName(String name) {
		log.debug("setName: set name " + name);
		if (getName() != null) {
			log.debug("setName: remove existing Substation " + getName());
			substations.remove(getName());
		}
		super.setName(name);
		substations.put(getName(),this);
	}
	
	public static Substation findByName(String substationName) {
		Substation result = null;
		
		result = substations.get(substationName);
		
		return result;
	}
	
	@Override
	protected String getDisplayName() { return "Substation"; }
}
