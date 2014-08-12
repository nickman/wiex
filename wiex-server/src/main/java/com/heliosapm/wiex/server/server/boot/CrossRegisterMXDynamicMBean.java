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

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.DynamicMBean;
import javax.management.InstanceNotFoundException;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanServer;
import javax.management.Notification;
import javax.management.NotificationBroadcasterSupport;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.management.ReflectionException;

/**
 * <p>Title: CrossRegisterMXDynamicMBean</p>
 * <p>Description: Cross registers wiex beans into another MBeanServer</p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>com.heliosapm.wiex.server.server.boot.CrossRegisterMXDynamicMBean</code></p>
 */

public class CrossRegisterMXDynamicMBean extends NotificationBroadcasterSupport implements DynamicMBean, NotificationListener, NotificationFilter {
	/** The target invocation object name */
	protected final ObjectName target;
	/** The MBeanInfo for the target */
	protected final MBeanInfo info;
	/** The MBeanServer where the target is registered */
	protected final MBeanServer server;
	
	protected static final Executor executor = Executors.newFixedThreadPool(2, new ThreadFactory(){
		final AtomicInteger serial = new AtomicInteger();
		final ThreadGroup threadGroup = new ThreadGroup("CrossRegisterThreadGroup");
		/**
		 * {@inheritDoc}
		 * @see java.util.concurrent.ThreadFactory#newThread(java.lang.Runnable)
		 */
		@Override
		public Thread newThread(Runnable r) {
			Thread t = new Thread(threadGroup, r, "CrossRegisterThread#" + serial.incrementAndGet());
			t.setDaemon(true);
			return t;
		}
	});

	/**
	 * Creates a new CrossRegisterMXDynamicMBean
	 * @param target The target invocation object name
	 * @param info The MBeanInfo for the target
	 * @param server The MBeanServer where the target is registered
	 */
	public CrossRegisterMXDynamicMBean(ObjectName target, MBeanInfo info, MBeanServer server) {
		super(executor);
		this.target = target;
		this.info = info;
		this.server = server;
	}
	

	/**
	 * {@inheritDoc}
	 * @see javax.management.DynamicMBean#getAttribute(java.lang.String)
	 */
	@Override
	public Object getAttribute(String attribute) throws AttributeNotFoundException, MBeanException, ReflectionException {
		try {
			return server.getAttribute(target, attribute);
		} catch (InstanceNotFoundException e) {
			throw new MBeanException(e, "ObjectName [" + target + "] not registered in MBeanServer [" + server.getDefaultDomain() + "]");
		}
	}

	/**
	 * {@inheritDoc}
	 * @see javax.management.DynamicMBean#setAttribute(javax.management.Attribute)
	 */
	@Override
	public void setAttribute(Attribute attribute) throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException {
		try {
			server.setAttribute(target, attribute);
		} catch (InstanceNotFoundException e) {
			throw new MBeanException(e, "ObjectName [" + target + "] not registered in MBeanServer [" + server.getDefaultDomain() + "]");
		}
	}

	/**
	 * {@inheritDoc}
	 * @see javax.management.DynamicMBean#getAttributes(java.lang.String[])
	 */
	@Override
	public AttributeList getAttributes(String[] attributes) {
		try {
			return server.getAttributes(target, attributes);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}		
	}

	/**
	 * {@inheritDoc}
	 * @see javax.management.DynamicMBean#setAttributes(javax.management.AttributeList)
	 */
	@Override
	public AttributeList setAttributes(AttributeList attributes) {
		try {
			return server.setAttributes(target, attributes);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}				
	}

	/**
	 * {@inheritDoc}
	 * @see javax.management.DynamicMBean#invoke(java.lang.String, java.lang.Object[], java.lang.String[])
	 */
	@Override
	public Object invoke(String actionName, Object[] params, String[] signature) throws MBeanException, ReflectionException {
		try {
			return server.invoke(target, actionName, params, signature);
		} catch (InstanceNotFoundException e) {
			throw new MBeanException(e, "ObjectName [" + target + "] not registered in MBeanServer [" + server.getDefaultDomain() + "]");
		}
		
	}

	/**
	 * {@inheritDoc}
	 * @see javax.management.DynamicMBean#getMBeanInfo()
	 */
	@Override
	public MBeanInfo getMBeanInfo() {
		return info;
	}

	/**
	 * {@inheritDoc}
	 * @see javax.management.NotificationFilter#isNotificationEnabled(javax.management.Notification)
	 */
	@Override
	public boolean isNotificationEnabled(Notification notification) {
		return (notification==null ? false : target.equals(notification.getSource()));
	}

	/**
	 * {@inheritDoc}
	 * @see javax.management.NotificationListener#handleNotification(javax.management.Notification, java.lang.Object)
	 */
	@Override
	public void handleNotification(Notification notification, Object handback) {
		if(target.equals(handback)) {
			sendNotification(notification);
		}		
	}


}
