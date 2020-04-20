package net.termer.rtflc.instructions;

public class DescendScopeInstruction implements RtflInstruction {
	public String toString() {
		// Invisible instruction
		return "";
	}
	
	public String originFile() {
		return "null";
	}

	public int originLine() {
		return 0;
	}

}
