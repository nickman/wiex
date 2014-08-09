package com.heliosapm.wiex.tracing.tracing.user;

/**
 * <p>Title: ITrace</p>
 * <p>Description: Defines the interface of User Id Locator concrete implementations. 
 * The classes are responsible for identifying the user Id associated with the thread.</p>
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Organization: Helios Development Group</p>
 * @author Whitehead
 * @version $Revision: 1.1 $
 */


public interface UserIdLocator {
	
	/**
	 * Returns the userId associated with the current thread.
	 * @return The user Id.
	 */
	public String getUserId();

}
