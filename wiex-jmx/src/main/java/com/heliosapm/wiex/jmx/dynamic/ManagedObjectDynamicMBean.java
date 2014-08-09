package com.heliosapm.wiex.jmx.dynamic;



import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.management.Attribute;
import javax.management.AttributeChangeNotification;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.DynamicMBean;
import javax.management.IntrospectionException;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.Notification;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import com.heliosapm.wiex.jmx.dynamic.annotation.JMXAttribute;
import com.heliosapm.wiex.jmx.dynamic.annotation.JMXManagedObject;
import com.heliosapm.wiex.jmx.dynamic.annotation.JMXOperation;
import com.heliosapm.wiex.jmx.dynamic.annotation.JMXOperationParameter;

/**
 * <p>Title: ManagedObjectDynamicMBean</p>
 * <p>Description: DynamicMBean implementation that dynamically exposes operations and attributes of passed objects.</p> 
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Organization: Helios Development Group</p>
 * @author Whitehead
 * @version $Revision: 1.6 $
 * @todo Add notification broadcasting for attribute changes.
 */

/**
 * <p>Title: ManagedObjectDynamicMBean</p>
 * <p>Description: </p> 
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Organization: Helios Development Group</p>
 * @author Whitehead
 * @version $Revision: 1.6 $
 */
public class ManagedObjectDynamicMBean extends javax.management.NotificationBroadcasterSupport implements DynamicMBean, MBeanRegistration {
	
 
	/**	A map of the current MBean's Attributes and target managed objects. */
	protected Map<String, AttributeContainer> attributes = new ConcurrentHashMap<String, AttributeContainer>();
	/**	A map of the current MBean's Operations and target managed objects. */
	protected Map<String, OperationContainer> operations = new ConcurrentHashMap<String, OperationContainer>();	
	/**	A map of the current MBean's Notifications and target managed objects. */
	protected List<MBeanNotificationInfo> notifications = Collections.synchronizedList(new ArrayList<MBeanNotificationInfo>());		
	/**	A blockig queue to hold asynch requests to be fed into the thread pool. */
	protected ArrayBlockingQueue<Runnable> asyncRequests = null;
	/**	ThreadPool to execute asynch requests */
	protected ThreadPoolExecutor threadPool = null;
	/**	The asynch request queue maximum size */
	protected int queueSize = 1000;
	/**	The default core pool size for the thread pool */
	protected int corePoolSize = 2;
	/**	The default maximum pool size for the thread pool */
	protected int maximumPoolSize = 5;
	/**	The default thread keep alive time for the thread pool */
	protected long keepAliveTime = 100;
	/**	The default unit for the default keep alive time */
	protected TimeUnit timeUnit = TimeUnit.SECONDS;
	/**	A reference to the MBeanServer that this MBean is registered in */
	protected MBeanServer server = null;
	/**	The MBean's object name */
	protected ObjectName objectName = null;
	/**	The MBeanServer's agent Id */
	protected String agentId = "";
	/**	The dynamic MBean MBeanInfo */
	protected MBeanInfo mbeanInfo = null;
	/**	Empty object array */
	protected final static  Object[] NO_ARGS = new Object[]{};
	
	/**
	 * Creates a new Managed Object Dynamic MBean with no asynch handler.
	 * @param objects An array of objects to be managed.
	 */
	public ManagedObjectDynamicMBean(Object...objects) {
		bootStrap(objects);
	}
	
	/**
	 * Creates a new Managed Object Dynamic MBean.
	 * @param defaultAsync If true, a default asynch handler is created.
	 * @param objects An array of objects to be managed.
	 */
	public ManagedObjectDynamicMBean(boolean defaultAsync, Object...objects) {
		bootStrap(objects);
		if(defaultAsync)initThreadPool();
	}
	
