package net.termer.rtflc.type;

import net.termer.rtflc.runtime.RuntimeException;
import net.termer.rtflc.runtime.Scope;
import net.termer.rtflc.type.assignment.AssignmentType;

public class StringType implements RtflType {
	private String str = null;
	
	public StringType(String string) {
		str = string;
	}

	public String name() {
		return "STRING";
	}
	public Object value() {
		return str;
	}
	public String toString() {
		return '"'+str+'"';
	}

	public boolean equals(RtflType value, Scope scope) throws RuntimeException {
		boolean eq = false;
		
		RtflType val = value instanceof AssignmentType ? ((AssignmentType) value).extractValue(scope) : value;
		if(val instanceof StringType) {
			eq = ((StringType) val).value().equals(str);
		}
		
		return eq;
	}
}