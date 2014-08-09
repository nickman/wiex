/**
 * 
 */
package com.heliosapm.wiex.tracing.tracing;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import com.heliosapm.wiex.jmx.dynamic.ManagedObjectDynamicMBean;
import com.heliosapm.wiex.jmx.dynamic.OperationNotFoundException;
import com.heliosapm.wiex.jmx.dynamic.annotation.JMXOperation;
import com.heliosapm.wiex.jmx.dynamic.annotation.JMXOperationParameter;
import com.heliosapm.wiex.jmx.util.MBeanServerHelper;
import com.heliosapm.wiex.tracing.tracing.jmx.TracingMetricType;
import com.heliosapm.wiex.tracing.tracing.jmx.metrics.AbstractMetric;
import com.heliosapm.wiex.tracing.tracing.jmx.metrics.IntCounterMetric;
import com.heliosapm.wiex.tracing.tracing.jmx.metrics.IntMetric;
import com.heliosapm.wiex.tracing.tracing.jmx.metrics.LongCounterMetric;
import com.heliosapm.wiex.tracing.tracing.jmx.metrics.LongMetric;
import com.heliosapm.wiex.tracing.tracing.jmx.metrics.StringMetric;
import com.heliosapm.wiex.tracing.tracing.thread.ThreadStats;

/**
 * <p>Title: JMXTracer</p>
 * <p>Description: </p> 
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Organization: Helios Development Group</p>
 * @author Whitehead
 * @version $Revision: 1.5 $
 */

public class JMXTracer extends AbstractTracer implements ITracer {
	
	protected MBeanServer mbeanServer = null;
	
	public static final String MBEAN_KEY_PROPERTY = "type=WIEXTracingMetric";
	public static final String JMX_DOMAIN_KEY = "wiex.tracing.jmx.domain";
	public static final String DEFAULT_JMX_DOMAIN = "DefaultDomain";
	
