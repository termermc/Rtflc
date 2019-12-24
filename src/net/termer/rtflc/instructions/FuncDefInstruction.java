package net.termer.rtflc.instructions;

public class FuncDefInstruction implements ClauseOpenerInstruction {
	private String originFile = null;
	private int originLine = 0;
	private String funcName = null;
	private String[] argNames = {};
	
	public FuncDefInstruction(String file, int line, String name) {
		originFile = file;
		originLine = line;
		funcName = name;
	}
	public FuncDefInstruction(String file, int line, String name, String[] args) {
		originFile = file;
		originLine = line;
		funcName = name;
		argNames = args;
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
	public String[] argumentNames() {
		return argNames;
	}
	
	public String toString() {
		return "func "+funcName+" {";
	}
}