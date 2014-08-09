/**
 * 
 */
package com.heliosapm.wiex.server.tracing.collectors.jdbc.cache;

import com.heliosapm.wiex.jmx.dynamic.ManagedObjectDynamicMBean;
import com.heliosapm.wiex.jmx.dynamic.annotation.JMXManagedObject;
import com.heliosapm.wiex.jmx.dynamic.annotation.JMXOperation;
import com.heliosapm.wiex.jmx.dynamic.annotation.JMXOperationParameter;

/**
 * <p>Title: CachedResultSetService</p>
 * <p>Description: Customized ManagedObjectDynamicMBean for handling cached result sets.</p> 
 * <p>Copyright: Copyright (c) 2007</p>
 * <p>Company: Helios Development Group</p>
 * @author Whitehead
 * @version $Revision$
 */
@JMXManagedObject (annotated=true, declared=true)
public class CachedResultSetService extends ManagedObjectDynamicMBean {
	
	public CachedResultSetService() {
		reflectObject(this);
	}
	
	/**
	 * Adds a new CachedResultSet attribute to the CachedResultSetService.
	 * @param attributeName
	 * @param crs
	 */
	@JMXOperation (name="addCachedResultSet", description="Adds a new CachedResultSet attribute to the CachedResultSetService.")
	public void addCachedResultSet(@JMXOperationParameter (description="The CachedResultSet to add as an attribute", name="CachedResultSet") CachedResultSet crs) {
		reflectObject(crs);
		updateMBeanInfo();
	}
}
