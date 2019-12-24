package net.termer.rtflc.type.assignment;

import net.termer.rtflc.runtime.Scope;
import net.termer.rtflc.type.RtflType;
import net.termer.rtflc.runtime.RuntimeException;

public class FunctionCallAssignment implements AssignmentType {
	private String funcName = null;
	private RtflType[] funcArgs = null;
	
	public FunctionCallAssignment(String name, RtflType[] args) {
		funcName = name;
		funcArgs = args;
	}
	
	public String name() {
		return "FUNC_CALL";
	}
	public Object value() {
		return null;
	}
	
	public String functionName() {
		return funcName;
	}
	public RtflType[] functionArgs() {
		return funcArgs;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder(funcName+'(');
		
		for(int i = 0; i < funcArgs.length; i++) {
			sb.append(funcArgs[i]);
			if(i < funcArgs.length-1)
				sb.append(", ");
		}
		
		return sb.append(')').toString();
	}
	
	public boolean equals(RtflType val, Scope scope) throws RuntimeException {
		return extractValue(scope).equals(val, scope);
	}
	
	public RtflType extractValue(Scope scope) throws RuntimeException {
		RtflType val = null;
		
		RtflType[] args = new RtflType[funcArgs.length];
		for(int i = 0; i < funcArgs.length; i++) {
			if(funcArgs[i] instanceof AssignmentType) {
				args[i] = ((AssignmentType)funcArgs[i]).extractValue(scope);
			} else {
				args[i] = funcArgs[i];
			}
		}
		val = scope.function(funcName).run(args, scope.runtime(), scope.descend(null));
		
		return val;
	}
}
