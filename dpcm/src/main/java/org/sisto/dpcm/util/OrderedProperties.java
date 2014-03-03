package org.sisto.dpcm.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;

public class OrderedProperties extends Properties {
	
	private static final long serialVersionUID = 6236932064374519758L;
	private ArrayList<Object> ordered = new ArrayList<Object>();
	
	public OrderedProperties() {
		super();
	}
	
	@Override
	public Enumeration<?> propertyNames() {
		return (Collections.enumeration(this.ordered));
	}
	
	@Override
	public Enumeration<Object> keys() {
		return (Collections.enumeration(this.ordered));
	}
	
	@Override
	public Object put(Object key, Object value) {
		Object ret = null;
		
		this.addObject(key);
		ret  = super.put(key, value);
		
		return ret;
	}
	
	@Override
	public void putAll(Map<?,?> entries) {
		for (Object key : entries.keySet()) {
			if (! containsKey(key)) {
				this.addObject(key);
			}
		}
		super.putAll(entries);
	}
	
	@Override
	public Object remove(Object key) {
		Object ret = null;
		
		removeObject(key);
		ret = super.remove(key);
		
		return ret;
	}
	
	private void addObject(Object key) {
		if (! containsKey(key)) {
			this.ordered.add(key);
		}
	}
	
	private void removeObject(Object key) {
		if (this.ordered.contains(key)) {
			this.ordered.remove(key);
		}
	}
}
