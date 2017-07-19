package com.omniprise.cim.outage.model;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import javax.xml.datatype.XMLGregorianCalendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.omniprise.cim.graph.ACLineSegmentNode;
import com.omniprise.cim.graph.BreakerNode;
import com.omniprise.cim.graph.CustomerLoadNode;
import com.omniprise.cim.graph.DisconnectorNode;
import com.omniprise.cim.graph.LineNode;
import com.omniprise.cim.graph.MeasurementResourceNode;
import com.omniprise.cim.graph.OutageResourceNode;
import com.omniprise.cim.graph.PowerTransformerNode;
import com.omniprise.cim.graph.RdfNode;
import com.omniprise.cim.graph.SeriesCompensatorNode;
import com.omniprise.cim.graph.ShuntCompensatorNode;
import com.omniprise.cim.graph.StaticVarCompensatorNode;
import com.omniprise.cim.graph.SubstationNode;
import com.omniprise.cim.graph.SwitchNode;
import com.omniprise.cim.graph.TerminalNode;
import com.omniprise.cim.model.OutageResource;
import com.omniprise.cim.model.Rdf;
import com.omniprise.cim.parse.model.EquipmentType;
import com.omniprise.cim.parse.model.Outage;

public class OutageModel {
	private static Logger log = LoggerFactory.getLogger(OutageModel.class); 

	private static DateFormat formatter = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");

	public static String outageToString(Outage outage) {
		StringBuffer result = new StringBuffer();
		
		result.append("");
		if (outage.getPlannedStartDate() != null) result.append(formatter.format(outage.getPlannedStartDate().toGregorianCalendar().getTime()));
		result.append(",");
		if (outage.getPlannedEndDate() != null) result.append(formatter.format(outage.getPlannedEndDate().toGregorianCalendar().getTime()));
		result.append(",");
		if (outage.getActualStartDate() != null) result.append(formatter.format(outage.getActualStartDate().toGregorianCalendar().getTime()));
		result.append(",");
		if (outage.getActualEndDate() != null) result.append(formatter.format(outage.getActualEndDate().toGregorianCalendar().getTime()));
		result.append(",");
		if (outage.getEffectiveStartHour() != null) result.append(formatter.format(outage.getEffectiveStartHour().toGregorianCalendar().getTime()));
		result.append(",");
		if (outage.getEffectiveEndHour() != null) result.append(formatter.format(outage.getEffectiveEndHour().toGregorianCalendar().getTime()));
		result.append(",");
		if (outage.getSwitchOutageStatus() != null) result.append(outage.getSwitchOutageStatus());
		result.append(",");
		result.append(outage.getEquipmentType());
		result.append(",");
		if (outage.getEquipmentName() != null) result.append(outage.getEquipmentName());
		result.append(",");
		if (outage.getEquipmentFromStationName() != null) result.append(outage.getEquipmentFromStationName());
		result.append(",");
		if (outage.getEquipmentToStationName() != null) result.append(outage.getEquipmentToStationName());
		result.append(",");
		result.append(outage.getTeid());
		
		return result.toString();
	}
	
	public static String hiddenOutageToString(Outage outage) {
		StringBuffer result = new StringBuffer();
		
		result.append("");
		if (outage.getPlannedStartDate() != null) result.append(formatter.format(outage.getPlannedStartDate().toGregorianCalendar().getTime()));
		result.append(",");
		if (outage.getPlannedEndDate() != null) result.append(formatter.format(outage.getPlannedEndDate().toGregorianCalendar().getTime()));
		result.append(",");
		result.append(outage.getEquipmentType());
		result.append(",");
		if (outage.getEquipmentName() != null) result.append(outage.getEquipmentName());
		result.append(",");
		if (outage.getEquipmentFromStationName() != null) result.append(outage.getEquipmentFromStationName());
		result.append(",");
		if (outage.getEquipmentToStationName() != null) result.append(outage.getEquipmentToStationName());
		result.append(",");
		if (outage.getBaseVoltage() != null) result.append(outage.getBaseVoltage());
		result.append(",");
		result.append(outage.getTeid());
		
		return result.toString();
	}

