/**
 * 
 */
package com.heliosapm.wiex.jmx.util;

import java.io.IOException;
import java.util.Set;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.InvalidAttributeValueException;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServerConnection;
import javax.management.NotCompliantMBeanException;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.QueryExp;
import javax.management.ReflectionException;

/**
 * <p>Title: SmartMBeanServer</p>
 * <p>Description: An MBean Server that will create MBeans and Attributes on invocation.</p> 
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Organization: Helios Development Group</p>
 * @author Whitehead
 * @version $Revision$
 */

public class SmartMBeanServer implements MBeanServerConnection {

	/**
	 * @param name
	 * @param listener
	 * @param filter
	 * @param handback
	 * @throws InstanceNotFoundException
	 * @throws IOException
	 * @see javax.management.MBeanServerConnection#addNotificationListener(javax.management.ObjectName, javax.management.NotificationListener, javax.management.NotificationFilter, java.lang.Object)
	 */
	public void addNotificationListener(ObjectName name,
			NotificationListener listener, NotificationFilter filter,
			Object handback) throws InstanceNotFoundException, IOException {
		// TODO Auto-generated method stub

	}

	/**
	 * @param name
	 * @param listener
	 * @param filter
	 * @param handback
	 * @throws InstanceNotFoundException
	 * @throws IOException
	 * @see javax.management.MBeanServerConnection#addNotificationListener(javax.management.ObjectName, javax.management.ObjectName, javax.management.NotificationFilter, java.lang.Object)
	 */
	public void addNotificationListener(ObjectName name, ObjectName listener,
			NotificationFilter filter, Object handback)
			throws InstanceNotFoundException, IOException {
		// TODO Auto-generated method stub

	}

