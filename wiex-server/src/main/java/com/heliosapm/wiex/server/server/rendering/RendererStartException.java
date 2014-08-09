/**
 * 
 */
package com.heliosapm.wiex.server.server.rendering;

/**
 * <p>Title: RendererStartException</p>
 * <p>Description: Exception for issuing start exceptions on renderers.</p> 
 * <p>Copyright: Copyright (c) 2007</p>
 * <p>Company: Helios Development Group</p>
 * @author Whitehead
 * @version $Revision$
 */

public class RendererStartException extends Exception {

	/**	 */
	private static final long serialVersionUID = -6544532003726193491L;

	/**
	 * 
	 */
	public RendererStartException() {

	}

	/**
	 * @param message
	 */
	public RendererStartException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public RendererStartException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public RendererStartException(String message, Throwable cause) {
		super(message, cause);
	}

}
