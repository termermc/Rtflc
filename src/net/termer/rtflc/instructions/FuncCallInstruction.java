package net.termer.rtflc.instructions;

import net.termer.rtflc.type.RtflType;

public class FuncCallInstruction implements RtflInstruction {
	private String originFile = null;
	private int originLine = 0;
	private String funcName = null;
	private RtflType[] funcArgs = null;
	
	public FuncCallInstruction(String file, int line, String name, RtflType[] args) {
		originFile = file;
		originLine = line;
		funcName = name;
		funcArgs = args;
	}
	
	public String originFile() {
		return originFile;
	}
	public int originLine() {
		return originLine;
	}
	
	public String functionName() {
		return funcName;
	}
	public RtflType[] functionArguments() {
		return funcArgs;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder(funcName+'(');
		
		for(int i = 0; i < funcArgs.length; i++) {
			sb.append(funcArgs[i].toString());
			
			if(i < funcArgs.length-1)
				sb.append(", ");
		}
		
		return sb.append(')').toString();
	}
}
