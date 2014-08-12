package com.heliosapm.wiex.tracing.tracing;

import java.util.concurrent.TimeUnit;

import com.heliosapm.wiex.jmx.dynamic.annotation.JMXAttribute;
import com.heliosapm.wiex.jmx.dynamic.annotation.JMXOperation;
import com.heliosapm.wiex.jmx.dynamic.annotation.JMXOperationParameter;
import com.heliosapm.wiex.tracing.tracing.thread.ThreadStats;
import com.heliosapm.wiex.tracing.tracing.user.UserIdLocator;

/**
 * <p>Title: ITrace</p>
 * <p>Description: Interface that defines the behaviour of a concrete tracer.</p>
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Organization: Helios Development Group</p>
 * @author Whitehead
 * @version $Revision: 1.14 $
 */

public interface ITracer {

	public static final String CPU_TIME = "CPU Time";
	public static final String USER_CPU_TIME = "User CPU Time";
	public static final String ELAPSED_TIME = "Elapsed Time";
	public static final String WAIT_TIME = "Wait Time";
	public static final String BLOCK_TIME = "Block Time";
	public static final String WAIT_COUNT = "Wait Count";
	public static final String BLOCK_COUNT = "Block Count";
	public static final String INVOCATION_COUNT = "Invocations Per Interval";
	public static final String ERROR_COUNT = "Errors Per Interval";
	public static final String CONCURRENT_COUNT = "Concurrent Invocations";
	public static final String POOL_SIZE = "Current Pool Size";
	public static final String MAX_POOL_SIZE = "Maximum Pool Size";
	public static final String AGENT_ELAPSED = "Monitoring Overhead";
	public static final String ANONYMOUS = "Anonymous";
	public static final String SYSTEM = "System";
//	public static final String RSEG_DELIM = "|";
//	public static final String METR_DELIM = ":";
//	public static final String EJB_RESOURCE_NAME_PREFIX = "EJB|Extended|";
//	public static final String HTTP_RESOURCE_NAME_PREFIX = "Servlets|Extended|";
//	public static final String USER_NAME_PREFIX = "User|"; 
	
	/**
	 * Indicates if this tracer is connected
	 * @return true if this tracer is connected, false otherwise
	 */
	@JMXAttribute(description="Indicates if this tracer is connected.", name="Connected")
	public boolean isConnected();
	
	/**
	 * Returns the full metric name segment delimeter.
	 * e.g. "|" is the delimeter for Wily Introscope.
	 * @return The segment delimeter.
	 */
	@JMXAttribute(description="The metric category delimeter.", name="SegmentDelimeter")
	public String getSegmentDelimeter();
	
	/**
	 * Returns the full metric name segment delimeter escaped if required for regex parsing.
	 * e.g. "\\|" is the escaped delimeter for Wily Introscope.
	 * @return The segment delimeter.
	 */
	public String getEscapedSegmentDelimeter();
	

	/**
	 * Returns the delimeter between the segments and the metric name.
	 * e.g. ":" is the delimeter for Wily Introscope.
	 * @return The metric delimeter.
	 */	
	public String getMetricDelimeter();
	
	/**
	 * Returns the delimeter between the segments and the metric name escaped if required for regex parsing.
	 * e.g. "\\:" is the delimeter for Wily Introscope.
	 * @return The metric delimeter.
	 */	
	public String getEscapedMetricDelimeter();
	
	
	/**
	 * Returns the user name associated with the current thread.
	 * Implementations should return <code>ITrace.ANONYMOUS</code> if the user cannot be identified
	 * or <code>ITrace.SYSTEM</code> if the thread is know to be a server thread.
	 * @return The user Id.
	 */
	public String getUserId();
	
	
	/**
	 * Records a metric which is fed into a high, low, average aggregator.
	 * If no recordings are made in the interval, a zero will be automatically recorded.
	 * @param segment The resource segments of the full metric name. e.g. <code>EJB|Session|OrderBean|saveOrder</code>
	 * @param metric The metric name. e.g. <code>Elapsed Time</code>
	 * @param value The collected reading for the metric.
	 */
	public void recordMetric(String segment, String metric, long value);