	/**
	 * Instantiates a new JMX Tracer
	 */
	public JMXTracer()  {
		String jmxDomain = System.getProperty(JMX_DOMAIN_KEY, DEFAULT_JMX_DOMAIN);
		try {
			mbeanServer = MBeanServerHelper.getMBeanServer(jmxDomain);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * @return The escaped JMX Metric Delimeter
	 * @see com.heliosapm.wiex.tracing.tracing.ITracer#getEscapedMetricDelimeter()
	 */
	public String getEscapedMetricDelimeter() {
		return ":";
	}

	/**
	 * @return The escaped JMX Segment Delimeter
	 * @see com.heliosapm.wiex.tracing.tracing.ITracer#getEscapedSegmentDelimeter()
	 */
	public String getEscapedSegmentDelimeter() {
		return "\\.";
	}

	/**
	 * @return The JMX Metric Delimeter
	 * @see com.heliosapm.wiex.tracing.tracing.ITracer#getMetricDelimeter()
	 */
	public String getMetricDelimeter() {
		return ":";
	}

	/**
	 * @return The JMX Segment Delimeter
	 * @see com.heliosapm.wiex.tracing.tracing.ITracer#getSegmentDelimeter()
	 */
	public String getSegmentDelimeter() {
		return ".";
	}


	/**
	 * @param segment
	 * @param metric
	 * @param value
	 * @see com.heliosapm.wiex.tracing.tracing.ITracer#recordCounterMetric(java.lang.String, java.lang.String, long)
	 */
	public void recordCounterMetric(String segment, String metric, long value) {
		if(!TracerFactory.isTraceEnabled(segment)) return;
		update(segment + ":" + MBEAN_KEY_PROPERTY, metric, value, TracingMetricType.LONG_COUNTER);

	}

	/**
	 * @param segment
	 * @param metric
	 * @param value
	 * @see com.heliosapm.wiex.tracing.tracing.ITracer#recordCounterMetric(java.lang.String, java.lang.String, int)
	 */
	public void recordCounterMetric(String segment, String metric, int value) {
		if(!TracerFactory.isTraceEnabled(segment)) return;
		update(segment + ":" + MBEAN_KEY_PROPERTY, metric, value, TracingMetricType.INT_COUNTER);

	}

	/**
	 * @param segment
	 * @param metric
	 * @param value
	 * @see com.heliosapm.wiex.tracing.tracing.ITracer#recordCounterMetricAdd(java.lang.String, java.lang.String, long)
	 */
	public void recordCounterMetricAdd(String segment, String metric, long value) {
		if(!TracerFactory.isTraceEnabled(segment)) return;
		// TODO Auto-generated method stub

	}

	/**
	 * @param segment
	 * @param metric
	 * @param value
	 * @see com.heliosapm.wiex.tracing.tracing.ITracer#recordCounterMetricAdd(java.lang.String, java.lang.String, int)
	 */
	public void recordCounterMetricAdd(String segment, String metric, int value) {
		if(!TracerFactory.isTraceEnabled(segment)) return;
		// TODO Auto-generated method stub

	}

	/**
	 * @param fullMetricName
	 * @see com.heliosapm.wiex.tracing.tracing.ITracer#recordIntervalIncident(java.lang.String)
	 */
	public void recordIntervalIncident(String fullMetricName) {
		if(!TracerFactory.isTraceEnabled(fullMetricName)) return;
		// TODO Auto-generated method stub

	}

	/**
	 * @param fullMetricName
	 * @param incidents
	 * @see com.heliosapm.wiex.tracing.tracing.ITracer#recordIntervalIncident(java.lang.String, int)
	 */
	public void recordIntervalIncident(String fullMetricName, int incidents) {
		if(!TracerFactory.isTraceEnabled(fullMetricName)) return;
		// TODO Auto-generated method stub

	}

	/**
	 * @param segment
	 * @param metric
	 * @param value
	 * @see com.heliosapm.wiex.tracing.tracing.ITracer#recordMetric(java.lang.String, java.lang.String, long)
	 */
	public void recordMetric(String segment, String metric, long value) {
		if(!TracerFactory.isTraceEnabled(segment)) return;
		update(segment + ":" + MBEAN_KEY_PROPERTY, metric, value, TracingMetricType.LONG);

	}

	/**
	 * @param segment
	 * @param metric
	 * @param value
	 * @see com.heliosapm.wiex.tracing.tracing.ITracer#recordMetric(java.lang.String, java.lang.String, int)
	 */
	public void recordMetric(String segment, String metric, int value) {
		if(!TracerFactory.isTraceEnabled(segment)) return;
		update(segment + ":" + MBEAN_KEY_PROPERTY, metric, value, TracingMetricType.INT);

	}

	/**
	 * @param segment
	 * @param metric
	 * @param value
	 * @see com.heliosapm.wiex.tracing.tracing.ITracer#recordMetric(java.lang.String, java.lang.String, java.lang.String)
	 */
	public void recordMetric(String segment, String metric, String value) {
		if(!TracerFactory.isTraceEnabled(segment)) return;
		update(segment + ":" + MBEAN_KEY_PROPERTY, metric, value, TracingMetricType.STRING);

	}

	/**
	 * @param segment
	 * @param metric
	 * @see com.heliosapm.wiex.tracing.tracing.ITracer#recordMetric(java.lang.String, java.lang.String)
	 */
	public void recordMetric(String segment, String metric) {
		if(!TracerFactory.isTraceEnabled(segment)) return;
		// TODO Auto-generated method stub

	}

	/**
	 * Records a set of thread stats collected as a delta between two points in a code path.
	 * @param segment The resource segments of the full metric name. e.g. <code>EJB|Session|OrderBean|saveOrder</code>
	 * @param ts A populated delta ThreadStats instance.
	 */
	@JMXOperation(description="Records a ThreadStat Delta metric", name="RecordThreadStatMetric")
	public void recordMetric(
			@JMXOperationParameter(description="The metric segment", name="MetricSegment") String segment, 
			@JMXOperationParameter(description="A closed ThreadStat", name="ThreadStat")ThreadStats ts) { 							
			
		if(!TracerFactory.isTraceEnabled(segment)) return;
		try {
			recordMetric(segment, ThreadStats.ELAPSED, ts.getElapsedTime());
			recordMetric(segment, ThreadStats.BLOCK_COUNT, ts.getBlockCount());
			recordMetric(segment, ThreadStats.WAIT_COUNT, ts.getWaitCount());
			recordMetric(segment, ThreadStats.BLOCK_TIME, ts.getBlockTime());
			recordMetric(segment, ThreadStats.WAIT_TIME, ts.getWaitTime());
			recordMetric(segment, ThreadStats.CPU, ts.getCpuTime());
			recordMetric(segment, ThreadStats.USER_CPU, ts.getUserCpuTime());
			recordMetric(segment, "Invocations per ms.");
		} catch (Throwable e) {}

	}


	/**
	 * @param segment
	 * @param metric
	 * @param incidents
	 * @see com.heliosapm.wiex.tracing.tracing.ITracer#recordMetricIncidents(java.lang.String, java.lang.String, int)
	 */
	public void recordMetricIncidents(String segment, String metric,
			int incidents) {
		if(!TracerFactory.isTraceEnabled(segment)) return;
		// TODO Auto-generated method stub

	}

	/**
	 * @param segment
	 * @param metric
	 * @param timestamp
	 * @see com.heliosapm.wiex.tracing.tracing.ITracer#recordTimeStamp(java.lang.String, java.lang.String, long)
	 */
	public void recordTimeStamp(String segment, String metric, long timestamp) {
		if(!TracerFactory.isTraceEnabled(segment)) return;
		// TODO Auto-generated method stub

	}

	/**
	 * @param category
	 * @param name
	 * @see com.heliosapm.wiex.tracing.tracing.ITracer#reinitializeMetric(java.lang.String, java.lang.String)
	 */
	public void reinitializeMetric(String category, String name) {
		
		// TODO Auto-generated method stub

	}

	/**
	 * @param category
	 * @param name
	 * @see com.heliosapm.wiex.tracing.tracing.ITracer#resetIntCounter(java.lang.String, java.lang.String)
	 */
	public void resetIntCounter(String category, String name) {
		// TODO Auto-generated method stub

	}

	/**
	 * @param fullName
	 * @see com.heliosapm.wiex.tracing.tracing.ITracer#resetIntCounter(java.lang.String)
	 */
	public void resetIntCounter(String fullName) {
		// TODO Auto-generated method stub

	}

	/**
	 * @param category
	 * @param name
	 * @see com.heliosapm.wiex.tracing.tracing.ITracer#resetLongCounter(java.lang.String, java.lang.String)
	 */
	public void resetLongCounter(String category, String name) {
		// TODO Auto-generated method stub

	}

	/**
	 * @param fullName
	 * @see com.heliosapm.wiex.tracing.tracing.ITracer#resetLongCounter(java.lang.String)
	 */
	public void resetLongCounter(String fullName) {
		// TODO Auto-generated method stub

	}

	/**
	 * @param category
	 * @param name
	 * @param value
	 * @see com.heliosapm.wiex.tracing.tracing.ITracer#setIntCounter(java.lang.String, java.lang.String, int)
	 */
	public void setIntCounter(String category, String name, int value) {
		// TODO Auto-generated method stub

	}

	/**
	 * @param fullName
	 * @param value
	 * @see com.heliosapm.wiex.tracing.tracing.ITracer#setIntCounter(java.lang.String, int)
	 */
	public void setIntCounter(String fullName, int value) {
		// TODO Auto-generated method stub

	}

	/**
	 * @param category
	 * @param name
	 * @param value
	 * @see com.heliosapm.wiex.tracing.tracing.ITracer#setLongCounter(java.lang.String, java.lang.String, long)
	 */
	public void setLongCounter(String category, String name, long value) {
		// TODO Auto-generated method stub

	}

	/**
	 * @param fullName
	 * @param value
	 * @see com.heliosapm.wiex.tracing.tracing.ITracer#setLongCounter(java.lang.String, long)
	 */
	public void setLongCounter(String fullName, long value) {
		if(!TracerFactory.isTraceEnabled(fullName)) return;
		// TODO Auto-generated method stub

	}


	
	
	protected void update(String objectName, String attributeName, Object value, TracingMetricType metricType) {
		Object managedObject = null;
		try {
			ObjectName on = new ObjectName(objectName);
			//Attribute attribute = new Attribute(attributeName, value);
			try {
				//mbeanServer.setAttribute(on, attribute);
				mbeanServer.invoke(on, "Update " + attributeName, new Object[]{value}, new String[]{"java.lang.Object"});
			} catch (InstanceNotFoundException ine) {
				// MBean was not registered
				managedObject = createMetric(metricType, attributeName); 
				ManagedObjectDynamicMBean mbean = new ManagedObjectDynamicMBean(managedObject); 
				mbeanServer.registerMBean(mbean, on);
				//mbeanServer.setAttribute(on, attribute);
				mbeanServer.invoke(on, "Update " + attributeName, new Object[]{value}, new String[]{"java.lang.Object"});
			} catch (OperationNotFoundException ane) {				
				// Managed Object was not registered
				managedObject = createMetric(metricType, attributeName);
				mbeanServer.invoke(on, "addManagedObject", new Object[]{managedObject}, new String[]{"java.lang.Object"});
				//mbeanServer.setAttribute(on, attribute);
				mbeanServer.invoke(on, "Update " + attributeName, new Object[]{value}, new String[]{"java.lang.Object"});
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	protected AbstractMetric createMetric(TracingMetricType metricType, String attributeName) {
		AbstractMetric metric = null;
		if(metricType.equals(TracingMetricType.INT)) {
			return new IntMetric(attributeName);
		} else if(metricType.equals(TracingMetricType.INT_COUNTER)) {
			return new IntCounterMetric(attributeName);
		} else if(metricType.equals(TracingMetricType.LONG_COUNTER)) {
			return new LongCounterMetric(attributeName);
		} else if(metricType.equals(TracingMetricType.LONG)) {
			return new LongMetric(attributeName);
		}  else if(metricType.equals(TracingMetricType.STRING)) {
			return new StringMetric(attributeName);
		}
		return metric;
		
	}
	

}
