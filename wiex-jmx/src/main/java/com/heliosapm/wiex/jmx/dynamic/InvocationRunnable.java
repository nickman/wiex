package com.heliosapm.wiex.jmx.dynamic;

/**
 * <p>Title: InvocationRunnable</p>
 * <p>Description: Container class to store invocations that will be passed off to a thread pool for execution.</p> 
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Organization: Helios Development Group</p>
 * @author Whitehead
 * @version $Revision: 1.1 $
 */

public class InvocationRunnable implements Runnable {
	/**	The mbean that the invocation will be executed against. */
	protected ManagedObjectDynamicMBean target = null;
	/**	The operation name that will be invoked. */
	String actionName = null;
	/**	The invocation parameters */
	Object[] params = null;
	/** The invocation target operation signature */
	String[] signature = null;
	/**	A unique key identifying the invocation */
	long invocationIdentifier = 0L;
	
	/**
	 * Builds an invocation for queueing or serializing.
	 * @param target The mbean that the invocation will be executed against.
	 * @param actionName The operation name that will be invoked.
	 * @param params he invocation parameters
	 * @param signature The invocation target operation signature
	 * @param invocationIdentifier A unique key identifying the invocation
	 */
	protected InvocationRunnable(ManagedObjectDynamicMBean target, String actionName, Object[] params, String[] signature, long invocationIdentifier) {
		this.target = target;
		this.actionName = actionName;
		this.params = params;
		this.signature = signature;
		this.invocationIdentifier = invocationIdentifier;
	}
	
	/**
	 * Implementation of the Runnable interface. Will be called by the thread pool thread to invoke the original operation invocation.
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		// TODO Need exception handling
		try {
			Object obj = target.internalInvoke(actionName, params, signature);
			target.notifyAsynchRequestComplete(new StringBuilder(actionName).append(renderParams(params)).toString(), obj, invocationIdentifier);
		} catch (Exception e) {
			
		} 
	}
	
	/**
	 * Converts an array of object parameters into a readable string.
	 * @param par An array of object parameters.
	 * @return A readable string.
	 */
	protected String renderParams(Object[] par) {
		StringBuilder sb = new StringBuilder("(");
		for(Object o: par) {
			sb.append(o.toString());
			sb.append(",");
		}
		if(sb.length()>1) sb.deleteCharAt(sb.length()-1);
		sb.append(")");
		return sb.toString();
	}
}

