/**
 * 
 */
package com.heliosapm.wiex.tracing.tracing.util;

/**
 * <p>Title: Utils</p>
 * <p>Description: Miscellaneous Utilities for WIEXTracing</p> 
 * <p>Copyright: Copyright (c) 2007</p>
 * <p>Organization: Helios Development Group</p>
 * @author Whitehead
 * @version $Revision: 1.1 $
 */

public class Utils {

	
	/**
	 * Looks for a value for <code>name</code> in Environment Variables, then System Properties.
	 * Throws a RuntimeExcption if it is not found in either.
	 * @param name The name of the environmental variable or system property.
	 * @return The value of the environmental variable or system property.
	 */
	public static String getEnvThenSystemProperty(String name) {
		String s = System.getenv(name);
		if(s==null) s = System.getProperty(name);
		if(s!=null) return s;
		else throw new RuntimeException("Property [" + name + "] Not Found In Env or System Properties");
	}
	/**
	 * Looks for a value for <code>name</code> in System Properties, then Environmen Variables.
	 * Throws a RuntimeExcption if it is not found in either.
	 * @param name The name of the environmental variable or system property.
	 * @return The value of the environmental variable or system property.
	 */
	public static String getSystemPropertyThenEnv(String name) {
		String s = System.getProperty(name);
		if(s==null) s = System.getenv(name);
		if(s!=null) return s;
		else throw new RuntimeException("Property [" + name + "] Not Found In System Properties or Env");
	}
	
}