	/**
	 * Records a metric which is fed into a high, low, average aggregator.
	 * If no recordings are made in the interval, a zero will be automatically recorded.
	 * @param segment The resource segments of the full metric name. e.g. <code>EJB|Session|OrderBean|saveOrder</code>
	 * @param metric The metric name. e.g. <code>Elapsed Time</code>
	 * @param value The collected reading for the metric.
	 */	
	public void recordMetric(String segment, String metric, int value);

	/**
	 * Records a metric string.
	 * @param segment The resource segments of the full metric name. e.g. <code>EJB|Session|OrderBean</code>
	 * @param metric The metric name. e.g. <code>MetaData</code>
	 * @param value The metric value string.
	 */		
	public void recordMetric(String segment, String metric, String value);

	/**
	 * Records one incident of a metric 
	 * @param segment The resource segments of the full metric name. e.g. <code>EJB|Session|OrderBean|saveOrder</code>
	 * @param metric The metric name. e.g. <code>Invocations Per Interval</code>
	 */			
	public void recordMetric(String segment, String metric);
	
	/**
	 * Records incidents of a metric
	 * @param segment The resource segments of the full metric name. e.g. <code>EJB|Session|OrderBean|saveOrder</code>
	 * @param metric The metric name. e.g. <code>Invocations Per Interval</code>
	 * @param incidents The number of incidents to record.
	 */
	public void recordMetricIncidents(String segment, String metric, int incidents);

	/**
	 * Records a timestamp metric which is fed into a min/max aggregator for the time perdiod.
	 * If no recordings are made in the interval, the last recorded values will be recorded in place.
	 * @param segment The resource segments of the full metric name. e.g. <code>EJB|Session|OrderBean|saveOrder</code>
	 * @param metric The metric name. e.g. <code>Last Time Invoked</code>
	 * @param timestamp The timestamp value for the metric.
	 */		
	public void recordTimeStamp(String segment, String metric, long timestamp);

	/**
	 * Records a metric which is fed into a high, low, average aggregator.
	 * If no recordings are made in the interval, the values in the last timeslice will be recorded.
	 * @param segment The resource segments of the full metric name. e.g. <code>Kitchen|Freezers|57</code>
	 * @param metric The metric name. e.g. <code>Temperature</code>
	 * @param value The collected reading for the metric.
	 */	
	public void recordCounterMetric(String segment, String metric, long value);

	/**
	 * Records a metric which is fed into a high, low, average aggregator.
	 * If no recordings are made in the interval, the values in the last timeslice will be recorded.
	 * @param segment The resource segments of the full metric name. e.g. <code>Kitchen|Freezers|57</code>
	 * @param metric The metric name. e.g. <code>Temperature</code>
	 * @param value The collected reading for the metric.
	 */		
	public void recordCounterMetric(String segment, String metric, int value);
	
	/**
	 * Adds the passed value to the counter's current state.
	 * If no recordings are made in the interval, the values in the last timeslice will be recorded.
	 * @param segment The resource segments of the full metric name. e.g. <code>Kitchen|Freezers|57</code>
	 * @param metric The metric name. e.g. <code>Temperature</code>
	 * @param value The value to be added to the current counter metric.
	 */	
	public void recordCounterMetricAdd(String segment, String metric, long value);

	/**
	 * Adds the passed value to the counter's current state.
	 * If no recordings are made in the interval, the values in the last timeslice will be recorded.
	 * @param segment The resource segments of the full metric name. e.g. <code>Kitchen|Freezers|57</code>
	 * @param metric The metric name. e.g. <code>Temperature</code>
	 * @param value The value to be added to the current counter metric.
	 */		
	public void recordCounterMetricAdd(String segment, String metric, int value);
	
	
	/**
	 * Records a set of thread stats collected as a delta between two points in a code path.
	 * @param segment The resource segments of the full metric name. e.g. <code>EJB|Session|OrderBean|saveOrder</code>
	 * @param ts A populated delta ThreadStats instance.
	 */
	public void recordMetric(String segment, ThreadStats ts);
	
	/**
	 * Records a single incident of a metric for the current sampling interval.
	 * @param fullMetricName The full metric name to record the incident for.
	 */
	public void recordIntervalIncident(String fullMetricName);
	
	/**
	 * Records a number of incidences of a metric for the current sampling interval.
	 * @param fullMetricName The full metric name to record the incidences for.
	 * @param incidents The number of incidences to record.
	 */
	public void recordIntervalIncident(String fullMetricName, int incidents);
	
