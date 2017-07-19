package com.omniprise.cim.graph;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.omniprise.cim.model.ACLineSegment;
import com.omniprise.cim.model.Analog;
import com.omniprise.cim.model.BaseVoltage;
import com.omniprise.cim.model.Bay;
import com.omniprise.cim.model.Breaker;
import com.omniprise.cim.model.BusbarSection;
import com.omniprise.cim.model.ConnectivityNode;
import com.omniprise.cim.model.ConnectivityNodeGroup;
import com.omniprise.cim.model.CustomerLoad;
import com.omniprise.cim.model.Disconnector;
import com.omniprise.cim.model.Discrete;
import com.omniprise.cim.model.EndCap;
import com.omniprise.cim.model.GroundDisconnector;
import com.omniprise.cim.model.Line;
import com.omniprise.cim.model.PowerTransformer;
import com.omniprise.cim.model.Rdf;
import com.omniprise.cim.model.SeriesCompensator;
import com.omniprise.cim.model.ShuntCompensator;
import com.omniprise.cim.model.StaticVarCompensator;
import com.omniprise.cim.model.StationSupply;
import com.omniprise.cim.model.Substation;
import com.omniprise.cim.model.SynchronousMachine;
import com.omniprise.cim.model.Terminal;
import com.omniprise.cim.model.TransformerWinding;
import com.omniprise.cim.model.VoltageLevel;

public class RdfGraph {
	private static Logger log = LoggerFactory.getLogger(RdfGraph.class); 
	private static int model = -1;
	
	private static RdfNode create(Rdf source, int model) {
		RdfNode result = null;
		
		if (ACLineSegment.class.isInstance(source)) result = new ACLineSegmentNode(source, model);
		else if (Analog.class.isInstance(source)) result = new AnalogNode(source, model);
		else if (BaseVoltage.class.isInstance(source)) result = new BaseVoltageNode(source, model);
		else if (Bay.class.isInstance(source)) result = new BayNode(source, model);
		else if (Breaker.class.isInstance(source)) result = new BreakerNode(source, model);
		else if (BusbarSection.class.isInstance(source)) result = new BusbarSectionNode(source, model);
		else if (ConnectivityNode.class.isInstance(source)) result = new ConnectivityElementNode(source, model);
		else if (ConnectivityNodeGroup.class.isInstance(source)) result = new ConnectivityNodeGroupNode(source, model);
		else if (CustomerLoad.class.isInstance(source)) result = new CustomerLoadNode(source, model);
		else if (Disconnector.class.isInstance(source)) result = new DisconnectorNode(source, model);
		else if (Discrete.class.isInstance(source)) result = new DiscreteNode(source, model);
		else if (EndCap.class.isInstance(source)) result = new EndCapNode(source, model);
		else if (GroundDisconnector.class.isInstance(source)) result = new GroundDisconnectorNode(source, model);
		else if (Line.class.isInstance(source)) result = new LineNode(source, model);
		else if (PowerTransformer.class.isInstance(source)) result = new PowerTransformerNode(source, model);
		else if (SeriesCompensator.class.isInstance(source)) result = new SeriesCompensatorNode(source, model);
		else if (ShuntCompensator.class.isInstance(source)) result = new ShuntCompensatorNode(source, model);
		else if (StaticVarCompensator.class.isInstance(source)) result = new StaticVarCompensatorNode(source, model);
		else if (StationSupply.class.isInstance(source)) result = new StationSupplyNode(source, model);
		else if (Substation.class.isInstance(source)) result = new SubstationNode(source, model);
		else if (SynchronousMachine.class.isInstance(source)) result = new SynchronousMachineNode(source, model);
		else if (Terminal.class.isInstance(source)) result = new TerminalNode(source, model);
		else if (TransformerWinding.class.isInstance(source)) result = new TransformerWindingNode(source, model);
		else if (VoltageLevel.class.isInstance(source)) result = new VoltageLevelNode(source, model);

		return result;
	}
	
	public static int initialize() {
		int initializedModel = ++model;
		
		Set<String> rdfIds = Rdf.getAllIds();
		for (String rdfId : rdfIds) {
			create(Rdf.findById(rdfId), initializedModel);
		}
		
		return initializedModel;
	}
	
	public static int getCurrentModel() {
		return model;
	}
	
	public static void connect(int model) {
		Set<String> terminalNodeIds = TerminalNode.getTerminalNodeIds(model);
		for(String terminalNodeId : terminalNodeIds) {
			TerminalNode node = TerminalNode.findByTerminalNodeId(terminalNodeId, model);
			node.connect();
		}
	}

