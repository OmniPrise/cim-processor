package com.omniprise.cim.graph;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.omniprise.cim.model.Rdf;
import com.omniprise.cim.model.Terminal;

public class TerminalNode extends RdfNode {
	private static Logger log = LoggerFactory.getLogger(TerminalNode.class); 
	private static Map<Integer,Map<String,TerminalNode>> modelTerminalNodes = new HashMap<Integer,Map<String,TerminalNode>>();

	private ConnectivityElementNode connectivityNode;
	private ConductingEquipmentNode conductingEquipment;

	public TerminalNode(Rdf source, int model) {
		super(source, model);
		Map<String,TerminalNode> terminalNodes = modelTerminalNodes.get(model);
		if (terminalNodes == null) {
			terminalNodes = new HashMap<String,TerminalNode>();
			modelTerminalNodes.put(model, terminalNodes);
		}
		terminalNodes.put(source.getId(), this);
		log.debug("Created TerminalNode");
	}
	
	public ConnectivityElementNode getConnectivityNode() {
		return connectivityNode;
	}

	public void setConnectivityNode(ConnectivityElementNode connectivityNode) {
		this.connectivityNode = connectivityNode;
	}

	public ConductingEquipmentNode getConductingEquipment() {
		return conductingEquipment;
	}

	public void setConductingEquipment(ConductingEquipmentNode conductingEquipment) {
		this.conductingEquipment = conductingEquipment;
	}
	
	public static Set<String> getTerminalNodeIds(int model) {
		Set<String> result = null;
		Map<String,TerminalNode> terminalNodes = modelTerminalNodes.get(model);
		if (terminalNodes != null) result = terminalNodes.keySet(); 
		return result;
	}
	
	public static TerminalNode findByTerminalNodeId(String terminalNodeId, int model) {
		TerminalNode result = null;
		Map<String,TerminalNode> terminalNodes = modelTerminalNodes.get(model);
		if (terminalNodes != null) result = terminalNodes.get(terminalNodeId);
		return result;
	}
	
	@Override
	public void connect() {
		super.connect();
		Terminal terminal = (Terminal)getSource();
		if (connectivityNode == null) {
			connectivityNode = (ConnectivityElementNode)RdfNode.findByNodeId(terminal.getConnectivityNode(),getModel());
			if (connectivityNode != null) {
				if (!connectivityNode.isConnectedElement()) log.info("ConnectivityElementNode not identified as connected element");
				connectivityNode.addTerminal(this);
				setConnected(true);
			} else {
				log.debug("ConnectivityNode " + terminal.getConnectivityNode() + " not found");
			}
		}
		if (conductingEquipment == null) {
			conductingEquipment = (ConductingEquipmentNode)RdfNode.findByNodeId(terminal.getConductingEquipment(),getModel());
			if (conductingEquipment != null) {
				if (!conductingEquipment.isConnectedElement()) log.info("ConductingEquipmentNode not identified as connected element");
				conductingEquipment.addTerminal(this);
				setConnected(true);
			} else {
				log.debug("ConductingEquipment " + terminal.getConductingEquipment() + " not found");
			}
		}
	}
	
	@Override
	public void disconnect() {
		if (connectivityNode != null) {
			connectivityNode.removeTerminal(this);
		}
		if (conductingEquipment != null) {
			conductingEquipment.removeTerminal(this);
		}
		super.disconnect();
		connectivityNode = null;
		conductingEquipment = null;
	}

	@Override
	public boolean isConnectedElement() {
		return true;
	}
	
	@Override
	public void setIsland(int island, Queue<RdfNode> queue, boolean assumeConnected) {
		if (!isVisited()) {
			super.setIsland(island, queue, assumeConnected);
			if (connectivityNode != null) queue.add(connectivityNode);
			if (conductingEquipment != null) queue.add(conductingEquipment);
		}
	}

	@Override
	public String getDisplayName() { return "Terminal"; }

	@Override
	public String toDiagram() { 
		String result = "";
		
		if (conductingEquipment != null && connectivityNode != null) result = "\t\"" + conductingEquipment.getDiagramName() + "\" -- \"" + connectivityNode.getDiagramName() + "\";";
		return result; 
	}
	
	@Override
	public String toString() { 
		String result = super.toString();
		Terminal terminal = (Terminal)getSource();
		//RdfNode connectivityNode = RdfNode.findByNodeId(terminal.getConnectivityNode(), getModel());
		//RdfNode conductingEquipment = RdfNode.findByNodeId(terminal.getConductingEquipment(), getModel());
		if (getConnectivityNode() == null) {
			result += ",ConnectivityNode," + terminal.getConnectivityNode() + "-NotPresentInRdfModel";
		} else {
			result += "," + getConnectivityNode().toIdentifier();
		}
		if (getConductingEquipment() == null) {
			result += ",ConductingEquipment," + terminal.getConductingEquipment() + "-NotPresentInRdfModel";
		} else {
			result += "," + getConductingEquipment().toIdentifier();
		}
		return result;
	}
}
