package net.termer.rtflc.instructions;

public class EndClauseInstruction implements RtflInstruction {
	private String originFile = null;
	private int originLine = 0;
	
	public EndClauseInstruction(String file, int line) {
		originFile = file;
		originLine = line;
	}
	
	public String originFile() {
		return originFile;
	}
	public int originLine() {
		return originLine;
	}
	
	public String toString() {
		return "}";
	}
}
