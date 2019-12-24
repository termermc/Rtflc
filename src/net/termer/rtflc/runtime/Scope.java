package net.termer.rtflc.runtime;

import java.util.ArrayList;
import java.util.HashMap;

import net.termer.rtflc.runtime.RtflRuntime;
import net.termer.rtflc.runtime.RtflRuntime.LocalVar;
import net.termer.rtflc.instructions.RtflInstruction;
import net.termer.rtflc.type.RtflType;

/**
 * Class to store information about the current scope of executing instructions
 * @author termer
 * @since 1.0
 */
public class Scope {
	// Runtime
	private RtflRuntime rt = null;
	// Local variable aliases
	private HashMap<String, Integer> locals = null;
	// Parent scope, if any
	private Scope parent = null;
	// Scope cause, if any
	private RtflInstruction cause = null;
	// List of functions that may not be executed in this scope
	private ArrayList<String> restrictedFuncs = new ArrayList<String>();
	
	/**
	 * Instantiates a new Scope object
	 * @param runtime the Runtime associated with this scope
	 * @param localAliases all aliases to local variables visible to this scope
	 * @param causeInstruction the instruction that caused this new scope
	 * @since 1.0
	 */
	public Scope(RtflRuntime runtime, HashMap<String, Integer> localAliases, RtflInstruction causeInstruction) {
		rt = runtime;
		locals = localAliases == null ? new HashMap<String, Integer>() : localAliases;
		cause = causeInstruction;
	}
	/**
	 * Instantiates a new Scope object
	 * @param runtime the Runtime associated with this scope
	 * @param localAliases all aliases to local variables visible to this scope
	 * @param causeInstruction the instruction that caused this new scope
	 * @param parentScope the scope above this scope
	 * @since 1.0
	 */
	public Scope(RtflRuntime runtime, HashMap<String, Integer> localAliases, RtflInstruction causeInstruction, Scope parentScope) {
		rt = runtime;
		locals = localAliases == null ? new HashMap<String, Integer>() : localAliases;
		cause = causeInstruction;
		parent = parentScope;
	}
	/**
	 * Instantiates a new Scope object
	 * @param runtime the Runtime associated with this scope
	 * @param localAliases all aliases to local variables visible to this scope
	 * @param restrictedFunctions a list of functions that cannot be called in this scope or any of its children
	 * @param causeInstruction the instruction that caused this new scope
	 * @param parentScope the scope above this scope
	 * @since 1.0
	 */
	public Scope(RtflRuntime runtime, HashMap<String, Integer> localAliases, ArrayList<String> restrictedFunctions, RtflInstruction causeInstruction, Scope parentScope) {
		rt = runtime;
		locals = localAliases == null ? new HashMap<String, Integer>() : localAliases;
		restrictedFuncs = restrictedFunctions;
		cause = causeInstruction;
		parent = parentScope;
	}
	
	/**
	 * Returns the parent of this Scope, null if it has none
	 * @return The parent of this scope if any
	 * @since 1.0
	 */
	public Scope parent() {
		return parent;
	}
	/**
	 * Returns this scope's Runtime
	 * @return this scope's Runtime
	 * @since 1.0
	 */
	public RtflRuntime runtime() {
		return rt;
	}
	/**
	 * Returns the instruction that caused the creation of this scope
	 * @return the instruction that caused this scope
	 * @since 1.0
	 */
	public RtflInstruction cause() {
		return cause;
	}
	/**
	 * Returns a map of local variable name aliases to their internal IDs
	 * @return a map of local variable aliases
	 * @since 1.0
	 */
	public HashMap<String, Integer> variableAliases() {
		return locals;
	}
	
