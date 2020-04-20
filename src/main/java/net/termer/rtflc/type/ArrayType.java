package net.termer.rtflc.type;

import java.util.ArrayList;

import net.termer.rtflc.runtime.RuntimeException;
import net.termer.rtflc.runtime.Scope;
import net.termer.rtflc.type.assignment.AssignmentType;

public class ArrayType implements RtflType {
	private ArrayList<RtflType> arr = new ArrayList<RtflType>();
	
	public ArrayType() {
		// Nothing to do :DDDDD
	}
	public ArrayType(RtflType[] vals) {
		for(RtflType val : vals)
			arr.add(val);
	}
	public ArrayType(ArrayList<RtflType> vals) {
		arr.addAll(vals);
	}
	
	public String name() {
		return "ARRAY";
	}
	public Object value() {
		return arr;
	}
	public boolean equals(RtflType value, Scope scope) throws RuntimeException {
		boolean eq = false;
		
		RtflType val = resolveVal(value, scope);
		if(val instanceof ArrayType) {
			ArrayType varr = (ArrayType) val;
			if(varr.arr.size() == arr.size() ) {
				// Compare elements of both arrays
				for(int i = 0; i < arr.size(); i++) {
					if(varr.arr.get(i).equals(arr.get(i), scope)) {
						eq = true; 
					} else {
						eq = false;
						break;
					}
				}
			}
		}
		
		return eq;
	}
	private RtflType resolveVal(RtflType val, Scope scope) throws RuntimeException {
		return val instanceof AssignmentType ? ((AssignmentType) val).extractValue(scope) : val;
	}
	
	public String toString() {
		return arr.toString();
	}
}
