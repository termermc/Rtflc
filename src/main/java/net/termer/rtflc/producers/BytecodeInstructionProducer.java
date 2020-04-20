package net.termer.rtflc.producers;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import net.termer.rtflc.consumers.InstructionConsumer;
import net.termer.rtflc.instructions.*;
import net.termer.rtflc.runtime.RuntimeException;
import net.termer.rtflc.type.BoolType;
import net.termer.rtflc.type.DoubleType;
import net.termer.rtflc.type.assignment.ArrayIndexAssignment;
import net.termer.rtflc.type.assignment.AssignmentType;
import net.termer.rtflc.type.assignment.FunctionCallAssignment;
import net.termer.rtflc.type.assignment.LogicAssignment;
import net.termer.rtflc.type.assignment.MapFieldAssignment;
import net.termer.rtflc.type.assignment.NotAssignment;
import net.termer.rtflc.type.assignment.VarRefAssignment;
import net.termer.rtflc.utils.LogicComparison;
import net.termer.rtflc.type.*;

/**
 * Class that provides methods to parse bytecode into RtflInstruction objects.
 * @author termer
 * @since 1.0
 */
public class BytecodeInstructionProducer {
	/*
	 * OPCODES
	 * 0 - Variable definition
	 * 1 - Local variable definition
	 * 2 - Variable assignment
	 * 3 - Variable de-initialization
	 * 4 - Function call
	 * 5 - Return statement
	 * 6 - If statement
	 * 7 - While statement
	 * 8 - Try statement
	 * 9 - End clause (ending curly bracket)
	 * 10 - Function definition
	 * 11 - Function de-initialization
	 * 12 - Async block
	 * 14 - Descend scope
	 * 15 - Ascend scope
	 * 16 - Array assignment
	 */
	/*
	 * VALTYPES
	 * 0 - Null
	 * 1 - Bool
	 * 2 - Integer
	 * 3 - Double
	 * 4 - String
	 * 5 - Long String (over 256 characters)
	 * 6 - Function call
	 * 7 - Variable reference
	 * 8 - Comparison
	 * 9 - Inverse
	 * 10 - Array element
	 * 11 - Map field
	 */
	
