package net.termer.rtflc.compiler;

/**
 * Utility class to store Rtfl compiler options
 * @author termer
 * @since 1.0
 */
public class CompilerOptions {
	private boolean compileLiteralLoads = false;
	private boolean compileLiteralRequires = false;
	private boolean packageLiteralLoads = false;
	private boolean packageLiteralRequires = false;
	private boolean preserveLineNumbers = true;
	
	/**
	 * Sets whether the compiler should compile all files referenced by loads with literal paths in them.
	 * Example: Compiler reading `load("another_file.rtfl")` in the source would cause it to compile `another_file.rtfl` to `another_file.rtfc` and reference it instead of the non-compiled script. 
	 * @param set The value to set this option
	 * @return this, to be used fluently
	 * @since 1.0
	 */
	public CompilerOptions compileLiteralLoads(boolean set) {
		compileLiteralLoads = set;
		return this;
	}
	/**
	 * Returns whether the compiler should compile all files referenced by loads with literal paths in them.
	 * Example: Compiler reading `load("another_file.rtfl")` in the source would cause it to compile `another_file.rtfl` to `another_file.rtfc` and reference it instead of the non-compiled script.
	 * @return The value of this option
	 * @since 1.0
	 */
	public boolean compileLiteralLoads() {
		return compileLiteralLoads;
	}
	
	/**
	 * Sets whether the compiler should compile all files referenced by requires with literal paths in them.
	 * Example: Compiler reading `require("library")` in the source would cause it to compile `libs/library.rtfl` to `libs/library.rtfc` and reference it instead of the non-compiled script. 
	 * @param set The value to set this option
	 * @return this, to be used fluently
	 * @since 1.0
	 */
	public CompilerOptions compileLiteralRequires(boolean set) {
		compileLiteralRequires = set;
		return this;
	}
	/**
	 * Returns whether the compiler should compile all files referenced by requires with literal paths in them.
	 * xample: Compiler reading `load("another_file.rtfl")` in the source would cause it to compile `another_file.rtfl` to `another_file.rtfc` and reference it instead of the non-compiled script.
	 * @return The value of this option
	 * @since 1.0
	 */
	public boolean compileLiteralRequires() {
		return compileLiteralRequires;
	}
	
	/**
	 * Sets whether the compiler should package all files referenced by loads with literal paths in them into the same file.
	 * Example: Compiler reading `load("another_file.rtfl")` in the source would cause it to compile `another_file.rtfl` and include the file in place of the load() call. 
	 * @param set The value to set this option
	 * @return this, to be used fluently
	 * @since 1.0
	 */
	public CompilerOptions packageLiteralLoads(boolean set) {
		packageLiteralLoads = set;
		return this;
	}
	/**
	 * Returns whether the compiler should package all files referenced by loads with literal paths in them into the same file.
	 * Example: Compiler reading `load("another_file.rtfl")` in the source would cause it to compile `another_file.rtfl` and include the file in place of the load() call.
	 * @return The value of this option
	 * @since 1.0
	 */
	public boolean packageLiteralLoads() {
		return packageLiteralLoads;
	}
	
	/**
	 * Sets whether the compiler should package all files referenced by requires with literal paths in them into the same file.
	 * Example: Compiler reading `require("library")` in the source would cause it to compile `libs/library.rtfl` and include the file in place of the require() call, if not already required()'d. 
	 * @param set The value to set this option
	 * @return this, to be used fluently
	 * @since 1.0
	 */
	public CompilerOptions packageLiteralRequires(boolean set) {
		packageLiteralRequires = set;
		return this;
	}
	/**
	 * Returns whether the compiler should package all files referenced by requires with literal paths in them into the same file.
	 * Example: Compiler reading `require("library")` in the source would cause it to compile `libs/library.rtfl` and include the file in place of the require() call, if not already required()'d.
	 * @return The value of this option
	 * @since 1.0
	 */
	public boolean packageLiteralRequires() {
		return packageLiteralRequires;
	}
	
	/**
	 * Sets whether the compiler should preserve original source line numbers 
	 * @param set The value to set this option
	 * @return this, to be used fluently
	 * @since 1.0
	 */
	public CompilerOptions preserveLineNumbers(boolean set) {
		preserveLineNumbers = set;
		return this;
	}
	/**
	 * Returns whether the compiler should preserve original source line numbers
	 * @return The value of this option
	 * @since 1.0
	 */
	public boolean preserveLineNumbers() {
		return preserveLineNumbers;
	}
}
