/**
 * 
 */
package com.heliosapm.wiex.server.tracing;

/**
 * <p>Title: SBSTracingVersion</p>
 * <p>Description: Prints SBSTracing Release Version.</p> 
 * <p>Copyright: Copyright (c) 2007</p>
 * <p>Company: Helios Development Group</p>
 * @author Whitehead
 * @version $Revision: 1.2 $
 * Main-Class: 
 */

public class SBSTracingVersion {
	public static final String VERSION = "@SBS.TRACING.VERSION@";

	/**
	 * Prints SBSTracing Release Version.
	 * @param args No args.
	 */
	public static void main(String[] args) {
		System.out.println(VERSION);
	}

}
