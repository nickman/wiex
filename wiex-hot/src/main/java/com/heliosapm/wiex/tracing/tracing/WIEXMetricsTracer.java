package com.heliosapm.wiex.tracing.tracing;

import com.heliosapm.wiex.tracing.metrics.client.WIEXMetricsClient;
import com.heliosapm.wiex.tracing.tracing.thread.ThreadStats;

/**
 * <p>Title: WIEXMetricsTracer</p>
 * <p>Description: A concrete tracer that logs metrics to WIEXMetrics. 
 * The allowable length of metric names may cause issues for the WIEXMetrics persistors if the database column length is too short.</p>
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Organization: Helios Development Group</p>
 * @author Whitehead
 * @version $Revision: 1.2 $ 
 */
public class WIEXMetricsTracer extends AbstractTracer {

	/* (non-Javadoc)
	 * @see com.heliosapm.wiex.tracing.tracing.ITrace#getMetricDelimeter()
	 */
	public String getMetricDelimeter() {
		return ":";
	}

	/* (non-Javadoc)
	 * @see com.heliosapm.wiex.tracing.tracing.ITrace#getSegmentDelimeter()
	 */
	public String getSegmentDelimeter() {
		return "/";
	}


	/* (non-Javadoc)
	 * @see com.heliosapm.wiex.tracing.tracing.ITrace#recordCounterMetric(java.lang.String, java.lang.String, long)
	 */
	public void recordCounterMetric(String segment, String metric, long value) {
		WIEXMetricsClient.getInstance().registerElapsedMetric(segment + getMetricDelimeter() + metric, value);

	}

	/* (non-Javadoc)
	 * @see com.heliosapm.wiex.tracing.tracing.ITrace#recordCounterMetric(java.lang.String, java.lang.String, int)
	 */
	public void recordCounterMetric(String segment, String metric, int value) {
		WIEXMetricsClient.getInstance().registerElapsedMetric(segment + getMetricDelimeter() + metric, value);

	}

	/* (non-Javadoc)
	 * @see com.heliosapm.wiex.tracing.tracing.ITrace#recordMetric(java.lang.String, java.lang.String, long)
	 */
	public void recordMetric(String segment, String metric, long value) {
		WIEXMetricsClient.getInstance().registerElapsedMetric(segment + getMetricDelimeter() + metric, value);

	}

	/* (non-Javadoc)
	 * @see com.heliosapm.wiex.tracing.tracing.ITrace#recordMetric(java.lang.String, java.lang.String, int)
	 */
	public void recordMetric(String segment, String metric, int value) {
		WIEXMetricsClient.getInstance().registerElapsedMetric(segment + getMetricDelimeter() + metric, value);
	}

	/* (non-Javadoc)
	 * @see com.heliosapm.wiex.tracing.tracing.ITrace#recordMetric(java.lang.String, java.lang.String, java.lang.String)
	 */
	public void recordMetric(String segment, String metric, String value) {
		// Not supported
	}

	/* (non-Javadoc)
	 * @see com.heliosapm.wiex.tracing.tracing.ITrace#recordMetric(java.lang.String, java.lang.String)
	 */
	public void recordMetric(String segment, String metric) {
		// Not supported
	}

	/* (non-Javadoc)
	 * @see com.heliosapm.wiex.tracing.tracing.ITrace#recordMetric(java.lang.String, com.heliosapm.wiex.tracing.tracing.thread.ThreadStats)
	 */
	public void recordMetric(String segment, ThreadStats ts) {
		WIEXMetricsClient.getInstance().registerElapsedMetric(segment + getMetricDelimeter() + ThreadStats.ELAPSED , ts.getElapsedTime());
		WIEXMetricsClient.getInstance().registerElapsedMetric(segment + getMetricDelimeter() + ThreadStats.WAIT_COUNT , ts.getWaitCount());
		WIEXMetricsClient.getInstance().registerElapsedMetric(segment + getMetricDelimeter() + ThreadStats.BLOCK_COUNT , ts.getBlockCount());			
		if(isCpuTimeEnabled()) {
			WIEXMetricsClient.getInstance().registerElapsedMetric(segment + getMetricDelimeter() + ThreadStats.CPU , ts.getCpuTime());		
			WIEXMetricsClient.getInstance().registerElapsedMetric(segment + getMetricDelimeter() + ThreadStats.USER_CPU , ts.getUserCpuTime());
		}
		if(isContentionEnabled()) {		
			WIEXMetricsClient.getInstance().registerElapsedMetric(segment + getMetricDelimeter() + ThreadStats.WAIT_TIME , ts.getWaitTime());
			WIEXMetricsClient.getInstance().registerElapsedMetric(segment + getMetricDelimeter() + ThreadStats.BLOCK_TIME , ts.getBlockTime());
		}
		
	}

	/* (non-Javadoc)
	 * @see com.heliosapm.wiex.tracing.tracing.ITrace#recordMetricIncidents(java.lang.String, java.lang.String, int)
	 */
	public void recordMetricIncidents(String segment, String metric,
			int incidents) {
		// Not supported
	}

	/* (non-Javadoc)
	 * @see com.heliosapm.wiex.tracing.tracing.ITrace#recordTimeStamp(java.lang.String, java.lang.String, long)
	 */
	public void recordTimeStamp(String segment, String metric, long timestamp) {
		// Not supported
	}

	public void recordCounterMetricAdd(String segment, String metric, long value) {
		//	Not supported
		
	}

	public void recordCounterMetricAdd(String segment, String metric, int value) {
		// Not supported
		
	}

}
