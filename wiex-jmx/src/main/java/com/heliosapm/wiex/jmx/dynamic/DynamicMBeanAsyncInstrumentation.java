package com.heliosapm.wiex.jmx.dynamic;

import com.heliosapm.wiex.jmx.dynamic.annotation.JMXAttribute;

/**
 * <p>Title: DynamicMBeanAsyncInstrumentation</p>
 * <p>Description: Managed object for exposing a ManagedObjectDynamicMBean's Async Instrumentation</p> 
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Organization: Helios Development Group</p>
 * @author Whitehead
 * @version $Revision: 1.1 $
 */

public class DynamicMBeanAsyncInstrumentation {
	
	/**The instance of the ManagedObjectDynamicMBean that this object will issue core functions for.*/
	protected ManagedObjectDynamicMBean mbean = null;
	
	/**
	 * Internla constructor.
	 * @param mbean The ManagedObjectDynamicMBean to issue core functions for.
	 */
	protected DynamicMBeanAsyncInstrumentation(ManagedObjectDynamicMBean mbean) {
		this.mbean = mbean;
	}
	
	/**
	 * The depth of the asynchronous request processor queue.
	 * @return The async request depth.
	 */
	@JMXAttribute(description="The Depth of the Asynch Request Queue", name="AsyncQueueDepth")
	public int getAsyncQueueDepth() {
		if(mbean.asyncRequests!=null) return mbean.asyncRequests.size();
		else return 0;
	}
	
	/**
	 * The number of active threads in the asynchronous request processing thread pool.
	 * @return The number of active threads in the thread pool.
	 */
	@JMXAttribute(description="The Number of Active Threads In The Asynch Processor Thread Pool", name="AsyncActiveThreads")
	public int getAsyncActiveThreads() {
		if(mbean.threadPool!=null) return mbean.threadPool.getActiveCount();
		else return 0;
	}
	
	/**
	 * Returns the current MBeanServer Agent Id.
	 * @return The current MBeanServer Agent Id.
	 */
	@JMXAttribute(description="The MBeanServer Id", name="MBeanServerId")
	public String getMBeanServerId() {
		return mbean.agentId;
	}
	
	
	/**
	 * The number of core pool size of the asynchronous request processing thread pool.
	 * @return The core pool size the thread pool.
	 */
	@JMXAttribute(description="The Current Thread Pool Size for the Asynch Processor", name="AsyncCurrentPoolSize")
	public int getAsyncPoolSize() {
		if(mbean.threadPool!=null) return mbean.threadPool.getPoolSize();
		else return 0;
	}
	
	
	/**
	 * The largest pool size of the asynchronous request processing thread pool since it was started.
	 * @return The largest pool size of the thread pool.
	 */
	@JMXAttribute(description="The Largest Thread Pool Size for the Asynch Processor", name="AsyncLargestPoolSize")
	public int getAsyncLargestPoolSize() {
		if(mbean.threadPool!=null) return mbean.threadPool.getLargestPoolSize();
		else return 0;
	}
}
