package com.heliosapm.wiex.server.collectors;




import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.management.AttributeNotFoundException;
import javax.management.MBeanServer;
import javax.management.MBeanServerInvocationHandler;
import javax.management.ObjectName;

import org.apache.log4j.Logger;

import com.heliosapm.wiex.jmx.dynamic.annotation.JMXAttribute;
import com.heliosapm.wiex.jmx.dynamic.annotation.JMXManagedObject;
import com.heliosapm.wiex.jmx.dynamic.annotation.JMXOperation;
import com.heliosapm.wiex.jmx.threads.IScheduledThreadPoolService;
import com.heliosapm.wiex.jmx.threads.ScheduledThreadPoolService;
import com.heliosapm.wiex.jmx.threads.ThreadPoolService;
import com.heliosapm.wiex.jmx.util.MBeanServerHelper;
import com.heliosapm.wiex.server.collectors.jdbc.cache.CachedResultSet;
import com.heliosapm.wiex.server.collectors.jdbc.cache.CachedResultSetService;
import com.heliosapm.wiex.server.server.utils.JMXUtils;
import com.heliosapm.wiex.tracing.tracing.ITracer;
import com.heliosapm.wiex.tracing.tracing.TracerFactory;
/**
 * <p>Title: BaseCollector</p>
 * <p>Description: A base class to build other com.heliosapm.wiex.server.collectors.</p> 
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Company: Helios Development Group</p>
 * @author Whitehead
 * @version $Revision: 1.12 $
 */
@JMXManagedObject(annotated=true, declared=false)
public abstract class BaseCollector {

	/**	The elapsed time for the last collection in ms. */
	protected long collectTime = 0L;
	
	/** Collector connection properties */
	protected Properties connectionFactoryProperties = new Properties();
	
	/** Deployment start Stack Trace */
	protected StackTraceElement[] deploymentStartStackTrace = null;
	/** Deployment construct Stack Trace */
	protected StackTraceElement[] deploymentConstructStackTrace = null;
	
	
	/**	The collector's polling timer */
	protected static IScheduledThreadPoolService scheduler = null;
	/** The collector's asynch executor */
	protected static ExecutorService asynchExecutor = null;
	
	/**	The system property name of the JMX domain */
	public static final String JMX_DOMAIN_REGISTRATION = "sbs.tracer.jmx.domain";
	/**	The system property name of the collection scheduler core pool size */
	public static final String SCHEDULER_CORE_POOL_SIZE_PROPERTY = "sbs.tracer.scheduler.corepoolsize";
	/**	The system property name of the collection asynch executor core pool size */
	public static final String ASYNCH_EXECUTOR_CORE_POOL_SIZE_PROPERTY = "sbs.tracer.asynchexec.corepoolsize";
	
	/**	The default collection scheduler core pool size */
	public static final int SCHEDULER_CORE_POOL_SIZE = 10;
	/**	The default collection synch executor core pool size */
	public static final int ASYNCH_EXECUTOR_CORE_POOL_SIZE = 10;
	
	
	protected static Pattern targetPattern = Pattern.compile("\\{TARGET-PROPERTY:([a-zA-Z\\(\\)\\s-]+)}");
	protected static Pattern thisPattern = Pattern.compile("\\{THIS-PROPERTY:([a-zA-Z\\(\\)\\s-]+)}");
	protected static Pattern targetDomainPattern = Pattern.compile("\\{TARGET-DOMAIN:([\\d+])}");
	protected static Pattern thisDomainPattern = Pattern.compile("\\{THIS-DOMAIN:([\\d+])}");	
	protected static Pattern connFactoryPropertiesPattern = Pattern.compile("\\{CF-PROPERTY:([a-zA-Z\\.\\(\\)\\s-]+)}");
	protected static Pattern segmentPattern = Pattern.compile("\\{SEGMENT:([\\d+])}");
	
	
	
	
	
	protected static String jmxDomain = null;

	/**	The collector's polling timer task. */
	protected SimpleTimerTask timerTask = null;
	
	/**	The collector's JMX MBean ObjectName */
	protected ObjectName objectName = null;
	/**	The MBeanServer instance that registers the MBean */
	protected MBeanServer mbeanServer = null;
	