	/**
	 * Creates a new Managed Object Dynamic MBean with a custom configured asynch handler.
	 * @param queueSize The queue size of the asynch request blocking queue.
	 * @param corePoolSize The core thread count in the asynch thread pool.
	 * @param maximumPoolSize The maximum thread count in the asynch thread pool.
	 * @param keepAliveTime The idle thread keep alive time.
	 * @param timeUnit The idle thread keep alive time unit.
	 * @param objects An array of objects to be managed.
	 */
	public ManagedObjectDynamicMBean(int queueSize, int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit timeUnit, Object...objects) {
		this.queueSize = queueSize;
		this.corePoolSize = corePoolSize;
		this.maximumPoolSize = maximumPoolSize;
		this.keepAliveTime = keepAliveTime;
		this.timeUnit = timeUnit;	
		bootStrap();
		initThreadPool();
	}
	
	
	/**
	 * Callback from a completed asynch request.
	 * @param message A string representation of the invocation.
	 * @param returnObject The object returned from the asynch invocation.
	 * @param invocationIdentifier A unique id assigned to the asynch invocation.
	 */
	protected void notifyAsynchRequestComplete(String message, Object returnObject, long invocationIdentifier) {		
		Notification notification = new Notification("jmx.asynchrequest.complete", this, invocationIdentifier, message);
		notification.setUserData(returnObject);
		sendNotification(notification);
	}
	
		
	/**
	 * Boot straps the managed objects at mbean construction time.
	 * @param objects The array of objects to be managed.
	 */
	protected void bootStrap(Object...objects) {
		for(Object o: objects) {
			reflectObject(o);
		}
		reflectObject(new DynamicMBeanCoreFunctions(this));		
		notifications.add(new MBeanNotificationInfo(null, AttributeChangeNotification.class.getName(), "Attribute Change Notification"));
		updateMBeanInfo();			
	}
	
