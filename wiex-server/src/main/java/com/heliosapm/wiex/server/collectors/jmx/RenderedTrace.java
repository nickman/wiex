/**
 * 
 */
package com.heliosapm.wiex.server.collectors.jmx;

/**
 * <p>Title: RenderedTrace</p>
 * <p>Description: A container for trace values generated from <code>ObjectTracer</code>s.</p> 
 * <p>Copyright: Copyright (c) 2007</p>
 * <p>Company: Helios Development Group</p>
 * @author Whitehead
 * @version $Revision: 1.1 $
 */

public class RenderedTrace {
	protected String segment = null;
	protected String metric = null;
	protected String type = null;
	protected String value = null;
	protected Long frequency = null;
	protected String timeUnit = null;
	
	
	/**
	 * Creates a new RenderedTrace with no reset timer data.
	 * @param segment The metric segment suffix.
	 * @param metric The metric name.
	 * @param type The tracing type.
	 * @param value The value of the trace in string format.
	 */
	public RenderedTrace(String segment, String metric, String type, String value) {
		this.segment = segment;
		this.metric = metric;
		this.type = type;
		this.value = value;
	}
	/**
	 * Creates a new RenderedTrace with reset timer data.
	 * Note that reset timer data will be ignored if the type is not <code>TRLONG</code>.
	 * @param segment The metric segment suffix.
	 * @param metric The metric name.
	 * @param type The tracing type.
	 * @param value The value of the trace in string format.
	 * @param frequency The frequency of the timed reset.
	 * @param timeUnit The unit of the frequency.
	 */
	public RenderedTrace(String segment, String metric, String type, String value, Long frequency, String timeUnit) {
		this.segment = segment;
		this.metric = metric;
		this.type = type;
		this.value = value;
		this.frequency = frequency;
		this.timeUnit = timeUnit;
	}
	/**
	 * @return the frequency
	 */
	public Long getFrequency() {
		return frequency;
	}
	/**
	 * @return the metric
	 */
	public String getMetric() {
		return metric;
	}
	/**
	 * @return the segment
	 */
	public String getSegment() {
		return segment;
	}
	/**
	 * @return the timeUnit
	 */
	public String getTimeUnit() {
		return timeUnit;
	}
	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}
	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}
	/**
	 * @param frequency the frequency to set
	 */
	public void setFrequency(Long frequency) {
		this.frequency = frequency;
	}
	/**
	 * @param timeUnit the timeUnit to set
	 */
	public void setTimeUnit(String timeUnit) {
		this.timeUnit = timeUnit;
	}
}
