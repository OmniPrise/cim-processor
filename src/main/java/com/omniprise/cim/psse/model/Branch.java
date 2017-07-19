package com.omniprise.cim.psse.model;

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
