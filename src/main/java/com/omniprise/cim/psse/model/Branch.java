package com.omniprise.cim.psse.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Branch extends PsseElement {
	private static Logger log = LoggerFactory.getLogger(Branch.class); 
	
	public static void initialize(int model) {
	}

	private boolean zeroImpedanceBranch;
	private Bus fromBus;
	private Bus toBus;

	public Branch(int model) {
		super(model);
		log.debug("Created Branch instance");
	}
	
	public boolean isZeroImpedanceBranch() {
		return zeroImpedanceBranch;
	}

	public void setZeroImpedanceBranch(boolean zeroImpedanceBranch) {
		this.zeroImpedanceBranch = zeroImpedanceBranch;
	}

	public Bus getFromBus() {
		return fromBus;
	}

	public void setFromBus(Bus fromBus) {
		this.fromBus = fromBus;
	}

	public Bus getToBus() {
		return toBus;
	}

	public void setToBus(Bus toBus) {
		this.toBus = toBus;
	}
}
