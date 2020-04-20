package net.termer.rtflc.runtime;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import net.termer.rtflc.compiler.RtflCompiler;
import net.termer.rtflc.instructions.*;
import net.termer.rtflc.producers.BytecodeInstructionProducer;
import net.termer.rtflc.producers.ProducerException;
import net.termer.rtflc.producers.SourcecodeInstructionProducer;
import net.termer.rtflc.runtime.RtflFunction;
import net.termer.rtflc.runtime.RuntimeException;
import net.termer.rtflc.runtime.Scope;
import net.termer.rtflc.runtime.StandardFunctions;
import net.termer.rtflc.type.ArrayType;
import net.termer.rtflc.type.MapType;
import net.termer.rtflc.type.NullType;
import net.termer.rtflc.type.NumberType;
import net.termer.rtflc.type.RtflType;
import net.termer.rtflc.type.StringType;
import net.termer.rtflc.type.assignment.AssignmentType;
import net.termer.rtflc.utils.CacheInstructionConsumer;
import net.termer.rtflc.utils.RtflFunctionBuilder;

/**
 * Main Rtfl runtime class. Contains all components necessary to execute files, source code, and raw instructions.
 * @author termer
 * @since 1.0
 */
public class RtflRuntime {
	// Static methods
	/**
	 * Returns whether the stream provided contains the four byte signature for compiled Rtfl files.
	 * WARNING: Reads four bytes from the provided stream.
	 * @param in The InputStream to read
	 * @return Whether the stream contains a compiled Rtfl file's signature
	 * @throws IOException If reading from the stream fails
	 * @since 1.0
	 */
	public static boolean isCompiledScript(InputStream in) throws IOException {
		byte[] signature = new byte[4];
		in.read(signature);
		
		return new String(signature).equals("\001\003\003\007");
	}
	/**
	 * Returns the metadata of a compiled Rtfl file.
	 * Requires isCompiledScript() to be run on the same InputStream that is provided, or have four bytes read. 
	 * @param in The InputStream from which to read metadata
	 * @return The metadata of the compiled Rtfl file
	 * @throws IOException If reading from the stream fails
	 * @since 1.0
	 */
	public static RtflMetadata readCompiledMetadata(InputStream in) throws IOException {
		int compVer = in.read();
		int rtflVer =  in.read();
		
		// Read these after versions
		int nameLen = in.read();
		byte[] filename = new byte[nameLen];
		in.read(filename);
		boolean hasLineNums = in.read() > 0;
		
		return new RtflMetadata(new String(filename), compVer, rtflVer, hasLineNums);
	}
	
	private ConcurrentHashMap<String, RtflFunction> _functions = new ConcurrentHashMap<String, RtflFunction>();
	private ConcurrentHashMap<String, RtflType> _variables = new ConcurrentHashMap<String, RtflType>();
	private ConcurrentHashMap<Integer, LocalVar> _localVars = new ConcurrentHashMap<Integer, LocalVar>();
	private GarbageCollector _gc = null;
	private BufferedReader _terminalIn = null;
	
	private int _nextVarId = 0;
	
	private Scope _topScope = new Scope(this, new HashMap<String, Integer>(), null);
	
	/**
	 * Instantiates a new Rtfl runtime
	 * @since 1.0
	 */
	public RtflRuntime() {
		// Start garbage collector
		_gc = new GarbageCollector(20*1000, this);
		_gc.setDaemon(true);
		_gc.setName("RtflGC-"+newId());
		_gc.start();
	}
	
