/**
 * Helios, OpenSource Monitoring
 * Brought to you by the Helios Development Group
 *
 * Copyright 2014, Helios Development Group and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org. 
 *
 */
package com.heliosapm.wiex.server.server.boot;

import java.lang.management.ManagementFactory;

import javax.management.MBeanServer;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;

import org.jboss.logging.Logger;
import org.jboss.mx.util.MBeanServerLocator;
import org.jboss.system.ServiceMBeanSupport;
import org.jboss.util.propertyeditor.PropertyEditors;


/**
 * <p>Title: JMXMPServerConnectorService</p>
 * <p>Description: Service to deploy the JMXMP connector server</p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>com.heliosapm.wiex.server.server.boot.JMXMPServerConnectorService</code></p>
 */

public class JMXMPServerConnectorService extends ServiceMBeanSupport implements JMXMPServerConnectorServiceMBean {
	/** Instance logger */
	protected final Logger log = Logger.getLogger(getClass());
	
	/** The JMXServiceURL in string form */
	protected JMXServiceURL jmxServiceUrl = null;
	/** The name of the target mbeanserver, "jboss" or "platform" */
	protected String targetServer = null;
	
	/** The connector service */
	protected JMXConnectorServer connector = null;
	
	static {
		// registers the JMXServiceURL property editor
		PropertyEditors.registerEditor(JMXServiceURL.class, JMXServiceURLPropertyEditor.class);
	}
	
	/**
	 * Creates a new JMXMPServerConnectorService
	 */
	public JMXMPServerConnectorService() {
		log.info("Created JMXMPServerConnectorService Instance");
	}

	
	/**
	 * {@inheritDoc}
	 * @see org.jboss.system.ServiceMBeanSupport#startService()
	 */
	@Override
	protected void startService() throws Exception {		
		log.info("\n\t====================================================\n\tStarting service [" + serviceName + "]\n\t====================================================\n");
		StringBuilder cfg = new StringBuilder("\n\tConfiguration:");
		cfg.append("\n\tJMXServiceURL:").append(jmxServiceUrl);
		cfg.append("\n\tTarget MBeanServer:").append(targetServer);
		log.info(cfg);
		MBeanServer _server = null;
		if("jboss".equalsIgnoreCase(targetServer)) {
			_server = MBeanServerLocator.locateJBoss();
		} else if("platform".equalsIgnoreCase(targetServer)) {
			_server = ManagementFactory.getPlatformMBeanServer();
		} else {
			throw new Exception("Unrecognized target MBeanServer: [" + targetServer + "]");
		}
		connector = JMXConnectorServerFactory.newJMXConnectorServer(jmxServiceUrl, null, _server);
		connector.start();
		log.info("\n\t====================================================\n\tStarted service [" + serviceName + "]\n\t====================================================\n");
	}
	
	/**
	 * {@inheritDoc}
	 * @see org.jboss.system.ServiceMBeanSupport#stopService()
	 */
	@Override
	protected void stopService() throws Exception {
		log.info("\n\t====================================================\n\tStopping service [" + serviceName + "]\n\t====================================================\n");
		if(connector!=null) {
			try {
				connector.stop();
				log.info("Stopped Connector Server for [" + jmxServiceUrl + "]");
			} catch (Exception ex) {/* No Op */}
		}
		log.info("\n\t====================================================\n\tStopped service [" + serviceName + "]\n\t====================================================\n");
	}

	/**
	 * Returns the configured JMXServiceURL 
	 * @return the jmxServiceUrl
	 */
	public JMXServiceURL getJmxServiceUrl() {
		return jmxServiceUrl;
	}

	/**
	 * Sets the configured JMXServiceURL 
	 * @param jmxServiceUrl the jmxServiceUrl to set
	 */
	public void setJmxServiceUrl(JMXServiceURL jmxServiceUrl) {
		this.jmxServiceUrl = jmxServiceUrl;
	}


	/**
	 * Returns the name of the target mbeanserver
	 * @return the targetServer
	 */
	public String getTargetServer() {
		return targetServer;
	}


	/**
	 * Sets the name of the target mbeanserver
	 * @param targetServer the targetServer to set
	 */
	public void setTargetServer(String targetServer) {
		this.targetServer = targetServer;
	}


	/**
	 * Returns the current connection ids
	 * @return the current connection ids
	 * @see javax.management.remote.JMXConnectorServer#getConnectionIds()
	 */
	public String[] getConnectionIds() {
		return connector.getConnectionIds();
	}


	/**
	 * Returns the JMXServiceURL
	 * @return the JMXServiceURL
	 * @see javax.management.remote.JMXConnectorServerMBean#getAddress()
	 */
	public JMXServiceURL getAddress() {
		return connector.getAddress();
	}

}

