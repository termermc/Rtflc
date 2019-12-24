package net.termer.rtflc.type;

import net.termer.rtflc.runtime.RuntimeException;
import net.termer.rtflc.runtime.Scope;
import net.termer.rtflc.type.assignment.AssignmentType;

public class NullType implements RtflType {
	public String name() {
		return "NULL";
	}
	public Object value() {
		return null;
	}
	public String toString() {
		return "null";
	}
	public boolean equals(RtflType value, Scope scope) throws RuntimeException {
		RtflType val = value instanceof AssignmentType ? ((AssignmentType) value).extractValue(scope) : value;
		return val instanceof NullType;
	}
}