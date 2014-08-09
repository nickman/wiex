package com.heliosapm.wiex.server.collectors.jmx;

import java.util.Properties;
import javax.management.MBeanServerConnection;

/**
 * <p>Title: MBeanServerConnectionFactory</p>
 * <p>Description: Interface defining behavior for acquiring MBean Connections.</p> 
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Company: Helios Development Group</p>
 * @author Whitehead
 * @version $Revision: 1.1 $
 */

public interface MBeanServerConnectionFactory {
	/**
	 * Acquires and returns an MBeanServerConnection.
	 * @return An MBeanConnection.
	 * @throws MBeanServerConnectionFactoryException
	 */
	public MBeanServerConnection getMBeanServerConnection() throws MBeanServerConnectionFactoryException;
	/**
	 * Sets the configuration parameters for the connection factory.
	 * @param properties The configuration parameters.
	 */
	public void setProperties(Properties properties);
	
	/**
	 * Closes resources associated with the MBeanConnection.
	 */
	public void close();
	
}
