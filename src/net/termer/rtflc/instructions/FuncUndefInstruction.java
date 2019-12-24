package net.termer.rtflc.instructions;

public class FuncUndefInstruction implements RtflInstruction {
	private String originFile = null;
	private int originLine = 0;
	private String funcName = null;
	
	public FuncUndefInstruction(String file, int line, String name) {
		originFile = file;
		originLine = line;
		funcName = name;
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
	
	public String toString() {
		return "unfunc "+funcName;
	}
}
