/**
 * 
 */
package com.heliosapm.wiex.server.collectors.jmx.tracers;

import com.heliosapm.wiex.server.collectors.jmx.SimpleObjectTracer;

/**
 * <p>Title: ServiceMBeanStateTracer</p>
 * <p>Description: SimpleObjectTracer that reads the state of an MBean and returns 1 if the state is 3, and 0 for all other values.</p> 
 * <p>Copyright: Copyright (c) 2007</p>
 *  * <p>Company: Helios Development Group</p>
 * @author Whitehead
 * @version $Revision: 1.1 $
 */

public class ServiceMBeanStateTracer implements SimpleObjectTracer {

	/**
	 * Reads a string value, converts to an int, then returns 1 if the value is 3 and 0 for all other values.
	 * @param obj An object presumed to be an Integer. 
	 * @return A 1 if the service is <code>Started</code> (3) or 0. 
	 * @see com.adp.sbs.metrics.tracing.collectors.jmx.SimpleObjectTracer#renderTracingValue(java.lang.Object)
	 */
	public String renderTracingValue(Object obj) {
		try {
			Integer state = (Integer)obj;
			if(state.intValue()==3) return "1";
			else return "0";
		} catch (Exception e) {
			return "0";
		}
	}

}
