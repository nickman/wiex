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
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import javax.management.AttributeChangeNotification;
import javax.management.BadAttributeValueExpException;
import javax.management.BadBinaryOpValueExpException;
import javax.management.BadStringOperationException;
import javax.management.InvalidApplicationException;
import javax.management.MBeanServer;
import javax.management.MBeanServerDelegate;
import javax.management.MBeanServerNotification;
import javax.management.Notification;
import javax.management.NotificationBroadcaster;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.management.QueryEval;
import javax.management.QueryExp;

import org.jboss.system.ServiceMBeanSupport;

import com.heliosapm.wiex.tracing.tracing.ITracer;
import com.heliosapm.wiex.tracing.tracing.TracerFactory;

/**
 * <p>Title: WIEXBootstrap</p>
 * <p>Description: Bootstrap service for WIEX deployment. Provides deploy/undeploy dependencies and events and 
 * provides a central location where properties can be set.</p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>com.heliosapm.wiex.server.server.boot.WIEXBootstrap</code></p>
 */

public class WIEXBootstrap extends ServiceMBeanSupport implements WIEXBootstrapMBean, NotificationFilter, NotificationListener {
	/**  */
	private static final long serialVersionUID = 8781115395841617217L;
	/** The bootstrap defined properties */
	protected Properties wiexProperties = new Properties();
	/** The platform agent */
	protected MBeanServer platformServer = null;
	/** A set of cross-registered beans */
	protected final Map<ObjectName, CrossRegisterMXDynamicMBean> crossRegistered = new ConcurrentHashMap<ObjectName, CrossRegisterMXDynamicMBean>();
	
	/**
	 * {@inheritDoc}
	 * @see org.jboss.system.ServiceMBeanSupport#startService()
	 */
	public void startService() throws Exception {
		log.info("\n\t========================================\n\tStarting service [" + serviceName + "]\n\t========================================\n");
		System.getProperties().putAll(wiexProperties);
		platformServer = ManagementFactory.getPlatformMBeanServer();
		ITracer tracer = TracerFactory.getInstance();
		log.info("ITracer Enabled: [" + tracer.getClass().getName() + "]");
		//initializeCrossRegister();
		log.info("\n\t========================================\n\tStarted service [" + serviceName + "]\n\t========================================\n");
	}
	
	public void stopService() throws Exception {
//		log.info("\n\t========================================\n\tStopping service [" + serviceName + "]\n\t========================================\n");
		log.info("\n\t========================================\n\tStopped service [" + serviceName + "]\n\t========================================\n");
	}
	
	
	
	/**
	 * <p>Title: WiexObjectNameFilter</p>
	 * <p>Description: A JMX query to filter in WIEX domained ObjectNames</p> 
	 * <p>Company: Helios Development Group LLC</p>
	 * @author Whitehead (nwhitehead AT heliosdev DOT org)
	 * <p><code>com.heliosapm.wiex.server.server.boot.WiexObjectNameFilter</code></p>
	 */
	private static final class WiexObjectNameFilter extends QueryEval implements QueryExp {
		/**  */
		private static final long serialVersionUID = -6881944248714132956L;

		/**
		 * {@inheritDoc}
		 * @see javax.management.QueryExp#apply(javax.management.ObjectName)
		 */
		@Override
		public boolean apply(ObjectName name) throws BadStringOperationException, BadBinaryOpValueExpException, BadAttributeValueExpException, InvalidApplicationException { 
			if(name==null) return false;
			String domain = name.getDomain();
			if(domain==null) return false;
			return domain.startsWith("com.heliosapm.wiex");
		}
	}
	
	private static final WiexObjectNameFilter WIEX_ON_FILTER = new WiexObjectNameFilter();
	
	/**
	 * Initializes the CrossRegister process
	 */
	protected void initializeCrossRegister() {
		try {
			server.addNotificationListener(MBeanServerDelegate.DELEGATE_NAME, this, this, null);
			final ObjectName allFilter = new ObjectName("*:*");
			for(ObjectName on: server.queryNames(allFilter, WIEX_ON_FILTER)) {
				try {
					crossRegister(on);
				} catch (Exception ex) {
					log.error("Failed to cross-register MBean [" + on + "]:" + ex);
				}
			}
		} catch (Exception ex) {
			log.error("Unrecoverable error initializing cross-registers", ex);
		}
		
	}
	
	
	/**
	 * {@inheritDoc}
	 * @see com.heliosapm.wiex.server.server.boot.WIEXBootstrapMBean#setWiexProperties(java.util.Properties)
	 */
	public void setWiexProperties(Properties props) {
		if(props!=null) {
			wiexProperties.putAll(props);
		}
	}
	
