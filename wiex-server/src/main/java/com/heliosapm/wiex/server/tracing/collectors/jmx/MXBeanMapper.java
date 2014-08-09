/**
 * 
 */
package com.heliosapm.wiex.server.tracing.collectors.jmx;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryManagerMXBean;
import java.lang.management.MemoryPoolMXBean;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import com.heliosapm.wiex.jmx.util.MBeanServerHelper;


/**
 * <p>Title: MXBeanMapper</p>
 * <p>Description: Simple MBean that maps platform MBeans to the MBeanServer specified in conf. This assists in centralizing JMX diagnostics into one agent.</p> 
 * <p>Copyright: Copyright (c) 2007</p>
 * <p>Company: Helios Development Group</p>
 * @author Whitehead
 * @version $Revision: 1.1 $
 */

public class MXBeanMapper implements MXBeanMapperMBean {
	/** The JMX domain that the platform MXBeans should be re-registered with. Defaults to <code>jboss</code>. */
	protected String targetDomain = "jboss";
	
	/**
	 * @throws Exception
	 * @see com.heliosapm.wiex.server.collectors.jmx.MXBeanMapperMBean#start()
	 */
	public void start() throws Exception {
		
		MBeanServer targetServer = MBeanServerHelper.getMBeanServer(targetDomain);
		// Standard One Instance MXBeans
		try {
			targetServer.registerMBean(ManagementFactory.getClassLoadingMXBean(), new ObjectName(ManagementFactory.CLASS_LOADING_MXBEAN_NAME));
		} catch (Exception e) {}
		try {
			targetServer.registerMBean(ManagementFactory.getMemoryMXBean(), new ObjectName(ManagementFactory.MEMORY_MXBEAN_NAME));
		} catch (Exception e) {}
		try {
			targetServer.registerMBean(ManagementFactory.getThreadMXBean(), new ObjectName(ManagementFactory.THREAD_MXBEAN_NAME));
		} catch (Exception e) {}
		try {
			targetServer.registerMBean(ManagementFactory.getRuntimeMXBean(), new ObjectName(ManagementFactory.RUNTIME_MXBEAN_NAME));
		} catch (Exception e) {}
		try {
			targetServer.registerMBean(ManagementFactory.getOperatingSystemMXBean(), new ObjectName(ManagementFactory.OPERATING_SYSTEM_MXBEAN_NAME));
		} catch (Exception e) {}
		try {
			targetServer.registerMBean(ManagementFactory.getCompilationMXBean(), new ObjectName(ManagementFactory.COMPILATION_MXBEAN_NAME));
		} catch (Exception e) {}
		// Multiple Instance MBeans
		for(GarbageCollectorMXBean gcMx : ManagementFactory.getGarbageCollectorMXBeans()) {
			try {
				targetServer.registerMBean(gcMx, new ObjectName(ManagementFactory.GARBAGE_COLLECTOR_MXBEAN_DOMAIN_TYPE + ",name=" + gcMx.getName()));
			} catch (Exception e) {}			
		}
		for(MemoryManagerMXBean mmMx : ManagementFactory.getMemoryManagerMXBeans()) {
			try {
				targetServer.registerMBean(mmMx, new ObjectName(ManagementFactory.MEMORY_MANAGER_MXBEAN_DOMAIN_TYPE + ",name=" + mmMx.getName()));
			} catch (Exception e) {}			
		}
		for(MemoryPoolMXBean mpMx : ManagementFactory.getMemoryPoolMXBeans()) {
			try {
				targetServer.registerMBean(mpMx, new ObjectName(ManagementFactory.MEMORY_POOL_MXBEAN_DOMAIN_TYPE + ",name=" + mpMx.getName()));
			} catch (Exception e) {}			
		}
	}
	
	/**
	 * 
	 * @see com.heliosapm.wiex.server.collectors.jmx.MXBeanMapperMBean#stop()
	 */
	public void stop() {
		
	}
	

	/**
	 * @return
	 * @see com.heliosapm.wiex.server.collectors.jmx.MXBeanMapperMBean#getTargetDomain()
	 */
	public String getTargetDomain() {
		return targetDomain;
	}

	/**
	 * @param targetDomain
	 * @see com.heliosapm.wiex.server.collectors.jmx.MXBeanMapperMBean#setTargetDomain(java.lang.String)
	 */
	public void setTargetDomain(String targetDomain) {
		this.targetDomain = targetDomain;
	}
}
