package net.termer.rtflc.producers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.termer.rtflc.consumers.InstructionConsumer;
import net.termer.rtflc.instructions.*;
import net.termer.rtflc.runtime.RuntimeException;
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
import net.termer.rtflc.type.assignment.VarRefAssignment;
import net.termer.rtflc.utils.LogicComparison;
import net.termer.rtflc.type.assignment.NotAssignment;

/**
 * Class that provides methods to parse source code into RtflInstruction objects.
 * @author termer
 * @since 1.0
 */
public class SourcecodeInstructionProducer {
	/* Regex */
	// Statements
	private static final Pattern patGlobalVarDef = Pattern.compile("^def[ ]*([a-zA-Z0-9_-]*)[ ]*=[ ]*(.*)$");
	private static final Pattern patLocalVarDef = Pattern.compile("^local[ ]*([a-zA-Z0-9_-]*)[ ]*=[ ]*(.*)$");
	private static final Pattern patVarAssignment = Pattern.compile("^([a-zA-Z0-9_-]*)[ ]*=[ ]*(.*)$");
	private static final Pattern patVarUndef = Pattern.compile("^undef[ ]*([a-zA-Z0-9_-]*)$");
	private static final Pattern patFuncCall = Pattern.compile("^*([a-zA-Z0-9_-]*)\\((.*)\\)$");
	private static final Pattern patMethodCall = Pattern.compile("^(.+)\\.([a-zA-Z0-9_-]+)(\\((.*)\\))?$");
	private static final Pattern patReturn = Pattern.compile("^return (.*)$");
	private static final Pattern patIf = Pattern.compile("^if (.+)[ ]*\\{$");
	private static final Pattern patWhile = Pattern.compile("^while (.+)[ ]*\\{$");
	private static final Pattern patTry = Pattern.compile("^error ([a-zA-Z0-9_-]*)[ ]*\\{$");
	private static final Pattern patFuncDef = Pattern.compile("^func ([a-zA-Z0-9_-]*)[ ]*\\{$");
	private static final Pattern patFuncUndef = Pattern.compile("^unfunc[ ]*([a-zA-Z0-9_-]*)$");
	private static final Pattern patAsync = Pattern.compile("^async[ ]*\\{$");
	private static final Pattern patArrayAssignment = Pattern.compile("^(.+)[ ]*\\[[ ]*(.+)[ ]*\\][ ]*=[ ]*(.+)$");
	private static final Pattern patMapFieldAssignment = Pattern.compile("^(.+)->([a-zA-Z0-9_-]+)[ ]*=[ ]*(.+)$");
	private static final Pattern patFuncDefArgs = Pattern.compile("^func[ ]+([a-zA-Z0-9_-]*)\\([ ]*(.*)[ ]*\\)[ ]*\\{$");
	// Data types
	private static final Pattern patFunc = patFuncCall;
	private static final Pattern patMethod = patMethodCall;
	private static final Pattern patNumber = Pattern.compile("^(-?[0-9]*[\\.]?[0-9]*)?$");
	private static final Pattern patString = Pattern.compile("^\"(.*)\"$");
	private static final Pattern patVar = Pattern.compile("^([a-zA-Z0-9_.-]*)$");
	private static final Pattern patLogic = Pattern.compile("^\\!?\\[[ ]*(.+)[ ]*(=|&|\\||>|<)[ ]*(.+)[ ]*\\]$");
	private static final Pattern patSimpleLogic = Pattern.compile("^\\!?\\[[ ]*(.+)[ ]*\\]$");
	private static final Pattern patArrayIndex = Pattern.compile("(.+)[ ]*\\[[ ]*(.+)[ ]*\\]");
	private static final Pattern patMapField = Pattern.compile("(.+)->([a-zA-Z0-9_-]+)");
	