	/** The collector's log	 */
	protected Logger log = null;
	
	/**	The frequency of the polling. Defaulted to 15 seconds. */
	protected long frequency = 15000;
	
	/**	A version string for the collector module. */
	protected String VERSION = null;
	/**	A short name for the collector module.  */
	protected String MODULE = null;
	
	/** The segment delimeter for the currently defined tracer */
	protected String delim = "";
	/** The currently configured tracer */
	protected ITracer tracer = null;
	/** The root segment */
	protected String rootSegment = "";
	/** The sub-segments making up the root segment */
	protected String[] subSegments = new String[]{};
	
	/** The segment prefix elements */
	protected String[] segmentPrefixElements = null;
	/** The compiled segment prefix */
	protected String segmentPrefix = "";
	/** Log errors */
	protected boolean logErrors = false;
	/** Trace Collection Times */
	protected boolean traceCollectionTime = false;
	
	/** Execute Immediate Collect on Start */
	protected boolean immediateCollect = false;
	
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
	/** The parameter name constant for the metric type value of Long Counter Add To*/
	public static final String METRIC_TYPE_COUNTER_LONG_ADD = "CLONG+";
	/** The parameter name constant for the metric type value of Int Counter Add To*/
	public static final String METRIC_TYPE_COUNTER_INT_ADD = "CINT+";
	/** The parameter name constant for the metric type value of Long Counter Subtract From*/
	public static final String METRIC_TYPE_COUNTER_LONG_SUBTRACT = "CLONG-";
	/** The parameter name constant for the metric type value of Int Counter Subtract From*/
	public static final String METRIC_TYPE_COUNTER_INT_SUBTRACT = "CINT-";	
	
	/** The parameter name constant for the metric type value of Incident*/
	public static final String METRIC_TYPE_INCIDENT = "INCIDENT";
	
	/** The parameter name constant for the metric type value of Delta Int*/
	public static final String METRIC_TYPE_DELTA_INT = "DINT";
	/** The parameter name constant for the metric type value of Delta Long*/
	public static final String METRIC_TYPE_DELTA_LONG = "DLONG";
	
	/** The parameter name constant for the metric type value of Counter Delta Int*/
	public static final String METRIC_TYPE_COUNTER_DELTA_INT = "CDINT";
	/** The parameter name constant for the metric type value of Counter Delta Long*/
	public static final String METRIC_TYPE_COUNTER_DELTA_LONG = "CDLONG";
	
	/** The parameter name constant for the metric type value of Counter Delta Int*/
	public static final String METRIC_TYPE_COUNTER_DELTA_INT_ADD = "CDINT+";
	/** The parameter name constant for the metric type value of Counter Delta Long*/
	public static final String METRIC_TYPE_COUNTER_DELTA_LONG_ADD = "CDLONG+";

	/** The parameter name constant for the metric type value of Counter Delta Int*/
	public static final String METRIC_TYPE_COUNTER_DELTA_INT_SUBTRACT = "CDINT-";
	/** The parameter name constant for the metric type value of Counter Delta Long*/
	public static final String METRIC_TYPE_COUNTER_DELTA_LONG_SUBTRACT = "CDLONG-";
	
	
	/** The parameter name constant for the metric type value of Time Reset Counter*/
	public static final String METRIC_TYPE_TIMED_RESET_COUNTER = "TRLONG";
	
	/**	The system property name to read to get the default interval reset frequency */
	public static final String DEFAULT_RESET_FREQUENCY_PROPERTY = "com.heliosapm.wiex.tracing.default.reset.frequency";
	/**	The system property name to read to get the default interval reset frequency unit */
	public static final String DEFAULT_RESET_FREQUENCY_UNIT_PROPERTY = "com.heliosapm.wiex.tracing.default.reset.frequency.unit";

	/**	The default interval reset frequency */
	public static final long DEFAULT_RESET_FREQUENCY = 300L;
	/**	The default interval reset frequency unit */
	public static final String DEFAULT_RESET_FREQUENCY_UNIT = "SECONDS";
	
