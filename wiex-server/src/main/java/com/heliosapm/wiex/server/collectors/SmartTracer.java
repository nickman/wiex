/**
 * 
 */
package com.heliosapm.wiex.server.collectors;

import com.heliosapm.wiex.tracing.tracing.ITracer;
import com.heliosapm.wiex.tracing.tracing.TracerFactory;

/**
 * <p>Title: SmartTracer</p>
 * <p>Description: A handler for constructing traces from string types.</p> 
 * <p>Copyright: Copyright (c) 2007</p>
 * <p>Company: Helios Development Group</p>
 * @author Whitehead
 * @version $Revision: 1.1 $
 */

public class SmartTracer {
	/** The parameter name constant for the metric type value of Long*/
	public static final String METRIC_TYPE_LONG = "LONG";
	/** The parameter name constant for the metric type value of Int*/
	public static final String METRIC_TYPE_INT = "INT";
	/** The parameter name constant for the metric type value of String*/
	public static final String METRIC_TYPE_STRING = "STRING";	
	/** The parameter name constant for the metric type value of Long Counter*/
	public static final String METRIC_TYPE_COUNTER_LONG = "CLONG";
	/** The parameter name constant for the metric type value of Int Counter*/
	public static final String METRIC_TYPE_COUNTER_INT = "CINT";
	/** The parameter name constant for the metric type value of Incident*/
	public static final String METRIC_TYPE_INCIDENT = "INCIDENT";
	/** The parameter name constant for the metric type value of Delta Int*/
	public static final String METRIC_TYPE_DELTA_INT = "DINT";
	/** The parameter name constant for the metric type value of Counter Delta Int*/
	public static final String METRIC_TYPE_COUNTER_DELTA_INT = "CDINT";
	/** The parameter name constant for the metric type value of Delta Long*/
	public static final String METRIC_TYPE_DELTA_LONG = "DLONG";
	/** The parameter name constant for the metric type value of Counter Delta Long*/
	public static final String METRIC_TYPE_COUNTER_DELTA_LONG = "CDLONG";
	
	/**
	 * Records an individual metric.
	 * @param category Them metric resource segment.
	 * @param name The metric name.
	 * @param value The value of the submited metric.
	 * @param type The type of the submited metric.
	 * @param tracer The tracer instance to trace with.
	 * @throws Exception 
	 */
	public static void recordTrace(String category, String name, String value, String type, ITracer tracer) {
		if(value==null) return;
		if(type.equalsIgnoreCase(METRIC_TYPE_LONG)) {
			tracer.recordMetric(category, name, Long.parseLong(value));
		} else if(type.equalsIgnoreCase(METRIC_TYPE_INT)) {
			tracer.recordMetric(category, name, Integer.parseInt(value));
		} else if(type.equalsIgnoreCase(METRIC_TYPE_STRING)) {
			tracer.recordMetric(category, name, value);
		} else if(type.equalsIgnoreCase(METRIC_TYPE_COUNTER_LONG)) {
			tracer.recordCounterMetric(category, name, Long.parseLong(value));
		} else if(type.equalsIgnoreCase(METRIC_TYPE_COUNTER_INT)) {
			tracer.recordCounterMetric(category, name, Integer.parseInt(value));
		} else if(type.equalsIgnoreCase(METRIC_TYPE_INCIDENT)) {
			tracer.recordMetric(category, name);
		} else if(type.equalsIgnoreCase(METRIC_TYPE_COUNTER_DELTA_INT)) {
			tracer.recordCounterMetricDelta(category, name, Integer.parseInt(value));
		} else if(type.equalsIgnoreCase(METRIC_TYPE_DELTA_INT)) {
			tracer.recordMetricDelta(category, name, Integer.parseInt(value));
		} else if(type.equalsIgnoreCase(METRIC_TYPE_COUNTER_DELTA_LONG)) {
			tracer.recordCounterMetricDelta(category, name, Long.parseLong(value));
		} else if(type.equalsIgnoreCase(METRIC_TYPE_DELTA_LONG)) {
			tracer.recordMetricDelta(category, name, Long.parseLong(value));			
		} else {
			throw new RuntimeException("Metric Type Not Recognized:" + type);
		}
	}
	
	/**
	 * Records an individual metric.
	 * Acquires the tracer from the TracerFactory.
	 * @param category Them metric resource segment.
	 * @param name The metric name.
	 * @param value The value of the submitted metric.
	 * @param type The type of the submitted metric.
	 * @throws Exception 
	 */
	public static void recordTrace(String category, String name, String value, String type) throws Exception {
		recordTrace(category, name, value, type, TracerFactory.getInstance());
	}		
	

}
