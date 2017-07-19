package com.omniprise.cim.model;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Line extends EquipmentContainer {
	private static Logger log = LoggerFactory.getLogger(Line.class);
	private static Map<String,Line> lines = new HashMap<String,Line>();

	Line() {
		log.debug("Created Line instance");
	}
	
	@Override
	public void setName(String name) {
		log.debug("setName: set name " + name);
		if (getName() != null) {
			log.debug("setName: remove existing Line " + getName());
			lines.remove(getName());
		}
		super.setName(name);
		lines.put(getName(),this);
	}
	
	public static Line findByName(String lineName) {
		Line result = null;
		
		result = lines.get(lineName);
		
		return result;
	}
	
	@Override
	protected String getDisplayName() { return "Line"; }
}