	// Terminal input functions
	/**
	 * Opens terminal input
	 * @return this, to be used fluently
	 * @since 1.0
	 */
	public RtflRuntime openTerminal() {
		if(_terminalIn == null)
			_terminalIn = new BufferedReader(new InputStreamReader(System.in));
		
		return this;
	}
	/**
	 * Closes terminal input
	 * @throws RuntimeException If closing terminal input fails
	 * @return this, to be used fluently
	 * @since 1.0
	 */
	public RtflRuntime closeTerminal() throws RuntimeException {
		if(_terminalIn != null) {
			try {
				_terminalIn.close();
			} catch (IOException e) {
				throw new RuntimeException("Failed to close terminal: "+e.getMessage());
			} finally {
				_terminalIn = null;
			}
		}
		
		return this;
	}
	/**
	 * Returns if terminal input is open
	 * @return Whether terminal input is open
	 * @since 1.0
	 */
	public boolean terminalOpen() {
		return _terminalIn != null;
	}
	/**
	 * Reads a line from the terminal if terminal input is open
	 * @return The line read from the terminal
	 * @throws RuntimeException If reading the input fails
	 * @since 1.0
	 */
	public String readTerminal() throws RuntimeException {
		String in = null;
		
		if(_terminalIn == null)
			throw new RuntimeException("Terminal is not open");
		
		try {
			in = _terminalIn.readLine();
		} catch (IOException e) {
			throw new RuntimeException("Error reading terminal input: "+e.getMessage());
		}
		return in;
	}
	
	/**
	 * Executes Rtfl instructions at the top scope
	 * @param instructions The instructions to execute
	 * @return The value returned by the executed instructions, a NullType if nothing is returned
	 * @throws RuntimeException If there is an error while executing instructions
	 * @since 1.0
	 */
	public RtflType execute(RtflInstruction[] instructions) throws RuntimeException {
		return execute(instructions, _topScope);
	}
	/**
	 * Executes Rtfl instructions
	 * @param instructions The instructions to execute
	 * @param scope The scope in which to run the instructions
	 * @return The value returned by the executed instructions, a NullType if nothing is returned
	 * @throws RuntimeException If there is an error while executing instructions
	 * @since 1.0
	 */
	public RtflType execute(RtflInstruction[] instructions, Scope scope) throws RuntimeException {
		return execute(instructions, scope, false);
	}
	/**
	 * Executes a String of Rtfl code
	 * @param code The Rtfl code to execute
	 * @return The value returned by the executed instructions, a NullType if nothing is returned
	 * @throws RuntimeException If there is an error while executing instructions
	 * @throws IOException If reading the code String fails
	 * @throws ProducerException If parsing/reading the instructions fails
	 * @since 1.0
	 */
	public RtflType execute(String code) throws RuntimeException, IOException, ProducerException {
		return execute(code, _topScope);
	}
	/**
	 * Executes a String of Rtfl code
	 * @param code The Rtfl code to execute
	 * @param scope The scope in which to run the code
	 * @return The value returned by the executed instructions, a NullType if nothing is returned
	 * @throws RuntimeException If there is an error while executing instructions
	 * @throws IOException If reading the code String fails
	 * @throws ProducerException If parsing/reading the instructions fails
	 * @since 1.0
	 */
	public RtflType execute(String code, Scope scope) throws RuntimeException, IOException, ProducerException {
		CacheInstructionConsumer cache = new CacheInstructionConsumer();
		
		SourcecodeInstructionProducer.produce("eval", new ByteArrayInputStream(code.getBytes()), cache);
		
		return execute(cache.cache.toArray(new RtflInstruction[0]), scope, true);
	}
	/**
	 * Executes Rtfl instructions asynchronously
	 * @param instructions The instructions to execute
	 * @param scope The scope in which to run the instructions
	 * @return this, to be used fluently
	 * @since 1.0
	 */
	public RtflRuntime executeAsync(RtflInstruction[] instructions, Scope scope) {
		Thread asyncThread = new Thread(() -> {
				try {
					execute(instructions, scope, true);
				} catch (RuntimeException e) {
					String where = e.cause() == null ? "unknown:0" : e.cause().originFile()+':'+e.cause().originLine();
					System.err.println("(async) "+where+' '+e.getMessage());
				}
		});
		asyncThread.setName("RtflWorker-"+this.newId());
		
		// Add ownership of local variables
		for(int localId : scope.variableAliases().values())
			_localVars.get(localId).addOwner(asyncThread.getName());
		
		// Begin execution
		asyncThread.start();
		
		return this;
	}
	