	public static Map<String,SubstationNode> findLineSubstations(LineNode line, int model, boolean alreadyReset) {
		Map<String,SubstationNode> result = new HashMap<String,SubstationNode>();
		
		Map<String,ACLineSegmentNode> lineSegments = line.getLineSegments();
		for (String acLineSegmentId : lineSegments.keySet()) {
			ACLineSegmentNode acLineSegment = lineSegments.get(acLineSegmentId);
			for (TerminalNode terminal : acLineSegment.getTerminals()) {
				SubstationNode substation = findSubstation(terminal, acLineSegment, true, alreadyReset);
				if (substation != null) {
					if (!result.containsKey(substation.getId())) result.put(substation.getId(),substation);
				}
			}
		}
		
		return result;
	}
	
	public static SubstationNode findSubstation(TerminalNode startNode, ACLineSegmentNode oragin, boolean assumeConnected, boolean alreadyReset) {
		SubstationNode result = null;

		Queue<RdfNode> visitQueue = new LinkedList<RdfNode>();
		if (!alreadyReset) reset(startNode.getModel(), true, false, false, false, false);
		
		log.trace("Mark oragin as visited");
		oragin.setVisited(true);
		log.trace("Mark terminal as visited and enqueue: " + oragin.toIdentifier());
		startNode.setVisited(true);
		visitQueue.add(startNode);
		log.trace("Begin processing...");
		while (!visitQueue.isEmpty()) {
			RdfNode node = visitQueue.poll();
			log.trace("Evaluate node: " + node.toIdentifier());
			// check to see if we're done
			result = node.getSubstation();
			if (result != null) {
				break;
			}
			
			// enqueue next items and go
			if (TerminalNode.class.isInstance(node)) {
				TerminalNode terminal = (TerminalNode)node;
				RdfNode next = terminal.getConductingEquipment();
				if (next != null && !next.isVisited()) {
					log.trace("Enqueue: " + next.toIdentifier());
					next.setVisited(true);
					visitQueue.add(next);
				}
				next = terminal.getConnectivityNode();
				if (next != null && !next.isVisited()) {
					log.trace("Enqueue: " + next.toIdentifier());
					next.setVisited(true);
					visitQueue.add(next);
				}
			} else if (ConnectivityElementNode.class.isInstance(node)) {
				ConnectivityElementNode connectivityNode = (ConnectivityElementNode)node;
				for (TerminalNode terminal : connectivityNode.getTerminals()) {
					if (!terminal.isVisited()) {
						log.trace("Enqueue: " + terminal.toIdentifier());
						terminal.setVisited(true);
						visitQueue.add(terminal);
					}
				}
			} else if (OutageResourceNode.class.isInstance(node)) {
				OutageResourceNode outageResource = (OutageResourceNode)node;
				if (assumeConnected || !outageResource.isOutage()) {
					if (PowerTransformerNode.class.isInstance(node)) {
						PowerTransformerNode powerTransformer = (PowerTransformerNode)node;
						for (TransformerWindingNode transformerWinding : powerTransformer.getTransformerWindings()) {
							if (!transformerWinding.isVisited()) {
								log.trace("Enqueue: " + transformerWinding.toIdentifier());
								transformerWinding.setVisited(true);
								visitQueue.add(transformerWinding);
							}
						}
					} else if (TransformerWindingNode.class.isInstance(node)) {
						TransformerWindingNode transformerWinding = (TransformerWindingNode)node;
						RdfNode next = transformerWinding.getPowerTransformer();
						if (next != null && !next.isVisited()) {
							log.trace("Enqueue: " + next.toIdentifier());
							next.setVisited(true);
							visitQueue.add(next);
						}
						for (TerminalNode terminal : transformerWinding.getTerminals()) {
							if (!terminal.isVisited()) {
								log.trace("Enqueue: " + terminal.toIdentifier());
								terminal.setVisited(true);
								visitQueue.add(terminal);
							}
						}
					} else if (ConductingEquipmentNode.class.isInstance(node)) {
						ConductingEquipmentNode conductingEquipment = (ConductingEquipmentNode)node;
						for (TerminalNode terminal : conductingEquipment.getTerminals()) {
							if (!terminal.isVisited()) {
								log.trace("Enqueue: " + terminal.toIdentifier());
								terminal.setVisited(true);
								visitQueue.add(terminal);
							}
						}
					}
				}
			}
		}
		
		resetFrom(oragin, true, false, false, false, false);

		return result;
	}