	public static void produce(String src, InputStream in, InstructionConsumer cons) throws IOException, ProducerException, RuntimeException {
		// Initialize reader with UTF-8 support
		BufferedReader buf = new BufferedReader(new InputStreamReader(in, "UTF8"));
		int lnNum = 0;
		
		while(buf.ready()) {
			String ln = buf.readLine().trim();
			if(ln.endsWith(";"))
				ln = ln.substring(0, ln.length()-1);
			lnNum++;
			Matcher lnMatch = patGlobalVarDef.matcher(ln);
			
			// Only deal with lines containing content
			if(ln.length() > 0 && !ln.startsWith("//") && !ln.startsWith("#")) {
				if(
					lnMatch.matches()
					// Global variable definition
				) {
					cons.consume(new VarDefInstruction(src, lnNum, lnMatch.group(1), resolveValue(src, lnNum, lnMatch.group(2))));
				} else if(
					(lnMatch = patLocalVarDef.matcher(ln)).matches()
					// Local variable definition
				) {
					cons.consume(new VarLocalDefInstruction(src, lnNum, lnMatch.group(1), resolveValue(src, lnNum, lnMatch.group(2))));
				} else if(
					(lnMatch = patArrayAssignment.matcher(ln)).matches()
					// Array assignment
				) {
					RtflType arr = resolveValue(src, lnNum, lnMatch.group(1));
					RtflType idx = resolveValue(src, lnNum, lnMatch.group(2));
					RtflType val = resolveValue(src, lnNum, lnMatch.group(3));
					
					if(!(idx instanceof AssignmentType || idx instanceof NumberType)) {
						throw new ProducerException("Non-number/bool value provided for logic expression", src, lnNum);
					}
					
					cons.consume(new ArrayAssignInstruction(src, lnNum, arr, idx, val));
				} else if(
					(lnMatch = patMapFieldAssignment.matcher(ln)).matches()
					// Map field assignment
				) {
					RtflType map = resolveValue(src, lnNum, lnMatch.group(1));
					String field = lnMatch.group(2);
					RtflType value = resolveValue(src, lnNum, lnMatch.group(3));
					
					cons.consume(new MapAssignInstruction(src, lnNum, map, field, value));
				} else if(
					(lnMatch = patVarAssignment.matcher(ln)).matches()
					// Variable assignment
				) {
					cons.consume(new VarAssignInstruction(src, lnNum, lnMatch.group(1), resolveValue(src, lnNum, lnMatch.group(2))));
				} else if(
					(lnMatch = patVarUndef.matcher(ln)).matches()
					// Variable un-definition
				) {
					cons.consume(new VarUndefInstruction(src, lnNum, lnMatch.group(1)));
				} else if(
					(lnMatch = patReturn.matcher(ln)).matches()
					// Return statement
				) {
					cons.consume(new ReturnInstruction(src, lnNum, resolveValue(src, lnNum, lnMatch.group(1))));
				} else if(
					(lnMatch = patFuncCall.matcher(ln)).matches()
					// Function call
				) {
					String funcName = lnMatch.group(1);
					RtflType[] args = parseFuncArgs(src, lnNum, lnMatch.group(2).trim());
					FuncCallInstruction instr = new FuncCallInstruction(src, lnNum, funcName, args);
					cons.consume(instr);
				} else if(
					(lnMatch = patMethodCall.matcher(ln)).matches()
					// Method call
				) {
					RtflType exp = resolveValue(src, lnNum, lnMatch.group(1));
					String funcName = lnMatch.group(2);
					
					ArrayList<RtflType> args = new ArrayList<RtflType>();
					args.add(exp);
					if(lnMatch.group(4) != null) {
						RtflType[] parseArgs = parseFuncArgs(src, lnNum, lnMatch.group(4));
						for(RtflType arg : parseArgs)
							args.add(arg);
					}
					FuncCallInstruction instr = new FuncCallInstruction(src, lnNum, funcName, args.toArray(new RtflType[0]));
					cons.consume(instr);
				} else if(
					(lnMatch = patIf.matcher(ln)).matches()
					// If statement
				) {
					// Check if condition is a literal
					RtflType cond = resolveValue(src, lnNum, lnMatch.group(1));
					if(cond instanceof AssignmentType || cond instanceof NumberType) {
						cons.consume(new IfInstruction(src, lnNum, resolveValue(src, lnNum, lnMatch.group(1))));
					} else {
						throw new ProducerException("Non-number/bool value provided for 'if' instruction", src, lnNum);
					}
				} else if(
					(lnMatch = patWhile.matcher(ln)).matches()
					// While loop
				) {
					// Check if condition is a literal
					RtflType cond = resolveValue(src, lnNum, lnMatch.group(1));
					if(cond instanceof AssignmentType || cond instanceof NumberType) {
						cons.consume(new WhileInstruction(src, lnNum, resolveValue(src, lnNum, lnMatch.group(1))));
					} else {
						throw new ProducerException("Non-number/bool value provided for 'while' instruction", src, lnNum);
					}
				} else if(
					(lnMatch = patTry.matcher(ln)).matches()
					// Try (error) statement
				) {
					cons.consume(new TryInstruction(src, lnNum, lnMatch.group(1)));
				} else if(
					(lnMatch = patFuncDef.matcher(ln)).matches()
					// Function definition
				) {
					cons.consume(new FuncDefInstruction(src, lnNum, lnMatch.group(1)));
				} else if(
					(lnMatch = patFuncDefArgs.matcher(ln)).matches()
					// Function definition with argument names
				) {
					// Parse argument names
					String[] rawNames = lnMatch.group(2).split(",");
					ArrayList<String> names = new ArrayList<String>();
					
					for(String name : rawNames) {
						if(patVar.matcher(name.trim()).matches())
							names.add(name.trim());
						else
							throw new ProducerException("Argument name cannot contain special characters");
					}
					
					cons.consume(new FuncDefInstruction(src, lnNum, lnMatch.group(1), names.toArray(new String[0])));
				} else if(
					(lnMatch = patFuncUndef.matcher(ln)).matches()
					// Function un-definition
				) {
					cons.consume(new FuncUndefInstruction(src, lnNum, lnMatch.group(1)));
				} else if(
					(lnMatch = patAsync.matcher(ln)).matches()
					// Async block
				) {
					cons.consume(new AsyncInstruction(src, lnNum));
				} else if(ln.equalsIgnoreCase("}")) {
					// End clause
					cons.consume(new EndClauseInstruction(src, lnNum));
				} else {
					throw new ProducerException("Encountered invalid expression: "+ln, src, lnNum);
				}
			}
		}
		
		// Tell consumer instructions are finished
		cons.finish();
		
		// Close buffer after producing instructions
		buf.close();
	}
	
