/**
 * 
 */
package com.heliosapm.wiex.jmx.remote.alias;

import java.io.IOException;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.DynamicMBean;
import javax.management.InstanceNotFoundException;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.ReflectionException;

/**
 * <p>Title: RemoteAliasMBean</p>
 * <p>Description: </p> 
 * <p>Copyright: Copyright (c) 2007</p>
 * <p>Organization: Helios Development Group</p>
 * @author Whitehead
 * @version $Revision$
 */

public class RemoteAliasMBean implements DynamicMBean {
	
	protected MBeanServerConnection server = null;
	protected ObjectName objectName = null;
	
	/**
	 * Constructs a new alias MBean for the designated object name using the passed MBeanServer.
	 * @param server
	 * @param objectName
	 */
	public RemoteAliasMBean(MBeanServerConnection  server, ObjectName objectName) {
		this.server = server;
		this.objectName = objectName;
	}

	/**
	 * @param attribute
	 * @return
	 * @throws AttributeNotFoundException
	 * @throws MBeanException
	 * @throws ReflectionException
	 * @see javax.management.DynamicMBean#getAttribute(java.lang.String)
	 */
	public Object getAttribute(String attribute)
			throws AttributeNotFoundException, MBeanException,
			ReflectionException {
		try {
			return server.getAttribute(objectName, attribute);
		} catch (InstanceNotFoundException e) {
			throw new MBeanException(e, "Instance of MBean Not Found:" + e);
		} catch (IOException ioe) {
			throw new MBeanException(ioe, "IOException Issuing getAttribute:" + ioe);
		}
	}

	/**
	 * @param attributes
	 * @return
	 * @see javax.management.DynamicMBean#getAttributes(java.lang.String[])
	 */
	public AttributeList getAttributes(String[] attributes) {
		AttributeList al = new AttributeList();
		for(String attribute: attributes) {
				try {
					al.add(new Attribute(attribute, getAttribute(attribute)));
				} catch (Exception e) {}
		}
		return al;
	}

	/**
	 * @return
	 * @see javax.management.DynamicMBean#getMBeanInfo()
	 */
	public MBeanInfo getMBeanInfo() {
		try {
			return server.getMBeanInfo(objectName);
		} catch (Exception e) {
			throw new RuntimeException("getMBeanInfo Exception", e);
		} 
	}

	/**
	 * @param actionName
	 * @param params
	 * @param signature
	 * @return
	 * @throws MBeanException
	 * @throws ReflectionException
	 * @see javax.management.DynamicMBean#invoke(java.lang.String, java.lang.Object[], java.lang.String[])
	 */
	public Object invoke(String actionName, Object[] params, String[] signature)
			throws MBeanException, ReflectionException {
		try {
			return server.invoke(objectName, actionName, params, signature);
		} catch (InstanceNotFoundException e) {
			throw new RuntimeException("Invocation Exception for " + actionName, e);
		}  catch (IOException ioe) {
			throw new MBeanException(ioe, "IOException Issuing invoke." + actionName + ":" + ioe);
		}
	}

	/**
	 * @param attribute
	 * @throws AttributeNotFoundException
	 * @throws InvalidAttributeValueException
	 * @throws MBeanException
	 * @throws ReflectionException
	 * @see javax.management.DynamicMBean#setAttribute(javax.management.Attribute)
	 */
	public void setAttribute(Attribute attribute)
			throws AttributeNotFoundException, InvalidAttributeValueException,
			MBeanException, ReflectionException {
		try {
			server.setAttribute(objectName, attribute);
		} catch (InstanceNotFoundException e) {
			throw new RuntimeException("SetAttribute Exception for " + attribute, e);
		}   catch (IOException ioe) {
			throw new MBeanException(ioe, "IOException Issuing setAttribute:" + ioe);
		}

	}

	/**
	 * @param attributes
	 * @return
	 * @see javax.management.DynamicMBean#setAttributes(javax.management.AttributeList)
	 */
	public AttributeList setAttributes(AttributeList attributes) {
		AttributeList al = new AttributeList();
		for(int i = 0; i < attributes.size(); i++) {
			try {
				setAttribute((Attribute)al.get(i));
				al.add(al.get(i));
			} catch (Exception e) {}
		}
		return al;
	}

	/**
	 * @return
	 */
	public ObjectName getObjectName() {
		return objectName;
	}

	/**
	 * @param objectName
	 */
	public void setObjectName(ObjectName objectName) {
		this.objectName = objectName;
	}

	/**
	 * @return
	 */
	public MBeanServerConnection getServer() {
		return server;
	}

	/**
	 * @param server
	 */
	public void setServer(MBeanServerConnection server) {
		this.server = server;
	}

}
