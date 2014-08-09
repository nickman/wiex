/**
 * 
 */
package com.heliosapm.wiex.server.collectors.java;

import gnu.trove.map.hash.TObjectLongHashMap;

import java.lang.management.CompilationMXBean;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryType;
import java.lang.management.MemoryUsage;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadMXBean;
import java.lang.reflect.Method;
import java.util.List;

import com.heliosapm.wiex.jmx.dynamic.annotation.JMXAttribute;
import com.heliosapm.wiex.jmx.dynamic.annotation.JMXManagedObject;
import com.heliosapm.wiex.jmx.dynamic.annotation.JMXOperation;
import com.heliosapm.wiex.server.collectors.BaseCollector;


/**
 * <p>Title: JVMCollector</p>
 * <p>Description: A collector for base JVM stats.</p> 
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Company: Helios Development Group</p>
 * @author Whitehead
 * @version $Revision: 1.7 $
 */
@JMXManagedObject(annotated=true, declared=false)
public class JVMCollector extends BaseCollector {

	
	
	
	/** A reference to the JVM's compilation MXBean	 */
	protected CompilationMXBean compilationMXBean = null;
	/** A reference to the JVM's Runtime MXBean */
	protected RuntimeMXBean runtimeMXBean = null;
	/** A list of JVM's garbage collection MXBeans	 */
	protected List<GarbageCollectorMXBean> garbageCollectorMXBeans = null;
	/** The MemoryMXBean */
	protected MemoryMXBean memoryMXBean = null;
	/** OS MXBean */
	protected OperatingSystemMXBean osMXBean = null;
	/** Thread MXBean */
	protected ThreadMXBean threadMXBean = null;
	
	/** Memory Pool MXBeans */
	protected List<MemoryPoolMXBean> memoryPoolMXBeans = null;
	/** Indicates if OS stats can be collected from com.sun.management.OperatingSystem */
	protected boolean isSunOSMBean = false;
	/** Indicates if OS stats can be collected from com.ibm.lang.management.OperatingSystemMXBeanImpl */
	protected boolean isIBMOSMBean = false;
	
	/** Cache for last read values */
	protected TObjectLongHashMap<String> gcDeltas = null;
	/** SunOSMBean Method for getCommittedVirtualMemorySize */
	protected Method getCommittedVirtualMemorySizeMethod = null;
	/** SunOSMBean Method for getFreePhysicalMemorySize */
	protected Method getFreePhysicalMemorySizeMethod = null;
	/** SunOSMBean Method for getFreeSwapSpaceSize */
	protected Method getFreeSwapSpaceSizeMethod = null;
	/** SunOSMBean Method for getProcessCpuTime */
	protected Method getProcessCpuTimeMethod = null;
	/** SunOSMBean Method for getTotalPhysicalMemorySize */
	protected Method getTotalPhysicalMemorySizeMethod = null;
	/** SunOSMBean Method for getTotalSwapSpaceSize */
	protected Method getTotalSwapSpaceSizeMethod = null;
	/** IBMOSMBean Method for getProcessingCapacity */
	protected Method getProcessingCapacityMethod = null;
	/** IBMOSMBean Method for getTotalPhysicalMemory */
	protected Method getTotalPhysicalMemoryMethod = null;
	
	
	
	
	
	
	
