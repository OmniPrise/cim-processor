package com.omniprise.cim.graph;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.omniprise.cim.model.ACLineSegment;
import com.omniprise.cim.model.Rdf;

public class ACLineSegmentNode extends ConductingEquipmentNode implements DualTerminalEquipment {
	private static Logger log = LoggerFactory.getLogger(ACLineSegmentNode.class); 
	
	private LineNode line = null;
	private SubstationNode fromStation = null;
	private SubstationNode toStation = null;

	public ACLineSegmentNode(Rdf source, int model) {
		super(source, model);
		log.debug("Created ACLineSegmentNode");
	}

	@Override
	public String getName() {
		String result = super.getName();
		if (line != null) result = line.getName() + result;
		
		return result;
	}

	public LineNode getLine() {
		return line;
	}

	public void setLine(LineNode line) {
		this.line = line;
	}

	public SubstationNode getFromStation() {
		return fromStation;
	}

	public void setFromStation(SubstationNode fromStation) {
		this.fromStation = fromStation;
	}

	public SubstationNode getToStation() {
		return toStation;
	}

	public void setToStation(SubstationNode toStation) {
		this.toStation = toStation;
	}
	
	@Override
	public void connect() {
		super.connect();
		ACLineSegment acLineSegment = (ACLineSegment)getSource();
		// TODO: This logic is odd, and keeping both the line and the equipment container seems redundant
		if(line == null && acLineSegment.getEquipmentContainer() != null) line = (LineNode)RdfNode.findByNodeId(acLineSegment.getEquipmentContainer(), getModel());
		if (getEquipmentContainer() != null) {
			LineNode line = (LineNode)getEquipmentContainer();
			line.addLineSegment(this);
		}
	}

	@Override
	public void disconnect() {
		if (line != null) {
			line.removeLineSegment(this);
		}
		super.disconnect();
		line = null;
		fromStation = null;
		toStation = null;
	}

	public static String getNodeName() { return "ACLineSegment"; }

	@Override
	public String getDiagramType() { return "ln"; }
	
	@Override
	public String getDiagramShape() { return "house"; }

	@Override
	public String getDisplayName() { return "ACLineSegment"; }
	
	@Override
	public String toString() { 
		String result = super.toString();
		ACLineSegment acLineSegment = (ACLineSegment)getSource();
		//RdfNode line = RdfNode.findByNodeId(acLineSegment.getEquipmentContainer(), getModel());
		if (getLine() == null) {
			result += ",Line," + acLineSegment.getEquipmentContainer() + "-NotPresentInRdfModel";
		} else {
			result += "," + getLine().toIdentifier();
		}
		return result;
	}
}
