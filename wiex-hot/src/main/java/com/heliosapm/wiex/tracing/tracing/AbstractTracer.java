package com.heliosapm.wiex.tracing.tracing;

import gnu.trove.map.hash.TObjectIntHashMap;
import gnu.trove.map.hash.TObjectLongHashMap;
import gnu.trove.procedure.TObjectIntProcedure;
import gnu.trove.procedure.TObjectLongProcedure;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.heliosapm.wiex.jmx.dynamic.annotation.JMXAttribute;
import com.heliosapm.wiex.jmx.dynamic.annotation.JMXManagedObject;
import com.heliosapm.wiex.jmx.dynamic.annotation.JMXOperation;
import com.heliosapm.wiex.jmx.dynamic.annotation.JMXOperationParameter;
import com.heliosapm.wiex.tracing.tracing.thread.ThreadStats;
import com.heliosapm.wiex.tracing.tracing.user.UserIdLocator;
import com.heliosapm.wiex.tracing.tracing.util.Utils;



/**
 * <p>Title: AbstractTracer</p>
 * <p>Description: Helper class from which concrete tracers can extend.</p>
 * <p>For delta state tuning: The default initial capacity and load factor for longs and ints is 100 and 0.5F repsoectively.
 * These can be tuned using environmental variables or system properties (referenced in that order) using the following names:
 * <ul>
 * <li><b>Int Deltas Initial Capacity</b>:<code>wiex.tracing.deltas.int.initialcapacity</code></li> 
 * <li><b>Int Deltas Load Factor</b>:<code>wiex.tracing.deltas.int.loadfactor</code></li>
 * <li><b>Long Deltas Initial Capacity</b>:<code>wiex.tracing.deltas.long.initialcapacity</code></li> 
 * <li><b>Long Deltas Load Factor</b>:<code>wiex.tracing.deltas.long.loadfactor</code></li>
 * </ul>
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Organization: Helios Development Group</p>
 * @author Whitehead
 * @version $Revision: 1.18 $
 */
@JMXManagedObject(annotated=true, declared=true)
public abstract class AbstractTracer implements ITracer {

	/** The class hierarchy logger */
	protected static Logger log = null;
	/** A reference to the JVM's ThreaqdMXBean for thread statistics */
	protected static ThreadMXBean tmx = null;
	/** Indicates if CPU Timings are enabled for the JVM */
	protected static boolean cpuTimeEnabled = false;
	/** Indicates if CPU Contention Monitoring is enabled for the JVM */
	protected static boolean contentionEnabled = false;
	/** A thread local to hold a StringBuilder for thread safe high speed string appending */
	protected ThreadLocal<StringBuilder> buffer = new ThreadLocal<StringBuilder>();
	/** Holds the name of the UserId Locator Class Name */
	protected UserIdLocator userIdLocator = null;
	/** Pattern for parsing trace statements */
	protected Pattern pattern = null;
	