	/** The number of CPUs */
	protected int cpuCount = 0;
	/** The start time of the reading in ns*/
	protected long startTimeNs = -1;
	/** The start time of the reading in ms*/
	protected long startTimeMs = -1;
	/** Empty Class Array */
	protected static final Class[] CLASS_ARGS = new Class[]{};
	/** Empty Object Array */
	protected static final Object[] OBJECT_ARGS = new Object[]{};
	
	
	public static final String HEAP_MEMORY = "Heap memory";
	public static final String NON_HEAP_MEMORY = "Non-heap memory";
	public static final String MEMORY_COMMITTED = "Committed (bytes)";
	public static final String MEMORY_INIT = "Init (bytes)";
	public static final String MEMORY_MAX = "Max (bytes)";
	public static final String MEMORY_USED = "Used (bytes)";
	public static final String MEMORY_PERCENT_OF_COMMITTED = "% Used of Committed";
	public static final String MEMORY_PERCENT_OF_MAX = "% Used of Max";
	
	
	
	
	/**
	 * Instantiates a new JVM Collector and collects JVM stats and references.
	 */
	public JVMCollector() {
		super();
		StringBuilder message = new StringBuilder("\n===============================================================\n\tJVM MX Beans\n===============================================================");
		threadMXBean = ManagementFactory.getThreadMXBean();
		message.append("\n\tThreadMXBean:").append(threadMXBean.getClass().getName());
		compilationMXBean = ManagementFactory.getCompilationMXBean();
		message.append("\n\tCompilationMXBean:").append(compilationMXBean.getClass().getName());
		runtimeMXBean = ManagementFactory.getRuntimeMXBean();
		message.append("\n\tRuntimeMXBean:").append(runtimeMXBean.getClass().getName());
		garbageCollectorMXBeans = ManagementFactory.getGarbageCollectorMXBeans();
		message.append("\n\tGarbageCollectorMXBeans:");
		for(GarbageCollectorMXBean gb: garbageCollectorMXBeans) {
			message.append("\n\t\t").append(gb.getName()).append(":").append(gb.getClass().getName());
		}
		gcDeltas = new TObjectLongHashMap<String>(garbageCollectorMXBeans.size()+5);
		memoryMXBean = ManagementFactory.getMemoryMXBean();
		message.append("\n\tMemoryMXBean:").append(memoryMXBean.getClass().getName());
		osMXBean = ManagementFactory.getOperatingSystemMXBean();
		message.append("\n\tOperatingSystemMXBea:").append(osMXBean.getClass().getName());		
		memoryPoolMXBeans = ManagementFactory.getMemoryPoolMXBeans();
		message.append("\n\tMemoryPoolMXBeans:");
		for(MemoryPoolMXBean mb: memoryPoolMXBeans) {
			message.append("\n\t\t").append(mb.getName()).append(":").append(mb.getClass().getName());
		}
		
		if("com.sun.management.OperatingSystem".equalsIgnoreCase(osMXBean.getClass().getName())) {
			isSunOSMBean = true;
			initSunOSMethods();
		} else {
			if("com.ibm.lang.management.OperatingSystemMXBeanImpl".equalsIgnoreCase(osMXBean.getClass().getName())) {
				isIBMOSMBean = true;
				initIBMOSMethods();
			}
		}
		
		message.append("\n\tisSunOSMBean:").append(isSunOSMBean);
		message.append("\n\tisIBMOSMBean:").append(isIBMOSMBean);
		message.append("\n===============================================================");
		log.info(message.toString());

		cpuCount = osMXBean.getAvailableProcessors();
		rootSegment = tracer.buildSegment(segmentPrefix, "JVM");
		traceVMInfo();
		
	}
	
	/**
	 * Intializes the methods for the Sun Operating System MXBean for invocation by reflection.
	 */
	protected void initSunOSMethods() {
		try {
			Class clazz = Class.forName("com.sun.management.OperatingSystemMXBean");
			getCommittedVirtualMemorySizeMethod = clazz.getMethod("getCommittedVirtualMemorySize", CLASS_ARGS);
			getFreePhysicalMemorySizeMethod = clazz.getMethod("getFreePhysicalMemorySize", CLASS_ARGS);
			getFreeSwapSpaceSizeMethod = clazz.getMethod("getFreeSwapSpaceSize", CLASS_ARGS);
			getProcessCpuTimeMethod = clazz.getMethod("getProcessCpuTime", CLASS_ARGS);
			getTotalPhysicalMemorySizeMethod = clazz.getMethod("getTotalPhysicalMemorySize", CLASS_ARGS);
			getTotalSwapSpaceSizeMethod = clazz.getMethod("getTotalSwapSpaceSize", CLASS_ARGS);
			log.info("SunOSMXBean Methods Initialized");
		} catch (Exception e) {
			if(logErrors) {
				log.error("Failed to Initialize SunOSMethods", e);
			}
			isSunOSMBean = false;
		}
	}
	
	/**
	 * Intializes the methods for the IBM Operating System MXBean for invocation by reflection. 
	 */
	protected void initIBMOSMethods() { 
		try {
			Class clazz = Class.forName("com.ibm.lang.management.OperatingSystemMXBean");
			getProcessingCapacityMethod = clazz.getMethod("getProcessingCapacity", CLASS_ARGS);
			getTotalPhysicalMemoryMethod = clazz.getMethod("getTotalPhysicalMemory", CLASS_ARGS);
			log.info("IBMOSMXBean Methods Initialized");
		} catch (Exception e) {
			if(logErrors) {
				log.error("Failed to Initialize IBMOSMethods", e);
			}
			isIBMOSMBean = false;
		}		
	}
	
