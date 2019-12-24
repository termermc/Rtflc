package net.termer.rtflc.producers;

/**
 * Exception to be thrown when an instruction producer fails
 * @author termer
 * @since 1.0
 */
public class ProducerException extends Exception {
	private static final long serialVersionUID = 1L;
	
	private String originFile = "unknown";
	private int originLine = 0;
	
	private String excMsg = null;
	
	public ProducerException(String msg) {
		excMsg = msg;
	}
	public ProducerException(String msg, String file, int line) {
		excMsg = msg;
		originFile = file;
		originLine = line;
	}
	
	/**
	 * The origin of the instruction that caused the producer to fail
	 * @return The origin of instruction that caused the producer to fail
	 * @since 1.0
	 */
	public String getOriginFile() {
		return originFile;
	}
	/**
	 * The origin line of the instruction that caused the producer to fail
	 * @return The origin line of the instruction that caused the producer to fail
	 * @since 1.0
	 */
	public int getOriginLine() {
		return originLine;
	}
	
	/**
	 * Returns the formatted error message for this exception.
	 * Format: "originFile:originLine message"
	 * @return The formatted error message for this exception
	 * @since 1.0
	 */
	public String getMessage() {
		return originFile+':'+originLine+' '+excMsg;
	}
}