	// TODO: Convert to a breadth first implementation
	public static int mapIslands(int model, boolean assumeConnected) {
		int island = 0;
		
		Set<String> rdfIds = Rdf.getAllIds();
		for(String rdfId : rdfIds) {
			RdfNode node = RdfNode.findByNodeId(rdfId, model);
			if (node.isConnectedElement() && !node.isVisited()) {
				island++;
				Queue<RdfNode> queue = new LinkedList<RdfNode>();
				queue.add(node);
				while (!queue.isEmpty()) {
					RdfNode queueNode = queue.poll();
					if (!queueNode.isVisited()) queueNode.setIsland(island, queue, assumeConnected);
				}
			}
		}

		return island;
	}
	
	public static List<RdfNode> findShortestPath(TerminalNode startNode, TerminalNode endNode, RdfNode oragin, boolean assumeConnected, boolean alreadyReset) {
		List<RdfNode> result = null;
		
		Queue<RdfNode> visitQueue = new LinkedList<RdfNode>();
		if (!alreadyReset) reset(startNode.getModel(), true, false, true, false, false);
		
		log.trace("Mark oragin as visited: " + oragin.toIdentifier());
		oragin.setVisited(true);
		log.trace("Mark start node as visited and enqueue: " + startNode.toIdentifier());
		startNode.setVisited(true);
		visitQueue.add(startNode);
		log.trace("Begin processing...");
		boolean found = false;
		while (!visitQueue.isEmpty()) {
			RdfNode node = visitQueue.poll();
			log.trace("Evaluate node: " + node.toIdentifier());
			// check to see if we're done
			if (endNode.getId().equals(node.getId())) {
				log.trace("Matched target node...");
				found = true;
				break;
			}
			// enqueue next items and go
			if (TerminalNode.class.isInstance(node)) {
				TerminalNode terminal = (TerminalNode)node;
				RdfNode next = terminal.getConductingEquipment();
				if (next != null && !next.isVisited()) {
					log.trace("Enqueue: " + next.toIdentifier());
					next.setVisited(true);
					next.setPath(node);
					visitQueue.add(next);
				}
				next = terminal.getConnectivityNode();
				if (next != null && !next.isVisited()) {
					log.trace("Enqueue: " + next.toIdentifier());
					next.setVisited(true);
					next.setPath(node);
					visitQueue.add(next);
				}
			} else if (ConnectivityElementNode.class.isInstance(node)) {
				ConnectivityElementNode connectivityNode = (ConnectivityElementNode)node;
				for (TerminalNode terminal : connectivityNode.getTerminals()) {
					if (!terminal.isVisited()) {
						log.trace("Enqueue: " + terminal.toIdentifier());
						terminal.setVisited(true);
						terminal.setPath(node);
						visitQueue.add(terminal);
					}
				}
			} else if (OutageResourceNode.class.isInstance(node)) {
				OutageResourceNode outageResource = (OutageResourceNode)node;
				if (assumeConnected || !outageResource.isOutage()) {
					if (PowerTransformerNode.class.isInstance(node)) {
						PowerTransformerNode powerTransformer = (PowerTransformerNode)node;
						for (TransformerWindingNode transformerWinding : powerTransformer.getTransformerWindings()) {
							if (!transformerWinding.isVisited()) {
								log.trace("Enqueue: " + transformerWinding.toIdentifier());
								transformerWinding.setVisited(true);
								transformerWinding.setPath(node);
								visitQueue.add(transformerWinding);
							}
						}
					} else if (TransformerWindingNode.class.isInstance(node)) {
						TransformerWindingNode transformerWinding = (TransformerWindingNode)node;
						RdfNode next = transformerWinding.getPowerTransformer();
						if (next != null && !next.isVisited()) {
							log.trace("Enqueue: " + next.toIdentifier());
							next.setVisited(true);
							next.setPath(node);
							visitQueue.add(next);
						}
						for (TerminalNode terminal : transformerWinding.getTerminals()) {
							if (!terminal.isVisited()) {
								log.trace("Enqueue: " + terminal.toIdentifier());
								terminal.setVisited(true);
								terminal.setPath(node);
								visitQueue.add(terminal);
							}
						}
					} else if (ConductingEquipmentNode.class.isInstance(node)) {
						ConductingEquipmentNode conductingEquipment = (ConductingEquipmentNode)node;
						for (TerminalNode terminal : conductingEquipment.getTerminals()) {
							if (!terminal.isVisited()) {
								log.trace("Enqueue: " + terminal.toIdentifier());
								terminal.setVisited(true);
								terminal.setPath(node);
								visitQueue.add(terminal);
							}
						}
					}
				}
			}
		}
		if (found) {
			// process from endNode to startNode to construct the path
			result = new LinkedList<RdfNode>();
			RdfNode node = endNode;
			while (node != null) {
				result.add(0, node);;
				node = node.getPath();
			}
		}
		
		resetFrom(oragin, true, false, true, false, false);
		
		return result;
	}
	
