package net.termer.rtflc.runtime;

import net.termer.rtflc.instructions.RtflInstruction;

/**
 * Exception to be thrown when an Rtfl runtime fails to execute an instruction
 * @author termer
 * @since 1.0
 */
public class RuntimeException extends Exception {
	private RtflInstruction _cause = null;
	
	public RuntimeException(String msg) {
		super(msg);
	}
	public RuntimeException(String msg, RtflInstruction cause) {
		super(msg);
		_cause = cause;
	}
	
	/**
	 * Returns the instruction that caused the runtime to fail
	 * @return The instruction that caused the runtime to fail
	 * @since 1.0
	 */
	public RtflInstruction cause() {
		return _cause;
	}
	
	private static final long serialVersionUID = 1L;
}