
package com.heliosapm.wiex.server.collectors.jmx;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXServiceURL;
import javax.management.remote.JMXConnectorFactory;

/**
 * <p>Title: RemoteRMIMBeanServerConnectionFactory</p>
 * <p>Description: MBeanServerConnectionFactory for Remote JMX Monitoring Using the Java 1.5 Agent Remote RMI Connection</p>
 * <p>Configuration Properties:<ul>
 * <li><code>jmx.rmi.url</code>The full JMXServiceURL.
 * </ul>  
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Company: Helios Development Group</p>
 * @author Whitehead
 * @version $Revision: 1.1 $
 */

public class RemoteRMIMBeanServerConnectionFactory implements
		MBeanServerConnectionFactory {
	
	/**	The JMX RMI Connection URL */
	protected String jMXServiceURL = null;
	/** Passed properties mapped for JMX Connection */
	protected Map<String, String> connectionEnvironment = new HashMap<String, String>();
	/** The JMX Connection */
	protected JMXConnector jmxConnector = null;
	
	public static final String JMX_SERVICE_URL = "jmx.rmi.url";
	public static final String JMX_SERVICE_URL_DEFAULT = "service:jmx:rmi:///jndi/rmi://localhost:8004/server";

	/**
	 * Acquires an MBeanServer connection using the JMXConnectorServerFactory.
	 * @return An MBeanServer Connection
	 * @throws MBeanServerConnectionFactoryException
	 * @see com.heliosapm.wiex.server.collectors.jmx.MBeanServerConnectionFactory#getMBeanServerConnection()
	 */
	public MBeanServerConnection getMBeanServerConnection()
			throws MBeanServerConnectionFactoryException {
		try {
			if(jmxConnector==null) {
				JMXServiceURL url = new JMXServiceURL(jMXServiceURL);
				jmxConnector = JMXConnectorFactory.newJMXConnector(url, connectionEnvironment);
				jmxConnector.connect();
			}			
			return jmxConnector.getMBeanServerConnection();			
		} catch (Exception e) {
			try { jmxConnector.close(); } catch (Exception e2) {}
			jmxConnector=null;
			throw new MBeanServerConnectionFactoryException("Failed to acquire JMX Connection", e);
		}
	}

	/**
	 * Sets the factories connection properties.
	 * @param properties Should supply a value for the property <code>jmx.rmi.url</code>.
	 * @see com.heliosapm.wiex.server.collectors.jmx.MBeanServerConnectionFactory#setProperties(java.util.Properties)
	 */
	public void setProperties(Properties properties) {
		jMXServiceURL = properties.getProperty(JMX_SERVICE_URL, JMX_SERVICE_URL_DEFAULT);
		connectionEnvironment.clear();
		String name = null;
		String value = null;
		for(Entry entry: properties.entrySet()) {
			name = entry.getKey().toString();
			value = entry.getValue().toString();
			connectionEnvironment.put(name, value);			
		}		
		
	}
	
	/**
	 * Closes the JMXConnection
	 * @see com.heliosapm.wiex.server.collectors.jmx.MBeanServerConnectionFactory#close()
	 */
	public void close() {
		try { jmxConnector.close(); } catch (Exception e2) {}
		jmxConnector=null;		
	}

	/**
	 * The JMX RMI Connection URL
	 * @return the jMXServiceURL
	 */
	public String getJMXServiceURL() {
		return jMXServiceURL;
	}

	/**
	 * Sets the JMX RMI Connection URL
	 * @param serviceURL the jMXServiceURL to set
	 */
	public void setJMXServiceURL(String serviceURL) {
		jMXServiceURL = serviceURL;
	}

}
