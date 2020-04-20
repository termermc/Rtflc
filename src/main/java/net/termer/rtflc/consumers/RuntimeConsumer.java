package net.termer.rtflc.consumers;

import java.io.IOException;
import java.util.ArrayList;

import net.termer.rtflc.instructions.RtflInstruction;
import net.termer.rtflc.runtime.RtflRuntime;
import net.termer.rtflc.runtime.RuntimeException;

/**
 * InstructionConsumer implementation that executes RtflInstruction objects
 * @author termer
 * @since 1.0
 */
public class RuntimeConsumer implements InstructionConsumer {
	// Cached instructions
	private ArrayList<RtflInstruction> _insts = new ArrayList<RtflInstruction>();
	private RtflRuntime _runtime = null;
	
	public RuntimeConsumer(RtflRuntime runtime) {
		_runtime = runtime;
	}
	
	public void consume(RtflInstruction instruction) throws IOException {
		// Cache instruction
		_insts.add(instruction);
	}
	public void finish() throws RuntimeException {
		// Execute all cached instructions
		_runtime.execute(_insts.toArray(new RtflInstruction[0]));
		// Clear cache
		_insts.clear();
	}
}