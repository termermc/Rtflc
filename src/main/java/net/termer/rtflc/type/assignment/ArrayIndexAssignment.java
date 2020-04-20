package net.termer.rtflc.type.assignment;

import java.util.ArrayList;

import net.termer.rtflc.runtime.RuntimeException;
import net.termer.rtflc.runtime.Scope;
import net.termer.rtflc.type.ArrayType;
import net.termer.rtflc.type.NumberType;
import net.termer.rtflc.type.RtflType;

public class ArrayIndexAssignment implements AssignmentType {
	private RtflType _array = null;
	private RtflType _index = null;
	
	public ArrayIndexAssignment(RtflType array, RtflType index) {
		_array = array;
		_index = index;
	}
	
	public String name() {
		return "ARRAY_INDEX";
	}
	public Object value() {
		return null;
	}
	
	public RtflType array() {
		return _array;
	}
	public RtflType index() {
		return _index;
	}
	
	public boolean equals(RtflType value, Scope scope) throws RuntimeException {
		return extractValue(scope).equals(value);
	}

	public RtflType extractValue(Scope scope) throws RuntimeException {
		RtflType arr = _array instanceof AssignmentType ? ((AssignmentType) _array).extractValue(scope) : _array;
		RtflType idx = _index instanceof AssignmentType ? ((AssignmentType) _index).extractValue(scope) : _index;
		
		RtflType res = null;
		
		if(arr instanceof ArrayType) {
			if(idx instanceof NumberType) {
				@SuppressWarnings("unchecked")
				ArrayList<RtflType> array = (ArrayList<RtflType>) arr.value();
				NumberType index = (NumberType) idx;
				
				try {
					res = array.get(index.toInt());
				} catch(IndexOutOfBoundsException e) {
					throw new RuntimeException("Index "+index.toInt()+" is out of bounds");
				}
			} else {
				throw new RuntimeException("Cannot select element at non-number index");
			}
		} else {
			throw new RuntimeException("Cannot get array element from non-array value");
		}
		
		return res;
	}
	
	public String toString() {
		return _array.toString()+'['+_index+']';
	}
}
