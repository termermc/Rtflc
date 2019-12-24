package net.termer.rtflc.compiler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import net.termer.rtflc.consumers.CompilerInstructionConsumer;
import net.termer.rtflc.consumers.InstructionConsumer;
import net.termer.rtflc.instructions.AscendScopeInstruction;
import net.termer.rtflc.instructions.DescendScopeInstruction;
import net.termer.rtflc.instructions.FuncCallInstruction;
import net.termer.rtflc.instructions.RtflInstruction;
import net.termer.rtflc.producers.BytecodeInstructionProducer;
import net.termer.rtflc.producers.ProducerException;
import net.termer.rtflc.producers.SourcecodeInstructionProducer;
import net.termer.rtflc.runtime.RtflRuntime;
import net.termer.rtflc.runtime.RtflRuntime.RtflMetadata;
import net.termer.rtflc.runtime.RuntimeException;
import net.termer.rtflc.type.RtflType;
import net.termer.rtflc.type.StringType;

/**
 * Main Rtfl compiler class. Can compile Rtfl files using options provided.
 * @author termer
 * @since 1.0
 */
public class RtflCompiler {
	public static final int COMPILER_VERSION = 0;
	public static final int RTFL_VERSION = 4;
	
	private final CompilerOptions _options;
	private ArrayList<String> _requires = new ArrayList<String>();
	private ArrayList<String> _loads = new ArrayList<String>();
	
	/**
	 * Instantiates a new Rtfl compiler with the provided settings
	 * @param options
	 */
	public RtflCompiler(CompilerOptions options) {
		_options = options;
	}
	
	/**
	 * Returns this compiler's options.
	 * @return This compiler's options
	 * @since 1.0
	 */
	public CompilerOptions options() {
		return _options;
	}
	/**
	 * Returns a list of require() functions that have already been compiled or packaged
	 * @return A list of compiled or packaged require()s
	 * @since 1.0
	 */
	public ArrayList<String> requiresLoaded() {
		return _requires;
	}
	/**
	 * Returns a list of load() functions that have already been compiled
	 * @return A list of compiled load()s
	 * @since 1.0
	 */
	public ArrayList<String> loadsCompiled() {
		return _loads;
	}
	
	/**
	 * Compiles the provided Rtfl file, applying all compiler options
	 * @param file The file to read
	 * @param out The output to write
	 * @throws RuntimeException If compiling fails
	 * @throws ProducerException If parsing fails
	 * @throws IOException If reading the input fails
	 * @since 1.0
	 */
	public void compile(File file, OutputStream out) throws IOException, ProducerException, RuntimeException {
		compile(file, out, true);
	}
	/**
	 * Compiles the provided Rtfl file, applying all compiler options
	 * @param file The file to read
	 * @param out The output to write
	 * @param writeMetadata Whether to write metadata headers (for when compiling a new binary)
	 * @throws RuntimeException If compiling fails
	 * @throws ProducerException If parsing fails
	 * @throws IOException If reading the input fails
	 * @since 1.0
	 */
	public void compile(File file, OutputStream out, boolean writeMetadata) throws IOException, ProducerException, RuntimeException {
		// Buffer input stream
		FileInputStream in = new FileInputStream(file);
		
		// Initialize instruction to bytecode translator
		CompilerInstructionConsumer cons = new CompilerInstructionConsumer(
			out,
			_options.preserveLineNumbers()
		);
		
		// Compiler consumer
		CompilerConsumer comp = new CompilerConsumer(this, cons, out);
		
		// Print message
		System.out.println((writeMetadata ? "Compiling " : "Packaging ")+file.getPath()+"...");
		
		// Write metadata
		if(writeMetadata) {
			out.write(new byte[] {1, 3, 3, 7});
			out.write((byte) COMPILER_VERSION);
			out.write((byte) RTFL_VERSION);
			out.write(file.getName().length());
			out.write(file.getName().getBytes(StandardCharsets.UTF_8));
			out.write(_options.preserveLineNumbers() ? 1 : 0);
		}
		
		if(RtflRuntime.isCompiledScript(in)) {
			RtflMetadata meta = RtflRuntime.readCompiledMetadata(in);
			
			// Write a source swap meta instruction to tell the runtime this bytecode is coming from a new source
			if(!writeMetadata)
				swapSource(meta.fileName, this, out);
			
			// Produce from bytecode (effectively just copy bytecode from file into the output stream)
			BytecodeInstructionProducer.produce(file.getName(), in, comp, _options.preserveLineNumbers());
		} else {
			// Reset input stream
			in.close();
			in = new FileInputStream(file);
			
			// Produce from source
			SourcecodeInstructionProducer.produce(file.getName(), in, comp);
		}
		
		in.close();
	}
	
