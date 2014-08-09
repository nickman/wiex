/**
 * 
 */
package com.heliosapm.wiex.server.collectors.jmx;

/**
 * <p>Title: MBeanServerConnectionFactoryException</p>
 * <p>Description: MBeanServerConnection acquisition exception class.</p> 
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Company: Helios Development Group</p>
 * @author Whitehead
 * @version $Revision: 1.1 $
 */

public class MBeanServerConnectionFactoryException extends Exception {
	/**	 */
	private static final long serialVersionUID = -7737969813303173144L;

	/**
	 * 
	 */
	public MBeanServerConnectionFactoryException() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 */
	public MBeanServerConnectionFactoryException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param cause
	 */
	public MBeanServerConnectionFactoryException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 * @param cause
	 */
	public MBeanServerConnectionFactoryException(String message,
			Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

}