	public static String hiddenOutageHeader() {
		return "StartDate,EndDate,EquipmentType,EquipmentName,EquipmentFromStation,EquipmentToStation,BaseVoltage,TEID";
	}
	
	public static String hiddenOutageDifferenceHeader() {
		return "StartDate,EndDate,EquipmentType,EquipmentName,EquipmentFromStation,EquipmentToStation,BaseVoltage,TEID,OutageStatusChange";
	}
	
	public static boolean outageAndResourceEquipmentMatchType(Outage outage, RdfNode outageResource) {
		boolean result = false;
		
		if  (MeasurementResourceNode.class.isInstance(outageResource)) {
			MeasurementResourceNode measurementResource = (MeasurementResourceNode)outageResource;
			TerminalNode terminal = measurementResource.getTerminal();
			if (terminal != null && terminal.getConductingEquipment() != null) {
				outageResource = terminal.getConductingEquipment();
			} else {
				outageResource = null;
			}
		}		
		if (outageResource != null && ((LineNode.class.isInstance(outageResource) && "LN".equals(outage.getEquipmentType().value()))
				|| (ACLineSegmentNode.class.isInstance(outageResource) && "LN".equals(outage.getEquipmentType().value()))
				|| (PowerTransformerNode.class.isInstance(outageResource) && "XF".equals(outage.getEquipmentType().value()))
				|| (CustomerLoadNode.class.isInstance(outageResource) && "LD".equals(outage.getEquipmentType().value()))
				|| (BreakerNode.class.isInstance(outageResource) && "CB".equals(outage.getEquipmentType().value()))
				|| (DisconnectorNode.class.isInstance(outageResource) && "DSC".equals(outage.getEquipmentType().value()))
				|| (ShuntCompensatorNode.class.isInstance(outageResource) && "SVC".equals(outage.getEquipmentType().value()))
				|| (StaticVarCompensatorNode.class.isInstance(outageResource) && "SVC".equals(outage.getEquipmentType().value()))
				|| (SeriesCompensatorNode.class.isInstance(outageResource) && "SC".equals(outage.getEquipmentType().value()))) ) {
			result = true;
		} else if (outageResource != null) {
			log.info("Outage and Resource equipoment types don't match: " + outageToString(outage) + " vs " + outageResource.toIdentifier());
		}
		return result;
	}
	
	public enum MatchResult { MATCH, MISMATCH, INDIRECT_MATCH, UNEXPECTED_EQUIPMENT_TYPE, MAPFAILURE_UNEXPECTED_EQUPMENT_TYPE, UNMATCHED_TEID, MISSING_SUBSTATION, MAPFAILURE_TERMINAL, MAPFAILURE_EQUIPMENT }

	public static class OutageMatch {
		private MatchResult matchResult = null;
		private RdfNode node = null;

		public MatchResult getMatchResult() {
			return matchResult;
		}
		public void setMatchResult(MatchResult matchResult) {
			this.matchResult = matchResult;
		}
		public RdfNode getNode() {
			return node;
		}
		public void setNode(RdfNode node) {
			this.node = node;
		}
	}
	
