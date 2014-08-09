package com.heliosapm.wiex.jmx.dynamic;

import java.lang.reflect.Method;

import javax.management.MBeanAttributeInfo;

/**
 * <p>Title: AttributeContainer</p>
 * <p>Description: Maintains a colection of attributes and managed objects with the link between the attribute and the accessor method.</p> 
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Organization: Helios Development Group</p>
 * @author Whitehead
 * @version $Revision: 1.1 $
 */

public class AttributeContainer {
	
	/**	The managed object where the attribute resides. */
	protected Object targetObject = null;
	/**	A generated attribute info for the managed object's exposed attribute. */
	protected MBeanAttributeInfo attributeInfo = null;
	/**	The method for setting the attribute in the managed object. */
	protected Method targetSetterMethod = null;
	/**	 The method for getting the attribute in the managed object. */
	protected Method targetGetterMethod = null;
	
	/**
	 * Simple constructor.
	 */
	public AttributeContainer() {
		
	}
	
	/**
	 * Creates a new attribute container for the specified managed object's attribute. 
	 * @param targetObject The managed object.
	 * @param attributeInfo The JMX Attribute information.
	 * @param targetGetterMethod The method for setting the attribute in the managed object.
	 * @param targetSetterMethod The method for getting the attribute in the managed object.
	 */
	public AttributeContainer(Object targetObject, MBeanAttributeInfo attributeInfo, Method targetGetterMethod, Method targetSetterMethod) {
		super();
		this.targetObject = targetObject;
		this.attributeInfo = attributeInfo;
		this.targetSetterMethod = targetSetterMethod;
		this.targetGetterMethod = targetGetterMethod;
	}

	/**
	 * @return the attributeInfo
	 */
	public MBeanAttributeInfo getAttributeInfo() {
		return attributeInfo;
	}

	/**
	 * @param attributeInfo the attributeInfo to set
	 */
	public void setAttributeInfo(MBeanAttributeInfo attributeInfo) {
		this.attributeInfo = attributeInfo;
	}

	/**
	 * @return the targetGetterMethod
	 */
	public Method getTargetGetterMethod() {
		return targetGetterMethod;
	}

	/**
	 * @param targetGetterMethod the targetGetterMethod to set
	 */
	public void setTargetGetterMethod(Method targetGetterMethod) {
		this.targetGetterMethod = targetGetterMethod;
	}

	/**
	 * @return the targetObject
	 */
	public Object getTargetObject() {
		return targetObject;
	}

	/**
	 * @param targetObject the targetObject to set
	 */
	public void setTargetObject(Object targetObject) {
		this.targetObject = targetObject;
	}

	/**
	 * @return the targetSetterMethod
	 */
	public Method getTargetSetterMethod() {
		return targetSetterMethod;
	}

	/**
	 * @param targetSetterMethod the targetSetterMethod to set
	 */
	public void setTargetSetterMethod(Method targetSetterMethod) {
		this.targetSetterMethod = targetSetterMethod;
	}
	
}