	/**	The configured reset time unit */
	protected String resetTimeUnit = null;
	/**	The configured reset frequency */
	protected long resetFrequency = 0L;

	
	/** The object name of the collection scheduler */
	public static final String COLLECTION_SCHEDULER_OBJECT_NAME = "com.heliosapm.wiex.tracing:service=CollectionScheduler";
	/** The object name of the asynch executor scheduler */
	public static final String ASYNCH_EXECUTOR_OBJECT_NAME = "com.heliosapm.wiex.tracing:service=AsynchExecutor";
	
	
	/** The default object name of the cache service */
	public static final ObjectName CACHE_SERVICE_OBJECT_NAME = JMXUtils.safeObjectName("com.heliosapm.wiex.tracing.collectors:service=CacheService,name=Default");
	
	
	
	static {		
		try {
			jmxDomain = System.getProperty(JMX_DOMAIN_REGISTRATION, "DefaultDomain");
			String schedulerCorePoolSizeStr = System.getProperty(SCHEDULER_CORE_POOL_SIZE_PROPERTY);
			String executorCorePoolSizeStr = System.getProperty(ASYNCH_EXECUTOR_CORE_POOL_SIZE_PROPERTY);
			int schedulerCorePoolSize = 0;
			int executorCorePoolSize = 0;
			try {
				schedulerCorePoolSize = Integer.parseInt(schedulerCorePoolSizeStr);
			} catch (Exception e) {
				schedulerCorePoolSize = SCHEDULER_CORE_POOL_SIZE;
			}
			try {
				executorCorePoolSize = Integer.parseInt(executorCorePoolSizeStr);
			} catch (Exception e) {
				executorCorePoolSize = ASYNCH_EXECUTOR_CORE_POOL_SIZE;
			} 
			
			MBeanServer server = MBeanServerHelper.getMBeanServer(jmxDomain);
			
			try {
				ObjectName schedulerObjectName = new ObjectName(COLLECTION_SCHEDULER_OBJECT_NAME);
				if(!server.isRegistered(schedulerObjectName)) {
					ScheduledThreadPoolService stp = new ScheduledThreadPoolService();
					stp.setCorePoolSize(schedulerCorePoolSize);		
					stp.setPrestartThreads(1);
					stp.setShutdownTime(1);
					server.registerMBean(stp, schedulerObjectName);
					server.invoke(schedulerObjectName, "start", new Object[]{}, new String[]{});
					scheduler = (IScheduledThreadPoolService) MBeanServerInvocationHandler.newProxyInstance(server, schedulerObjectName, IScheduledThreadPoolService.class, false);
				}
			} catch (Exception e) {
				throw new RuntimeException("Failed to Start CollectionScheduler", e);
			}
			try {
				ObjectName executorObjectName = new ObjectName(ASYNCH_EXECUTOR_OBJECT_NAME);
				if(!server.isRegistered(executorObjectName)) {
					ThreadPoolService stp = new ThreadPoolService();
					stp.setCorePoolSize(executorCorePoolSize);		
					stp.setPrestartThreads(1);
					stp.setShutdownTime(1);
					server.registerMBean(stp, executorObjectName);
					server.invoke(executorObjectName, "start", new Object[]{}, new String[]{});
					asynchExecutor = (ExecutorService) MBeanServerInvocationHandler.newProxyInstance(server, executorObjectName, ExecutorService.class, false);
				}
			} catch (Exception e) {
				throw new RuntimeException("Failed to Start CollectionScheduler", e);
			}
			
		} catch (Throwable t) {
			throw new RuntimeException("Unexpected Exception in BaseCollector static initializer", t);
		}
		
		
	}
	
	
	/**
	 * The abstract class constructor. 
	 * All extensions of this class should call <code>super()</code> in their constructor.
	 * Initializes the reset interval frequency and time unit.
	 */
	public BaseCollector() {
		deploymentConstructStackTrace = Thread.currentThread().getStackTrace();
		log = Logger.getLogger(getClass());
		tracer = TracerFactory.getInstance();
		delim = tracer.getSegmentDelimeter();	
		init();
		// Access the system properties that define the frequency and time unit for this class.
		// If either is null, look up the general generic system property.
		String resetFrequencyStr = System.getProperty(DEFAULT_RESET_FREQUENCY_PROPERTY + "." + this.getClass().getName());
		String resetTimeUnitStr = System.getProperty(DEFAULT_RESET_FREQUENCY_UNIT_PROPERTY + "." + this.getClass().getName());
		if(resetFrequencyStr==null) {
			resetFrequencyStr = System.getProperty(DEFAULT_RESET_FREQUENCY_PROPERTY);
		}
		if(resetTimeUnitStr==null) {
			resetTimeUnitStr = System.getProperty(DEFAULT_RESET_FREQUENCY_UNIT_PROPERTY);
		}
		// Now test each of the values.
		if(resetFrequencyStr==null) {
			// if the frequency is null, the system property was not defined.
			// the defaults should be used.
			resetFrequency = DEFAULT_RESET_FREQUENCY;
			resetTimeUnit = DEFAULT_RESET_FREQUENCY_UNIT;
		} else {
			// the system property for frequency was defined, 
			// but if it cannot be parsed, set back to the defaults. 
			try {
				resetFrequency = Long.parseLong(resetFrequencyStr);
			} catch (Exception e) {
				resetFrequency = DEFAULT_RESET_FREQUENCY;
				resetTimeUnit = DEFAULT_RESET_FREQUENCY_UNIT;				
			}
		}
		// If the resetTimeUnit was already set, that means we are using a defaulted frequency,
		// so we do not read the system property for unit.
		// If we do use the system property for unit, it must be a valid string, or we reset back to the default.
		if(resetTimeUnit==null) {
			try {
				resetTimeUnit = TimeUnit.valueOf(resetTimeUnitStr).toString();
			} catch (Exception e) {
				resetTimeUnit = DEFAULT_RESET_FREQUENCY_UNIT;
			}
		}
		StringBuilder buff = new StringBuilder("Instantiated Collector:\t");
		buff.append(getVersion());
		buff.append("\n\tReset Frequency:").append(resetFrequency);
		buff.append("\n\tReset Frequency Unit:").append(resetTimeUnit);
		log.info(buff.toString());
		
	}
	