	/**
	 * Resolves a String denoting an RtflType into an RtflType object
	 * @param src the source name the String came from (doesn't have to be a filename)
	 * @param ln the line that this String originally appeared on
	 * @param value the String to parse into an RtflType
	 * @return the RtflType 
	 * @throws ProducerException if an invalid value expression is provided
	 * @since 1.0
	 */
	public static RtflType resolveValue(String src, int ln, String value) throws ProducerException {
		RtflType val = null;
		String str = value.trim();
		
		Matcher valMatch = null;
		if(str.equalsIgnoreCase("null")) {
			val = new NullType();
		} else if(str.equalsIgnoreCase("true")) {
			val = new BoolType(true);
		} else if(str.equalsIgnoreCase("false")) {
			val = new BoolType(false);
		} else if(
			(valMatch = patString.matcher(str)).matches()
		) {
			val = new StringType(
				valMatch.group(1)
					.replace("\\\\", "\\")
					.replace("\\\"", "\"")
					.replace("\\n", "\n")
					.replace("\\t", "\t")
					.replace("\\r", "\r")
					.replace("\\b", "\b")
					.replace("\\f", "\f")
			);
		} else if(
			(valMatch = patNumber.matcher(str)).matches()
		) {
			val = str.contains(".") ? new DoubleType(Double.parseDouble(str)) : new IntType(Integer.parseInt(str));
		} else if(
			(valMatch = patFunc.matcher(str)).matches()
			// Function
		) {
			// Fetch function name
			String funcName = valMatch.group(1);
			
			// Resolve arguments
			RtflType[] args = parseFuncArgs(src, ln, valMatch.group(2).trim());
			
			// Output value
			val = new FunctionCallAssignment(funcName, args);
		} else if(
			(valMatch = patMethod.matcher(str)).matches()
			// Method
		) {
			RtflType exp = resolveValue(src, ln, valMatch.group(1));
			String funcName = valMatch.group(2);
			
			ArrayList<RtflType> args = new ArrayList<RtflType>();
			args.add(exp);
			if(valMatch.group(4) != null) {
				RtflType[] parseArgs = parseFuncArgs(src, ln, valMatch.group(4));
				for(RtflType arg : parseArgs)
					args.add(arg);
			}
			val = new FunctionCallAssignment(funcName, args.toArray(new RtflType[0]));
		} else if(
			(valMatch = patVar.matcher(str)).matches()
			// Variable reference
		) {
			val = new VarRefAssignment(valMatch.group(1));
		} else if(
			(valMatch = patLogic.matcher(str)).matches()
			// Logic expression
		) {
			RtflType val1 = resolveValue(src, ln, valMatch.group(1));
			RtflType val2 = resolveValue(src, ln, valMatch.group(3));
			LogicComparison comp = LogicComparison.byChar(valMatch.group(2).charAt(0));
			
			// Check if both values are either AssignmentTypes or NumberTypes
			if(
				(val1 instanceof AssignmentType || val1 instanceof NumberType) &&
				(val2 instanceof AssignmentType || val2 instanceof NumberType)
			) {
				val = new LogicAssignment(val1, comp, val2, str.startsWith("!"));
			} else if(comp != LogicComparison.EQUAL) {
				throw new ProducerException("Non-number/bool value provided for logic expression", src, ln);
			} else {
				val = new LogicAssignment(val1, comp, val2, str.startsWith("!"));
			}
		} else if(
			(valMatch = patSimpleLogic.matcher(str)).matches()
			// Simple logic (e.g. ![value])
		) {
			RtflType logicVal = resolveValue(src, ln, valMatch.group(1));
			
			if(logicVal instanceof AssignmentType) {
				if(str.startsWith("!"))
					val = new NotAssignment(logicVal);
				else
					val = logicVal;
			} else if(logicVal instanceof NumberType) {
				// Check if inverse
				if(str.startsWith("!"))
					val = new BoolType(!(((NumberType) logicVal).toDouble() > 0));
				else
					val = new BoolType(((NumberType) logicVal).toDouble() > 0);
			} else {
				throw new ProducerException("Non-number/bool value provided for logic expression", src, ln);
			}
		} else if(
			(valMatch = patArrayIndex.matcher(str)).matches()
			// Array index
		) {
			RtflType arr = resolveValue(src, ln, valMatch.group(1));
			RtflType idx = resolveValue(src, ln, valMatch.group(2));
			
			if(!(idx instanceof NumberType || idx instanceof AssignmentType)) {
				throw new ProducerException("Non-number value provided as array index", src, ln);
			}
			
			val = new ArrayIndexAssignment(arr, idx);
		} else if(
			(valMatch = patMapField.matcher(str)).matches()
			// Map field
		) {
			RtflType map = resolveValue(src, ln, valMatch.group(1));
			String field = valMatch.group(2);
			
			val = new MapFieldAssignment(map, field);
		} else {
			throw new ProducerException("Encountered invalid value expression: "+str, src, ln);
		}
		
		return val;
	}
	
