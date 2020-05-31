package net.termer.rtflc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import net.termer.rtflc.type.ArrayType;
import net.termer.rtflc.type.RtflType;
import net.termer.rtflc.type.StringType;
import net.termer.rtflc.utils.ArgParser;
import net.termer.rtflc.compiler.CompilerOptions;
import net.termer.rtflc.compiler.RtflCompiler;
import net.termer.rtflc.producers.ProducerException;
import net.termer.rtflc.runtime.RtflRuntime;
import net.termer.rtflc.runtime.RuntimeException;

public class Main {
	public static final double RTFLC_VERSION = 1.3;
	public static final double RTFL_VERSION = 1.4;
	
	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
		ArgParser arg = new ArgParser(args);
		
		if(arg.option("help") || arg.flag('h')) {
			// Print program help
			String jarName = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getName();
			System.out.println(
				"java -jar "+jarName+" <SCRIPT/BINARY> [OPTIONS]\n" + 
				"\n" + 
				"-h, --help                      prints this message\n" +
				"-v, --version                   prints the version of Rtfl supported and the version of Rtflc running\n" +
				"-c, --compile                   compiles the specified script\n" + 
				"-t, --time                      displays the time it took to execute or compile the provided script/binary (in milliseconds)\n" + 
				"-l, --compile-literal-loads     compiles all scripts or binaries that are referenced with `load()` calls with literal string paths in them, and references the compiled versions\n" + 
				"-r, --compile-literal-requires  compiles all scripts or binaries that are referenced with `require()` calls with literal string paths in them, and references the compiled versions\n" + 
				"-p, --package-literal-loads     packages all scripts or binaries that are referenced with `load()` calls into the compiled binary output instead of referencing them\n" + 
				"-e, --package-literal-requires  packages all scripts or binaries that are references with `require()` calls into the compiled binary output instead of referencing them\n" + 
				"-n, --preserve-line-numbers     preserves line numbers for instructions in compiled binaries for debugging purposes\n" +
				"-i, --disable-interop           disables Java/Rtfl interop functions\n" + 
				"--out=FILENAME                  specifies the path to output the compiled binary to\n" + 
				"\n" + 
				"Examples:\n" + 
				"  java -jar "+jarName+" script.rtfl --time  Executes script.rtfl and outputs the time it took to execute it\n" + 
				"  java -jar "+jarName+" script.rtfl --compile --package-literal-loads Compiles script.rtfl and packages all `load()` calls with literal paths specified"
			);
		} else if(arg.option("version") || arg.flag('v')) {
			System.out.println("Supporting Rtfl version "+RTFL_VERSION+", running Rtflc "+RTFLC_VERSION);
		} else if(arg.arguments().length > 0) {
			// Check if referenced file is real and not a directory
			File file = new File(arg.arguments()[0]);
			if(file.isFile()) {
				// Check if executing or compiling
				if(arg.option("compile") || arg.flag('c')) {
					// Setup compiler with command line options
					RtflCompiler compiler = new RtflCompiler(
						new CompilerOptions()
							.compileLiteralLoads(arg.option("compile-literal-loads") || arg.flag('l'))
							.compileLiteralRequires(arg.option("compile-literal-requires") || arg.flag('r'))
							.packageLiteralLoads(arg.option("package-literal-loads") || arg.flag('p'))
							.packageLiteralRequires(arg.option("package-literal-requires") || arg.flag('e'))
							.preserveLineNumbers(arg.option("preserve-line-numbers") || arg.flag('n'))
					);
					
					String outPath = file.getPath();
					
					// Resolve compile output path
					if(arg.optionString("out") != null) {
						outPath = arg.optionString("out");
					} else {
						if(outPath.endsWith(".rtfl"))
							outPath = outPath.substring(0, outPath.length()-1)+'c';
						else
							outPath = outPath+".rtfc";
					}
					
					// Compile
					try {
						// Get start time (for timing purposes)
						long startMs = System.currentTimeMillis();
						
						// Compile file
						compiler.compile(file, new FileOutputStream(outPath));
						
						// Print amount of time the compile took if specified in options
						long endMs = System.currentTimeMillis();
						if(arg.option("time") || arg.flag('t'))
							System.out.println("Took "+(endMs-startMs)+"ms to compile file");
						
					} catch (IOException | ProducerException | RuntimeException e) {
						System.err.println("Failed to compile file:");
						e.printStackTrace();
					}
				} else {
					// Create runtime and import standard library
					RtflRuntime rt = new RtflRuntime().importStandard();
					
					if(!arg.option("disable-interop") && !arg.flag('i'))
						rt.importJavaInterop();
					
					// Make launch arguments available to runtime
					ArrayType rtflArgs = new ArrayType();
					for(int i = 1; i < arg.arguments().length; i++)
						((ArrayList<RtflType>) rtflArgs.value()).add(new StringType(arg.arguments()[i]));
					rt.globalVarables().put("args", rtflArgs);
					
					try {
						// Record start time
						long startMs = System.currentTimeMillis();
						
						// Execute file
						rt.executeFile(file);
						
						// If enabled, print the time it took to read and execute file
						long endMs = System.currentTimeMillis();
						if(arg.option("time") || arg.flag('t'))
							System.out.println("Took "+(endMs-startMs)+"ms to read and execute file");
						
					} catch (IOException e) {
						System.err.println("Error while reading file:");
						e.printStackTrace();
					} catch (RuntimeException e) {
						// Print runtime error
						String where = e.cause() == null ? "unknown:0" : e.cause().originFile()+':'+e.cause().originLine();
						System.err.println(where+' '+e.getMessage());
					} catch (ProducerException e) {
						// Print compile error
						System.err.println("Failed to execute file: "+e.getMessage());
					}
				}
			} else {
				System.out.println("Specified path does not point to a file");
			}
		} else {
			System.out.println("Please provide a path to an Rtfl file or specify --help");
		}
	}
}