	// ===============================================================
	//			Caching Operations
	// ===============================================================	
	
	/**
	 * Checks the agent to verify the existence of the targetMBean, and that the target MBean contains the target Attribute.
	 * If either is missing, they are created.
	 * @param targetMBean The target data source MBean.
	 * @param targetAttribute The target data source attribute name. 
	 * @param containerClassName The class name of the cached result container.
	 * @param attributes Named attributes to set against the created container.
	 */
	protected void initializeCachedResultMBean(ObjectName targetMBean, String targetAttribute, String containerClassName, Map<String, String> attributes) {
		CachedResultSetService crss = null;
		try {
			if(!mbeanServer.isRegistered(targetMBean)) {
				crss = new CachedResultSetService();				
				mbeanServer.registerMBean(crss, targetMBean);
			} 
			try {
				mbeanServer.getAttribute(targetMBean, targetAttribute);
			} catch (AttributeNotFoundException anfe) {
				Class containerClazz = Class.forName(containerClassName);
				Class parameterClazz = null;
				Constructor containerCtor = containerClazz.getConstructor(new Class[]{String.class});
				CachedResultSet crs = (CachedResultSet)containerCtor.newInstance(new Object[]{targetAttribute});
				for(Map.Entry<String, String> entry: attributes.entrySet()) {
					parameterClazz = JMXUtils.getAttributeType(containerClazz, entry.getKey());
					JMXUtils.setAttribute(entry.getKey(), entry.getValue(), parameterClazz, crs);
				}
				//new CachedResultSetImpl(targetAttribute);
				mbeanServer.invoke(targetMBean, "addCachedResultSet", new Object[]{crs}, new String[]{CachedResultSet.class.getName()});				
				//mbeanServer.invoke(targetMBean, "addManagedObject", new Object[]{crs}, new String[]{Object.class.getName()});
			}
		} catch (Exception e) {
			log.error("Exception Setting Up CachedResult Container:[" + targetMBean + "/" + targetAttribute + "]", e);
			throw new RuntimeException("Exception Setting Up CachedResult Container:[" + targetMBean + "/" + targetAttribute + "]", e);
		}
	}	
	
