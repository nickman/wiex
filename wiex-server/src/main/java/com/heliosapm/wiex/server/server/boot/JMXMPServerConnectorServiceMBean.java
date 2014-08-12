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

import javax.management.remote.JMXServiceURL;
import org.jboss.system.ServiceMBean;


/**
 * <p>Title: JMXMPServerConnectorServiceMBean</p>
 * <p>Description: </p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>com.heliosapm.wiex.server.server.boot.JMXMPServerConnectorServiceMBean</code></p>
 */

public interface JMXMPServerConnectorServiceMBean extends ServiceMBean {
	/**
	 * Returns the configured JMXServiceURL 
	 * @return the jmxServiceUrl
	 */
	public JMXServiceURL getJmxServiceUrl();

	/**
	 * Sets the configured JMXServiceURL 
	 * @param jmxServiceUrl the jmxServiceUrl to set
	 */
	public void setJmxServiceUrl(JMXServiceURL jmxServiceUrl);


	/**
	 * Returns the name of the target mbeanserver
	 * @return the targetServer
	 */
	public String getTargetServer();


	/**
	 * Sets the name of the target mbeanserver
	 * @param targetServer the targetServer to set
	 */
	public void setTargetServer(String targetServer);


	/**
	 * Returns the current connection ids
	 * @return the current connection ids
	 * @see javax.management.remote.JMXConnectorServer#getConnectionIds()
	 */
	public String[] getConnectionIds();


	/**
	 * Returns the JMXServiceURL
	 * @return the JMXServiceURL
	 * @see javax.management.remote.JMXConnectorServerMBean#getAddress()
	 */
	public JMXServiceURL getAddress();


}