	/** Tracer task scheduler for scheduling things like reconnection to a remote service  */
	protected final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2, new ThreadFactory() {
		protected final AtomicInteger serial = new AtomicInteger();
		protected final ThreadGroup threadGroup = new ThreadGroup("AbstractTracerSchedulerThreadGroup");
		@Override
		public Thread newThread(Runnable r) {
			Thread t = new Thread(threadGroup, r, "AbstractTracerSchedulerThread#" + serial.incrementAndGet());
			t.setDaemon(true);
			return t;
		}
	});
	
	/** Delta Tracker for ints */
	protected static TObjectIntHashMap<java.lang.String> intDeltas = null;
	/** Delta Tracker for longs */
	protected static TObjectLongHashMap<java.lang.String> longDeltas = null;
	
	protected static final String INT_DELTA_CAPACITY = "wiex.tracing.deltas.int.initialcapacity";
	protected static final String INT_LOAD_FACTOR = "wiex.tracing.deltas.int.loadfactor"; 
	protected static final String LONG_DELTA_CAPACITY = "wiex.tracing.deltas.long.initialcapacity";
	protected static final String LONG_LOAD_FACTOR = "wiex.tracing.deltas.long.loadfactor"; 

	
	/**
	 * Abstract constructor. Called by default.
	 * Instantiated logger and compiles base trace parser.
	 */
	public AbstractTracer () {
		log = Logger.getLogger(getClass());
		pattern = Pattern.compile(getEscapedSegmentDelimeter());
		int initialIntDeltaCapacity = 100;
		int initialLongDeltaCapacity = 100;
		float initialIntDeltaLoadFactor = 0.5F;
		float initialLongDeltaLoadFactor = 0.5F;
		try { initialIntDeltaCapacity = Integer.parseInt(Utils.getEnvThenSystemProperty(INT_DELTA_CAPACITY)); } catch (Exception e) {}
		try { initialIntDeltaLoadFactor = Float.parseFloat(Utils.getEnvThenSystemProperty(INT_LOAD_FACTOR)); } catch (Exception e) {}
		try { initialLongDeltaCapacity = Integer.parseInt(Utils.getEnvThenSystemProperty(LONG_DELTA_CAPACITY)); } catch (Exception e) {}		
		try { initialLongDeltaLoadFactor = Float.parseFloat(Utils.getEnvThenSystemProperty(LONG_LOAD_FACTOR)); } catch (Exception e) {}
		
		
		longDeltas = new TObjectLongHashMap<java.lang.String>(initialLongDeltaCapacity, initialLongDeltaLoadFactor);
		intDeltas = new TObjectIntHashMap<java.lang.String>(initialIntDeltaCapacity, initialIntDeltaLoadFactor);
		
		
	}
	
	/**
	 * Sets the instance of the UserId locator.
	 */
	public void setUserIdLocator(UserIdLocator userIdLocator) {
		this.userIdLocator = userIdLocator;
	}
	
	/* (non-Javadoc)
	 * @see com.heliosapm.wiex.tracing.tracing.ITrace#getUserId()
	 */
	public String getUserId() {
		return userIdLocator.getUserId();
	}
	
	/**
	 * Returns the prefix for a user based metric segment.
	 * @return The user segment prefix.
	 */
	public String getUserIdPefix() {
		return "User" + this.getSegmentDelimeter();
	}
	
	

	/**
	 * Initializes the ThreadMXBean 
	 * and set the cpu timing and contention monitoring availability flags.
	 */ 
	static {
		tmx = ManagementFactory.getThreadMXBean();		
		if(tmx.isCurrentThreadCpuTimeSupported()) {
			tmx.setThreadCpuTimeEnabled(true);
			cpuTimeEnabled = true;
		} else {
			cpuTimeEnabled = false;
		}
		
		if(tmx.isThreadContentionMonitoringSupported()) {
			tmx.setThreadContentionMonitoringEnabled(true);
			contentionEnabled = true;
		} else {
			contentionEnabled = false;
		}
	}	
	
	/**
	 * Acquires and truncates the current thread's StringBuilder.
	 * @return A truncated string builder for use by the current thread.
	 */
	public StringBuilder getStringBuilder() {
		StringBuilder sb = buffer.get();
		if(sb==null) {
			sb = new StringBuilder();
			buffer.set(sb);
		}
		sb.setLength(0);
		return sb;
	}
	
	/**
	 * Acquires and truncates the current thread's StringBuilder, then appends the passed stringy
	 * @param cs The stringy to append to the truncated StringBuilder
	 * @return A truncated string builder for use by the current thread.
	 */
	public StringBuilder getStringBuilder(CharSequence cs) {
		StringBuilder sb = buffer.get();
		if(sb==null) {
			sb = new StringBuilder();
			buffer.set(sb);
		}
		sb.setLength(0);
		return cs==null ? sb : sb.append(cs);
	}
	
	

	/**
	 * Creates a concatenated segment string.
	 * Interleaves delimeters between each provided 
	 * segment and returns the completed string.
	 * If the segments are null or have a length of zero, a balnk string will be returned with no delimeter.
	 * @param segments An array of segments to interleave.
	 * @return The full segment.
	 */
	public String buildSegment(String...segments) {		
		return buildSegment(false, segments);
	}
	

	
	
	/**
	 * Creates a concatenated segment string.
	 * Interleaves delimeters between each provided 
	 * segment and returns the completed string.
	 * If the segments are null or have a length of zero, a balnk string will be returned with no delimeter.
	 * @param leaveTrailingDelim If true, the trailing delimeter will be left in. Otherwise, it will be removed.
	 * @param segments An array of segments to interleave.
	 * @return The full segment.
	 */
	public String buildSegment(boolean leaveTrailingDelim, String...segments) {
		if(segments==null || segments.length<1 ) {
			return "";			
		}
		StringBuilder sb = getStringBuilder();
		int segmentsLength = 0;
		for(String s: segments) {
			if(s==null || "".equals(s)) continue;
			sb.append(s).append(getSegmentDelimeter());
			segmentsLength+=s.length();
		}
		if(segmentsLength<1) return "";
		if(leaveTrailingDelim) return sb.toString();
		else {
			if(getSegmentDelimeter().length()>0) {
				return sb.deleteCharAt(sb.length()-1).toString();
			} else {
				return sb.toString();
			}
		}
	}
	
	/**
	 * Creates a concatenated segment string starting with the provided base.
	 * Interleaves delimeters between each provided 
	 * segment and returns the completed string.
	 * @param base The initial prefix for the segment.
	 * @param leaveTrailingDelim If true, the trailing delimeter will be left in. Otherwise, it will be removed.
	 * @param segments An array of segments to interleave.
	 * @return The full segment.
	 */
	public String buildSegment(String base, boolean leaveTrailingDelim, String...segments) {
		if(base==null || base.length()<1) return buildSegment(leaveTrailingDelim, segments);
		if(segments==null) return buildSegment(leaveTrailingDelim, base);
		else {
			String[] newSegments = new String[segments.length+1];
			newSegments[0] = base;
			System.arraycopy(segments, 0, newSegments, 1, segments.length);
			String seg  = buildSegment(leaveTrailingDelim, newSegments);
			return seg;
		}
	}	
	
	
	/**
	 * Creats a newly created and populated undiffed ThreadStats for the current thread.
	 * @return A new absolute ThreadStats for the current thread.
	 */
	public ThreadStats getThreadStatsInstance() {
		return ThreadStats.getInstance(tmx);
	}
	
	/**
	 * Creats a newly created and populated undiffed ThreadStats for the thread Id passed in.
	 * @param threadId The thread Id of the thread to generates stats for.
	 * @return A new absolute ThreadStats.
	 */
	public ThreadStats getThreadStatsInstance(long threadId) {
		return ThreadStats.getInstance(tmx, threadId);
	}
	
	/**
	 * Updates the thread stats with current values.
	 * This undiffs the ThreadStatus.
	 * @param threadStats The ThreadStats to be updated.
	 */
	public void updateThreadStats(ThreadStats threadStats) {
		threadStats.update(tmx);
	}

	/**
	 * Creates a new current ThreadStats and calcs a delta between the new and the passed
	 * and updates the passed.
	 * @param threadStats
	 */	
	public void deltaThreadStats(ThreadStats threadStats) {
		threadStats.delta(ThreadStats.getInstance(tmx));
	}
	
	/**
	 * @return the contentionEnabled
	 */
	public static boolean isContentionEnabled() {
		return contentionEnabled;
	}


	/**
	 * @return the cpuTimeEnabled
	 */
	public static boolean isCpuTimeEnabled() {
		return cpuTimeEnabled;
	}	
	
	/**
	 * A no-op to instrument externally.
	 * @param key The full resource segment name of a metric.
	 */
	public void trace(String key) {
		if(!TracerFactory.isTraceEnabled(key)) return;
		String[] segments = pattern.split(key);
		if(segments.length==0) return;
		else if(segments.length==1) _trace(segments[0]);
		else if(segments.length==2) _trace(segments[0], segments[1]);
		else if(segments.length==3) _trace(segments[0], segments[1], segments[2]);
		else if(segments.length==4) _trace(segments[0], segments[1], segments[2], segments[3]);
		else if(segments.length==5) _trace(segments[0], segments[1], segments[2], segments[3], segments[4]);
		else if(segments.length==6) _trace(segments[0], segments[1], segments[2], segments[3], segments[4], segments[5]);
		else if(segments.length==7) _trace(segments[0], segments[1], segments[2], segments[3], segments[4], segments[5], segments[6]);
		else if(segments.length==8) _trace(segments[0], segments[1], segments[2], segments[3], segments[4], segments[5], segments[6], segments[7]);
		else if(segments.length==9) _trace(segments[0], segments[1], segments[2], segments[3], segments[4], segments[5], segments[6], segments[7], segments[8]);
		else if(segments.length==10) _trace(segments[0], segments[1], segments[2], segments[3], segments[4], segments[5], segments[6], segments[7], segments[8], segments[9]);
		else if(segments.length==11) _trace(segments[0], segments[1], segments[2], segments[3], segments[4], segments[5], segments[6], segments[7], segments[8], segments[9], segments[10]);
	}
	
	protected void _trace(String a) {}
	protected void _trace(String a, String b) {}
	protected void _trace(String a, String b, String c) {}
	protected void _trace(String a, String b, String c, String d) {}
	protected void _trace(String a, String b, String c, String d, String e) {}
	protected void _trace(String a, String b, String c, String d, String e, String f) {}
	protected void _trace(String a, String b, String c, String d, String e, String f, String g) {}
	protected void _trace(String a, String b, String c, String d, String e, String f, String g, String h) {}
	protected void _trace(String a, String b, String c, String d, String e, String f, String g, String h, String i) {}
	protected void _trace(String a, String b, String c, String d, String e, String f, String g, String h, String i, String j) {}
	protected void _trace(String a, String b, String c, String d, String e, String f, String g, String h, String i, String j, String k) {}
	
	
	/**
	 * Returns the delimeter between the segments and the metric name escaped if required for regex parsing.
	 * e.g. "\\:" is the delimeter for Wily Introscope.
	 * @return The metric delimeter.
	 */	
	public String getEscapedMetricDelimeter() {
		return getMetricDelimeter();
	}
	
	/**
	 * Returns the full metric name segment delimeter escaped if required for regex parsing.
	 * e.g. "\\|" is the escaped delimeter for Wily Introscope.
	 * @return The segment delimeter.
	 */
	public String getEscapedSegmentDelimeter() {
		return "\\" + getSegmentDelimeter();
	}
	
	/**
	 * Resets a counter to zero for a long counter.
	 * @param category The metric's resource segment.
	 * @param name The metric name.
	 */
	public void resetLongCounter(String category, String name) {
		
	}

	/**
	 * Resets a counter to zero for a long counter.
	 * @param fullName The metric's fully qualified name.
	 */
	public void resetLongCounter(String fullName) {
		
	}
	
	/**
	 * Resets a counter to zero for an int counter.
	 * @param category The metric's resource segment.
	 * @param name The metric name.
	 */
	public void resetIntCounter(String category, String name) {
		
	}

	/**
	 * Resets a counter to zero for an int counter.
	 * @param fullName The metric's fully qualified name.
	 */
	public void resetIntCounter(String fullName) {
		
	}
	
	/**
	 * Sets the absolute value for an int counter.
	 * @param category The metric's resource segment.
	 * @param name The metric name.
	 * @param value The value to set the counter to.
	 */
	public void setIntCounter(String category, String name, int value) {
		
	}
	
	/**
	 * Sets the absolute value for an int counter.
	 * @param fullName The metric's fully qualified name.
	 * @param value The value to set the counter to.
	 */	
	public void setIntCounter(String fullName, int value) {
		
	}
	
	/**
	 * Sets the absolute value for a long counter.
	 * @param category The metric's resource segment.
	 * @param name The metric name.
	 * @param value The value to set the counter to.
	 */
	public void setLongCounter(String category, String name, long value) {
		
	}
	
	/**
	 * Sets the absolute value for a long counter.
	 * @param fullName The metric's fully qualified name.
	 * @param value The value to set the counter to.
	 */	
	public void setLongCounter(String fullName, long value) {
		
	}
	
	/**
	 * Reinitializes the metric.
	 * @param category The metric category.
	 * @param name The metric name.
	 */
	public void reinitializeMetric(String category, String name) {
		 
	}
	
	/**
	 * Records a single incident of a metric for the current sampling interval.
	 * @param fullMetricName The full metric name to record the incident for.
	 */
	public void recordIntervalIncident(String fullMetricName) {
		
	}
	
	/**
	 * Records a number of incidences of a metric for the current sampling interval.
	 * @param fullMetricName The full metric name to record the incidences for.
	 * @param incidents The number of incidences to record.
	 */
	public void recordIntervalIncident(String fullMetricName, int incidents) {
		
	}
	
	/**
	 * Records a single incident of a metric for the current sampling interval.
	 * @param segment The metric segment
	 * @param metricName The metric name 
	 */
	public void recordIntervalIncident(String segment, String metricName) {
		
	}
	
	/**
	 * Records a number of incidences of a metric for the current sampling interval.
	 * @param segment The metric segment
	 * @param metricName The metric name 
	 * @param incidents The number of incidences to record.
	 */
	public void recordIntervalIncident(String segment, String metricName, int incidents) {
		
	}


	
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
	public void recordIntervalReset(String segment, String metric, long value, long frequency, TimeUnit timeUnit) {
		TracerFactory.recordIntervalReset(segment, metric, value, frequency, timeUnit);
	}

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
	@JMXOperation(description="Schedules a regular reset to zero of an interval counter.", name="RegisterIntervalReset")
	public void recordIntervalReset(
			@JMXOperationParameter(description="The segment name", name="SegmentName") String segment, 
			@JMXOperationParameter(description="The metric name", name="MetricName") String metric, 
			@JMXOperationParameter(description="The value to increment the counter by", name="Value") long value, 
			@JMXOperationParameter(description="The frequency of the reset", name="Frequency") long frequency, 
			@JMXOperationParameter(description="The unit of the frequency of the reset(NANOSECONDS,MICROSECONDS,MILLISECONDS,SECONDS)", name="Unit") String timeUnit) {
		TracerFactory.recordIntervalReset(segment, metric, value, frequency, TimeUnit.valueOf(timeUnit));
	}
	
	/**
	 * If a reset timer is active for the passed metric, it is cancelled.
	 * @param segment
	 * @param metric
	 */
	@JMXOperation(description="Cancels a reset timer", name="CancelIntervalReset")
	public void cancelIntervalReset(			
			@JMXOperationParameter(description="The segment name", name="SegmentName") String segment, 
			@JMXOperationParameter(description="The metric name", name="MetricName") String metric) {
		TracerFactory.cancelIntervalReset(segment, metric);		
	}
	
	/**
	 * Returns true if there is an active reset time for the passed metric.
	 * @param segment
	 * @param metric
	 * @return true if a reset time is active.
	 */
	@JMXOperation(description="Returns true if there is a rest timer active for the passed metric.", name="IsResetActive")
	public boolean isResetActive(			
			@JMXOperationParameter(description="The segment name", name="SegmentName") String segment, 
			@JMXOperationParameter(description="The metric name", name="MetricName") String metric) {
		return TracerFactory.isResetActive(segment, metric);
	}

	
	
	/**
	 * Records the delta of the passed value and the previosuly recorded value as a "sticky" counter.
	 * If there is no previous value, the value is stored in state, but no trace is recorded.
	 * @param segment The segment name.
	 * @param metricName The metric name
	 * @param value The new valoue to calculate the delta from.
	 */
	@JMXOperation(description="Records the delta of the passed value and the previosuly recorded value as a sticky counter.", name="RecordIntCounterMetricDelta")
	public void recordCounterMetricDelta(String segment, String metricName, int value) {
		int d = deltaInt(segment, metricName, value);
		if(d>=0) {
			recordCounterMetric(segment, metricName, d);
		}
	}
	
	/**
	 * Adds the passed value to the delta of the passed value to the current sticky counter recorded value.
	 * @param segment The segment name.
	 * @param metricName The metric name
	 * @param value The value to add to the current stcky delta
	 */
	@JMXOperation(description="Adds the passed value to the delta of the passed value to the current sticky counter recorded value.", name="RecordIntCounterMetricDeltaAdd")
	public void recordCounterMetricDeltaAdd(String segment, String metricName, int value) {
		int d = deltaInt(segment, metricName, value);
		if(d>=0) {
			recordCounterMetricAdd(segment, metricName, d);
		}
	}
	
	/**
	 * Adds the passed value to the delta of the passed value to the current sticky counter recorded value.
	 * @param segment The segment name.
	 * @param metricName The metric name
	 * @param value The value to add to the current stcky delta
	 */
	@JMXOperation(description="Adds the passed value to the delta of the passed value to the current sticky counter recorded value.", name="RecordIntCounterMetricDeltaAdd")
	public void recordCounterMetricDeltaAdd(String segment, String metricName, long value) {
		long d = deltaLong(segment, metricName, value);
		if(d>=0) {
			recordCounterMetricAdd(segment, metricName, d);
		}
	}
	
	
	
	
	
	/**
	 * Records the delta of the passed value and the previosuly recorded value as a "sticky" counter.
	 * If there is no previous value, the value is stored in state, but no trace is recorded.
	 * @param segment The segment name.
	 * @param metricName The metric name
	 * @param value The new valoue to calculate the delta from.
	 */	
	@JMXOperation(description="Records the delta of the passed value and the previosuly recorded value as a sticky counter.", name="RecordLongCounterMetricDelta")
	public void recordCounterMetricDelta(String segment, String metricName, long value) {
		long d = deltaLong(segment, metricName, value);
		if(d>=0) {
			recordCounterMetric(segment, metricName, d);
		}		
	}
	
	/**
	 * Records the delta of the passed value and the previosuly recorded value as an interval averaged counter.
	 * If there is no previous value, the value is stored in state, but no trace is recorded.
	 * @param segment The segment name.
	 * @param metricName The metric name
	 * @param value The new valoue to calculate the delta from.
	 */
	@JMXOperation(description="Records the delta of the passed value and the previosuly recorded value as a sticky counter.", name="RecordIntMetricDelta")
	public void recordMetricDelta(String segment, String metricName, int value) {
		int d = deltaInt(segment, metricName, value);
		if(d>=0) {
			recordMetric(segment, metricName, d);
		}		
	}
	
	/**
	 * Records the delta of the passed value and the previosuly recorded value as an interval averaged counter.
	 * If there is no previous value, the value is stored in state, but no trace is recorded.
	 * @param segment The segment name.
	 * @param metricName The metric name
	 * @param value The new valoue to calculate the delta from.
	 */
	@JMXOperation(description="Records the delta of the passed value and the previosuly recorded value as a sticky counter.", name="RecordLongMetricDelta")	
	public void recordMetricDelta(String segment, String metricName, long value) {
		long d = deltaLong(segment, metricName, value);
		if(d>=0) {
			recordMetric(segment, metricName, d);
		}				
	}
			
	/**
	 * Determines the delta of the passed value for the passed keys against the value in state and stores the passed value in state.
	 * If no value is held in state, returns -Integer.MAX_VALUE.
	 * @param segment The segment name
	 * @param metric The metric name
	 * @param value The new int value
	 * @return The delta of the passed value against the value in state, or -Integer.MAX_VALUE.
	 */
	@JMXOperation(description="Determines the delta of the passed value for the passed keys against the value in state and stores the passed value in state.", name="Delta int Value")
	public synchronized int deltaInt(
			@JMXOperationParameter(description="The segment name", name="SegmentName") String segment, 
			@JMXOperationParameter(description="The metric name", name="MetricName") String metric, 
			@JMXOperationParameter(description="The new int value", name="NewInt") int value
			) {
		String key = new StringBuilder(segment).append(":").append(metric).toString();
		if(intDeltas.containsKey(key)) {
			int d = value-intDeltas.get(key);
			intDeltas.put(key, value);
			return d;
		} else {
			intDeltas.put(key, value);
			return -Integer.MAX_VALUE;
		}
	}
	
	/**
	 * Determines the delta of the passed value for the passed keys against the value in state and stores the passed value in state.
	 * If no value is held in state, returns -Long.MAX_VALUE.
	 * @param segment The segment name
	 * @param metric The metric name
	 * @param value The new long value
	 * @return The delta of the passed value against the value in state, or -Long.MAX_VALUE.
	 */
	@JMXOperation(description="Determines the delta of the passed value for the passed keys against the value in state and stores the passed value in state.", name="Delta long Value")
	public synchronized long deltaLong(
			@JMXOperationParameter(description="The segment name", name="SegmentName") String segment, 
			@JMXOperationParameter(description="The metric name", name="MetricName") String metric, 
			@JMXOperationParameter(description="The new long value", name="NewLong") long value			
			) {
		String key = new StringBuilder(segment).append(":").append(metric).toString();
		if(longDeltas.containsKey(key)) {
			long d = value-longDeltas.get(key);
			longDeltas.put(key, value);
			return d;
		} else {
			longDeltas.put(key, value);
			return -Long.MAX_VALUE;
		}
	}
	
	/**
	 * Returns the number of ints held in state for deltas.
	 * @return The size of the intDeltas.
	 */
	@JMXAttribute(description="The number of ints held in state for deltas.", name="IntDeltaStateSize")
	public int getIntDeltaSize() {
		return intDeltas.size();
	}
	
	/**
	 * Returns the number of ints held in state for deltas.
	 * @return The size of the longDeltas.
	 */
	@JMXAttribute(description="The number of longs held in state for deltas.", name="longDeltaSize")
	public int getLongDeltaSize() {
		return longDeltas.size();
	}
	
	
	
	/**
     * Returns a string representation of the delta maps.
	 * @return A formated string report.
	 */
	@JMXOperation(description="Returns a string representation of the delta maps.", name="printDeltas")
	public String printDeltas() {
		StringBuilder buff = new StringBuilder("Delta Report\n\tInt Deltas");
		IntDeltaSummarizer iSummary = new IntDeltaSummarizer(buff);
		intDeltas.forEachEntry(iSummary);
		buff.append("\n\tLong Deltas");
		LongDeltaSummarizer lSummary = new LongDeltaSummarizer(buff);
		longDeltas.forEachEntry(lSummary);
		return buff.toString(); 
	}
	
	
	/**
	 * Clears the Deltas State.
	 */
	@JMXOperation(description="Clears the Deltas State.", name="clearDeltas")
	public void clearDeltas() {
		longDeltas.clear();
		intDeltas.clear();
	}

	/**
	 * Compacts the Deltas
	 */
	@JMXOperation(description="Compacts the Deltas State.", name="compactDeltas")
	public void compactDeltas() {
		longDeltas.compact();
		intDeltas.compact();
	}
	
	/**
	 * Sets the state of the metric locally and on the target collector server.
	 * @param category The category segment of the metric.
	 * @param metric The metric name.
	 * @param on If true, status is turned on. If false, it is turned off.
	 * @return true if metric was found.  
	 */
	@JMXOperation(description="Sets the state of the metric locally and on the target collector server.", name="setShutOffState")
	public boolean setShutOffState(
			@JMXOperationParameter(description="The category segment of the metric.", name="category") String category, 
			@JMXOperationParameter(description="The metric name.", name="metric") String metric, 
			@JMXOperationParameter(description="If true, status is turned on. If false, it is turned off.", name="on") boolean on) {
		return false;
	}
	
	/**
	 * Returns the shutoff state of the passed metric.
	 * @param category The category segment of the metric.
	 * @param metric The metric name.
	 * @return false if the metric is not shut off. true if it is shut off.
	 */
	@JMXOperation(description="Returns the shutoff state of the passed metric.", name="getShutOffState")
	public boolean getShutOffState(
			@JMXOperationParameter(description="The category segment of the metric.", name="category") String category, 
			@JMXOperationParameter(description="The metric name.", name="metric") String metric){
		return false;
	}
	
	
	

}

