package com.omniprise.cim.processor;

/*-
 * #%L
 * ERCOT CIM Processor
 * %%
 * Copyright (C) 2017 OmniPrise, LLC
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.PushbackReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.omniprise.cim.graph.ACLineSegmentNode;
import com.omniprise.cim.graph.BayNode;
import com.omniprise.cim.graph.ConductingEquipmentNode;
import com.omniprise.cim.graph.ConnectivityElementNode;
import com.omniprise.cim.graph.ConnectivityNodeGroupNode;
import com.omniprise.cim.graph.DisconnectorNode;
import com.omniprise.cim.graph.DualTerminalEquipment;
import com.omniprise.cim.graph.LineNode;
import com.omniprise.cim.graph.OutageResourceNode;
import com.omniprise.cim.graph.PowerTransformerNode;
import com.omniprise.cim.graph.RdfGraph;
import com.omniprise.cim.graph.RdfNode;
import com.omniprise.cim.graph.SingleTerminalEquipment;
import com.omniprise.cim.graph.SubstationNode;
import com.omniprise.cim.graph.SwitchNode;
import com.omniprise.cim.graph.TerminalNode;
import com.omniprise.cim.graph.TransformerWindingNode;
import com.omniprise.cim.graph.VoltageLevelNode;
import com.omniprise.cim.model.BusbarSection;
import com.omniprise.cim.model.ConductingEquipment;
import com.omniprise.cim.model.ConnectivityNode;
import com.omniprise.cim.model.CustomerLoad;
import com.omniprise.cim.model.EndCap;
import com.omniprise.cim.model.Equipment;
import com.omniprise.cim.model.GroundDisconnector;
import com.omniprise.cim.model.Line;
import com.omniprise.cim.model.PowerTransformer;
import com.omniprise.cim.model.Rdf;
import com.omniprise.cim.model.RdfModel;
import com.omniprise.cim.model.ShuntCompensator;
import com.omniprise.cim.model.StaticVarCompensator;
import com.omniprise.cim.model.StationSupply;
import com.omniprise.cim.model.Substation;
import com.omniprise.cim.model.SynchronousMachine;
import com.omniprise.cim.model.Terminal;
import com.omniprise.cim.model.TransformerWinding;
import com.omniprise.cim.model.VoltageLevel;
import com.omniprise.cim.outage.model.HiddenOutageComparator;
import com.omniprise.cim.outage.model.OutageModel;
import com.omniprise.cim.outage.model.OutageModel.MatchResult;
import com.omniprise.cim.psse.model.Bus;
import com.omniprise.cim.psse.model.PsseModel;
import com.omniprise.cim.parse.model.Attribute;
import com.omniprise.cim.parse.model.Element;
import com.omniprise.cim.parse.model.EquipmentType;
import com.omniprise.cim.parse.model.Outage;
import com.omniprise.cim.parse.model.SwitchOutageStatus;

public class ModelParser {
	private static Logger log = LoggerFactory.getLogger(ModelParser.class); 
	
	private static String cimFileName = null;
	private static String outageFileName = null;
	private static String outageDate = "11-01-2014 13:30:00";
	
	private static Element model = null;
	private static Element current = null;
	private static Rdf currentRdf = null;
	private static HashMap<String,Element> map = new HashMap<String,Element>();
	private static HashMap<String,Element> classMap = new HashMap<String,Element>();
	private static HashMap<String,Element> identifiedObjectMap = new HashMap<String,Element>();
	private static HashMap<String,Attribute> attributeMap = new HashMap<String,Attribute>();
	private static HashMap<String,Element> idElements = new HashMap<String,Element>();
	private static HashMap<String,Element> resourceElements = new HashMap<String,Element>();
	private static LinkedList<Element> stack = new LinkedList<Element>();
	
	private static DateFormat formatter = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");
	private static DateFormat csvFormatter = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
	private static long cimElements = 0;
	private static long errors = 0L;
	private static int maxDepth = 0;
	
	private static List<Outage> outages = new LinkedList<Outage>();

	private static boolean reportParseAnalysis = false;
	private static List<String> reportRdfNode = new LinkedList<String>();
	private static List<String> reportSubstationEquipment = new LinkedList<String>();
	private static List<String> diagramSubstationEquipment = new LinkedList<String>();
	private static List<String> diagramLineSubstations = new LinkedList<String>();
	private static boolean reportIslandEquipment = false;
	private static boolean reportIslandEquipmentDetail = false;
	private static boolean reportRadialLines = false;
	private static boolean reportRadialLinesDetail = false;
	private static boolean reportDisconnectedEquipment = false;
	private static boolean reportDisconnectedEquipmentDetail = false;
	private static boolean reportOutageAnalysis = false;
	private static boolean reportOutageAnalysisDetail = false;
	private static boolean reportImpactAnalysis = false;
	private static boolean reportImpactAnalysisDetail = false;
	private static boolean reportImpactAnalysisDiagrams = false;
	private static boolean testOutageIncrements = false;
	
	private static boolean collapseUsingConnectivityNodeGroup = false;
	private static boolean collapseUsingConnectivityNode = false;

	private static boolean reportConnectivityNodeAnalysis = false;
	
	private static int dailyOutageAnalysisThreads = 1;
	private static String dailyOutageAnalysisStartDate = null;
	private static String dailyOutageAnalysisDifferenceStartDate = null;
	private static int dailyOutageAnalysisDays = 1;
	private static boolean dailyAnalysisIncludeRadial = false;
	private static boolean dailyAnalysisIncludeIslands = false;
	private static boolean dailyAnalysisLineDetail = false;
	private static int dailyOutageQueueDepthLimit = -1;
	private static int dailyOutageQueueDepthRelease = -1;

	private static String currentHiddenOutageDate = null;
	private static String priorHiddenOutageDate = null;

	public static void main(String[] args) {
		boolean proceed = !parseCommandLine(args);
		
		if (proceed) {
			if (cimFileName != null) {
				PushbackReader in = null;
				try {
					in = new PushbackReader(new FileReader(cimFileName));
				} catch (FileNotFoundException e) {
					log.error("Supplied CIM file not found: " + cimFileName);
				}
				if (in != null) {
					File file = new File(cimFileName);
					log.info("Start CIM Parse: " + formatter.format(new Date()));
					log.info("CIM File: " + file.getName());
					cimElements = processCimFile(in);
					log.info("End CIM Parse: " + formatter.format(new Date()));
					analizeCim();
					try {
						in.close();
					} catch (IOException e) {
						// ignore any error closing the file
					}
				}
			}
			if (outageFileName != null) {
				CSVParser in = null;
				try {
					in = new CSVParser(new FileReader(outageFileName),CSVFormat.EXCEL.withHeader());
				} catch (IOException e) {
					log.error("Supplied Outage file not found: " + outageFileName);
				}
				if (in != null) {
					File file = new File(outageFileName);
					log.info("Start Outage Parse: " + formatter.format(new Date()));
					log.info("Outage File: " + file.getName());
					processOutageFile(in);
					log.info("End Outage Parse with " + outages.size() + " records: " + formatter.format(new Date()));
					analizeOutages();
					try {
						in.close();
					} catch (IOException e) {
						// ignore any error closing the file
					}
				}
			}
			if (outages.size() > 0 && Rdf.getAllIds().size() > 0 && (reportImpactAnalysis || reportImpactAnalysisDetail | reportImpactAnalysisDiagrams)) {
				// this is what we came for
				evaluateOutageImpact();
			}
			if (Rdf.getAllIds().size() > 0 && (diagramSubstationEquipment.size() > 0 || diagramLineSubstations.size() > 0)) {
				// establish model
				int model = RdfGraph.initialize();
				RdfGraph.connect(model);
				if (diagramSubstationEquipment.size() > 0) {
					for (String substationName : diagramSubstationEquipment) {
						log.info("Creating substation diagram for " + substationName);
						String fileName = substationName + ".gv";
						diagramSubstation(substationName, fileName, model, false);
					}
				}
				if (diagramLineSubstations.size() > 0) {
					for (String lineName : diagramLineSubstations) {
						log.info("Creating line substation diagram for " + lineName);
						String fileName = "line-" + lineName + ".gv";
						diagramLine(lineName, fileName, model);
					}
				}
				if (outages.size() > 0) {
					GregorianCalendar calendar = new GregorianCalendar();
					try {
						calendar.setTime(formatter.parse(outageDate));
					} catch (ParseException e) {
						// ignore for now
					}
					applyOutages(calendar, model, false);
				}
				if (outages.size() > 0 && diagramSubstationEquipment.size() > 0) {
					for (String substationName : diagramSubstationEquipment) {
						String fileName = substationName + "-outage.gv";
						diagramSubstation(substationName, fileName, model, false);
					}
				}
				if (diagramLineSubstations.size() > 0) {
					for (String lineName : diagramLineSubstations) {
						String fileName = "line-" + lineName + "-outage.gv";
						diagramLine(lineName, fileName, model);
					}
				}
			}
			if (Rdf.getAllIds().size() > 0 && outages.size() > 0 && testOutageIncrements) {
				analizeOutageIncrements();
			}
			if (reportRdfNode.size() > 0) {
				int model = RdfGraph.getCurrentModel();
				if (model < 0) {
					log.info("Establish the model for analysis");
					model = RdfGraph.initialize();
					RdfGraph.connect(model);
				}
				for (String rdfId : reportRdfNode) {
					Rdf rdf = Rdf.findById(rdfId);
					if (rdf != null) {
						log.info("Rdf: " + rdf.toString());
						RdfNode rdfNode = RdfNode.findByNodeId(rdfId, model);
						if (rdfNode != null) {
							log.info("RdfNode: " + rdfNode.toString());
						} else {
							log.info("RdfNode wiht rdf:ID + " + rdfId + " not found in model " + model);
						}
					} else {
						log.info("Rdf with rdf:ID " + rdfId + " not found");
					}
				}
			}
			if (Rdf.getAllIds().size() > 0 && collapseUsingConnectivityNodeGroup) {
				int rdfModel = RdfGraph.getCurrentModel();
				if (rdfModel < 0) {
					log.info("Establish the model for analysis");
					rdfModel = RdfGraph.initialize();
					RdfGraph.connect(rdfModel);
				}
				int psseModel = PsseModel.initialize();
				collapseUsingConnectivityNodeGroup(rdfModel, psseModel);
			}
			if (Rdf.getAllIds().size() > 0 && collapseUsingConnectivityNode) {
				int rdfModel = RdfGraph.getCurrentModel();
				if (rdfModel < 0) {
					log.info("Establish the model for analysis");
					rdfModel = RdfGraph.initialize();
					RdfGraph.connect(rdfModel);
				}
				int psseModel = PsseModel.initialize();
				collapseUsingConnectivityNode(rdfModel, psseModel);
			}
			if (Rdf.getAllIds().size() > 0 && reportConnectivityNodeAnalysis) {
				log.info("");
				log.info("Evaluate ConnectivityNodes");
				int connectivityNodes = 0;
				int connectivityNodesWithoutSubstation = 0;
				int rdfModel = RdfGraph.getCurrentModel();
				if (rdfModel < 0) {
					log.info("Establish the model for analysis");
					rdfModel = RdfGraph.initialize();
					RdfGraph.connect(rdfModel);
				}
				for(String rdfId : Rdf.getAllIds()) {
					RdfNode node = RdfNode.findByNodeId(rdfId, rdfModel);
					if (ConnectivityElementNode.class.isInstance(node)) {
						connectivityNodes++;
						ConnectivityElementNode connectivityNode = (ConnectivityElementNode)node;
						if (connectivityNode.getSubstation() == null) {
							connectivityNodesWithoutSubstation++;
							log.info("ConnectivityNode " + connectivityNode.getId() + " has no substation");
						}
					}
				}
				log.info("Evaluated " + connectivityNodes + " ConnectivityNode elements");
				log.info("Found " + connectivityNodesWithoutSubstation + " ConnectivityNode element(s) without a Substation");
			}
			if (priorHiddenOutageDate != null) {
				CSVParser in = null;
				String fileName = "";
				NavigableSet<Outage> priorHiddenOutages = new TreeSet<Outage>(new HiddenOutageComparator());
				try {
					DateFormat dateFormatter = new SimpleDateFormat("MM-dd-yyyy");
					DateFormat dateOrderedFormatter = new SimpleDateFormat("yyyy-MM-dd");
					fileName = "hidden_outages-" + dateOrderedFormatter.format(dateFormatter.parse(priorHiddenOutageDate)) + ".csv";
					in = new CSVParser(new FileReader(fileName),CSVFormat.EXCEL.withHeader());
				} catch (ParseException e) {
					log.error("Supplied date parsed: " + priorHiddenOutageDate);
				} catch (IOException e) {
					log.error("Supplied Hidden Outage file not found: " + fileName);
				}
				if (in != null) {
					log.info("Start Hidden Outage Parse: " + formatter.format(new Date()));
					log.info("Hidden Outage File: " + fileName);
					processHiddenOutageFile(in, priorHiddenOutages);
					log.info("End Hidden Outage Parse with " + priorHiddenOutages.size() + " records: " + formatter.format(new Date()));
					try {
						in.close();
					} catch (IOException e) {
						// ignore any error closing the file
					}
				}
				if (currentHiddenOutageDate != null) {
					in = null;
					fileName = "";
					NavigableSet<Outage> currentHiddenOutages = new TreeSet<Outage>(new HiddenOutageComparator());
					try {
						DateFormat dateFormatter = new SimpleDateFormat("MM-dd-yyyy");
						DateFormat dateOrderedFormatter = new SimpleDateFormat("yyyy-MM-dd");
						fileName = "hidden_outages-" + dateOrderedFormatter.format(dateFormatter.parse(currentHiddenOutageDate)) + ".csv";
						in = new CSVParser(new FileReader(fileName),CSVFormat.EXCEL.withHeader());
					} catch (ParseException e) {
						log.error("Supplied date parsed: " + currentHiddenOutageDate);
					} catch (IOException e) {
						log.error("Supplied Hidden Outage file not found: " + fileName);
					}
					if (in != null) {
						log.info("Start Hidden Outage Parse: " + formatter.format(new Date()));
						log.info("Hidden Outage File: " + fileName);
						processHiddenOutageFile(in, currentHiddenOutages);
						log.info("End Hidden Outage Parse with " + currentHiddenOutages.size() + " records: " + formatter.format(new Date()));
						try {
							in.close();
						} catch (IOException e) {
							// ignore any error closing the file
						}
					}
					// identify outages in priorHiddenOutages not in currentHiddenOutages
					DatatypeFactory factory = null;
					Outage match;
					try {
						factory = DatatypeFactory.newInstance();
						GregorianCalendar beginningOfTime = new GregorianCalendar();
						beginningOfTime.setTime(formatter.parse("01-01-1900 00:00:00"));
						XMLGregorianCalendar matchDate = factory.newXMLGregorianCalendar(beginningOfTime);
						match = new Outage();
						match.setEffectiveStartHour(matchDate);
						match.setEffectiveEndHour(matchDate);
						for (Outage outage : priorHiddenOutages) {
							match.setTeid(outage.getTeid());
							Outage comparison = currentHiddenOutages.ceiling(match);
							if (comparison == null || comparison.getTeid() != outage.getTeid()) {
								// in prior but not in current
								log.info(OutageModel.hiddenOutageToString(outage) + ",Out");
							} else  {
								boolean overlap = false;
								while (overlap == false && comparison != null && comparison.getTeid() == outage.getTeid()) {
									// walk the list looking for an overlap - using < and > rather than <= and >= due to extra second on endHour in Outage
									if (outage.getEffectiveStartHour().compare(comparison.getEffectiveEndHour()) < 0 &&
										outage.getEffectiveEndHour().compare(comparison.getEffectiveStartHour()) > 0) {
										overlap = true;
									} else {
										comparison = currentHiddenOutages.higher(comparison);
									}
								}
								if (!overlap) {
									// in prior but not in current
									log.info(OutageModel.hiddenOutageToString(outage) + ",Out");
								}
							}
						}
						// identify outages in currentHiddenOutages not in priorHiddenOutages
						for (Outage outage : currentHiddenOutages) {
							match.setTeid(outage.getTeid());
							Outage comparison = priorHiddenOutages.ceiling(match);
							if (comparison == null || comparison.getTeid() != outage.getTeid()) {
								// in current but not in prior
								log.info(OutageModel.hiddenOutageToString(outage) + ",In");
							} else  {
								boolean overlap = false;
								while (overlap == false && comparison != null && comparison.getTeid() == outage.getTeid()) {
									// walk the list looking for an overlap - using < and > rather than <= and >= due to extra second on endHour in Outage
									if (outage.getEffectiveStartHour().compare(comparison.getEffectiveEndHour()) < 0 &&
										outage.getEffectiveEndHour().compare(comparison.getEffectiveStartHour()) > 0) {
										overlap = true;
									} else {
										comparison = priorHiddenOutages.higher(comparison);
									}
								}
								if (!overlap) {
									// in current but not in prior
									log.info(OutageModel.hiddenOutageToString(outage) + ",In");
								}
							}
						}
					} catch (DatatypeConfigurationException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			if (Rdf.getAllIds().size() > 0 && outages.size() > 0 && dailyOutageAnalysisStartDate != null) {
				analizeDailyOutages();
			}
		}
	}
	
	private static void collapseUsingConnectivityNodeGroup(int rdfModel, int psseModel) {
		int connectivityNodeGroups = 0;
		int processedConnectivityNodes = 0;
		int mismatchedConnectivityNodeSubstations = 0;
		for(String rdfId : Rdf.getAllIds()) {
			RdfNode node = RdfNode.findByNodeId(rdfId, rdfModel);
			if (ConnectivityNodeGroupNode.class.isInstance(node)) {
				connectivityNodeGroups++;
				ConnectivityNodeGroupNode connectivityNodeGroup = (ConnectivityNodeGroupNode)node;
				PsseModel.createBus(connectivityNodeGroup, psseModel);
				String substationId = null;
				Map<String,ConnectivityElementNode> connectivityNodes = connectivityNodeGroup.getConnectivityNodes();
				for (String connectivityNodeId : connectivityNodeGroup.getConnectivityNodes().keySet()) {
					ConnectivityElementNode connectivityNode = (ConnectivityElementNode)connectivityNodes.get(connectivityNodeId);
					if (substationId == null) substationId = connectivityNode.getSubstation().getId();
					if (substationId != null && !connectivityNode.getSubstation().getId().equals(substationId)) mismatchedConnectivityNodeSubstations++;
					processedConnectivityNodes++;
				}
			}
		}
		log.info("");
		log.info("Initial collapsing of model using ConnectivityNodeGroup");
		log.info("... examined " + connectivityNodeGroups + " ConnectivityNodeGroups");
		log.info("... associated with " + processedConnectivityNodes + " ConnectivityNodes");
		log.info("... resulted in " + Bus.getBusNames(psseModel).keySet().size() + " busses associated with " + Bus.getConnectivityNodeBusses(psseModel).keySet().size() + " ConnectivityNodes");
		log.info("... there were " + mismatchedConnectivityNodeSubstations + " mismatched ConnectivityNode Substations");
		// add switches and transformer windings, and identify ZBR switches (those with connectivity nodes in two different busses)
		processedConnectivityNodes = 0;
		int includedConnectivityNodes = 0;
		for (String rdfId : Rdf.getAllIds()) {
			Rdf rdf = Rdf.findById(rdfId);
			if (ConnectivityNode.class.isInstance(rdf)) {
				processedConnectivityNodes++;
				if (Bus.getConnectivityNodeBusses(psseModel).containsKey(rdfId)) {
					includedConnectivityNodes++;
				}
			}
		}
		log.info("Examined " + processedConnectivityNodes + " ConnectivityNode elements");
		log.info("... " + includedConnectivityNodes + " of which are mapped to a Bus");
		log.info("... leaving " + (processedConnectivityNodes - includedConnectivityNodes) + " unmapped");
	}
	
	private static void collapseUsingConnectivityNode(int rdfModel, int psseModel) {
		int connectivityNodes = 0;
		for(String rdfId : Rdf.getAllIds()) {
			RdfNode node = RdfNode.findByNodeId(rdfId, rdfModel);
			if (ConnectivityElementNode.class.isInstance(node)) {
				connectivityNodes++;
				ConnectivityElementNode connectivityNode = (ConnectivityElementNode)node;
				if (connectivityNode.getPsseBusName() != null && connectivityNode.getPsseBusNumber() != null) PsseModel.createBus(connectivityNode, psseModel);
			}
		}
		log.info("");
		log.info("Initial collapsing of model using ConnectivityNode");
		log.info("... examined " + connectivityNodes + " ConnectivityNodes");
		log.info("... resulted in " + Bus.getBusNames(psseModel).keySet().size() + " busses associated with " + Bus.getConnectivityNodeBusses(psseModel).keySet().size() + " ConnectivityNodes");
		// add switches (ignoring switch state), associated connectivity nodes and transformer windings, and identify ZBR switches
		Map<String,Bus> bussesByNumber = Bus.getBusNumbers(psseModel);
		Map<String,Bus> connectivityNodeBusses = Bus.getConnectivityNodeBusses(psseModel);
		int differentBusNumberBeforeZbr = 0;
		for (String busNumber : bussesByNumber.keySet()) {
			Bus bus = bussesByNumber.get(busNumber);
			List<String> busConnectivityNodeList = new LinkedList<String>(bus.getPsseEquipment().keySet());
			for (String rdfId : busConnectivityNodeList) {
				// for each bus, visit everything until either a ZBR or ACLineSegment it found, adding equipment to the mix
				ConnectivityElementNode connectivityNode = (ConnectivityElementNode)RdfNode.findByNodeId(rdfId, rdfModel);
				Queue<RdfNode> queue = new LinkedList<RdfNode>();
				connectivityNode.setVisited(true);
				queue.add(connectivityNode);
				while (!queue.isEmpty()) {
					RdfNode node = queue.poll();
					if (TerminalNode.class.isInstance(node)) {
						TerminalNode terminal = (TerminalNode)node;
						RdfNode next = terminal.getConductingEquipment();
						if (next != null && !next.isVisited()) {
							next.setVisited(true);
							queue.add(next);
						}
						next = terminal.getConnectivityNode();
						if (next != null && !next.isVisited()) {
							next.setVisited(true);
							queue.add(next);
						}
					} else if (ConnectivityElementNode.class.isInstance(node)) {
						ConnectivityElementNode currentConnectivityNode = (ConnectivityElementNode)node;
						if (!connectivityNodeBusses.containsKey(currentConnectivityNode.getId())) {
							connectivityNodeBusses.put(currentConnectivityNode.getId(), bus);
							bus.addPsseEquipment(currentConnectivityNode);
						}
						if (bus.getNumber().equals(connectivityNodeBusses.get(currentConnectivityNode.getId()).getNumber())) {
							for (TerminalNode terminal : currentConnectivityNode.getTerminals()) {
								if (!terminal.isVisited()) {
									terminal.setVisited(true);
									queue.add(terminal);
								}
							}
						} else {
							differentBusNumberBeforeZbr++;
							log.debug("... Ran into ConnectivityNode with a different PSSEBusNumber before search broken by a ZBR Switch");
						}
					} else if (SwitchNode.class.isInstance(node)) {
						SwitchNode switchNode = (SwitchNode)node;
						if (!switchNode.isZbr()) {
							bus.addPsseEquipment(switchNode);
							for (TerminalNode terminal : switchNode.getTerminals()) {
								if (!terminal.isVisited()) {
									terminal.setVisited(true);
									queue.add(terminal);
								}
							}
						}
					} else if (TransformerWindingNode.class.isInstance(node)) {
						TransformerWindingNode transformerWinding = (TransformerWindingNode)node;
						bus.addPsseEquipment(transformerWinding);
					}
				}
			}
			for (String rdfId : bus.getPsseEquipment().keySet()) {
				RdfNode node = bus.getPsseEquipment().get(rdfId);
				if (node.isVisited()) RdfGraph.resetFrom(node, true, false, false, false, false);
			}
		}
		log.info("... ran into a different PSSEBusNumber " + differentBusNumberBeforeZbr + " times before hitting a ZBR Swtich");
		log.info("... this likely includes some multiply counted instances");
		connectivityNodes = 0;
		int includedConnectivityNodes = 0;
		for (String rdfId : Rdf.getAllIds()) {
			Rdf rdf = Rdf.findById(rdfId);
			if (ConnectivityNode.class.isInstance(rdf)) {
				connectivityNodes++;
				if (Bus.getConnectivityNodeBusses(psseModel).containsKey(rdfId)) {
					includedConnectivityNodes++;
				}
			}
		}
		log.info("Examined " + connectivityNodes + " ConnectivityNode elements");
		log.info("... " + includedConnectivityNodes + " of which are mapped to a Bus");
		log.info("... leaving " + (connectivityNodes - includedConnectivityNodes) + " unmapped");
	}
	
	public static class AppliedOutages {
		private List<Outage> appliedOutages = null;
		Calendar effectiveStart = null;
		Calendar effectiveEnd = null;
		int outagesApplied = 0;
		int outagesNotApplied = 0;
		int examinedOutages = 0;
		int outagesInDateRange = 0;

		public AppliedOutages() {
			appliedOutages = new LinkedList<Outage>();
		}
		
		public List<Outage> getAppliedOutages() {
			return appliedOutages;
		}

		public Calendar getEffectiveStart() {
			return effectiveStart;
		}

		public void setEffectiveStart(Calendar effectiveStart) {
			this.effectiveStart = effectiveStart;
		}

		public Calendar getEffectiveEnd() {
			return effectiveEnd;
		}

		public void setEffectiveEnd(Calendar effectiveEnd) {
			this.effectiveEnd = effectiveEnd;
		}

		public int getOutagesApplied() {
			return outagesApplied;
		}

		public void incramentOutagesApplied() {
			outagesApplied++;
		}

		public int getOutagesNotApplied() {
			return outagesNotApplied;
		}

		public void incramentOutagesNotApplied() {
			outagesNotApplied++;
		}

		public int getExaminedOutages() {
			return examinedOutages;
		}

		public void incramentExaminedOutages() {
			examinedOutages++;
		}

		public int getOutagesInDateRange() {
			return outagesInDateRange;
		}

		public void incramentOutagesInDateRange() {
			outagesInDateRange++;
		}
	}
	
	private static AppliedOutages applyOutages(Calendar outageDate, int model, boolean logDetail) {
		AppliedOutages result = new AppliedOutages();
		result.setEffectiveStart(outageDate);
		
		// apply outages to model
		log.trace("Apply outages for " + formatter.format(outageDate.getTime()));
		// keep the list of applied outages for later evaluation
		for (Outage outage : outages) {
			result.incramentExaminedOutages();
			if (outage.getEffectiveStartHour().toGregorianCalendar().compareTo(outageDate) <= 0 && outageDate.compareTo(outage.getEffectiveEndHour().toGregorianCalendar()) <= 0) {
				result.incramentOutagesInDateRange();
				OutageModel.OutageMatch outageMatch = OutageModel.match(outage, model, logDetail);
				if (OutageModel.applyOutage(outage, outageMatch, logDetail)) {
					result.getAppliedOutages().add(outage);
					if (result.getEffectiveEnd() == null || result.getEffectiveEnd().compareTo(outage.getEffectiveEndHour().toGregorianCalendar()) > 0) result.setEffectiveEnd((GregorianCalendar)outage.getEffectiveEndHour().toGregorianCalendar().clone());
					result.incramentOutagesApplied();
				} else {
					result.incramentOutagesNotApplied();
				}
			}
		}
		log.trace("Examined " + result.getExaminedOutages() + " outage(s)");
		log.trace("" + result.getOutagesInDateRange() + " outage(s) in date range");
		log.trace("Applied " + result.getOutagesApplied() + " outage(s) in date range");
		log.trace("Did not apply " + result.getOutagesNotApplied() + " outage(s) in date range");

		return result;
	}

	private static List<ACLineSegmentNode> diagramSubstation(String substationName, String fileName, int model, boolean subgraph) {
		List<ACLineSegmentNode> result = new LinkedList<ACLineSegmentNode>();

		Substation substationId = Substation.findByName(substationName);
		if (substationId != null) {
			try {
				PrintWriter writer = new PrintWriter(new FileWriter(fileName));
				SubstationNode substation = (SubstationNode)RdfNode.findByNodeId(substationId.getId(), model);
				// this needs to remain a reset() for the time being
				// TODO: improve resetFrom() to account for EquipmentContainers as well as mapping through Terminals
				RdfGraph.reset(model, true, false, false, false, false);
				result = diagramSubstation(substation, writer, model, subgraph);
				writer.close();
			} catch (IOException e) {
				log.info("Unable to open file: " + fileName);
			}
		}
		
		return result;
	}

	private static List<ACLineSegmentNode> diagramSubstation(SubstationNode substation, PrintWriter writer, int model, boolean subgraph) {
		List<ACLineSegmentNode> result = new LinkedList<ACLineSegmentNode>();
		
		Map<String,TerminalNode> terminals = new HashMap<String,TerminalNode>();
		Map<String,PowerTransformerNode> transformers = new HashMap<String,PowerTransformerNode>();
		Map<String,ACLineSegmentNode> acLineSegments = new HashMap<String,ACLineSegmentNode>();
		Map<String,TransformerWindingNode> unmodeledTransformerWindings = new HashMap<String,TransformerWindingNode>();
		Map<String,Boolean> equipmentModelState = new HashMap<String,Boolean>();
		if (subgraph) writer.println("# subgraph \"" + substation.getName() + "\" {");
		else writer.println("graph \"" + substation.getName() + "\" {");
		List<VoltageLevel> voltageLevelIds = VoltageLevel.findVoltageLevels(substation.getId());
		if (voltageLevelIds != null) {
			for (VoltageLevel voltageLevelId : voltageLevelIds) {
				boolean nonTransformerWindingEquipment = false;
				Map<String,TransformerWindingNode> transformerWindings = new HashMap<String,TransformerWindingNode>();
				VoltageLevelNode voltageLevel = (VoltageLevelNode)RdfNode.findByNodeId(voltageLevelId.getId(), model);
				writer.println("\tsubgraph \"cluster_" + substation.getName() + "-" + voltageLevel.getName() + "\" {");
				writer.println("\t\tlabel=\"Voltage Level " + voltageLevel.getName() + "\"; graph[style=dotted];");
				List<Rdf> voltageLevelEquipment = Rdf.findRdfByContainer(voltageLevel.getId()); 
				if ( voltageLevelEquipment != null) {
					for (Rdf rdf : voltageLevelEquipment) {
						RdfNode node = RdfNode.findByNodeId(rdf.getId(), model);
						if (!node.isVisited()) {
							writer.println(node.toDiagram());
							node.setVisited(true);
							equipmentModelState.put(node.getId(), true);
						}
						if (ConductingEquipmentNode.class.isInstance(node)) {
							ConductingEquipmentNode conductingEquipment = (ConductingEquipmentNode)node;
							for (TerminalNode terminal : conductingEquipment.getTerminals()) {
								if (!terminals.containsKey(terminal.getId())) terminals.put(terminal.getId(), terminal);
							}
							if (TransformerWindingNode.class.isInstance(node)) {
								unmodeledTransformerWindings.remove(node.getId());
							} else {
								nonTransformerWindingEquipment = true;
							}
						} else if (ConnectivityElementNode.class.isInstance(node)) {
							ConnectivityElementNode connectivityNode = (ConnectivityElementNode)node;
							for (TerminalNode terminal : connectivityNode.getTerminals()) {
								if (!terminals.containsKey(terminal.getId())) terminals.put(terminal.getId(), terminal);
								ConductingEquipmentNode equipment = terminal.getConductingEquipment();
								if (equipment != null ) {
									if (!equipmentModelState.containsKey(equipment.getId())) equipmentModelState.put(equipment.getId(), false);
									if (TransformerWindingNode.class.isInstance(equipment)) {
										TransformerWindingNode transformerWinding = (TransformerWindingNode)equipment;
										PowerTransformerNode transformer = transformerWinding.getPowerTransformer();
										if (transformer != null && !transformers.containsKey(transformer.getId())) transformers.put(transformer.getId(), transformer);
										if (!transformerWindings.containsKey(transformerWinding.getId())) transformerWindings.put(transformerWinding.getId(), transformerWinding);
										if (!unmodeledTransformerWindings.containsKey(transformerWinding.getId())) unmodeledTransformerWindings.put(transformerWinding.getId(), transformerWinding);
									} else if (ACLineSegmentNode.class.isInstance(equipment)) {
										if (!acLineSegments.containsKey(equipment.getId())) acLineSegments.put(equipment.getId(), (ACLineSegmentNode)equipment);
									}
								}
							}
						} else if (BayNode.class.isInstance(node)) {
							List<Rdf> bayEquipment = Equipment.findRdfByContainer(node.getId()); 
							if ( bayEquipment != null) {
								for (Rdf bayRdf : bayEquipment) {
									ConductingEquipmentNode bayNode = (ConductingEquipmentNode)RdfNode.findByNodeId(bayRdf.getId(), model);
									if (!bayNode.isVisited()) {
										writer.println(bayNode.toDiagram());
										bayNode.setVisited(true);
										equipmentModelState.put(bayNode.getId(), true);
									}
									for (TerminalNode terminal : bayNode.getTerminals()) {
										if (!terminals.containsKey(terminal.getId())) terminals.put(terminal.getId(), terminal);
									}
									nonTransformerWindingEquipment = true;
								}
							}
						}
					}
					if (nonTransformerWindingEquipment && transformerWindings.size() > 0) {
						writer.println("");
						for (String transformerWindingId : transformerWindings.keySet()) {
							TransformerWindingNode transformerWinding = transformerWindings.get(transformerWindingId);
							unmodeledTransformerWindings.remove(transformerWinding.getId());
							if (!transformerWinding.isVisited()) {
								writer.println(transformerWinding.toDiagram());
								transformerWinding.setVisited(true);
								equipmentModelState.put(transformerWinding.getId(), true);
							}
						}
					}
				}
				writer.println("\t}");
			}
		}
		if (transformers.size() > 0) {
			writer.println("");
			writer.println("\tsubgraph \"" + substation.getName() + "-transformers\" {");
			for (String transformerId : transformers.keySet()) {
				PowerTransformerNode transformer = transformers.get(transformerId);
				if (!transformer.isVisited()) {
					// don't set visited on transformers here... that happens at the end
					writer.println(transformer.toDiagram());
					equipmentModelState.put(transformer.getId(), true);
				}
			}
			writer.println("\t}");
		}
		if (acLineSegments.size() > 0) {
			writer.println("");
			writer.println("\tsubgraph \"" + substation.getName() + "-lines\" {");
			for (String acLineSegmentId : acLineSegments.keySet()) {
				ACLineSegmentNode acLineSegment = acLineSegments.get(acLineSegmentId);
				if (!acLineSegment.isVisited()) {
					writer.println(acLineSegment.toDiagram());
					acLineSegment.setVisited(true);
					equipmentModelState.put(acLineSegment.getId(), true);
				}
				result.add(acLineSegment);
			}
			writer.println("\t}");
		}
		if (unmodeledTransformerWindings.size() > 0) {
			writer.println("");
			writer.println("\tsubgraph \"" + substation.getName() + "-unmodeledTransformerWindings\" {");
			for (String transformerWindingId : unmodeledTransformerWindings.keySet()) {
				TransformerWindingNode transformerWinding = unmodeledTransformerWindings.get(transformerWindingId);
				if (!transformerWinding.isVisited()) {
					writer.println(transformerWinding.toDiagram());
					transformerWinding.setVisited(true);
					equipmentModelState.put(transformerWinding.getId(), true);
				}
			}
			writer.println("\t}");
		}
		boolean unmodeledEquipment = false;
		log.debug("... check for unmodeled equipment...");
		for (String equipmentId : equipmentModelState.keySet()) {
			boolean modeled = equipmentModelState.get(equipmentId).booleanValue();
			if (!modeled) {
				if (!unmodeledEquipment) {
					unmodeledEquipment = true;
					writer.println("");
					writer.println("\tsubgraph \"unmodeledEquipment\" {");
				}
				RdfNode node = RdfNode.findByNodeId(equipmentId, model);
				if (node != null) {
					writer.println(node.toDiagram());
				}
			}
		}
		if (unmodeledEquipment) writer.println("\t}");
		if(terminals.size() > 0) {
			writer.println("");
			for (String terminalId : terminals.keySet()) {
				TerminalNode terminal = terminals.get(terminalId);
				if (!terminal.isVisited()) {
					writer.println(terminal.toDiagram());
					terminal.setVisited(true);
				}
			}
		}
		if (transformers.size() > 0) {
			writer.println("");
			for (String transformerId : transformers.keySet()) {
				PowerTransformerNode transformer = transformers.get(transformerId);
				if (!transformer.isVisited()) {
					for (TransformerWindingNode transformerWinding : transformer.getTransformerWindings()) {
						writer.println("\t\"" + transformer.getDiagramName() + "\" -- \"" + transformerWinding.getDiagramName() + "\";");
					}
					transformer.setVisited(true);
				}
			}
		}
		if (subgraph) writer.println("# }");
		else writer.println("}");
		
		return result;
	}

	private static void diagramLine(String lineName, String fileName, int model) {
		Line lineId = Line.findByName(lineName);
		if (lineId != null) {
			try {
				PrintWriter writer = new PrintWriter(new FileWriter(fileName));
				LineNode line = (LineNode)RdfNode.findByNodeId(lineId.getId(), model);
				diagramLine(line, writer, model);
				writer.close();
			} catch (IOException e) {
				log.info("Unable to open file: " + fileName);
			}
		}
	}

	private static void diagramLine(LineNode line, PrintWriter writer, int model) {
		writer.println("graph \"" + line.getName() + "\" {");
		Map<String,SubstationNode> substations = RdfGraph.findLineSubstations(line, model, true);
		// this needs to remain a reset() for the time being
		// TODO: improve resetFrom() to account for EquipmentContainers as well as mapping through Terminals
		RdfGraph.reset(model, true, false, false, false, false);
		Map<String,ACLineSegmentNode> lineElements = line.getLineSegments();
		for (String acLineSegmentId : lineElements.keySet()) {
			ACLineSegmentNode acLineSegment = lineElements.get(acLineSegmentId);
			acLineSegment.setDiagramNodeOfInterest(true);
		}
		Map<String,ACLineSegmentNode> modeledACLineSegments = new HashMap<String,ACLineSegmentNode>();
		for (String substationId : substations.keySet()) {
			SubstationNode substation = substations.get(substationId);
			List<ACLineSegmentNode> diagrammedACLineSegments = diagramSubstation(substation, writer, model,true);
			for (ACLineSegmentNode acLineSegment : diagrammedACLineSegments) {
				if (lineElements.containsKey(acLineSegment.getId()) && !modeledACLineSegments.containsKey(acLineSegment)) modeledACLineSegments.put(acLineSegment.getId(), acLineSegment);
			}
		}
		for (String acLineSegmentId : lineElements.keySet()) {
			ACLineSegmentNode acLineSegment = lineElements.get(acLineSegmentId);
			acLineSegment.setDiagramNodeOfInterest(false);
		}
		writer.println("}");
	}
	
	private static void analizeCim() {
		// log basic expectations
		log.info("Total RDF elements: " + Rdf.getAllIds().size());
		// create all the nodes
		Set<String> rdfIds = Rdf.getAllIds();
		log.info("Establish " + rdfIds.size() + " graph nodes: " + formatter.format(new Date()));
		int model = RdfGraph.initialize();
		int connectedEquipment = 0;
		int graphBays = 0;
		int graphTerminals = 0;
		int graphConnectivityNodes = 0;
		int graphPowerTransformers = 0;
		int graphConductingEquipment = 0;
		int graphSingleTerminalEquipment = 0;
		int graphDualTerminalEquipment = 0;
		for (String rdfId : rdfIds) {
			RdfNode node = RdfNode.findByNodeId(rdfId,model);
			if (node.isConnectedElement()) connectedEquipment++;
			if (TerminalNode.class.isInstance(node)) graphTerminals++;
			if (ConnectivityElementNode.class.isInstance(node)) graphConnectivityNodes++;
			if (PowerTransformerNode.class.isInstance(node)) graphPowerTransformers++;
			if (ConductingEquipmentNode.class.isInstance(node)) graphConductingEquipment++;
			if (SingleTerminalEquipment.class.isInstance(node)) graphSingleTerminalEquipment++;
			if (DualTerminalEquipment.class.isInstance(node)) graphDualTerminalEquipment++;
			if (BayNode.class.isInstance(node)) graphBays++;
		}
		log.info("Connectable equipment in graph: " + connectedEquipment);
		log.info("Terminals in graph: " + graphTerminals);
		log.info("ConnectivityNodes in graph: " + graphConnectivityNodes);
		log.info("PowerTransformers in graph: " + graphPowerTransformers);
		log.info("ConductingEquipment in graph: " + graphConductingEquipment);
		log.info("Bays in graph: " + graphBays);
		log.info("SingleTerminalEquipment in graph: " + graphSingleTerminalEquipment);
		log.info("DualTerminalEqupment in graph: " + graphDualTerminalEquipment);
		// inspect bay content
		Set<String> bayEquipmentTypes = new HashSet<String>();
		for (String containerEquipmentId : Rdf.getContainerIds()) {
			RdfNode node = RdfNode.findByNodeId(containerEquipmentId, model);
			if (BayNode.class.isInstance(node)) {
				for (Rdf rdfId : Rdf.findRdfByContainer(node.getId())) {
					RdfNode bayNode = RdfNode.findByNodeId(rdfId.getId(), model);
					bayEquipmentTypes.add(bayNode.getDisplayName());
				}
			}
		}
		log.info("");
		log.info("List of equipment types contained in Bays");
		log.info("-----------------------------------------");
		for (String bayEquipmentType : bayEquipmentTypes) {
			log.info(bayEquipmentType);
		}
		log.info("");
		// establish the graph
		Set<String> terminalNodeIds = TerminalNode.getTerminalNodeIds(model);
		log.info("Connecting " + terminalNodeIds.size() + " terminals: " + formatter.format(new Date()));
		RdfGraph.connect(model);
		// check for unconnected elements
		log.info("Check for disconnected elements: " + formatter.format(new Date()));
		int connectedElements = 0;
		int disconnectedElements = 0;
		int singleTerminalConnectivityNodes = 0;
		int nullTerminalConnectivityNodes = 0;
		int nullTerminalConductingEquipment = 0;
		for (String rdfId : rdfIds) {
			RdfNode node = RdfNode.findByNodeId(rdfId, model);
			if (ConnectivityElementNode.class.isInstance(node)) {
				ConnectivityElementNode connectivityNode = (ConnectivityElementNode)node;
				if (connectivityNode.getTerminals().size() < 2) singleTerminalConnectivityNodes++;
			}
			if (TerminalNode.class.isInstance(node)) {
				TerminalNode terminal = (TerminalNode)node;
				if (terminal.getConnectivityNode() == null) nullTerminalConnectivityNodes++;
				if (terminal.getConductingEquipment() == null) nullTerminalConductingEquipment++;
			}
			if (node.isConnectedElement()) {
				if (!node.isConnected()) {
					disconnectedElements++;
					log.info("Unconnected RdfNode " + rdfId + " type " + node.getClass().getName());
				} else {
					connectedElements++;
				}
			}
		}
		log.info("Identified " + connectedElements + " connected elements");
		log.info("Identified " + disconnectedElements + " disconnected elements");
		log.info("Identified " + singleTerminalConnectivityNodes + " single terminal ConnectivityNodes");
		log.info("Identified " + nullTerminalConnectivityNodes + " terminals missing ConnectivityNode connections");
		log.info("Identified " + nullTerminalConductingEquipment + " Terminals missing ConductingEquipment connections");
		// report substation equipment
		for (String substationName : reportSubstationEquipment) {
			listSubstationEquipment(substationName, model);
		}
		// count two and three winding transformers
		int powerTransformers = 0;
		int twoWindingTransformers = 0;
		int threeWindingTransformers = 0;
		Set<String> powerTransformerIds = PowerTransformer.getPowerTransformerIds();
		for (String powerTransformerId : powerTransformerIds) {
			PowerTransformerNode powerTransformer = (PowerTransformerNode)RdfNode.findByNodeId(powerTransformerId, model);
			powerTransformers++;
			if (powerTransformer.getTransformerWindings().size() == 2) twoWindingTransformers++;
			if (powerTransformer.getTransformerWindings().size() == 3) threeWindingTransformers++;
		}
		log.info("Identified " + powerTransformers + " PowerTransformers");
		log.info("... " + twoWindingTransformers + " of which have two TransformerWindings");
		log.info("... " + threeWindingTransformers + " of which have three TransformerWindings");
		// create a second model
		log.info("Establish second model: " + formatter.format(new Date()));
		int secondModel = RdfGraph.initialize();
		// establish the graph
		log.info("Establish second graph: " + formatter.format(new Date()));
		RdfGraph.connect(secondModel);
		// detect islands
		if (reportIslandEquipment || reportIslandEquipmentDetail) {
			Date startTime = new Date();
			int islands = RdfGraph.mapIslands(model, true);
			Date endTime = new Date();
			long duration = endTime.getTime() - startTime.getTime();
			log.info("Identified " + islands + " island(s) in " + duration + " milliseconds: " + formatter.format(endTime));
			for (int currentIsland = 0 ; currentIsland <= islands  && currentIsland < 6 ; currentIsland++) {
				if (reportIslandEquipment && currentIsland > 1) {
					String sectionTitle = "Equipment in Island " + currentIsland;
					log.info(sectionTitle);
					log.info(StringUtils.repeat('-', sectionTitle.length()));
				}
				int islandCount = 0;
				for(String rdfId : rdfIds) {
					RdfNode node = RdfNode.findByNodeId(rdfId, model);
					if (node.isConnectedElement() && node.getIsland() == currentIsland) {
						islandCount++;
						if (reportIslandEquipment && currentIsland > 1) log.info(node.toString());
					}
				}
				log.info("Island " + currentIsland + " has " + islandCount + " elements");
			}
			// now consider switch state
			log.info("Check island count considering switch state: " + formatter.format(new Date()));
			// detect islands
			int secondIslands = RdfGraph.mapIslands(secondModel, false);
			log.info("Identified " + secondIslands + " island(s):" + formatter.format(new Date()));
			for (int currentIsland = 0 ; currentIsland <= secondIslands  && currentIsland < 6 ; currentIsland++) {
				if (reportIslandEquipment && currentIsland > 1) {
					String sectionTitle = "Equipment in Island " + currentIsland;
					log.info(sectionTitle);
					log.info(StringUtils.repeat('-', sectionTitle.length()));
				}
				int islandCount = 0;
				for(String rdfId : rdfIds) {
					RdfNode node = RdfNode.findByNodeId(rdfId, secondModel);
					if (node.isConnectedElement() && node.getIsland() == currentIsland) {
						islandCount++;
						if (reportIslandEquipment && currentIsland > 1) log.info(node.toString());
					}
				}
				log.info("Island " + currentIsland + " has " + islandCount + " elements");
			}
			// now consider switch state
			log.info("Check island count after setting everything as outaged: " + formatter.format(new Date()));
			// reset islands and open all switches
			RdfGraph.reset(secondModel, true, true, false, false, true);
			// detect islands again
			secondIslands = RdfGraph.mapIslands(secondModel, false);
			log.info("Identified " + secondIslands + " island(s):" + formatter.format(new Date()));
		}
		if (reportRadialLines || reportRadialLinesDetail) {
			// reset model to default state
			RdfGraph.reset(secondModel, true, true, true, true, false);
			// list COMAN_TN equipment
			RdfNode oragin = RdfNode.findByNodeId("ED7137C0-C1FB-446B-94DE-2121A79FAD65", secondModel);
			RdfGraph.listEquipment(oragin,new HashSet<String>(Arrays.asList(new String[]{"FDB1F840-FAA6-4931-9F4D-12B5C8B3C9DA","C1E18FEA-0597-475D-A174-586684F3FC60","EB2D94B5-1A46-4085-881F-BD0B7DE47E19"})),true);
			// detect radial line against line broken by COMAN_TN outage on 11/1/2014
			RdfGraph.reset(secondModel, true, true, true, true, false);
			log.info("Check radial nature of line disconnected by COMAN_TN outage on 11/1/2014 without outages applied id: B0256D25-DFCB-4B37-A571-86DA69C4A3A0");
			ACLineSegmentNode testSegment = (ACLineSegmentNode)RdfNode.findByNodeId("B0256D25-DFCB-4B37-A571-86DA69C4A3A0", secondModel);
			List<RdfNode> shortestPath = RdfGraph.findShortestPath(testSegment.getTerminals().get(0), testSegment.getTerminals().get(1), testSegment, false, true);
			if (shortestPath == null) {
				log.info("ACLineSegment " + testSegment.getId() + " came up radial even without outages");
			} else {
				log.info("ACLineSegment terminals connected by path of " + shortestPath.size() + " elements");
				for (RdfNode node : shortestPath) {
					log.trace(node.toIdentifier());
				}
			}
			// detect radial lines
			Map<String,Map<String,OutageResourceNode>> radialEquipment = analizeRadialEquipment(secondModel, false);
			// break the comanche breakers and detect radial lines again
			log.info("Open COMAN_TN switches associated with 11/1/2014 outage");
			RdfGraph.setOutages(secondModel,new HashSet<String>(Arrays.asList(new String[]{"ED7137C0-C1FB-446B-94DE-2121A79FAD65","69193EB7-ABB8-4AF6-9A39-5C69B9CFF638","804DA9FD-93D4-42BF-A4BA-316D9B4AD42C","8EFC2791-0EAA-47DD-A5D0-3C66F1D90CC9","1272651E-7E92-40C7-9037-4CFB78D4042B","9A13D544-0D66-43BD-9ADD-E1DBA08458AF","E0B77D53-B9E3-4805-A348-4768C8147408","2CC4BD92-2CB5-44C1-92C8-9CBC1A7E371E"})));
			DisconnectorNode disconnector = (DisconnectorNode)RdfNode.findByNodeId("9A13D544-0D66-43BD-9ADD-E1DBA08458AF", secondModel);
			if (!disconnector.isOpen()) {
				log.info("Failed to open disconnector " + disconnector.getName() + " ID " + disconnector.getId());
				disconnector.setOpen(true);
			}
			testSegment = (ACLineSegmentNode)RdfNode.findByNodeId("B0256D25-DFCB-4B37-A571-86DA69C4A3A0", secondModel);
			shortestPath = RdfGraph.findShortestPath(testSegment.getTerminals().get(0), testSegment.getTerminals().get(1), testSegment, false, true);
			if (shortestPath == null) {
				log.info("ACLineSegment " + testSegment.getId() + " came up radial");
			} else {
				log.info("ACLineSegment terminals connected by path of " + shortestPath.size() + " elements");
				for (RdfNode node : shortestPath) {
					log.trace(node.toIdentifier());
				}
			}
			// detect radial lines again
			Map<String,Map<String,OutageResourceNode>> originalRadialEquipment = radialEquipment;
			radialEquipment = analizeRadialEquipment(secondModel, false);
			Map<String,OutageResourceNode> workingEquipment = radialEquipment.get(ACLineSegmentNode.getNodeName());
			for (String equipmentId : originalRadialEquipment.get(ACLineSegmentNode.getNodeName()).keySet()) {
				workingEquipment.remove(equipmentId);
			}
			workingEquipment = radialEquipment.get(LineNode.getNodeName());
			for (String equipmentId : originalRadialEquipment.get(LineNode.getNodeName()).keySet()) {
				workingEquipment.remove(equipmentId);
			}
			String sectionTitle = "" + radialEquipment.get(ACLineSegmentNode.getNodeName()).size() + " additional radial ACLineSegments in " + radialEquipment.get(LineNode.getNodeName()).size() + " additional radial Lines detected";
			log.info(sectionTitle);
			log.info(StringUtils.repeat('-', sectionTitle.length()));
			workingEquipment = radialEquipment.get(ACLineSegmentNode.getNodeName());
			for (String equipmentId : workingEquipment.keySet()) {
				log.info(workingEquipment.get(equipmentId).toIdentifier());
			}
			log.info(StringUtils.repeat('-', sectionTitle.length()));
			workingEquipment = radialEquipment.get(LineNode.getNodeName());
			for (String equipmentId : workingEquipment.keySet()) {
				log.info(workingEquipment.get(equipmentId).toIdentifier());
			}
		}
		// report disconnected lines
		if (reportDisconnectedEquipment || reportDisconnectedEquipmentDetail) {
			// reset the model to the default state
			RdfGraph.reset(secondModel, true, true, true, true, false);
			log.info("Check disconnected state of line disconnected by COMAN_TN outage on 11/1/2014 without outages applied id: B0256D25-DFCB-4B37-A571-86DA69C4A3A0");
			ACLineSegmentNode testSegment = (ACLineSegmentNode)RdfNode.findByNodeId("B0256D25-DFCB-4B37-A571-86DA69C4A3A0", secondModel);
			RdfGraph.SourceSinkRecord sourceSink1 = RdfGraph.findSourceAndSink(testSegment.getTerminals().get(0),  testSegment, false, true);
			RdfGraph.SourceSinkRecord sourceSink2 = RdfGraph.findSourceAndSink(testSegment.getTerminals().get(1),  testSegment, false, true);
			if ((sourceSink1.hasSource() && sourceSink2.hasSink()) || (sourceSink1.hasSink() && sourceSink2.hasSource())) {
				log.info("ACLineSegment " + testSegment.getId() + " is energized");
			} else {
				log.info("ACLineSegment " + testSegment.getId() + " came up disconnected even without outages");
			}
			// detect disconnected lines
			Map<String,Map<String,OutageResourceNode>> disconnectedEquipment = analizeDisconnectedEquipment(secondModel, false);
			// break the comanche breakers and detect disconnected lines again
			log.info("Open COMAN_TN switches associated with 11/1/2014 outage");
			RdfGraph.setOutages(secondModel,new HashSet<String>(Arrays.asList(new String[]{"ED7137C0-C1FB-446B-94DE-2121A79FAD65","69193EB7-ABB8-4AF6-9A39-5C69B9CFF638","804DA9FD-93D4-42BF-A4BA-316D9B4AD42C","8EFC2791-0EAA-47DD-A5D0-3C66F1D90CC9","1272651E-7E92-40C7-9037-4CFB78D4042B","9A13D544-0D66-43BD-9ADD-E1DBA08458AF","E0B77D53-B9E3-4805-A348-4768C8147408","2CC4BD92-2CB5-44C1-92C8-9CBC1A7E371E"})));
			DisconnectorNode disconnector = (DisconnectorNode)RdfNode.findByNodeId("9A13D544-0D66-43BD-9ADD-E1DBA08458AF", secondModel);
			if (!disconnector.isOpen()) {
				log.info("Failed to open disconnector " + disconnector.getName() + " ID " + disconnector.getId());
				disconnector.setOpen(true);
			}
			testSegment = (ACLineSegmentNode)RdfNode.findByNodeId("B0256D25-DFCB-4B37-A571-86DA69C4A3A0", secondModel);
			sourceSink1 = RdfGraph.findSourceAndSink(testSegment.getTerminals().get(0),  testSegment, false, true);
			sourceSink2 = RdfGraph.findSourceAndSink(testSegment.getTerminals().get(1),  testSegment, false, true);
			if ((sourceSink1.hasSource() && sourceSink2.hasSink()) || (sourceSink1.hasSink() && sourceSink2.hasSource())) {
				log.info("ACLineSegment " + testSegment.getId() + " is still energized");
			} else {
				log.info("ACLineSegment " + testSegment.getId() + " came up disconnected");
			}
			// detect disconnected lines again
			Map<String,Map<String,OutageResourceNode>> originalDisconnectedEquipment = disconnectedEquipment;
			disconnectedEquipment = analizeDisconnectedEquipment(secondModel, false);
			Map<String,OutageResourceNode> workingEquipment = disconnectedEquipment.get(LineNode.getNodeName());
			for (String equipmentId : originalDisconnectedEquipment.get(LineNode.getNodeName()).keySet()) {
				workingEquipment.remove(equipmentId);
			}
			workingEquipment = disconnectedEquipment.get(ACLineSegmentNode.getNodeName());
			for (String equipmentId : originalDisconnectedEquipment.get(ACLineSegmentNode.getNodeName()).keySet()) {
				workingEquipment.remove(equipmentId);
			}
			workingEquipment = disconnectedEquipment.get(PowerTransformerNode.getNodeName());
			for (String equipmentId : originalDisconnectedEquipment.get(PowerTransformerNode.getNodeName()).keySet()) {
				workingEquipment.remove(equipmentId);
			}
			String sectionTitle = "" + disconnectedEquipment.get(ACLineSegmentNode.getNodeName()).size() + " additional disconnected ACLineSegments in " + disconnectedEquipment.get(LineNode.getNodeName()).size() + " additional disconnected Lines detected";
			log.info(sectionTitle);
			log.info(StringUtils.repeat('-', sectionTitle.length()));
			workingEquipment = disconnectedEquipment.get(ACLineSegmentNode.getNodeName());
			for (String equipmentId : workingEquipment.keySet()) {
				log.info(workingEquipment.get(equipmentId).toIdentifier());
			}
			log.info(StringUtils.repeat('-', sectionTitle.length()));
			workingEquipment = disconnectedEquipment.get(LineNode.getNodeName());
			for (String equipmentId : workingEquipment.keySet()) {
				log.info(workingEquipment.get(equipmentId).toIdentifier());
			}
			sectionTitle = "" + disconnectedEquipment.get(PowerTransformerNode.getNodeName()).size() + " additional disconnected PowerTransformers detected";
			log.info(sectionTitle);
			log.info(StringUtils.repeat('-', sectionTitle.length()));
			workingEquipment = disconnectedEquipment.get(PowerTransformerNode.getNodeName());
			for (String equipmentId : workingEquipment.keySet()) {
				log.info(workingEquipment.get(equipmentId).toIdentifier());
			}
		}
		// wrap it up
		log.info("Finished processing CIM: " + formatter.format(new Date()));
	}
	
	private static Map<String,Map<String,OutageResourceNode>> analizeRadialEquipment(int model, boolean assumeConnected) {
		Map<String,Map<String,OutageResourceNode>> result = new HashMap<String,Map<String,OutageResourceNode>>();
		result.put(ACLineSegmentNode.getNodeName(), new HashMap<String,OutageResourceNode>());
		result.put(LineNode.getNodeName(), new HashMap<String,OutageResourceNode>());
		
		int acLineSegments = 0;
		Date startTime = new Date();
		log.info("Detect radial ACLineSegments: " + formatter.format(startTime));
		for (String rdfId : Rdf.getAllIds()) {
			RdfNode node = RdfNode.findByNodeId(rdfId, model);
			if (ACLineSegmentNode.class.isInstance(node)) {
				acLineSegments++;
				if ((acLineSegments % 250) == 0L) {
					log.trace("Processed " + acLineSegments + " elements... " + formatter.format(new Date()));
				}
				ACLineSegmentNode acLineSegment = (ACLineSegmentNode)node;
				// ignore ACLineSegment that is part of an already identified radial line
				if (!result.containsKey(acLineSegment.getEquipmentContainer().getId())) {
					List<TerminalNode> terminals = acLineSegment.getTerminals();
					if (terminals.size() != 2) {
						log.debug("ACLineSegment " + acLineSegment.getId() + " has " + terminals.size() + " connected terminal(s)");
					} else if (RdfGraph.findShortestPath(terminals.get(0), terminals.get(1), acLineSegment, assumeConnected, true) == null) {
						if (reportRadialLinesDetail) log.info("ACLineSegment " + acLineSegment.getId() + " is part of a radial line");
						result.get(ACLineSegmentNode.getNodeName()).put(acLineSegment.getId(), acLineSegment);
						LineNode line = (LineNode)acLineSegment.getEquipmentContainer();
						if (!result.get(LineNode.getNodeName()).containsKey(line.getId())) result.get(LineNode.getNodeName()).put(line.getId(), line);
					}
				} else {
					// the ACLineSegment is part of a radial line and is therefore radial itself
					result.get(ACLineSegmentNode.getNodeName()).put(acLineSegment.getId(), acLineSegment);
				}
			}
		}
		Date endTime = new Date();
		long duration = endTime.getTime() - startTime.getTime();
		log.info("Processed " + acLineSegments + " ACLineSegment nodes in " + duration + " milliseconds");
		log.info("Detected " + result.get(ACLineSegmentNode.getNodeName()).size() + " radial ACLineSegments in " + result.get(LineNode.getNodeName()).size() + " lines");
		log.info("Done radial ACLineSegments: " + formatter.format(endTime));
		
		return result;
	}
	
	private static Map<String,Map<String,OutageResourceNode>> analizeDisconnectedEquipment(int model, boolean assumeConnected) {
		Map<String,Map<String,OutageResourceNode>> result = new HashMap<String,Map<String,OutageResourceNode>>();
		result.put(ACLineSegmentNode.getNodeName(), new HashMap<String,OutageResourceNode>());
		result.put(LineNode.getNodeName(), new HashMap<String,OutageResourceNode>());
		result.put(PowerTransformerNode.getNodeName(), new HashMap<String,OutageResourceNode>());
		
		int acLineSegments = 0;
		int powerTransformers = 0;
		Date startTime = new Date();
		log.info("Detect disconnected ACLineSegments and PowerTransformers: " + formatter.format(startTime));
		for (String rdfId : Rdf.getAllIds()) {
			RdfNode node = RdfNode.findByNodeId(rdfId, model);
			if (ACLineSegmentNode.class.isInstance(node)) {
				acLineSegments++;
				if (((acLineSegments + powerTransformers) % 250) == 0L) {
					log.trace("Processed " + acLineSegments + " ACLineSegment and " + powerTransformers + " PowerTransformer elements... " + formatter.format(new Date()));
				}
				ACLineSegmentNode acLineSegment = (ACLineSegmentNode)node;
				List<TerminalNode> terminals = acLineSegment.getTerminals();
				if (terminals.size() != 2) {
					log.debug("ACLineSegment " + acLineSegment.getId() + " has " + terminals.size() + " connected terminal(s)");
				} else {
					boolean disconnected = true;
					RdfGraph.SourceSinkRecord sourceSinkA = RdfGraph.findSourceAndSink(terminals.get(0),  acLineSegment, false, true);
					RdfGraph.SourceSinkRecord sourceSinkB = null;
					if (sourceSinkA.hasSource() || sourceSinkA.hasSink()) {
						sourceSinkB = RdfGraph.findSourceAndSink(terminals.get(1),  acLineSegment, false, true);
						if ((sourceSinkA.hasSource() && sourceSinkB.hasSink()) || (sourceSinkA.hasSink() && sourceSinkB.hasSource())) {
							disconnected = false;
						}
					}
					if (disconnected) {
						if (reportDisconnectedEquipmentDetail) log.info("ACLineSegment " + acLineSegment.getId() + " is disconnected");
						result.get(ACLineSegmentNode.getNodeName()).put(acLineSegment.getId(), acLineSegment);
						LineNode line = (LineNode)acLineSegment.getEquipmentContainer();
						if (!result.get(LineNode.getNodeName()).containsKey(line.getId())) result.get(LineNode.getNodeName()).put(line.getId(), line);
					}
				}
			} else if (PowerTransformerNode.class.isInstance(node)) {
				powerTransformers++;
				if (((acLineSegments + powerTransformers) % 250) == 0L) {
					log.trace("Processed " + acLineSegments + " ACLineSegment and " + powerTransformers + " PowerTransformer elements... " + formatter.format(new Date()));
				}
				PowerTransformerNode powerTransformer = (PowerTransformerNode)node;
				List<TransformerWindingNode> transformerWindings = powerTransformer.getTransformerWindings();
				boolean disconnected = true;
				RdfGraph.SourceSinkRecord sourceAndSink = new RdfGraph.SourceSinkRecord();
				for (TransformerWindingNode transformerWinding : transformerWindings) {
					if (transformerWinding.getTerminals().size() == 1) {
						RdfGraph.SourceSinkRecord transformwerWindingSourceSink = RdfGraph.findSourceAndSink(transformerWinding.getTerminals().iterator().next(), transformerWinding, assumeConnected, true);
						if ((sourceAndSink.hasSource() && transformwerWindingSourceSink.hasSink()) || (sourceAndSink.hasSink() && transformwerWindingSourceSink.hasSource())) {
							disconnected = false;
							break;
						} else {
							if (transformwerWindingSourceSink.hasSource()) sourceAndSink.setSource(transformwerWindingSourceSink.getSource());
							if (transformwerWindingSourceSink.hasSink()) sourceAndSink.setSink(transformwerWindingSourceSink.getSink());
						}
					}
				}
				if (disconnected) {
					if (reportDisconnectedEquipmentDetail) log.info("PowerTransformer ID " + powerTransformer.getId() + " named " + powerTransformer.getName() + " in Substation " + powerTransformer.getSubstation().getName() + " is disconnected");
					result.get(PowerTransformerNode.getNodeName()).put(powerTransformer.getId(), powerTransformer);
				}
			}
		}
		Date endTime = new Date();
		long duration = endTime.getTime() - startTime.getTime();
		log.info("Processed " + acLineSegments + " ACLineSegment and " + powerTransformers + " PowerTransformer nodes in " + duration + " milliseconds");
		log.info("Detected " + result.get(ACLineSegmentNode.getNodeName()).size() + " disconnected ACLineSegments in " + result.get(LineNode.getNodeName()).size() + " lines");
		log.info("Detected " + result.get(PowerTransformerNode.getNodeName()).size() + " disconnected PowerTransformers");
		log.info("Done detecting disconnected ACLineSegments and PowerTransformers: " + formatter.format(endTime));

		return result;
	}
	
	private static void analizeOutages() {
		try {
			List<Outage> selectedOutages = new LinkedList<Outage>();
			Date target = formatter.parse(outageDate);
			GregorianCalendar calendar = new GregorianCalendar();
			// TODO: Determine if the timezone is required
			//calendar.setTimeZone(TimeZone.getTimeZone("America/Chigago"));
			calendar.setTime(target);
			for (Outage outage : outages) {
				if (outage.getPlannedStartDate().toGregorianCalendar().compareTo(calendar)<= 0 && calendar.compareTo(outage.getPlannedEndDate().toGregorianCalendar())<= 0 && outage.getEquipmentFromStationName().equals("COMAN_TN")) {
					selectedOutages.add(outage);
				}
			}
			if (reportOutageAnalysisDetail) log.info("");
			String sectionTitle = "COMAN_TN outages during " + formatter.format(target) + " (" + selectedOutages.size() + ")";
			log.info(sectionTitle);
			if (reportOutageAnalysisDetail) {
				log.info(StringUtils.repeat('-', sectionTitle.length()));
				for (Outage outage : selectedOutages) {
					log.info(OutageModel.outageToString(outage));
				}
			}
			selectedOutages = new LinkedList<Outage>();
			for (Outage outage : outages) {
				if (outage.getPlannedStartDate().toGregorianCalendar().compareTo(calendar)<= 0 && calendar.compareTo(outage.getPlannedEndDate().toGregorianCalendar())<= 0) {
					selectedOutages.add(outage);
				}
			}
			if (reportOutageAnalysisDetail) {
				log.info("");
				sectionTitle = "All outages during " + formatter.format(target) + " (" + selectedOutages.size() + ")";
				log.info(sectionTitle);
				if (reportOutageAnalysisDetail) {
					log.info(StringUtils.repeat('-', sectionTitle.length()));
					for (Outage outage : selectedOutages) {
						log.info(OutageModel.outageToString(outage));
					}
				}
			}
			// if the CIM file has been processed, match outages to RdfNodes
			if (Rdf.getAllIds().size() > 0) {
				int examinedOutages = 0;
				int outagesInDateRange = 0;
				int matchedOutages = 0;
				int matchedLines = 0;
				int matchedACLineSegments = 0;
				int matchedTransformers = 0;
				int matchedLoads = 0;
				int matchedBreakers = 0;
				int matchedDisconnectors = 0;
				int matchedCompensators = 0;
				int matchedSeriesCompensators = 0;
				int unmatchedTeids = 0;
				int missingSubstation = 0;
				int indirectlyMatchedOutages = 0;
				int unexpectedResource = 0;
				int mixedOutageVsResource = 0;
				int terminalMapFailures = 0;
				int equipmentMapFailures = 0;
				
				log.info("");
				// try to apply outages
				log.info("Establish CIM model to compare outages to");
				int model = RdfGraph.initialize();
				RdfGraph.connect(model);
				if (reportOutageAnalysis) {
					// match all outages to model
					log.info("Match all outages");
					for (Outage outage : outages) {
						examinedOutages++;
						outagesInDateRange++;
						OutageModel.OutageMatch outageMatch = OutageModel.match(outage, model, reportOutageAnalysisDetail);
						OutageModel.MatchResult matchResult = outageMatch.getMatchResult();
						if (matchResult == MatchResult.MATCH || matchResult == MatchResult.INDIRECT_MATCH) {
							matchedOutages++;
							if (matchResult == MatchResult.INDIRECT_MATCH) {
								indirectlyMatchedOutages++;
							}
							if ("LN".equals(outage.getEquipmentType().value())) {
								matchedLines++;
								if (matchResult == MatchResult.INDIRECT_MATCH) {
									matchedACLineSegments++;
								}
							} else if ("XF".equals(outage.getEquipmentType().value())) {
								matchedTransformers++;
							} else if ("LD".equals(outage.getEquipmentType().value())) {
								matchedLoads++;
							} else if ("CB".equals(outage.getEquipmentType().value())) {
								matchedBreakers++;
							} else if ("DSC".equals(outage.getEquipmentType().value())) {
								matchedDisconnectors++;
							} else if ("SVC".equals(outage.getEquipmentType().value())) {
								matchedCompensators++;
							} else if ("SC".equals(outage.getEquipmentType().value())) {
								matchedSeriesCompensators++;
							}
						} else if (matchResult == MatchResult.UNMATCHED_TEID) {
							unmatchedTeids++;
						} else if (matchResult == MatchResult.MISSING_SUBSTATION) {
							missingSubstation++;
						} else if (matchResult == MatchResult.UNEXPECTED_EQUIPMENT_TYPE || matchResult == MatchResult.MAPFAILURE_UNEXPECTED_EQUPMENT_TYPE) {
							unexpectedResource++;
						} else if (matchResult == MatchResult.MAPFAILURE_TERMINAL) {
							terminalMapFailures++;
						} else if (matchResult == MatchResult.MAPFAILURE_EQUIPMENT) {
							equipmentMapFailures++;
						} else if (matchResult == MatchResult.MISMATCH) {
							mixedOutageVsResource++;
						}
					}
					log.info("Examined " + examinedOutages + " outages");
					log.info("" + outagesInDateRange + " outages matched dates");
					log.info("Matched " + matchedOutages + " outages to Rdf records");
					log.info("... Indirectly matched " + indirectlyMatchedOutages + " outages");
					log.info("... Matched "  + matchedLines + " Lines");
					log.info("...... Matched "  + matchedACLineSegments + " ACLineSegments");
					log.info("... Matched "  + matchedTransformers + " Transformers");
					log.info("... Matched "  + matchedLoads + " CustomerLoads");
					log.info("... Matched "  + matchedBreakers + " Breakers");
					log.info("... Matched "  + matchedDisconnectors + " Disconnectors");
					log.info("... Matched "  + matchedCompensators + " ShuntCompensators and StaticVarCompensators");
					log.info("... Matched "  + matchedSeriesCompensators + " SeriesCompensators");
					log.info("Failed to identify " + unmatchedTeids + " TEIDs from Outges to CIM");
					log.info("Failed to find substation on " + missingSubstation + " TEIDs");
					log.info("Failed to map terminal on " + terminalMapFailures + " indirect TEIDs");
					log.info("Failed to map equipment on " + equipmentMapFailures + " indirect TEIDs");
					log.info("Failed to match substation or equipoment name for " + mixedOutageVsResource + " TEIDs");
					log.info("Found "  + unexpectedResource + " unexpeccted resources mapping TEID from Outages to CIM");
					// match outages to model for a date range
					examinedOutages = 0;
					outagesInDateRange = 0;
					matchedOutages = 0;
					matchedLines = 0;
					matchedACLineSegments = 0;
					matchedTransformers = 0;
					matchedLoads = 0;
					matchedBreakers = 0;
					matchedDisconnectors = 0;
					matchedCompensators = 0;
					matchedSeriesCompensators = 0;
					unmatchedTeids = 0;
					missingSubstation = 0;
					indirectlyMatchedOutages = 0;
					unexpectedResource = 0;
					mixedOutageVsResource = 0;
					terminalMapFailures = 0;
					equipmentMapFailures = 0;
					GregorianCalendar beginDate = new GregorianCalendar();
					beginDate.setTime(formatter.parse("01-07-2014 00:00:00"));
					GregorianCalendar endDate = new GregorianCalendar();
					endDate.setTime(formatter.parse("01-21-2014 00:00:00"));
					log.info("");
					log.info("Match outages between " + formatter.format(beginDate.getTime()) + " and " + formatter.format(endDate.getTime()));
					for (Outage outage : outages) {
						examinedOutages++;
						if (outage.getPlannedStartDate().toGregorianCalendar().compareTo(endDate)<= 0 && beginDate.compareTo(outage.getPlannedEndDate().toGregorianCalendar())<= 0) {
							outagesInDateRange++;
							OutageModel.OutageMatch outageMatch = OutageModel.match(outage, model, reportOutageAnalysisDetail);
							OutageModel.MatchResult matchResult = outageMatch.getMatchResult();
							if (matchResult == MatchResult.MATCH || matchResult == MatchResult.INDIRECT_MATCH) {
								matchedOutages++;
								if (matchResult == MatchResult.INDIRECT_MATCH) {
									indirectlyMatchedOutages++;
								}
								if ("LN".equals(outage.getEquipmentType().value())) {
									matchedLines++;
									if (matchResult == MatchResult.INDIRECT_MATCH) {
										matchedACLineSegments++;
									}
								} else if ("XF".equals(outage.getEquipmentType().value())) {
									matchedTransformers++;
								} else if ("LD".equals(outage.getEquipmentType().value())) {
									matchedLoads++;
								} else if ("CB".equals(outage.getEquipmentType().value())) {
									matchedBreakers++;
								} else if ("DSC".equals(outage.getEquipmentType().value())) {
									matchedDisconnectors++;
								} else if ("SVC".equals(outage.getEquipmentType().value())) {
									matchedCompensators++;
								} else if ("SC".equals(outage.getEquipmentType().value())) {
									matchedSeriesCompensators++;
								}
							} else if (matchResult == MatchResult.UNMATCHED_TEID) {
								unmatchedTeids++;
							} else if (matchResult == MatchResult.MISSING_SUBSTATION) {
								missingSubstation++;
							} else if (matchResult == MatchResult.UNEXPECTED_EQUIPMENT_TYPE || matchResult == MatchResult.MAPFAILURE_UNEXPECTED_EQUPMENT_TYPE) {
								unexpectedResource++;
							} else if (matchResult == MatchResult.MAPFAILURE_TERMINAL) {
								terminalMapFailures++;
							} else if (matchResult == MatchResult.MAPFAILURE_EQUIPMENT) {
								equipmentMapFailures++;
							} else if (matchResult == MatchResult.MISMATCH) {
								mixedOutageVsResource++;
							}
						}
					}
					log.info("Examined " + examinedOutages + " outages");
					log.info("" + outagesInDateRange + " outages matched dates");
					log.info("Matched " + matchedOutages + " outages to Rdf records");
					log.info("... Indirectly matched " + indirectlyMatchedOutages + " outages");
					log.info("... Matched "  + matchedLines + " Lines");
					log.info("...... Matched "  + matchedACLineSegments + " ACLineSegments");
					log.info("... Matched "  + matchedTransformers + " Transformers");
					log.info("... Matched "  + matchedLoads + " CustomerLoads");
					log.info("... Matched "  + matchedBreakers + " Breakers");
					log.info("... Matched "  + matchedDisconnectors + " Disconnectors");
					log.info("... Matched "  + matchedCompensators + " ShuntCompensators and StaticVarCompensators");
					log.info("... Matched "  + matchedSeriesCompensators + " SeriesCompensators");
					log.info("Failed to identify " + unmatchedTeids + " TEIDs from Outges to CIM");
					log.info("Failed to find substation on " + missingSubstation + " TEIDs");
					log.info("Failed to map terminal on " + terminalMapFailures + " indirect TEIDs");
					log.info("Failed to map equipment on " + equipmentMapFailures + " indirect TEIDs");
					log.info("Failed to match substation or equipoment name for " + mixedOutageVsResource + " TEIDs");
					log.info("Found "  + unexpectedResource + " unexpeccted resources mapping TEID from Outages to CIM");
				}
				// match outages to model for outageDate
				examinedOutages = 0;
				outagesInDateRange = 0;
				matchedOutages = 0;
				matchedLines = 0;
				matchedACLineSegments = 0;
				matchedTransformers = 0;
				matchedLoads = 0;
				matchedBreakers = 0;
				matchedDisconnectors = 0;
				matchedCompensators = 0;
				matchedSeriesCompensators = 0;
				unmatchedTeids = 0;
				missingSubstation = 0;
				indirectlyMatchedOutages = 0;
				unexpectedResource = 0;
				mixedOutageVsResource = 0;
				terminalMapFailures = 0;
				equipmentMapFailures = 0;
				GregorianCalendar date = new GregorianCalendar();
				date.setTime(formatter.parse(outageDate));
				log.info("");
				log.info("Match outages for " + formatter.format(date.getTime()));
				for (Outage outage : outages) {
					examinedOutages++;
					if (outage.getPlannedStartDate().toGregorianCalendar().compareTo(date)<= 0 && date.compareTo(outage.getPlannedEndDate().toGregorianCalendar())<= 0) {
						outagesInDateRange++;
						OutageModel.OutageMatch outageMatch = OutageModel.match(outage, model, reportOutageAnalysisDetail);
						OutageModel.MatchResult matchResult = outageMatch.getMatchResult();
						if (matchResult == MatchResult.MATCH || matchResult == MatchResult.INDIRECT_MATCH) {
							matchedOutages++;
							if (matchResult == MatchResult.INDIRECT_MATCH) {
								indirectlyMatchedOutages++;
							}
							if ("LN".equals(outage.getEquipmentType().value())) {
								matchedLines++;
								if (matchResult == MatchResult.INDIRECT_MATCH) {
									matchedACLineSegments++;
								}
							} else if ("XF".equals(outage.getEquipmentType().value())) {
								matchedTransformers++;
							} else if ("LD".equals(outage.getEquipmentType().value())) {
								matchedLoads++;
							} else if ("CB".equals(outage.getEquipmentType().value())) {
								matchedBreakers++;
							} else if ("DSC".equals(outage.getEquipmentType().value())) {
								matchedDisconnectors++;
							} else if ("SVC".equals(outage.getEquipmentType().value())) {
								matchedCompensators++;
							} else if ("SC".equals(outage.getEquipmentType().value())) {
								matchedSeriesCompensators++;
							}
						} else if (matchResult == MatchResult.UNMATCHED_TEID) {
							unmatchedTeids++;
						} else if (matchResult == MatchResult.MISSING_SUBSTATION) {
							missingSubstation++;
						} else if (matchResult == MatchResult.UNEXPECTED_EQUIPMENT_TYPE || matchResult == MatchResult.MAPFAILURE_UNEXPECTED_EQUPMENT_TYPE) {
							unexpectedResource++;
						} else if (matchResult == MatchResult.MAPFAILURE_TERMINAL) {
							terminalMapFailures++;
						} else if (matchResult == MatchResult.MAPFAILURE_EQUIPMENT) {
							equipmentMapFailures++;
						} else if (matchResult == MatchResult.MISMATCH) {
							mixedOutageVsResource++;
						}
					}
				}
				log.info("Matched " + matchedOutages + " outages to Rdf records");
				log.info("... Indirectly matched " + indirectlyMatchedOutages + " outages");
				log.info("... Matched "  + matchedLines + " Lines");
				log.info("...... Matched "  + matchedACLineSegments + " ACLineSegments");
				log.info("... Matched "  + matchedTransformers + " Transformers");
				log.info("... Matched "  + matchedLoads + " CustomerLoads");
				log.info("... Matched "  + matchedBreakers + " Breakers");
				log.info("... Matched "  + matchedDisconnectors + " Disconnectors");
				log.info("... Matched "  + matchedCompensators + " ShuntCompensators and StaticVarCompensators");
				log.info("... Matched "  + matchedSeriesCompensators + " SeriesCompensators");
				log.info("Failed to identify " + unmatchedTeids + " TEIDs from Outges to CIM");
				log.info("Failed to find substation on " + missingSubstation + " TEIDs");
				log.info("Failed to map terminal on " + terminalMapFailures + " indirect TEIDs");
				log.info("Failed to map equipment on " + equipmentMapFailures + " indirect TEIDs");
				log.info("Failed to match substation or equipoment name for " + mixedOutageVsResource + " TEIDs");
				log.info("Found "  + unexpectedResource + " unexpeccted resources mapping TEID from Outages to CIM");
			}
		} catch (ParseException e) {
			// ignore for now
		}
	}
	
	private static void evaluateOutageImpact() {
		log.info("");
		log.info("Establish CIM model to evaluate outages against");
		int model = RdfGraph.initialize();
		RdfGraph.connect(model);
		// evaluate islands
		log.info("");
		log.info("Evaluate model before applying outages");
		Date startTime = new Date();
		int islands = RdfGraph.mapIslands(model, true);
		Date endTime = new Date();
		long duration = endTime.getTime() - startTime.getTime();
		log.info("Identified " + islands + " island(s) in " + duration + " milliseconds: " + formatter.format(endTime));
		// detect radial lines
		log.info("");
		startTime = new Date();
		Map<String,Map<String,OutageResourceNode>> radialEquipment = analizeRadialEquipment(model, false);
		endTime = new Date();
		duration = endTime.getTime() - startTime.getTime();
		log.info("Identified " + radialEquipment.get(ACLineSegmentNode.getNodeName()).size() + " radial ACLineSegments in " + radialEquipment.get(LineNode.getNodeName()).size() + " radial Lines in " + duration + " milliseconds: " + formatter.format(endTime));
		// detect disconnected lines
		log.info("");
		startTime = new Date();
		Map<String,Map<String,OutageResourceNode>> disconnectedEquipment = analizeDisconnectedEquipment(model, false);
		endTime = new Date();
		duration = endTime.getTime() - startTime.getTime();
		log.info("Identified " + disconnectedEquipment.get(ACLineSegmentNode.getNodeName()).size() + " disconnected ACLineSegments in "+ disconnectedEquipment.get(LineNode.getNodeName()).size() + " disconnected Lines");
		log.info("Identified " + disconnectedEquipment.get(PowerTransformerNode.getNodeName()).size() + " disconnected PowerTransformers");
		log.info("Disconnected equipoment detection completed in " + duration + " milliseconds: " + formatter.format(endTime));
		// apply outages to model
		log.info("");
		GregorianCalendar calendar = new GregorianCalendar();
		try {
			calendar.setTime(formatter.parse(outageDate));
		} catch (ParseException e) {
			// ignore for now
		}
		AppliedOutages appliedOutages = applyOutages(calendar, model, reportImpactAnalysisDetail);
		// evaluate islands
		log.info("");
		log.info("Evaluate model after applying outages");
		startTime = new Date();
		islands = RdfGraph.mapIslands(model, true);
		endTime = new Date();
		duration = endTime.getTime() - startTime.getTime();
		log.info("Identified " + islands + " island(s) in " + duration + " milliseconds: " + formatter.format(endTime));
		// detect radial lines
		log.info("");
		startTime = new Date();
		Map<String,Map<String,OutageResourceNode>> originalRadialEquipment = radialEquipment;
		radialEquipment = analizeRadialEquipment(model, false);
		endTime = new Date();
		duration = endTime.getTime() - startTime.getTime();
		log.info("Identified " + radialEquipment.get(ACLineSegmentNode.getNodeName()).size() + " radial ACLineSegments in " + radialEquipment.get(LineNode.getNodeName()).size() + " radial Lines in " + duration + " milliseconds: " + formatter.format(endTime));
		Map<String,OutageResourceNode> workingEquipment = radialEquipment.get(ACLineSegmentNode.getNodeName());
		for (String equipmentId : originalRadialEquipment.get(ACLineSegmentNode.getNodeName()).keySet()) {
			workingEquipment.remove(equipmentId);
		}
		workingEquipment = radialEquipment.get(LineNode.getNodeName());
		for (String equipmentId : originalRadialEquipment.get(LineNode.getNodeName()).keySet()) {
			workingEquipment.remove(equipmentId);
		}
		log.info("");
		String sectionTitle = "" + radialEquipment.get(ACLineSegmentNode.getNodeName()).size() + " additional radial ACLineSegments in " + radialEquipment.get(LineNode.getNodeName()).size() + " additional radial Lines detected";
		log.info(sectionTitle);
		if (reportImpactAnalysisDetail) {
			log.info(StringUtils.repeat('-', sectionTitle.length()));
			workingEquipment = radialEquipment.get(ACLineSegmentNode.getNodeName());
			for (String equipmentId : workingEquipment.keySet()) {
				log.info(workingEquipment.get(equipmentId).toIdentifier());
			}
			log.info(StringUtils.repeat('-', sectionTitle.length()));
			workingEquipment = radialEquipment.get(LineNode.getNodeName());
			for (String equipmentId : workingEquipment.keySet()) {
				log.info(workingEquipment.get(equipmentId).toIdentifier());
			}
		}
		// remove lines applied as outages
		Map<String,OutageResourceNode> acLineSegments = radialEquipment.get(ACLineSegmentNode.getNodeName());
		Map<String,OutageResourceNode> lines = radialEquipment.get(LineNode.getNodeName());
		List<String> removedLines = new LinkedList<String>();
		for (Outage outage : appliedOutages.getAppliedOutages()) {
			if ("LN".equals(outage.getEquipmentType().value())) {
				ACLineSegmentNode acLineSegment = (ACLineSegmentNode)acLineSegments.remove(outage.getRdfGuid());
				if (acLineSegment != null) {
					LineNode line = (LineNode)lines.remove(acLineSegment.getEquipmentContainer().getId());
					if (line != null ) {
						removedLines.add(line.getId());
					}
				}
				LineNode line = (LineNode)lines.remove(outage.getRdfGuid());
				if (line != null ) {
					removedLines.add(line.getId());
				}
			}
		}
		List<String> acLineSegmentsToRemove = new LinkedList<String>();
		for (String rdfId : acLineSegments.keySet()) {
			ACLineSegmentNode acLineSegment = (ACLineSegmentNode)acLineSegments.get(rdfId);
			if (removedLines.contains(acLineSegment.getEquipmentContainer().getId())) acLineSegmentsToRemove.add(acLineSegment.getId());
		}
		for (String rdfId : acLineSegmentsToRemove) {
			acLineSegments.remove(rdfId);
		}
		log.info("");
		sectionTitle = "" + acLineSegments.size() + " additional radial ACLineSegments in " + lines.size() + " additional radial Lines not included in outages";
		log.info(sectionTitle);
		log.info(StringUtils.repeat('-', sectionTitle.length()));
		workingEquipment = acLineSegments;
		for (String equipmentId : workingEquipment.keySet()) {
			log.info(workingEquipment.get(equipmentId).toIdentifier());
		}
		log.info(StringUtils.repeat('-', sectionTitle.length()));
		workingEquipment = lines;
		for (String equipmentId : workingEquipment.keySet()) {
			log.info(workingEquipment.get(equipmentId).toIdentifier());
		}
		// detect disconnected lines
		log.info("");
		startTime = new Date();
		Map<String,Map<String,OutageResourceNode>> originalDisconnectedLines = disconnectedEquipment;
		disconnectedEquipment = analizeDisconnectedEquipment(model, false);
		endTime = new Date();
		duration = endTime.getTime() - startTime.getTime();
		log.info("Identified " + disconnectedEquipment.get(ACLineSegmentNode.getNodeName()).size() + " disconnected ACLineSegments in "+ disconnectedEquipment.get(LineNode.getNodeName()).size() + " disconnected Lines");
		log.info("Identified " + disconnectedEquipment.get(PowerTransformerNode.getNodeName()).size() + " disconnected PowerTransformers");
		log.info("Disconnected equipment detection completed in " + duration + " milliseconds: " + formatter.format(endTime));
		workingEquipment = disconnectedEquipment.get(ACLineSegmentNode.getNodeName());
		for (String equipmentId : originalDisconnectedLines.get(ACLineSegmentNode.getNodeName()).keySet()) {
			workingEquipment.remove(equipmentId);
		}
		workingEquipment = disconnectedEquipment.get(LineNode.getNodeName());
		for (String equipmentId : originalDisconnectedLines.get(LineNode.getNodeName()).keySet()) {
			workingEquipment.remove(equipmentId);
		}
		workingEquipment = disconnectedEquipment.get(PowerTransformerNode.getNodeName());
		for (String equipmentId : originalDisconnectedLines.get(PowerTransformerNode.getNodeName()).keySet()) {
			workingEquipment.remove(equipmentId);
		}
		log.info("");
		sectionTitle = "" + disconnectedEquipment.get(ACLineSegmentNode.getNodeName()).size() + " additional disconnected ACLineSegments in " + disconnectedEquipment.get(LineNode.getNodeName()).size() + " additional disconnected Lines detected";
		log.info(sectionTitle);
		if (reportImpactAnalysisDetail) {
			log.info(StringUtils.repeat('-', sectionTitle.length()));
			workingEquipment = disconnectedEquipment.get(ACLineSegmentNode.getNodeName());
			for (String equipmentId : workingEquipment.keySet()) {
				log.info(workingEquipment.get(equipmentId).toIdentifier());
			}
			log.info(StringUtils.repeat('-', sectionTitle.length()));
			workingEquipment = disconnectedEquipment.get(LineNode.getNodeName());
			for (String equipmentId : workingEquipment.keySet()) {
				log.info(workingEquipment.get(equipmentId).toIdentifier());
			}
		}
		sectionTitle = "" + disconnectedEquipment.get(PowerTransformerNode.getNodeName()).size() + " additional disconnected PowerTransformers detected";
		log.info(sectionTitle);
		if (reportImpactAnalysisDetail) {
			log.info(StringUtils.repeat('-', sectionTitle.length()));
			workingEquipment = disconnectedEquipment.get(PowerTransformerNode.getNodeName());
			for (String equipmentId : workingEquipment.keySet()) {
				log.info(workingEquipment.get(equipmentId).toIdentifier());
			}
		}
		// remove lines and power transformers applied as outages
		acLineSegments = disconnectedEquipment.get(ACLineSegmentNode.getNodeName());
		lines = disconnectedEquipment.get(LineNode.getNodeName());
		Map<String,OutageResourceNode> powerTransformers = disconnectedEquipment.get(PowerTransformerNode.getNodeName());
		removedLines = new LinkedList<String>();
		for (Outage outage : appliedOutages.getAppliedOutages()) {
			if ("LN".equals(outage.getEquipmentType().value())) {
				ACLineSegmentNode acLineSegment = (ACLineSegmentNode)acLineSegments.remove(outage.getRdfGuid());
				if (acLineSegment != null) {
					LineNode line = (LineNode)lines.remove(acLineSegment.getEquipmentContainer().getId());
					if (line != null ) {
						removedLines.add(line.getId());
					}
				}
				LineNode line = (LineNode)lines.remove(outage.getRdfGuid());
				if (line != null ) {
					removedLines.add(line.getId());
				}
			} else if ("XF".equals(outage.getEquipmentType().value())) {
				powerTransformers.remove(outage.getRdfGuid());
			}
		}
		acLineSegmentsToRemove = new LinkedList<String>();
		for (String rdfId : acLineSegments.keySet()) {
			ACLineSegmentNode acLineSegment = (ACLineSegmentNode)acLineSegments.get(rdfId);
			if (removedLines.contains(acLineSegment.getEquipmentContainer().getId())) acLineSegmentsToRemove.add(acLineSegment.getId());
		}
		for (String rdfId : acLineSegmentsToRemove) {
			acLineSegments.remove(rdfId);
		}
		log.info("");
		sectionTitle = "" + acLineSegments.size() + " additional disconnected ACLineSegments in " + lines.size() + " additional disconnected Lines not included in outages";
		log.info(sectionTitle);
		log.info(StringUtils.repeat('-', sectionTitle.length()));
		workingEquipment = acLineSegments;
		for (String equipmentId : workingEquipment.keySet()) {
			log.info(workingEquipment.get(equipmentId).toIdentifier());
		}
		log.info(StringUtils.repeat('-', sectionTitle.length()));
		workingEquipment = lines;
		for (String equipmentId : workingEquipment.keySet()) {
			LineNode line = (LineNode)workingEquipment.get(equipmentId);
			log.info(line.toIdentifier());
			if (reportImpactAnalysisDiagrams) {
				String fileName = "line-" + line.getName()  + "-outage.gv";
				diagramLine(line.getName(),fileName,line.getModel());
			}
		}
		sectionTitle = "" + powerTransformers.size() + " additional disconnected PowerTransformers detected";
		log.info(sectionTitle);
		log.info(StringUtils.repeat('-', sectionTitle.length()));
		workingEquipment = powerTransformers;
		for (String equipmentId : workingEquipment.keySet()) {
			log.info(workingEquipment.get(equipmentId).toIdentifier());
		}
	}

	private enum Step { XML_TAG_START, START_TAG_START, END_TAG_START, TAG_START, VALUE, XML_TAG_END, START_TAG_END, END_TAG_END, COMMENT_TAG_END, TAG_END, UNEXPECTED, UNEXPECTED_LEADING, UNEXPECTED_TRAILING, DONE }
	
	private static long processCimFile(PushbackReader in) {
		long result = 0L;

		int nextCharAsInt = -1;
		StringBuffer value = new StringBuffer();
		Step step = Step.XML_TAG_START;
		Step priorStep = Step.XML_TAG_START;
		boolean testForBOM = true;
		//Stack<String> stack = new Stack<String>();
		try {
			while ((nextCharAsInt = in.read()) != -1) {
				if (testForBOM) {
					testForBOM = false;
					log.debug("...test byte order mark 0x" + Integer.toHexString(nextCharAsInt));
					if (nextCharAsInt == 0xfffe || nextCharAsInt == 0xfeff) {
						log.debug("... found UTF-16 byte order mark");
						continue;
					} else if (nextCharAsInt == 0xef) {
						log.debug("... check for UTF-8 byte order mark");
						int secondBomByte = in.read();
						if (secondBomByte == 0xbb) {
							int thirdBomByte = in.read();
							if (thirdBomByte == 0xBF) {
								log.debug("... found byte order mark 0xef 0xbb 0xbf for UTF-8");
								continue;
							} else {
								in.unread(thirdBomByte);
								in.unread(secondBomByte);
							}
						} else {
							in.unread(secondBomByte);
						}
					}
				}
				char nextChar = (char)nextCharAsInt;
				switch (step) {
					case XML_TAG_START:
					case START_TAG_START:
					case END_TAG_START:
					case TAG_START:
						// ignore white-space and look for the '<'
						if (nextChar == '<') {
							value.append(nextChar);
							if (step == Step.XML_TAG_START) step = Step.XML_TAG_END;
							else if (step == Step.START_TAG_START) step = Step.START_TAG_END;
							else if (step == Step.END_TAG_START) step = Step.END_TAG_END;
							else step = Step.TAG_END;
						} else if (Character.isWhitespace(nextChar) && step != Step.XML_TAG_START) {
							// ignore it
						} else {
							value.append(nextChar);
							if (step == Step.TAG_START) {
								step = Step.VALUE;
							} else {
								if (step == Step.XML_TAG_START) step = Step.UNEXPECTED_LEADING;
								else step = Step.UNEXPECTED;
							}
						}
						break;
					case VALUE:
					case UNEXPECTED:
					case UNEXPECTED_LEADING:
						// consume everything to the next '<'
						// TODO: Later will need to detect a comment in the value - hopefully this won't blow up right now
						if (nextChar == '<') {
							in.unread(nextChar);
							if (step == Step.VALUE) {
								log.trace("Value : " + value.toString().trim());
								if (currentRdf != null ) currentRdf.processElement(current, value.toString());
								step = Step.END_TAG_START;
							} else {
								log.info("Unexpected character(s) stream : " + value.toString());
								errors++;
								if (step == Step.UNEXPECTED_LEADING) step = Step.XML_TAG_START;
								else step = Step.TAG_START;
								
							}
							value.setLength(0);
						} else {
							// unexpected content
							value.append(nextChar);
						}
						break;
					case XML_TAG_END:
					case START_TAG_END:
					case END_TAG_END:
					case COMMENT_TAG_END:
					case TAG_END:
						// consume everything up to the closing '>'
						if (step == Step.XML_TAG_END && value.length() == 1) {
							value.append(nextChar);
							if (nextChar == '-' || Character.isWhitespace(nextChar)) {
								step = Step.UNEXPECTED;
							} else if (nextChar != '?') {
								step = Step.START_TAG_END;
							}
						} else if (step == Step.TAG_END) {
							value.append(nextChar);
							if (nextChar == '!') {
								priorStep = Step.TAG_START;
								step = Step.COMMENT_TAG_END;
							} else if (nextChar == '/') {
								if (stack.isEmpty()) step = Step.UNEXPECTED;
								else step = Step.END_TAG_END;
							} else if (nextChar == '>' || nextChar == '-' || Character.isWhitespace(nextChar)) {
								step = Step.UNEXPECTED;
							} else {
								step = Step.START_TAG_END;
							}
						} else if (nextChar == '>') {
							value.append(nextChar);
							if (step == Step.COMMENT_TAG_END) {
								// detect the proper end of a comment tag, otherwise keep reading
								if (value.length() > 7 && " -->".equals(value.substring(value.length()-4))) {
									// comment tag properly ended
									log.trace("Comment : " + value.toString().trim());
									value.setLength(0);
									step = priorStep;
								}
							} else if (step == Step.XML_TAG_END) {
								// detect the proper end of the XML tag
								if (value.length() > 6 && "?>".equals(value.substring(value.length()-2))) {
									// XML tag properly ended
									log.trace("XML tag : " + value.toString().trim());
									value.setLength(0);
									step = Step.START_TAG_START;
								} else {
									step = Step.UNEXPECTED;
								}
							} else if (step == Step.END_TAG_END) {
								// detect the proper end of the end tag by matching against the stack
								Element parent = current;
								current = stack.poll();
								if (parent.getName().equals(baseName(value.substring(2,value.length()-1)))) {
									log.trace("End tag: " + current);
									currentRdf = RdfModel.popRdf(currentRdf, value.substring(2,value.length()-1));
								} else {
									log.info("Unexpected end tag mismatch: " + current.getName() + " vs. " + baseName(value.substring(2,value.length()-3)));
									errors++;
								}
								if (stack.isEmpty()) step = Step.DONE;
								else step = Step.TAG_START;
								value.setLength(0);
							} else if (step == Step.START_TAG_END) {
								// identify the tag and determine if it is self ending
								String startTag = getStartTag(value);
								registerIdentifiedObjects(current,startTag);
								if (value.charAt(value.length() - 2) == '/') {
									Element selfEndingStart = getElement(current,startTag);
									parseAttributes(selfEndingStart,currentRdf,value);
								} else {
									stack.add(0, current);
									if (maxDepth < stack.size()) maxDepth = stack.size();
									current = getElement(current,startTag);
									currentRdf = parseAttributes(current,currentRdf,value);
								}
								result++;
								if ((result % 1000000L) == 0L) {
									log.trace("Processed " + result + " elements...");
								}
								step = Step.TAG_START;
								value.setLength(0);
							}
						} else if (nextChar == '!' && value.length() == 1) {
							value.append(nextChar);
							priorStep = startForEndTag(step);
							step = Step.COMMENT_TAG_END;
						} else {
							value.append(nextChar);
						}
						break;
					case DONE:
						if (Character.isWhitespace(nextChar)) {
							// ignore it
						} else {
							log.info("Unexpected content at end of file");
							step = Step.UNEXPECTED_TRAILING;
							errors++;
						}
						break;
					case UNEXPECTED_TRAILING:
						// ignore it
						break;
				}
			}
			if (step != Step.DONE) {
				log.info("Unexpected end of file");
				errors++;
			}
			if (reportParseAnalysis) reportParseAnalysis();
		} catch (IOException e) {
			log.error("Error processing file:" + e.getMessage());
		}
		
		return result;
	}
	
	private static void recordElement(Element recordElement) {
		log.info("");
		String sectionTitle =  recordElement.getName() + " Elements (" + recordElement.getElement().size() + ")";
		log.info(sectionTitle);
		log.info(StringUtils.repeat('-', sectionTitle.length()));
		for(Element element : recordElement.getElement()) {
			log.info(element.getName());
		}
		log.info("");
		sectionTitle =  recordElement.getName() + " Attributes (" + recordElement.getAttribute().size() + ")";
		log.info(sectionTitle);
		log.info(StringUtils.repeat('-', sectionTitle.length()));
		for(Attribute attribute : recordElement.getAttribute()) {
			log.info(attribute.getName());
		}
	}
	
	private static Element getElement(Element parent, String tag) {
		Element result = null;

		int namespaceEnd = tag.indexOf(':') + 1;
		int classNameEnd = tag.lastIndexOf('.') + 1;
		String namespace = "";
		if (namespaceEnd != 0) namespace = tag.substring(0,namespaceEnd-1);
		String name = tag.substring(Math.max(namespaceEnd,classNameEnd));
		String className = name;
		if (classNameEnd != 0) className = tag.substring(namespaceEnd,classNameEnd-1);
		log.trace("Namespace: " + namespace + ", Class: " + className + ", Name: " + name);
		
		if (model == null) {
			log.trace("Adding root element: " + name);
			model = new Element();
			model.setNamespace(namespace);
			model.setClassName(className);
			model.setName(name);
			map.put(tag,model);
			result = model;
		} else {
			for(Element element : parent.getElement()) {
				if (namespace.equals(element.getNamespace()) && className.equals(element.getClassName()) && name.equals(element.getName())) {
					result = element;
					break;
				}
			}
			if (result == null) {
				result = map.get(tag);
				if (result != null) {
					parent.getElement().add(result);
					log.trace("Adding tag " + name + " to parent " + parent.getName());
				}
			}
			if (result == null) {
				result = new Element();
				result.setNamespace(namespace);
				result.setClassName(className);
				result.setName(name);
				map.put(tag, result);
				log.trace("Adding tag " + name + " to parent " + parent.getName());
				parent.getElement().add(result);
			}
		}
		
		// populate  class
		Element classElement = classMap.get(namespace + ":" + className);
		if (classElement == null) {
			classElement = new Element();
			classElement.setNamespace(namespace);
			classElement.setClassName(className);
			classElement.setName(className);
			classMap.put(namespace + ":" + className, classElement);
		}
		if (!name.equals(className)) {
			Element classField = null;
			for(Element element : classElement.getElement()) {
				if (name.equals(element.getName())) {
					classField = element;
					break;
				}
			}
			if (classField == null) {
				classField = new Element();
				classField.setNamespace(namespace);
				classField.setClassName(className);
				classField.setName(name);
				classElement.getElement().add(classField);
			}
		}
		
		return result;
	}
	
	private static Attribute getAttribute(Element element, String attributeName) {
		Attribute result = null;

		int namespaceEnd = attributeName.indexOf(':') + 1;
		int classNameEnd = attributeName.lastIndexOf('.') + 1;
		String namespace = "";
		if (namespaceEnd != 0) namespace = attributeName.substring(0,namespaceEnd-1);
		String name = attributeName.substring(Math.max(namespaceEnd,classNameEnd));
		String className = name;
		if (classNameEnd != 0) className = attributeName.substring(namespaceEnd,classNameEnd-1);
		log.trace("Namespace: " + namespace + ", Class: " + className + ", Name: " + name);
		
		for(Attribute attribute : element.getAttribute()) {
			if (namespace.equals(attribute.getNamespace()) && className.equals(attribute.getClassName()) && name.equals(attribute.getName())) {
				result = attribute;
				break;
			}
		}
		if (result == null) {
			result = new Attribute();
			result.setNamespace(namespace);
			result.setClassName(className);
			result.setName(name);
			log.trace("Adding tag " + name + " to element " + element.getName());
			element.getAttribute().add(result);
		}
		
		return result;
	}

	private static void registerIdentifiedObjects(Element current, String startTag) {
		int namespaceEnd = startTag.indexOf(':') + 1;
		int classNameEnd = startTag.lastIndexOf('.') + 1;
		String namespace = "";
		if (namespaceEnd != 0) namespace = startTag.substring(0,namespaceEnd-1);
		String name = startTag.substring(Math.max(namespaceEnd,classNameEnd));
		String className = name;
		if (classNameEnd != 0) className = startTag.substring(namespaceEnd,classNameEnd-1);
		log.trace("Namespace: " + namespace + ", Class: " + className + ", Name: " + name);
		
		if (namespace.equals("cim") && className.equals("IdentifiedObject")) {
			String elementName = current.getNamespace() + ":" + current.getClassName();
			if (!current.getClassName().equals(current.getName())) elementName += "." + name;
			Element element = identifiedObjectMap.get(elementName);
			if (element == null) {
				identifiedObjectMap.put(elementName, current);
			}
		}
	}
	
	private static String baseName(String fullName) {
		int start = 0;
		int namespaceEnd = fullName.indexOf(':') + 1;
		int classNameEnd = fullName.lastIndexOf('.') + 1;
		return fullName.substring(Math.max(start, Math.max(namespaceEnd,classNameEnd)));
	}
	
	private static Step startForEndTag(Step step) {
		Step result = Step.START_TAG_START;
		if (step == Step.TAG_END)  result = Step.TAG_START;
		if (step == Step.END_TAG_END) result = Step.END_TAG_START;
		return result;
	}
	
	private static String getStartTag(StringBuffer tag) {
		int endPos = tag.length() - 1;
		if (tag.charAt(endPos - 1) == '/') endPos--;
		for (int testIndex = 2 ; testIndex < endPos ; testIndex++ ) {
			if (Character.isWhitespace(tag.charAt(testIndex))) endPos = testIndex;
		}
		return tag.substring(1, endPos);
	}

	private static Rdf parseAttributes(Element element, Rdf currentRdf, StringBuffer tag) {
		Rdf result = currentRdf;
		
		int endPos = tag.length() - 1;
		if (tag.charAt(endPos - 1) == '/') endPos--;
		int startPos = 1;
		for ( ; startPos < endPos ; startPos++ ) {
			if (Character.isWhitespace(tag.charAt(startPos))) break;
		}
		if (startPos + 1 < endPos) {
			String workingContent = tag.substring(startPos+1,endPos).trim();
			log.trace(element.getNamespace() + ":" + element.getClassName() + " attribute list: [" + workingContent + "]");
			int position;
			while (workingContent.length() > 0) {
				position = workingContent.indexOf('=');
				if (position == -1 ) {
					log.error("missing assignment in attribute list");
					errors ++;
					break;
				}
				String attributeName = workingContent.substring(0, position).trim();
				boolean isGuid = false;
				if ("rdf:ID".equals(attributeName) || "rdf:resource".equals(attributeName)) isGuid = true;
				String attributeValue = null;
				workingContent = workingContent.substring(position + 1).trim();
				if (workingContent.length() == 0) {
					log.error("missing value in attribute list");
					errors ++;
					break;
				}
				if (workingContent.charAt(0) == '"') {
					position = workingContent.indexOf('"', 1);
					if (position == -1 ) {
						log.error("missing close quote in attribute list");
						errors ++;
						break;
					}
					attributeValue = workingContent.substring(1,position);
					workingContent = workingContent.substring(position + 1).trim();
				} else {
					position = workingContent.indexOf(' ');
					if (position == -1 ) position = workingContent.length();
					attributeValue = workingContent.substring(0,position);
					workingContent = workingContent.substring(position + 1).trim();
				}
				int start = 0;
				if (attributeValue.charAt(start) == '#') start++;
				if (attributeValue.charAt(start) == '_') start++;
				if (attributeValue.charAt(start) == '{') start++;
				int end = attributeValue.length() - 1;
				if (attributeValue.charAt(end) == '}') end--;
				attributeValue = attributeValue.substring(start,end+1);
				// remove whitespace from GUIDs 
				if (isGuid) {
					for (int index = 0 ; index < attributeValue.length() ; index++) {
						if (Character.isWhitespace(attributeValue.charAt(index))) {
							log.info("Whitespace as position " + index + "of GUID: " + attributeValue);
							if (index == 0) attributeValue = attributeValue.substring(1);
							else if (index == attributeValue.length() - 1) attributeValue = attributeValue.substring(0, attributeValue.length() - 1);
							else attributeValue = attributeValue.substring(0, index) + attributeValue.substring(index + 1);
							index--;
							log.info("Updated GUID: " + attributeValue);
						}
					}
				}
				log.trace("Attribute: " + attributeName + ", Value: " + attributeValue);
				Attribute attribute = getAttribute(element,attributeName);
				String elementKey = element.getNamespace() + ":" + element.getClassName();
				if (!element.getClassName().equals(element.getName())) {
					elementKey += "." + element.getName();
				}
				String attributeKey = attribute.getNamespace() + ":" + attribute.getClassName();
				if (!attribute.getClassName().equals(attribute.getName())) {
					attributeKey += "." + attribute.getName();
				}
				if ("rdf:ID".equals(attributeKey)) {
					Element idElement = idElements.get(elementKey);
					if (idElement == null) {
						idElements.put(elementKey, element);
					}
					String rdfName = current.getNamespace() + ":" + current.getClassName();
					result = RdfModel.create(rdfName, attributeValue);
				}
				if ("rdf:resource".equals(attributeKey)) {
					Element resourceElement = resourceElements.get(elementKey);
					if (resourceElement == null) {
						resourceElements.put(elementKey, element);
					}
					if (currentRdf != null) currentRdf.processAttribute(element, attributeValue);
				}
				Attribute mapAttribute = attributeMap.get(attributeKey);
				if (mapAttribute == null) {
					attributeMap.put(attributeKey, attribute);
				}
			}
		}
		
		return result;
	}
	
	private static void listSubstationEquipment(String substationName, int model) {
		Substation substation = Substation.findByName(substationName);
		log.info("");
		if (substation == null) {
			log.info("Substation not found: " + substationName);
		} else {
			String sectionTitle = "List Substation: " + substation.getName();
			log.info(sectionTitle);
			log.info(StringUtils.repeat('-', sectionTitle.length()));
			List<VoltageLevel> substationVoltageLevels = VoltageLevel.findVoltageLevels(substation.getId());
			if (substationVoltageLevels != null) {
				for (VoltageLevel voltageLevel : substationVoltageLevels) {
					VoltageLevelNode voltageLevelNode = (VoltageLevelNode)RdfNode.findByNodeId(voltageLevel.getId(), model);
					log.info(voltageLevelNode.toString());
					List<Rdf> voltageLevelEquipment = Equipment.findRdfByContainer(voltageLevel.getId()); 
					if ( voltageLevelEquipment == null) {
						log.info("No equipment");
					} else {
						for (Rdf rdf : voltageLevelEquipment) {
							RdfNode node = RdfNode.findByNodeId(rdf.getId(), model);
							log.info(node.toString());
							if (BayNode.class.isInstance(node)) {
								List<Rdf> bayEquipment = Equipment.findRdfByContainer(node.getId()); 
								if ( bayEquipment == null) {
									log.info("No equipment in bay " + node.getName());
								} else {
									for (Rdf bayRdf : bayEquipment) {
										RdfNode bayNode = RdfNode.findByNodeId(bayRdf.getId(), model);
										log.info("... " + bayNode.toString());
									}
								}
							}
						}
					}
				}
			}
		}
	}
	
	private static void reportParseAnalysis() {
		log.info("Maximum model depth: " + maxDepth);
		log.info("Completed parsing file");
		log.info("XML Element count: " + cimElements);
		log.info("Error count: " + errors);
		log.info("");
		log.info("" + map.size() + " unique Elements");
		log.info("" + model.getElement().size() + " elements off root " + model.getName());
		log.info("" + classMap.size() + " identified classes");
		log.info("" + attributeMap.size() + " identified attributes");
		log.info("" + idElements.size() + " elements with an rdf:ID attribute");
		if (errors == 0) log.info("Successful parse");
		log.info("");
		log.info("Full Element List");
		log.info("-----------------");
		for(String key : map.keySet()) {
			log.info(key);
		}
		log.info("");
		log.info("Root Element");
		log.info("------------");
		log.info(model.getName());
		recordElement(model);
		log.info("");
		String sectionTitle = "Identified Classes (" + classMap.size() + ")";
		log.info(sectionTitle);
		log.info(StringUtils.repeat('-', sectionTitle.length()));
		for(String key : classMap.keySet()) {
			log.info(key);
		}
		log.info("");
		sectionTitle = "Identified Attributes (" + attributeMap.size() + ")";
		log.info(sectionTitle);
		log.info(StringUtils.repeat('-', sectionTitle.length()));
		for(String key : attributeMap.keySet()) {
			log.info(key);
		}
		log.info("");
		sectionTitle = "IdentifiedObject Elements (" + identifiedObjectMap.size() + ")";
		log.info(sectionTitle);
		log.info(StringUtils.repeat('-', sectionTitle.length()));
		for(String key : identifiedObjectMap.keySet()) {
			log.info(key);
		}
		log.info("");
		sectionTitle = "Elements with an rdf:ID attribute (" + idElements.size() + ")";
		log.info(sectionTitle);
		log.info(StringUtils.repeat('-', sectionTitle.length()));
		for(String key : idElements.keySet()) {
			log.info(key);
		}
		log.info("");
		sectionTitle = "Elements with an rdf:resource attribute (" + resourceElements.size() + ")";
		log.info(sectionTitle);
		log.info(StringUtils.repeat('-', sectionTitle.length()));
		for(String key : resourceElements.keySet()) {
			log.info(key);
		}
		Element element = classMap.get("cim:IdentifiedObject");
		recordElement(element);
		element = map.get("cim:Substation");
		recordElement(element);
		element = map.get("cim:Terminal");
		recordElement(element);
		element = map.get("cim:Breaker");
		recordElement(element);
		element = map.get("cim:Disconnector");
		recordElement(element);
		log.info("");
		Set<String> terminalIds = Terminal.getTerimalIds();
		sectionTitle = "Review Terminals (" + terminalIds.size() + ")";
		log.info(sectionTitle);
		log.info(StringUtils.repeat('-', sectionTitle.length()));
		int unmatchedConductingEquipment = 0;
		int unmatchedConnectivityNodes = 0;
		for(String terminalId : terminalIds) {
			Terminal terminal = Terminal.findByTermialId(terminalId);
			ConductingEquipment conductiongEquipment = (ConductingEquipment)Rdf.findById(terminal.getConductingEquipment());
			if (conductiongEquipment == null) {
				log.trace("Unable to locate ConductingEquipment " + terminal.getConductingEquipment() + " for Terminal " + terminal.getId());
				unmatchedConductingEquipment++;
			}
			ConnectivityNode connectivityNode = (ConnectivityNode)Rdf.findById(terminal.getConnectivityNode());
			if (connectivityNode == null) {
				log.trace("Unable to locate ConnectivityNode " + terminal.getConnectivityNode());
				unmatchedConnectivityNodes++;
			}
		}
		log.info("" + unmatchedConductingEquipment + " unmatched ConductingEquipment ids");
		log.info("" + unmatchedConnectivityNodes + " unmatched ConnectivityNode ids");
		log.info("");
		Set<String> conductingEquipmentIds = ConductingEquipment.getConductingEquipmentIds();
		sectionTitle = "Review ConductingEquipment (" + conductingEquipmentIds.size() + ")";
		log.info(sectionTitle);
		log.info(StringUtils.repeat('-', sectionTitle.length()));
		int conductingEquipmentSansTerminals = 0;
		int otherConductingEquipmentNotTwoTerminals = 0;
		int singleTerminalComponentsWitMultipleTerminals = 0;
		for(String id : conductingEquipmentIds) {
			ConductingEquipment equipment = ConductingEquipment.findConductingEquipmentById(id);
			List<Terminal> conductingEquipmentTerminals = Terminal.findTerminalsByConductingEquipmentId(equipment.getId());
			if (conductingEquipmentTerminals == null) {
				log.trace("Unable to locate Terminal for ConductingEquipment " + equipment.getId());
				conductingEquipmentSansTerminals++;
			} else {
				Rdf rdf = Rdf.findById(id);
				if (rdf.getClass().getName().equals(BusbarSection.class.getName())
						|| rdf.getClass().getName().equals(CustomerLoad.class.getName())
						|| rdf.getClass().getName().equals(EndCap.class.getName())
						|| rdf.getClass().getName().equals(GroundDisconnector.class.getName())
						|| rdf.getClass().getName().equals(ShuntCompensator.class.getName())
						|| rdf.getClass().getName().equals(StaticVarCompensator.class.getName())
						|| rdf.getClass().getName().equals(StationSupply.class.getName())
						|| rdf.getClass().getName().equals(SynchronousMachine.class.getName())
						|| rdf.getClass().getName().equals(TransformerWinding.class.getName())) {
					if (conductingEquipmentTerminals.size() != 1) {
						singleTerminalComponentsWitMultipleTerminals++;
					}
				} else {
					if (conductingEquipmentTerminals.size() != 2) {
						log.info("Identified " + conductingEquipmentTerminals.size() + " terminals for ConductingEquipment " + equipment.getId());
						if (rdf != null) log.info("... of type " + rdf.getClass().getName());
						otherConductingEquipmentNotTwoTerminals++;
					}
				}
			}
		}
		log.info("" + conductingEquipmentSansTerminals + " conducting equipment with no Terminals");
		log.info("" + singleTerminalComponentsWitMultipleTerminals + " single Terminal components with multiple terminals");
		log.info("" + otherConductingEquipmentNotTwoTerminals + " other conducting equipment with Terminal count not 2");
		log.info("");
		Set<String> connectivityNodeIds = ConnectivityNode.getConnectivityNodeIds();
		sectionTitle = "Review ConnectivityNodes (" + connectivityNodeIds.size() + ")";
		log.info(sectionTitle);
		log.info(StringUtils.repeat('-', sectionTitle.length()));
		int connectivityNodesSansTerminals = 0;
		int connectivityNodesUnderTwoTerminals = 0;
		for(String id : connectivityNodeIds) {
			ConnectivityNode connectivityNode = ConnectivityNode.findConnectivityNodeById(id);
			List<Terminal> connectivityNodeTerminals = Terminal.findTerminalsByConnectivityNodeId(connectivityNode.getId());
			if (connectivityNodeTerminals == null) {
				log.trace("Unable to locate Terminal for ConnectivityNode " + connectivityNode.getId());
				connectivityNodesSansTerminals++;
			}
			if (connectivityNodeTerminals.size() < 2) {
				log.trace("Identified " + connectivityNodeTerminals.size() + " terminals for ConnectivityNode " + connectivityNode.getId());
				connectivityNodesUnderTwoTerminals++;
			}
		}
		log.info("" + connectivityNodesSansTerminals + " connectivity nodes with no Terminals");
		log.info("" + connectivityNodesUnderTwoTerminals + " connectivity nodes with Terminal count under 2");
	}
	
	private static void processOutageFile(CSVParser in) {
		for (CSVRecord csvRecord: in) {
			Outage outage = new Outage();
			DatatypeFactory factory = null;

			try {
				factory = DatatypeFactory.newInstance();
			} catch (DatatypeConfigurationException e) {
				// ignore for now
			}
			GregorianCalendar calendar = new GregorianCalendar();
			Duration negativeOneSecond = factory.newDuration("-PT1S");
			// TODO: Determine if the timezone is required
			//calendar.setTimeZone(TimeZone.getTimeZone("America/Chicago"));
			try {
				calendar.setTime(csvFormatter.parse(csvRecord.get("PlannedStartDate")));
				outage.setPlannedStartDate(factory.newXMLGregorianCalendar(calendar));
			} catch (ParseException e) {
				// ignore for now
			}
			try {
				calendar.setTime(csvFormatter.parse(csvRecord.get("PlannedEndDate")));
				outage.setPlannedEndDate(factory.newXMLGregorianCalendar(calendar));
			} catch (ParseException e) {
				// ignore for now
			}
			try {
				calendar.setTime(csvFormatter.parse(csvRecord.get("ActualStartDate")));
				outage.setActualStartDate(factory.newXMLGregorianCalendar(calendar));
			} catch (ParseException e) {
				// ignore for now
			}
			try {
				calendar.setTime(csvFormatter.parse(csvRecord.get("ActualEndDate")));
				outage.setActualEndDate(factory.newXMLGregorianCalendar(calendar));
			} catch (ParseException e) {
				// ignore for now
			}
			String switchOutageStatus = csvRecord.get("BreakerSwitchOutageStatus");
			if (StringUtils.isNotBlank(switchOutageStatus)) outage.setSwitchOutageStatus(SwitchOutageStatus.fromValue(switchOutageStatus));
			outage.setEquipmentType(EquipmentType.fromValue(csvRecord.get("EquipmentType")));
			outage.setEquipmentName(csvRecord.get("EquipmentName"));
			outage.setEquipmentFromStationName(csvRecord.get("EquipmentFromStationName"));
			outage.setEquipmentToStationName(csvRecord.get("EquipmentToStationName"));
			outage.setTeid(Integer.valueOf(csvRecord.get("TEID")));
			XMLGregorianCalendar effectiveHour = (XMLGregorianCalendar)outage.getPlannedStartDate().clone();
			if (outage.getActualStartDate() != null) effectiveHour = (XMLGregorianCalendar)outage.getActualStartDate().clone();
			effectiveHour.setMinute(0);
			effectiveHour.setSecond(0);
			outage.setEffectiveStartHour(effectiveHour);
			effectiveHour = (XMLGregorianCalendar)outage.getPlannedEndDate().clone();
			if (outage.getActualEndDate() != null) effectiveHour = (XMLGregorianCalendar)outage.getActualEndDate().clone();
			if (effectiveHour.getMinute() != 0 || effectiveHour.getSecond() != 0) {
				effectiveHour.setMinute(59);
				effectiveHour.setSecond(59);
			} else {
				effectiveHour.add(negativeOneSecond);
			}
			outage.setEffectiveEndHour(effectiveHour);
			outages.add(outage);
		}
	}
	
	private static void processHiddenOutageFile(CSVParser in, Set<Outage> hiddenOutages) {
		for (CSVRecord csvRecord: in) {
			Outage outage = new Outage();
			DatatypeFactory factory = null;

			try {
				factory = DatatypeFactory.newInstance();
			} catch (DatatypeConfigurationException e) {
				// ignore for now
			}
			GregorianCalendar calendar = new GregorianCalendar();
			// TODO: Determine if the following is needed Duration negativeOneSecond = factory.newDuration("-PT1S");
			// TODO: Determine if the timezone is required
			//calendar.setTimeZone(TimeZone.getTimeZone("America/Chicago"));
			try {
				calendar.setTime(formatter.parse(csvRecord.get("StartDate")));
				outage.setPlannedStartDate(factory.newXMLGregorianCalendar(calendar));
				outage.setEffectiveStartHour(factory.newXMLGregorianCalendar(calendar));
			} catch (ParseException e) {
				// ignore for now
			}
			try {
				calendar.setTime(formatter.parse(csvRecord.get("EndDate")));
				outage.setPlannedEndDate(factory.newXMLGregorianCalendar(calendar));
				outage.setEffectiveEndHour(factory.newXMLGregorianCalendar(calendar));
			} catch (ParseException e) {
				// ignore for now
			}
			outage.setEquipmentType(EquipmentType.fromValue(csvRecord.get("EquipmentType")));
			outage.setEquipmentName(csvRecord.get("EquipmentName"));
			outage.setEquipmentFromStationName(csvRecord.get("EquipmentFromStation"));
			outage.setEquipmentToStationName(csvRecord.get("EquipmentToStation"));
			outage.setBaseVoltage(csvRecord.get("BaseVoltage"));
			outage.setTeid(Integer.valueOf(csvRecord.get("TEID")));
			hiddenOutages.add(outage);
		}
	}

	private static class OutageAnalysisWorker implements Runnable {
		private static Object lock = new Object();
		public static Object QueueDepthLock = new Object();
		public static AtomicBoolean queueLocked = new AtomicBoolean(false);
		public static AtomicInteger queueDepth = new AtomicInteger(0);
		
		int rdfModel = -1;
		AppliedOutages appliedOutages = null;
		Map<String,Map<String,OutageResourceNode>> originalRadialEquipment = null;
		Map<String,Map<String,OutageResourceNode>> originalDisconnectedEquipment = null;
		Set<Outage> hiddenOutages = null;
		Set<Outage> radializedButNotDisconnectedEquipment = null;
		XMLGregorianCalendar startDate = null;
		XMLGregorianCalendar endDate = null;
		
		public OutageAnalysisWorker(Set<Outage> hiddenOutages, Set<Outage> radializedButNotDisconnectedEquipment, XMLGregorianCalendar startDate, XMLGregorianCalendar endDate, Map<String,Map<String,OutageResourceNode>> originalRadialEquipment, Map<String,Map<String,OutageResourceNode>> originalDisconnectedEquipment) {
			log.info("Establish the model for analysis");
			rdfModel = RdfGraph.initialize();
			RdfGraph.connect(rdfModel);
			this.originalRadialEquipment = originalRadialEquipment;
			this.originalDisconnectedEquipment = originalDisconnectedEquipment;
			this.hiddenOutages = hiddenOutages;
			this.radializedButNotDisconnectedEquipment = radializedButNotDisconnectedEquipment;
			this.startDate = (XMLGregorianCalendar)startDate.clone();
			this.endDate = (XMLGregorianCalendar)endDate.clone();
		}
		
		private String outageDateRange() {
			return "[" + formatter.format(appliedOutages.getEffectiveStart().getTime()) + " to " + formatter.format(appliedOutages.getEffectiveEnd().getTime()) + "]";
		}
		
		@Override
		public void run() {
			Date startTime = null;
			Date endTime = null;
			long duration = 0;
			Map<String,Map<String,OutageResourceNode>> radialEquipment = null;
			Map<String,OutageResourceNode> allDisconnectedACLineSegments = null;
			Map<String,OutageResourceNode> workingEquipment = null;
			Map<String,OutageResourceNode> acLineSegments = null;
			Map<String,OutageResourceNode> lines = null;
			List<String> acLineSegmentsToRemove = null;
			List<String> removedLines = null;
			String sectionTitle = null;
			if (dailyAnalysisIncludeRadial) {
				// detect radial lines
				log.info("");
				startTime = new Date();
				radialEquipment = analizeRadialEquipment(rdfModel, false);
				endTime = new Date();
				duration = endTime.getTime() - startTime.getTime();
				log.info(outageDateRange() + " Identified " + radialEquipment.get(ACLineSegmentNode.getNodeName()).size() + " radial ACLineSegments in " + radialEquipment.get(LineNode.getNodeName()).size() + " radial Lines in " + duration + " milliseconds: " + formatter.format(endTime));
				workingEquipment = radialEquipment.get(ACLineSegmentNode.getNodeName());
				for (String equipmentId : originalRadialEquipment.get(ACLineSegmentNode.getNodeName()).keySet()) {
					workingEquipment.remove(equipmentId);
				}
				workingEquipment = radialEquipment.get(LineNode.getNodeName());
				for (String equipmentId : originalRadialEquipment.get(LineNode.getNodeName()).keySet()) {
					workingEquipment.remove(equipmentId);
				}
				sectionTitle = outageDateRange() + " " + radialEquipment.get(ACLineSegmentNode.getNodeName()).size() + " additional radial ACLineSegments in " + radialEquipment.get(LineNode.getNodeName()).size() + " additional radial Lines detected";
				log.info(sectionTitle);
				// remove lines applied as outages
				acLineSegments = radialEquipment.get(ACLineSegmentNode.getNodeName());
				lines = radialEquipment.get(LineNode.getNodeName());
				removedLines = new LinkedList<String>();
				for (Outage outage : appliedOutages.getAppliedOutages()) {
					if ("LN".equals(outage.getEquipmentType().value())) {
						ACLineSegmentNode acLineSegment = (ACLineSegmentNode)acLineSegments.remove(outage.getRdfGuid());
						if (acLineSegment != null) {
							LineNode line = (LineNode)lines.remove(acLineSegment.getEquipmentContainer().getId());
							if (line != null ) {
								removedLines.add(line.getId());
							}
						}
						LineNode line = (LineNode)lines.remove(outage.getRdfGuid());
						if (line != null ) {
							removedLines.add(line.getId());
						}
					}
				}
				acLineSegmentsToRemove = new LinkedList<String>();
				for (String rdfId : acLineSegments.keySet()) {
					ACLineSegmentNode acLineSegment = (ACLineSegmentNode)acLineSegments.get(rdfId);
					if (removedLines.contains(acLineSegment.getEquipmentContainer().getId())) acLineSegmentsToRemove.add(acLineSegment.getId());
				}
				for (String rdfId : acLineSegmentsToRemove) {
					acLineSegments.remove(rdfId);
				}
				synchronized (lock) {
					log.info("");
					sectionTitle = outageDateRange() + " " + acLineSegments.size() + " additional radial ACLineSegments in " + lines.size() + " additional radial Lines not included in outages";
					log.info(sectionTitle);
					log.info(StringUtils.repeat('-', sectionTitle.length()));
					workingEquipment = acLineSegments;
					for (String equipmentId : workingEquipment.keySet()) {
						log.info(workingEquipment.get(equipmentId).toIdentifier());
					}
					if (dailyAnalysisLineDetail) {
						log.info(StringUtils.repeat('-', sectionTitle.length()));
						workingEquipment = lines;
						for (String equipmentId : workingEquipment.keySet()) {
							log.info(workingEquipment.get(equipmentId).toIdentifier());
						}
					}
				}
			}
			// detect disconnected lines
			startTime = new Date();
			Map<String,Map<String,OutageResourceNode>> disconnectedEquipment = analizeDisconnectedEquipment(rdfModel, false);
			endTime = new Date();
			duration = endTime.getTime() - startTime.getTime();
			log.info(outageDateRange() + " Identified " + disconnectedEquipment.get(ACLineSegmentNode.getNodeName()).size() + " disconnected ACLineSegments in "+ disconnectedEquipment.get(LineNode.getNodeName()).size() + " disconnected Lines");
			log.info(outageDateRange() + " Identified " + disconnectedEquipment.get(PowerTransformerNode.getNodeName()).size() + " disconnected PowerTransformers");
			log.info(outageDateRange() + " Disconnected equipment detection completed in " + duration + " milliseconds: " + formatter.format(endTime));
			// remove lines and power transformers of eqiupment which were disconnected before outages were applied
			allDisconnectedACLineSegments = new HashMap<String,OutageResourceNode>(disconnectedEquipment.get(ACLineSegmentNode.getNodeName()));
			workingEquipment = disconnectedEquipment.get(ACLineSegmentNode.getNodeName());
			for (String equipmentId : originalDisconnectedEquipment.get(ACLineSegmentNode.getNodeName()).keySet()) {
				workingEquipment.remove(equipmentId);
			}
			workingEquipment = disconnectedEquipment.get(LineNode.getNodeName());
			for (String equipmentId : originalDisconnectedEquipment.get(LineNode.getNodeName()).keySet()) {
				workingEquipment.remove(equipmentId);
			}
			workingEquipment = disconnectedEquipment.get(PowerTransformerNode.getNodeName());
			for (String equipmentId : originalDisconnectedEquipment.get(PowerTransformerNode.getNodeName()).keySet()) {
				workingEquipment.remove(equipmentId);
			}
			sectionTitle = outageDateRange() + " " + disconnectedEquipment.get(ACLineSegmentNode.getNodeName()).size() + " additional disconnected ACLineSegments in " + disconnectedEquipment.get(LineNode.getNodeName()).size() + " additional disconnected Lines detected";
			log.info(sectionTitle);
			sectionTitle = outageDateRange() + " " + disconnectedEquipment.get(PowerTransformerNode.getNodeName()).size() + " additional disconnected PowerTransformers detected";
			log.info(sectionTitle);
			// remove lines and power transformers applied as outages
			acLineSegments = disconnectedEquipment.get(ACLineSegmentNode.getNodeName());
			lines = disconnectedEquipment.get(LineNode.getNodeName());
			Map<String,OutageResourceNode> powerTransformers = disconnectedEquipment.get(PowerTransformerNode.getNodeName());
			removedLines = new LinkedList<String>();
			for (Outage outage : appliedOutages.getAppliedOutages()) {
				if ("LN".equals(outage.getEquipmentType().value())) {
					ACLineSegmentNode acLineSegment = (ACLineSegmentNode)acLineSegments.remove(outage.getRdfGuid());
					if (acLineSegment != null) {
						LineNode line = (LineNode)lines.remove(acLineSegment.getEquipmentContainer().getId());
						if (line != null ) {
							removedLines.add(line.getId());
						}
					}
					LineNode line = (LineNode)lines.remove(outage.getRdfGuid());
					if (line != null ) {
						removedLines.add(line.getId());
					}
				} else if ("XF".equals(outage.getEquipmentType().value())) {
					powerTransformers.remove(outage.getRdfGuid());
				}
			}
			acLineSegmentsToRemove = new LinkedList<String>();
			for (String rdfId : acLineSegments.keySet()) {
				ACLineSegmentNode acLineSegment = (ACLineSegmentNode)acLineSegments.get(rdfId);
				if (removedLines.contains(acLineSegment.getEquipmentContainer().getId())) acLineSegmentsToRemove.add(acLineSegment.getId());
			}
			for (String rdfId : acLineSegmentsToRemove) {
				acLineSegments.remove(rdfId);
			}
			synchronized (lock) {
				log.info("");
				sectionTitle = outageDateRange() + " " + acLineSegments.size() + " additional disconnected AC line segments in " + lines.size() + " additional disconnected lines not included in outages";
				log.info(sectionTitle);
				log.info(StringUtils.repeat('-', sectionTitle.length()));
				workingEquipment = acLineSegments;
				for (String equipmentId : workingEquipment.keySet()) {
					ACLineSegmentNode node = (ACLineSegmentNode)workingEquipment.get(equipmentId);
					log.info(workingEquipment.get(equipmentId).toIdentifier());
					RdfGraph.identifySubstations(node, true);
					hiddenOutages.add(OutageModel.createOutage(node, startDate, endDate));
				}
				workingEquipment = powerTransformers;
				for (String equipmentId : workingEquipment.keySet()) {
					PowerTransformerNode node = (PowerTransformerNode)workingEquipment.get(equipmentId);
					log.info(workingEquipment.get(equipmentId).toIdentifier());
					hiddenOutages.add(OutageModel.createOutage(node, startDate, endDate));
				}
				if (dailyAnalysisLineDetail) {
					log.info(StringUtils.repeat('-', sectionTitle.length()));
					workingEquipment = lines;
					for (String equipmentId : workingEquipment.keySet()) {
						log.info(workingEquipment.get(equipmentId).toIdentifier());
					}
				}
				if (dailyAnalysisIncludeRadial) {
					// identify radial but not disconnected equipment
					Map<String,OutageResourceNode> radialACLineSegments = radialEquipment.get(ACLineSegmentNode.getNodeName());
					log.info("");
					sectionTitle = outageDateRange() + " " + radialACLineSegments.size() + " radialized AC line segments";
					log.info(sectionTitle);
					log.info(StringUtils.repeat('-', sectionTitle.length()));
					for (String lineId : radialACLineSegments.keySet()) {
						ACLineSegmentNode node = (ACLineSegmentNode)radialACLineSegments.get(lineId);
						if (allDisconnectedACLineSegments.get(lineId) == null) {
							RdfGraph.identifySubstations(node, true);
							radializedButNotDisconnectedEquipment.add(OutageModel.createOutage(node, startDate, endDate));
						}
					}
				}
			}
			RdfNode.releaseModel(rdfModel);
			queueDepth.decrementAndGet();
			if (dailyOutageQueueDepthRelease != -1) {
				synchronized(QueueDepthLock) {
					if (queueLocked.get() && queueDepth.get() <= dailyOutageQueueDepthRelease) {
						queueLocked.set(false);
						QueueDepthLock.notifyAll();
					}
				}
			}
		}
		
		public AppliedOutages applyOutages(Calendar calendar, boolean assumeConnected) {
			appliedOutages = ModelParser.applyOutages(calendar, rdfModel, assumeConnected);
			GregorianCalendar gregorianCalendar = new GregorianCalendar();
			gregorianCalendar.setTime(appliedOutages.getEffectiveEnd().getTime());
			try {
				DatatypeFactory factory = DatatypeFactory.newInstance();
				endDate = factory.newXMLGregorianCalendar(gregorianCalendar);
				Duration oneSecond = factory.newDuration("PT1S");
				endDate.add(oneSecond);
			} catch (DatatypeConfigurationException e) {
				// do nothing
			}
			return appliedOutages;
		}
	}
	
	private static void analizeDailyOutages() {
		Date startTime = null;
		Date endTime = null;
		long duration = 0;
		Date processStart = new Date();
		log.info("");
		log.info("Process daily outages started " + formatter.format(processStart));
		log.info("");
		log.info("Establish CIM model to evaluate outages against");
		int baseModel = RdfGraph.initialize();
		RdfGraph.connect(baseModel);
		if (dailyAnalysisIncludeIslands) {
			// evaluate islands
			log.info("");
			log.info("Evaluate model before applying outages");
			startTime = new Date();
			int islands = RdfGraph.mapIslands(baseModel, true);
			endTime = new Date();
			duration = endTime.getTime() - startTime.getTime();
			log.info("Identified " + islands + " island(s) in " + duration + " milliseconds: " + formatter.format(endTime));
		}
		Map<String,Map<String,OutageResourceNode>> radialEquipment = null;
		if (dailyAnalysisIncludeRadial) {
			// detect radial lines
			log.info("");
			startTime = new Date();
			radialEquipment = analizeRadialEquipment(baseModel, false);
			endTime = new Date();
			duration = endTime.getTime() - startTime.getTime();
			log.info("Identified " + radialEquipment.get(ACLineSegmentNode.getNodeName()).size() + " radial ACLineSegments in " + radialEquipment.get(LineNode.getNodeName()).size() + " radial Lines in " + duration + " milliseconds: " + formatter.format(endTime));
		}
		// detect disconnected lines
		log.info("");
		startTime = new Date();
		Map<String,Map<String,OutageResourceNode>> disconnectedEquipment = analizeDisconnectedEquipment(baseModel, false);
		endTime = new Date();
		duration = endTime.getTime() - startTime.getTime();
		log.info("Identified " + disconnectedEquipment.get(ACLineSegmentNode.getNodeName()).size() + " disconnected ACLineSegments in "+ disconnectedEquipment.get(LineNode.getNodeName()).size() + " disconnected Lines");
		log.info("Identified " + disconnectedEquipment.get(PowerTransformerNode.getNodeName()).size() + " disconnected PowerTransformers");
		log.info("Disconnected equipment detection completed in " + duration + " milliseconds: " + formatter.format(endTime));
		// process daily outages
		Set<Outage> hiddenOutages = new TreeSet<Outage>(new HiddenOutageComparator());
		Set<Outage> radialButNotDisconnectedEquipment = new TreeSet<Outage>(new HiddenOutageComparator());
		DatatypeFactory factory = null;
		GregorianCalendar calendar = new GregorianCalendar();
		int workUnits = 0;
		try {
			factory = DatatypeFactory.newInstance();
			Duration oneSecond = factory.newDuration("PT1S");
			Duration negativeOneSecond = factory.newDuration("-PT1S");
			Duration processDays = factory.newDuration("P" + dailyOutageAnalysisDays + "D");
			DateFormat dateFormatter = new SimpleDateFormat("MM-dd-yyyy");
			DateFormat dateOrderedFormatter = new SimpleDateFormat("yyyy-MM-dd");
			calendar.setTime(dateFormatter.parse(dailyOutageAnalysisStartDate));
			XMLGregorianCalendar startDate = factory.newXMLGregorianCalendar(calendar);
			startDate.setTime(0, 0, 0);
			XMLGregorianCalendar endDate = (XMLGregorianCalendar)startDate.clone();
			endDate.add(processDays);
			endDate.add(negativeOneSecond);
			log.info("Process outages between " + formatter.format(startDate.toGregorianCalendar().getTime()) + " and " + formatter.format(endDate.toGregorianCalendar().getTime()));

			List<OutageAnalysisWorker> workers = new LinkedList<OutageAnalysisWorker>();
			ExecutorService executor = Executors.newFixedThreadPool(dailyOutageAnalysisThreads);
			while (endDate.compare(startDate) > 0) {
				workUnits ++;

				OutageAnalysisWorker worker = new OutageAnalysisWorker(hiddenOutages, radialButNotDisconnectedEquipment, startDate, endDate, radialEquipment, disconnectedEquipment);
				workers.add(worker);
				AppliedOutages appliedOutages = worker.applyOutages(startDate.toGregorianCalendar(), false);
				executor.execute(worker);
				OutageAnalysisWorker.queueDepth.incrementAndGet();
				if (dailyOutageQueueDepthLimit != -1) {
					synchronized(OutageAnalysisWorker.QueueDepthLock) {
						if (OutageAnalysisWorker.queueDepth.get() >= dailyOutageQueueDepthLimit) {
							try {
								log.info("Queue depth limit reached... waiting for queue depth to reduce...");
								OutageAnalysisWorker.queueLocked.set(true);
								OutageAnalysisWorker.QueueDepthLock.wait();
							} catch (InterruptedException e) {
								// ignore it... got woken up...
							}
						}
					}
				}
				if (appliedOutages.getEffectiveEnd().compareTo(endDate.toGregorianCalendar()) > 0) appliedOutages.setEffectiveEnd(endDate.toGregorianCalendar());
				log.info("Applied " + appliedOutages.getOutagesApplied() + " outages between " + formatter.format(appliedOutages.getEffectiveStart().getTime()) + " and " + formatter.format(appliedOutages.getEffectiveEnd().getTime()));
				calendar.setTime(appliedOutages.getEffectiveEnd().getTime());
				startDate = factory.newXMLGregorianCalendar(calendar);
				startDate.add(oneSecond);
			}
			log.info("Reported " + workUnits + " sets of applied outages");
			executor.shutdown();
			while (!executor.isTerminated()) {
				try {
					log.info("... waiting for threads to complete...");
					executor.awaitTermination(10, TimeUnit.MINUTES);
				} catch (InterruptedException e) {
					// do nothing
				}
			}
			log.info("");
			log.info("Process daily outages thread execution completed... generate outage report...");
			NavigableSet<Outage> currentHiddenOutages = new TreeSet<Outage>(new HiddenOutageComparator());
			try {
				PrintWriter out = new PrintWriter(new FileWriter("hidden_outages-" + dateOrderedFormatter.format(dateFormatter.parse(dailyOutageAnalysisStartDate)) + ".csv"));
				out.println(OutageModel.hiddenOutageHeader());
				Outage currentOutage = null;
				for(Outage outage : hiddenOutages) {
					if (currentOutage == null) {
						currentOutage = outage;
					} else if (currentOutage.getTeid() == outage.getTeid() && currentOutage.getPlannedEndDate().compare(outage.getPlannedStartDate()) == 0) {
						currentOutage.setPlannedEndDate(outage.getPlannedEndDate());
					} else {
						out.println(OutageModel.hiddenOutageToString(currentOutage));
						currentHiddenOutages.add(currentOutage);
						currentOutage = outage;
					}
				}
				if (currentOutage != null) {
					out.println(OutageModel.hiddenOutageToString(currentOutage));
					currentHiddenOutages.add(currentOutage);
				}
				out.close();
				if (dailyOutageAnalysisDifferenceStartDate != null) {
					log.info("");
					log.info("... generate outage different report...");
					out = new PrintWriter(new FileWriter("hidden_outages_difference-" + dateOrderedFormatter.format(dateFormatter.parse(dailyOutageAnalysisStartDate)) + ".csv"));
					out.println(OutageModel.hiddenOutageDifferenceHeader());
					CSVParser in = null;
					String fileName = "";
					NavigableSet<Outage> priorHiddenOutages = new TreeSet<Outage>(new HiddenOutageComparator());
					try {
						fileName = "hidden_outages-" + dateOrderedFormatter.format(dateFormatter.parse(dailyOutageAnalysisDifferenceStartDate)) + ".csv";
						in = new CSVParser(new FileReader(fileName),CSVFormat.EXCEL.withHeader());
					} catch (ParseException e) {
						log.error("Supplied date parsed: " + dailyOutageAnalysisDifferenceStartDate);
					} catch (IOException e) {
						log.error("Supplied Hidden Outage file not found: " + fileName);
					}
					if (in != null) {
						log.info("Start Hidden Outage Parse: " + formatter.format(new Date()));
						log.info("Hidden Outage File: " + fileName);
						processHiddenOutageFile(in, priorHiddenOutages);
						log.info("End Hidden Outage Parse with " + priorHiddenOutages.size() + " records: " + formatter.format(new Date()));
						try {
							in.close();
						} catch (IOException e) {
							// ignore any error closing the file
						}
					}
					// identify outages in priorHiddenOutages not in currentHiddenOutages
					GregorianCalendar beginningOfTime = new GregorianCalendar();
					beginningOfTime.setTime(formatter.parse("01-01-1900 00:00:00"));
					XMLGregorianCalendar matchDate = factory.newXMLGregorianCalendar(beginningOfTime);
					Outage match = new Outage();
					match.setEffectiveStartHour(matchDate);
					match.setEffectiveEndHour(matchDate);
					for (Outage outage : priorHiddenOutages) {
						match.setTeid(outage.getTeid());
						Outage comparison = currentHiddenOutages.ceiling(match);
						if (comparison == null || comparison.getTeid() != outage.getTeid()) {
							// in prior but not in current
							out.println(OutageModel.hiddenOutageToString(outage) + ",Out");
						} else  {
							boolean overlap = false;
							while (overlap == false && comparison != null && comparison.getTeid() == outage.getTeid()) {
								// walk the list looking for an overlap - using < and > rather than <= and >= due to extra second on endHour in Outage
								if (outage.getEffectiveStartHour().compare(comparison.getEffectiveEndHour()) < 0 &&
									outage.getEffectiveEndHour().compare(comparison.getEffectiveStartHour()) > 0) {
									overlap = true;
								} else {
									comparison = currentHiddenOutages.higher(comparison);
								}
							}
							if (!overlap) {
								// in prior but not in current
								out.println(OutageModel.hiddenOutageToString(outage) + ",Out");
							}
						}
					}
					// identify outages in currentHiddenOutages not in priorHiddenOutages
					for (Outage outage : currentHiddenOutages) {
						match.setTeid(outage.getTeid());
						Outage comparison = priorHiddenOutages.ceiling(match);
						if (comparison == null || comparison.getTeid() != outage.getTeid()) {
							// in current but not in prior
							out.println(OutageModel.hiddenOutageToString(outage) + ",In");
						} else  {
							boolean overlap = false;
							while (overlap == false && comparison != null && comparison.getTeid() == outage.getTeid()) {
								// walk the list looking for an overlap - using < and > rather than <= and >= due to extra second on endHour in Outage
								if (outage.getEffectiveStartHour().compare(comparison.getEffectiveEndHour()) < 0 &&
									outage.getEffectiveEndHour().compare(comparison.getEffectiveStartHour()) > 0) {
									overlap = true;
								} else {
									comparison = priorHiddenOutages.higher(comparison);
								}
							}
							if (!overlap) {
								// in current but not in prior
								out.println(OutageModel.hiddenOutageToString(outage) + ",In");
							}
						}
					}
					out.close();
				}
				if (dailyAnalysisIncludeRadial) {
					NavigableSet<Outage> currentRadializedButNotDisconnectedEquipment = new TreeSet<Outage>(new HiddenOutageComparator());
					out = new PrintWriter(new FileWriter("radialized_but_not_disconnected-" + dateOrderedFormatter.format(dateFormatter.parse(dailyOutageAnalysisStartDate)) + ".csv"));
					out.println(OutageModel.hiddenOutageHeader());
					currentOutage = null;
					for(Outage outage : radialButNotDisconnectedEquipment) {
						if (currentOutage == null) {
							currentOutage = outage;
						} else if (currentOutage.getTeid() == outage.getTeid() && currentOutage.getPlannedEndDate().compare(outage.getPlannedStartDate()) == 0) {
							currentOutage.setPlannedEndDate(outage.getPlannedEndDate());
						} else {
							out.println(OutageModel.hiddenOutageToString(currentOutage));
							currentRadializedButNotDisconnectedEquipment.add(currentOutage);
							currentOutage = outage;
						}
					}
					if (currentOutage != null) {
						out.println(OutageModel.hiddenOutageToString(currentOutage));
						currentRadializedButNotDisconnectedEquipment.add(currentOutage);
					}
					out.close();
					if (dailyOutageAnalysisDifferenceStartDate != null) {
						log.info("");
						log.info("... generate radial but not disconnected different report...");
						out = new PrintWriter(new FileWriter("radialized_but_not_disconnected-difference-" + dateOrderedFormatter.format(dateFormatter.parse(dailyOutageAnalysisStartDate)) + ".csv"));
						out.println(OutageModel.hiddenOutageDifferenceHeader());
						CSVParser in = null;
						String fileName = "";
						NavigableSet<Outage> priorRadializedButNotDisconnectedEquipment = new TreeSet<Outage>(new HiddenOutageComparator());
						try {
							fileName = "radialized_but_not_disconnected-" + dateOrderedFormatter.format(dateFormatter.parse(dailyOutageAnalysisDifferenceStartDate)) + ".csv";
							in = new CSVParser(new FileReader(fileName),CSVFormat.EXCEL.withHeader());
						} catch (ParseException e) {
							log.error("Supplied date parsed: " + dailyOutageAnalysisDifferenceStartDate);
						} catch (IOException e) {
							log.error("Supplied Hidden Outage file not found: " + fileName);
						}
						if (in != null) {
							log.info("Start Hidden Outage Parse: " + formatter.format(new Date()));
							log.info("Hidden Outage File: " + fileName);
							processHiddenOutageFile(in, priorRadializedButNotDisconnectedEquipment);
							log.info("End Hidden Outage Parse with " + priorRadializedButNotDisconnectedEquipment.size() + " records: " + formatter.format(new Date()));
							try {
								in.close();
							} catch (IOException e) {
								// ignore any error closing the file
							}
						}
						// identify outages in priorRadializedButNotDisconnectedEquipment not in currentRadializedButNotDisconnectedEquipment
						GregorianCalendar beginningOfTime = new GregorianCalendar();
						beginningOfTime.setTime(formatter.parse("01-01-1900 00:00:00"));
						XMLGregorianCalendar matchDate = factory.newXMLGregorianCalendar(beginningOfTime);
						Outage match = new Outage();
						match.setEffectiveStartHour(matchDate);
						match.setEffectiveEndHour(matchDate);
						for (Outage outage : priorRadializedButNotDisconnectedEquipment) {
							match.setTeid(outage.getTeid());
							Outage comparison = currentRadializedButNotDisconnectedEquipment.ceiling(match);
							if (comparison == null || comparison.getTeid() != outage.getTeid()) {
								// in prior but not in current
								out.println(OutageModel.hiddenOutageToString(outage) + ",Out");
							} else  {
								boolean overlap = false;
								while (overlap == false && comparison != null && comparison.getTeid() == outage.getTeid()) {
									// walk the list looking for an overlap - using < and > rather than <= and >= due to extra second on endHour in Outage
									if (outage.getEffectiveStartHour().compare(comparison.getEffectiveEndHour()) < 0 &&
										outage.getEffectiveEndHour().compare(comparison.getEffectiveStartHour()) > 0) {
										overlap = true;
									} else {
										comparison = currentRadializedButNotDisconnectedEquipment.higher(comparison);
									}
								}
								if (!overlap) {
									// in prior but not in current
									out.println(OutageModel.hiddenOutageToString(outage) + ",Out");
								}
							}
						}
						// identify outages in currentRadializedButNotDisconnectedEquipment not in priorRadializedButNotDisconnectedEquipment
						for (Outage outage : currentRadializedButNotDisconnectedEquipment) {
							match.setTeid(outage.getTeid());
							Outage comparison = priorRadializedButNotDisconnectedEquipment.ceiling(match);
							if (comparison == null || comparison.getTeid() != outage.getTeid()) {
								// in current but not in prior
								out.println(OutageModel.hiddenOutageToString(outage) + ",In");
							} else  {
								boolean overlap = false;
								while (overlap == false && comparison != null && comparison.getTeid() == outage.getTeid()) {
									// walk the list looking for an overlap - using < and > rather than <= and >= due to extra second on endHour in Outage
									if (outage.getEffectiveStartHour().compare(comparison.getEffectiveEndHour()) < 0 &&
										outage.getEffectiveEndHour().compare(comparison.getEffectiveStartHour()) > 0) {
										overlap = true;
									} else {
										comparison = priorRadializedButNotDisconnectedEquipment.higher(comparison);
									}
								}
								if (!overlap) {
									// in current but not in prior
									out.println(OutageModel.hiddenOutageToString(outage) + ",In");
								}
							}
						}
						out.close();
					}
				}
			} catch (IOException e) {
				// do nothing for now
			}
			Date processEnd = new Date();
			log.info("");
			log.info("Process daily outages completed " + formatter.format(processEnd));
			long processDuration = processEnd.getTime() - processStart.getTime();
			log.info("Processeing " + workUnits + " outage analyses using " + dailyOutageAnalysisThreads + " threads took " + ((processDuration + 59999L) / 60000L) + " minutes");
		} catch (DatatypeConfigurationException e) {
			log.info("DatatypeFactor initialization failure");
		} catch (ParseException e) {
			log.info("Failed to parse outageDate " + dailyOutageAnalysisStartDate);
		}
		
	}
	
	private static void analizeOutageIncrements() {
		log.info("");
		DatatypeFactory factory = null;
		GregorianCalendar calendar = new GregorianCalendar();
		try {
			factory = DatatypeFactory.newInstance();
			Duration oneSecond = factory.newDuration("PT1S");
			Duration negativeOneSecond = factory.newDuration("-PT1S");
			Duration oneDay = factory.newDuration("P1D");
			calendar.setTime(formatter.parse(outageDate));
			XMLGregorianCalendar startDate = factory.newXMLGregorianCalendar(calendar);
			startDate.setTime(0, 0, 0);
			XMLGregorianCalendar endDate = (XMLGregorianCalendar)startDate.clone();
			endDate.add(oneDay);
			endDate.add(negativeOneSecond);
			int model = RdfGraph.getCurrentModel();
			if (model < 0) {
				log.info("Establish the model for analysis");
				model = RdfGraph.initialize();
				RdfGraph.connect(model);
			}
			log.info("Identify 24 hours of outage start and end times between " + formatter.format(startDate.toGregorianCalendar().getTime()) + " and " + formatter.format(endDate.toGregorianCalendar().getTime()));
			int passes = 0;
			while (endDate.compare(startDate) > 0) {
				passes ++;
				RdfGraph.reset(model, true, true, true, true, false);
				AppliedOutages appliedOutages = applyOutages(startDate.toGregorianCalendar(), model, false);
				if (appliedOutages.getEffectiveEnd().compareTo(endDate.toGregorianCalendar()) > 0) appliedOutages.setEffectiveEnd(endDate.toGregorianCalendar());
				log.info("Applied " + appliedOutages.getOutagesApplied() + " outages between " + formatter.format(appliedOutages.getEffectiveStart().getTime()) + " and " + formatter.format(appliedOutages.getEffectiveEnd().getTime()));
				calendar.setTime(appliedOutages.getEffectiveEnd().getTime());
				startDate = factory.newXMLGregorianCalendar(calendar);
				startDate.add(oneSecond);
			}
			log.info("Reported " + passes + " sets of applied outages");
		} catch (DatatypeConfigurationException e) {
			log.info("DatatypeFactor initialization failure");
		} catch (ParseException e) {
			log.info("Failed to parse outageDate " + outageDate);
		}
	}
	
	private static boolean parseCommandLine(String[] args) {
		int index = 0;
		boolean error = false;
		boolean usage = false;
		// parse command line options
		while (index < args.length) {
			if ("-cimFile".equals(args[index]) && (args.length-index) > 1) {
				cimFileName = args[index+1];
				index ++;
			} else if ("-outageFile".equals(args[index]) && (args.length-index) > 1) {
				outageFileName = args[index+1];
				index ++;
			} else if ("-outageDate".equals(args[index]) && (args.length-index) > 1) {
				outageDate = args[index+1];
				index ++;
			} else if ("-help".equals(args[index])) {
				usage = true;
			} else if ("-parseAnalysis".equals(args[index])) {
				reportParseAnalysis = true;
			} else if ("-rdfInfo".equals(args[index]) && (args.length-index) > 1) {
				reportRdfNode.add(args[index+1]);
				index ++;
			} else if ("-substationAnalysis".equals(args[index]) && (args.length-index) > 1) {
				reportSubstationEquipment.add(args[index+1]);
				index ++;
			} else if ("-substationDiagram".equals(args[index]) && (args.length-index) > 1) {
				diagramSubstationEquipment.add(args[index+1]);
				index ++;
			} else if ("-lineSubstationDiagram".equals(args[index]) && (args.length-index) > 1) {
				diagramLineSubstations.add(args[index+1]);
				index ++;
			} else if ("-islandAnalysis".equals(args[index])) {
				reportIslandEquipment = true;
			} else if ("-islandAnalysisDetail".equals(args[index])) {
				reportIslandEquipmentDetail = true;
			} else if ("-radialAnalysis".equals(args[index])) {
				reportRadialLines = true;
			} else if ("-radialAnalysisDetail".equals(args[index])) {
				reportRadialLinesDetail = true;
			} else if ("-disconnectedAnalysis".equals(args[index])) {
				reportDisconnectedEquipment = true;
			} else if ("-disconnectedAnalysisDetail".equals(args[index])) {
				reportDisconnectedEquipmentDetail = true;
			} else if ("-outageAnalysis".equals(args[index])) {
				reportOutageAnalysis = true;
			} else if ("-outageAnalysisDetail".equals(args[index])) {
				reportOutageAnalysisDetail = true;
			} else if ("-impactAnalysis".equals(args[index])) {
				reportImpactAnalysis = true;
			} else if ("-impactAnalysisDetail".equals(args[index])) {
				reportImpactAnalysisDetail = true;
			} else if ("-impactAnalysisDiagrams".equals(args[index])) {
				reportImpactAnalysisDiagrams = true;
			} else if ("-testOutageIncrements".equals(args[index])) {
				testOutageIncrements = true;
			} else if ("-collapseCimByConnectivityNodeGroup".equals(args[index])) {
				collapseUsingConnectivityNodeGroup = true;
			} else if ("-collapseCimByConnectivityNode".equals(args[index])) {
				collapseUsingConnectivityNode = true;
			} else if ("-connectivityNodeAnalysis".equals(args[index])) {
				reportConnectivityNodeAnalysis = true;
			} else if ("-dailyOutageAnalysisThreads".equals(args[index]) && (args.length-index) > 1) {
				dailyOutageAnalysisThreads = Integer.parseInt(args[index+1]);
				index ++;
			} else if ("-dailyOutageAnalysisStart".equals(args[index]) && (args.length-index) > 1) {
				dailyOutageAnalysisStartDate = args[index+1];
				index ++;
			} else if ("-dailyOutageAnalysisDiff".equals(args[index]) && (args.length-index) > 1) {
				dailyOutageAnalysisDifferenceStartDate = args[index+1];
				index ++;
			} else if ("-dailyOutageAnalysisDays".equals(args[index]) && (args.length-index) > 1) {
				dailyOutageAnalysisDays = Integer.parseInt(args[index+1]);
				index ++;
			} else if ("-dailyAnalysisIncludeRadial".equals(args[index])) {
				dailyAnalysisIncludeRadial = true;
			} else if ("-dailyAnalysisIncludeIslands".equals(args[index])) {
				dailyAnalysisIncludeIslands = true;
			} else if ("-dailyAnalysisLineDetail".equals(args[index])) {
				dailyAnalysisLineDetail = true;
			} else if ("-dailyOutageAnalysisQueueLimit".equals(args[index]) && (args.length-index) > 1) {
				dailyOutageQueueDepthLimit = Integer.parseInt(args[index+1]);
				index ++;
			} else if ("-dailyOutageAnalysisQueueRelease".equals(args[index]) && (args.length-index) > 1) {
				dailyOutageQueueDepthRelease = Integer.parseInt(args[index+1]);
				index ++;
			} else if ("-differenceCurrent".equals(args[index]) && (args.length-index) > 1) {
				currentHiddenOutageDate = args[index+1];
				index ++;
			} else if ("-differencePrior".equals(args[index]) && (args.length-index) > 1) {
				priorHiddenOutageDate = args[index+1];
				index ++;
			} else {
				error = true;
				log.error("Unexpected value: " + args[index]);
			}
			index ++;
		}
		if (error) {
			log.error("Failure parsing command line");
		}
		if (usage || error) {
			log.info("Usage: ModelParser [options]");
			log.info("Options: -help                                    : provide this message");
			log.info("         -cimFile <filenName>                     : the name of the CIM XML file to process");
			log.info("         -outageFile <filenName>                  : the name of the outage CSV file to process");
			log.info("         -parseAnalysis                           : output parse analysis");
			log.info("         -rdfInfo <rdfId>                         : output rdf node info");
			log.info("         -substationAnalysis <substationName>     : output substation equipment");
			log.info("         -substationDiagram <substationName>      : output substation diagram");
			log.info("         -lineSubstationDiagram <lineName>        : output line substation diagram");
			log.info("         -islandAnalysis                          : report equipment in islands");
			log.info("         -islandAnalysisDetail                    : detailed report of equipment in islands");
			log.info("         -radialAnalysis                          : report radial lines");
			log.info("         -radialAnalysisDetail                    : detailed report of radial lines");
			log.info("         -disconnectedAnalysis                    : report disconnected lines");
			log.info("         -disconnectedAnalysisDetail              : detailed report of disconnected lines");
			log.info("         -outageDate \"MM-dd-yyyy HH:mm:ss\"      : desired outage analysis date");
			log.info("         -outageAnalysis                          : report outage analysis");
			log.info("         -outageAnalysisDetail                    : detailed report of outage analysis");
			log.info("         -impactAnalysis                          : report outage impact analysis");
			log.info("         -impactAnalysisDetail                    : detailed report of outage impact analysis");
			log.info("         -impactAnalysisDiagrams                  : diagrams of lines identified during impact analysis");
			log.info("         -testOutageIncrements                    : test and report outage increments");
			log.info("         -collapseCimByConnectivityNodeGroup      : collapse the CIM by the ConnectivityNodeGroup technique");
			log.info("         -collapseCimByConnectivityNode           : collapse the CIM by the ConnectivityNode technique");
			log.info("         -connectivityNodeAnalysis                : report connectivity nodes not in a substation");
			log.info("         -dailyOutageAnalysisThreads nnn          : the number of threads to allocate for daily outage analysis");
			log.info("         -dailyOutageAnalysisStart \"MM-dd-yyyy\" : the start date for daily outage analysis");
			log.info("         -dailyOutageAnalysisDiff \"MM-dd-yyyy\"  : the start date for daily outage analysis used for difference analysis");
			log.info("         -dailyOutageAnalysisDays nnn             : the number of days to perform daily outage analysis for");
			log.info("         -dailyOutageAnalysisIncludeRadial        : include radial line analysis in daily outage analysis");
			log.info("         -dailyOutageAnalysisIncludeIslands       : include island analysis in daily outage analysis");
			log.info("         -dailyOutageAnalysisLineDetail           : include lines in addition to ac line segments in daily outage analysis");
			log.info("         -dailyOutageAnalysisQueueLimit nnn       : queue depth limit for daily outage analysis");
			log.info("         -dailyOutageAnalysisQueueRelease nnn     : queue depth at which to begin resume thread generation for daily outage analysis");
			log.info("         -dailyOutageAnalysisLineDetail           : include lines in addition to ac line segments in daily outage analysis");
			log.info("         -differenceCurrent \"MM-dd-yyyy\"        : the date for the daily outage file to treat as current hidden outages");
			log.info("         -differencePrior \"MM-dd-yyyy\"          : the date for the daily outage file to treat as prioer hidden outages");
		}
		return error;
	}
}
