package net.termer.rtflc.instructions;

public class AsyncInstruction implements ClauseOpenerInstruction {
	private int _line = -1;
	private String _file = null;
	
	public AsyncInstruction(String file, int line) {
		_file = file;
		_line = line;
	}
	
	public String toString() {
		return "async {";
	}
	
	public String originFile() {
		return _file;
	}
	public int originLine() {
		return _line;
	}
}
