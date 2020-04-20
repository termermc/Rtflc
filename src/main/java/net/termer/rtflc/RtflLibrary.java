package net.termer.rtflc;

import net.termer.rtflc.runtime.RtflRuntime;

/**
 * Basic library interface to be extended by external jar libraries
 * @author termer
 * @since 1.0
 */
public interface RtflLibrary {
	/**
	 * Method to be run when the library is loaded
	 * @param runtime The instance of the Rtfl runtime that loaded this library
	 * @since 1.0
	 */
	public void initialize(RtflRuntime runtime);
}
