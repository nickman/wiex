package com.heliosapm.wiex.tracing.tracing.jmx.metrics;

import com.heliosapm.wiex.jmx.dynamic.annotation.JMXAttribute;
import com.heliosapm.wiex.jmx.dynamic.annotation.JMXManagedObject;

/**
 * <p>Title: IntCounterMetric</p>
 * <p>Description: JMX Managed Object for Int Counter Metric</p> 
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Organization: Helios Development Group</p>
 * @author Whitehead
 * @version $Revision: 1.1 $
 */
@JMXManagedObject(annotated=true)
public class IntCounterMetric extends IntMetric {

	
	public IntCounterMetric(String name) {
		super(name);
	}	
	
	/**
	 * Returns the value of the metric at which it was last set or initialized to.
	 * @return the value
	 */
	@JMXAttribute(expose=true, name="getMetricName", description="getDescription", introspect=true )	
	public int getValue() {
		return value;
	}
	
	/**
	 * @return The description for the metric.
	 * @see com.heliosapm.wiex.tracing.tracing.jmx.metrics.IntMetric#getDescription()
	 */
	@Override
	public String getDescription() {
		return "Integer Counter Metric";
	}
	
	
}
