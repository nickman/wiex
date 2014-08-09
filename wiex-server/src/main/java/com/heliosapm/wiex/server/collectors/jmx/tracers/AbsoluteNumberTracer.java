/**
 * 
 */
package com.heliosapm.wiex.server.collectors.jmx.tracers;

import com.heliosapm.wiex.server.collectors.jmx.SimpleObjectTracer;

/**
 * <p>Title: AbsoluteNumberTracer</p>
 * <p>Description: Converts the returned numerical value to an absolute number.</p> 
 * <p>Copyright: Copyright (c) 2007</p>
 * <p>Company: Helios Development Group</p>
 * @author Whitehead
 * @version $Revision$
 */

public class AbsoluteNumberTracer implements SimpleObjectTracer {

	/**
	 * 
	 */
	public AbsoluteNumberTracer() {
		
	}

	/**
	 * @param obj
	 * @return
	 * @see com.adp.sbs.metrics.tracing.collectors.jmx.SimpleObjectTracer#renderTracingValue(java.lang.Object)
	 */
	public String renderTracingValue(Object obj) {
		int i = Integer.parseInt(obj.toString());
		return "" + Math.abs(i);		
	}

}
