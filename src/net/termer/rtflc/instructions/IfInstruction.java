package net.termer.rtflc.instructions;

import net.termer.rtflc.type.RtflType;

public class IfInstruction implements ClauseOpenerInstruction {
	private String originFile = null;
	private int originLine = 0;
	private RtflType ifCondition = null;
	
	public IfInstruction(String file, int line, RtflType condition) {
		originFile = file;
		originLine = line;
		ifCondition = condition;
	}
	
	public RtflType condition() {
		return ifCondition;
	}
	
	public String originFile() {
		return originFile;
	}
	public int originLine() {
		return originLine;
	}
	
	public String toString() {
		return "if "+ifCondition.toString()+" {";
	}
}
