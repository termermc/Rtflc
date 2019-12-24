package net.termer.rtflc.type;

import net.termer.rtflc.runtime.RuntimeException;
import net.termer.rtflc.runtime.Scope;
import net.termer.rtflc.type.assignment.AssignmentType;

public class BoolType implements NumberType {
	private boolean val = false;
	public BoolType(boolean bool) {
		val = bool;
	}
	
	public String name() {
		return "BOOL";
	}
	public Object value() {
		return val;
	}
	public String toString() {
		return Boolean.toString(val);
	}
	
	public int toInt() {
		return val ? 1 : 0;
	}
	public double toDouble() {
		return val ? 1.0 : 0.0;
	}
	
	public boolean equals(RtflType value, Scope scope) throws RuntimeException {
		boolean eq = false;
		
		RtflType v = value instanceof AssignmentType ? ((AssignmentType) value).extractValue(scope) : value;
		if(value instanceof NumberType) {
			eq = ((NumberType)v).toDouble() == toDouble();
		}
		
		return eq;
	}
}
