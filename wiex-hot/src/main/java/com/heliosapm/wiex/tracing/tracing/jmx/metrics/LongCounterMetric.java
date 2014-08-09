package com.heliosapm.wiex.tracing.tracing.jmx.metrics;

import com.heliosapm.wiex.jmx.dynamic.annotation.JMXAttribute;
import com.heliosapm.wiex.jmx.dynamic.annotation.JMXManagedObject;
import com.heliosapm.wiex.jmx.dynamic.annotation.JMXOperation;
import com.heliosapm.wiex.jmx.dynamic.annotation.JMXOperationParameter;

/**
 * <p>Title: LongCounterMetric</p>
 * <p>Description: </p> 
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Organization: Helios Development Group</p>
 * @author Whitehead
 * @version $Revision: 1.1 $
 */
@JMXManagedObject(annotated=true)
public class LongCounterMetric extends LongMetric {
	
	public LongCounterMetric(String name) {
		super(name);
	}
	
	
	/**
	 * @return the value
	 */
	@JMXAttribute(expose=true, name="getMetricName", description="getDescription", introspect=true )	
	public long getValue() {
		return value;
	}

	@Override
	public String getDescription() {
		return "Long Counter Metric";
	}
	
	
	/**
	 * @param value the value to set
	 */
	
	@JMXOperation(expose=true, name="getOperName", description="getOperDesc", introspect=true)
	public void updateValue(@JMXOperationParameter(name="MetricValue", description="The new Long value for the metric") Object value) {
		value = ((Long)value).longValue();
		readings.incrementAndGet();
		this.timestamp.set(System.currentTimeMillis());
	}
	
	

}
