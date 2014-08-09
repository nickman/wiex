package com.heliosapm.wiex.server.collectors.jmx;

import java.util.Properties;

import javax.management.MBeanServerConnection;

import com.heliosapm.wiex.jmx.util.MBeanServerHelper;

/**
 * <p>Title: LocalMBeanServerConnectionFactory</p>
 * <p>Description: MBeanConnectionFactory for local (in VM) MBeanServerConnections.</p>
 * <p>Configuration Properties:<ul>
 * <li><code>sbstracing.jmx.factory.domain</code>: The default jmx domain of the local MBeanServer to attach to. Defaults to <code>DefaultDomain</code>
 * </ul>
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Company: Helios Development Group</p>
 * @author Whitehead
 * @version $Revision: 1.1 $
 */

public class LocalMBeanServerConnectionFactory implements
		MBeanServerConnectionFactory {
	
	public static final String DEFAULT_JMX_DOMAIN = "DefaultDomain";
	public static final String JMX_DOMAIN_PROPERTY = "sbstracing.jmx.factory.domain";
	
	protected String jmxDomain = null;
	/**
	 * Acquires a local MBeanServerConnection.
	 * @return An MBeanServerConnection.
	 * @throws MBeanServerConnectionFactoryException 
	 * @see com.heliosapm.wiex.server.collectors.jmx.MBeanServerConnectionFactory#getMBeanServerConnection()
	 */
	public MBeanServerConnection getMBeanServerConnection() throws MBeanServerConnectionFactoryException {
		try {
			return MBeanServerHelper.getMBeanServer(jmxDomain);
		} catch (Exception e) {
			throw new MBeanServerConnectionFactoryException("Failed to get MBeanServerConnection", e);
		}
	}

	/**
	 * Sets the factory's configuration.
	 * @param properties A set of config properties.
	 * @see com.heliosapm.wiex.server.collectors.jmx.MBeanServerConnectionFactory#setProperties(java.util.Properties)
	 */
	public void setProperties(Properties properties) {
		jmxDomain = properties.getProperty(JMX_DOMAIN_PROPERTY, DEFAULT_JMX_DOMAIN);

	}
	
	/**
	 * No Op.
	 * @see com.heliosapm.wiex.server.collectors.jmx.MBeanServerConnectionFactory#close()
	 */
	public void close() {
		
	}

}
