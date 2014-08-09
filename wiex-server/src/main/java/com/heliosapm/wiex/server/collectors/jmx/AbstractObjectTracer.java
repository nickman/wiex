/**
 * 
 */
package com.heliosapm.wiex.server.collectors.jmx;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>Title: AbstractObjectTracer</p>
 * <p>Description: </p> 
 * <p>Copyright: Copyright (c) 2007</p>
 * <p>Company: Helios Development Group</p>
 * @author Whitehead
 * @version $Revision: 1.1 $
 */

public abstract class AbstractObjectTracer implements ObjectTracer {
	protected Map<String, Object> properties = new HashMap<String, Object>();

	/**
	 * Adds a new configuration property.
	 * @param name The name of the property.
	 * @param value The value of the property.
	 * @see com.heliosapm.wiex.server.collectors.jmx.ObjectTracer#setProperty(java.lang.String, java.lang.Object)
	 */
	public void setProperty(String name, Object value) {
		properties.put(name, value);
	}

}
