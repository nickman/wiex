/**
 * 
 */
package com.heliosapm.wiex.server.tracing.collectors.ajax;

import java.io.IOException;

/**
 * @author WhiteheN
 *
 */
public interface FlushableCloseable {
	public void close() throws IOException;
	public void flush() throws IOException;
}
