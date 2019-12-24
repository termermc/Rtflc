package net.termer.rtflc.instructions;

import net.termer.rtflc.type.RtflType;

public class ArrayAssignInstruction implements RtflInstruction {
	private final String originFile;
	private final int originLine;
	private final RtflType _array;
	private final RtflType _index;
	private final RtflType _assignment;
	
	public ArrayAssignInstruction(String file, int line, RtflType array, RtflType index, RtflType assignment) {
		originFile = file;
		originLine = line;
		_array = array;
		_index = index;
		_assignment = assignment;
	}
	
	public RtflType array() {
		return _array;
	}
	public RtflType index() {
		return _index;
	}
	public RtflType assignValue() {
		return _assignment;
	}
	
	public String originFile() {
		return originFile;
	}

	public int originLine() {
		return originLine;
	}
	
	public String toString() {
		return _array.toString()+'['+_index+"] = "+_assignment;
	}
}