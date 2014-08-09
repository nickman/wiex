package com.heliosapm.wiex.jmx.dynamic.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;


/**
 * <p>Title: JMXOperationParameter</p>
 * <p>Description: Annotation to allow fine grained control of how operation parameters are exposed through JMX.</p> 
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Organization: Helios Development Group</p>
 * @author Whitehead
 * @version $Revision: 1.1 $
 */

@Retention(RetentionPolicy.RUNTIME)
@Documented
@Target(ElementType.PARAMETER)
public @interface JMXOperationParameter {
	
	/**
	 * The parameter name.
	 * @return The parameter name.
	 */
	String name() default "";
	
	
	/**
	 * The parameter description.
	 * @return The parameter description.
	 */
	String description() default "MBean Operation Parameter";
	
	
	
}