	/**
	 * Records a single incident of a metric for the current sampling interval.
	 * @param segment The metric segment
	 * @param metricName The metric name 
	 */
	public void recordIntervalIncident(String segment, String metricName);
	
	/**
	 * Records a number of incidences of a metric for the current sampling interval.
	 * @param segment The metric segment
	 * @param metricName The metric name 
	 * @param incidents The number of incidences to record.
	 */
	public void recordIntervalIncident(String segment, String metricName, int incidents);
	
	/**
	 * Checks the reset time cache for a reset timer for the compiled metric name using <code>segment</code> and <code>metric</code>.
	 * If a timer does not exist, one is created and scheduled for the supplied time using <code>frequency</code> and <code>timeUnit</code>.
	 * Then increments the specified counter by <code>value</code> using the method <code>recordCounterMetricAdd(String segment, String metric, long value)</code> 
	 * @param segment The metric segment.
	 * @param metric The metric name.
	 * @param value The value to increment.
	 * @param frequency The frequency of the reset.
	 * @param timeUnit The unit of the frequency.
	 */
	public void recordIntervalReset(String segment, String metric, long value, long frequency, TimeUnit timeUnit);
	
	/**
	 * Checks the reset time cache for a reset timer for the compiled metric name using <code>segment</code> and <code>metric</code>.
	 * If a timer does not exist, one is created and scheduled for the supplied time using <code>frequency</code> and <code>timeUnit</code>.
	 * Then increments the specified counter by <code>value</code> using the method <code>recordCounterMetricAdd(String segment, String metric, long value)</code> 
	 * @param segment The metric segment.
	 * @param metric The metric name.
	 * @param value The value to increment.
	 * @param frequency The frequency of the reset.
	 * @param timeUnit The unit of the frequency.
	 */	
	public void recordIntervalReset(String segment, String metric, long value, long frequency, String timeUnit);
	
	/**
	 * If a reset timer is active for the passed metric, it is cancelled.
	 * @param segment
	 * @param metric
	 */
	public void cancelIntervalReset(String segment, String metric);
	
	/**
	 * Returns true if there is an active reset time for the passed metric.
	 * @param segment
	 * @param metric
	 * @return true if a reset time is active.
	 */	
	public boolean isResetActive(String segment, String metric);		
	
	/**
	 * Records the delta of the passed value and the previosuly recorded value as a "sticky" counter.
	 * If there is no previous value, the value is stored in state, but no trace is recorded.
	 * @param segment The segment name.
	 * @param metricName The metric name
	 * @param value The new valoue to calculate the delta from.
	 */
	public void recordCounterMetricDelta(String segment, String metricName, int value);
	
	/**
	 * Records the delta of the passed value and the previosuly recorded value as a "sticky" counter.
	 * If there is no previous value, the value is stored in state, but no trace is recorded.
	 * @param segment The segment name.
	 * @param metricName The metric name
	 * @param value The new valoue to calculate the delta from.
	 */	
	public void recordCounterMetricDelta(String segment, String metricName, long value);
	
	/**
	 * Records the delta of the passed value and the previosuly recorded value as an interval averaged counter.
	 * If there is no previous value, the value is stored in state, but no trace is recorded.
	 * @param segment The segment name.
	 * @param metricName The metric name
	 * @param value The new valoue to calculate the delta from.
	 */	
	public void recordMetricDelta(String segment, String metricName, int value);
	
	/**
	 * Records the delta of the passed value and the previosuly recorded value as an interval averaged counter.
	 * If there is no previous value, the value is stored in state, but no trace is recorded.
	 * @param segment The segment name.
	 * @param metricName The metric name
	 * @param value The new valoue to calculate the delta from.
	 */		
	public void recordMetricDelta(String segment, String metricName, long value);
		

	
	
	/**
	 * Sets the instance of the UserId locator.
	 */
	public void setUserIdLocator(UserIdLocator userIdLocator);
	
	/**
	 * Acquires and truncates the current thread's StringBuidler.
	 * This provides a high performance thread-safe string builder.
	 * @return A truncated string builder for use by this thread.
	 */
	public StringBuilder getStringBuilder();	
	
	/**
	 * Creats a newly created and populated undiffed ThreadStats for the current thread.
	 * @return A new absolute ThreadStats for the current thread.
	 */
	public ThreadStats getThreadStatsInstance();
	