	public static OutageMatch match(Outage outage, int model, boolean logDetail) {
		OutageMatch result = new OutageMatch();
		result.setMatchResult(MatchResult.MISMATCH);
		
		Rdf rdf = OutageResource.findByTeid("" + outage.getTeid());
		if (rdf == null) {
			if (logDetail) log.info("Unable to find TEID " + outage.getTeid() + " in CIM model");
			result.setMatchResult(MatchResult.UNMATCHED_TEID);
		} else {
			OutageResourceNode outageResource = (OutageResourceNode)RdfNode.findByNodeId(rdf.getId(), model);
			result.setNode(outageResource);
			if (LineNode.class.isInstance(outageResource)) {
				if (outageResource.getName().equals(outage.getEquipmentName()) && "LN".equals(outage.getEquipmentType().value())) {
					result.setMatchResult(MatchResult.MATCH);
				} else {
					if (logDetail) log.info("Mismatched: " + OutageModel.outageToString(outage) + " vs " + outageResource.toIdentifier());
					result.setMatchResult(MatchResult.MISMATCH);
				}
			} else if (ACLineSegmentNode.class.isInstance(outageResource)) {
				if (outageResource.getName().equals(outage.getEquipmentName()) && "LN".equals(outage.getEquipmentType().value())) {
					result.setMatchResult(MatchResult.MATCH);
				} else {
					ACLineSegmentNode acLineSegment = (ACLineSegmentNode)outageResource;
					LineNode line = (LineNode)acLineSegment.getEquipmentContainer();
					if ((line.getName() + acLineSegment.getName()).equals(outage.getEquipmentName()) && "LN".equals(outage.getEquipmentType().value())) {
						result.setMatchResult(MatchResult.INDIRECT_MATCH);
					} else {
						if (logDetail) log.info("Mismatched: " + OutageModel.outageToString(outage) + " vs " + outageResource.toIdentifier() + " in " + line.toIdentifier());
						result.setMatchResult(MatchResult.MISMATCH);
					}
				}
			} else {
				SubstationNode substation = outageResource.getSubstation();
				if (substation == null) {
					if (logDetail) log.info("Missing Substation: " + OutageModel.outageToString(outage) + " vs " + outageResource.toIdentifier());
					result.setMatchResult(MatchResult.MISSING_SUBSTATION);
				} else if (PowerTransformerNode.class.isInstance(outageResource)) {
					if (substation.getName().equals(outage.getEquipmentFromStationName()) && outageResource.getName().equals(outage.getEquipmentName()) && "XF".equals(outage.getEquipmentType().value())) {
						result.setMatchResult(MatchResult.MATCH);
					} else {
						if (logDetail) log.info("Mismatched: " + OutageModel.outageToString(outage) + " vs " + outageResource.toIdentifier() + " at " + substation.toIdentifier());
						result.setMatchResult(MatchResult.MISMATCH);
					}
				} else if (CustomerLoadNode.class.isInstance(outageResource)) {
					if (substation.getName().equals(outage.getEquipmentFromStationName()) && outageResource.getName().equals(outage.getEquipmentName()) && "LD".equals(outage.getEquipmentType().value())) {
						result.setMatchResult(MatchResult.MATCH);
					} else {
						if (logDetail) log.info("Mismatched: " + OutageModel.outageToString(outage) + " vs " + outageResource.toIdentifier() + " at " + substation.toIdentifier());
						result.setMatchResult(MatchResult.MISMATCH);
					}
				} else if (BreakerNode.class.isInstance(outageResource)) {
					if (substation.getName().equals(outage.getEquipmentFromStationName()) && outageResource.getName().equals(outage.getEquipmentName()) && "CB".equals(outage.getEquipmentType().value())) {
						result.setMatchResult(MatchResult.MATCH);
					} else {
						if (logDetail) log.info("Mismatched: " + OutageModel.outageToString(outage) + " vs " + outageResource.toIdentifier() + " at " + substation.toIdentifier());
						result.setMatchResult(MatchResult.MISMATCH);
					}
				} else if (DisconnectorNode.class.isInstance(outageResource)) {
					if (substation.getName().equals(outage.getEquipmentFromStationName()) && outageResource.getName().equals(outage.getEquipmentName()) && "DSC".equals(outage.getEquipmentType().value())) {
						result.setMatchResult(MatchResult.MATCH);
					} else {
						if (logDetail) log.info("Mismatched: " + OutageModel.outageToString(outage) + " vs " + outageResource.toIdentifier() + " at " + substation.toIdentifier());
						result.setMatchResult(MatchResult.MISMATCH);
					}
				} else if (ShuntCompensatorNode.class.isInstance(outageResource)) {
					if (substation.getName().equals(outage.getEquipmentFromStationName()) && outageResource.getName().equals(outage.getEquipmentName()) && "SVC".equals(outage.getEquipmentType().value())) {
						result.setMatchResult(MatchResult.MATCH);
					} else {
						if (logDetail) log.info("Mismatched: " + OutageModel.outageToString(outage) + " vs " + outageResource.toIdentifier() + " at " + substation.toIdentifier());
						result.setMatchResult(MatchResult.MISMATCH);
					}
				} else if (StaticVarCompensatorNode.class.isInstance(outageResource)) {
					if (substation.getName().equals(outage.getEquipmentFromStationName()) && outageResource.getName().equals(outage.getEquipmentName()) && "SVC".equals(outage.getEquipmentType().value())) {
						result.setMatchResult(MatchResult.MATCH);
					} else {
						if (logDetail) log.info("Mismatched: " + OutageModel.outageToString(outage) + " vs " + outageResource.toIdentifier() + " at " + substation.toIdentifier());
						result.setMatchResult(MatchResult.MISMATCH);
					}
				} else if (SeriesCompensatorNode.class.isInstance(outageResource)) {
					if (substation.getName().equals(outage.getEquipmentFromStationName()) && outageResource.getName().equals(outage.getEquipmentName()) && "SC".equals(outage.getEquipmentType().value())) {
						result.setMatchResult(MatchResult.MATCH);
					} else {
						if (logDetail) log.info("Mismatched: " + OutageModel.outageToString(outage) + " vs " + outageResource.toIdentifier() + " at " + substation.toIdentifier());
						result.setMatchResult(MatchResult.MISMATCH);
					}
				} else if (MeasurementResourceNode.class.isInstance(outageResource)) {
					MeasurementResourceNode measurementResource = (MeasurementResourceNode)outageResource;
					TerminalNode terminal = measurementResource.getTerminal();
					if (terminal == null) {
						if (logDetail) log.info("Unable to map MeasurementEquipment to Terminal: " + OutageModel.outageToString(outage) + " vs " + outageResource.toIdentifier() + " at " + substation.toIdentifier());
						result.setMatchResult(MatchResult.MAPFAILURE_TERMINAL);
					} else if (terminal.getConductingEquipment() == null) {
						if (logDetail) log.info("Unable to map Termina to ConductingEquipment: " + OutageModel.outageToString(outage) + " vs " + outageResource.toIdentifier() + " terminal " + terminal.getConductingEquipment().toIdentifier() + " at " + substation.toIdentifier());
						result.setMatchResult(MatchResult.MAPFAILURE_EQUIPMENT);
					} else {
						OutageResourceNode mappedOutageResource = terminal.getConductingEquipment();
						result.setNode(mappedOutageResource);
						substation = mappedOutageResource.getSubstation();
						if (substation == null) {
							if (logDetail) log.info("Missing Substation: " + OutageModel.outageToString(outage) + " vs " + outageResource.toIdentifier() + " mapped to " + mappedOutageResource.toIdentifier());
							result.setMatchResult(MatchResult.MISSING_SUBSTATION);
						} else if (PowerTransformerNode.class.isInstance(mappedOutageResource)) {
							if (substation.getName().equals(outage.getEquipmentFromStationName()) && mappedOutageResource.getName().equals(outage.getEquipmentName()) && "XF".equals(outage.getEquipmentType().value())) {
								result.setMatchResult(MatchResult.INDIRECT_MATCH);
							} else {
								if (logDetail) log.info("Mismatched: " + OutageModel.outageToString(outage) + " vs " + outageResource.toIdentifier() + " mapped to " + mappedOutageResource.toIdentifier() + " at " + substation.toIdentifier());
								result.setMatchResult(MatchResult.MISMATCH);
							}
						} else if (CustomerLoadNode.class.isInstance(mappedOutageResource)) {
							if (substation.getName().equals(outage.getEquipmentFromStationName()) && mappedOutageResource.getName().equals(outage.getEquipmentName()) && "LD".equals(outage.getEquipmentType().value())) {
								result.setMatchResult(MatchResult.INDIRECT_MATCH);
							} else {
								if (logDetail) log.info("Mismatched: " + OutageModel.outageToString(outage) + " vs " + outageResource.toIdentifier() + " mapped to " + mappedOutageResource.toIdentifier() + " at " + substation.toIdentifier());
								result.setMatchResult(MatchResult.MISMATCH);
							}
						} else if (BreakerNode.class.isInstance(mappedOutageResource)) {
							if (substation.getName().equals(outage.getEquipmentFromStationName()) && mappedOutageResource.getName().equals(outage.getEquipmentName()) && "CB".equals(outage.getEquipmentType().value())) {
								result.setMatchResult(MatchResult.INDIRECT_MATCH);
							} else {
								if (logDetail) log.info("Mismatched: " + OutageModel.outageToString(outage) + " vs " + outageResource.toIdentifier() + " mapped to " + mappedOutageResource.toIdentifier() + " at " + substation.toIdentifier());
								result.setMatchResult(MatchResult.MISMATCH);
							}
						} else if (DisconnectorNode.class.isInstance(mappedOutageResource)) {
							if (substation.getName().equals(outage.getEquipmentFromStationName()) && mappedOutageResource.getName().equals(outage.getEquipmentName()) && "DSC".equals(outage.getEquipmentType().value())) {
								result.setMatchResult(MatchResult.INDIRECT_MATCH);
							} else {
								if (logDetail) log.info("Mismatched: " + OutageModel.outageToString(outage) + " vs " + outageResource.toIdentifier() + " mapped to " + mappedOutageResource.toIdentifier() + " at " + substation.toIdentifier());
								result.setMatchResult(MatchResult.MISMATCH);
							}
						} else if (ShuntCompensatorNode.class.isInstance(mappedOutageResource)) {
							if (substation.getName().equals(outage.getEquipmentFromStationName()) && mappedOutageResource.getName().equals(outage.getEquipmentName()) && "SVC".equals(outage.getEquipmentType().value())) {
								result.setMatchResult(MatchResult.INDIRECT_MATCH);
							} else {
								if (logDetail) log.info("Mismatched: " + OutageModel.outageToString(outage) + " vs " + outageResource.toIdentifier() + " mapped to " + mappedOutageResource.toIdentifier() + " at " + substation.toIdentifier());
								result.setMatchResult(MatchResult.MISMATCH);
							}
						} else if (StaticVarCompensatorNode.class.isInstance(mappedOutageResource)) {
							if (substation.getName().equals(outage.getEquipmentFromStationName()) && mappedOutageResource.getName().equals(outage.getEquipmentName()) && "SVC".equals(outage.getEquipmentType().value())) {
								result.setMatchResult(MatchResult.INDIRECT_MATCH);
							} else {
								if (logDetail) log.info("Mismatched: " + OutageModel.outageToString(outage) + " vs " + outageResource.toIdentifier() + " mapped to " + mappedOutageResource.toIdentifier() + " at " + substation.toIdentifier());
								result.setMatchResult(MatchResult.MISMATCH);
							}
						} else if (SeriesCompensatorNode.class.isInstance(mappedOutageResource)) {
							if (substation.getName().equals(outage.getEquipmentFromStationName()) && mappedOutageResource.getName().equals(outage.getEquipmentName()) && "SC".equals(outage.getEquipmentType().value())) {
								result.setMatchResult(MatchResult.INDIRECT_MATCH);
							} else {
								if (logDetail) log.info("Mismatched: " + OutageModel.outageToString(outage) + " vs " + outageResource.toIdentifier() + " mapped to " + mappedOutageResource.toIdentifier() + " at " + substation.toIdentifier());
								result.setMatchResult(MatchResult.MISMATCH);
							}
						} else {
							if (logDetail) log.info("Unexpected OutageResource: " + OutageModel.outageToString(outage) + " vs " + outageResource.toIdentifier() + " mapped to " + mappedOutageResource.toIdentifier() + " at " + substation.toIdentifier());
							result.setMatchResult(MatchResult.MAPFAILURE_UNEXPECTED_EQUPMENT_TYPE);
						}
					}
				} else {
					if (logDetail) log.info("Unexpected OutageResource: " + OutageModel.outageToString(outage) + " vs " + outageResource.toIdentifier() + " at " + substation.toIdentifier());
					result.setMatchResult(MatchResult.UNEXPECTED_EQUIPMENT_TYPE);
				}
			}
		}

		return result;
	}
	