	/**
	 * Executes an Rtfl file (script or bytecode) at top level
	 * @param file The file to execute
	 * @return The RtflType value returned by the file, a NullType if nothing
	 * @throws IOException If reading the file fails
	 * @throws RuntimeException If executing the file fails
	 * @throws ProducerException If parsing/reading the file fails
	 * @since 1.0
	 */
	public RtflType executeFile(File file) throws IOException, RuntimeException, ProducerException {
		return executeFile(file, _topScope);
	}
	/**
	 * Executes an Rtfl file (script or bytecode)
	 * @param file The file to execute
	 * @param scope The scope in which to execute te file
	 * @return The RtflType value returned by the file, a NullType if nothing
	 * @throws IOException If reading the file fails
	 * @throws RuntimeException If executing the file fails
	 * @throws ProducerException If parsing/reading the file fails
	 * @since 1.0
	 */
	public RtflType executeFile(File file, Scope scope) throws IOException, RuntimeException, ProducerException {
		CacheInstructionConsumer cache = new CacheInstructionConsumer();
		RtflType result = null;
		
		if(file.exists()) {
			if(file.isFile()) {
				FileInputStream fin = new FileInputStream(file);
				
				// Check if file is bytecode or a script
				if(isCompiledScript(fin)) {
					// Read file metadata
					RtflMetadata meta = readCompiledMetadata(fin);
					
					if(meta.rtflVersion > RtflCompiler.RTFL_VERSION)
						throw new RuntimeException("Binary was compiled for a newer version of Rtfl (compiled for "+meta.rtflVersion+", running "+RtflCompiler.RTFL_VERSION+')');
					
					// Read bytecode
					BytecodeInstructionProducer.produce(meta.fileName, fin, cache, meta.hasLineNumbers);
				} else {
					fin.close();
					fin = new FileInputStream(file);
					
					// Parse script
					SourcecodeInstructionProducer.produce(file.getName(), fin, cache);
				}
				
				// Execute instructions
				result = execute(cache.cache.toArray(new RtflInstruction[0]), scope);
			} else {
				throw new RuntimeException("Provided path is not a file");
			}
		} else {
			throw new RuntimeException("Provided file does not exist");
		}
		
		return result;
	}
	