	public static void listEquipment(RdfNode oragin, HashSet<String> terminalList, boolean alreadyReset) {
		Queue<RdfNode> visitQueue = new LinkedList<RdfNode>();
		if (!alreadyReset) reset(oragin.getModel(), true, false, false, false, false);
		
		log.info("List Equipment");
		log.info("--------------");
		log.trace("Mark oragin as visited and enqueue: " + oragin.toIdentifier());
		oragin.setVisited(true);
		visitQueue.add(oragin);
		log.trace("Begin processing...");
		while (!visitQueue.isEmpty()) {
			RdfNode node = visitQueue.poll();
			log.info(node.toIdentifier());

			if (!terminalList.contains(node.getId())) {
				// enqueue the next items and go
				if (TerminalNode.class.isInstance(node)) {
					TerminalNode terminal = (TerminalNode)node;
					RdfNode next = terminal.getConductingEquipment();
					if (next != null && !next.isVisited()) {
						log.trace("Enqueue: " + next.toIdentifier());
						next.setVisited(true);
						visitQueue.add(next);
					}
					next = terminal.getConnectivityNode();
					if (next != null && !next.isVisited()) {
						log.trace("Enqueue: " + next.toIdentifier());
						next.setVisited(true);
						visitQueue.add(next);
					}
				} else if (ConnectivityElementNode.class.isInstance(node)) {
					ConnectivityElementNode connectivityNode = (ConnectivityElementNode)node;
					for (TerminalNode terminal : connectivityNode.getTerminals()) {
						if (!terminal.isVisited()) {
							log.trace("Enqueue: " + terminal.toIdentifier());
							terminal.setVisited(true);
							visitQueue.add(terminal);
						}
					}
				} else if (PowerTransformerNode.class.isInstance(node)) {
					PowerTransformerNode powerTransformer = (PowerTransformerNode)node;
					for (TransformerWindingNode transformerWinding : powerTransformer.getTransformerWindings()) {
						if (!transformerWinding.isVisited()) {
							log.trace("Enqueue: " + transformerWinding.toIdentifier());
							transformerWinding.setVisited(true);
							visitQueue.add(transformerWinding);
						}
					}
				} else if (TransformerWindingNode.class.isInstance(node)) {
					TransformerWindingNode transformerWinding = (TransformerWindingNode)node;
					RdfNode next = transformerWinding.getPowerTransformer();
					if (next != null && !next.isVisited()) {
						log.trace("Enqueue: " + next.toIdentifier());
						next.setVisited(true);
						visitQueue.add(next);
					}
					for (TerminalNode terminal : transformerWinding.getTerminals()) {
						if (!terminal.isVisited()) {
							log.trace("Enqueue: " + terminal.toIdentifier());
							terminal.setVisited(true);
							visitQueue.add(terminal);
						}
					}
				} else if (ConductingEquipmentNode.class.isInstance(node)) {
					ConductingEquipmentNode conductingEquipment = (ConductingEquipmentNode)node;
					for (TerminalNode terminal : conductingEquipment.getTerminals()) {
						if (!terminal.isVisited()) {
							log.trace("Enqueue: " + terminal.toIdentifier());
							terminal.setVisited(true);
							visitQueue.add(terminal);
						}
					}
				}
			}
		}
		
		resetFrom(oragin, true, false, false, false, false);
	}
	
	public static class SourceSinkRecord {
		private RdfNode source = null;
		private RdfNode sink = null;
		
		public void setSource(RdfNode source) {
			this.source = source;
		}
		
		public RdfNode getSource() {
			return source;
		}
		
		public boolean hasSource() {
			return (source != null);
		}
		
		public void setSink(RdfNode sink) {
			this.sink = sink;
		}
		
		public RdfNode getSink() {
			return sink;
		}
		
		public boolean hasSink() {
			return (sink != null);
		}
	}
	
