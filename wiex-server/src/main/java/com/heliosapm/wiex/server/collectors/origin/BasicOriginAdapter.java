/**
 * 
 */
package com.heliosapm.wiex.server.collectors.origin;

/**
 * <p>Title: BasicOriginAdapter</p>
 * <p>Description: Simple OriginAdapter implementation simply returns the IP address unmodified.</p> 
 * <p>Copyright: Copyright (c) 2007</p>
 * <p>Company: Helios Development Group</p>
 * @author Whitehead
 * @version $Revision: 1.1 $
 */

public class BasicOriginAdapter implements OriginAdapter {

	/**
	 * Returns the same value passed in.
	 * @param ipAddress The originating IP address of a metric submission.
	 * @return Non-Decoded ip address.
	 * @see com.heliosapm.wiex.server.collectors.origin.OriginAdapter#decodeOrigin(java.lang.String)
	 */
	public String decodeOrigin(String ipAddress) {
		return ipAddress;
	}

	/**
	 * Returns the category unmodified.
	 * @param category The tracing category 
	 * @param delimeter The tracer's delimeter.
	 * @param location The name of the location that is to be use for rewriting.
	 * @return The passed category.
	 */	
	public String rewriteCategory(String category, String delimeter, String location) {
			return category;
	}

}
