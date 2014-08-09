package com.heliosapm.wiex.tracing.tracing.jmx.metrics;

import java.util.concurrent.atomic.AtomicLong;

import com.heliosapm.wiex.jmx.dynamic.annotation.JMXAttribute;
import com.heliosapm.wiex.jmx.dynamic.annotation.JMXManagedObject;
import com.heliosapm.wiex.jmx.dynamic.annotation.JMXOperation;
import com.heliosapm.wiex.jmx.dynamic.annotation.JMXOperationParameter;

/**
 * <p>Title: LongMetric</p>
 * <p>Description: JMX Managed Object for Long metric</p> 
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Organization: Helios Development Group</p>
 * @author Whitehead
 * @version $Revision: 1.1 $
 */
@JMXManagedObject(annotated=true)
public class LongMetric  extends AbstractMetric {

	/**The value of the metric	 */
	protected long value = 0L;
	
	
	
	
	protected AtomicLong average = new AtomicLong(0L);
	/**The timestamp that the metric was last set at */
	protected AtomicLong timestamp = new AtomicLong(0L);
	protected AtomicLong high = new AtomicLong(0L);
	protected AtomicLong low = new AtomicLong(0L);
	
	public LongMetric(String name) {
		super(name);
	}
	
	
	protected void reset() {
		average.set(0L);
		high.set(0L);
		low.set(0L);		
	}
	
	
	/**
	 * @return the value
	 */
	@JMXAttribute(expose=true, name="getMetricName", description="getDescription", introspect=true )	
	public long getValue() {
		if(System.currentTimeMillis()-timestamp.get()>=INTERVAL) {
			reset();
			return 0L;
		} else {
			return average.get();
		}
	}
	
	@JMXAttribute(expose=true, name="getLowMetricName", description="getLowDescription", introspect=true )
	public long getLow() {
		if(System.currentTimeMillis()-timestamp.get()>=INTERVAL) {
			reset();
			return 0L;
		} else {
			return low.get();
		}		
	}

	@JMXAttribute(expose=true, name="getHighMetricName", description="getHighDescription", introspect=true )	
	public long getHigh() {
		if(System.currentTimeMillis()-timestamp.get()>=INTERVAL) {
			reset();
			return 0L;
		} else {
			return high.get();
		}		
	}
	

	@Override
	public String getDescription() {
		return "Long Metric";
	}
	
	
	/**
	 * @param value the value to set
	 */
	
	@JMXOperation(expose=true, name="getOperName", description="getOperDesc", introspect=true)
	public void updateValue(@JMXOperationParameter(name="MetricValue", description="The new Long value for the metric") Object value) {
		long newValue = ((Long)value).longValue();
		long localReadings = readings.incrementAndGet();
		if(localReadings==1) {
			average.set(newValue);
			low.set(newValue);
			high.set(newValue);
		} else {
			long localAverage = (average.get() + newValue)/2; 
			average.set(localAverage);
			if(newValue < low.get()) low.set(newValue);
			if(newValue > high.get()) high.set(newValue);
		}
		this.value = ((Long)value).longValue();
		this.timestamp.set(System.currentTimeMillis());
	}


	
	
}