	public static boolean applyOutage(Outage outage, OutageMatch outageMatch, boolean logDetail) {
		boolean result = false;
		
		OutageModel.MatchResult matchResult = outageMatch.getMatchResult();
		OutageResourceNode outageResource = (OutageResourceNode)outageMatch.getNode();
		if (outageResource != null) {
			if (!outage.getEquipmentName().equalsIgnoreCase(outageResource.getName())) {
				log.debug("Outage teid " + outage.getTeid() + " named " + outage.getEquipmentName() + " does not match OutageResource name " + outageResource.getName());
			}
			if (matchResult == MatchResult.MISSING_SUBSTATION && OutageModel.outageAndResourceEquipmentMatchType(outage,outageResource)) {
				// verified the equipment types match before applying outage
				if (logDetail) log.info("Applying outage missing substation: " + outageToString(outage));
				result = true;
			} else if (matchResult != MatchResult.MISSING_SUBSTATION
					&& matchResult != MatchResult.UNEXPECTED_EQUIPMENT_TYPE 
					&& matchResult != MatchResult.MAPFAILURE_UNEXPECTED_EQUPMENT_TYPE 
					&& matchResult != MatchResult.MAPFAILURE_TERMINAL
					&& matchResult != MatchResult.MAPFAILURE_EQUIPMENT) {
				if (logDetail) log.info("Applying outage: " + outageToString(outage));
				result = true;
			} else {
				if (logDetail) log.info("Skipping outage: " + outageToString(outage));
			}
			if (result = true) {
				outage.setRdfGuid(outageResource.getId());
				outageResource.setOutage(true);
				if (SwitchNode.class.isInstance(outageResource)) {
					SwitchNode node = (SwitchNode)outageResource;
					if (outage.getSwitchOutageStatus() != null) {
						if ("O".equals(outage.getSwitchOutageStatus().value())) {
							node.setOpen(true);
						} else if ("C".equals(outage.getSwitchOutageStatus().value())) {
							node.setOpen(false);
						}
					}
				}
			}
		}

		return result;
	}
	
