package com.heliosapm.wiex.jmx.dynamic;

import com.heliosapm.wiex.jmx.dynamic.annotation.JMXAttribute;
import com.heliosapm.wiex.jmx.dynamic.annotation.JMXManagedObject;
import com.heliosapm.wiex.jmx.dynamic.annotation.JMXOperation;
import com.heliosapm.wiex.jmx.dynamic.annotation.JMXOperationParameter;

/**
 * <p>Title: DynamicMBeanCoreFunctions</p>
 * <p>Description: A set of core functions for the ManagedObjectDynamicMBean.
 * Implemented as a stand alone object to keep the simple invocation structure.</p> 
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Organization: Helios Development Group</p>
 * @author Whitehead
 * @version $Revision: 1.2 $
 */
@JMXManagedObject(declared=true, annotated=true)
public class DynamicMBeanCoreFunctions {
	
	/**The instance of the ManagedObjectDynamicMBean that this object will issue core functions for.*/
	protected ManagedObjectDynamicMBean mbean = null;
	
	/**
	 * Internla constructor.
	 * @param mbean The ManagedObjectDynamicMBean to issue core functions for.
	 */
	protected DynamicMBeanCoreFunctions(ManagedObjectDynamicMBean mbean) {
		this.mbean = mbean;
	}
	
	/**
	 * Adds a new managed object to the managed object mbean.
	 * @param managedObject An object to be managed.
	 */
	@JMXOperation(description="Adds a new object to be managed.", name="addManagedObject")
	public void addManagedObject(@JMXOperationParameter(name="NewManagedObject", description="A new object to be managed by the MBean.") Object managedObject) {
		mbean.reflectObject(managedObject);
		mbean.updateMBeanInfo();
	}
		
	
}
