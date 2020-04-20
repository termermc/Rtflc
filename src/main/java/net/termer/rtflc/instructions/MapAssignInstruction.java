package net.termer.rtflc.instructions;

import net.termer.rtflc.type.RtflType;

public class MapAssignInstruction implements RtflInstruction {
	private final String originFile;
	private final int originLine;
	private final RtflType _map;
	private final String _field;
	private final RtflType _assignment;
	
	public MapAssignInstruction(String file, int line, RtflType map, String field, RtflType assignment) {
		originFile = file;
		originLine = line;
		_map = map;
		_field = field;
		_assignment = assignment;
	}
	
	public RtflType map() {
		return _map;
	}
	public String field() {
		return _field;
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
		return _map+"->"+_field+" = "+_assignment;
	}
}