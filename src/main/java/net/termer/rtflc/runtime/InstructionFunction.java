package net.termer.rtflc.runtime;

import net.termer.rtflc.instructions.RtflInstruction;
import net.termer.rtflc.type.IntType;
import net.termer.rtflc.type.RtflType;
import net.termer.rtflc.runtime.RtflRuntime;

/**
 * Function that contains RtflInstruction objects to execute
 * @author termer
 * @since 1.0
 */
public class InstructionFunction implements RtflFunction {
	private RtflInstruction[] insts = null;
	private String[] argNames = {};
	
	/**
	 * Instantiates a new InstructionFunction with the provided instructions
	 * @param instructions the instructions to store in this function
	 * @since 1.0
	 */
	public InstructionFunction(RtflInstruction[] instructions) {
		insts = instructions;
	}
	/**
	 * Instantiates a new InstructionFunction with the provided instructions and argument names
	 * @param instructions the instructions to store in this function
	 * @param argumentNames The names of this function's arguments
	 * @since 1.0
	 */
	public InstructionFunction(RtflInstruction[] instructions, String[] argumentNames) {
		insts = instructions;
		argNames = argumentNames;
	}
	
	public RtflType run(RtflType[] args, RtflRuntime rt, Scope scope) throws RuntimeException {
		// Define argument variables
		for(int i = 0; i < args.length; i++) {
			if(i < argNames.length)
				scope.createLocalVar(argNames[i], args[i]);
			scope.createLocalVar("arg"+(i+1), args[i]);
		}
		scope.createLocalVar("arglen", new IntType(args.length));
		
		RtflType val = rt.execute(insts, scope);
		
		// Undefine argument variables
		for(int i = 0; i < args.length; i++) {
			if(i < argNames.length)
				if(scope.variableAliases().containsKey(argNames[i]))
					scope.undefineVar(argNames[i]);
			String vname = "arg"+(i+1);
			if(scope.variableAliases().containsKey(vname))
				scope.undefineVar(vname);
		}
		if(scope.variableAliases().containsKey("arglen"))
			scope.undefineVar("arglen");
		return val;
	}
}