	/**
	 * Initializes the VERSION and MODULE.
	 * @see com.heliosapm.wiex.server.collectors.BaseCollector#init()
	 */
	public void init() {
		VERSION = "$Revision: 1.7 $";
		MODULE = "JVMCollector";
	}		
	
	/**
	 * Collects various MXBean stats from the JVM.
	 * @see com.heliosapm.wiex.server.collectors.BaseCollector#collect()
	 */
	@Override
	@JMXOperation(description="Collects JVM Stats", expose=true, name="collect")
	public void collect() {
		readJVMMXStats();

	}
	
	/**
	 * True if the VM's OperatingSystemMXBean is Sun's
	 * @return True if the VM's OperatingSystemMXBean is Sun's
	 */
	@JMXAttribute(description="True if the VM's OperatingSystemMXBean is Sun's", name="SunOSMXBean")
	public boolean getSunOSMXBean() {
		return isSunOSMBean;
	}

	/**
	 * True if the VM's OperatingSystemMXBean is IBM's
	 * @return True if the VM's OperatingSystemMXBean is IBM's
	 */
	@JMXAttribute(description="True if the VM's OperatingSystemMXBean is IBM's", name="IBMOSMXBean")	
	public boolean getIBMOSMXBean() {
		return isIBMOSMBean;
	}

	
	/**
	 * Traces VM information.
	 */
	protected void traceVMInfo() {
		tracer.recordMetric(tracer.buildSegment(segmentPrefix, false, "JVM"), "JIT Name", compilationMXBean.getName());
		StringBuilder startupParams  = new StringBuilder("JVM Input Arguments\n");
		List<String> params = runtimeMXBean.getInputArguments();
		for(String s: params) {
			startupParams.append("\t");
			startupParams.append(s);
			startupParams.append("\n");
		}
		tracer.recordMetric(tracer.buildSegment(segmentPrefix, false, "JVM"), "Startup Parameters", startupParams.toString());		
		tracer.recordTimeStamp(tracer.buildSegment(segmentPrefix, false, "JVM"), "Startup Time", runtimeMXBean.getStartTime());
		StringBuilder jvmInfo = new StringBuilder("JVM Information");
		jvmInfo.append("\tManagement Version:");
		jvmInfo.append(runtimeMXBean.getManagementSpecVersion());
		jvmInfo.append("\n");
		jvmInfo.append("\tName:");
		jvmInfo.append(runtimeMXBean.getName());
		jvmInfo.append("\n");
		jvmInfo.append("\tSpec Name:");
		jvmInfo.append(runtimeMXBean.getSpecName());		
		jvmInfo.append("\n");
		jvmInfo.append("\tSpec Vendor:");
		jvmInfo.append(runtimeMXBean.getSpecVendor());		
		jvmInfo.append("\n");
		jvmInfo.append("\tSpec Version:");
		jvmInfo.append(runtimeMXBean.getSpecVersion());		
		jvmInfo.append("\n");
		jvmInfo.append("\tVM Name:");
		jvmInfo.append(runtimeMXBean.getVmName());		
		jvmInfo.append("\n");
		jvmInfo.append("\tVM Vendor:");
		jvmInfo.append(runtimeMXBean.getVmVendor());		
		jvmInfo.append("\n");
		jvmInfo.append("\tVM Version:");
		jvmInfo.append(runtimeMXBean.getVmVersion());		
		jvmInfo.append("\n");
		tracer.recordMetric(tracer.buildSegment(segmentPrefix, false, "JVM"), "VM Details", jvmInfo.toString());		
		tracer.recordMetric(tracer.buildSegment(segmentPrefix, false, "JVM"), "Available Processors", "" + cpuCount);
		try {
			if(isIBMOSMBean) {
				tracer.recordMetric(tracer.buildSegment(segmentPrefix, false, "JVM"), "Processing Capacity", getProcessingCapacityMethod.invoke(osMXBean, OBJECT_ARGS).toString());				
			}			
		} catch (Exception e) {
			if(logErrors) {
				log.error("Exception Collecting IBM OS MXBean Processing Capacity", e);
			}			
		} 
		try {
			if(isIBMOSMBean) {
				tracer.recordMetric(tracer.buildSegment(segmentPrefix, false, "JVM"), "Total Physical Memory", getTotalPhysicalMemoryMethod.invoke(osMXBean, OBJECT_ARGS).toString());				
			}			
		} catch (Exception e) {
			if(logErrors) {
				log.error("Exception Collecting IBM OS MXBean Total Physical Memory", e);
			}			
		} 
		
	}
	
