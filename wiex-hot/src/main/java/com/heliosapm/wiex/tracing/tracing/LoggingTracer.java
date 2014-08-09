package com.heliosapm.wiex.tracing.tracing;

import com.heliosapm.wiex.tracing.tracing.thread.ThreadStats;


/**
 * <p>Title: LoggingTracer</p>
 * <p>Description: A concrete tracer that logs metrics to a log4j hierarchy. Does not support getUserId</p>
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Organization: Helios Development Group</p>
 * @author Whitehead
 * @version $Revision: 1.2 $ 
 */
public class LoggingTracer extends AbstractTracer {
	
 
	protected static final String VALUE_DELIM = ":-";
	protected static final String COUNTER = "[Counter]";
	protected static final String COUNTER_ADD = "[CounterAdd]";
	protected static final String INCIDENTS = "[Incidents]";
	
	

	/* (non-Javadoc)
	 * @see com.heliosapm.wiex.tracing.tracing.ITrace#getMetricDelimeter()
	 */
	public String getMetricDelimeter() {
		return ".";
	}

	/* (non-Javadoc)
	 * @see com.heliosapm.wiex.tracing.tracing.ITrace#getSegmentDelimeter()
	 */
	public String getSegmentDelimeter() {
		return ".";
	}

	/* (non-Javadoc)
	 * @see com.heliosapm.wiex.tracing.tracing.ITrace#getUserId()
	 */
	public String getUserId() {
		return "";
	}

	/* (non-Javadoc)
	 * @see com.heliosapm.wiex.tracing.tracing.ITrace#recordCounterMetric(java.lang.String, java.lang.String, long)
	 */
	public void recordCounterMetric(String segment, String metric, long value) {
		log.trace(getStringBuilder().append(COUNTER).append(segment).append(getSegmentDelimeter()).append(metric).append(VALUE_DELIM).append(value));

	}

	/* (non-Javadoc)
	 * @see com.heliosapm.wiex.tracing.tracing.ITrace#recordCounterMetric(java.lang.String, java.lang.String, int)
	 */
	public void recordCounterMetric(String segment, String metric, int value) {
		log.trace(getStringBuilder().append(COUNTER).append(segment).append(getSegmentDelimeter()).append(metric).append(VALUE_DELIM).append(value));

	}

	/* (non-Javadoc)
	 * @see com.heliosapm.wiex.tracing.tracing.ITrace#recordMetric(java.lang.String, java.lang.String, long)
	 */
	public void recordMetric(String segment, String metric, long value) {
		log.trace(getStringBuilder().append(segment).append(getSegmentDelimeter()).append(metric).append(VALUE_DELIM).append(value));

	}

	/* (non-Javadoc)
	 * @see com.heliosapm.wiex.tracing.tracing.ITrace#recordMetric(java.lang.String, java.lang.String, int)
	 */
	public void recordMetric(String segment, String metric, int value) {
		log.trace(getStringBuilder().append(segment).append(getSegmentDelimeter()).append(metric).append(VALUE_DELIM).append(value));
	}

	/* (non-Javadoc)
	 * @see com.heliosapm.wiex.tracing.tracing.ITrace#recordMetric(java.lang.String, java.lang.String, java.lang.String)
	 */
	public void recordMetric(String segment, String metric, String value) {
		log.trace(getStringBuilder().append(segment).append(getSegmentDelimeter()).append(metric).append(VALUE_DELIM).append(value));
	}

	/* (non-Javadoc)
	 * @see com.heliosapm.wiex.tracing.tracing.ITrace#recordMetric(java.lang.String, java.lang.String)
	 */
	public void recordMetric(String segment, String metric) {
		log.trace(getStringBuilder().append(segment).append(getSegmentDelimeter()).append(metric));

	}

	/**
	 * {@inheritDoc}
	 * @see com.heliosapm.wiex.tracing.tracing.ITracer#recordMetric(java.lang.String, com.heliosapm.wiex.tracing.tracing.thread.ThreadStats)
	 */
	public void recordMetric(String segment, ThreadStats ts) {
		log.trace(getStringBuilder().append(segment).append(getSegmentDelimeter()).append(ts.toString()));		

	}

	/**
	 * {@inheritDoc}
	 * @see com.heliosapm.wiex.tracing.tracing.ITracer#recordMetricIncidents(java.lang.String, java.lang.String, int)
	 */
	public void recordMetricIncidents(String segment, String metric,
			int incidents) {
		log.trace(getStringBuilder().append(INCIDENTS).append(segment).append(getSegmentDelimeter()).append(metric));

	}

	/* (non-Javadoc)
	 * @see com.heliosapm.wiex.tracing.tracing.ITrace#recordTimeStamp(java.lang.String, java.lang.String, long)
	 */
	public void recordTimeStamp(String segment, String metric, long timestamp) {
		log.trace(getStringBuilder().append(segment).append(getSegmentDelimeter()).append(metric).append(VALUE_DELIM).append(new java.util.Date(timestamp).toString()));
	}

	public void recordCounterMetricAdd(String segment, String metric, long value) {
		log.trace(getStringBuilder().append(COUNTER_ADD).append(segment).append(getSegmentDelimeter()).append(metric).append(VALUE_DELIM).append(value));
		
	}

	public void recordCounterMetricAdd(String segment, String metric, int value) {
		log.trace(getStringBuilder().append(COUNTER_ADD).append(segment).append(getSegmentDelimeter()).append(metric).append(VALUE_DELIM).append(value));
		
	}

}
