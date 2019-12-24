package net.termer.rtflc.compiler;

import java.io.IOException;

/**
 * Exception to be thrown when there's an issue compiling Rtfl code
 * @author termer
 * @since 1.0
 */
public class CompilerException extends IOException {
	private static final long serialVersionUID = 1L;

	public CompilerException(String msg) {
		super(msg);
	}
}
