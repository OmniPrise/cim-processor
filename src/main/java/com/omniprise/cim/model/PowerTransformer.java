package com.omniprise.cim.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PowerTransformer extends EquipmentContainer {
	private static Logger log = LoggerFactory.getLogger(PowerTransformer.class); 
	
	private static Map<String,PowerTransformer> powerTransformers = new HashMap<String,PowerTransformer>();

	public PowerTransformer() {
		log.debug("Created PowerTransformer instance");
	}

	public static Set<String> getPowerTransformerIds() {
		return powerTransformers.keySet();
	}
	
	public static PowerTransformer findByPowerTransformerId(String powerTransformerId) {
		return powerTransformers.get(powerTransformerId);
	}
	
	@Override
	public void setId(String id) {
		if (getId() != null) {
			log.debug("setId: remove existing element id " + getId());
			powerTransformers.remove(getId());
		}
		super.setId(id);
		powerTransformers.put(getId(),this);
	}
	
	@Override
	protected String getDisplayName() { return "PowerTransformer"; }
}
