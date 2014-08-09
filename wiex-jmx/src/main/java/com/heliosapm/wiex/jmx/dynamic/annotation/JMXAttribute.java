package com.heliosapm.wiex.jmx.dynamic.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * <p>Title: JMXAttribute</p>
 * <p>Description: Annotation to allow fine grained control of how an object's attributes is exposed through JMX.</p> 
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Organization: Helios Development Group</p>
 * @author Whitehead
 * @version $Revision: 1.2 $
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented

public @interface JMXAttribute {
	
	/**
	 * Should attribute change notificiations be broadcast.
	 * Note that unless the managted object dynamic MBean can determine the "getter" method, 
	 * a change will always be broadcast even if the value has not changed.
	 * @return if true, attribute change notificiations will be broadcast.
	 */
	boolean broadcastChange() default false;
	
	/**
	 * Should the attribute be exposed.
	 * @return if true, the attribute will be exposed.
	 */
	boolean expose() default true;
	
	
	/**
	 * The attribute name.
	 * @return The attribute name.
	 */
	String name() default "";
	
	/**
	 * If introscopect is true, the name and descrption annotations contains getter methods that will expose those values.
	 * @return true if introspect is on.
	 */
	boolean introspect() default false;
	
	
	/**
	 * The attribute description.
	 * @return The attribute description.
	 */
	String description() default "MBean Attribute";
	
	
	

}
