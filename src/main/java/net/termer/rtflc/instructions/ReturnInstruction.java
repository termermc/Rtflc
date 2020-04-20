package net.termer.rtflc.instructions;

import net.termer.rtflc.type.RtflType;

public class ReturnInstruction implements RtflInstruction {
	private String originFile = null;
	private int originLine = 0;
	private RtflType returnVal = null;
	
	public ReturnInstruction(String file, int line, RtflType val) {
		originFile = file;
		originLine = line;
		returnVal = val;
	}
	
	public RtflType returnValue() {
		return returnVal;
	}
	
	public String originFile() {
		return originFile;
	}
	public int originLine() {
		return originLine;
	}
	
	public String toString() {
		return "return "+returnVal.toString();
	}
}
