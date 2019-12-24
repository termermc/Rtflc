package net.termer.rtflc.runtime;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONArray;
import org.json.JSONObject;

import net.termer.rtflc.instructions.RtflInstruction;
import net.termer.rtflc.producers.ProducerException;
import net.termer.rtflc.producers.SourcecodeInstructionProducer;
import net.termer.rtflc.type.ArrayType;
import net.termer.rtflc.type.BoolType;
import net.termer.rtflc.type.IntType;
import net.termer.rtflc.type.MapType;
import net.termer.rtflc.type.NullType;
import net.termer.rtflc.type.NumberType;
import net.termer.rtflc.type.DoubleType;
import net.termer.rtflc.type.RtflType;
import net.termer.rtflc.type.StringType;
import net.termer.rtflc.utils.CacheInstructionConsumer;
import net.termer.rtflc.utils.LibraryLoader;

import static net.termer.rtflc.utils.IOUtils.*;

/**
 * Utility class containing implementations of all Rtfl standard functions
 * @author termer
 * @since 1.0
 */
public class StandardFunctions {
	private HashMap<String, RtflFunction> funcs = new HashMap<String, RtflFunction>();
	// Files loaded by the require() function
	private ArrayList<String> requiredFiles = new ArrayList<String>();
	
	public StandardFunctions() {
		/* Create functions */
		
		funcs.put("print", new RtflFunction() {
			public RtflType run(RtflType[] args, RtflRuntime runtime, Scope scope) throws RuntimeException {
				for(RtflType arg : args)
					System.out.print(arg.value());
				
				return new NullType();
			}
		});
		funcs.put("println", new RtflFunction() {
			public RtflType run(RtflType[] args, RtflRuntime runtime, Scope scope) throws RuntimeException {
				for(RtflType arg : args)
					System.out.print(arg.value());
				System.out.println();
				
				return new NullType();
			}
		});
		funcs.put("add", new RtflFunction() {
			public RtflType run(RtflType[] args, RtflRuntime runtime, Scope scope) throws RuntimeException {
				RtflType num = new NullType();
				
				if(args.length > 1) {
					if(args[0] instanceof NumberType && args[1] instanceof NumberType) {
						if(args[0] instanceof DoubleType || args[1] instanceof DoubleType) {
							num = new DoubleType(((NumberType) args[0]).toDouble() + ((NumberType) args[1]).toDouble());
						} else {
							num = new IntType(((int) args[0].value()) + ((int) args[1].value()));
						}
					}
				} else {
					throw new RuntimeException("Must provide at least 2 arguments");
				}
				
				return num;
			}
		});
		funcs.put("sub", new RtflFunction() {
			public RtflType run(RtflType[] args, RtflRuntime runtime, Scope scope) throws RuntimeException {
				RtflType num = new NullType();
				
				if(args.length > 1) {
					if(args[0] instanceof NumberType && args[1] instanceof NumberType) {
						if(args[0] instanceof DoubleType || args[1] instanceof DoubleType) {
							num = new DoubleType(((NumberType) args[0]).toDouble() - ((NumberType) args[1]).toDouble());
						} else {
							num = new IntType(((int) args[0].value()) - ((int) args[1].value()));
						}
					}
				} else {
					throw new RuntimeException("Must provide at least 2 arguments");
				}
				
				return num;
			}
		});
		funcs.put("mul", new RtflFunction() {
			public RtflType run(RtflType[] args, RtflRuntime runtime, Scope scope) throws RuntimeException {
				RtflType num = new NullType();
				
				if(args.length > 1) {
					if(args[0] instanceof NumberType && args[1] instanceof NumberType) {
						if(args[0] instanceof DoubleType || args[1] instanceof DoubleType) {
							num = new DoubleType(((NumberType) args[0]).toDouble() * ((NumberType) args[1]).toDouble());
						} else {
							num = new IntType(((int) args[0].value()) * ((int) args[1].value()));
						}
					}
				} else {
					throw new RuntimeException("Must provide at least 2 arguments");
				}
				
				return num;
			}
		});
		funcs.put("div", new RtflFunction() {
			public RtflType run(RtflType[] args, RtflRuntime runtime, Scope scope) throws RuntimeException {
				RtflType num = new NullType();
				
				if(args.length > 1) {
					if(args[0] instanceof NumberType && args[1] instanceof NumberType) {
						double dbl = ((NumberType) args[0]).toDouble() / ((NumberType) args[1]).toDouble();
						
						// Check is result is an int
						if(dbl == Math.floor(dbl) && !Double.isInfinite(dbl))
							num = new IntType((int) dbl);
						else
							num = new DoubleType(dbl);
					}
				} else {
					throw new RuntimeException("Must provide at least 2 arguments");
				}
				
				return num;
			}
		});
		funcs.put("sleep", new RtflFunction() {
			public RtflType run(RtflType[] args, RtflRuntime runtime, Scope scope) throws RuntimeException {
				if(args.length > 0) {
					if(args[0] instanceof NumberType) {
						try {
							Thread.sleep(((NumberType) args[0]).toInt());
						} catch (InterruptedException e) {
							throw new RuntimeException("Failed to sleep because of internal error: "+e.getMessage());
						}
					} else {
						throw new RuntimeException("Provided non-number argument");
					}
				} else {
					throw new RuntimeException("Must provide at least 1 argument");
				}
				
				return new NullType();
			}
		});
		funcs.put("gc", new RtflFunction() {
			public RtflType run(RtflType[] args, RtflRuntime runtime, Scope scope) {
				return new IntType(runtime.garbageCollector().collect());
			}
		});
		funcs.put("gc_pause", new RtflFunction() {
			public RtflType run(RtflType[] args, RtflRuntime runtime, Scope scope) {
				runtime.garbageCollector().pause();
				return new NullType();
			}
		});
		funcs.put("gc_resume", new RtflFunction() {
			public RtflType run(RtflType[] args, RtflRuntime runtime, Scope scope) {
				runtime.garbageCollector().unpause();
				return new NullType();
			}
		});
		funcs.put("eval", new EvalFunction(false, false));
		funcs.put("async", new EvalFunction(true, false));
		funcs.put("load", new EvalFunction(false, true));
		funcs.put("load_async", new EvalFunction(true, true));
		funcs.put("require", new RtflFunction() {
			public RtflType run(RtflType[] args, RtflRuntime runtime, Scope scope) throws RuntimeException {
				if(args.length > 0) {
					if(args[0] instanceof StringType) {
						String path = (String) args[0].value();
						File file = null;
						// Resolve file
						if(path.contains(".") || path.contains("/")) {
							file = new File(path);
						} else {
							file = new File("libs/"+path+".rtfc");
							if(!file.isFile())
								file = new File("libs/"+path+".rtfl");
						}
						
						if(!requiredFiles.contains(file.getAbsolutePath())) {
							try {
								runtime.executeFile(file, scope);
								requiredFiles.add(file.getAbsolutePath());
							} catch(FileNotFoundException e) {
								throw new RuntimeException("File/library \""+path+"\" does not exist");
							} catch (IOException | ProducerException e) {
								throw new RuntimeException("Failed to execute file: "+e.getMessage());
							}
						}
					} else {
						throw new RuntimeException("Provided non-string argument");
					}
				} else {
					throw new RuntimeException("Must provide at least 1 argument");
				}
				
				return new NullType();
			}
		});
		funcs.put("inc", new RtflFunction() {
			public RtflType run(RtflType[] args, RtflRuntime runtime, Scope scope) throws RuntimeException {
				if(args.length > 0) {
					if(args[0] instanceof StringType) {
						String varName = (String) ((StringType) args[0]).value();
						RtflType varVal = scope.varValue(varName);
						
						if(varVal instanceof DoubleType)
							scope.assignVar(varName, new DoubleType(((DoubleType) varVal).toDouble()+1));
						else
							scope.assignVar(varName, new IntType(((IntType) varVal).toInt()+1));
					} else {
						throw new RuntimeException("Provided non-string argument");
					}
				} else {
					throw new RuntimeException("Must provide at least 1 argument");
				}
				
				return new NullType();
			}
		});
		funcs.put("dec", new RtflFunction() {
			public RtflType run(RtflType[] args, RtflRuntime runtime, Scope scope) throws RuntimeException {
				if(args.length > 0) {
					if(args[0] instanceof StringType) {
						String varName = (String) ((StringType) args[0]).value();
						RtflType varVal = scope.varValue(varName);
						
						if(varVal instanceof DoubleType)
							scope.assignVar(varName, new DoubleType(((DoubleType) varVal).toDouble()-1));
						else
							scope.assignVar(varName, new IntType(((IntType) varVal).toInt()-1));
					} else {
						throw new RuntimeException("Provided non-string argument");
					}
				} else {
					throw new RuntimeException("Must provide at least 1 argument");
				}
				
				return new NullType();
			}
		});
		funcs.put("equals", new RtflFunction() {
			public RtflType run(RtflType[] args, RtflRuntime runtime, Scope scope) throws RuntimeException {
				boolean equals = false;
				
				if(args.length > 1) {
					equals = args[0].equals(args[1], scope);
				} else {
					throw new RuntimeException("Must provide at least 2 arguments");
				}
				
				return new BoolType(equals);
			}
		});
		funcs.put("more_than", new RtflFunction() {
			public RtflType run(RtflType[] args, RtflRuntime runtime, Scope scope) throws RuntimeException {
				boolean more = false;
				
				if(args.length > 1) {
					if(args[0] instanceof NumberType && args[1] instanceof NumberType) {
						more = ((NumberType) args[0]).toDouble() > ((NumberType) args[1]).toDouble();
					} else {
						throw new RuntimeException("Provided non-number argument");
					}
				} else {
					throw new RuntimeException("Must provide at least 2 arguments");
				}
				
				return new BoolType(more);
			}
		});
		funcs.put("less_than", new RtflFunction() {
			public RtflType run(RtflType[] args, RtflRuntime runtime, Scope scope) throws RuntimeException {
				boolean less = false;
				
				if(args.length > 1) {
					if(args[0] instanceof NumberType && args[1] instanceof NumberType) {
						less = ((NumberType) args[0]).toDouble() < ((NumberType) args[1]).toDouble();
					} else {
						throw new RuntimeException("Provided non-number argument");
					}
				} else {
					throw new RuntimeException("Must provide at least 2 arguments");
				}
				
				return new BoolType(less);
			}
		});
		funcs.put("not", new RtflFunction() {
			public RtflType run(RtflType[] args, RtflRuntime runtime, Scope scope) throws RuntimeException {
				BoolType inverse = null;
				
				if(args.length > 0) {
					if(args[0] instanceof NumberType) {
						inverse = new BoolType(!(((NumberType) args[0]).toDouble() > 0));
					} else {
						throw new RuntimeException("Provided non-number/bool argument");
					}
				} else {
					throw new RuntimeException("Must provide at least 1 argument");
				}
				
				return inverse;
			}
		});
		funcs.put("and", new RtflFunction() {
			public RtflType run(RtflType[] args, RtflRuntime runtime, Scope scope) throws RuntimeException {
				BoolType val = null;
				
				if(args.length > 1) {
					if(args[0] instanceof NumberType && args[1] instanceof NumberType) {
						val = new BoolType(
							((NumberType) args[0]).toDouble() > 0
							&&
							((NumberType) args[1]).toDouble() > 0
						);
					} else {
						throw new RuntimeException("Provided non-number/bool argument");
					}
				} else {
					throw new RuntimeException("Must provide at least 2 arguments");
				}
				
				return val;
			}
		});
		funcs.put("or", new RtflFunction() {
			public RtflType run(RtflType[] args, RtflRuntime runtime, Scope scope) throws RuntimeException {
				BoolType val = null;
				
				if(args.length > 1) {
					if(args[0] instanceof NumberType && args[1] instanceof NumberType) {
						val = new BoolType(
							((NumberType) args[0]).toDouble() > 0
							||
							((NumberType) args[1]).toDouble() > 0
						);
					} else {
						throw new RuntimeException("Provided non-number/bool argument");
					}
				} else {
					throw new RuntimeException("Must provide at least 2 arguments");
				}
				
				return val;
			}
		});
		funcs.put("concat", new RtflFunction() {
			public RtflType run(RtflType[] args, RtflRuntime runtime, Scope scope) throws RuntimeException {
				StringBuilder str = null;
				
				if(args.length > 1) {
					String value = args[0].value() == null ? "null" : args[0].value().toString();
					str = new StringBuilder(value);
					
					for(int i = 1; i < args.length; i++)
						str.append(args[i].value() == null ? "null" : args[i].value().toString());
					
				} else {
					throw new RuntimeException("Must provide at least 2 arguments");
				}
				
				return new StringType(str.toString());
			}
		});
		funcs.put("string_contains", new RtflFunction() {
			public RtflType run(RtflType[] args, RtflRuntime runtime, Scope scope) throws RuntimeException {
				BoolType contains = null;
				
				if(args.length > 1) {
					if(args[0] instanceof StringType && args[1] instanceof StringType) {
						String str = (String) args[0].value();
						String substr = (String) args[1].value();
						
						contains = new BoolType(str.contains(substr));
					} else {
						throw new RuntimeException("Provided non-string argument");
					}
				} else {
					throw new RuntimeException("Must provide at least 2 arguments");
				}
				
				return contains;
			}
		});
		funcs.put("string_trim", new RtflFunction() {
			public RtflType run(RtflType[] args, RtflRuntime runtime, Scope scope) throws RuntimeException {
				StringType trimmed = null;
				
				if(args.length > 0) {
					if(args[0] instanceof StringType) {
						String str = (String) args[0].value();
						
						trimmed = new StringType(str.trim());
					} else {
						throw new RuntimeException("Provided non-string argument");
					}
				} else {
					throw new RuntimeException("Must provide at least 1 argument");
				}
				
				return trimmed;
			}
		});
		funcs.put("var", new RtflFunction() {
			public RtflType run(RtflType[] args, RtflRuntime runtime, Scope scope) throws RuntimeException {
				RtflType varVal = new NullType();
				
				if(args.length > 0) {
					if(args[0] instanceof StringType) {
						String varName = (String) args[0].value();
						
						varVal = scope.varValue(varName);
					} else {
						throw new RuntimeException("Provided non-string argument");
					}
				} else {
					throw new RuntimeException("Must provide at least 1 argument");
				}
				
				return varVal;
			}
		});
		funcs.put("to_string", new RtflFunction() {
			public RtflType run(RtflType[] args, RtflRuntime runtime, Scope scope) throws RuntimeException {
				StringType val = null;
				
				if(args.length > 0) {
					val = new StringType(args[0].value() == null ? "null" : args[0].value().toString());
				} else {
					throw new RuntimeException("Must provide at least 1 argument");
				}
				
				return val;
			}
		});
		funcs.put("read_file", new RtflFunction() {
			public RtflType run(RtflType[] args, RtflRuntime runtime, Scope scope) throws RuntimeException {
				StringType val = null;
				
				if(args.length > 0) {
					if(args[0] instanceof StringType) {
						String path = (String) args[0].value();
						try {
							val = new StringType(readFile(path));
						} catch(FileNotFoundException e) {
							throw new RuntimeException("File \""+path+"\" does not exist");
						} catch(IOException e) {
							throw new RuntimeException("Error reading file \""+path+"\": "+e.getMessage());
						}
					} else {
						throw new RuntimeException("Provided non-string argument");
					}
				} else {
					throw new RuntimeException("Must provide at least 1 argument");
				}
				
				return val;
			}
		});
		funcs.put("write_file", new RtflFunction() {
			public RtflType run(RtflType[] args, RtflRuntime runtime, Scope scope) throws RuntimeException {
				if(args.length > 1) {
					if(args[0] instanceof StringType) {
						boolean append = false;
						if(args.length > 2) {
							if(args[2] instanceof BoolType)
								append = (Boolean) args[2].value();
							else
								throw new RuntimeException("Provided non-bool type for append argument");
						}
						String path = (String) args[0].value();
						String content = args[1].value() == null ? "null" : args[1].value().toString();
						try {
							writeFile(path, content, append);
						} catch(FileNotFoundException e) {
							throw new RuntimeException("File \""+path+"\" does not exist");
						} catch(IOException e) {
							throw new RuntimeException("Error writing to file \""+path+"\": "+e.getMessage());
						}
					} else {
						throw new RuntimeException("Provided non-string path");
					}
				} else {
					throw new RuntimeException("Must provide at least 2 arguments");
				}
				
				return new NullType();
			}
		});
		funcs.put("file_exists", new RtflFunction() {
			public RtflType run(RtflType[] args, RtflRuntime runtime, Scope scope) throws RuntimeException {
				BoolType val = null;
				
				if(args.length > 0) {
					if(args[0] instanceof StringType) {
						val = new BoolType(new File((String) args[0].value()).exists());
					} else {
						throw new RuntimeException("Provided non-string path");
					}
				} else {
					throw new RuntimeException("Must provide at least 1 argument");
				}
				
				return val;
			}
		});
		funcs.put("is_file", new RtflFunction() {
			public RtflType run(RtflType[] args, RtflRuntime runtime, Scope scope) throws RuntimeException {
				BoolType val = null;
				
				if(args.length > 0) {
					if(args[0] instanceof StringType) {
						val = new BoolType(new File((String) args[0].value()).isFile());
					} else {
						throw new RuntimeException("Provided non-string path");
					}
				} else {
					throw new RuntimeException("Must provide at least 1 argument");
				}
				
				return val;
			}
		});
		funcs.put("is_directory", new RtflFunction() {
			public RtflType run(RtflType[] args, RtflRuntime runtime, Scope scope) throws RuntimeException {
				BoolType val = null;
				
				if(args.length > 0) {
					if(args[0] instanceof StringType) {
						val = new BoolType(new File((String) args[0].value()).isDirectory());
					} else {
						throw new RuntimeException("Provided non-string path");
					}
				} else {
					throw new RuntimeException("Must provide at least 1 argument");
				}
				
				return val;
			}
		});
		funcs.put("delete_file", new RtflFunction() {
			public RtflType run(RtflType[] args, RtflRuntime runtime, Scope scope) throws RuntimeException {
				if(args.length > 0) {
					if(args[0] instanceof StringType) {
						File file = new File((String) args[0].value());
						
						if(file.exists()) {
							if(file.isDirectory() && file.list().length > 0)
								throw new RuntimeException("Cannot delete directories with files in them");
							else
								file.delete();
						} else {
							throw new RuntimeException("File \""+((String) args[0].value())+"\" does not exist");
						}
					} else {
						throw new RuntimeException("Provided non-string path");
					}
				} else {
					throw new RuntimeException("Must provide at least 1 argument");
				}
				
				return new NullType();
			}
		});
		funcs.put("list_files", new RtflFunction() {
			public RtflType run(RtflType[] args, RtflRuntime runtime, Scope scope) throws RuntimeException {
				ArrayType files = new ArrayType();
				
				if(args.length > 0) {
					if(args[0] instanceof StringType) {
						File file = new File((String) args[0].value());
						@SuppressWarnings("unchecked")
						ArrayList<RtflType> fileNames = ((ArrayList<RtflType>) files.value());
						
						if(file.exists()) {
							if(file.isDirectory())
								for(String path : file.list())
									fileNames.add(new StringType(path));
							else
								throw new RuntimeException("Path \""+((String) args[0].value())+"\" does not point to a directory");
						} else {
							throw new RuntimeException("Path \""+((String) args[0].value())+"\" does not exist");
						}
					} else {
						throw new RuntimeException("Provided non-string path");
					}
				} else {
					throw new RuntimeException("Must provide at least 1 argument");
				}
				
				return files;
			}
		});
		funcs.put("create_directory", new RtflFunction() {
			public RtflType run(RtflType[] args, RtflRuntime runtime, Scope scope) throws RuntimeException {
				if(args.length > 0) {
					if(args[0] instanceof StringType) {
						File file = new File((String) args[0].value());
						
						file.mkdirs();
					} else {
						throw new RuntimeException("Provided non-string path");
					}
				} else {
					throw new RuntimeException("Must provide at least 1 argument");
				}
				
				return new NullType();
			}
		});
		funcs.put("move_file", new RtflFunction() {
			public RtflType run(RtflType[] args, RtflRuntime runtime, Scope scope) throws RuntimeException {
				if(args.length > 1) {
					if(args[0] instanceof StringType && args[1] instanceof StringType) {
						File file = new File((String) args[0].value());
						file.renameTo(new File((String) args[1].value()));
					} else {
						throw new RuntimeException("Provided non-string path");
					}
				} else {
					throw new RuntimeException("Must provide at least 2 arguments");
				}
				
				return new NullType();
			}
		});
		funcs.put("open_terminal", new RtflFunction() {
			public RtflType run(RtflType[] args, RtflRuntime runtime, Scope scope) throws RuntimeException {
				runtime.openTerminal();
				
				return new NullType();
			}
		});
		funcs.put("close_terminal", new RtflFunction() {
			public RtflType run(RtflType[] args, RtflRuntime runtime, Scope scope) throws RuntimeException {
				runtime.closeTerminal();
				
				return new NullType();
			}
		});
		funcs.put("terminal_open", new RtflFunction() {
			public RtflType run(RtflType[] args, RtflRuntime runtime, Scope scope) throws RuntimeException {
				return new BoolType(runtime.terminalOpen());
			}
		});
		funcs.put("read_terminal", new RtflFunction() {
			public RtflType run(RtflType[] args, RtflRuntime runtime, Scope scope) throws RuntimeException {
				return new StringType(runtime.readTerminal());
			}
		});
		funcs.put("exit", new RtflFunction() {
			public RtflType run(RtflType[] args, RtflRuntime runtime, Scope scope) throws RuntimeException {
				if(args.length > 0 && args[0] instanceof NumberType)
					System.exit(((NumberType) args[0]).toInt());
				else
					System.exit(0);
				
				return new NullType();
			}
		});
		funcs.put("system_property", new RtflFunction() {
			public RtflType run(RtflType[] args, RtflRuntime runtime, Scope scope) throws RuntimeException {
				RtflType prop = null;
				
				if(args.length > 0) {
					if(args[0] instanceof StringType) {
						String sysProp = System.getProperty((String) args[0].value());
						prop = sysProp == null ? new NullType() : new StringType(sysProp);
					} else {
						throw new RuntimeException("Provided non-string property name");
					}
				} else {
					throw new RuntimeException("Must provide at least 1 argument");
				}
				
				return prop;
			}
		});
		funcs.put("array", new RtflFunction() {
			public RtflType run(RtflType[] args, RtflRuntime runtime, Scope scope) throws RuntimeException {
				return new ArrayType(args);
			}
		});
		funcs.put("array_add", new RtflFunction() {
			public RtflType run(RtflType[] args, RtflRuntime runtime, Scope scope) throws RuntimeException {
				if(args.length > 1) {
					if(args[0] instanceof ArrayType) {
						@SuppressWarnings("unchecked")
						ArrayList<RtflType> arr = (ArrayList<RtflType>) ((ArrayType) args[0]).value();
						for(int i = 1; i < args.length; i++)
							arr.add(args[i]);
					} else {
						throw new RuntimeException("Did not provide array to add elements to");
					}
				} else {
					throw new RuntimeException("Must provide at least 2 arguments");
				}
				
				return new NullType();
			}
		});
		funcs.put("array_contains", new RtflFunction() {
			public RtflType run(RtflType[] args, RtflRuntime runtime, Scope scope) throws RuntimeException {
				boolean contains = false;
				
				if(args.length > 1) {
					if(args[0] instanceof ArrayType) {
						@SuppressWarnings("unchecked")
						ArrayList<RtflType> arr = (ArrayList<RtflType>) ((ArrayType) args[0]).value();
						for(RtflType elem : arr) {
							if(elem.equals(args[1], scope)) {
								contains = true;
								break;
							}
						}
					} else {
						throw new RuntimeException("Did not provide array to search");
					}
				} else {
					throw new RuntimeException("Must provide at least 2 arguments");
				}
				
				return new BoolType(contains);
			}
		});
		funcs.put("array_remove", new RtflFunction() {
			public RtflType run(RtflType[] args, RtflRuntime runtime, Scope scope) throws RuntimeException {
				if(args.length > 1) {
					if(args[0] instanceof ArrayType) {
						@SuppressWarnings("unchecked")
						ArrayList<RtflType> arr = (ArrayList<RtflType>) ((ArrayType) args[0]).value();
						
						RtflType rem = args[1];
						if(rem instanceof NumberType) {
							arr.remove(((NumberType) rem).toInt());
						} else {
							for(int i = 0; i < arr.size(); i++)
								if(rem.value().equals(arr.get(i).value()))
									arr.remove(i);
						}
					} else {
						throw new RuntimeException("Did not provide array to remove elements from");
					}
				} else {
					throw new RuntimeException("Must provide at least 2 arguments");
				}
				
				return new NullType();
			}
		});
		funcs.put("array_get", new RtflFunction() {
			public RtflType run(RtflType[] args, RtflRuntime runtime, Scope scope) throws RuntimeException {
				RtflType val = new NullType();
				
				if(args.length > 1) {
					if(args[0] instanceof ArrayType) {
						@SuppressWarnings("unchecked")
						ArrayList<RtflType> arr = (ArrayList<RtflType>) ((ArrayType) args[0]).value();
						
						RtflType index = args[1];
						try {
							if(index instanceof NumberType)
								val = arr.get(((NumberType) index).toInt());
							else
								throw new RuntimeException("Index must be a number");
						} catch(IndexOutOfBoundsException e) {
							throw new RuntimeException("Index "+index+" out of bounds (array length is "+arr.size()+')');
						}
					} else {
						throw new RuntimeException("Did not provide array to read");
					}
				} else {
					throw new RuntimeException("Must provide at least 2 arguments");
				}
				
				return val;
			}
		});
		funcs.put("array_set", new RtflFunction() {
			public RtflType run(RtflType[] args, RtflRuntime runtime, Scope scope) throws RuntimeException {
				if(args.length > 2) {
					if(args[0] instanceof ArrayType) {
						@SuppressWarnings("unchecked")
						ArrayList<RtflType> arr = (ArrayList<RtflType>) ((ArrayType) args[0]).value();
						
						RtflType index = args[1];
						try {
							if(index instanceof NumberType)
								arr.set(((NumberType) index).toInt(), args[2]);
							else
								throw new RuntimeException("Index must be a number");
						} catch(IndexOutOfBoundsException e) {
							throw new RuntimeException("Index "+index+" out of bounds (array length is "+arr.size()+')');
						}
					} else {
						throw new RuntimeException("Did not provide array to set");
					}
				} else {
					throw new RuntimeException("Must provide at least 3 arguments");
				}
				
				return new NullType();
			}
		});
		funcs.put("array_length", new RtflFunction() {
			public RtflType run(RtflType[] args, RtflRuntime runtime, Scope scope) throws RuntimeException {
				IntType len = null;
				
				if(args.length > 0) {
					if(args[0] instanceof ArrayType) {
						@SuppressWarnings("unchecked")
						ArrayList<RtflType> arr = (ArrayList<RtflType>) ((ArrayType) args[0]).value();
						
						len = new IntType(arr.size());
					} else {
						throw new RuntimeException("Did not provide array to mearsure");
					}
				} else {
					throw new RuntimeException("Must provide at least 1 argument");
				}
				
				return len;
			}
		});
		funcs.put("split", new RtflFunction() {
			@SuppressWarnings("unchecked")
			public RtflType run(RtflType[] args, RtflRuntime runtime, Scope scope) throws RuntimeException {
				ArrayType parts = new ArrayType();
				
				if(args.length > 1) {
					if(args[0] instanceof StringType && args[1] instanceof StringType) {
						String str = (String) args[0].value();
						String splitter = (String) args[1].value();
						String[] bits = str.split(splitter);
						
						for(String bit : bits)
							((ArrayList<RtflType>) parts.value()).add(new StringType(bit));
					} else {
						throw new RuntimeException("Provided non-string argument");
					}
				} else {
					throw new RuntimeException("Must provide at least 2 arguments");
				}
				
				return parts;
			}
		});
		funcs.put("index_of", new RtflFunction() {
			public RtflType run(RtflType[] args, RtflRuntime runtime, Scope scope) throws RuntimeException {
				IntType index = null;
				
				if(args.length > 1) {
					if(args[0] instanceof StringType && args[1] instanceof StringType) {
						String str = (String) args[0].value();
						String str2 = (String) args[1].value();
						
						index = new IntType(str.indexOf(str2));
					} else {
						throw new RuntimeException("Provided non-string argument");
					}
				} else {
					throw new RuntimeException("Must provide at least 2 arguments");
				}
				
				return index;
			}
		});
		funcs.put("starts_with", new RtflFunction() {
			public RtflType run(RtflType[] args, RtflRuntime runtime, Scope scope) throws RuntimeException {
				BoolType result = null;
				
				if(args.length > 1) {
					if(args[0] instanceof StringType && args[1] instanceof StringType) {
						String str = (String) args[0].value();
						String start = (String) args[1].value();
						
						result = new BoolType(str.startsWith(start));
					} else {
						throw new RuntimeException("Provided non-string argument");
					}
				} else {
					throw new RuntimeException("Must provide at least 2 arguments");
				}
				
				return result;
			}
		});
		funcs.put("ends_with", new RtflFunction() {
			public RtflType run(RtflType[] args, RtflRuntime runtime, Scope scope) throws RuntimeException {
				BoolType result = null;
				
				if(args.length > 1) {
					if(args[0] instanceof StringType && args[1] instanceof StringType) {
						String str = (String) args[0].value();
						String end = (String) args[1].value();
						
						result = new BoolType(str.endsWith(end));
					} else {
						throw new RuntimeException("Provided non-string argument");
					}
				} else {
					throw new RuntimeException("Must provide at least 2 arguments");
				}
				
				return result;
			}
		});
		funcs.put("string_replace", new RtflFunction() {
			public RtflType run(RtflType[] args, RtflRuntime runtime, Scope scope) throws RuntimeException {
				StringType result = null;
				
				if(args.length > 2) {
					if(args[0] instanceof StringType && args[1] instanceof StringType && args[2] instanceof StringType) {
						String str = (String) args[0].value();
						String replace = (String) args[1].value();
						String replaceVal = (String) args[2].value();
						
						result = new StringType(str.replace(replace, replaceVal));
					} else {
						throw new RuntimeException("Provided non-string argument");
					}
				} else {
					throw new RuntimeException("Must provide at least 3 arguments");
				}
				
				return result;
			}
		});
		funcs.put("substring", new RtflFunction() {
			public RtflType run(RtflType[] args, RtflRuntime runtime, Scope scope) throws RuntimeException {
				StringType sub = null;
				
				if(args.length > 1) {
					if(args[0] instanceof StringType) {
						String str = (String) args[0].value();
						int start = -1;
						int end = str.length();
						
						if(args[1] instanceof NumberType) {
							start = ((NumberType) args[1]).toInt();
							if(args.length > 2 && args[2] instanceof NumberType) {
								end = ((NumberType) args[2]).toInt();
							}
							
							try {
								sub = new StringType(str.substring(start, end));
							} catch(StringIndexOutOfBoundsException e) {
								throw new RuntimeException("String range is out of bounds");
							}
						} else {
							throw new RuntimeException("Starting index must be a number");
						}
					} else {
						throw new RuntimeException("Provided non-string argument");
					}
				} else {
					throw new RuntimeException("Must provide at least 1 argument");
				}
				
				return sub;
			}
		});
		funcs.put("char_at", new RtflFunction() {
			public RtflType run(RtflType[] args, RtflRuntime runtime, Scope scope) throws RuntimeException {
				StringType ch = null;
				
				if(args.length > 1) {
					if(args[0] instanceof StringType) {
						String str = (String) args[0].value();
						
						if(args[1] instanceof NumberType) {
							int index = ((NumberType) args[1]).toInt();
							
							try {
								// Poor man's way of converting a char to a String
								ch = new StringType(str.charAt(index)+"");
							} catch(StringIndexOutOfBoundsException e) {
								throw new RuntimeException("Character index is out of bounds");
							}
						} else {
							throw new RuntimeException("Character index must be a number");
						}
					} else {
						throw new RuntimeException("Provided non-string argument");
					}
				} else {
					throw new RuntimeException("Must provide at least 1 argument");
				}
				
				return ch;
			}
		});
		funcs.put("string_length", new RtflFunction() {
			public RtflType run(RtflType[] args, RtflRuntime runtime, Scope scope) throws RuntimeException {
				IntType len = null;
				
				if(args.length > 0) {
					if(args[0] instanceof StringType) {
						String str = (String) args[0].value();
						
						len = new IntType(str.length());
					} else {
						throw new RuntimeException("Did not provide string to measure");
					}
				} else {
					throw new RuntimeException("Must provide at least 1 argument");
				}
				
				return len;
			}
		});
		funcs.put("type", new RtflFunction() {
			public RtflType run(RtflType[] args, RtflRuntime runtime, Scope scope) throws RuntimeException {
				String type = null;
				
				if(args.length > 0) {
					if(args[0] instanceof ArrayType)
						type = "array";
					else if(args[0] instanceof BoolType)
						type = "boolean";
					else if(args[0] instanceof NumberType)
						type = "number";
					else if(args[0] instanceof NullType)
						type = "null";
					else if(args[0] instanceof StringType)
						type = "string";
					else
						type = args[0].name().toLowerCase();
				} else {
					throw new RuntimeException("Must provide at least 1 argument");
				}
				
				return new StringType(type);
			}
		});
		funcs.put("to_number", new RtflFunction() {
			public RtflType run(RtflType[] args, RtflRuntime runtime, Scope scope) throws RuntimeException {
				NumberType num = null;
				
				if(args.length > 0) {
					if(args[0] instanceof NumberType)
						num = (NumberType) args[0];
					else if(args[0] instanceof BoolType)
						num = new IntType(((NumberType) args[0]).toInt());
					else if(args[0] instanceof StringType) {
						String str = (String) args[0].value();
						
						try {
							if(str.contains(".")) {
								num = new DoubleType(Double.parseDouble(str));
							} else {
								num = new IntType(Integer.parseInt(str));
							}
						} catch(NumberFormatException e) {
							throw new RuntimeException("String \""+str+"\" does not represent a number");
						}
					} else
						throw new RuntimeException("Cannot convert \""+args[0]+"\" to number");
				} else {
					throw new RuntimeException("Must provide at least 1 argument");
				}
				
				return num;
			}
		});
		funcs.put("read_http", new RtflFunction() {
			public RtflType run(RtflType[] args, RtflRuntime runtime, Scope scope) throws RuntimeException {
				StringType res = null;
				
				if(args.length > 0) {
					if(args[0] instanceof StringType) {
						String url = (String) args[0].value();
						String method = "GET";
						
						if(args.length > 1 && args[1] instanceof StringType) {
							method = (String) args[1].value();
						}
						
						try {
							URL obj = new URL(url);
							HttpURLConnection con = (HttpURLConnection) obj.openConnection();
							con.setRequestMethod(method);
							StringBuilder response = new StringBuilder();
							BufferedInputStream buf = new BufferedInputStream(con.getInputStream());
							
							int read = 0;
							while((read = buf.read()) > -1)
								response.append((char) read);
							
							buf.close();
							
							res = new StringType(response.toString());
						} catch(IOException e) {
							throw new RuntimeException("Failed to load URL: "+e.getMessage());
						}
					}
				} else {
					throw new RuntimeException("Must provide at least 1 argument");
				}
				
				return res;
			}
		});
		funcs.put("exec", new RtflFunction() {
			public RtflType run(RtflType[] args, RtflRuntime runtime, Scope scope) throws RuntimeException {
				String out = null;
				
				if(args.length > 0) {
					ArrayList<String> procArgs = new ArrayList<String>();
					// Resolve arguments
					for(RtflType arg : args)
						procArgs.add(arg.value().toString());
					
					ProcessBuilder pb = new ProcessBuilder(procArgs);
					pb.redirectErrorStream(true);
					
					InputStream in = null;
					ByteArrayOutputStream baos = null;
					try {
						Process proc = pb.start();
						in = proc.getInputStream();
						baos = new ByteArrayOutputStream();
						
						byte[] b = new byte[1024];
						int size = 0;
						while((size = in.read(b)) != -1)
							baos.write(b, 0, size);
						
						out = new String(baos.toByteArray());
					} catch(IOException e) {
						throw new RuntimeException("Failed to execute process: "+e.getMessage());
					} finally {
						try {
							if(in != null) in.close();
							if(baos != null) baos.close();
						} catch(IOException e) {}
					}
				} else {
					throw new RuntimeException("Must provide at least 1 argument");
				}
				
				return new StringType(out);
			}
		});
		funcs.put("map", new RtflFunction() {
			public RtflType run(RtflType[] args, RtflRuntime runtime, Scope scope) throws RuntimeException {
				return new MapType();
			}
		});
		funcs.put("map_keys", new RtflFunction() {
			public RtflType run(RtflType[] args, RtflRuntime runtime, Scope scope) throws RuntimeException {
				ArrayType val = null;
				
				if(args.length > 0) {
					if(args[0] instanceof MapType) {
						@SuppressWarnings("unchecked")
						ConcurrentHashMap<String, RtflType> map = (ConcurrentHashMap<String, RtflType>) args[0].value();
						
						ArrayList<RtflType> arr = new ArrayList<RtflType>();
						for(String key : map.keySet())
							arr.add(new StringType(key));
						
						val = new ArrayType(arr);
					} else {
						throw new RuntimeException("Provided non-map argument");
					}
				} else {
					throw new RuntimeException("Must provide at least 1 argument");
				}
				
				return val;
			}
		});
		funcs.put("map_values", new RtflFunction() {
			public RtflType run(RtflType[] args, RtflRuntime runtime, Scope scope) throws RuntimeException {
				ArrayType val = null;
				
				if(args.length > 0) {
					if(args[0] instanceof MapType) {
						@SuppressWarnings("unchecked")
						ConcurrentHashMap<String, RtflType> map = (ConcurrentHashMap<String, RtflType>) args[0].value();
						
						ArrayList<RtflType> arr = new ArrayList<RtflType>();
						for(RtflType value : map.values())
							arr.add(value);
						
						val = new ArrayType(arr);
					} else {
						throw new RuntimeException("Provided non-map argument");
					}
				} else {
					throw new RuntimeException("Must provide at least 1 argument");
				}
				
				return val;
			}
		});
		funcs.put("map_contains_key", new RtflFunction() {
			public RtflType run(RtflType[] args, RtflRuntime runtime, Scope scope) throws RuntimeException {
				boolean contains = false;
				
				if(args.length > 1) {
					if(args[0] instanceof MapType) {
						@SuppressWarnings("unchecked")
						ConcurrentHashMap<String, RtflType> map = (ConcurrentHashMap<String, RtflType>) args[0].value();
						
						contains = map.containsKey((String) args[1].value());
					} else {
						throw new RuntimeException("Provided non-map argument");
					}
				} else {
					throw new RuntimeException("Must provide at least 2 arguments");
				}
				
				return new BoolType(contains);
			}
		});
		funcs.put("map_put", new RtflFunction() {
			public RtflType run(RtflType[] args, RtflRuntime runtime, Scope scope) throws RuntimeException {
				if(args.length > 2) {
					if(args[0] instanceof MapType) {
						if(args[1] instanceof StringType) {
							@SuppressWarnings("unchecked")
							ConcurrentHashMap<String, RtflType> map = (ConcurrentHashMap<String, RtflType>) args[0].value();
							
							map.put((String) args[1].value(), args[2]);
						} else {
							throw new RuntimeException("Key must be a string");
						}
					} else {
						throw new RuntimeException("Provided non-map argument");
					}
				} else {
					throw new RuntimeException("Must provide at least 3 arguments");
				}
				
				return new NullType();
			}
		});
		// Alias to map_put
		funcs.put("map_set", funcs.get("map_put"));
		funcs.put("map_get", new RtflFunction() {
			public RtflType run(RtflType[] args, RtflRuntime runtime, Scope scope) throws RuntimeException {
				RtflType val = new NullType();
				if(args.length > 1) {
					if(args[0] instanceof MapType) {
						if(args[1] instanceof StringType) {
							@SuppressWarnings("unchecked")
							ConcurrentHashMap<String, RtflType> map = (ConcurrentHashMap<String, RtflType>) args[0].value();
							
							RtflType value = map.get((String) args[1].value());
							if(value != null)
								val = value;
						} else {
							throw new RuntimeException("Key must be a string");
						}
					} else {
						throw new RuntimeException("Provided non-map argument");
					}
				} else {
					throw new RuntimeException("Must provide at least 2 arguments");
				}
				
				return val;
			}
		});
		funcs.put("map_remove", new RtflFunction() {
			public RtflType run(RtflType[] args, RtflRuntime runtime, Scope scope) throws RuntimeException {
				if(args.length > 1) {
					if(args[0] instanceof MapType) {
						if(args[1] instanceof StringType) {
							@SuppressWarnings("unchecked")
							ConcurrentHashMap<String, RtflType> map = (ConcurrentHashMap<String, RtflType>) args[0].value();
							
							map.remove((String) args[1].value());
						} else {
							throw new RuntimeException("Key must be a string");
						}
					} else {
						throw new RuntimeException("Provided non-map argument");
					}
				} else {
					throw new RuntimeException("Must provide at least 2 arguments");
				}
				
				return new NullType();
			}
		});
		funcs.put("to_json", new JsonParseFunction(false));
		funcs.put("from_json", new JsonParseFunction(true));
		funcs.put("restrict", new RtflFunction() {
			public RtflType run(RtflType[] args, RtflRuntime runtime, Scope scope) throws RuntimeException {
				if(args.length > 0) {
					if(args[0] instanceof StringType) {
						String funcName = (String) args[0].value();
						
						// Restrict function in parent scope, if available
						if(scope.parent() != null)
							scope.parent().restrictFunc(funcName);
					} else {
						throw new RuntimeException("Provided non-string argument");
					}
				} else {
					throw new RuntimeException("Must provide at least 1 argument");
				}
				
				return new NullType();
			}
		});
		funcs.put("library", new RtflFunction() {
			public RtflType run(RtflType[] args, RtflRuntime runtime, Scope scope) throws RuntimeException {
				if(args.length > 0) {
					if(args[0] instanceof StringType) {
						File jar = new File((String) args[0].value());
						
						try {
							LibraryLoader.loadLibrary(jar).initialize(runtime);
						} catch(Exception e) {
							throw new RuntimeException("Failed to load JAR library: "+e.getMessage());
						}
					} else {
						throw new RuntimeException("Provided non-string argument");
					}
				} else {
					throw new RuntimeException("Must provide at least 1 argument");
				}
				
				return new NullType();
			}
		});
		funcs.put("throw", new RtflFunction() {
			public RtflType run(RtflType[] args, RtflRuntime runtime, Scope scope) throws RuntimeException {
				if(args.length > 0) {
					if(args[0] instanceof StringType) {
						throw new RuntimeException((String) args[0].value());
					} else {
						throw new RuntimeException("Provided non-string argument");
					}
				} else {
					throw new RuntimeException("Must provide at least 1 argument");
				}
			}
		});
		funcs.put("copy_func", new RtflFunction() {
			public RtflType run(RtflType[] args, RtflRuntime runtime, Scope scope) throws RuntimeException {
				if(args.length > 1) {
					if(args[0] instanceof StringType && args[1] instanceof StringType) {
						if(scope.function(((String) args[0].value())) != null)
							runtime.functions().put(
								((String) args[1].value()),
								scope.function(((String) args[0].value()))
							);
					}
				} else {
					throw new RuntimeException("Musst provide at least two arguments");
				}
				
				return new NullType();
			}
		});
	}
	
