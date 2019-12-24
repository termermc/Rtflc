package net.termer.rtflc.instructions;

import net.termer.rtflc.type.RtflType;

public class VarDefInstruction implements RtflInstruction {
	private String originFile = null;
	private int originLine = 0;
	private String varName = null;
	private RtflType varValue = null;
	
	public VarDefInstruction(String file, int line, String name, RtflType value) {
		originFile = file;
		originLine = line;
		varName = name;
		varValue = value;
	}
	
	public String originFile() {
		return originFile;
	}
	public int originLine() {
		return originLine;
	}
	
	public String variableName() {
		return varName;
	}
	public RtflType variableValue() {
		return varValue;
	}
	
	public String toString() {
		return "def "+varName+" = "+varValue.toString();
	}
}
