package net.termer.rtflc.consumers;

import java.io.IOException;

import net.termer.rtflc.instructions.RtflInstruction;
import net.termer.rtflc.runtime.RuntimeException;

/**
 * Interface defining InstructionConsumers, classes that take in RtflInstruction objects and do various things with them
 * @author termer
 * @since 1.0
 */
public interface InstructionConsumer {
	/**
	 * Takes in instruction
	 * @param instruction The instruction to take in
	 * @throws IOException If dealing with the instruction fails
	 * @since 1.0
	 */
	public void consume(RtflInstruction instruction) throws IOException;
	/**
	 * Tells this consumer that it is finished receiving instructions
	 * @throws RuntimeException If dealing with the instructions taken in fails
	 * @since 1.0
	 */
	public void finish() throws RuntimeException;
}