	/**
	 * Executes Rtfl instructions
	 * @param instructions The instructions to execute
	 * @param scope The scope in which to run the instructions
	 * @param disownAll Whether to delete all local variables after execution (should only be used on top-level execution)
	 * @return The value returned by the executed instructions, a NullType if nothing is returned
	 * @throws RuntimeException If there is an error while executing instructions
	 * @since 1.0
	 */
	@SuppressWarnings("unchecked")
	public RtflType execute(RtflInstruction[] instructions, Scope scope, boolean disownAll) throws RuntimeException {
		RtflType val = new NullType();
		ArrayList<Integer> localIds = new ArrayList<Integer>();
		
		for(int i = 0; i < instructions.length; i++) {
			RtflInstruction inst = instructions[i];
			
			try {
				// Check instructions
				if(inst instanceof VarDefInstruction) {
					VarDefInstruction ins = (VarDefInstruction) inst;
					_variables.put(ins.variableName(), resolveValue(ins.variableValue(), scope));
				} else if(inst instanceof VarLocalDefInstruction) {
					VarLocalDefInstruction ins = (VarLocalDefInstruction) inst;
					
					int varId = scope.createLocalVar(ins.variableName(), resolveValue(ins.variableValue(), scope));
					localIds.add(varId);
				} else if(inst instanceof VarAssignInstruction) {
					VarAssignInstruction ins = (VarAssignInstruction) inst;
					
					scope.assignVar(ins.variableName(), resolveValue(ins.assignValue(), scope));
				} else if(inst instanceof ArrayAssignInstruction) {
					ArrayAssignInstruction ins = (ArrayAssignInstruction) inst;
					
					RtflType array = resolveValue(ins.array(), scope);
					RtflType index = resolveValue(ins.index(), scope);
					RtflType value = resolveValue(ins.assignValue(), scope);
					
					if(!(array instanceof ArrayType))
						throw new RuntimeException("Cannot get element from non-array", inst);
					if(!(index instanceof NumberType))
						throw new RuntimeException("Provided non-number index");
					
					((ArrayList<RtflType>) array.value()).set(((NumberType) index).toInt(), value);
				} else if(inst instanceof MapAssignInstruction) {
					MapAssignInstruction ins = (MapAssignInstruction) inst;
					
					RtflType map = resolveValue(ins.map(), scope);
					String field = ins.field();
					RtflType value = resolveValue(ins.assignValue(), scope);
					
					if(!(map instanceof MapType))
						throw new RuntimeException("Cannot get field from non-map", inst);
					
					((ConcurrentHashMap<String, RtflType>) map.value()).put(field, value);
				} else if(inst instanceof VarUndefInstruction) {
					VarUndefInstruction ins = (VarUndefInstruction) inst;
					
					int undefId = scope.undefineVar(ins.variableName());
					if(undefId > -1)
						localIds.remove(new Integer(undefId));
				} else if(inst instanceof FuncCallInstruction) {
					FuncCallInstruction ins = (FuncCallInstruction) inst;
					scope.function(ins.functionName()).run(
						resolveValues(ins.functionArguments(), scope),
						this,
						scope.descend(ins)
					);
				} else if(inst instanceof ReturnInstruction) {
					ReturnInstruction ins = (ReturnInstruction) inst;
					val = resolveValue(ins.returnValue(), scope);
				} else if(inst instanceof IfInstruction) {
					IfInstruction ins = (IfInstruction) inst;
					
					// Check condition
					RtflType cond = resolveValue(ins.condition(), scope);
					boolean exec = false;
					if(cond instanceof NumberType) {
						exec = ((NumberType) cond).toDouble() > 0;
					} else {
						throw new RuntimeException("Non-number/bool value provided for 'if' instruction", inst);
					}
					
					// Cache `if` body instructions if condition is true
					int level = 1;
					ArrayList<RtflInstruction> instCache = new ArrayList<RtflInstruction>();
					// Loop through instructions
					for(int j = i+1; j < instructions.length && level > 0; j++) {
						RtflInstruction _inst = instructions[j];
						if(_inst instanceof ClauseOpenerInstruction) {
							level++;
							if(exec)
								instCache.add(_inst);
						} else if(_inst instanceof EndClauseInstruction) {
							level--;
							if(level > 0 && exec)
								instCache.add(_inst);
							else
								i = j;
						} else if(exec) {
							instCache.add(_inst);
						}
					}
					
					// Execute instructions if condition is true
					if(exec)
						execute(instCache.toArray(new RtflInstruction[0]), scope.descend(inst));
				} else if(inst instanceof WhileInstruction) {
					WhileInstruction ins = (WhileInstruction) inst;
					
					// Cache `while` body instructions
					int level = 1;
					ArrayList<RtflInstruction> instCache = new ArrayList<RtflInstruction>();
					// Loop through instructions
					for(int j = i+1; j < instructions.length && level > 0; j++) {
						RtflInstruction _inst = instructions[j];
						if(_inst instanceof ClauseOpenerInstruction) {
							level++;
							instCache.add(_inst);
						} else if(_inst instanceof EndClauseInstruction) {
							level--;
							if(level > 0)
								instCache.add(_inst);
							else
								i = j;
						} else {
							instCache.add(_inst);
						}
					}
					
					// Loop instructions
					while(true) {
						RtflType cond = resolveValue(ins.condition(), scope);
						// Check condition
						if(cond instanceof NumberType) {
							if(((NumberType) cond).toDouble() > 0)
								execute(instCache.toArray(new RtflInstruction[0]), scope.descend(inst));
							else
								break;
						} else {
							throw new RuntimeException("Non-number/bool value provided for 'while' instruction", inst);
						}
					}
				} else if(inst instanceof TryInstruction) {
					TryInstruction ins = (TryInstruction) inst;
					
					// Cache `error`/`try` body instructions
					int level = 1;
					ArrayList<RtflInstruction> instCache = new ArrayList<RtflInstruction>();
					// Loop through instructions
					for(int j = i+1; j < instructions.length && level > 0; j++) {
						RtflInstruction _inst = instructions[j];
						if(_inst instanceof ClauseOpenerInstruction) {
							level++;
							instCache.add(_inst);
						} else if(_inst instanceof EndClauseInstruction) {
							level--;
							if(level > 0)
								instCache.add(_inst);
							else
								i = j;
						} else {
							instCache.add(_inst);
						}
					}
					
					scope.createLocalVar(ins.variableName(), new StringType("ok"));
					try {
						execute(instCache.toArray(new RtflInstruction[0]), scope.descend(inst));
					} catch(RuntimeException e) {
						scope.assignVar(ins.variableName(), new StringType(e.getMessage()));
					}
				} else if(inst instanceof EndClauseInstruction) {
					// No action is needed
				} else if(inst instanceof FuncDefInstruction) {
					FuncDefInstruction ins = (FuncDefInstruction) inst;
					
					// Fetch function body instructions
					int level = 1;
					ArrayList<RtflInstruction> instCache = new ArrayList<RtflInstruction>();
					// Loop through instructions
					for(int j = i+1; j < instructions.length && level > 0; j++) {
						RtflInstruction _inst = instructions[j];
						if(_inst instanceof ClauseOpenerInstruction) {
							level++;
							instCache.add(_inst);
						} else if(_inst instanceof EndClauseInstruction) {
							level--;
							if(level > 0)
								instCache.add(_inst);
							else
								i = j;
						} else {
							instCache.add(_inst);
						}
					}
					
					// Create function
					_functions.put(ins.functionName(), new InstructionFunction(instCache.toArray(new RtflInstruction[0]), ins.argumentNames()));
				} else if(inst instanceof FuncUndefInstruction) {
					FuncUndefInstruction ins = (FuncUndefInstruction) inst;
					
					// Remove function by name
					_functions.remove(ins.functionName());
				} else if(inst instanceof AsyncInstruction) {
					// Fetch clause body instructions
					int level = 1;
					ArrayList<RtflInstruction> instCache = new ArrayList<RtflInstruction>();
					// Loop through instructions
					for(int j = i+1; j < instructions.length && level > 0; j++) {
						RtflInstruction _inst = instructions[j];
						if(_inst instanceof ClauseOpenerInstruction) {
							level++;
							instCache.add(_inst);
						} else if(_inst instanceof EndClauseInstruction) {
							level--;
							if(level > 0)
								instCache.add(_inst);
							else
								i = j;
						} else {
							instCache.add(_inst);
						}
					}
					
					// Execute instructions asynchronously
					executeAsync(instCache.toArray(new RtflInstruction[0]), scope.descend(inst));
				} else if(inst instanceof DescendScopeInstruction) {
					// Descend the current operating scope
					scope = scope.descend(inst);
				} else if(inst instanceof AscendScopeInstruction) {
					// Ascend the current operating scope
					scope = scope.parent();
				}
			} catch(RuntimeException e) {
				// Remove ownership of variables to avoid leaks
				if(disownAll) {
					for(int localId : scope.variableAliases().values())
						if(_localVars.containsKey(localId))
							_localVars.get(localId).removeOwner(Thread.currentThread().getName());
				} else {
					for(int localId : localIds)
						if(_localVars.containsKey(localId))
							_localVars.get(localId).removeOwner(Thread.currentThread().getName());
				}
				
				// Add cause to exception if not present and throw again
				if(e.cause() == null)
					throw new RuntimeException(e.getMessage(), inst);
				else
					throw e;
			}
		}
		
		// Remove ownership of vars created in this execution
		if(disownAll) {
			for(int localId : scope.variableAliases().values())
				if(_localVars.containsKey(localId))
					_localVars.get(localId).removeOwner(Thread.currentThread().getName());
		} else {
			for(int localId : localIds)
				if(_localVars.containsKey(localId))
					_localVars.get(localId).removeOwner(Thread.currentThread().getName());
		}
		
		return val;
	}
	
