package net.termer.rtflc.type;

import net.termer.rtflc.runtime.RuntimeException;
import net.termer.rtflc.runtime.Scope;
import net.termer.rtflc.type.assignment.AssignmentType;

public class DoubleType implements NumberType {
	private double val = 0.0;
	
	public DoubleType(double dbl) {
		val = dbl;
	}
	
	public String name() {
		return "DOUBLE";
	}
	public Object value() {
		return val;
	}
	
	public String toString() {
		return Double.toString(val);
	}

	public int toInt() {
		return (int) val;
	}
	public double toDouble() {
		return val;
	}
	
	public boolean equals(RtflType value, Scope scope) throws RuntimeException {
		boolean eq = false;
		
		RtflType v = value instanceof AssignmentType ? ((AssignmentType) value).extractValue(scope) : value;
		if(value instanceof NumberType) {
			eq = ((NumberType)v).toDouble() == val;
		}
		
		return eq;
	}
}
