/**
 * 
 */
package com.heliosapm.wiex.server.server.utils;

import java.beans.Expression;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.lang.reflect.Method;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

/**
 * <p>Title: JMXUtils</p>
 * <p>Description: Some JMX and Reflection MetaData Helper Methods</p> 
 * <p>Copyright: Copyright (c) 2007</p>
 * <p>Company: Helios Development Group</p>
 * @author Whitehead
 * @version $Revision$
 */

public class JMXUtils {
	/**
	 * Determines the type of the named parameter for the passed type.
	 * @param targetType The target type to invoke against.
	 * @param name The attribute name.
	 * @return The class of the parameter type
	 * @throws Exception 
	 */
	public static Class getAttributeType(Class targetType, String name) throws Exception {		
		String methodName = "set" + name;
		for(Method method: targetType.getMethods()) {
			if(method.getName().equals(methodName) && method.getParameterTypes().length==1) {
				return method.getParameterTypes()[0];
			}
		}
		throw new Exception("Type Could Not Be Determined for [" + name + "] in Class [" + targetType.getName() + "]");
	}
	
	
	/**
	 * Sets an attribute on the passed object instance.
	 * @param name The name of the attribute.
	 * @param value The string value.
	 * @param targetType The type of the attribute to set.
	 * @param target The object instance to set on.
	 * @throws Exception 
	 */
	public static void setAttribute(String name, String value, Class targetType, Object target) throws Exception {
		PropertyEditor propertyEditor = PropertyEditorManager.findEditor(targetType);
		if(propertyEditor==null) throw new RuntimeException("No PropertyEditor Found for type [" + targetType.getName() + "] in object of type + [" + target.getClass().getName() + "]" );
		propertyEditor.setAsText(value);
		Object typedValue = propertyEditor.getValue();
		Expression expression = new Expression(target, "set" + name, new Object[]{typedValue});
		expression.execute();
	}
	
	/**
	 * No checked exception ObjectName creator.
	 * @param name The string to build an object name from.
	 * @return A new object name.
	 */
	public static ObjectName safeObjectName(String name) {
		try {
			return new ObjectName(name);
		} catch (Exception e) {
			throw new RuntimeException("Failed to create ObjectName from [" + name + "]", e);
		} 
	}
}