	/**
	 * Calls the child class concrete implementation to do any constructive initialization.
	 */
	public abstract void init();
	
	/**
	 * The abstract declaration of the collect method.
	 */
	public abstract void collect();
	

	/**
	 * Records an individual metric.
	 * @param category Them metric resource segment.
	 * @param name The metric name.
	 * @param value The value of the submited metric.
	 * @param type The type of the submited metric.
	 * @throws Exception 
	 */
	public void recordTrace(String category, String name, String value, String type, Object ... args) throws Exception {
		if(value==null) return;
		if(type.equalsIgnoreCase(METRIC_TYPE_LONG)) {
			tracer.recordMetric(category, name, Long.parseLong(value));
		} else if(type.equalsIgnoreCase(METRIC_TYPE_INT)) {
			tracer.recordMetric(category, name, Integer.parseInt(value));
		} else if(type.equalsIgnoreCase(METRIC_TYPE_STRING)) {
			tracer.recordMetric(category, name, value);
		} else if(type.equalsIgnoreCase(METRIC_TYPE_COUNTER_LONG)) {
			tracer.recordCounterMetric(category, name, Long.parseLong(value));
		} else if(type.equalsIgnoreCase(METRIC_TYPE_COUNTER_LONG_ADD)) {
			tracer.recordCounterMetricAdd(category, name, Long.parseLong(value));
		} else if(type.equalsIgnoreCase(METRIC_TYPE_COUNTER_LONG_SUBTRACT)) {
			tracer.recordCounterMetricAdd(category, name, Long.parseLong(value)*-1L);						
		} else if(type.equalsIgnoreCase(METRIC_TYPE_COUNTER_INT)) {
			tracer.recordCounterMetric(category, name, Integer.parseInt(value));
		} else if(type.equalsIgnoreCase(METRIC_TYPE_COUNTER_INT_ADD)) {
			tracer.recordCounterMetricAdd(category, name, Integer.parseInt(value));
		} else if(type.equalsIgnoreCase(METRIC_TYPE_COUNTER_INT_SUBTRACT)) {
			tracer.recordCounterMetricAdd(category, name, Integer.parseInt(value)*-1);						
		} else if(type.equalsIgnoreCase(METRIC_TYPE_INCIDENT)) {
			tracer.recordMetric(category, name);
		} else if(type.equalsIgnoreCase(METRIC_TYPE_COUNTER_DELTA_INT)) {
			tracer.recordCounterMetricDelta(category, name, Integer.parseInt(value));
		} else if(type.equalsIgnoreCase(METRIC_TYPE_COUNTER_DELTA_LONG)) {
			tracer.recordCounterMetricDelta(category, name, Long.parseLong(value));

			
		} else if(type.equalsIgnoreCase(METRIC_TYPE_DELTA_INT)) {
			tracer.recordMetricDelta(category, name, Integer.parseInt(value));
		} else if(type.equalsIgnoreCase(METRIC_TYPE_DELTA_LONG)) {
			tracer.recordMetricDelta(category, name, Long.parseLong(value));		
		} else if(type.equalsIgnoreCase(METRIC_TYPE_TIMED_RESET_COUNTER)) {
			if(args==null || args.length != 2 ) {
				tracer.recordIntervalReset(category, name, Long.parseLong(value), resetFrequency, resetTimeUnit);
			}  else {
				tracer.recordIntervalReset(category, name, Long.parseLong(value), ((Long)args[0]).longValue(), (String)args[1]);
			}			
		} else {
			throw new Exception("Metric Type Not Recognized:" + type);
		}
	}	
	
	
	
	
	/**
	 * Returns true if collection time errors should be logged.
	 * @return true if collection time errors should be logged.
	 */
	@JMXAttribute(description="If true, collection time errors will be logged.", name="LogErrors")
	public boolean getLogErrors() {
		return logErrors;
	}
	
	/**
	 * The classloader of the sheduling timer.
	 * @return A class loader.
	 */
	@JMXAttribute(description="The classloader of the sheduling timer.", name="TimerClassLoader")
	public ClassLoader getTimerClassLoader() {
		return BaseCollector.class.getClassLoader();
	}
	
