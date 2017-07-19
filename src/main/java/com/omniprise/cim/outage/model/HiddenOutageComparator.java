package com.omniprise.cim.outage.model;

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


import java.util.Comparator;

import com.omniprise.cim.parse.model.Outage;

public class HiddenOutageComparator implements Comparator<Outage> {
	@Override
	public int compare(Outage left, Outage right) {
		int result = 0;
		
		result = Integer.valueOf(left.getTeid()).compareTo(Integer.valueOf(right.getTeid()));
		if (result == 0) result = left.getEffectiveStartHour().compare(right.getEffectiveStartHour());
		if (result == 0) result = left.getEffectiveEndHour().compare(right.getEffectiveEndHour());
		
		return result;
	}
}
