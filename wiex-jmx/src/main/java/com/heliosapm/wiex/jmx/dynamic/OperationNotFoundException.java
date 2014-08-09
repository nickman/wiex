/**
 * 
 */
package com.heliosapm.wiex.jmx.dynamic;

import javax.management.MBeanException;

/**
 * <p>Title: OperationNotFoundException</p>
 * <p>Description: </p> 
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Organization: Helios Development Group</p>
 * @author Whitehead
 * @version $Revision: 1.1 $
 */

public class OperationNotFoundException extends MBeanException {

	/**
	 * @param e
	 */
	public OperationNotFoundException(Exception e) {
		super(e);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param e
	 * @param message
	 */
	public OperationNotFoundException(Exception e, String message) {
		super(e, message);
		// TODO Auto-generated constructor stub
	}

}