	// Utility consumer to translate instructions into bytecode
	private class CompilerConsumer implements InstructionConsumer {
		private RtflCompiler comp;
		private InstructionConsumer cons;
		private OutputStream out;
		
		public CompilerConsumer(RtflCompiler compiler, InstructionConsumer consumer, OutputStream output) {
			comp = compiler;
			cons = consumer;
			out = output;
		}
		
		public void consume(RtflInstruction inst) throws IOException {
			if(inst instanceof FuncCallInstruction) {
				FuncCallInstruction ins = (FuncCallInstruction) inst;
				
				boolean writeInst = true;
				
				// Check if function call contains a literal String
				if(ins.functionArguments().length > 0 && ins.functionArguments()[0] instanceof StringType && (ins.functionName().equals("load") || ins.functionName().equals("require"))) {
					String arg = (String) ins.functionArguments()[0].value();
					File file = new File(arg);
					if(ins.functionName().equals("require")) {
						// Resolve require path
						if(!arg.contains(".") && !arg.contains("/")) {
							file = new File("libs/"+arg+".rtfc");
							if(!file.isFile())
								file = new File("libs/"+arg+".rtfl");
						}
					}
					if(file.isFile()) {
						try {
							if(ins.functionName().equals("load")) {
								if(comp.options().packageLiteralLoads()) {
									// If function is `load` and --package-literal-loads is enabled, compile it and package it in the output
									
									writeInst = false;
									cons.consume(new DescendScopeInstruction());
									comp.compile(file, out, false);
									swapSource(inst.originFile(), comp, out);
									cons.consume(new AscendScopeInstruction());
								} else if(comp.options().compileLiteralLoads()) {
									// If function is `load` and --compile-literal-loads is enabled, compile it
									
									writeInst = false;
									String compPath = arg;
									if(compPath.endsWith(".rtfl"))
										compPath = compPath.substring(0, compPath.length()-1)+'c';
									else
										compPath+=".rtflc";
									
									// Compile new file if not already compiled
									if(!comp.loadsCompiled().contains(file.getCanonicalPath())) {
										if(!file.isFile())
											comp.compile(file, new FileOutputStream(compPath), true);
										comp.loadsCompiled().add(file.getCanonicalPath());
									}
									
									// Inject load to compiled script
									cons.consume(new FuncCallInstruction(inst.originFile(), inst.originLine(), "load", new RtflType[] {new StringType(compPath)}));
								}
							} else if(ins.functionName().equals("require")) {
								if(comp.options().packageLiteralRequires()) {
									// If function is `require` and --package-literal-requires is enabled, compile it and package it in the output (assuming it hasn't already)
									writeInst = false;
									if(!comp.requiresLoaded().contains(file.getCanonicalPath())) {
										cons.consume(new DescendScopeInstruction());
										comp.compile(file, out, false);
										swapSource(inst.originFile(), comp, out);
										cons.consume(new AscendScopeInstruction());
										comp.requiresLoaded().add(file.getCanonicalPath());
									}
								} else if(comp.options().compileLiteralRequires()) {
									// If function is `require` and --compile-literal-requires is enabled, compile it (assuming it hasn't already)
									writeInst = false;
									String compPath = file.getPath();
									if(compPath.endsWith(".rtfl"))
										compPath = compPath.substring(0, compPath.length()-1)+'c';
									else
										compPath+=".rtflc";
									
									// Compile new file if not already compiled
									if(!comp.requiresLoaded().contains(file.getCanonicalPath())) {
										if(!file.isFile())
											comp.compile(file, new FileOutputStream(compPath), true);
										comp.requiresLoaded().add(file.getCanonicalPath());
									}
									
									// Inject load to compiled script
									cons.consume(new FuncCallInstruction(inst.originFile(), inst.originLine(), "require", new RtflType[] {new StringType(arg)}));
								}
							}
						} catch(Exception e) {
							throw new IOException(e.getMessage());
						}
					} else {
						throw new FileNotFoundException("Path specified in "+ins.functionName()+" at "+inst.originFile()+':'+inst.originLine()+" is not a file");
					}
				}
				
				if(writeInst)
					cons.consume(inst);
			} else {
				cons.consume(inst);
			}
		}
		public void finish() throws RuntimeException {}
	}
	
	// Write a source swap instruction
	private void swapSource(String source, RtflCompiler compiler, OutputStream out) throws IOException {
		if(compiler.options().preserveLineNumbers())
			out.write(new byte[] {0,0});
		out.write(new byte[] {13, (byte) source.length()});
		out.write(source.getBytes(StandardCharsets.UTF_8));
	}
}