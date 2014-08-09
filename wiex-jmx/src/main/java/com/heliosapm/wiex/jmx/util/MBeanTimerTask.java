package com.heliosapm.wiex.jmx.util;

import java.util.TimerTask;

import javax.management.MBeanServer;
import javax.management.ObjectName;


/**
 * <p>Title: MBeanTimerTask</p>
 * <p>Description: A time task that stores an MBean invocation and invokes it when the timer executes.</p> 
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Organization: Helios Development Group</p>
 * @author Whitehead
 * @version $Revision: 1.2 $
 */

public class MBeanTimerTask extends TimerTask {

	  /** A Handle to the MX Agent */
	  protected MBeanServer server = null;
	  /** The object name of the MBean that the callback will go back to */
	  protected ObjectName objectName = null;
	  /** The operation name of the MBean method that the callback will go back to */
	  protected String method = null;
	  /** The parameter value that will be passed to the MBean operation that the callback will go back to */
	  protected Object[] args = null;
	  /** The parameter types that will be passed to the MBean operation that the callback will go back to */
	  protected String[] types = null;


	  /**
	   * Creates a new MBeanTimerTask
	   * @param server The JMX Agent Hosting the target MBean for the callback
	   * @param objectName The target object name for the callback
	   * @param method The name of the method in the callback
	   * @param args The parameter values to be passed to the method in the callback
	   * @param types The parameter types to be passed to the method in the callback
	   */
	  public MBeanTimerTask(MBeanServer server, ObjectName objectName, String method, Object[] args, String[] types) {
	    this.server = server;
	    this.objectName = objectName;
	    this.method = method;
	    this.args = args;
	    this.types = types;
	  }

	  /**
	   * Invokes the callback configured. Supresses any exception thrown.
	   */
	  public void run() {
	    try {
	      server.invoke(objectName, method, args,types );
	    }
	    catch (Exception ex) {
	    }
	  }

	/**
	 * Renders a human readable description of the object.
	 * @return A string rendering of the object.
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
	    StringBuilder buffer = new StringBuilder();
	    buffer.append("<MBeanTimerTask>\n");
	    buffer.append("\t<objectName>").append(objectName).append("</objectName>\n");
	    buffer.append("\t<method>").append(method).append("</method>\n");
	    buffer.append("\t<args>");
	    for(int i = 0; i < args.length; i++) {
	      buffer.append("\t\t<type>").append(types[i]).append("</type>\n");
	      buffer.append("\t\t<arg>").append(args[i].toString()).append("</arg>\n");
	    }
	    buffer.append("\t</args>\n");
	    buffer.append("</MBeanTimerTask>\n");
	    return buffer.toString();
	  }

	}