	/**
	 * Returns all currently loaded functions
	 * @return All functions
	 * @since 1.0
	 */
	public ConcurrentHashMap<String, RtflFunction> functions() {
		return _functions;
	}
	/**
	 * Returns all global variables
	 * @return All global variables
	 * @since 1.0
	 */
	public ConcurrentHashMap<String, RtflType> globalVarables() {
		return _variables;
	}
	/**
	 * Returns all local variables. The map contains the values of the locals along with their IDs as keys.
	 * @return All local variables
	 * @since 1.0
	 */
	public ConcurrentHashMap<Integer, LocalVar> localVariables() {
		return _localVars;
	}
	/**
	 * Returns this runtime's garbage collector daemon
	 * @return The garbage collector daemon
	 * @since 1.0
	 */
	public GarbageCollector garbageCollector() {
		return _gc;
	}
	
	/**
	 * Imports all Rtfl standard functions into this Runtime
	 * @return this, to be used fluently
	 * @since 1.0
	 */
	public RtflRuntime importStandard() {
		_functions.putAll(new StandardFunctions().functions());
		return this;
	}
	/**
	 * Imports all Java interop functions into this Runtime
	 * @return this, to be used fluently
	 * @since 1.0
	 */
	public RtflRuntime importJavaInterop() {
		new JavaInteropFunctions(this);
		
		return this;
	}
	
