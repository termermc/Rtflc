package net.termer.rtflc.type.assignment;

import net.termer.rtflc.runtime.RuntimeException;
import net.termer.rtflc.runtime.Scope;
import net.termer.rtflc.type.RtflType;

public class VarRefAssignment implements AssignmentType {
	private String varName = null;
	
	public VarRefAssignment(String name) {
		varName = name;
	}
	
	public boolean equals(RtflType val, Scope scope) throws RuntimeException {
		return extractValue(scope).equals(val, scope);
	}
	
	public String name() {
		return "VAR_REF";
	}
	public Object value() {
		return null;
	}
	
	public String variableName() {
		return varName;
	}
	
	public String toString() {
		return varName;
	}
	
	public RtflType extractValue(Scope scope) throws RuntimeException {
		return scope.varValue(varName);
	}
}
