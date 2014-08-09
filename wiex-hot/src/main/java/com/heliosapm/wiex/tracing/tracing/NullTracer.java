package com.heliosapm.wiex.tracing.tracing;

import com.heliosapm.wiex.tracing.tracing.thread.ThreadStats;

/**
 * <p>Title: NullTracer</p>
 * <p>Description: A concrete tracer that does nothing.</p>
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Organization: Helios Development Group</p>
 * @author Whitehead
 * @version $Revision: 1.4 $ 
 */

public class NullTracer extends AbstractTracer {

	/* (non-Javadoc)
	 * @see com.heliosapm.wiex.tracing.tracing.ITrace#getMetricDelimeter()
	 */
	public String getMetricDelimeter() {
		return "";
	}

	/* (non-Javadoc)
	 * @see com.heliosapm.wiex.tracing.tracing.ITrace#getSegmentDelimeter()
	 */
	public String getSegmentDelimeter() {
		return "";
	}
	
	public String getEscapedSegmentDelimeter() {
		return "";
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
	}

	/* (non-Javadoc)
	 * @see com.heliosapm.wiex.tracing.tracing.ITrace#recordCounterMetric(java.lang.String, java.lang.String, int)
	 */
	public void recordCounterMetric(String segment, String metric, int value) {
	}

	/* (non-Javadoc)
	 * @see com.heliosapm.wiex.tracing.tracing.ITrace#recordMetric(java.lang.String, java.lang.String, long)
	 */
	public void recordMetric(String segment, String metric, long value) {
	}

	/* (non-Javadoc)
	 * @see com.heliosapm.wiex.tracing.tracing.ITrace#recordMetric(java.lang.String, java.lang.String, int)
	 */
	public void recordMetric(String segment, String metric, int value) {
	}

	/* (non-Javadoc)
	 * @see com.heliosapm.wiex.tracing.tracing.ITrace#recordMetric(java.lang.String, java.lang.String, java.lang.String)
	 */
	public void recordMetric(String segment, String metric, String value) {
	}

	/* (non-Javadoc)
	 * @see com.heliosapm.wiex.tracing.tracing.ITrace#recordMetric(java.lang.String, java.lang.String)
	 */
	public void recordMetric(String segment, String metric) {
	}

	/**
	 * {@inheritDoc}
	 * @see com.heliosapm.wiex.tracing.tracing.ITracer#recordMetric(java.lang.String, com.heliosapm.wiex.tracing.tracing.thread.ThreadStats)
	 */
	public void recordMetric(String segment, ThreadStats ts) {
	}

	/**
	 * {@inheritDoc}
	 * @see com.heliosapm.wiex.tracing.tracing.ITracer#recordMetricIncidents(java.lang.String, java.lang.String, int)
	 */
	public void recordMetricIncidents(String segment, String metric,int incidents) {
	}

	/**
	 * {@inheritDoc}
	 * @see com.heliosapm.wiex.tracing.tracing.ITracer#recordTimeStamp(java.lang.String, java.lang.String, long)
	 */
	public void recordTimeStamp(String segment, String metric, long timestamp) {
	}

	/**
	 * {@inheritDoc}
	 * @see com.heliosapm.wiex.tracing.tracing.ITracer#recordCounterMetricAdd(java.lang.String, java.lang.String, long)
	 */
	public void recordCounterMetricAdd(String segment, String metric, long value) {
		
	}

	/**
	 * {@inheritDoc}
	 * @see com.heliosapm.wiex.tracing.tracing.ITracer#recordCounterMetricAdd(java.lang.String, java.lang.String, int)
	 */
	public void recordCounterMetricAdd(String segment, String metric, int value) {
		
	}

}
