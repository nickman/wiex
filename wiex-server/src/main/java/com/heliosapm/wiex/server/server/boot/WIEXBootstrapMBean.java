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

import java.util.Properties;

import org.jboss.system.ServiceMBean;

/**
 * <p>Title: WIEXBootstrapMBean</p>
 * <p>Description: JMX MBean interface for {@link WIEXBootstrap}</p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>com.heliosapm.wiex.server.server.boot.WIEXBootstrapMBean</code></p>
 */

public interface WIEXBootstrapMBean extends ServiceMBean {
	/**
	 * Sets the WIEX configuration properties to load
	 * @param props the WIEX configuration properties
	 */
	public void setWiexProperties(Properties props);
	
	/**
	 * Returns the WIEX configured properties
	 * @return the WIEX configured properties
	 */
	public Properties getWiexProperties();
	
	/**
	 * Returns the value of the named wiex property, or the default if not found
	 * @param name The name of the property
	 * @param defaultValue The default value
	 * @return the effective value of the property
	 */
	public String getProperty(String name, String defaultValue);
	
	/**
	 * Returns the value of the named wiex property
	 * @param name The name of the property
	 * @return the configured value of the property
	 */
	public String getProperty(String name);
	
	
	/**
	 * Sets the name wiex property value
	 * @param name The name of the property
	 * @param value The value of the property
	 */
	public void setProperty(String name, String value);

}