	/**
	 * If set to true, will cause collection errors to be logged.
	 * @param b
	 */
	public void setLogErrors(boolean b) {
		logErrors = b;
	}
	
	/**
	 * Returns true if collection times should be traced.
	 * @return true if collection times should be traced.
	 */
	@JMXAttribute(description="If true, collection times should be traced.", name="LogTraceTimes")
	public boolean getTraceCollectionTime() {
		return this.traceCollectionTime;
	}
	
	/**
	 * If set to true, collection times should be traced.
	 * @param b
	 */
	public void setTraceCollectionTime(boolean b) {
		traceCollectionTime = b;
	}
	
	
	
	/**
	 * Returns a reference to the MBeanServer.
	 * @return the mbeanServer
	 */
	@JMXAttribute(expose=false)
	public MBeanServer getMbeanServer() {
		return mbeanServer;
	}

	/**
	 * Sets the collector's MBeanServer
	 * @param mbeanServer the mbeanServer to set
	 */
	public void setMbeanServer(MBeanServer mbeanServer) {
		this.mbeanServer = mbeanServer;
	}

	/**
	 * Return's the collector's JMX MBean ObjectName.
	 * @return the objectName
	 */
	@JMXAttribute(expose=false)
	public ObjectName getObjectName() {
		return objectName;
	}

	/**
	 * Sets the collector's JMX MBean ObjectName.
	 * @param objectName the objectName to set
	 */
	public void setObjectName(ObjectName objectName) {
		this.objectName = objectName;
	}
	
	/**
	 * Returns the frequency of the collector's polling.
	 * @return the frequency
	 */
	@JMXAttribute(description="The frequency of the com.heliosapm.wiex.server.collectors polling.", name="PollFrequency")	
	public long getFrequency() {
		return frequency;
	}

	/**
	 * Sets the frequency of the collector's polling.
	 * @param frequency the frequency to set
	 */
	public void setFrequency(long frequency) {
		boolean changed = (this.frequency != frequency);
		this.frequency = frequency;
		if(changed && timerTask != null) {
			timerTask.stop();
			scheduler.remove(timerTask);
			try {
				scheduleCollection();
			} catch (Exception e) {
				log.error("Failed to Reschedule task", e);
			}
		}
	}

	/**
	 * Returns the SCM Version of this class.
	 * @return String A collector descriptive string.
	 */
	@JMXAttribute(description="The Component Version", name="Version")
	public String getVersion() {
		return MODULE + " " + VERSION.replaceAll(" ", "").replaceAll("\\$", "").replaceAll("Revision", "").replaceAll(":", "");
	}	
	
	/**
	 * Returns the elapsed time of the last collection.
	 * @return the collectTime
	 */
	@JMXAttribute(description="The last elapsed time to collect in ms.", name="CollectTime")
	public long getCollectTime() {
		return collectTime;
	}	
	
	/**
	 * Starts the collector's polling.
	 * If the frequency is less than 1, no polling is scheduled.
	 * @throws Exception
	 */
	@JMXOperation(description="Starts The Service", expose=true, name="start")
	public void start() throws Exception {
		log.info("\n\t========================================================\n\tStarting Collector:" + objectName + "\n\t========================================================" );
		deploymentStartStackTrace = Thread.currentThread().getStackTrace();
		tokenizeSegmentPrefixElements();
		scheduleCollection();
		if(immediateCollect) {
			log.info("Issuing Immediate Collect For " + objectName);
			collect();
			log.info("Immediate Collect Complete:" + objectName);
		}
		log.info("\n\t========================================================\n\tStarted Collector:" + objectName + "\n\t========================================================" );		
	}
	
	/**
	 * Displays the MBean's construct and start stack traces.
	 * @return Formatted Stack Trace.
	 */
	@JMXOperation(description="Displays the MBean construct and start stack traces.", expose=true, name="displayDeploymentStackTraces")
	public String displayDeploymentStackTraces() {
		StringBuilder buff = new StringBuilder("Deployment Stack Traces");
		buff.append("\nConstruct Stack Trace");
		for(StackTraceElement ste: deploymentConstructStackTrace) {
			buff.append("\n\t").append(ste.toString());
		}
		buff.append("\nStart Stack Trace");
		for(StackTraceElement ste: deploymentStartStackTrace) {
			buff.append("\n\t").append(ste.toString());
		}		
		return buff.toString();
	}
	