	/**
	 * Creats a newly created and populated undiffed ThreadStats for the thread Id passed in.
	 * @param threadId The thread Id of the thread to generates stats for.
	 * @return A new absolute ThreadStats.
	 */
	public ThreadStats getThreadStatsInstance(long threadId);	
	
	/**
	 * Returns the prefix for a user based metric segment.
	 * @return The user segment prefix.
	 */
	public String getUserIdPefix();	
	
	/**
	 * Updates the thread stats with current values.
	 * This undiffs the ThreadStatus.
	 */
	public void updateThreadStats(ThreadStats threadStats);	
	
	/**
	 * Creates a new current ThreadStats and calcs a delta between the new and the passed
	 * and updates the passed.
	 * @param threadStats
	 */
	public void deltaThreadStats(ThreadStats threadStats);
	
	/**
	 * No Op trace command for external instrumentation.
	 * @param key
	 */
	public void trace(String key);
	
	/**
	 * Resets a counter to zero for a long counter
	 * @param category The metric's resource segment.
	 * @param name The metric name.
	 */
	public void resetLongCounter(String category, String name);

	/**
	 * Resets a counter to zero for a long counter
	 * @param fullName The metric's fully qualified name.
	 */
	public void resetLongCounter(String fullName);
	
	/**
	 * Resets a counter to zero for an int counter
	 * @param category The metric's resource segment.
	 * @param name The metric name.
	 */
	public void resetIntCounter(String category, String name);

	/**
	 * Resets a counter to zero for an int counter
	 * @param fullName The metric's fully qualified name.
	 */
	public void resetIntCounter(String fullName);
	
	/**
	 * Sets the absolute value for an int counter.
	 * @param category The metric's resource segment.
	 * @param name The metric name.
	 * @param value The value to set the counter to.
	 */
	public void setIntCounter(String category, String name, int value);
	
	/**
	 * Sets the absolute value for an int counter.
	 * @param fullName The metric's fully qualified name.
	 * @param value The value to set the counter to.
	 */	
	public void setIntCounter(String fullName, int value);
	
	/**
	 * Sets the absolute value for a long counter.
	 * @param category The metric's resource segment.
	 * @param name The metric name.
	 * @param value The value to set the counter to.
	 */
	public void setLongCounter(String category, String name, long value);
	
	/**
	 * Sets the absolute value for a long counter.
	 * @param fullName The metric's fully qualified name.
	 * @param value The value to set the counter to.
	 */	
	public void setLongCounter(String fullName, long value);
	
	/**
	 * Reinitializes the metric.
	 * @param category The metric category.
	 * @param name The metric name.
	 */
	public void reinitializeMetric(String category, String name);
	
	/**
	 * Creates a concatenated segment string.
	 * Interleaves delimeters between each provided 
	 * segment and returns the completed string.
	 * @param segments An array of segments to interleave.
	 * @return The full segment.
	 */
	public String buildSegment(String...segments);	
	
	/**
	 * Creates a concatenated segment string.
	 * Interleaves delimeters between each provided 
	 * segment and returns the completed string.
	 * @param leaveTrailingDelim If true, the trailing delimeter will be left in. Otherwise, it will be removed.
	 * @param segments An array of segments to interleave.
	 * @return The full segment.
	 */
	public String buildSegment(boolean leaveTrailingDelim, String...segments);	
	
	/**
	 * Creates a concatenated segment string starting with the provided base.
	 * Interleaves delimeters between each provided 
	 * segment and returns the completed string.
	 * @param base The initial prefix for the segment.
	 * @param leaveTrailingDelim If true, the trailing delimeter will be left in. Otherwise, it will be removed.
	 * @param segments An array of segments to interleave.
	 * @return The full segment.
	 */
	public String buildSegment(String base, boolean leaveTrailingDelim, String...segments);	

	
	/**
	 * Sets the state of the metric locally and on the target collector server.
	 * @param category The category segment of the metric.
	 * @param metric The metric name.
	 * @param on If true, status is turned on. If false, it is turned off.
	 * @return true if metric was found. 
	 */
	public boolean setShutOffState(String category, String metric, boolean on);
	
	/**
	 * Returns the shutoff state of the passed metric.
	 * @param category The category segment of the metric.
	 * @param metric The metric name.
	 * @return false if the metric is not shut off. true if it is shut off.
	 */

	public boolean getShutOffState(String category, String metric);

	
	
	

}