	/**
	 * @param className
	 * @param name
	 * @return
	 * @throws ReflectionException
	 * @throws InstanceAlreadyExistsException
	 * @throws MBeanRegistrationException
	 * @throws MBeanException
	 * @throws NotCompliantMBeanException
	 * @throws IOException
	 * @see javax.management.MBeanServerConnection#createMBean(java.lang.String, javax.management.ObjectName)
	 */
	public ObjectInstance createMBean(String className, ObjectName name)
			throws ReflectionException, InstanceAlreadyExistsException,
			MBeanRegistrationException, MBeanException,
			NotCompliantMBeanException, IOException {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @param className
	 * @param name
	 * @param loaderName
	 * @return
	 * @throws ReflectionException
	 * @throws InstanceAlreadyExistsException
	 * @throws MBeanRegistrationException
	 * @throws MBeanException
	 * @throws NotCompliantMBeanException
	 * @throws InstanceNotFoundException
	 * @throws IOException
	 * @see javax.management.MBeanServerConnection#createMBean(java.lang.String, javax.management.ObjectName, javax.management.ObjectName)
	 */
	public ObjectInstance createMBean(String className, ObjectName name,
			ObjectName loaderName) throws ReflectionException,
			InstanceAlreadyExistsException, MBeanRegistrationException,
			MBeanException, NotCompliantMBeanException,
			InstanceNotFoundException, IOException {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @param className
	 * @param name
	 * @param params
	 * @param signature
	 * @return
	 * @throws ReflectionException
	 * @throws InstanceAlreadyExistsException
	 * @throws MBeanRegistrationException
	 * @throws MBeanException
	 * @throws NotCompliantMBeanException
	 * @throws IOException
	 * @see javax.management.MBeanServerConnection#createMBean(java.lang.String, javax.management.ObjectName, java.lang.Object[], java.lang.String[])
	 */
	public ObjectInstance createMBean(String className, ObjectName name,
			Object[] params, String[] signature) throws ReflectionException,
			InstanceAlreadyExistsException, MBeanRegistrationException,
			MBeanException, NotCompliantMBeanException, IOException {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @param className
	 * @param name
	 * @param loaderName
	 * @param params
	 * @param signature
	 * @return
	 * @throws ReflectionException
	 * @throws InstanceAlreadyExistsException
	 * @throws MBeanRegistrationException
	 * @throws MBeanException
	 * @throws NotCompliantMBeanException
	 * @throws InstanceNotFoundException
	 * @throws IOException
	 * @see javax.management.MBeanServerConnection#createMBean(java.lang.String, javax.management.ObjectName, javax.management.ObjectName, java.lang.Object[], java.lang.String[])
	 */
	public ObjectInstance createMBean(String className, ObjectName name,
			ObjectName loaderName, Object[] params, String[] signature)
			throws ReflectionException, InstanceAlreadyExistsException,
			MBeanRegistrationException, MBeanException,
			NotCompliantMBeanException, InstanceNotFoundException, IOException {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @param name
	 * @param attribute
	 * @return
	 * @throws MBeanException
	 * @throws AttributeNotFoundException
	 * @throws InstanceNotFoundException
	 * @throws ReflectionException
	 * @throws IOException
	 * @see javax.management.MBeanServerConnection#getAttribute(javax.management.ObjectName, java.lang.String)
	 */
	public Object getAttribute(ObjectName name, String attribute)
			throws MBeanException, AttributeNotFoundException,
			InstanceNotFoundException, ReflectionException, IOException {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @param name
	 * @param attributes
	 * @return
	 * @throws InstanceNotFoundException
	 * @throws ReflectionException
	 * @throws IOException
	 * @see javax.management.MBeanServerConnection#getAttributes(javax.management.ObjectName, java.lang.String[])
	 */
	public AttributeList getAttributes(ObjectName name, String[] attributes)
			throws InstanceNotFoundException, ReflectionException, IOException {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @return
	 * @throws IOException
	 * @see javax.management.MBeanServerConnection#getDefaultDomain()
	 */
	public String getDefaultDomain() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @return
	 * @throws IOException
	 * @see javax.management.MBeanServerConnection#getDomains()
	 */
	public String[] getDomains() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @return
	 * @throws IOException
	 * @see javax.management.MBeanServerConnection#getMBeanCount()
	 */
	public Integer getMBeanCount() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @param name
	 * @return
	 * @throws InstanceNotFoundException
	 * @throws IntrospectionException
	 * @throws ReflectionException
	 * @throws IOException
	 * @see javax.management.MBeanServerConnection#getMBeanInfo(javax.management.ObjectName)
	 */
	public MBeanInfo getMBeanInfo(ObjectName name)
			throws InstanceNotFoundException, IntrospectionException,
			ReflectionException, IOException {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @param name
	 * @return
	 * @throws InstanceNotFoundException
	 * @throws IOException
	 * @see javax.management.MBeanServerConnection#getObjectInstance(javax.management.ObjectName)
	 */
	public ObjectInstance getObjectInstance(ObjectName name)
			throws InstanceNotFoundException, IOException {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @param name
	 * @param operationName
	 * @param params
	 * @param signature
	 * @return
	 * @throws InstanceNotFoundException
	 * @throws MBeanException
	 * @throws ReflectionException
	 * @throws IOException
	 * @see javax.management.MBeanServerConnection#invoke(javax.management.ObjectName, java.lang.String, java.lang.Object[], java.lang.String[])
	 */
	public Object invoke(ObjectName name, String operationName,
			Object[] params, String[] signature)
			throws InstanceNotFoundException, MBeanException,
			ReflectionException, IOException {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @param name
	 * @param className
	 * @return
	 * @throws InstanceNotFoundException
	 * @throws IOException
	 * @see javax.management.MBeanServerConnection#isInstanceOf(javax.management.ObjectName, java.lang.String)
	 */
	public boolean isInstanceOf(ObjectName name, String className)
			throws InstanceNotFoundException, IOException {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * @param name
	 * @return
	 * @throws IOException
	 * @see javax.management.MBeanServerConnection#isRegistered(javax.management.ObjectName)
	 */
	public boolean isRegistered(ObjectName name) throws IOException {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * @param name
	 * @param query
	 * @return
	 * @throws IOException
	 * @see javax.management.MBeanServerConnection#queryMBeans(javax.management.ObjectName, javax.management.QueryExp)
	 */
	public Set queryMBeans(ObjectName name, QueryExp query) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @param name
	 * @param query
	 * @return
	 * @throws IOException
	 * @see javax.management.MBeanServerConnection#queryNames(javax.management.ObjectName, javax.management.QueryExp)
	 */
	public Set queryNames(ObjectName name, QueryExp query) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @param name
	 * @param listener
	 * @throws InstanceNotFoundException
	 * @throws ListenerNotFoundException
	 * @throws IOException
	 * @see javax.management.MBeanServerConnection#removeNotificationListener(javax.management.ObjectName, javax.management.ObjectName)
	 */
	public void removeNotificationListener(ObjectName name, ObjectName listener)
			throws InstanceNotFoundException, ListenerNotFoundException,
			IOException {
		// TODO Auto-generated method stub

	}

	/**
	 * @param name
	 * @param listener
	 * @throws InstanceNotFoundException
	 * @throws ListenerNotFoundException
	 * @throws IOException
	 * @see javax.management.MBeanServerConnection#removeNotificationListener(javax.management.ObjectName, javax.management.NotificationListener)
	 */
	public void removeNotificationListener(ObjectName name,
			NotificationListener listener) throws InstanceNotFoundException,
			ListenerNotFoundException, IOException {
		// TODO Auto-generated method stub

	}

	/**
	 * @param name
	 * @param listener
	 * @param filter
	 * @param handback
	 * @throws InstanceNotFoundException
	 * @throws ListenerNotFoundException
	 * @throws IOException
	 * @see javax.management.MBeanServerConnection#removeNotificationListener(javax.management.ObjectName, javax.management.ObjectName, javax.management.NotificationFilter, java.lang.Object)
	 */
	public void removeNotificationListener(ObjectName name,
			ObjectName listener, NotificationFilter filter, Object handback)
			throws InstanceNotFoundException, ListenerNotFoundException,
			IOException {
		// TODO Auto-generated method stub

	}

	/**
	 * @param name
	 * @param listener
	 * @param filter
	 * @param handback
	 * @throws InstanceNotFoundException
	 * @throws ListenerNotFoundException
	 * @throws IOException
	 * @see javax.management.MBeanServerConnection#removeNotificationListener(javax.management.ObjectName, javax.management.NotificationListener, javax.management.NotificationFilter, java.lang.Object)
	 */
	public void removeNotificationListener(ObjectName name,
			NotificationListener listener, NotificationFilter filter,
			Object handback) throws InstanceNotFoundException,
			ListenerNotFoundException, IOException {
		// TODO Auto-generated method stub

	}

	/**
	 * @param name
	 * @param attribute
	 * @throws InstanceNotFoundException
	 * @throws AttributeNotFoundException
	 * @throws InvalidAttributeValueException
	 * @throws MBeanException
	 * @throws ReflectionException
	 * @throws IOException
	 * @see javax.management.MBeanServerConnection#setAttribute(javax.management.ObjectName, javax.management.Attribute)
	 */
	public void setAttribute(ObjectName name, Attribute attribute)
			throws InstanceNotFoundException, AttributeNotFoundException,
			InvalidAttributeValueException, MBeanException,
			ReflectionException, IOException {
		// TODO Auto-generated method stub

	}

	/**
	 * @param name
	 * @param attributes
	 * @return
	 * @throws InstanceNotFoundException
	 * @throws ReflectionException
	 * @throws IOException
	 * @see javax.management.MBeanServerConnection#setAttributes(javax.management.ObjectName, javax.management.AttributeList)
	 */
	public AttributeList setAttributes(ObjectName name, AttributeList attributes)
			throws InstanceNotFoundException, ReflectionException, IOException {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @param name
	 * @throws InstanceNotFoundException
	 * @throws MBeanRegistrationException
	 * @throws IOException
	 * @see javax.management.MBeanServerConnection#unregisterMBean(javax.management.ObjectName)
	 */
	public void unregisterMBean(ObjectName name)
			throws InstanceNotFoundException, MBeanRegistrationException,
			IOException {
		// TODO Auto-generated method stub

	}

}