	public static SourceSinkRecord findSourceAndSink(TerminalNode startNode, OutageResourceNode oragin, boolean assumeConnected, boolean alreadyReset) {
		SourceSinkRecord result = new SourceSinkRecord();
		
		Queue<RdfNode> visitQueue = new LinkedList<RdfNode>();
		if (!alreadyReset) reset(startNode.getModel(), true, false, false, false, false);
		
		log.trace("Mark oragin as visited");
		oragin.setVisited(true);
		log.trace("Mark terminal as visited and enqueue: " + oragin.toIdentifier());
		startNode.setVisited(true);
		visitQueue.add(startNode);
		log.trace("Begin processing...");
		while (!visitQueue.isEmpty()) {
			RdfNode node = visitQueue.poll();
			log.trace("Evaluate node: " + node.toIdentifier());
			// check to see if we're done
			if (CustomerLoadNode.class.isInstance(node)) result.setSink(node);
			else if (SynchronousMachineNode.class.isInstance(node) || (TransformerWindingNode.class.isInstance(oragin) && (ShuntCompensatorNode.class.isInstance(node) || StaticVarCompensatorNode.class.isInstance(node) || EndCapNode.class.isInstance(node)))) result.setSource(node);
			if (result.hasSource() && result.hasSink()) {
				break;
			}
			
			// enqueue next items and go
			if (TerminalNode.class.isInstance(node)) {
				TerminalNode terminal = (TerminalNode)node;
				RdfNode next = terminal.getConductingEquipment();
				if (next != null && !next.isVisited()) {
					log.trace("Enqueue: " + next.toIdentifier());
					next.setVisited(true);
					visitQueue.add(next);
				}
				next = terminal.getConnectivityNode();
				if (next != null && !next.isVisited()) {
					log.trace("Enqueue: " + next.toIdentifier());
					next.setVisited(true);
					visitQueue.add(next);
				}
			} else if (ConnectivityElementNode.class.isInstance(node)) {
				ConnectivityElementNode connectivityNode = (ConnectivityElementNode)node;
				for (TerminalNode terminal : connectivityNode.getTerminals()) {
					if (!terminal.isVisited()) {
						log.trace("Enqueue: " + terminal.toIdentifier());
						terminal.setVisited(true);
						visitQueue.add(terminal);
					}
				}
			} else if (OutageResourceNode.class.isInstance(node)) {
				OutageResourceNode outageResource = (OutageResourceNode)node;
				if (assumeConnected || !outageResource.isOutage()) {
					if (PowerTransformerNode.class.isInstance(node)) {
						PowerTransformerNode powerTransformer = (PowerTransformerNode)node;
						for (TransformerWindingNode transformerWinding : powerTransformer.getTransformerWindings()) {
							if (!transformerWinding.isVisited()) {
								log.trace("Enqueue: " + transformerWinding.toIdentifier());
								transformerWinding.setVisited(true);
								visitQueue.add(transformerWinding);
							}
						}
					} else if (TransformerWindingNode.class.isInstance(node)) {
						TransformerWindingNode transformerWinding = (TransformerWindingNode)node;
						RdfNode next = transformerWinding.getPowerTransformer();
						if (next != null && !next.isVisited()) {
							log.trace("Enqueue: " + next.toIdentifier());
							next.setVisited(true);
							visitQueue.add(next);
						}
						for (TerminalNode terminal : transformerWinding.getTerminals()) {
							if (!terminal.isVisited()) {
								log.trace("Enqueue: " + terminal.toIdentifier());
								terminal.setVisited(true);
								visitQueue.add(terminal);
							}
						}
					} else if (ConductingEquipmentNode.class.isInstance(node)) {
						ConductingEquipmentNode conductingEquipment = (ConductingEquipmentNode)node;
						for (TerminalNode terminal : conductingEquipment.getTerminals()) {
							if (!terminal.isVisited()) {
								log.trace("Enqueue: " + terminal.toIdentifier());
								terminal.setVisited(true);
								visitQueue.add(terminal);
							}
						}
					}
				}
			}
		}
		
		resetFrom(oragin, true, false, false, false, false);
		
		return result;
	}

	public static void setOutages(int outragesModel, HashSet<String> equipmentIds) {
		for(String rdfId : equipmentIds) {
			OutageResourceNode outageResource = (OutageResourceNode)RdfNode.findByNodeId(rdfId, outragesModel);
			outageResource.setOutage(true);
		}
	}
	
