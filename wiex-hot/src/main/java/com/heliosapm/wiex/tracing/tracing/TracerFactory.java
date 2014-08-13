package com.heliosapm.wiex.tracing.tracing;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.log4j.Logger;

import com.heliosapm.wiex.jmx.dynamic.ManagedObjectDynamicMBean;
import com.heliosapm.wiex.jmx.dynamic.annotation.JMXAttribute;
import com.heliosapm.wiex.jmx.dynamic.annotation.JMXManagedObject;
import com.heliosapm.wiex.jmx.dynamic.annotation.JMXOperation;
import com.heliosapm.wiex.jmx.dynamic.annotation.JMXOperationParameter;
import com.heliosapm.wiex.jmx.util.MBeanServerHelper;
import com.heliosapm.wiex.tracing.tracing.comparators.DescendingLongComparator;
import com.heliosapm.wiex.tracing.tracing.user.NullUserIdLocator;
import com.heliosapm.wiex.tracing.tracing.user.UserIdLocator;


/**
 * <p>Title: TracerFactory</p>
 * <p>Description: Factory for instantiating and caching instancers of configured tracers.</p> 
 * <p>The type of tracer and userIdLocator are defined by class names set in system properties as follows:
 * <ul>
 * <li>Tracer: <b>wiex.tracer.class.name</b>
 * <li>Default: <code>com.heliosapm.wiex.tracing.tracing.NullTracer</code> 
 * <li>UserIdLocator: <b>wiex.userid.locator.class.name</b>
 * <li>Default: <code>com.heliosapm.wiex.tracing.tracing.user.NullUserIdLocator</code>
 * </ul></p>
 * <p>If the system property <code>wiex.tracer.jmx.domain</code> is set, the factory will attempt to register itself as an MBean
 * with the MBeanServer at the domain defined by the property.</p>
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Organization: Helios Development Group</p>
 * @author Whitehead
 * @version $Revision: 1.22 $
 * @see com.heliosapm.wiex.tracing.tracing.NullTracer
 * @see com.heliosapm.wiex.tracing.tracing.user.NullUserIdLocator
 */


@JMXManagedObject(annotated=true, declared=true)
public class TracerFactory   {
	
	protected static Logger log = Logger.getLogger(TracerFactory.class);
	
	public static final String VERSION = "@WIEX.TRACING.VERSION@";
	
	public static final String WIEX_TRACER_CLASS_NAME = "wiex.tracer.class.name";
	public static final String WIEX_USERID_LOCATOR_CLASS_NAME = "wiex.userid.locator.class.name";
	public static final String WIEX_DEFAULT_TRACER_CLASS_NAME = com.heliosapm.wiex.tracing.tracing.NullTracer.class.getName();
	public static final String WIEX_DEFAULT_USERID_LOCATOR_CLASS_NAME = com.heliosapm.wiex.tracing.tracing.user.NullUserIdLocator.class.getName();
	public static final String JMX_DOMAIN_REGISTRATION = "wiex.jmx.domain";
	public static final String BOOTSTRAP_PREFIX = "WIEXTRACING.";
	public static final String USE_NAME_LOOKUP_CACHE = "wiex.tracer.usenamelookupcache";
	
	
	protected static String tracerClassName = null;
	protected static String userIdLocatorClassName = null;
	protected static String jmxDomain = null;
	
	protected static volatile ITracer tracer = null;
	protected static UserIdLocator userIdLocator = null;
	protected static boolean listenOnChange = false;
	protected static boolean useNameLookupCache = false;
	
	protected static ConcurrentHashMap<String, String> tracerLoggers = new ConcurrentHashMap<String, String>(new HashMap<String, String>(10000, .6F)); 
	
	
	public static ObjectHierarchyTree<Boolean> tracingState = null;
	
	/** Average Lookup Time */
	protected static AtomicLong averageLookupTime = new AtomicLong(0L);
	/** one reading taken */
	protected static boolean oneReading = false;
	
	protected static Timer resetTimer = null;
	
	
	protected static long tracersRequested = 0;
	protected static Object lock = new Object();
	protected static Map<String, ResetTimerTask> resetNames = new ConcurrentHashMap<String, ResetTimerTask>();
	
