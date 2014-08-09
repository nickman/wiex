package com.heliosapm.wiex.tracing.tracing.jmx.metrics;

import com.heliosapm.wiex.jmx.dynamic.annotation.JMXAttribute;
import com.heliosapm.wiex.jmx.dynamic.annotation.JMXManagedObject;
import com.heliosapm.wiex.jmx.dynamic.annotation.JMXOperation;
import com.heliosapm.wiex.jmx.dynamic.annotation.JMXOperationParameter;

/**
 * <p>Title: IntMetric</p>
 * <p>Description: JMX Managed Object for Int Metric</p> 
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Organization: Helios Development Group</p>
 * @author Whitehead
 * @version $Revision: 1.1 $
 */
@JMXManagedObject(annotated=true)
public class IntMetric  extends AbstractMetric {

	/**The value of the metric	 */
	protected int value = 0;
	/**The timestamp that the metric was last set at */
	protected long timestamp = 0L;
	
	public IntMetric(String name) {
		super(name);
	}	
	
	/**
	 * @return the value
	 */
	@JMXAttribute(expose=true, name="getMetricName", description="getDescription", introspect=true )	
	public int getValue() {
		if(System.currentTimeMillis()-timestamp>=INTERVAL) {
			return 0;
		} else {
			return value;
		}
	}
	
	@Override
	public String getDescription() {
		return "Integer Metric";
	}
	
	
	/**
	 * @param value the value to set
	 */
	@JMXOperation(expose=true, name="getOperName", description="getOperDesc", introspect=true)
	public void updateValue(@JMXOperationParameter(name="MetricValue", description="The new Integer value for the metric")Object value) {
		this.value = ((Integer)value).intValue();
		this.timestamp = System.currentTimeMillis();
	}

	
	
}