	/**
	 * {@inheritDoc}
	 * @see com.heliosapm.wiex.server.server.boot.WIEXBootstrapMBean#getWiexProperties()
	 */
	public Properties getWiexProperties() {
		return wiexProperties;
	}
	
	/**
	 * {@inheritDoc}
	 * @see com.heliosapm.wiex.server.server.boot.WIEXBootstrapMBean#getProperty(java.lang.String, java.lang.String)
	 */
	public String getProperty(String name, String defaultValue) {
		return wiexProperties.getProperty(name, defaultValue);
	}
	
	/**
	 * {@inheritDoc}
	 * @see com.heliosapm.wiex.server.server.boot.WIEXBootstrapMBean#getProperty(java.lang.String)
	 */
	public String getProperty(String name) {
		return getProperty(name, null);
	}
	
	/**
	 * {@inheritDoc}
	 * @see com.heliosapm.wiex.server.server.boot.WIEXBootstrapMBean#setProperty(java.lang.String, java.lang.String)
	 */
	public void setProperty(String name, String value) {
		Object old = wiexProperties.setProperty(name.trim(), value.trim());		
		if(!value.equals(old)) {
			System.setProperty(name.trim(), value.trim());
			sendNotification(new AttributeChangeNotification(serviceName, this.nextNotificationSequenceNumber(), System.currentTimeMillis(), "Property [%s] changed from [%s] to [%s]", name, String.class.getName(), old, value.trim()));
		}
	}

	/**
	 * Attempts to cross-register the MBean registered with the passed ObjectName
	 * @param on the ObjectName of the MBean to cross-register
	 */
	protected void crossRegister(ObjectName on) {
		if(!crossRegistered.containsKey(on)) {
			synchronized(on) {
				if(!crossRegistered.containsKey(on)) {
					
					try {
						CrossRegisterMXDynamicMBean dmb = new  CrossRegisterMXDynamicMBean(on, server.getMBeanInfo(on), server);
						if(!platformServer.isRegistered(on)) {
							platformServer.registerMBean(dmb, on);
						}
						if(server.isInstanceOf(on, NotificationBroadcaster.class.getName())) {
							server.addNotificationListener(on, dmb, dmb, on);
						}
						crossRegistered.put(on, dmb);
					} catch (Exception ex) {
						log.warn("Failed to cross-register [" + on + "]", ex);
					}
				}
			}
		}
	}
	
	/**
	 * Unregisters a cross-registered MBean
	 * @param on the ObjectName of the MBean to un-register
	 */
	protected void unregister(ObjectName on) {
		try {
			CrossRegisterMXDynamicMBean dmb = crossRegistered.get(on);
			if(platformServer.isRegistered(on)) {
				platformServer.unregisterMBean(on);
			}
			try { server.removeNotificationListener(on, dmb); } catch (Exception ex) {/* No Op */}
		} catch (Exception ex) {
			log.warn("Failed to undeploy cross-register [" + on + "]", ex);
		} finally {
			crossRegistered.remove(on);
		}
	}
	
	/**
	 * {@inheritDoc}
	 * @see javax.management.NotificationListener#handleNotification(javax.management.Notification, java.lang.Object)
	 */
	@Override
	public void handleNotification(Notification notification, Object handback) {
		if(notification!=null && notification instanceof MBeanServerNotification) {
			MBeanServerNotification notif = (MBeanServerNotification)notification;
			ObjectName on = notif.getMBeanName();
			if(on!=null) {
				if(on.getDomain().startsWith("com.heliosapm.wiex")) {
					if( MBeanServerNotification.REGISTRATION_NOTIFICATION.equals(notif.getType())) {
						crossRegister(on);
					} else if( MBeanServerNotification.UNREGISTRATION_NOTIFICATION.equals(notif.getType())) {
						unregister(on);
					}
				}
			}
		}		
	}

	/**
	 * {@inheritDoc}
	 * @see javax.management.NotificationFilter#isNotificationEnabled(javax.management.Notification)
	 */
	@Override
	public boolean isNotificationEnabled(Notification notification) {
		//log.info("\n\tProcessing Notification: [" + notification + "]");
		if(notification!=null && notification instanceof MBeanServerNotification) {
			MBeanServerNotification notif = (MBeanServerNotification)notification;
			ObjectName on = notif.getMBeanName();
			if(on!=null) {
				if(on.getDomain().startsWith("com.heliosapm.wiex")) {
					return true;
				}
			}
		}
		return false;
	}
	
	
}
