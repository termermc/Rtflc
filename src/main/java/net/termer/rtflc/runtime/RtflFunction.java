package net.termer.rtflc.runtime;

import net.termer.rtflc.type.RtflType;
import net.termer.rtflc.runtime.Scope;
import net.termer.rtflc.runtime.RtflRuntime;

/**
 * Interface for defining functions
 * @author termer
 * @since 1.0
 */
public interface RtflFunction {
	/**
	 * Runs the function in the provided scope.
	 * Using the current scope for execution would be akin to simply dropping the function code into the current code block.
	 * For distinct scoping inside of this function, you should use the output of scope.descend() for this method.
	 * @param args the arguments provided to this function
	 * @param runtime the Runtime to use for executing this function
	 * @param scope the scope in which to execute this function
	 * @return the return value of this function. Should be NullType if none.
	 * @throws RuntimeException if a runtime error occurs during the execution of this function
	 * @since 1.0
	 */
	public RtflType run(RtflType[] args, RtflRuntime runtime, Scope scope) throws RuntimeException;
}