	private static RtflType[] parseFuncArgs(String src, int ln, String argStr) throws ProducerException {
		char[] chars = argStr.toCharArray();
		boolean openQuote = false;
		int openPars = 0;
		ArrayList<RtflType> args = new ArrayList<RtflType>();
		
		StringBuilder argBuf = new StringBuilder();
		for(int i = 0; i < chars.length; i++) {
			char c = chars[i];
			
			// Handle escaped quotes
			if(i < 1 && c == '"') {
				openQuote = !openQuote;
			} else if(c == '"' && (i > 0 && chars[i-1] != '\\' || i > 1 && chars[i-2] == '\\')) {
				openQuote = !openQuote;
			} else if(c == '"' && i > 1 && chars[i-2] == '\\') {
				openQuote = !openQuote;
			}
			
			// Handle open parenthesis
			if(!openQuote) {
				switch(c) {
				case '(':
					openPars++;
					break;
				case ')':
					if(openPars > -1)
						openPars--;
					break;
				}
			}
			
			// Split arguments
			if(!openQuote && openPars < 1 && c == ',') {
				args.add(resolveValue(src, ln, argBuf.toString()));
				argBuf = new StringBuilder();
			} else {
				argBuf.append(c);
			}
		}
		// Resolve last argument
		String lastArg = argBuf.toString().trim();
		if(lastArg.length() > 0)
			args.add(resolveValue(src, ln, lastArg));
		
		return args.toArray(new RtflType[0]);
	}
}