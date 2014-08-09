package com.heliosapm.wiex.jmx.dynamic.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>Title: JMXManagedObject</p>
 * <p>Description: Annotation to allow fine grained control of how an object's meta data, attributes and operations are exposed through JMX.</p> 
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Organization: Helios Development Group</p>
 * @author Whitehead
 * @version $Revision: 1.1 $
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface JMXManagedObject {
	/**
	 * true if the managed object should have only declared methods exposed as operations and attributes.
	 * @return true if the managed object should have only declared methods exposed as operations and attributes. 
	 */
	boolean declared() default true;
	
	/**
	 * true if the managed object should have only annotated methods exposed as operations and attributes.
	 * @return true if the managed object should have only annotated methods exposed as operations and attributes. 
	 */
	boolean annotated() default false; 
	

}
