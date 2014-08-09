package com.heliosapm.wiex.jmx.dynamic.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.management.MBeanOperationInfo;

/**
 * <p>Title: JMXOperation</p>
 * <p>Description: Annotation to allow fine grained control of how an object's operations is exposed through JMX.</p> 
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Organization: Helios Development Group</p>
 * @author Whitehead
 * @version $Revision: 1.2 $
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface JMXOperation {

	/**
	 * Should the operation be exposed.
	 * @return if true, the attribute will be exposed.
	 */
	boolean expose() default true;
	
	/**
	 * The operation description.
	 * @return The operation description.
	 */
	String description() default "MBean Operation"; 
	
	/**
	 * The operation name.
	 * @return The operation name.
	 */
	String name() default "";
	
	/**
	 * The MBean Operation Impact.
	 * @return The MBean Operation Impact.
	 */
	int impact() default MBeanOperationInfo.UNKNOWN;
	
	
	/**
	 * If true, the operation will be invoked asynchronously.
	 * @return if true, operation is asynchronous.
	 */
	boolean async() default false;
	
	/**
	 * If introscopect is true, the name and descrption annotations contains getter methods that will expose those values.
	 * @return true if introspect is on.
	 */
	boolean introspect() default false;
	
	

}
