/**
 * 
 */
package com.heliosapm.wiex.server.tracing.collectors.origin;

/**
 * <p>Title: OriginAdapter</p>
 * <p>Description: Interface defining the behaviour of an OriginAdapter implementation.
 * An OriginAdapter accepts an originating IP address and returns a domain specific decode.</p> 
 * <p>Copyright: Copyright (c) 2007</p>
 * <p>Company: Helios Development Group</p>
 * @author Whitehead
 * @version $Revision: 1.1 $
 */


public interface OriginAdapter {
	/**
	 * Decodes the passed IP address using a domain specific decofing algorithm or lookup decode process.
	 * @param ipAddress The originating IP address of a metric.
	 * @return A domain specific decode of the passed IP address.
	 */
	public String decodeOrigin(String ipAddress);
	
	/**
	 * Rewrites an existing category to generalize it for the decoded origin.
	 * @param category The tracing category 
	 * @param delimeter The tracer's delimeter.
	 * @param location The name of the location that is to be use for rewriting.
	 * @return A rewriten category.
	 */
	public String rewriteCategory(String category, String delimeter, String location);
}
