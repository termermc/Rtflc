package net.termer.rtflc.utils;

import java.io.IOException;
import java.util.ArrayList;

import net.termer.rtflc.consumers.InstructionConsumer;
import net.termer.rtflc.instructions.RtflInstruction;
import net.termer.rtflc.runtime.RuntimeException;

/**
 * Basic class to cache instructions from a producer
 * @author termer
 * @since 1.0
 */
public class CacheInstructionConsumer implements InstructionConsumer {
	public ArrayList<RtflInstruction> cache = new ArrayList<RtflInstruction>();
	
	public void consume(RtflInstruction instruction) throws IOException {
		cache.add(instruction);
	}

	public void finish() throws RuntimeException {}
	
}