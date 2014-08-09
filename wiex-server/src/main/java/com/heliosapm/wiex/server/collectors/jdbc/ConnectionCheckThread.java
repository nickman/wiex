/**
 * 
 */
package com.heliosapm.wiex.server.collectors.jdbc;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * <p>Title: ConnectionCheckThread</p>
 * <p>Description: A thread spwaned to check on the status of a connection request through JDBC.</p> 
 * <p>Copyright: Copyright (c) 2007</p>
 * <p>Company: Helios Development Group</p>
 * @author Whitehead
 * @version $Revision: 1.1 $
 */

public class ConnectionCheckThread extends Thread {

	protected long timeOut = 0L;
	protected AtomicBoolean complete = new AtomicBoolean(false);
	protected AtomicBoolean sleeping = new AtomicBoolean(false);
	protected Thread connectingThread = null;
	protected static AtomicLong serial = new AtomicLong(0L);
	protected boolean interruptConnector = false;
	
	
	
	/**
	 * Creates a new ConnectionCheckThread that checks the status of the connection request after the specified timeout period.
	 * If the connection is not made, an exception will be propagated by the passed UncaughtExceptionHandler.
	 * @param timeOut The timeout period.
	 * @param exceptionHandler The threads exception handler.
	 * @param connectingThread The thread issuing the JDBC connect.
	 * @param interruptConnector If true, the connecting thread will be interrupted on timeout.
	 */
	public ConnectionCheckThread(long timeOut, Thread.UncaughtExceptionHandler exceptionHandler, Thread connectingThread, boolean interruptConnector) {
		this.timeOut = timeOut;
		long mySerial = serial.incrementAndGet();
		this.setName("JDBC Connection Check Thread. TimeOut:" + timeOut + " Version " + mySerial);
		this.setDaemon(true);		
		this.setUncaughtExceptionHandler(exceptionHandler);
		this.connectingThread = connectingThread;
		this.interruptConnector =  interruptConnector;
	}
	
	/**
	 * Sleeps for the configured timeout and then checks the status of the connection request.
	 * If the connection has failed, and exception is thrown.
	 * @see java.lang.Thread#run()
	 */
	public void run() {
		try {
			sleeping.set(true);
			Thread.sleep(timeOut);
			sleeping.set(false);
			if(!complete.get()) {
				if(interruptConnector) {
					connectingThread.interrupt();
				}
				throw new ConnectionTimeOutException("Failed to acquire connection before timeout expiration of " + timeOut, connectingThread);
			}
		} catch (InterruptedException e) {
			if(!complete.get()) {
				throw new ConnectionTimeOutException("Failed to acquire connection before timeout expiration of " + timeOut, connectingThread);
			} 
		} finally {
			sleeping.set(false);
		}
	}
	
	/**
	 * If the thread is still sleeping, it is interrupted.
	 * Otherwise, does nothing.
	 * @see java.lang.Thread#interrupt()
	 */
	@Override
	public void interrupt() {
		if(sleeping.get()) {
			super.interrupt();
		}
	}

	/**
	 * @return the complete
	 */
	public boolean isComplete() {
		return complete.get();
	}

	/**
	 * @param complete the complete to set
	 */
	public void setComplete(boolean complete) {
		this.complete.set(complete);
	}

	/**
	 * @return the sleeping
	 */
	public boolean isSleeping() {
		return sleeping.get();
	}


}
