package net.termer.rtflc.instructions;

public class TryInstruction implements ClauseOpenerInstruction {
	private String originFile = null;
	private int originLine = 0;
	private String varName = null;
	
	public TryInstruction(String file, int line, String var) {
		originFile = file;
		originLine = line;
		varName = var;
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
		return "error "+varName+" {";
	}
}
