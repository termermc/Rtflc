package net.termer.rtflc.type.assignment;

import java.util.concurrent.ConcurrentHashMap;

import net.termer.rtflc.runtime.RuntimeException;
import net.termer.rtflc.runtime.Scope;
import net.termer.rtflc.type.MapType;
import net.termer.rtflc.type.NullType;
import net.termer.rtflc.type.RtflType;

public class MapFieldAssignment implements AssignmentType {
	private RtflType _map = null;
	private String _field = null;
	
	public MapFieldAssignment(RtflType map, String field) {
		_map = map;
		_field = field;
	}
	
	public String name() {
		return "MAP_FIELD";
	}
	public Object value() {
		return null;
	}
	
	public RtflType map() {
		return _map;
	}
	public String field() {
		return _field;
	}
	
	public boolean equals(RtflType value, Scope scope) throws RuntimeException {
		return extractValue(scope).equals(value);
	}

	public RtflType extractValue(Scope scope) throws RuntimeException {
		RtflType mp = _map instanceof AssignmentType ? ((AssignmentType) _map).extractValue(scope) : _map;
		RtflType res = null;
		
		if(mp instanceof MapType) {
				@SuppressWarnings("unchecked")
				ConcurrentHashMap<String, RtflType> map = (ConcurrentHashMap<String, RtflType>) mp.value();
				
				res = map.get(_field);
				res = res == null ? new NullType() : res;
		} else {
			throw new RuntimeException("Cannot get field of non-map value");
		}
		
		return res;
	}
	
	public String toString() {
		return _map+"->"+_field;
	}
}