	/**
	 * Returns all standard Rtfl functions
	 * @return all standard Rtfl functions
	 * @since 1.0
	 */
	public HashMap<String, RtflFunction> functions() {
		return funcs;
	}
	
	// Standard `eval` function implementation
	private class EvalFunction implements RtflFunction {
		private boolean _async = false;
		private boolean _fromFile = false;
		
		public EvalFunction(boolean async, boolean fromFile) {
			_async = async;
			_fromFile = fromFile;
		}
		
		public RtflType run(RtflType[] args, RtflRuntime runtime, Scope scope) throws RuntimeException {
			RtflType result = new NullType();
			
			if(args.length > 0) {
				if(args[0] instanceof StringType) {
					InputStream in = null;
					// Read from file if specified
					if(_fromFile) {
						String path = (String) ((StringType) args[0]).value();
						try {
							runtime.executeFile(new File(path), scope);
						} catch(FileNotFoundException e) {
							throw new RuntimeException("File \""+path+"\" does not exist");
						} catch (IOException | ProducerException e) {
							throw new RuntimeException("Failed to execute file: "+e.getMessage());
						}
					} else {
						// Convert code to byte stream
						String code = (String) ((StringType) args[0]).value();
						in = new ByteArrayInputStream(code.getBytes(StandardCharsets.UTF_8));
						
						// Cache and execute instructions
						CacheInstructionConsumer cons = new CacheInstructionConsumer();
						try {
							// Generate instructions from source
							SourcecodeInstructionProducer.produce(_fromFile ? new File((String) args[0].value()).getName() : "eval", in, cons);
							
							// Fetch and execute instructions
							if(_async)
								runtime.executeAsync(cons.cache.toArray(new RtflInstruction[0]), scope);
							else
								result = runtime.execute(cons.cache.toArray(new RtflInstruction[0]), scope);
						} catch (IOException | ProducerException | RuntimeException e) {
							// Throw message as RuntimeException if not already
							if(e instanceof RuntimeException)
								throw (RuntimeException) e;
							else
								throw new RuntimeException(e.getMessage());
						}
					}
				} else {
					throw new RuntimeException("Provided non-string argument");
				}
			} else {
				throw new RuntimeException("Must provide at least 1 argument");
			}
			
			return result;
		}
	}
	// All JSON parsing related function implementations
	private class JsonParseFunction implements RtflFunction {
		private boolean _toMap = false;
		