	/**
	 * Initializes the asynch request thread pool and request queue.
	 * Registers the DynamicMBeanAsyncInstrumentation to expose async instrumentation.
	 */
	protected void initThreadPool() {
		asyncRequests = new ArrayBlockingQueue<Runnable>(queueSize, true);
		threadPool = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, timeUnit, asyncRequests);
		reflectObject(new DynamicMBeanAsyncInstrumentation(this));
	}
	
	/**
	 * Creates a new MBeanInfo from the configured collections of attributes, operations and notifications.
	 */
	protected void updateMBeanInfo() {
		mbeanInfo = new MBeanInfo(
				this.getClass().getName(),
				"ManagedObjectDynamicMBean",
				getAttrInfos(),
				new MBeanConstructorInfo[]{},
				getOperInfos(),
				notifications.toArray(new MBeanNotificationInfo[notifications.size()])
		);		
	}
	
	/**
	 * Reflects a passed managed objects and generates the appropriate MBean attributes and operations.
	 * The reflection looks for recognized JMX annotations and applies where applicable.
	 * Minimum reflection will create javabean style attributes and method named operations.
	 * @param object
	 */
	protected void reflectObject(Object object) {
		boolean annotatedOnly = false;
		Method[] methods = null;
		boolean async = false;
		if(object.getClass().isAnnotationPresent(JMXManagedObject.class)) {
			annotatedOnly = object.getClass().getAnnotation(JMXManagedObject.class).annotated();
			if(object.getClass().getAnnotation(JMXManagedObject.class).declared()) {
				methods = object.getClass().getDeclaredMethods();				
			} else {
				methods = object.getClass().getMethods();
			}
		} else {
			methods = object.getClass().getMethods();
		}
		
		MBeanAttributeInfo attrInfo = null;
		MBeanOperationInfo operInfo = null;
		String attrName = null;
		String operName = null;
		String descr = null;		
		int impact = 0;
		JMXAttribute jmxAttr = null;
		JMXOperation jmxOper = null;
		
		for(Method method: methods) {
			if((method.getName().startsWith("get")|| method.getName().startsWith("is"))  && method.getParameterTypes().length==0) {
				try {					
					Method setterMethod = exists(methods, "set" + generateAttributeName(method), 1);
					if(method.isAnnotationPresent(JMXAttribute.class)) {						
						jmxAttr = method.getAnnotation(JMXAttribute.class);
						
						if(!jmxAttr.expose()) continue;
						if(jmxAttr.introspect()) {
							descr = (String)object.getClass().getMethod(jmxAttr.description(), new Class[]{}).invoke(object, new Object[]{});
							attrName = (String)object.getClass().getMethod(jmxAttr.name(), new Class[]{}).invoke(object, new Object[]{});
						} else {
							descr = jmxAttr.description();						
							attrName = jmxAttr.name();							
						}
						if(attrName.equalsIgnoreCase("")) attrName = generateAttributeName(method);
					} else {
						if(annotatedOnly) continue;
						descr = "MBean Attribute";
						attrName = generateAttributeName(method);
					}
					attrInfo = new MBeanAttributeInfo(attrName, descr, method, setterMethod);					
					attributes.put(attrName, new AttributeContainer(object, attrInfo, method, setterMethod));
				} catch (Exception e) {
				}
			} else {
				try {
					if(!method.getName().startsWith("set") || method.getParameterTypes().length > 1) {
						if(method.isAnnotationPresent(JMXOperation.class)) {
							jmxOper = method.getAnnotation(JMXOperation.class);
							if(!jmxOper.expose()) continue;
							if(!jmxOper.introspect()) {
								descr = jmxOper.description();
								operName = jmxOper.name();								
							} else {
								descr = (String)object.getClass().getMethod(jmxOper.description(), new Class[]{}).invoke(object, new Object[]{});
								operName = (String)object.getClass().getMethod(jmxOper.name(), new Class[]{}).invoke(object, new Object[]{});								
							}
							impact = jmxOper.impact();
							async = jmxOper.async();
						} else {
							if(annotatedOnly) continue;
							descr = "MBean Operation";
							operName = method.getName();
							impact = MBeanOperationInfo.UNKNOWN;
						}					
						Annotation[][] opParams = method.getParameterAnnotations();
						MBeanParameterInfo[] paramInfos = new MBeanParameterInfo[method.getParameterTypes().length]; 
						JMXOperationParameter opParamAnnotation = null;
						String paramName = null;
						// hasCode for operName, 
						for(int i = 0; i < method.getParameterTypes().length; i++) {
							opParamAnnotation = getJMXOperationParameter(opParams[i]);
							if(opParamAnnotation==null) {
								paramInfos[i] = new MBeanParameterInfo("Parameter" + i, method.getParameterTypes()[i].getName(), "MBean Operation Parameter");
							} else {
								paramName = opParamAnnotation.name();
								paramInfos[i] = new MBeanParameterInfo(paramName.equalsIgnoreCase("") ? "Parameter" + i : paramName, method.getParameterTypes()[i].getName(), opParamAnnotation.description());
								
							}
						} // -2116838301  
						operInfo = new MBeanOperationInfo(operName, descr, paramInfos, method.getReturnType().getName(), impact);					
						operations.put(hashOperationName(operName, method.getParameterTypes()), new OperationContainer(object, operInfo, method, async));
					}
				} catch (Exception e) {
					
				}
			}			
		}
	}
	
	/**
	 * Extracts a located JMXOperationParameter from an array of annotations.
	 * @param annotations An array of annotations.
	 * @return A found JMXOperationParameter or null if one is not found.
	 */
	protected JMXOperationParameter getJMXOperationParameter(Annotation[] annotations) {
		for(Annotation a: annotations) {
			if(a.annotationType().equals(JMXOperationParameter.class)) {
				return (JMXOperationParameter)a;
			} 
		}
		return null;
	}
	
	/**
	 * Parses out an attribute name from a method name.
	 * @param m The method to parse the attribute name from.
	 * @return The attribute name.
	 */
	protected String generateAttributeName(Method m) {
		if(m.getName().startsWith("get")) {
			return m.getName().substring(3);  
		} else {
			return m.getName().substring(2);
		}						
		
	}
	
	/**
	 * Looks for a method with the specified and and specified number of parameters from an array of methods.
	 * @param methods An array of methods to look in for a match.
	 * @param methodName The method name to look for.
	 * @param paramCount The number of parameters the method should have.
	 * @return The matching method or null if one is not found.
	 */
	protected Method exists(Method[] methods, String methodName, int paramCount) {
		for(Method method: methods) {
			if(method.getName().equals(methodName) && method.getParameterTypes().length==paramCount) return method;
		}		
		return null;
	}

	/**
	 * Obtain the value of a specific attribute of the Dynamic MBean.
	 * @param attribute The name of the attribute to be retrieved
	 * @return The value of the attribute retrieved.
	 * @throws AttributeNotFoundException
	 * @throws MBeanException
	 * @throws ReflectionException
	 * @see javax.management.DynamicMBean#getAttribute(java.lang.String)
	 */
	public Object getAttribute(String attribute)
			throws AttributeNotFoundException, MBeanException,
			ReflectionException {
		AttributeContainer ac = attributes.get(attribute);
		if(ac==null) throw new AttributeNotFoundException("Attribute " + attribute + " Not Found");
		try {
			if(ac.getTargetGetterMethod().toString().contains(" static ")) {
				return ac.getTargetGetterMethod().invoke(null, NO_ARGS);
			} else {
				return ac.getTargetGetterMethod().invoke(ac.getTargetObject(), NO_ARGS);
			}
			
		} catch (Exception e) {
			throw new ReflectionException(e, "Failed to Invoke " + ac.getTargetGetterMethod().getName());
		} 
		
	}

	/**
	 * Get the values of several attributes of the Dynamic MBean.
	 * @param attributes A list of the attributes to be retrieved.
	 * @return The list of attributes retrieved.
	 * @see javax.management.DynamicMBean#getAttributes(java.lang.String[])
	 */
	public AttributeList getAttributes(String[] attributes) {
		AttributeList list = new AttributeList();
		
		for(String s: this.attributes.keySet()) {
			try {
				Object o = getAttribute(s);
				list.add(new Attribute(s, o));
			} catch (Exception e) {}
		}		
		return list;
		
	}

	/**
	 * Provides the exposed attributes and actions of the Dynamic MBean using an MBeanInfo object.
	 * @return An instance of MBeanInfo allowing all attributes and actions exposed by this Dynamic MBean to be retrieved.
	 * @see javax.management.DynamicMBean#getMBeanInfo()
	 */
	public MBeanInfo getMBeanInfo() {		
		return mbeanInfo;
	}
	
	/**
	 * Extracts an array of <code>MBeanAttributeInfo</code>s from the attributes collection.
	 * @return An array of <code>MBeanAttributeInfo</code>s representing the attributes exposed by this mbean.
	 */
	protected MBeanAttributeInfo[] getAttrInfos() {
		MBeanAttributeInfo[] infos = new MBeanAttributeInfo[attributes.size()];
		int x = 0;
		for(AttributeContainer c: attributes.values()) {
			infos[x] = c.getAttributeInfo();
			x++;
		}
		return infos;
	}
	
	/**
	 * Extracts an array of <code>MBeanOperationInfo</code>s from the operations collection.
	 * @return An array of <code>MBeanOperationInfo</code>s representing the operations exposed by this mbean.
	 */
	protected MBeanOperationInfo[] getOperInfos() {
		MBeanOperationInfo[] infos = new MBeanOperationInfo[operations.size()];
		int x = 0;
		for(OperationContainer c: operations.values()) {
			infos[x] = c.getOperInfo();
			x++;
		}
		return infos;
		
	}
	

	/**
	 * Invokes an exposes operation.
	 * If the managed object declared a JMXOperation annotation that marks the operation asynchronous, 
	 * the invocation will be passed to the asynch request queue for execution by the thread pool.
	 * Otherwise, the invocation will be executed in the current thread.
	 * Both asynch and synch invocations are passed to <code>internalInvoke</code>. 
	 * @param actionName The name of the operation to be invoked.
	 * @param params An array containing the parameters to be set when the action is invoked.
	 * @param signature An array containing the signature of the action. The class objects will be loaded through the same class loader as the one used for loading the MBean on which the action is invoked.
	 * @return The object returned by the action, which represents the result of invoking the action on the MBean specified or null if the call is asynchronous.
	 * @throws MBeanException
	 * @throws ReflectionException
	 * @see javax.management.DynamicMBean#invoke(java.lang.String, java.lang.Object[], java.lang.String[])
	 */
	public Object invoke(String actionName, Object[] params, String[] signature)
			throws MBeanException, ReflectionException {
		OperationContainer op = operations.get(hashOperationName(actionName, signature));
		if(op==null) throw new OperationNotFoundException(new Exception(), actionName);
		if(op.isAsynch() && threadPool != null) {
			long resultHandle = new StringBuilder(actionName).append(objectName.toString()).append(Thread.currentThread().getName()).append(agentId).append(System.nanoTime()).hashCode();
			threadPool.submit(new InvocationRunnable(this, actionName, params, signature, resultHandle));
			return null;
		} else {
			return internalInvoke(actionName, params, signature);
		}
	}
	
	/**
	 * Generates a unique hash code for an operation.
	 * @param actionName
	 * @param signature
	 * @return
	 */
	protected static String hashOperationName(String actionName, String[] signature) {
		StringBuilder buff = new StringBuilder(actionName);
		for(String s: signature) {
			buff.append(s);
		}
		return "" + buff.toString().hashCode();
	}
	
	/**
	 * Generates a unique hash code for an operation.
	 * @param actionName
	 * @param signature
	 * @return
	 */
	protected static String hashOperationName(String actionName, Class[] signature) {
		String[] classes = new String[signature.length];
		for(int i = 0; i < signature.length; i++) {			
			classes[i] = signature[i].getName();
		}
		return hashOperationName(actionName, classes);		
	}
	
	/**
	 * Internal invoke.
	 * @param actionName
	 * @param params
	 * @param signature
	 * @return The applicable return object from the invoked method.
	 * @throws MBeanException
	 * @throws ReflectionException
	 */
	protected Object internalInvoke(String actionName, Object[] params, String[] signature) throws MBeanException, ReflectionException {
		Object ret = null;
		OperationContainer op = operations.get(hashOperationName(actionName, signature));
		try {
			if(op.getTargetMethod().toString().contains(" static ")) {
				ret = op.getTargetMethod().invoke(null, params);
			} else {
				ret = op.getTargetMethod().invoke(op.getTargetObject(), params);
			}
			return ret;
		} catch (Exception e) {
			throw new ReflectionException(e, "Exception Invoking Operation " + op.getTargetMethod().getName());
		} 				
	}

	/**
	 * Set the value of a specific attribute of the Dynamic MBean.
	 * @param attribute Set the value of a specific attribute of the Dynamic MBean.
	 * @throws AttributeNotFoundException
	 * @throws InvalidAttributeValueException
	 * @throws MBeanException
	 * @throws ReflectionException
	 * @see javax.management.DynamicMBean#setAttribute(javax.management.Attribute)
	 */
	public void setAttribute(Attribute attribute)
			throws AttributeNotFoundException, InvalidAttributeValueException,
			MBeanException, ReflectionException {
		AttributeContainer ac = attributes.get(attribute.getName());
		if(ac==null) throw new AttributeNotFoundException("Attribute Not Found:" + attribute.getName());
		try {
			if(ac.getTargetSetterMethod().toString().contains(" static ")) {
				ac.getTargetSetterMethod().invoke(null, new Object[]{attribute.getValue()});
			} else {
				ac.getTargetSetterMethod().invoke(ac.getTargetObject(), new Object[]{attribute.getValue()});
			}
			
		} catch (Exception e) {
			throw new ReflectionException(e, "Exception Setting Attribute " + attribute.getName());
		}
	}

	/**
	 * Sets the values of several attributes of the Dynamic MBean.
	 * @param attributes A list of attributes: The identification of the attributes to be set and the values they are to be set to.
	 * @return The list of attributes that were set, with their new values.
	 * @see javax.management.DynamicMBean#setAttributes(javax.management.AttributeList)
	 */
	public AttributeList setAttributes(AttributeList attributes) {
		AttributeList list = new AttributeList();
		for(int i = 0; i < attributes.size(); i++) {			
			Attribute attr = (Attribute)attributes.get(i);
			try {
				setAttribute(attr);
				list.add(attr);
			} catch (Exception e) {}
			
		}
		return list;
	}


	/**
	 * Callback after having been unregistered in the MBean server.
	 * @see javax.management.MBeanRegistration#postDeregister()
	 */
	public void postDeregister() {
		
	}


	/**
	 * Allows the MBean to perform any operations needed after having been registered in the MBean server or after the registration has failed.
	 * @param registrationDone Indicates whether or not the MBean has been successfully registered in the MBean server. The value false means that the registration phase has failed.
	 * @see javax.management.MBeanRegistration#postRegister(java.lang.Boolean)
	 */
	public void postRegister(Boolean registrationDone) {
		
	}


	/**
	 * Allows the MBean to perform any operations it needs before being unregistered by the MBean server.
	 * @throws Exception
	 * @see javax.management.MBeanRegistration#preDeregister()
	 */
	public void preDeregister() throws Exception {
		
	}


	/**
	 * Callback from the MBean server before registration.
	 * Keeps references to the object name and mbean server.
	 * Creates the initial notifications.
	 * @param server A reference to the MBeanServer.
	 * @param name The MBean's ObjectName.
	 * @return The final ObjectName to register the MBean under.
	 * @throws Exception
	 * @see javax.management.MBeanRegistration#preRegister(javax.management.MBeanServer, javax.management.ObjectName)
	 */
	public ObjectName preRegister(MBeanServer server, ObjectName name) throws Exception {
		this.server = server;
		this.objectName = name;
		notifications.add(new MBeanNotificationInfo(new String[]{AttributeChangeNotification.ATTRIBUTE_CHANGE}, AttributeChangeNotification.class.getName(), "Attribute Change Notification"));
		notifications.add(new MBeanNotificationInfo(new String[]{"jmx.asynchrequest.complete"}, Notification.class.getName(), "Asynch Request Complete Notification"));
		updateMBeanInfo();
		try {
			agentId = (String)server.getAttribute(new ObjectName("JMImplementation:type=MBeanServerDelegate"), "MBeanServerId");
		} catch (Exception e) {
			// TODO: handle exception
		}
		return objectName;
	}

}





