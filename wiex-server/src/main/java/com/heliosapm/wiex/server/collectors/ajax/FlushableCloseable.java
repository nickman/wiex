/**
 * 
 */
package com.heliosapm.wiex.server.collectors.ajax;

import java.io.IOException;

/**
 * @author WhiteheN
 *
 */
public interface FlushableCloseable {
	public void close() throws IOException;
	public void flush() throws IOException;
}