	/**
	 * Exposes a Java method to this runtime as an Rtfl function
	 * @param object The object containing the method 
	 * @param name The name of the method
	 * @param parameters The method's parameter types
	 * @param importName What the new function should be named
	 * @return This, to the used fluently
	 * @throws NoSuchMethodException If the specified method (and its specified parameters) does not exist
	 * @throws SecurityException If the method cannot be accessed
	 * @since 1.0
	 */
	public RtflRuntime exposeMethodAs(Object object, String name, Class<?>[] parameters, String importName) throws NoSuchMethodException, SecurityException {
		_functions.put(importName, RtflFunctionBuilder.fromMethod(object.getClass(), name, parameters, object));
		
		return this;
	}
	/**
	 * Exposes a Java method to this runtime as an Rtfl function
	 * @param object The object containing the method 
	 * @param name The name of the method
	 * @param parameters The method's parameter types
	 * @return This, to the used fluently
	 * @throws NoSuchMethodException If the specified method (and its specified parameters) does not exist
	 * @throws SecurityException If the method cannot be accessed
	 * @since 1.0
	 */
	public RtflRuntime exposeMethod(Object object, String name, Class<?>[] parameters) throws NoSuchMethodException, SecurityException {
		_functions.put(name, RtflFunctionBuilder.fromMethod(object.getClass(), name, parameters, object));
		
		return this;
	}
	/**
	 * Exposes a static Java method to this runtime as an Rtfl function
	 * @param clazz The class containing the static method 
	 * @param name The name of the method
	 * @param parameters The method's parameter types
	 * @param importName What the new function should be named
	 * @return This, to the used fluently
	 * @throws NoSuchMethodException If the specified method (and its specified parameters) does not exist
	 * @throws SecurityException If the method cannot be accessed
	 * @since 1.0
	 */
	public RtflRuntime exposeStaticMethodAs(Class<?> clazz, String name, Class<?>[] parameters, String importName) throws NoSuchMethodException, SecurityException {
		_functions.put(importName, RtflFunctionBuilder.fromStaticMethod(clazz, name, parameters));
		
		return this;
	}
	/**
	 * Exposes a static Java method to this runtime as an Rtfl function
	 * @param clazz The class containing the static method 
	 * @param name The name of the method
	 * @param parameters The method's parameter types
	 * @return This, to the used fluently
	 * @throws NoSuchMethodException If the specified method (and its specified parameters) does not exist
	 * @throws SecurityException If the method cannot be accessed
	 * @since 1.0
	 */
	public RtflRuntime exposeStaticMethod(Class<?> clazz, String name, Class<?>[] parameters) throws NoSuchMethodException, SecurityException {
		_functions.put(name, RtflFunctionBuilder.fromStaticMethod(clazz, name, parameters));
		
		return this;
	}
	
	/**
	 * Generates a new unique integer ID
	 * @return the new ID
	 * @since 1.0
	 */
	public int newId() {
		return _nextVarId++;
	}
	