	/**
	 * Creates a new collection task and schedules it.
	 * If the frequency is not greater than zero, the task will not be scheduled.
	 * @throws Exception
	 */
	protected void scheduleCollection() throws Exception  {
		if(frequency>0) {
			timerTask = new SimpleTimerTask(this.getClass().getMethod("collect" , new Class[]{}), this, new Object[]{});
			scheduler.scheduleWithFixedDelay(timerTask, frequency, frequency, TimeUnit.MILLISECONDS);
			log.info("Collect Callback Scheduled on Frequency of " + frequency + " ms.");		
		}
	}
	
	/**
	 * Stops the collector's polling if a polling schedule has been established.
	 */
	@JMXOperation(description="Stops The Service", expose=true, name="stop")	
	public void stop() {
		log.info("\n\t========================================================\n\tStopping Collector:" + objectName + "\n\t========================================================" );
		if(timerTask!=null) {
			timerTask.stop();
			scheduler.remove(timerTask);
		}
		timerTask = null;
		log.info("\n\t========================================================\n\tStopped Collector:" + objectName + "\n\t========================================================" );
	}

	
	
	/**
	 * The root segment 
	 * @return the rootSegment
	 */
	public String getRootSegment() {
		return rootSegment;
	}

	/**
	 * @param rootSegment the rootSegment to set
	 */
	public void setRootSegment(String rootSegment) {
		this.rootSegment = rootSegment;
	}

	/**
	 * The sub-segments making up the root segment
	 * @return the subSegments
	 */
	public String[] getSubSegments() {
		return subSegments;
	}

	/**
	 * @param subSegments the subSegments to set
	 */
	public void setSubSegments(String[] subSegments) {
		this.subSegments = subSegments;
	}		
	
	/**
	 * The segment prefix 
	 * @return the segmentPrefix
	 */
	@JMXAttribute(description="The segment prefix", name="SegmentPrefix")
	public String getSegmentPrefix() {
		return segmentPrefix;
	}
	

	/**
	 * Sets the segment prefix elements
	 * @param segmentPrefixElements the segmentPrefixElements to set
	 */
	public void setSegmentPrefixElements(String[] segmentPrefixElements) {
		this.segmentPrefixElements = segmentPrefixElements;		
		segmentPrefix = tracer.buildSegment(false, this.segmentPrefixElements);
	}	
	
	/**
	 * Tokenizes segment prefix elements.
	 * Should be called on start() to ensure that the sequencing of attribute sets does not impact tokens.
	 */
	protected void tokenizeSegmentPrefixElements() {
		if(segmentPrefixElements==null || segmentPrefixElements.length < 1) {
			segmentPrefixElements = new String[0];
		} else {			
			for(int i = 0; i < segmentPrefixElements.length; i++) {
				segmentPrefixElements[i] = formatName(segmentPrefixElements[i]);
			}
		}		
		segmentPrefix = tracer.buildSegment(false, segmentPrefixElements);		
		
	}
	
	/**
	 * The segment prefix elements
	 * @return the segmentPrefixElements
	 */
	@JMXAttribute(description="The segment prefix elements", name="SegmentPrefixElements")
	public String[] getSegmentPrefixElements() {
		return segmentPrefixElements;
	}

	/**
	 * The default reset timer frequency.
	 * @return the resetFrequency
	 */
	@JMXAttribute(description="The default reset timer frequency", name="DefaultResetFrequency")
	public long getResetFrequency() {
		return resetFrequency;
	}

	/**
	 * Sets the default reset timer frequency.
	 * @param resetFrequency the resetFrequency to set
	 */
	public void setResetFrequency(long resetFrequency) {
		this.resetFrequency = resetFrequency;
	}

	/**
	 * The default reset timer frequency unit.
	 * @return the resetTimeUnit
	 */
	@JMXAttribute(description="The default reset timer frequency unit", name="DefaultResetUnit")
	public String getResetTimeUnit() {
		return resetTimeUnit;
	}

