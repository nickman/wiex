/**
 * 
 */
package com.heliosapm.wiex.server.tracing.collectors.jdbc;

/**
 * <p>Title: JDBCConnectionFactoryException</p>
 * <p>Description: Exception for JDBCConnectionFactory related errors.</p> 
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Company: Helios Development Group</p>
 * @author Whitehead
 * @version $Revision: 1.1 $
 */

public class JDBCConnectionFactoryException extends Exception {

	/**	 */
	private static final long serialVersionUID = 315342828694988505L;

	/**
	 * 
	 */
	public JDBCConnectionFactoryException() {
		
	}

	/**
	 * @param message
	 */
	public JDBCConnectionFactoryException(String message) {
		super(message);
		
	}

	/**
	 * @param cause
	 */
	public JDBCConnectionFactoryException(Throwable cause) {
		super(cause);
		
	}

	/**
	 * @param message
	 * @param cause
	 */
	public JDBCConnectionFactoryException(String message, Throwable cause) {
		super(message, cause);
	}

}
