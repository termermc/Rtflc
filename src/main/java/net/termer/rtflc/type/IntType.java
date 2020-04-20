package net.termer.rtflc.type;

import net.termer.rtflc.runtime.RuntimeException;
import net.termer.rtflc.runtime.Scope;
import net.termer.rtflc.type.assignment.AssignmentType;

public class IntType implements NumberType {
	private int val = 0;
	
	public IntType(int integer) {
		val = integer;
	}
	
	public String name() {
		return "INT";
	}
	public Object value() {
		return val;
	}
	public String toString() {
		return Integer.toString(val);
	}
	
	public int toInt() {
		return (int) val;
	}
	public double toDouble() {
		return (double) val;
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
