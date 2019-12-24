package net.termer.rtflc.consumers;

import net.termer.rtflc.instructions.RtflInstruction;
import net.termer.rtflc.type.BoolType;
import net.termer.rtflc.type.DoubleType;
import net.termer.rtflc.type.IntType;
import net.termer.rtflc.type.NullType;
import net.termer.rtflc.type.NumberType;
import net.termer.rtflc.type.RtflType;
import net.termer.rtflc.type.StringType;
import net.termer.rtflc.type.assignment.ArrayIndexAssignment;
import net.termer.rtflc.type.assignment.AssignmentType;
import net.termer.rtflc.type.assignment.FunctionCallAssignment;
import net.termer.rtflc.type.assignment.LogicAssignment;
import net.termer.rtflc.type.assignment.MapFieldAssignment;
import net.termer.rtflc.type.assignment.NotAssignment;
import net.termer.rtflc.type.assignment.VarRefAssignment;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import net.termer.rtflc.compiler.CompilerException;
import net.termer.rtflc.instructions.*;

/**
 * InstructionConsumer implementation that produces bytecode from RtflInstruction objects
 * @author termer
 * @since 1.0
 */
public class CompilerInstructionConsumer implements InstructionConsumer {
	private OutputStream out = null;
	private boolean writeLns = true;
	
	public CompilerInstructionConsumer(OutputStream output, boolean writeLines) {
		out = output;
		writeLns = writeLines;
	}
	
	public void consume(RtflInstruction inst) throws IOException {
		if(writeLns)
			writeShort((short) inst.originLine());
		
		if(inst instanceof VarDefInstruction) {
			VarDefInstruction ins = (VarDefInstruction) inst;
			// Write opcode
			out.write(0);
			// Var name length
			out.write((byte) ins.variableName().length());
			// Var name
			writeStr(ins.variableName());
			// Write value
			writeVal(ins.variableValue());
		} else if(inst instanceof VarLocalDefInstruction) {
			VarLocalDefInstruction ins = (VarLocalDefInstruction) inst;
			// Write opcode
			out.write(1);
			// Var name length
			out.write((byte) ins.variableName().length());
			// Var name
			writeStr(ins.variableName());
			// Write value
			writeVal(ins.variableValue());
		} else if(inst instanceof VarAssignInstruction) {
			VarAssignInstruction ins = (VarAssignInstruction) inst;
			// Write opcode
			out.write(2);
			// Var name length
			out.write((byte) ins.variableName().length());
			// Var name
			writeStr(ins.variableName());
			// Write value
			writeVal(ins.assignValue());
		} else if(inst instanceof VarUndefInstruction) {
			VarUndefInstruction ins = (VarUndefInstruction) inst;
			// Write opcode
			out.write(3);
			// Var name length
			out.write((byte) ins.variableName().length());
			// Var name
			writeStr(ins.variableName());
		} else if(inst instanceof FuncCallInstruction) {
			FuncCallInstruction ins = (FuncCallInstruction) inst;
			// Write opcode
			out.write(4);
			// Write function name length
			out.write(ins.functionName().length());
			// Write function name
			writeStr(ins.functionName());
			// Write arg length
			out.write(ins.functionArguments().length);
			// Write arguments
			for(RtflType arg : ins.functionArguments())
				writeVal(arg);
		} else if(inst instanceof ReturnInstruction) {
			ReturnInstruction ins = (ReturnInstruction) inst;
			// Write opcode
			out.write(5);
			// Write return value
			writeVal(ins.returnValue());
		} else if(inst instanceof IfInstruction) {
			IfInstruction ins = (IfInstruction) inst;
			// Write opcode
			out.write(6);
			// Check condition to make sure it's legal
			RtflType cond = ins.condition();
			if(cond instanceof NumberType || cond instanceof AssignmentType) {
				writeVal(cond);
			} else {
				throw new CompilerException("Non-number/bool value provided for 'if' instruction");
			}
		} else if(inst instanceof WhileInstruction) {
			WhileInstruction ins = (WhileInstruction) inst;
			// Write opcode
			out.write(7);
			// Check condition to make sure it's legal
			RtflType cond = ins.condition();
			if(cond instanceof NumberType || cond instanceof AssignmentType) {
				writeVal(cond);
			} else {
				throw new CompilerException("Non-number/bool value provided for 'while' instruction");
			}
		} else if(inst instanceof TryInstruction) {
			TryInstruction ins = (TryInstruction) inst;
			// Write opcode
			out.write(8);
			// Write try variable name length
			out.write(ins.variableName().length());
			// Write try variable name
			writeStr(ins.variableName());
		} else if(inst instanceof EndClauseInstruction) {
			// Write opcode
			out.write(9);
		} else if(inst instanceof FuncDefInstruction) {
			FuncDefInstruction ins = (FuncDefInstruction) inst;
			// Write opcode
			out.write(10);
			
			// Write func name length
			out.write(ins.functionName().length());
			// Write func name
			writeStr(ins.functionName());
			
			// Write the number of argument names
			out.write(ins.argumentNames().length);
			// Write argument names
			for(String name : ins.argumentNames()) {
				out.write(name.length());
				writeStr(name);
			}
		} else if(inst instanceof FuncUndefInstruction) {
			FuncUndefInstruction ins = (FuncUndefInstruction) inst;
			// Write opcode
			out.write(11);
			// Write func name length
			out.write(ins.functionName().length());
			// Write func name
			writeStr(ins.functionName());
		} else if(inst instanceof AsyncInstruction) {
			// Write opcode
			out.write(12);
		} else if(inst instanceof DescendScopeInstruction) {
			// Write opcode
			out.write(14);
		} else if(inst instanceof AscendScopeInstruction) {
			// Write opcode
			out.write(15);
		} else if(inst instanceof ArrayAssignInstruction) {
			ArrayAssignInstruction ins = (ArrayAssignInstruction) inst;
			
			// Write opcode
			out.write(16);
			
			// Write values
			writeVal(ins.array());
			writeVal(ins.index());
			writeVal(ins.assignValue());
		} else if(inst instanceof MapAssignInstruction) {
			MapAssignInstruction ins = (MapAssignInstruction) inst;
			
			// Write opcode
			out.write(17);
			
			// Write values
			writeVal(ins.map());
			out.write(ins.field().length());
			writeStr(ins.field());
			writeVal(ins.assignValue());
		}
	}
	