		public JsonParseFunction(boolean toMap) {
			_toMap = toMap;
		}
		
		@SuppressWarnings("unchecked")
		private JSONArray arrayToJson(ArrayType arrayType) {
			JSONArray arr = new JSONArray();
			
			for(RtflType val : (ArrayList<RtflType>) arrayType.value())
				if(val instanceof MapType)
					arr.put(mapToJson((MapType) val));
				else if(val instanceof ArrayType)
					arr.put(arrayToJson((ArrayType) val));
				else
					arr.put(val.value());
			
			return arr;
		}
		private JSONObject mapToJson(MapType mapType) {
			JSONObject json = new JSONObject();
			@SuppressWarnings("unchecked")
			ConcurrentHashMap<String, RtflType> map = (ConcurrentHashMap<String, RtflType>) mapType.value();
			
			// Loop through entries
			for(Entry<String, RtflType> entry : map.entrySet()) {
				// Process container types (array, map, etc)
				if(entry.getValue() instanceof MapType)
					json.put(entry.getKey(), mapToJson((MapType) entry.getValue()));
				else if(entry.getValue() instanceof ArrayType)
					json.put(entry.getKey(), arrayToJson((ArrayType) entry.getValue()));
				else
					json.put(entry.getKey(), entry.getValue().value());
			}
			
			return json;
		}
		
