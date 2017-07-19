package com.omniprise.cim.model;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.omniprise.cim.parse.model.Element;

// TODO: Get rid of artificial OutageResource and move logic to PowerSystemResource
public class OutageResource extends Rdf {
	private static Logger log = LoggerFactory.getLogger(OutageResource.class); 

	private static Map<String,OutageResource> outageResources = new HashMap<String,OutageResource>();
	
	private String teid;

	public OutageResource() {
		log.debug("Created OutageResource instance");
	}

	public String getTeid() {
		return teid;
	}

	public void setTeid(String teid) {
		this.teid = teid;
	}
	
	public static OutageResource findByTeid(String teid) {
		OutageResource result = null;
		log.debug("findByTeid: find PowerSystemResource element with id " + teid);
		result = outageResources.get(teid);
		if (result == null) log.debug("findByTeid: element not found");
		return result;
	}

	@Override
	protected boolean handleElement(Element element, String value) {
		boolean handled = false;
		
		if (!(handled = super.handleElement(element, value))) {
			if ( ("etx".equals(element.getNamespace()) && "PowerSystemResource".equals(element.getClassName()) && "teid".equals(element.getName()))
					||  ("etx".equals(element.getNamespace()) && "Analog".equals(element.getClassName()) && "teid".equals(element.getName()))
					||  ("etx".equals(element.getNamespace()) && "Discrete".equals(element.getClassName()) && "teid".equals(element.getName()))
					) {
				log.debug("handleElement: " + element.getNamespace() + ":" + element.getClassName() + "." + element.getName() + " = " + value);
				setTeid(value);
				OutageResource existing = outageResources.get(getTeid());
				if (existing != null) {
					log.info("!!!PowerSystemResource seq " + getSeqId() + " teid " + getTeid() + " is not unique!!!  " + getDisplayName() + " id " + getId());
				} else {
					outageResources.put(getTeid(), this);
				}
				handled = true;
			}
		}
		
		return handled;
	}
	
	@Override
	protected String getDisplayName() { return "OutageResource"; }
	
	@Override
	public String toString() { return super.toString() + "," + getTeid(); }
}