	/**
	 * Tasllies the CPU time of all the threads in the JVM.
	 * @return The total CPU time of all threads.
	 */
	public long getProcessCPUTime() {
		long t = 0L;
		for(long ti: threadMXBean.getAllThreadIds()) {
			t = t + threadMXBean.getThreadCpuTime(ti);
		}
		return t;
	}
	
	/**
	 * Traces some MX stats for the current JVM.
	 * Stats are:<ul>
	 * <li>Compile time 
	 * <li>GC time
	 * <li>GC collections
	 * </ul>
	 */
	protected void readJVMMXStats() {
		long start = System.currentTimeMillis();
		try {
			// ==============================================
			// Trace OS Stats
			// ==============================================			
			long virtualMem = 0L;
			long physicalMem = 0L;
			long swapMem = 0L;
			long totalPhysicalMem = 0L;
			long totalSwapMem = 0L;
			int cpuCount = 0;
			
			long cpuTime = 0L;
			long totalTimeNs = 0L;
			long totalTimeMs = 0L;
			if(startTimeNs == -1) {
				startTimeNs = System.nanoTime();
				startTimeMs = System.currentTimeMillis();
			} else {
				totalTimeNs = (System.nanoTime() - startTimeNs);
				totalTimeMs = (System.currentTimeMillis() - startTimeMs);
				startTimeNs = System.nanoTime();
				startTimeMs = System.currentTimeMillis();
			}
			if(isSunOSMBean) {
				try {
					virtualMem = ((Long)getCommittedVirtualMemorySizeMethod.invoke(osMXBean, OBJECT_ARGS)).longValue();
					physicalMem = ((Long)getFreePhysicalMemorySizeMethod.invoke(osMXBean, OBJECT_ARGS)).longValue();
					swapMem = ((Long)getFreeSwapSpaceSizeMethod.invoke(osMXBean, OBJECT_ARGS)).longValue();
					cpuTime = ((Long)getProcessCpuTimeMethod.invoke(osMXBean, OBJECT_ARGS)).longValue();
					totalPhysicalMem = ((Long)getTotalPhysicalMemorySizeMethod.invoke(osMXBean, OBJECT_ARGS)).longValue();
					totalSwapMem = ((Long)getTotalSwapSpaceSizeMethod.invoke(osMXBean, OBJECT_ARGS)).longValue();
					
					cpuCount = osMXBean.getAvailableProcessors();
					
					tracer.recordCounterMetric(tracer.buildSegment(segmentPrefix, false, "JVM", "OS Memory"), "Committed Virtual Memory (bytes)", virtualMem);
					tracer.recordCounterMetric(tracer.buildSegment(segmentPrefix, false, "JVM", "OS Memory"), "Free Physical Memory (bytes)", physicalMem);
					tracer.recordCounterMetric(tracer.buildSegment(segmentPrefix, false, "JVM", "OS Memory"), "Free Swap Space (bytes)", swapMem);
					tracer.recordCounterMetric(tracer.buildSegment(segmentPrefix, false, "JVM", "OS Memory"), "Total Physical Memory (bytes)", totalPhysicalMem);
					tracer.recordCounterMetric(tracer.buildSegment(segmentPrefix, false, "JVM", "OS Memory"), "Total Swap Space (bytes)", totalSwapMem);
				
				
					tracer.recordCounterMetricDelta(tracer.buildSegment(segmentPrefix, false, "JVM"), "Process CPU Time", cpuTime);
					if(totalTimeNs != 0L && gcDeltas.get("TotalCPUTime") > 0) {
						long elapsedCPUTime = cpuTime-gcDeltas.get("TotalCPUTime");
						gcDeltas.put("TotalCPUTime", cpuTime);						
						tracer.recordCounterMetric(tracer.buildSegment(segmentPrefix, false, "JVM"), "Process CPU %", (long)(((float)elapsedCPUTime/(float)(totalTimeNs*cpuCount))*100));
					} else {
						gcDeltas.put("TotalCPUTime", cpuTime);
					}
					
				} catch (Exception e) {
					if(logErrors) {
						log.error("Exception Collecting OS Level JVM Stats", e);
					}
				}
				
			}
			// ==============================================
			// Trace Compile Time
			// ==============================================
			if(compilationMXBean.isCompilationTimeMonitoringSupported()) {
				tracer.recordMetricDelta(tracer.buildSegment(segmentPrefix, false, "JVM"), "Compile Time", compilationMXBean.getTotalCompilationTime());
			}
			// ==============================================
			// Trace GC Time and Incidents
			// ==============================================		
			long gcTimeTotal = 0L;
			long gcCountTotal = 0L;
			long gcTime = 0L;
			long gcCount = 0L;
			float percentGCTime = 0F;
			for(GarbageCollectorMXBean gcBean: garbageCollectorMXBeans) {
				gcTime = gcBean.getCollectionTime();
				gcCount = gcBean.getCollectionCount();
				gcTimeTotal = gcTimeTotal + gcTime;
				gcCountTotal = gcCountTotal + gcCount;			
				tracer.recordMetricDelta(tracer.buildSegment(segmentPrefix, false, "JVM", "GarbageCollectors", gcBean.getName()), "GC Time (ms)", gcTime);			
				tracer.recordMetricDelta(tracer.buildSegment(segmentPrefix, false, "JVM", "GarbageCollectors", gcBean.getName()), "GC Collections", gcCount);
				try {
					if(totalTimeMs>0) {						
						percentGCTime = (float)(gcTime-gcDeltas.get(gcBean.getName()))/(float)totalTimeMs *100;
						gcDeltas.put(gcBean.getName(), gcTime);
						tracer.recordCounterMetric(tracer.buildSegment(segmentPrefix, false, "JVM", "GarbageCollectors", gcBean.getName()), "GC % Time", (long)percentGCTime);
					}
				} catch (Exception e) {
					if(logErrors) {
						log.error("Unexpected Error In GC Time Collection", e);
					}													
				}
			}		
			tracer.recordMetricDelta(tracer.buildSegment(segmentPrefix, false, "JVM"), "GC Time (ms)", gcTimeTotal);
			tracer.recordMetricDelta(tracer.buildSegment(segmentPrefix, false, "JVM"), "GC Collections", gcCountTotal);			
			try {
				if(totalTimeMs>0) {
					percentGCTime = (float)gcTimeTotal/(float)totalTimeMs *100;
					percentGCTime = (float)(gcTimeTotal-gcDeltas.get("TotalGCTime"))/(float)totalTimeMs *100;
					gcDeltas.put("TotalGCTime", gcTimeTotal);					
					tracer.recordCounterMetric(tracer.buildSegment(segmentPrefix, false, "JVM"), "GC % Time", (long)percentGCTime);
				}
			} catch (Exception e) {
				if(logErrors) {
					log.error("Unexpected Error In Total GC Time Collection", e);
				}			
			}
			 
			tracer.recordCounterMetric(tracer.buildSegment(segmentPrefix, false, "JVM"), "Objects Pending Finalization", memoryMXBean.getObjectPendingFinalizationCount());
			
			// ==============================================
			// Heap & Non Heap Usage
			// ==============================================		
			traceMemoryUsage(memoryMXBean.getHeapMemoryUsage(), null, true);
			traceMemoryUsage(memoryMXBean.getNonHeapMemoryUsage(), null, false);
			
			
			// ==============================================
			// Memory Pools Usage
			// ==============================================
			for(MemoryPoolMXBean memoryBean: memoryPoolMXBeans) {
				if(!memoryBean.isValid()) continue;
				MemoryUsage memoryPoolUsage = memoryBean.getUsage();
				traceMemoryUsage(memoryPoolUsage, memoryBean.getName(), memoryBean.getType().equals(MemoryType.HEAP));
			}
		} catch (Exception e) {
			if(logErrors) {
				log.error("Unexpected Error In JVM Stats Collection", e);
			}			
		}
		collectTime = System.currentTimeMillis() - start;	
		if(traceCollectionTime) {
			tracer.recordMetric(tracer.buildSegment(segmentPrefix, false, "JVM"), "Collector Time (ms)", collectTime);
		}
	}
	
	
	
