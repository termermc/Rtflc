package net.termer.rtflc.instructions;

/**
 * Interface for defining Rtfl instructions
 * @author termer
 * @since 1.0
 */
public interface RtflInstruction {
	/**
	 * The name of the origin this instruction came from
	 * @return The name of this instruction's origin
	 * @since 1.0
	 */
	public String originFile();
	/**
	 * The line number this instruction came from
	 * @return The line number of this instruction's origin
	 * @since 1.0
	 */
	public int originLine();
}
