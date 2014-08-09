
package com.heliosapm.wiex.tracing.tracing.jmx;

import javax.management.MBeanAttributeInfo;

/**
 * <p>Title: TracingMetric</p>
 * <p>Description: Container for dynamically defined attributes.</p> 
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Organization: Helios Development Group</p>
 * @author Whitehead
 * @version $Revision: 1.1 $
 */
public class TracingMetric {
	
	/** This metric's JMX attribute descriptor. */
	protected MBeanAttributeInfo mBeanAttributeInfo = null;
	/** The value of the attribute */
	protected Object value = null;
	/** The tracing type of the attribute */
	protected TracingMetricType tracingMetricType = null;
	

}