		@SuppressWarnings("unchecked")
		public RtflType jsonToValue(Object json) {
			RtflType val = null;
			
			if(json instanceof ArrayList<?>)
				val = new ArrayType(jsonToArray((ArrayList<Object>) json));
			else if(json instanceof Boolean)
				val = new BoolType((Boolean) json);
			else if(json instanceof Double)
				val = new DoubleType((Double) json);
			else if(json instanceof Integer)
				val = new IntType((Integer) json);
			else if(json instanceof Map<?, ?>)
				val = new MapType(jsonToMap((Map<String, Object>) json));
			else if(json instanceof String)
				val = new StringType((String) json);
			else
				val = new NullType();
			
			return val;
		}
		public ArrayList<RtflType> jsonToArray(ArrayList<Object> json) {
			ArrayList<RtflType> array = new ArrayList<RtflType>();
			
			// Loop through values
			for(Object obj : json)
				array.add(jsonToValue(obj));
			
			return array;
		}
		public HashMap<String, RtflType> jsonToMap(Map<String, Object> json) {
			HashMap<String, RtflType> map = new HashMap<String, RtflType>();
			
			// Loop through entries
			for(Entry<String, Object> entry : json.entrySet())
				map.put(entry.getKey(), jsonToValue(entry.getValue()));
			
			return map;
		}
		
		public RtflType run(RtflType[] args, RtflRuntime runtime, Scope scope) throws RuntimeException {
			RtflType val = null;
			
			if(args.length > 0) { 
				if(_toMap) {
					if(args[0] instanceof StringType) {
						try {
							Map<String, Object> map = new JSONObject((String) args[0].value()).toMap();
							
							val = new MapType(jsonToMap(map));
						} catch(Exception e) {
							throw new RuntimeException("Failed to parse JSON: "+e.getMessage());
						}
					} else {
						throw new RuntimeException("Provided non-string argument");
					}
				} else {
					if(args[0] instanceof MapType) {
						JSONObject json = mapToJson((MapType) args[0]);
						boolean pretty = false;
						if(args.length > 1 && args[1] instanceof BoolType)
							pretty = (Boolean) args[1].value();
						
						val = new StringType(json.toString(pretty ? 1 : 0));
					} else {
						throw new RuntimeException("Provided non-map argument");
					}
				}
			} else {
				throw new RuntimeException("Must provide at least 1 argument");
			}
			
			return val;
		}
	}
}
