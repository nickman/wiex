package com.heliosapm.wiex.jmx.dynamic;

import java.lang.reflect.Method;

import javax.management.MBeanOperationInfo;

/**
 * <p>Title: OperationContainer</p>
 * <p>Description: Maintains a colection of operations and managed objects with the link between the operation and the method.</p> 
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Organization: Helios Development Group</p>
 * @author Whitehead
 * @version $Revision: 1.1 $
 */

public class OperationContainer {
	/**	The managed object where the attribute resides. */
	protected Object targetObject = null;
	/**	The JMX information on the operation. */
	protected MBeanOperationInfo operInfo = null;
	/**	The actual method that backs the JMX operation. */
	protected Method targetMethod = null;
	/**	Indicates if the method will be handled by the asynch request thread pool. */
	protected boolean asynch = false;
	
	/**
	 * Creates a new OperationContainer for the passed managed object and specified method.
	 * @param targetObject The managed object where the attribute resides.
	 * @param operInfo The JMX information on the operation.
	 * @param targetMethod The actual method that backs the JMX operation.
	 * @param async Indicates if the method will be handled by the asynch request thread pool.
	 */
	public OperationContainer(Object targetObject, MBeanOperationInfo operInfo, Method targetMethod, boolean async) {
		super();
		this.targetObject = targetObject;
		this.operInfo = operInfo;
		this.targetMethod = targetMethod;
		this.asynch = async;
	}

	/**
	 * @return the asynch
	 */
	public boolean isAsynch() {
		return asynch;
	}

	/**
	 * @param asynch the asynch to set
	 */
	public void setAsynch(boolean asynch) {
		this.asynch = asynch;
	}

	/**
	 * @return the operInfo
	 */
	public MBeanOperationInfo getOperInfo() {
		return operInfo;
	}

	/**
	 * @param operInfo the operInfo to set
	 */
	public void setOperInfo(MBeanOperationInfo operInfo) {
		this.operInfo = operInfo;
	}

	/**
	 * @return the targetMethod
	 */
	public Method getTargetMethod() {
		return targetMethod;
	}

	/**
	 * @param targetMethod the targetMethod to set
	 */
	public void setTargetMethod(Method targetMethod) {
		this.targetMethod = targetMethod;
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

	
	
}