/**
 * <p>Title: IntDeltaSummarizer</p>
 * <p>Description: Helper to create a Delta Summary Report for Ints</p> 
 * <p>Copyright: Copyright (c) 2007</p>
 * <p>Organization: Helios Development Group</p>
 * @author Whitehead
 * @version $Revision: 1.18 $
 */
class IntDeltaSummarizer implements TObjectIntProcedure<String> {
	private StringBuilder buffer = null; 

	/**
	 * Constructs a new IntDeltaSummarizer
	 * @param buffer
	 */
	public IntDeltaSummarizer(StringBuilder buffer) {
		this.buffer = buffer;
	}
	/**
	 * @return the buffer
	 */
	public StringBuilder getBuffer() {
		return buffer;
	}
	/**
	 * Executes this procedure. A false return value indicates that the application executing this procedure should not invoke this procedure again.
	 * @param key
	 * @param value
	 * @return
	 */
	public boolean execute(String key, int value) {
		buffer.append("\n\t\t").append(key).append("\t:\t").append(value);
		return true;
	}
	
}

/**
 * <p>Title: LongDeltaSummarizer</p>
 * <p>Description: Helper to create a Delta Summary Report for Longs</p> 
 * <p>Copyright: Copyright (c) 2007</p>
 * <p>Organization: Helios Development Group</p>
 * @author Whitehead
 * @version $Revision: 1.18 $
 */
class LongDeltaSummarizer implements TObjectLongProcedure<String> {
	private StringBuilder buffer = null; 

	/**
	 * Constructs a new LongDeltaSummarizer
	 * @param buffer
	 */
	public LongDeltaSummarizer(StringBuilder buffer) {
		this.buffer = buffer;
	}
	/**
	 * @return the buffer
	 */
	public StringBuilder getBuffer() {
		return buffer;
	}
	/**
	 * Executes this procedure. A false return value indicates that the application executing this procedure should not invoke this procedure again.
	 * @param key
	 * @param value
	 * @return
	 */
	public boolean execute(String key, long value) {
		buffer.append("\n\t\t").append(key).append("\t:\t").append(value);
		return true;
	}
	
}
