package net.termer.rtflc.instructions;

public class VarUndefInstruction implements RtflInstruction {
	private String originFile = null;
	private int originLine = 0;
	private String varName = null;
	
	public VarUndefInstruction(String file, int line, String name) {
		originFile = file;
		originLine = line;
		varName = name;
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
	
	public String toString() {
		return "undef "+varName;
	}
}