	public static void reset(int resetModel, boolean visitors, boolean islands, boolean path, boolean outages, boolean openSwitches) {
		Set<String> rdfIds = Rdf.getAllIds();
		for(String rdfId : rdfIds) {
			RdfNode node = RdfNode.findByNodeId(rdfId, resetModel);
			if (visitors) node.setVisited(false);
			if (islands) node.setIsland(0);
			if (path) node.setPath(null);
			if (outages && OutageResourceNode.class.isInstance(node)) {
				OutageResourceNode outageNode = (OutageResourceNode)node;
				outageNode.resetOutage();
			}
			if (openSwitches && SwitchNode.class.isInstance(node)) {
				SwitchNode switchNode = (SwitchNode)node;
				switchNode.setOpen(true);
			}
		}
	}
	
	public static void resetNode(RdfNode node, boolean visitors, boolean islands, boolean path, boolean outages, boolean openSwitches) {
		if (visitors) node.setVisited(false);
		if (islands) node.setIsland(0);
		if (path) node.setPath(null);
		if (outages && OutageResourceNode.class.isInstance(node)) {
			OutageResourceNode outageNode = (OutageResourceNode)node;
			outageNode.resetOutage();
		}
		if (openSwitches && SwitchNode.class.isInstance(node)) {
			SwitchNode switchNode = (SwitchNode)node;
			switchNode.setOpen(true);
		}
	}
	
	public static void resetFrom(RdfNode oragin,  boolean visitors, boolean islands, boolean path, boolean outages, boolean openSwitches) {
		Queue<RdfNode> visitQueue = new LinkedList<RdfNode>();

		resetNode(oragin, visitors, islands, path, outages, openSwitches);
		visitQueue.add(oragin);
		while (!visitQueue.isEmpty()) {
			RdfNode node = visitQueue.poll();
			// enqueue next items and go
			if (TerminalNode.class.isInstance(node)) {
				TerminalNode terminal = (TerminalNode)node;
				RdfNode next = terminal.getConductingEquipment();
				if (next != null && next.isVisited()) {
					resetNode(next, visitors, islands, path, outages, openSwitches);
					visitQueue.add(next);
				}
				next = terminal.getConnectivityNode();
				if (next != null && next.isVisited()) {
					resetNode(next, visitors, islands, path, outages, openSwitches);
					visitQueue.add(next);
				}
			} else if (ConnectivityElementNode.class.isInstance(node)) {
				ConnectivityElementNode connectivityNode = (ConnectivityElementNode)node;
				for (TerminalNode terminal : connectivityNode.getTerminals()) {
					if (terminal.isVisited()) {
						resetNode(terminal, visitors, islands, path, outages, openSwitches);
						visitQueue.add(terminal);
					}
				}
			} else if (PowerTransformerNode.class.isInstance(node)) {
				PowerTransformerNode powerTransformer = (PowerTransformerNode)node;
				for (TransformerWindingNode transformerWinding : powerTransformer.getTransformerWindings()) {
					if (transformerWinding.isVisited()) {
						resetNode(transformerWinding, visitors, islands, path, outages, openSwitches);
						visitQueue.add(transformerWinding);
					}
				}
			} else if (TransformerWindingNode.class.isInstance(node)) {
				TransformerWindingNode transformerWinding = (TransformerWindingNode)node;
				RdfNode next = transformerWinding.getPowerTransformer();
				if (next != null && next.isVisited()) {
					resetNode(next, visitors, islands, path, outages, openSwitches);
					visitQueue.add(next);
				}
				for (TerminalNode terminal : transformerWinding.getTerminals()) {
					if (terminal.isVisited()) {
						resetNode(terminal, visitors, islands, path, outages, openSwitches);
						visitQueue.add(terminal);
					}
				}
			} else if (ConductingEquipmentNode.class.isInstance(node)) {
				ConductingEquipmentNode conductingEquipment = (ConductingEquipmentNode)node;
				for (TerminalNode terminal : conductingEquipment.getTerminals()) {
					if (terminal.isVisited()) {
						resetNode(terminal, visitors, islands, path, outages, openSwitches);
						visitQueue.add(terminal);
					}
				}
			}
		}
	}
	
	public static void identifySubstations(ACLineSegmentNode acLineSegment, boolean alreadyReset) {
		for (TerminalNode terminal : acLineSegment.getTerminals()) {
			SubstationNode substation = findSubstation(terminal, acLineSegment, true, alreadyReset);
			if (substation != null) {
				if (acLineSegment.getFromStation() == null ) acLineSegment.setFromStation(substation);
				else acLineSegment.setToStation(substation);
			}
		}
	}
}
