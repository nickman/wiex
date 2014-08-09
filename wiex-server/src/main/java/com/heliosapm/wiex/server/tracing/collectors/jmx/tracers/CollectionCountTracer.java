/**
 * 
 */
package com.heliosapm.wiex.server.tracing.collectors.jmx.tracers;

import java.util.Collection;

import com.heliosapm.wiex.server.tracing.collectors.jmx.SimpleObjectTracer;

/**
 * <p>Title: CollectionCountTracer</p>
 * <p>Description: Traces the size of a passed collection.</p> 
 * <p>Copyright: Copyright (c) 2007</p>
 * <p>Company: Helios Development Group</p>
 * @author Whitehead
 * @version $Revision: 1.1 $
 */

public class CollectionCountTracer implements SimpleObjectTracer {

	/**
	 * If the passed object implements the <code>java.util.Collection</code> interface, the size of the collection will be returned.
	 * @param obj The collection to be traced.
	 * @return The size of the collection or null if the passed object is not a <code>java.util.Collection</code>. 
	 * @see com.heliosapm.wiex.server.collectors.jmx.SimpleObjectTracer#renderTracingValue(java.lang.Object)
	 */
	public String renderTracingValue(Object obj) {
		if(obj instanceof Collection) {
			return "" + ((Collection)obj).size();
		} else {
			return null;
		}		
	}

}
