package com.heliosapm.wiex.tracing.tracing.user;

/**
 * <p>Title: NullUserIdLocator</p>
 * <p>Description: A UserIdLocator that returns a blank. 
 * For use when the tracer is expected to not be able to locate a user Id.</p>
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Organization: Helios Development Group</p>
 * @author Whitehead
 * @version $Revision: 1.1 $
 */

public class NullUserIdLocator implements UserIdLocator {

	/* (non-Javadoc)
	 * @see com.heliosapm.wiex.tracing.tracing.user.UserIdLocator#getUserId()
	 */
	public String getUserId() {
		return "Anonymous";
	}

}