	// Writes a short value to the output stream
	private void writeShort(short val) throws IOException {
		out.write(
			ByteBuffer
				.allocate(Short.BYTES)
				.putShort(val)
				.array()
		);
	}
	// Writes an int value to the output stream
	private void writeInt(int val) throws IOException {
		out.write(
			ByteBuffer
				.allocate(Integer.BYTES)
				.putInt(val)
				.array());
	}
	// Writes a double value to the output stream
	private void writeDouble(double val) throws IOException {
		out.write(
			ByteBuffer
				.allocate(Double.BYTES)
				.putDouble(val)
				.array()
		);
	}
	// Writes a String (UTF-8) value to the output stream
	private void writeStr(String str) throws IOException {
		out.write(str.getBytes(StandardCharsets.UTF_8));
	}
	// Writes a RtflType value to the output stream
	private void writeVal(RtflType val) throws IOException {
		if(val instanceof NullType) {
			out.write(0);
		} else if(val instanceof BoolType) {
			out.write(1);
			out.write(((BoolType) val).toInt());
		} else if(val instanceof IntType) {
			out.write(2);
			writeInt(((IntType) val).toInt());
		} else if(val instanceof DoubleType) {
			out.write(3);
			writeDouble(((DoubleType) val).toDouble());
		} else if(val instanceof StringType) {
			// Check whether String is over 256 chars
			String str = (String) val.value();
			if(str.length() > 256) {
				// Write long string
				out.write(5);
				writeShort((short) str.length());
				writeStr(str);
			} else {
				// Write short string
				out.write(4);
				out.write(str.length());
				writeStr(str);
			}
		} else if(val instanceof FunctionCallAssignment) {
			out.write(6);
			
			FunctionCallAssignment call = (FunctionCallAssignment) val;
			
			// Write function name length
			out.write(call.functionName().length());
			// Write function name
			writeStr(call.functionName());
			// Write length of arguments
			out.write(call.functionArgs().length);
			
			// Write arguments
			for(RtflType arg : call.functionArgs())
				writeVal(arg);
		} else if(val instanceof VarRefAssignment) {
			out.write(7);
			
			VarRefAssignment var = (VarRefAssignment) val;
			
			// Write var name length
			out.write(var.variableName().length());
			// Write var name
			writeStr(var.variableName());
		} else if(val instanceof LogicAssignment) {
			out.write(8);
			
			LogicAssignment var = (LogicAssignment) val;
			
			// Write comparison type
			out.write(var.comparisonType().ordinal());
			// Write whether it's inverse
			out.write(var.inverse() ? 1 : 0);
			// Write both values
			writeVal(var.firstValue());
			writeVal(var.secondValue());
		} else if(val instanceof NotAssignment) {
			out.write(9);
			
			NotAssignment var = (NotAssignment) val;
			
			// Write value
			writeVal(var.originalValue());
		} else if(val instanceof ArrayIndexAssignment) {
			out.write(10);
			
			ArrayIndexAssignment var = (ArrayIndexAssignment) val;
			
			// Write values
			writeVal(var.array());
			writeVal(var.index());
		} else if(val instanceof MapFieldAssignment) {
			out.write(11);
			
			MapFieldAssignment var = (MapFieldAssignment) val;
			
			// Write value
			writeVal(var.map());
			// Write field and field length
			out.write(var.field().length());
			writeStr(var.field());
		} else {
			throw new CompilerException("Failed to write unknown value type "+val.getClass().getName());
		}
	}
	
	public void finish() {}
}