	/**
	 * Traces the data in a memory pool.
	 * <p>
	 * <ul><li>JVM</li>
	 * <ul><li>Type (Heap memory/Non-heap memory)</li>
	 * <ul><li>Memory Pools</li>
	 * <ul><li>Name</li>
	 * </ul></ul></ul></ul>
	 * @param memoryUsage The <code>MemoryUsage</code> object to be traced.
	 * @param name The name of the memory pool. Null if the <code>MemoryUsage</code> does not represent a memory pool. 
	 * @param isHeap True if stats are for heap, false otherwise.
	 */
	protected void traceMemoryUsage(MemoryUsage memoryUsage, String name, boolean isHeap) {
		if(memoryUsage==null) return;
		float percentageOfMax = 0F;
		float percentageOfCommitted = 0F;
		float committed = 0F;
		float used = 0F;
		float max = 0F;
		String segment = null;
		committed = memoryUsage.getCommitted();
		used = memoryUsage.getUsed();
		max = memoryUsage.getMax();
		percentageOfMax = used / max * 100;
		percentageOfCommitted = used / committed * 100;
		if(name==null) {
			segment = tracer.buildSegment(segmentPrefix, false, "JVM", ((String)(isHeap ? HEAP_MEMORY : NON_HEAP_MEMORY)));
		} else {
			segment = tracer.buildSegment(segmentPrefix, false, "JVM", ((String)(isHeap ? HEAP_MEMORY : NON_HEAP_MEMORY)), "Memory Pools", name);
		}
		
		tracer.recordCounterMetric(segment, MEMORY_COMMITTED, (long)committed);
		tracer.recordCounterMetric(segment, MEMORY_INIT, memoryUsage.getInit());
		tracer.recordCounterMetric(segment, MEMORY_MAX, (long)max);
		tracer.recordCounterMetric(segment, MEMORY_USED, (long)used);
		tracer.recordCounterMetric(segment, MEMORY_PERCENT_OF_COMMITTED, (long)percentageOfCommitted);
		tracer.recordCounterMetric(segment, MEMORY_PERCENT_OF_MAX, (long)percentageOfMax);

	}
}