	/**
	 * Begins parsing bytecode and feeding it to an InstructionConsumer
	 * @param src the bytecode source's name (does not have to be filename)
	 * @param in the InputStream from which to read bytecode
	 * @param cons the InstructionConsumer to consume produced instructions
	 * @param readLines whether the source input has line numbers included that should be read
	 * @throws IOException if reading from the input fails
	 * @throws ProducerException if a bytecode parsing or reading error occurs
	 * @throws RuntimeException If consumer fails when finish() is called
	 * @since 1.0
	 */
	public static void produce(String src, InputStream in, InstructionConsumer cons, boolean readLines) throws IOException, ProducerException, RuntimeException {
		// Read input via buffer to improve performance
		BufferedInputStream buf = new BufferedInputStream(in);
		
		while(buf.available() > 0) {
			int ln = readLines ? readShort(buf) : 0;
			int opcode = buf.read();
			
			if(opcode == 0) {
				// VAR_DEF
				int nlen = buf.read();
				StringBuilder name = new StringBuilder(nlen);
				
				for(int i = 0; i < nlen; i++)
					name.append((char)buf.read());
				
				RtflType val = resolveVal(buf, src, ln);
				
				cons.consume(new VarDefInstruction(src, ln, name.toString(), val));
			} else if(opcode == 1) {
				// VAR_LOCAL_DEF
				int nlen = buf.read();
				StringBuilder name = new StringBuilder(nlen);
				
				for(int i = 0; i < nlen; i++)
					name.append((char)buf.read());
				
				RtflType val = resolveVal(buf, src, ln);
				
				cons.consume(new VarLocalDefInstruction(src, ln, name.toString(), val));
			} else if(opcode == 2) {
				// VAR_ASSIGN
				int nlen = buf.read();
				StringBuilder name = new StringBuilder(nlen);
				
				for(int i = 0; i < nlen; i++)
					name.append((char)buf.read());
				
				RtflType val = resolveVal(buf, src, ln);
				cons.consume(new VarAssignInstruction(src, ln, name.toString(), val));
			} else if(opcode == 3) {
				// VAR_UNDEF
				int nlen = buf.read();
				StringBuilder name = new StringBuilder(nlen);
				
				for(int i = 0; i < nlen; i++)
					name.append((char)buf.read());
				
				cons.consume(new VarUndefInstruction(src, ln, name.toString()));
			} else if(opcode == 4) {
				// FUNC_CALL
				int nlen = buf.read();
				StringBuilder name = new StringBuilder(nlen);
				
				for(int i = 0; i < nlen; i++)
					name.append((char)buf.read());
				
				int arglen = buf.read();
				
				ArrayList<RtflType> args = new ArrayList<RtflType>();
				for(int i = 0; i < arglen; i++)
					args.add(resolveVal(buf, src, ln));
				
				cons.consume(new FuncCallInstruction(src, ln, name.toString(), args.toArray(new RtflType[0])));
			} else if(opcode == 5) {
				// RETURN
				RtflType returnVal = resolveVal(buf, src, ln);
				
				cons.consume(new ReturnInstruction(src, ln, returnVal));
			} else if(opcode == 6) {
				// IF
				RtflType condition = resolveVal(buf, src, ln);
				
				if(condition instanceof NumberType || condition instanceof AssignmentType) {
					cons.consume(new IfInstruction(src, ln, condition));
				} else {
					throw new ProducerException("Non-number/bool value provided for 'if' instruction", src, ln);
				}
			} else if(opcode == 7) {
				// WHILE
				RtflType condition = resolveVal(buf, src, ln);
				
				if(condition instanceof NumberType || condition instanceof AssignmentType) {
					cons.consume(new WhileInstruction(src, ln, condition));
				} else {
					throw new ProducerException("Non-number/bool value provided for 'while' instruction", src, ln);
				}
			} else if(opcode == 8) {
				// TRY
				int nlen = buf.read();
				StringBuilder name = new StringBuilder(nlen);
				
				for(int i = 0; i < nlen; i++)
					name.append((char)buf.read());
				
				cons.consume(new TryInstruction(src, ln, name.toString()));
			} else if(opcode == 9) {
				// END_CLAUSE
				cons.consume(new EndClauseInstruction(src, ln));
			} else if(opcode == 10) {
				// FUNC_DEF
				int nlen = buf.read();
				StringBuilder name = new StringBuilder(nlen);
				for(int i = 0; i < nlen; i++)
					name.append((char)buf.read());
				
				// Read argument names
				int argNameCount = buf.read();
				String[] argNames = new String[argNameCount];
				for(int i = 0; i < argNameCount; i++) {
					int nameLen = buf.read();
					StringBuilder argName = new StringBuilder(nameLen);
					for(int j = 0; j < nameLen; j++)
						argName.append((char)buf.read());
					argNames[i] = argName.toString();
				}
				
				cons.consume(new FuncDefInstruction(src, ln, name.toString(), argNames));
			} else if(opcode == 11) {
				// FUNC_UNDEF
				int nlen = buf.read();
				StringBuilder name = new StringBuilder(nlen);
				for(int i = 0; i < nlen; i++)
					name.append((char)buf.read());
				
				cons.consume(new FuncUndefInstruction(src, ln, name.toString()));
				break;
			} else if(opcode == 12) {
				// ASYNC_BLOCK
				cons.consume(new AsyncInstruction(src, ln));
			} else if(opcode == 13) {
				// SWAP_SRC
				int nlen = buf.read();
				StringBuilder name = new StringBuilder(nlen);
				
				for(int i = 0; i < nlen; i++)
					name.append((char)buf.read());
				
				// Set source String to new source
				src = name.toString();
			} else if(opcode == 14) {
				// DESC_SCOPE
				cons.consume(new DescendScopeInstruction());
			} else if(opcode == 15) {
				// ASC_SCOPE
				cons.consume(new AscendScopeInstruction());
			} else if(opcode == 16) {
				// ARRAY_ASSIGN
				RtflType array = resolveVal(buf, src, ln);
				RtflType index = resolveVal(buf, src, ln);
				RtflType value = resolveVal(buf, src, ln);
				
				cons.consume(new ArrayAssignInstruction(src, ln, array, index, value));
			} else if(opcode == 17) {
				// MAP_ASSIGN
				RtflType map = resolveVal(buf, src, ln);
				int fieldLen = buf.read();
				StringBuilder field = new StringBuilder(fieldLen);
				for(int i = 0; i < fieldLen; i++)
					field.append((char)buf.read());
				
				RtflType value = resolveVal(buf, src, ln);
				
				cons.consume(new MapAssignInstruction(src, ln, map, field.toString(), value));
			} else {
				// INVALID
				throw new ProducerException("Encountered invalid opcode \""+opcode+"\", perhaps this was compiled for a newer version of Rtfl?");
			}
		}
		
		// Tell consumer instructions are finished
		cons.finish();
		
		// Close buffer after producing instructions
		buf.close();
	}
	
