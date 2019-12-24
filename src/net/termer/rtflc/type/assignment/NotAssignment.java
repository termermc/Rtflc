package net.termer.rtflc.type.assignment;

import net.termer.rtflc.runtime.RuntimeException;
import net.termer.rtflc.runtime.Scope;
import net.termer.rtflc.type.BoolType;
import net.termer.rtflc.type.NumberType;
import net.termer.rtflc.type.RtflType;

public class NotAssignment implements AssignmentType {
	private RtflType val = null;
	
	public NotAssignment(RtflType value) {
		val = value;
	}
	
	public String name() {
		return "LOGIC";
	}
	public Object value() {
		return null;
	}
	
	public RtflType originalValue() {
		return val;
	}
	
	public boolean equals(RtflType value, Scope scope) throws RuntimeException {
		return extractValue(scope).equals(value);
	}

	public RtflType extractValue(Scope scope) throws RuntimeException {
		RtflType result = val;
		
		// Extract real value of value if it's an AssignmentType 
		if(result instanceof AssignmentType)
			result = ((AssignmentType) result).extractValue(scope);
		
		if(result instanceof NumberType)
			return new BoolType(!(((NumberType) result).toDouble() > 0));
		else
			return new BoolType(true);
	}

}
