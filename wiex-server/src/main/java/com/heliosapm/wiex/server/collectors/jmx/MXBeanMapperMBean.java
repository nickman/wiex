package com.heliosapm.wiex.server.collectors.jmx;

/**
 * <p>Title: MXBeanMapperMBean</p>
 * <p>Description: Simple MBean Interface for MXBeanMapper.</p> 
 * <p>Copyright: Copyright (c) 2007</p>
 * <p>Company: Helios Development Group</p>
 * @author Whitehead
 * @version $Revision: 1.1 $
 */

public interface MXBeanMapperMBean {

	/**
	 * Executes the mapping.
	 * @throws Exception
	 */
	public abstract void start() throws Exception;

	/**
	 * No Op.
	 */
	public abstract void stop();

	/**
	 * @return the targetDomain
	 */
	public abstract String getTargetDomain();

	/**
	 * @param targetDomain the targetDomain to set
	 */
	public abstract void setTargetDomain(String targetDomain);

}