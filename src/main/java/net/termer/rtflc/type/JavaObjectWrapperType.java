package net.termer.rtflc.type;

import net.termer.rtflc.runtime.RuntimeException;
import net.termer.rtflc.runtime.Scope;
import net.termer.rtflc.type.assignment.AssignmentType;

public class JavaObjectWrapperType implements RtflType {
	private final Object _value;
	
	public JavaObjectWrapperType(Object value) {
		_value = value;
	}
	
	public String name() {
		return "JAVA";
	}
	public Object value() {
		return _value;
	}
	public String toString() {
		return _value.toString();
	}
	public boolean equals(RtflType value, Scope scope) throws RuntimeException {
		return value instanceof AssignmentType ? ((AssignmentType) value).extractValue(scope).value().equals(_value) : value.value().equals(_value);
	}
}
