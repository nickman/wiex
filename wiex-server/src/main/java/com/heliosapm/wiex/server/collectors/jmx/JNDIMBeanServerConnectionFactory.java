package com.heliosapm.wiex.server.collectors.jmx;

import java.util.Properties;
import java.util.Map.Entry;
import javax.management.MBeanServerConnection;
import javax.naming.Context;
import javax.naming.InitialContext;

/**
 * <p>Title: JNDIMBeanServerConnectionFactory</p>
 * <p>Description: Acquires MBeanServerConnection from JNDI.</p>
 * <p>Configuration Properties:<ul>
 * <li>Any <code>java.naming</code> property used to acquire a JNDI connection.
 * <li><code>sbstracing.jndi.name</code>:The JNDI name of the MBeanServerConnection. 
 * </ul>
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Company: Helios Development Group</p>
 * @author Whitehead
 * @version $Revision: 1.2 $
 */

public class JNDIMBeanServerConnectionFactory implements MBeanServerConnectionFactory {
	
	/**	The remote jndi properties to make the JNDI Connection */
	protected Properties jndiProperties = new Properties();
	/**	The JNDI name of the MBeanServerConnection Object to retrieve */
	protected String jndiName = null;
	/**	The context acquired */
	protected Context context = null;
	/**	Indicates if the JNDI conenction is valid */
	protected boolean connected = false;
	
	public static final String SBSTRACING_JNDI_NAME = "sbstracing.jndi.name";
	public static final String SBSTRACING_DEFAULT_JNDI_NAME = "/jmx/rmi/RMIAdaptor";

	/**
	 * Acquires and returns an MBeanServerConnection from a JNDI resident stub.
	 * @return An MBeanServerConnection
	 * @throws MBeanServerConnectionFactoryException
	 * @see com.heliosapm.wiex.server.collectors.jmx.MBeanServerConnectionFactory#getMBeanServerConnection()
	 */
	public MBeanServerConnection getMBeanServerConnection() throws MBeanServerConnectionFactoryException {
		if(!connected) {
			try {
				getContext();
			} catch (Exception e) {
				connected = false;
				throw new MBeanServerConnectionFactoryException("Failed to get JNDI Connection", e);
			}
		}
		try {
			return (MBeanServerConnection)context.lookup(jndiName);
		} catch (Exception e) {
			connected = false;
			throw new MBeanServerConnectionFactoryException("Failed JNDI Lookup of [" + jndiName + "]", e);
		}
	}
	
	/**
	 * @throws Exception
	 */
	protected void getContext() throws Exception {
		context = new InitialContext(jndiProperties);
		connected = true;
	}

	/**
	 * Adds any properties defined to the kjndi properties supplied to the InitialContextFactory.
	 * @param properties
	 * @see com.heliosapm.wiex.server.collectors.jmx.MBeanServerConnectionFactory#setProperties(java.util.Properties)
	 */
	public void setProperties(Properties properties) {
		jndiProperties.clear();
		jndiName = properties.getProperty(SBSTRACING_JNDI_NAME, SBSTRACING_DEFAULT_JNDI_NAME);
		jndiProperties.putAll(properties);
	}
	
	/**
	 * Closes the JNDI Context
	 * @see com.heliosapm.wiex.server.collectors.jmx.MBeanServerConnectionFactory#close()
	 */
	public void close() {
		try { context.close(); } catch (Exception e){}
	}

}