/*
	$Log: JVMCollector.java  $
	Revision 1.7 2007/03/13 14:04:32EDT Whitehead, Nicholas (whitehen) 
	New In Release:
	
	    * Updated to support acquiring Processing Capacity from the IBM JVM's OperatingSystemMXBean (com.ibm.lang.management.OperatingSystemMXBean)if that JVM is being used. The Processing Capacity is the collective capacity of the virtual processors in the partition the VM is running in. The value returned is in units of 1% of a physical processor's capacity, so a value of 100 is equal to 1 physical processor.
	    * Updated to support acquiring Total Physical Memory from the IBM JVM's OperatingSystemMXBean
	
	Fixed In Release:
	
	    * Modified Implementation of Sun's Operating System MXBean to remove runtime dependency on the presence of the class. This will allow the collector to be used with the IBM JDK. The presence of the class is detemrined by class name matching and invocations are made through reflection.
	Revision 1.6 2007/03/12 18:42:32EDT Whitehead, Nicholas (whitehen) 
	New In Release:
	
	    * JVMCollector
	          o If supported, OS Level Stats for:
	                + Committed Virtual Memory
	                + Free Physical Memory
	                + Free Swap Space
	                + Total Physical Memory
	                + Total Swap Space
	                + Elapsed CPU Time For Process
	                + % CPU Time For Process
	                + Percentages Pending
	          o Number of pending objects ready for finalization.
	          o Traces Heap, NonHeap and Memory Pool Memory Stats:
	                + Committed
	                + Init
	                + Used
	                + Max
	                + % Used of Committed
	                + % Used of Max
	          o Traces Garrbage Collection Times for :
	                + Total GC Time
	                + For Each Garbage Collector Instance
	                + Elapsed Time
	                + Collections
	                + % Of Elapsed Time Dedicated to GC
	
	Fixed In Release:
	
	    * JVMCollector Fixes
	          o Modified to use standard SegmentPrefixElements configuration.
	          o Fixed defect in GarbageCollection time and invocations where seperate instances of the gc-bean were being tracd to the same segment. Now the total time and invocation count are added up for all instances and then traced.
	Revision 1.5 2007/02/26 18:10:04EST Whitehead, Nicholas (whitehen) 
	Delegated constructor banner to base class
	Revision 1.4 2007/02/26 18:06:09EST Whitehead, Nicholas (whitehen) 
	Implemented init() method.
	Revision 1.3 2007/01/22 11:01:19EST Whitehead, Nicholas (whitehen) 
	Modified tracing of "GC Time", "Compile Time" and "GC Collections" to deltas.
*/