	/**
	 * Assigns the provided value to a variable with the specified name.
	 * If no local variable with the specified name is found, then globals will be checked.
	 * If no variable with the give name exists, then a RuntimeException will be thrown.
	 * @param varName the name of the variable
	 * @param value the value to assign
	 * @return Whether this variable assignment was to a local variable
	 * @throws RuntimeException if the specified variable does not exist
	 * @since 1.0
	 */
	public boolean assignVar(String varName, RtflType value) throws RuntimeException {
		boolean local = false;
		if(locals.containsKey(varName)) {
			int id = locals.get(varName);
			// Check if variable actually exists
			if(rt.localVariables().containsKey(id)) {
				local = true;
				rt.localVariables().replace(id, new LocalVar(value, Thread.currentThread().getName()));
			} else {
				locals.remove(varName);
				throw new RuntimeException("Attempted to assign value to undefined variable \""+varName+"\"");
			}
		} else if(rt.globalVarables().containsKey(varName)) {
			rt.globalVarables().replace(varName, value);
		} else {
			throw new RuntimeException("Attempted to assign value to undefined variable \""+varName+"\"");
		}
		
		return local;
	}
	/**
	 * Creates a new local variable with the specified name and value.
	 * Assigns the provided value to an existing local variable with the same name if it exists.
	 * @param varName the variable name
	 * @param value the value to assign
	 * @return The ID of the newly created local variable
	 * @since 1.0
	 */
	public int createLocalVar(String varName, RtflType value) {
		int id = rt.newId();
		
		if(locals.containsKey(varName))
			locals.remove(varName);
		
		rt.localVariables().put(id, new LocalVar(value, Thread.currentThread().getName()));
		locals.put(varName, id);
		
		return id;
	}
	/**
	 * Undefines the variable with the specified name.
	 * Local variables have priority; global variables are only undefined if no local variable with the provided name exists.
	 * Throws a RuntimeException if no variable with the specified name exists
	 * @param varName the name of the variable to undefine
	 * @throws RuntimeException if no variable with the specified name exists
	 * @return The ID of the local variable that was undefined. -1 if it was global.
	 * @since 1.0
	 */
	public int undefineVar(String varName) throws RuntimeException {
		int localId = -1;
		if(locals.containsKey(varName)) {
			int id = locals.get(varName);
			localId = id;
			locals.remove(varName);
			rt.localVariables().remove(id);
		} else if(rt.globalVarables().containsKey(varName)) {
			rt.globalVarables().remove(varName);
		} else {
			throw new RuntimeException("Attempted undefine undefined variable \""+varName+"\"");
		}
		return localId;
	}
	/**
	 * Undefines the specified function
	 * @param funcName The Name of the function to undefine
	 * @since 1.0
	 */
	public void undefineFunc(String funcName) {
		if(!restrictedFuncs.contains(funcName) && rt.functions().containsKey(funcName)) {
			rt.functions().remove(funcName);
		}
	}
	/**
	 * Restricts a function from being executed in this Scope
	 * @param funcName The name of the function to restrict
	 * @since 1.0
	 */
	public void restrictFunc(String funcName) {
		restrictedFuncs.add(funcName);
	}
	
	/**
	 * Returns the value of the provided variable name.
	 * Local variables have priority; global variables are only accessed if no local variable with the provided name exists.
	 * Throws a RuntimeException if no variable with the specified name exists.
	 * @param varName the variable name
	 * @return the variable's value
	 * @throws RuntimeException if no variable with the specified name exists
	 * @since 1.0
	 */
	public RtflType varValue(String varName) throws RuntimeException {
		RtflType val = null;
		
		if(locals.containsKey(varName)) {
			int id = locals.get(varName);
			
			// Check if variable actually exists
			LocalVar var = rt.localVariables().get(id);
			if(var == null) {
				locals.remove(varName);
				throw new RuntimeException("Attempted to retrieve value from undefined variable \""+varName+"\"");
			} else {
				val = var.value;
			}
		} else if(rt.globalVarables().containsKey(varName)) {
			val = rt.globalVarables().get(varName);
		} else {
			throw new RuntimeException("Attempted to retrieve value from undefined variable \""+varName+"\"");
		}
		
		return val;
	}
	/**
	 * Returns the function with the specified name.
	 * @param funcName the function name
	 * @return the function corresponding to the provided name
	 * @throws RuntimeException if the function does not exist or is restricted
	 * @since 1.0
	 */
	public RtflFunction function(String funcName) throws RuntimeException {
		RtflFunction func = null;
		
		if(restrictedFuncs.contains(funcName) || !rt.functions().containsKey(funcName)) {
			throw new RuntimeException("Attempted to call undefined or restricted function \""+funcName+"\"");
		} else {
			func = rt.functions().get(funcName);
		}
		
		return func;
	}
	
	/**
	 * Returns the full scope stack, starting from the top level scope down to the current scope
	 * @return the full scope stack
	 * @since 1.0
	 */
	public Scope[] scopeStack() {
		ArrayList<Scope> tmpArr = new ArrayList<Scope>();
		tmpArr.add(this);
		
		Scope scp = this;
		while(scp.parent() != null)
			tmpArr.add(scp = scp.parent());
		
		ArrayList<Scope> scopes = new ArrayList<Scope>();
		// Reverse the array
		for(int i = tmpArr.size()-1; i <= 0; i--)
			scopes.add(tmpArr.get(i));
		
		return scopes.toArray(new Scope[0]);
	}
	
	/**
	 * Descends a level and provides a new (non-reference) Scope for that level
	 * @param causeInstruction the instruction that caused this new scope
	 * @return the new Scope for a lower level
	 * @since 1.0
	 */
	public Scope descend(RtflInstruction causeInstruction) {
		return new Scope(rt, copyLocals(), restrictedFuncs, causeInstruction, this);
	}
	
	private HashMap<String, Integer> copyLocals() {
		HashMap<String, Integer> newMap = new HashMap<String, Integer>();
		
		newMap.putAll(locals);
		
		return newMap;
	}
}