	/**
	 * Resolves bytecode representing a data value into an RtflType object
	 * @param in the InputStream to read from
	 * @param src the bytecode source's name (does not have to be filename)
	 * @param ln the source line from which this value originated 
	 * @return the RtflType represented in the bytecode source
	 * @throws IOException if reading from the InputStream fails
	 * @throws ProducerException if an invalid type is encountered
	 * @since 1.0
	 */
	public static RtflType resolveVal(InputStream in, String src, int ln) throws IOException, ProducerException {
		RtflType val = null;
		
		int type = in.read();
		
		switch(type) {
		case 0:
			// Null
			val = new NullType();
			break;
		case 1:
			// Bool
			val = new BoolType(in.read() > 0);
			break;
		case 2:
			// Integer
			val = new IntType(readInt(in));
			break;
		case 3:
			// Double
			val = new DoubleType(readDouble(in));
			break;
		case 4:
			// String
			byte[] str = new byte[in.read()];
			in.read(str);
			
			val = new StringType(new String(str, StandardCharsets.UTF_8));
			break;
		case 5:
			// Long string
			byte[] strBytes = new byte[readShort(in)];
			in.read(strBytes);
			val = new StringType(new String(strBytes, StandardCharsets.UTF_8));
			break;
		case 6:
			// Function call
			int nlen = in.read();
			StringBuilder name = new StringBuilder(nlen);
			
			for(int i = 0; i < nlen; i++)
				name.append((char)in.read());
			
			int arglen = in.read();
			
			ArrayList<RtflType> args = new ArrayList<RtflType>();
			for(int i = 0; i < arglen; i++)
				args.add(resolveVal(in, src, ln));
			
			val = new FunctionCallAssignment(name.toString(), args.toArray(new RtflType[0]));
			break;
		case 7:
			// Variable reference
			int vlen = in.read();
			StringBuilder varName = new StringBuilder(vlen);
			
			for(int i = 0; i < vlen; i++)
				varName.append((char)in.read());
			
			val = new VarRefAssignment(varName.toString());
			break;
		case 8:
			// Comparison
			LogicComparison compType = LogicComparison.values()[in.read()];
			boolean inverse = in.read() > 0;
			RtflType comp1 = resolveVal(in, src, ln);
			RtflType comp2 = resolveVal(in, src, ln);
			
			val = new LogicAssignment(comp1, compType, comp2, inverse);
			break;
		case 9:
			// Inverse
			val = new NotAssignment(resolveVal(in, src, ln));
			break;
		case 10:
			// Array index
			RtflType array = resolveVal(in, src, ln);
			RtflType index = resolveVal(in, src, ln);
			
			val = new ArrayIndexAssignment(array, index);
			break;
		case 11:
			// Map field
			RtflType map = resolveVal(in, src, ln);
			int fieldLen = in.read();
			StringBuffer fieldName = new StringBuffer(fieldLen);
			for(int i = 0; i < fieldLen; i++)
				fieldName.append((char) in.read());
			
			val = new MapFieldAssignment(map, fieldName.toString());
			break;
		default:
			throw new ProducerException("Encountered invalid value type \""+type+"\", perhaps this was compiled for a newer version of Rtfl?", src, ln);
		}
		
		return val;
	}
	
	// Reads a short value from an InputStream
	@SuppressWarnings("static-access")
	private static short readShort(InputStream in) throws IOException {
		byte[] bytes = new byte[2];
		
		for(int i = 0; i < 2; i++)
			bytes[i] = (byte)in.read();
		
		return ByteBuffer.allocate(2).wrap(bytes).getShort();
	}
	// Reads an integer value from an InputStream
	@SuppressWarnings("static-access")
	private static int readInt(InputStream in) throws IOException {
		byte[] bytes = new byte[4];
		
		for(int i = 0; i < 4; i++)
			bytes[i] = (byte)in.read();
		
		return ByteBuffer.allocate(4).wrap(bytes).getInt();
	}
	// Reads a double value from an InputStream
	@SuppressWarnings("static-access")
	private static double readDouble(InputStream in) throws IOException {
		byte[] bytes = new byte[8];
		
		for(int i = 0; i < 8; i++)
			bytes[i] = (byte)in.read();
		
		return ByteBuffer.allocate(8).wrap(bytes).getDouble();
	}
}