	/**
	 * Sets the The default reset timer frequency unit.
	 * @param resetTimeUnit the resetTimeUnit to set
	 */
	public void setResetTimeUnit(String resetTimeUnit) {
		this.resetTimeUnit = resetTimeUnit;
	}

	/**
	 * @return the immediateCollect
	 */
	@JMXAttribute(description="If true, a collection is executed immediately after start.", name="ImmediateCollect")
	public boolean getImmediateCollect() {
		return immediateCollect;
	}

	/**
	 * @param immediateCollect the immediateCollect to set
	 */
	public void setImmediateCollect(boolean immediateCollect) {
		this.immediateCollect = immediateCollect;
	}
	
	/**
	 * Helper method to generate a percentage.
	 * @param part
	 * @param all
	 * @return A percentage value.
	 */
	protected static long percent(float part, float all) {
		return (long)((part)/all*100);
	}
	
	/**
	 * Applies pattern substitutions to the passed string for target properties from this MBean.
	 * @param name A value to be formatted.
	 * @return A formatted name.
	 */
	protected String formatName(String name) {
		if(name.contains("{THIS-PROPERTY")) {
			name = bindTokens(objectName, name, thisPattern);
		}
		if(name.contains("{CF-PROPERTY")) {
			name = bindTokens(connectionFactoryProperties, name, connFactoryPropertiesPattern);
		}
		if(name.contains("{THIS-DOMAIN")) {
			name = bindTokens(objectName, name, thisDomainPattern);
		}
		if(name.contains("{SEGMENT")) {
			name = bindTokens(objectName, name, segmentPattern);
		}				
		return name;
	}
	
	/**
	 * Applies pattern substitutions to the passed string for target properties from the target mbean.
	 * @param name A value to be formatted.
	 * @return A formatted name.
	 */
	protected String formatName(String name, ObjectName remoteMBean) {
		if(name.contains("{TARGET-PROPERTY")) {
			name = bindTokens(remoteMBean, name, targetPattern);
		}
		if(name.contains("{THIS-DOMAIN")) {
			name = bindTokens(objectName, name, targetDomainPattern);
		}				
		return name;
	}
	
	/**
	 * Takes the text passed and replaces tokens in accordance with the pattern 
	 * supplied taking the substitution vale from properties in the passed object name.
	 * @param targetObjectName The substitution values come from this object name.
	 * @param text The original text that will be substituted.
	 * @param p The pattern matcher to locate substitution tokens.
	 * @return The substituted string.
	 */
	public String bindTokens(ObjectName targetObjectName, String text, Pattern p) {
		Matcher matcher = p.matcher(text);
		String token = null;
		String property = null;
		String propertyValue = null;
		int pos = -1;
		while(matcher.find()) {
			token = matcher.group(0);
			property = matcher.group(1);
			propertyValue = targetObjectName.getKeyProperty(property);
            if(token.toUpperCase().contains("DOMAIN")) {
                pos = Integer.parseInt(property);
                propertyValue = targetObjectName.getDomain().split("\\.")[pos];
            } else if(token.toUpperCase().contains("SEGMENT")) {
            	pos = Integer.parseInt(property);
            	propertyValue = segmentPrefixElements[pos];
            } else {
                propertyValue = targetObjectName.getKeyProperty(property);
            }			
			text = text.replace(token, propertyValue);
		}
		return text;
	}
	
	/**
	 * Takes the text passed and replaces tokens in accordance with the pattern 
	 * supplied taking the substitution vale from the passed target properties.
	 * @param targetProperties The target properties to substitute from.
	 * @param text The original text that will be substituted.
	 * @param p The pattern matcher to locate substitution tokens.
	 * @return The substituted string.
	 */
	public String bindTokens(Properties targetProperties, String text, Pattern p) {		
		Matcher matcher = p.matcher(text);
		String token = null;
		String property = null;
		String propertyValue = null;
		while(matcher.find()) {
			token = matcher.group(0);
			property = matcher.group(1);
			propertyValue = targetProperties.getProperty(property);
			text = text.replace(token, propertyValue);
		}
		return text;
	}
	
	

}
