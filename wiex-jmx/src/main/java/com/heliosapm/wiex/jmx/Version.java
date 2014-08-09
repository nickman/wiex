
package com.heliosapm.wiex.jmx;

/**
 * <p>Title: Version</p>
 * <p>Description: Library Version Utility Class</p> 
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Organization: Helios Development Group</p>
 * @author Whitehead
 * @version $Revision: 1.1 $
 */

public class Version {
	
	/**
	 * No arg Main. Prints out the library version.
	 * @param args None
	 */
	public static void main(String args[]) {
		String version = Version.class.getPackage().getImplementationTitle() + "/" + Version.class.getPackage().getImplementationVersion();
		System.out.println(version);
	}
	
}
