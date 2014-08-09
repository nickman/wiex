/**
 * 
 */
package com.heliosapm.wiex.server.collectors.groovy;

/**
 * <p>Title: PreparedScript</p>
 * <p>Description: A container for a groovy script & compiler options</p> 
 * <p>Copyright: Copyright (c) 2007</p>
 * <p>Company: Helios Development Group</p>
 * @author Whitehead
 * @version $Revision: 1.1 $
 */

public class PreparedScript {
	String scriptText = null;
	String name = null;
	Boolean debug = null; // sets shell into debug mode.
	Integer recompInterval = null; // minimum recompilation interval
	Boolean recompile = null; // shell recompiles.
	String scriptBaseClass = null; // the base class to use for the generated scripts.
	String encoding = null;  // the encoding of the scripts to compile.
	Integer tolerance = null; // the number of compilation errors to tolerate before ending the compile.
	Boolean verbose = null; // verbose compilation
	Boolean warning = null;  // sets the compiler warning level.

}