	static {
		
		tracerClassName = System.getProperty(WIEX_TRACER_CLASS_NAME, WIEX_DEFAULT_TRACER_CLASS_NAME);
		userIdLocatorClassName = System.getProperty(WIEX_USERID_LOCATOR_CLASS_NAME, WIEX_DEFAULT_USERID_LOCATOR_CLASS_NAME);
		jmxDomain = System.getProperty(JMX_DOMAIN_REGISTRATION, "None");
		String s = System.getProperty(USE_NAME_LOOKUP_CACHE, "false");
		useNameLookupCache = s.equalsIgnoreCase("true");
		log.info("Version:" + VERSION);
	}
	
	/**
	 * Reads the system properties and sets the name space verbosity for any property prefixed with <code>BOOTSTRAP_PREFIX</code>.
	 */
	protected static void bootStrapVerbosity() {
		Properties p = System.getProperties();
		String name = null;
		String value = null;
		for(Entry<Object, Object> entry : p.entrySet()) {
			name = entry.getKey().toString();
			value = entry.getValue().toString();
			if(name.toUpperCase().startsWith(BOOTSTRAP_PREFIX)) {				
				setCategoryTracing(name.substring(BOOTSTRAP_PREFIX.length()), value.equalsIgnoreCase("true"));
			}
		}
	}
	
