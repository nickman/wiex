package com.heliosapm.wiex.tracing.tracing;

/**
 * <p>Title: IntroscopeAgentConnectionListener</p>
 * <p>Description: </p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * @version $LastChangedRevision$
 * <p><code>org.helios.tracing.extended.introscope.IntroscopeAgentConnectionListener</code></p>
 */

public interface IntroscopeAgentConnectionListener {
	/**
	 * Called when the agent connects
	 */
	public void connectionUp();
		
	/**
	 * Called when the agent disconnects
	 */
	public void connectionDown();

}