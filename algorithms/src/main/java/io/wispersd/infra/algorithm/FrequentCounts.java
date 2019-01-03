/*
 * Copyright 2018, Zetyun StreamTau All rights reserved.
 */
package io.wispersd.infra.algorithm;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

public class FrequentCounts<T> {
	private final Map<T, AtomicLong> counting = new HashMap<T, AtomicLong>();
	
	public void addElement(T element) {
		counting.computeIfAbsent(element, (k)->{return new AtomicLong(0);}).incrementAndGet();
	}
	
	
	public void decrementCountAndRemoveZeros() {
		final Set<T> keySetToRemove = new HashSet<T>();
		counting.forEach((k, v) -> {
			long valueAfterDec = v.decrementAndGet();
			if (valueAfterDec == 0) {
				keySetToRemove.add(k);
			}
		});
		for (T key: keySetToRemove) {
			counting.remove(key);
		}
	}
	
	public void merge(FrequentCounts<T> another) {
		another.counting.forEach((key, val) -> {
			AtomicLong itemCtn = counting.computeIfAbsent(key, (k)-> {return new AtomicLong(0);});
			itemCtn.addAndGet(val.longValue());
		});
	}

}
