package com.omniprise.cim.model;

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

public class RdfModel {
	private static Logger log = LoggerFactory.getLogger(RdfModel.class); 

	public static Rdf create(String elementName, String id) {
		Rdf result = null;
		
		if (elementName.equals("cim:ACLineSegment")) result = new ACLineSegment();
		else if (elementName.equals("cim:Analog")) result = new Analog();
		else if (elementName.equals("cim:BaseVoltage")) result = new BaseVoltage();
		else if (elementName.equals("cim:Bay")) result = new Bay();
		else if (elementName.equals("cim:Breaker")) result = new Breaker();
		else if (elementName.equals("cim:BusbarSection")) result = new BusbarSection();
		else if (elementName.equals("cim:ConnectivityNode")) result = new ConnectivityNode();
		else if (elementName.equals("etx:ConnectivityNodeGroup")) result = new ConnectivityNodeGroup();
		else if (elementName.equals("cim:CustomerLoad")) result = new CustomerLoad();
		else if (elementName.equals("cim:Disconnector")) result = new Disconnector();
		else if (elementName.equals("cim:Discrete")) result = new Discrete();
		else if (elementName.equals("etx:EndCap")) result = new EndCap();
		else if (elementName.equals("cim:GroundDisconnector")) result = new GroundDisconnector();
		else if (elementName.equals("cim:Line")) result = new Line();
		else if (elementName.equals("cim:PowerTransformer")) result = new PowerTransformer();
		else if (elementName.equals("cim:SeriesCompensator")) result = new SeriesCompensator();
		else if (elementName.equals("cim:ShuntCompensator")) result = new ShuntCompensator();
		else if (elementName.equals("cim:StaticVarCompensator")) result = new StaticVarCompensator();
		else if (elementName.equals("cim:StationSupply")) result = new StationSupply();
		else if (elementName.equals("cim:Substation")) result = new Substation();
		else if (elementName.equals("cim:SynchronousMachine")) result = new SynchronousMachine();
		else if (elementName.equals("cim:Terminal")) result = new Terminal();
		else if (elementName.equals("cim:TransformerWinding")) result = new TransformerWinding();
		else if (elementName.equals("cim:VoltageLevel")) result = new VoltageLevel();
		
		if (result != null) {
			result.setId(id);
		}
		
		return result;
	}
	
	public static Rdf popRdf(Rdf rdf, String elementName) {
		Rdf result = rdf;

		if (elementName.equals("cim:ACLineSegment") && ACLineSegment.class.getName().equals(rdf.getClass().getName())) result = null;
		else if (elementName.equals("cim:Analog") && Analog.class.getName().equals(rdf.getClass().getName())) result = null;
		else if (elementName.equals("cim:BaseVoltage") && BaseVoltage.class.getName().equals(rdf.getClass().getName())) result = null;
		else if (elementName.equals("cim:Breaker") &&  Breaker.class.getName().equals(rdf.getClass().getName())) result = null;
		else if (elementName.equals("cim:BusbarSection") &&  BusbarSection.class.getName().equals(rdf.getClass().getName())) result = null;
		else if (elementName.equals("cim:ConnectivityNode") && ConnectivityNode.class.getName().equals(rdf.getClass().getName())) result = null;
		else if (elementName.equals("cim:CustomerLoad") && CustomerLoad.class.getName().equals(rdf.getClass().getName())) result = null;
		else if (elementName.equals("cim:Disconnector") && Disconnector.class.getName().equals(rdf.getClass().getName())) result = null;
		else if (elementName.equals("cim:Discrete") && Discrete.class.getName().equals(rdf.getClass().getName())) result = null;
		else if (elementName.equals("etx:EndCap") && EndCap.class.getName().equals(rdf.getClass().getName())) result = null;
		else if (elementName.equals("cim:GroundDisconnector") && GroundDisconnector.class.getName().equals(rdf.getClass().getName())) result = null;
		else if (elementName.equals("cim:Line") && Line.class.getName().equals(rdf.getClass().getName())) result = null;
		else if (elementName.equals("cim:PowerTransformer") && PowerTransformer.class.getName().equals(rdf.getClass().getName())) result = null;
		else if (elementName.equals("cim:SeriesCompensator") && SeriesCompensator.class.getName().equals(rdf.getClass().getName())) result = null;
		else if (elementName.equals("cim:ShuntCompensator") && ShuntCompensator.class.getName().equals(rdf.getClass().getName())) result = null;
		else if (elementName.equals("cim:StaticVarCompensator") && StaticVarCompensator.class.getName().equals(rdf.getClass().getName())) result = null;
		else if (elementName.equals("cim:StationSupply") && StationSupply.class.getName().equals(rdf.getClass().getName())) result = null;
		else if (elementName.equals("cim:Substation") && Substation.class.getName().equals(rdf.getClass().getName())) result = null;
		else if (elementName.equals("cim:SynchronousMachine") && SynchronousMachine.class.getName().equals(rdf.getClass().getName())) result = null;
		else if (elementName.equals("cim:Terminal") && Terminal.class.getName().equals(rdf.getClass().getName())) result = null;
		else if (elementName.equals("cim:TransformerWinding") && TransformerWinding.class.getName().equals(rdf.getClass().getName())) result = null;
		else if (elementName.equals("cim:VoltageLevel") && VoltageLevel.class.getName().equals(rdf.getClass().getName())) result = null;

		return result;
	}
}
