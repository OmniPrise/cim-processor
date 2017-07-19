package com.omniprise.cim.outage.model;

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