	/**
	 * If the reset timer is null, it is created and started.
	 */
	protected static void checkResetTimer() {
		if(resetTimer==null) {
			synchronized(lock) {
				if(resetTimer==null) {
					resetTimer = new Timer("WIEXTracing Reset Timer", true);
				}
			}
		}
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
	public static void recordIntervalReset(String segment, String metric, long value, long frequency, TimeUnit timeUnit) {
		checkResetTimer();
		ITracer tracer = getInstance();
		String metricName = tracer.buildSegment(segment, metric);
		boolean timed = resetNames.containsKey(metricName);
		if(!timed) {
			ResetTimerTask rtt = new ResetTimerTask(getInstance(), segment, metric, frequency, timeUnit);
			resetNames.put(metricName, rtt);			
			long millis = TimeUnit.MILLISECONDS.convert(frequency, timeUnit); 
			resetTimer.schedule(rtt, millis, millis);
		} else {
			if(log.isDebugEnabled())log.debug("Metric [" + metricName + "] already scheduled for reset");
		}		
		tracer.recordCounterMetricAdd(segment, metric, value);
	}
	
	/**
	 * If a reset timer is active for the passed metric, it is cancelled. 
	 * @param segment
	 * @param metric
	 */
	@JMXOperation(description="Cancels the reset timer for the passed metric.", name="CancelIntervalReset")
	public static void cancelIntervalReset(String segment, String metric) {
		ITracer tracer = getInstance();
		String metricName = tracer.buildSegment(segment, metric);		
		if(isResetActive(segment,metric)) {
			ResetTimerTask rtt = resetNames.remove(metricName);
			if(rtt!=null) {
				rtt.cancel();
			}						
		}
	}
	
	/**
	 * Returns true if there is an active reset time for the passed metric.
	 * @param segment
	 * @param metric
	 * @return true if a reset time is active.
	 */
	
	public static boolean isResetActive(String segment, String metric) {
		ITracer tracer = getInstance();
		String metricName = tracer.buildSegment(segment, metric);
		return resetNames.containsKey(metricName);
	}
	
	
	/**
	 * The number of reset timers active.
	 * @return
	 */
	@JMXAttribute(description="The number of reset timers active.", name="ResetTimerCount")
	public static int getResetTimerCount() {
		return resetNames.size();
	}
	
	/**
	 * Prints a report of the currently active reset timers.
	 * @return An HTML String.
	 */
	@JMXOperation(description="Prints a report of the currently active reset timers.", name="ResetTimerReport")
	public static String printResetTimerReport() {
		StringBuilder buff = new StringBuilder("<table border=\"1\">");
		buff.append("<TR><TH>Segment</TH><TH>Metric</TH><TH>Frequency</TH><TH>Unit</TH><TH>Fire Count</TH><TH>Execution Time</TH></TR>");
		for(ResetTimerTask rtt: resetNames.values()) {
			buff.append("<TR><TD>").append(rtt.getSegment()).append("</TD>");
			buff.append("<TD>").append(rtt.getMetric()).append("</TD>");
			buff.append("<TD>").append(rtt.getFrequency()).append("</TD>");
			buff.append("<TD>").append(rtt.getTimeUnit()).append("</TD>");
			buff.append("<TD>").append(rtt.getFireCount()).append("</TD>");
			buff.append("<TD>").append(new Date(rtt.scheduledExecutionTime())).append("</TD></TR>");
		}
		buff.append("</TABLE>");
		return buff.toString();
	}
	

	
	/**
	 * Initializes the configured tracer.
	 */
	protected static synchronized void initTracer() {
		if(tracer!=null) return;
		ClassLoader currentThreadClassLoader = null;
		try {
			currentThreadClassLoader = Thread.currentThread().getContextClassLoader();
			Thread.currentThread().setContextClassLoader(ITracer.class.getClassLoader());
			try {
				tracer = (ITracer)Class.forName(tracerClassName).newInstance();
				tracingState = new ObjectHierarchyTree<Boolean>("\\.", "*", Boolean.TRUE, Boolean.FALSE);
				try {
					userIdLocator = (UserIdLocator)Class.forName(userIdLocatorClassName).newInstance();
				} catch (Throwable e) {
					log.error("Exception Instantiating Configured UserIdLocator:" + userIdLocatorClassName, e);
					userIdLocator = new NullUserIdLocator();
				}
				tracer.setUserIdLocator(userIdLocator);
			} catch (Exception e) {
				log.error("Throwable Instantiating Configured Tracer:" + tracerClassName, e);
				tracer = new NullTracer();
				tracer.setUserIdLocator(new NullUserIdLocator());
			}
			if(!jmxDomain.equalsIgnoreCase("None")) {
				registerMBean();
			}
		} finally {
			Thread.currentThread().setContextClassLoader(currentThreadClassLoader);
		}		
	}
	
	
	
	
	/**
	 * Registers the TracerFactoryMBean with the configured MBeanServer. 
	 */
	protected static void registerMBean() {
		try {
			MBeanServer server = MBeanServerHelper.getMBeanServer(jmxDomain);
			Object instance = new TracerFactory();
			int classLoaderHash = instance.getClass().getClassLoader().toString().hashCode();
			ObjectName on = new ObjectName("com.heliosapm.wiex.tracing:service=WIEXTracing,classLoader=" + classLoaderHash);
			ManagedObjectDynamicMBean mbean = new ManagedObjectDynamicMBean(getInstance(), new TracerFactory()); 
			server.registerMBean(mbean, on);
		} catch (Exception e) {
			log.error("Failed to Register MBean", e);
		}
	}
	
	/**
	 * The size of the tracing logger cache.
	 * @return The tracing logger cache size.
	 */
	@JMXAttribute(description="The size of the tracing logger cache.", name="Tracing Logger Count")
	public static int getTracingLoggerCount() {
		return tracingState.getTreeSize();
	}
	
	/**
	 * Prints a tracer logger status list.
	 * @return A simple format report of WIEXTracing tracing loggers.
	 */
	@JMXOperation(description="A simple format report of WIEXTracing tracing loggers.", name="Tracing Logger Report")
	public static String printTraceLoggerReport() {
		return tracingState.toString();
	}
	
	@JMXOperation(description="Resets the tracing verbosity back to boostrap levels", name="Tracing Verbosity Reset")
	public static void resetVerbosity() {
		tracingState.clear();
		bootStrapVerbosity();
	}
	
	
	
	
	
	
	/**
	 * Sets the enabled state of the category's tracing logger.
	 * @param category The category to set.
	 * @param enabled true if tracing should be enabled. if false, the logger is set to OFF.
	 * @return A string describing the state of the tracing logger.
	 */
	@JMXOperation(description="Sets the enabled state of the category tracing logger.", name="setCategoryTracing")
	public static String setCategoryTracing(
			@JMXOperationParameter(description="The full metric name", name="FullMetricName")String category, 
			@JMXOperationParameter(description="True for enabled, False for disabled", name="Enabled")boolean enabled) {
		
		String s = getTracerLogger(category);
		tracingState.setMember(s, Boolean.valueOf(enabled));
		return s + ":" + enabled;
		
	}
	
	/**
	 * Determines if trace is enabled for the passed category.
	 * @param tracerCategory
	 * @return true if trace is enabled.
	 */
	@JMXOperation(description="Determines if a tracing category is enabled.", name="TraceEnabled")
	public static boolean isTraceEnabled(@JMXOperationParameter(description="The full metric name", name="FullMetricName") String tracerCategory) {
		long start = System.currentTimeMillis();		
		boolean b = tracingState.getValue(getTracerLogger(tracerCategory));
		long elapsed = System.currentTimeMillis()-start;
		if(!oneReading) {
			oneReading = true;
			averageLookupTime.set(elapsed);
		} else {
			long currentAvg = averageLookupTime.get();			
			if((currentAvg + elapsed)==0L) {
				averageLookupTime.set(0L);
			} else {
				averageLookupTime.set((currentAvg + elapsed)/2);
			}						
		}		
		return b;
	}
	
	/**
	 * Looks up the tracing logger name for the passed category.
	 * If it is not found, it is created and cached.
	 * @param tracerCategory The category.
	 * @return The tracing logger name.
	 */
	@JMXOperation(description="Gets the tracing logger for the passed category.", name="GetTraceLogger")
	public static String getTracerLogger(@JMXOperationParameter(description="The full metric name", name="FullMetricName") String tracerCategory) {
		if(useNameLookupCache) {
			String tracerLoggerName = tracerLoggers.get(tracerCategory);
			if(tracerLoggerName!=null) return tracerLoggerName;
			else {
				if(tracer==null) tracer = getInstance();
				tracerLoggerName = tracer.getStringBuilder().append(tracerCategory.replace(tracer.getSegmentDelimeter(), ".").replace(tracer.getMetricDelimeter(), ".")).toString();
				tracerLoggers.put(tracerCategory, tracerLoggerName);			
			}
			return tracerLoggerName;
		} else {
			return tracer.getStringBuilder().append(tracerCategory.replace(tracer.getSegmentDelimeter(), ".").replace(tracer.getMetricDelimeter(), ".")).toString();
		}
	}
	
	/**
	 * Convenience private constructor.
	 */
	private TracerFactory() {
		
	}
	
	/**
	 * No arg Main. Prints out the library version.
	 * @param args
	 */
	public static void main(String args[]) {
		System.out.println("Version:" + VERSION);
	}
	
	/**
	 * Returns the singleton instance of the configured tracer.
	 * If either the tracer or the tracer's userIdLocator fail to instantiate, the defaults will be constructed.
	 * @return A singleton instance of an ITracer.
	 */
	@JMXAttribute(description="The currently configured ITracer", name="ITracer")
	public static ITracer getInstance() {		
		tracersRequested++; if(tracersRequested==Long.MAX_VALUE) tracersRequested = 0;		
		if(tracer==null) {
			synchronized(lock) {
				if(tracer==null) {
					initTracer();
					bootStrapVerbosity();
					
				}
			}
		}
		return tracer;
	}

	/**
	 * @return the tracerClassName
	 */
	@JMXAttribute(description="The currently configured Tracer Class Name", name="Tracer Class Name")
	public static String getTracerClassName() {
		return tracerClassName;
	}

	/**
	 * @return the userIdLocatorClassName
	 */
	@JMXAttribute(description="The currently configured UserId Locator Class Name", name="UserId Locator Class Name")
	public static String getUserIdLocatorClassName() {
		return userIdLocatorClassName;
	}

	/**
	 * Returns the version of the WIEXTracing library.
	 * @return The WIEXTracing version.
	 */
	@JMXAttribute(description="The version of WIEXTracing", name="WIEXTracing Version")
	public String getWIEXTracingVersion() {		
		return VERSION;
	}

	/**
	 * Returns the tracing class configured.
	 * @return The WIEXTracing tracing class.
	 */
	public String getTracerClass() {
		return tracerClassName;
	}

	/**
	 * Returns the number of times a tracer has been requested.
	 * @return The number of times a tracer has been requested.
	 */	
	@JMXAttribute(description="The number of times a tracer has been requested", name="Tracers Requested")
	public long getTracersRequested() {
		return tracersRequested;
	}
	
	/**
	 * Returns the average lookup time for verbosity enabled inquiries.
	 * @return The average verbosity inquiry lookup time.
	 */
	@JMXAttribute(description="The average verbosity inquiry lookup time.", name="Verbosity Average Lookup Time")
	public long getAverageVerbosityLookupTime() {
		return averageLookupTime.get();
	}
	
	/**
	 * Returns the average lookup time for the hierarchy tree in milliseconds.
	 * @return The average lookup time.
	 */
	@JMXAttribute(description="The average lookup time for hierarchy lookups.", name="Hierarchy Average Lookup Time")
	public long getHierarchyLookupTime() {
		return tracingState.getAverageLookupTime();
	}
	

	
	/**
	 * Returns the size of the logger lookup cache.
	 * @return The size of the logger lookup cache.
	 */
	@JMXAttribute(description="The size of the logger lookup cache.", name="Logger Lookup Cache Size")
	public int getTracerLogCacheSize() {
		return tracerLoggers.size(); 
	}
	
	/**
	 * Sets the tracer factory to use the name lookup cache
	 * @param b true makes the factory use the lookup cache.
	 */
	@JMXAttribute(description="Use the name lookup cache", name="Use Name Lookup Cache")
	public void setUseNameLookupCache(boolean b) {
		useNameLookupCache = b;
	}
	
	/**
	 * @return Is the name lookup cache in use.
	 */
	public boolean getUseNameLookupCache() {
		return useNameLookupCache;
	}

	/**
	 * Returns the userIdLocator class configured.
	 * @return The WIEXTracing userIdLocator class.
	 */
	public String getUserIdLocatorClass() {
		return userIdLocatorClassName;
	}
	

	/**
	 * Generates a report of the top <code>topThreadCount</code> threads in CPU time.
	 * @param sampleTime The sampling time.
	 * @param topThreadCount The number of top threads to report for.
	 * @param maxStackDepth The maximum depth of the thread's execution stack. (-1 is MAX)
	 * @return A thread status report.
	 */
	@JMXOperation(description="Reports the stack and details of the hungriest threads", name="Hungry Thread Report")
	public String hungryThreadReport(
			@JMXOperationParameter(description="The sample time (in ms) for the report.", name="SampleTime") long sampleTime, 
			@JMXOperationParameter(description="The top n number of threads to report for.", name="TopNThreads")int topThreadCount, 
			@JMXOperationParameter(description="The Maximum Depth of the Thread Stack Traces. (-1 is MAX)", name="StackDepth")int maxStackDepth) {
		StringBuilder buff = new StringBuilder();
		
		ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
		long[] allThreads = threadMXBean.getAllThreadIds();
		Map<Long, Long> startTime = new HashMap<Long, Long>(allThreads.length);
		Map<Long, Long> elapsedTime = new TreeMap<Long, Long>(new DescendingLongComparator());
		long time = 0L;
		long time2 = 0L;
		// Collect starting CPU time
		for(long l: allThreads) {
			time = threadMXBean.getThreadCpuTime(l);
			startTime.put(l, (time==-1) ? 0 : time);
		}
		// Sleep
		try {
			Thread.sleep(sampleTime);
		} catch (InterruptedException e) {
			return "Sample Time Error:" + e.getMessage();
		}
		for(long l: allThreads) {
			time2 = threadMXBean.getThreadCpuTime(l);
			if(time2 == -1) time2 = 0;
			if(!startTime.containsKey(l)) continue;
			time = startTime.get(l);			
			elapsedTime.put(time2-time, l);
		}
		int topThread = 1;
		ThreadInfo threadInfo = null;
		for(Entry<Long, Long> entry: elapsedTime.entrySet()) {
			long cpuTime = entry.getKey();
			long threadId = entry.getValue();
			threadInfo = threadMXBean.getThreadInfo(threadId, (maxStackDepth==-1) ? Integer.MAX_VALUE : maxStackDepth);
			if(threadInfo==null) continue;
			String waitingOnLock = threadInfo.getLockName();
			String lockOwner = threadInfo.getLockOwnerName();
			long lockHolderThreadId = threadInfo.getLockOwnerId();
			buff.append("Thread CPU Utilization Order:").append(topThread).append("\n");
			buff.append("\tThread ID:").append(threadId).append("\n");
			buff.append("\tThread CPU Time:").append(cpuTime).append(" ns.\n");
			buff.append("\tThread Name:").append(threadInfo.getThreadName()).append("\n");
			buff.append("\tThread State:").append(threadInfo.getThreadState()).append("\n");
			if(waitingOnLock!=null) {
				buff.append("\tWaiting On:").append(waitingOnLock).append("\n");
				buff.append("\t\tLockOwner:").append(lockOwner).append("\n");
				buff.append("\t\tLock Thread Id:").append(lockHolderThreadId).append("\n");
			}
			StackTraceElement[] stack = threadInfo.getStackTrace();
			buff.append("\tThread Stack:\n");
			for(StackTraceElement element: stack) {
				buff.append("\t\t");
				buff.append(element.toString());
				buff.append("\n");
			}
			buff.append("\n======================================================\n");
			topThread++;
			if(topThread>topThreadCount) break;
		}
		
		return buff.toString();
	}
	
	
	/**
	 * Generates a status report of the thread.
	 * @param threadId The thread to generate a report for.
	 * @param maxStackDepth The maximum depth of the thread's execution stack. (-1 is MAX)
	 * @return A status report on the specified thread.
	 */
	@JMXOperation(description="Reports the status of the specified thread.", name="Thread Status Report")
	public String reportTheadStatus(
			@JMXOperationParameter(description="The thread Id to report for.", name="ThreadId") long threadId, 
			@JMXOperationParameter(description="The Maximum Depth of the Thread Stack Traces. (-1 is MAX)", name="StackDepth")int maxStackDepth) {
		StringBuilder buff = new StringBuilder("Status Report for Thread ID:");
		buff.append(threadId);
		buff.append("\n");
		ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
		ThreadInfo threadInfo = threadMXBean.getThreadInfo(threadId, (maxStackDepth==-1) ? Integer.MAX_VALUE : maxStackDepth);
		if(threadInfo==null) return "Thread does not exist";
		String waitingOnLock = threadInfo.getLockName();
		String lockOwner = threadInfo.getLockOwnerName();
		long lockHolderThreadId = threadInfo.getLockOwnerId();
		buff.append("\tThread ID:").append(threadId).append("\n");
		buff.append("\tThread Name:").append(threadInfo.getThreadName()).append("\n");
		buff.append("\tThread CPU Time:").append(threadMXBean.getThreadCpuTime(threadId)).append(" ns.\n");
		buff.append("\tThread User CPU Time:").append(threadMXBean.getThreadUserTime(threadId)).append(" ns.\n");
		buff.append("\tThread Wait Time:").append(threadInfo.getWaitedTime()).append(" ms.\n");
		buff.append("\tThread Wait Count:").append(threadInfo.getWaitedCount()).append("\n");
		buff.append("\tThread Block Time:").append(threadInfo.getBlockedTime()).append(" ms.\n");
		buff.append("\tThread Block Count:").append(threadInfo.getBlockedCount()).append("\n");		
		buff.append("\tThread State:").append(threadInfo.getThreadState()).append("\n");
		if(waitingOnLock!=null) {
			buff.append("\tWaiting On:").append(waitingOnLock).append("\n");
			buff.append("\t\tLockOwner:").append(lockOwner).append("\n");
			buff.append("\t\tLock Thread Id:").append(lockHolderThreadId).append("\n");
		}
		
		StackTraceElement[] stack = threadInfo.getStackTrace();
		buff.append("\tThread Stack:\n");
		for(StackTraceElement element: stack) {
			buff.append("\t\t");
			buff.append(element.toString());
			buff.append("\n");
		}
		
		return buff.toString();
	}
		
	
	
}


class ResetTimerTask extends TimerTask {
	protected ITracer tracer = null;
	protected String segment = null;
	protected String metric = null;
	protected long frequency = 0L;
	protected TimeUnit timeUnit = null;
	protected long fireCount = 0;
	@Override
	public void run() {		
		tracer.resetLongCounter(segment, metric);
		fireCount++;
	}
	/**
	 * @param tracer
	 * @param segment
	 * @param metric
	 * @param frequency
	 * @param timeUnit
	 */
	public ResetTimerTask(ITracer tracer, String segment, String metric, long frequency, TimeUnit timeUnit) {
		this.tracer = tracer;
		this.segment = segment;
		this.metric = metric;
		this.frequency = frequency;
		this.timeUnit = timeUnit;
	}
	/**
	 * @return the frequency
	 */
	
	public long getFrequency() {
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
	
	public TimeUnit getTimeUnit() {
		return timeUnit;
	}
	/**
	 * @return the fireCount
	 * @JMXAttribute(description="The fireCount", name="fireCount")
	 * TODO Extract JMXAttribute Annotation
	 */
	
	public long getFireCount() {
		return fireCount;
	}
	
	

}