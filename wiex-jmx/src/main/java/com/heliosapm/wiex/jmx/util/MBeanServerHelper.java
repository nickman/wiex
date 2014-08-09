package com.heliosapm.wiex.jmx.util;

import java.util.Iterator;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;





/**
 * <p>Title: MBeanServerHelper</p>
 * <p>Description: A set of static functions to streamline interacting with an MBeanServer.</p> 
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Organization: Helios Development Group</p>
 * @author Whitehead
 * @version $Revision: 1.2 $
 */

public class MBeanServerHelper {

    /**
     * Acquires the specified MBeanServer from the MBeanServerFactory. 
	 * @param domain The default domain of the MBeanServer to return.
	 * @return An MBeanServer.
	 * @throws Exception Thrown if an MBeanServer cannot be located for the requested domain.
	 */
	public static MBeanServer getMBeanServer(String domain) throws Exception {
	    for (Iterator i = MBeanServerFactory.findMBeanServer(null).iterator(); i.hasNext(); ) {
	      MBeanServer server = (MBeanServer) i.next();
	      if (server.getDefaultDomain().equals(domain)) {
	        return server;
	      }
	    }
	    throw new Exception("No MBeanServer found for domain " + domain);
	  }
	
	/**
	 * Returns the JVMs default MBeanServer.
	 * @return A handle to the default MBeanServer.
	 */
	public static MBeanServer getMBeanServer() {
		return (MBeanServer) MBeanServerFactory.findMBeanServer(null).get(0);
	}
	  
	  


	  /**
	   * Acquires an attribute value from the default MBeanServer reissuing any exceptions as runtime only.
	   * @param objectName The string representation of the target MBean object name.
	   * @param attribute The name of the attribute to get.
	   * @throws RuntimeException Thrown if any exception results from the request.
	   * @return The attribute value.
	   */
	  public static Object getAttribute(String objectName, String attribute) throws RuntimeException {
	    try {
	      return getMBeanServer().getAttribute(new ObjectName(objectName), attribute);
	    }
	    catch (Exception ex) {
	      throw new RuntimeException("Exception Acquiring Attribute " + attribute + " from " + objectName, ex);
	    }
	  }
	  
    /**
     * Acquires an attribute value from the MBeanServer with the specified domain reissuing any exceptions as runtime only. 
	 * @param domain The default domain of the target MBeanServer. 
	 * @param objectName he string representation of the target MBean object name.
	 * @param attribute The name of the attribute to get.
	 * @return The attribute value.
	 * @throws RuntimeException Thrown if any exception results from the request.
	 */
	public static Object getAttribute(String domain, String objectName, String attribute) throws RuntimeException {
		  try {
			  return getMBeanServer(domain).getAttribute(new ObjectName(objectName), attribute);
		  }
		  catch (Exception ex) {
			  throw new RuntimeException("Exception Acquiring Attribute " + attribute + " from " + objectName, ex);
		  }
	  }
	  

	  /**
	   * Invokes an operation on the specified MBean in the default MBeanServer. Any excpetions are raised as RuntimeExceptions.
	   * @param objectName The string representation of the object name of the MBean on which the method is to be invoked.
	   * @param opName The name of the operation to be invoked.
	   * @param params An array containing the parameters to be set when the operation is invoked.
	   * @param types An array containing the signature of the operation. The class objects will be loaded using the same class loader as the one used for loading the MBean on which the operation was invoked. 
	   * @throws RuntimeException If any excpetions are thrown in the invocation.
	   * @return Object The object returned by the operation, which represents the result of invoking the operation on the MBean specified.
	   */
	  public Object invoke(String objectName, String opName, Object[] params, String[] types) throws RuntimeException {
	    try {
	      return getMBeanServer().invoke(new ObjectName(objectName), opName, params, types);
	    }
	    catch (Exception ex) {
	      throw new RuntimeException("Exception Invoking Operation " + opName + " on " + objectName, ex);
	    }
	  }
	  
	  /**
	   * Invokes an operation on the specified MBean in the MBeanServer with the default domain specified. Any excpetions are raised as RuntimeExceptions.
	   * @param domain The target MBeanServer default domain.
	   * @param objectName The string representation of the object name of the MBean on which the method is to be invoked.
	   * @param opName The name of the operation to be invoked.
	   * @param params An array containing the parameters to be set when the operation is invoked.
	   * @param types An array containing the signature of the operation. The class objects will be loaded using the same class loader as the one used for loading the MBean on which the operation was invoked. 
	   * @throws RuntimeException If any excpetions are thrown in the invocation.
	   * @return Object The object returned by the operation, which represents the result of invoking the operation on the MBean specified.
	   */
	  public Object invoke(String domain, String objectName, String opName, Object[] params, String[] types) throws RuntimeException {
	    try {
	      return getMBeanServer(domain).invoke(new ObjectName(objectName), opName, params, types);
	    }
	    catch (Exception ex) {
	      throw new RuntimeException("Exception Invoking Operation " + opName + " on " + objectName, ex);
	    }
	  }

	public static MBeanServer getJBossInstance() {
		try {
			return getMBeanServer("jboss");
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}
	  
	
}
