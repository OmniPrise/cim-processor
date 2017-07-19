package com.omniprise.cim.graph;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.omniprise.cim.model.ConnectivityNode;
import com.omniprise.cim.model.Rdf;

public class ConnectivityElementNode extends RdfNode {
	private static Logger log = LoggerFactory.getLogger(ConnectivityElementNode.class); 

	private List<TerminalNode> terminals = new LinkedList<TerminalNode>();
	private ConnectivityNodeGroupNode connectivityNodeGroup = null;
	private String psseBusName;
	private String psseBusNumber;

	public ConnectivityElementNode(Rdf source, int model) {
		super(source, model);
		ConnectivityNode connectivityNode = (ConnectivityNode)source;
		psseBusName = connectivityNode.getPsseBusName();
		psseBusNumber = connectivityNode.getPsseBusNumber();
		log.debug("Created ConnectivityElementNode");
	}

	public ConnectivityNodeGroupNode getConnectivityNodeGroup() {
		return connectivityNodeGroup;
	}

	public void setConnectivityNodeGroup(ConnectivityNodeGroupNode connectivityNodeGroup) {
		this.connectivityNodeGroup = connectivityNodeGroup;
	}

	public String getPsseBusName() {
		return psseBusName;
	}

	public void setPsseBusName(String psseBusName) {
		this.psseBusName = psseBusName;
	}

	public String getPsseBusNumber() {
		return psseBusNumber;
	}

	public void setPsseBusNumber(String psseBusNumber) {
		this.psseBusNumber = psseBusNumber;
	}

	public void addTerminal(TerminalNode terminal) {
		connect();
		terminals.add(terminal);
		setConnected(true);
	}

	public void removeTerminal(TerminalNode terminal) {
		terminals.remove(terminal);
		disconnect();
	}

	public List<TerminalNode> getTerminals() {
		return terminals;
	}
	
	@Override
	public boolean isConnectedElement() {
		return true;
	}

	@Override
	public void connect() {
		super.connect();
		ConnectivityNode connectivityNode = (ConnectivityNode)getSource();
		if (connectivityNodeGroup == null && connectivityNode.getConnectivityNodeGroup() != null) connectivityNodeGroup = (ConnectivityNodeGroupNode)RdfNode.findByNodeId(connectivityNode.getConnectivityNodeGroup(), getModel());
		if (connectivityNodeGroup != null) connectivityNodeGroup.addConnectivityNode(this);
	}

	@Override
	public void disconnect() {
		if (connectivityNodeGroup != null) {
			connectivityNodeGroup.removeConnectivityNode(this);
		}
		super.disconnect();
		connectivityNodeGroup = null;
	}
	
	@Override
	public void setIsland(int island, Queue<RdfNode> queue, boolean assumeConnected) {
		if (!isVisited()) {
			super.setIsland(island, queue, assumeConnected);
			for (TerminalNode node : terminals) {
				queue.add(node);
			}
		}
	}

	@Override
	public String getDiagramType() { return "cn"; }
	
	@Override
	public String getDiagramShape() { return "circle"; }

	@Override
	public String getDisplayName() { return "ConnectivityNode"; }

	@Override
	public String toString() { 
		String result = "";
		ConnectivityNode connectivityNode = (ConnectivityNode)getSource();
		//RdfNode equipmentContainer = RdfNode.findByNodeId(connectivityNode.getEquipmentContainer(), getModel());
		//RdfNode connectivityNodeGroup = RdfNode.findByNodeId(connectivityNode.getConnectivityNodeGroup(), getModel());
		result = super.toString();
		if (getEquipmentContainer() == null) {
			result += ",EquipmentContainer," + connectivityNode.getEquipmentContainer() + "-NotPresentInRdfModel";
		} else {
			result += "," + getEquipmentContainer().toIdentifier();
		}
		if (getConnectivityNodeGroup() == null) {
			result += ",ConnectivityNodeGroup," + connectivityNode.getConnectivityNodeGroup() + "-NotPresentInRdfModel";
		} else {
			result += "," + getConnectivityNodeGroup().toIdentifier();
		}
		for (TerminalNode terminal : terminals) {
			result += "," + terminal.toIdentifier();
		}
		return result;
	}
}
