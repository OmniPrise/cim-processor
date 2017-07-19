package com.omniprise.cim.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.omniprise.cim.parse.model.Element;

public class MeasurementResource extends OutageResource {
	private static Logger log = LoggerFactory.getLogger(MeasurementResource.class); 

	private String measurementTerminal;

	public MeasurementResource() {
		log.debug("Created MeasurementResource instance");
	}
	
	public String getMeasurementTerminal() {
		return measurementTerminal;
	}

	public void setMeasurementTerminal(String measurementTerminal) {
		this.measurementTerminal = measurementTerminal;
	}

	@Override
	protected boolean handleElement(Element element, String value) {
		boolean handled = false;
		
		if (!(handled = super.handleElement(element, value))) {
			if ("cim".equals(element.getNamespace()) && "Measurement".equals(element.getClassName()) && "Terminal".equals(element.getName())) {
				log.debug("handleElement: cim:PowerSystemResource.teid = " + value);
				setMeasurementTerminal(value);
				handled = true;
			}
		}
		
		return handled;
	}
	
	@Override
	protected String getDisplayName() { return "MeasurementResource"; }
}
