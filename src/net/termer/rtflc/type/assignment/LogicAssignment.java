package net.termer.rtflc.type.assignment;

import net.termer.rtflc.runtime.RuntimeException;
import net.termer.rtflc.runtime.Scope;
import net.termer.rtflc.type.BoolType;
import net.termer.rtflc.type.NumberType;
import net.termer.rtflc.type.RtflType;
import net.termer.rtflc.utils.LogicComparison;

public class LogicAssignment implements AssignmentType {
	private RtflType left = null;
	private LogicComparison comp = null;
	private RtflType right = null;
	private boolean inverse = false;
	
	public LogicAssignment(RtflType leftCondition, LogicComparison comparison, RtflType rightCondition, boolean isInverse) {
		left = leftCondition;
		comp = comparison;
		right = rightCondition;
		inverse = isInverse;
	}
	
	public String name() {
		return "LOGIC";
	}
	public Object value() {
		return null;
	}
	public boolean equals(RtflType val, Scope scope) throws RuntimeException {
		return extractValue(scope).equals(val, scope);
	}
	
	public String toString() {
		return (inverse ? '!' : "")+('['+left.toString()+' '+LogicComparison.toChar(comp)+' '+right.toString()+']');
	}
	
	public RtflType firstValue() {
		return left;
	}
	public RtflType secondValue() {
		return right;
	}
	public LogicComparison comparisonType() {
		return comp;
	}
	public boolean inverse() {
		return inverse;
	}
	
	public RtflType extractValue(Scope scope) throws RuntimeException {
		boolean val = false;
		
		RtflType l = left instanceof AssignmentType ? ((AssignmentType) left).extractValue(scope) : left;
		RtflType r = right instanceof AssignmentType ? ((AssignmentType) right).extractValue(scope) : right;
		
		if(comp == LogicComparison.EQUAL) {
			val = l.equals(r, scope);
		} else if(comp == LogicComparison.AND) {
			if(l instanceof NumberType && r instanceof NumberType) {
				val = ((NumberType) l).toDouble() > 0 && ((NumberType) r).toDouble() > 0;
			} else {
				val = false;
			}
		} else if(comp == LogicComparison.OR) {
			if(l instanceof NumberType && r instanceof NumberType) {
				val = ((NumberType) l).toDouble() > 0 || ((NumberType) r).toDouble() > 0;
			} else {
				val = false;
			}
		} else if(comp == LogicComparison.GREATER) {
			if(l instanceof NumberType && r instanceof NumberType) {
				val = ((NumberType) l).toDouble() > ((NumberType) r).toDouble();
			} else {
				val = false;
			}
		} else if(comp == LogicComparison.LESS) {
			if(l instanceof NumberType && r instanceof NumberType) {
				val = ((NumberType) l).toDouble() < ((NumberType) r).toDouble();
			} else {
				val = false;
			}
		} else {
			val = false;
		}
		
		return inverse ? new BoolType(!val) : new BoolType(val);
	}
}
