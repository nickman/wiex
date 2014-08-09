
package com.heliosapm.wiex.tracing.tracing.jmx;

/**
 * <p>Title: TracingMetricType</p>
 * <p>Description: Container for dynamically defined attributes.</p> 
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Organization: Helios Development Group</p>
 * @author Whitehead
 * @version $Revision: 1.1 $
 */
public enum TracingMetricType {
	
	/**
	 * A long average metric. 
	 */
	LONG, 
	
	/**
	 * An int average metric.
	 */
	INT, 
	/**
	 * 
	 */
	LONG_COUNTER, 
	/**
	 * 
	 */
	INT_COUNTER, 
	/**
	 * 
	 */
	STRING, 
	/**
	 * 
	 */
	DATETIME
}
