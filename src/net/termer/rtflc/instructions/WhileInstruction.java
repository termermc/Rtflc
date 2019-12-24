package net.termer.rtflc.instructions;

import net.termer.rtflc.type.RtflType;

public class WhileInstruction implements ClauseOpenerInstruction {
	private String originFile = null;
	private int originLine = 0;
	private RtflType whileCondition = null;
	
	public WhileInstruction(String file, int line, RtflType condition) {
		originFile = file;
		originLine = line;
		whileCondition = condition;
	}
	
	public RtflType condition() {
		return whileCondition;
	}
	
	public String originFile() {
		return originFile;
	}
	public int originLine() {
		return originLine;
	}
	
	public String toString() {
		return "while "+whileCondition.toString()+" {";
	}
}
