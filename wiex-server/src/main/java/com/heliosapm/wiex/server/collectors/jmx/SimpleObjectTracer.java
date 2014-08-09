/**
 * 
 */
package com.heliosapm.wiex.server.collectors.jmx;

/**
 * <p>Title: SimpleObjectTracer</p>
 * <p>Description: Defines behaviour for object translators that take complex objects and extract a simple numerical tracing stat.</p> 
 * <p>Copyright: Copyright (c) 2007</p>
 * <p>Company: Helios Development Group</p>
 * @author Whitehead
 * @version $Revision: 1.1 $
 */

public interface SimpleObjectTracer {

	/**
	 * Extracts a target tracing numerical value from the passed object and returns it.
	 * @param obj The object to extract a tracing calue from.
	 * @return The extracting tracing number in string format.
	 */
	public String renderTracingValue(Object obj);
}