	// Extracts values of the provided RtflType, including executing extractValue() if it is an AssignmentType
	private RtflType resolveValue(RtflType value, Scope scope) throws RuntimeException {
		RtflType val = value;
		
		if(val instanceof AssignmentType) {
			val = ((AssignmentType) value).extractValue(scope);
		}
		
		return val;
	}
	// Plural version of resolveValue()
	private RtflType[] resolveValues(RtflType[] values, Scope scope) throws RuntimeException {
		RtflType[] vals = new RtflType[values.length];
		
		for(int i = 0; i < values.length; i++) {
			if(values[i] instanceof AssignmentType) {
				vals[i] = ((AssignmentType) values[i]).extractValue(scope);
			} else {
				vals[i] = values[i];
			}
		}
		
		return vals;
	}
	
	public static class LocalVar {
		public RtflType value = null;
		public ArrayList<String> _owners = new ArrayList<String>();
		public boolean notInUse = false;
		
		public LocalVar(RtflType val, String thread) {
			value = val;
			_owners.add(thread);
		}
		
		public void addOwner(String thread) {
			_owners.add(thread);
		}
		public void removeOwner(String thread) {
			_owners.remove(thread);
			
			// Notify runtime that it is ready for garbage collection if not in use
			if(_owners.size() < 1)
				notInUse = true;
		}
	}
	
	/**
	 * Garbage collector daemon for local values no longer in use
	 * @author termer
	 * @since 1.0
	 */
	public class GarbageCollector extends Thread {
		// The interval at which the collector is run
		private long _interval = -1;
		// The interval adjustment (for smart collection)
		private long _adjustment = 0;
		// Whether the collector is paused
		private boolean _paused = false;
		// The runtime that owns this garbage collector
		private RtflRuntime _rt = null;
		
		/**
		 * Creates a new garbage collector daemon
		 * @param interval The interval at which the collector runs
		 * @param rt The runtime this garbage collector manages
		 * @since 1.0
		 */
		public GarbageCollector(long interval, RtflRuntime rt) {
			_interval = interval;
			_rt = rt;
		}
		
		/**
		 * Pauses collector execution
		 * @since 1.0
		 */
		public void pause() {
			_paused = true;
		}
		/**
		 * Resumes collector execution
		 * @since 1.0
		 */
		public void unpause() {
			_paused = false;
		}
		/**
		 * Sets whether the collector is paused
		 * @param paused Whether the collector is paused
		 * @since 1.0
		 */
		public void paused(boolean paused) {
			_paused = paused;
		}
		
		/**
		 * Returns whether the garbage collector is paused
		 * @return whether the garbage collector is paused
		 * @since 1.0
		 */
		public boolean paused() {
			return _paused;
		}
		
		/**
		 * Runs the garbage collector
		 * @return The amount of variables that were deleted on this run
		 * @since 1.0
		 */
		public int collect() {
			int deleted = 0;
			
			// Loop through variables and delete those that are not in use
			Integer[] varIds = _rt._localVars.keySet().toArray(new Integer[0]);
			for(int id : varIds) {
				// Check if var is deleted (and not null)
				if(_rt._localVars.get(id) != null && _rt._localVars.get(id).notInUse) {
					_rt._localVars.remove(id);
					deleted++;
				}
			}
			
			return deleted;
		}
		
		public void run() {
			// Start collection daemon
			while(true) {
				try {
					Thread.sleep(_interval+_adjustment);
					
					// Run collector
					collect();
				} catch (InterruptedException e) {
					if(!_paused) {
						System.err.println("Failed to put garbage collector to sleep:");
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	/**
	 * Utility class to store Rtfl compiled file metadata
	 * @author termer
	 * @since 1.0
	 */
	public static class RtflMetadata {
		/**
		 * The name of the original source file
		 */
		public final String fileName;
		/**
		 * The version of the compiler used to compile the binary
		 */
		public final int compilerVersion;
		/**
		 * The version of Rtfl this was compiled for
		 */
		public final int rtflVersion;
		/**
		 * Whether this binary includes the original line numbers of instructions
		 */
		public final boolean hasLineNumbers;
		
		public RtflMetadata(String file, int compVer, int rtflVer, boolean lineNumbers) {
			fileName = file;
			compilerVersion = compVer;
			rtflVersion = rtflVer;
			hasLineNumbers = lineNumbers;
		}
	}
}
