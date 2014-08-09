/**
 * 
 */
package com.heliosapm.wiex.server.tracing.collectors;

import java.lang.reflect.Method;

import org.apache.log4j.Logger;

/**
 * <p>Title: SimpleTimerTask</p>
 * <p>Description: </p> 
 * <p>Copyright: Copyright (c) 2007</p>
 * <p>Company: Helios Development Group</p>
 * @author Whitehead
 * @version $Revision: 1.2 $
 */

public class SimpleTimerTask implements Runnable {
	protected Method method = null;
	protected Object object = null;
	protected Object[] arguments = null;
	protected Logger log = null;
	protected boolean stop = false;
	
	/**
	 * @param method
	 * @param object
	 * @param arguments
	 */
	public SimpleTimerTask(Method method, Object object, Object[] arguments) {
		this.method = method;
		this.object = object;
		this.arguments = arguments;
		log = Logger.getLogger(object.getClass());
	}
	
	public void stop() {
		stop = true;
	}


	/**
	 * Runs the configured method with the supplied parameters against the configured object. 
	 * If the stop boolean is set to true, a RuntimeException is thrown to signal the scheduler to cancel this task. 
	 */
	public void run() {
		if(stop) throw new RuntimeException("This task has been stopped");
		try {
			method.invoke(object, arguments);
		} catch (Throwable e) {
			log.error("Failed to invoke " + method.toString() + " on instance of " + object.getClass(), e);
		}

	}

}
