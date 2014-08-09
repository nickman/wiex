/**
 * 
 */
package com.heliosapm.wiex.tracing.tracing.jmx.metrics;

import com.heliosapm.wiex.jmx.dynamic.annotation.JMXAttribute;
import com.heliosapm.wiex.jmx.dynamic.annotation.JMXManagedObject;
import com.heliosapm.wiex.jmx.dynamic.annotation.JMXOperation;
import com.heliosapm.wiex.jmx.dynamic.annotation.JMXOperationParameter;

/**
 * <p>Title: StringMetric</p>
 * <p>Description: JMX Managed Object for String attribute </p> 
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Organization: Helios Development Group</p>
 * @author Whitehead
 * @version $Revision: 1.1 $
 */
@JMXManagedObject(annotated=true)
public class StringMetric extends AbstractMetric {

	protected String value = null;
	
	/**
	 * @param name
	 */
	public StringMetric(String name) {
		super(name);	
	}

	/**
	 * @return The description of the metric.
	 * @see com.heliosapm.wiex.tracing.tracing.jmx.metrics.AbstractMetric#getDescription()
	 */
	@Override
	public String getDescription() {		
		return "String Metric";
	}
	
	/**
	 * @return the value
	 */
	@JMXAttribute(expose=true, name="getMetricName", description="getDescription", introspect=true )	
	public String getValue() {
		return value;
	}
		
	
	/**
	 * @param value the value to set
	 */
	@JMXOperation(expose=true, name="getOperName", description="getOperDesc", introspect=true)
	public void updateValue(@JMXOperationParameter(name="MetricValue", description="The new String value for the metric")Object value) {
		this.value = (String)value;
	}	

}
