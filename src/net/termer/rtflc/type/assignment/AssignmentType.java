package net.termer.rtflc.type.assignment;

import net.termer.rtflc.runtime.Scope;
import net.termer.rtflc.runtime.RuntimeException;
import net.termer.rtflc.type.RtflType;

/**
 * Special version of RtflType for types that aren't wrappers around Java types, but references and operations that can only be resolved on runtime
 * @author termer
 * @since 1.0
 */
public interface AssignmentType extends RtflType {
	/**
	 * Returns the wrapped RtflType value for this AssignmentType
	 * @param scope The scope in which to resolve this AssignmentType
	 * @return The wrapped RtflType value corresponding to this AssignmentType
	 * @throws RuntimeException If a reference is invalid, a function fails, or invalid operations are used
	 * @since 1.0
	 */
	public RtflType extractValue(Scope scope) throws RuntimeException;
}
