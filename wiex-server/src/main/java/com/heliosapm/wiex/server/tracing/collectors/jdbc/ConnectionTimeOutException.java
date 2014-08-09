/**
 * 
 */
package com.heliosapm.wiex.server.tracing.collectors.jdbc;

/**
 * <p>Title: ConnectionTimeOutException</p>
 * <p>Description: </p> 
 * <p>Copyright: Copyright (c) 2007</p>
 * <p>Company: Helios Development Group</p>
 * @author Whitehead
 * @version $Revision: 1.1 $
 */

public class ConnectionTimeOutException extends RuntimeException {
	/**	 */
	private static final long serialVersionUID = 6920589290420549020L;
	
	/**	The thread issuing the JDBC Connection */
	protected Thread connectingThread = null;
	
	/**
	 * The thread that is issuing the JDBC connection request.
	 * @param connectingThread
	 */
	public ConnectionTimeOutException(String message, Thread connectingThread) {
		super(message);
		this.connectingThread = connectingThread;
		
	}
	/**
	 * @return the connectingThread
	 */
	public Thread getConnectingThread() {
		return connectingThread;
	}

 
}
