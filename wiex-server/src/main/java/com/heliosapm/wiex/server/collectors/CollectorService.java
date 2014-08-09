package com.heliosapm.wiex.server.collectors;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import com.heliosapm.wiex.jmx.dynamic.ManagedObjectDynamicMBean;


/**
 * <p>Title: CollectorService</p>
 * <p>Description: Generic collection service that bootstraps any Collector module.</p> 
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Company: Helios Development Group</p>
 * @author Whitehead
 * @version $Revision: 1.1 $
 */

public class CollectorService extends ManagedObjectDynamicMBean {
	
	/**The managed collector */
	protected BaseCollector collector = null;
	
	/**
	 * Insantiates a new Collector service.
	 * @param collectorClassName The class name of the collector to manage. Must be the class name of a BaseCollector. 
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException
	 */
	public CollectorService(String collectorClassName) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		super();		
		collector = (BaseCollector)Class.forName(collectorClassName).newInstance();
		this.reflectObject(collector);
	}
	
	/**
	 * Insantiates a new Collector service with a preconfigured collector.
	 */
	public CollectorService(BaseCollector collector) {
		super();		
		this.collector = collector;
		this.reflectObject(collector);
	}	
	
	/**
	 * Callback from the MBeanServer before the mbean is registered.
	 * @param server The MBeanServer
	 * @param name The objectName of the MBean.
	 * @return The registered objectName
	 * @throws Exception
	 * @see com.heliosapm.wiex.jmx.dynamic.ManagedObjectDynamicMBean#preRegister(javax.management.MBeanServer, javax.management.ObjectName)
	 */
	public ObjectName preRegister(MBeanServer server, ObjectName name) throws Exception {
		
		collector.setMbeanServer(server);
		collector.setObjectName(name);
		
		return super.preRegister(server, name);
	}

	/**
	 * @return the collector
	 */
	public BaseCollector getCollector() {
		return collector;
	}

	/**
	 * @param collector the collector to set
	 */
	public void setCollector(BaseCollector collector) {
		this.collector = collector;
	}
	
}