	public static Outage createOutage(OutageResourceNode node, XMLGregorianCalendar startDate, XMLGregorianCalendar endDate) {
		Outage result = new Outage();
		
		result.setEffectiveStartHour((XMLGregorianCalendar)startDate.clone());
		result.setEffectiveEndHour((XMLGregorianCalendar)endDate.clone());
		result.setActualStartDate((XMLGregorianCalendar)startDate.clone());
		result.setActualEndDate((XMLGregorianCalendar)endDate.clone());
		result.setPlannedStartDate((XMLGregorianCalendar)startDate.clone());
		result.setPlannedEndDate((XMLGregorianCalendar)endDate.clone());
		result.setEquipmentName(node.getName());
		result.setTeid(node.getTeid());
		result.setRdfGuid(node.getId());
		if (ACLineSegmentNode.class.isInstance(node)) {
			ACLineSegmentNode acLineSegment = (ACLineSegmentNode)node;
			result.setEquipmentType(EquipmentType.LN);
			result.setBaseVoltage(acLineSegment.getBaseVoltage().getNominalVoltage());
			if (acLineSegment.getFromStation() != null) result.setEquipmentFromStationName(acLineSegment.getFromStation().getName());
			if (acLineSegment.getToStation() != null) result.setEquipmentToStationName(acLineSegment.getToStation().getName());
		} else if (PowerTransformerNode.class.isInstance(node)) {
			PowerTransformerNode powerTransformer = (PowerTransformerNode)node;
			result.setEquipmentType(EquipmentType.XF);
			result.setEquipmentFromStationName(powerTransformer.getSubstation().getName());
		}
		
		return result;
	}
}
