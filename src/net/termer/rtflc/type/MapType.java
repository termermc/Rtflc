package net.termer.rtflc.type;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.termer.rtflc.runtime.RuntimeException;
import net.termer.rtflc.runtime.Scope;

public class MapType implements RtflType {
	private ConcurrentHashMap<String, RtflType> _map = new ConcurrentHashMap<String, RtflType>();
	
	public MapType() {
		// No arguments
	}
	public MapType(Map<String, RtflType> map) {
		_map.putAll(map);
	}
	
	public String name() {
		return "MAP";
	}
	public Object value() {
		return _map;
	}
	public boolean equals(RtflType value, Scope scope) throws RuntimeException {
		return false;
	}
	
	public String toString() {
		return _map.toString();
	}
}
