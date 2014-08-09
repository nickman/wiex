package com.heliosapm.wiex.server.tracing.collectors.solaris;


import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.heliosapm.wiex.jmx.dynamic.annotation.JMXAttribute;
import com.heliosapm.wiex.jmx.dynamic.annotation.JMXManagedObject;
import com.heliosapm.wiex.jmx.dynamic.annotation.JMXOperation;
import com.heliosapm.wiex.server.tracing.collectors.BaseCollector;
import com.heliosapm.wiex.server.tracing.collectors.SmartTracer;
import com.heliosapm.wiex.tracing.tracing.ITracer;
import com.heliosapm.wiex.tracing.tracing.TracerFactory;

/**
 * <p>Title: SolarisCollector</p>
 * <p>Description: Collects SAR Resource Utilization Data for Solaris using <code>sar</code></p> 
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Company: Helios Development Group</p>
 * @author Whitehead
 * @version $Revision: 1.2 $
 */
@JMXManagedObject(annotated=true, declared=false)
public class SolarisCollector extends BaseCollector {
	
	
	/**The user CPU time for the host	 */
	protected float user = 0F;
	/**The user CPU time for the JVM	 */
	protected float jvmUser = 0F;
	/**The swaps for the JVM	 */
	protected float jvmSwaps = 0F;
	/**The nice CPU time for the host	 */
	protected float nice = 0F;
	/**The system CPU time for the JVM	 */
	protected float jvmSystem = 0F;
	/**The system CPU time for the host	 */
	protected float system = 0F;
	/**The IO Wait CPU time for the host	 */
	protected float iowait = 0F;
	/**The idle CPU time for the host	 */
	protected float idle = 0F;
	/**The derived total CPU time for the host	 */
	protected float utilized = 0F;
	/**The system CPU request queue depth for the host	 */
	protected float queueSize = 0F;
	/**The system process list size for the host	 */
	protected float processListSize = 0F;
	/**The context switch rate for the host	 */
	protected float contextSwitches = 0F;
	/**The process id for the JVM */
	protected int pid = -1;
	/** The root segment */
	protected String baseSegment = "";
	/** The VMStat Decode for the segment */
	protected Map<Integer, String[]> VM_STAT_DECODE_SEGMENT = new HashMap<Integer, String[]>(22);
	/** Indicates if Disk Segment Headers are Set */
	protected boolean diskHeadersSet = false;
	/** Compiled Include Patterns and Type */
	protected Map<Pattern, String> compiledPatterns = new ConcurrentHashMap<Pattern, String>();
	/** Accepted Kernel Entries and Type */
	protected Map<String, String> acceptedStats = new ConcurrentHashMap<String, String>();
	/** Denied Kernel Entries */
	protected Set<String> deniedStats = new CopyOnWriteArraySet<String>(); 
	
	
	/**
	 * Adds an include on kstat metrics.
	 * This is a convenience method for direct bean manipulation.
	 * @param pattern The regex pattern of the kstat metric.
	 * @param traceType
	 */
	public void setInclude(String pattern, String traceType) {
		try {
			Pattern pt = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
			compiledPatterns.put(pt, traceType);
		} catch (Exception e) {
			if(logErrors) {
				log.error("Failed to add include pattern [" + pattern + "]", e);
			}
		}
	}
	
	
	
	protected void initVMStatMap() {
		int i = 0;
		ITracer it = TracerFactory.getInstance();
		
		VM_STAT_DECODE_SEGMENT.put(i++, new String[]{it.buildSegment("VMStat", "Process States"), "In Run Queue"});
		VM_STAT_DECODE_SEGMENT.put(i++, new String[]{it.buildSegment("VMStat", "Process States"), "Blocked For Resource"});
		VM_STAT_DECODE_SEGMENT.put(i++, new String[]{it.buildSegment("VMStat", "Process States"), "Swapped Runnable"});
		
		VM_STAT_DECODE_SEGMENT.put(i++, new String[]{it.buildSegment("VMStat", "Memory"), "Swap Space Available (kb)"});
		VM_STAT_DECODE_SEGMENT.put(i++, new String[]{it.buildSegment("VMStat", "Memory"), "Size of Free List (kb)"});
		
		VM_STAT_DECODE_SEGMENT.put(i++, new String[]{it.buildSegment("VMStat", "Paging"), "Page Reclaims"});
		VM_STAT_DECODE_SEGMENT.put(i++, new String[]{it.buildSegment("VMStat", "Paging"), "Mirror Faults"});
		VM_STAT_DECODE_SEGMENT.put(i++, new String[]{it.buildSegment("VMStat", "Paging"), "Kilopbytes Paged In"});
		VM_STAT_DECODE_SEGMENT.put(i++, new String[]{it.buildSegment("VMStat", "Paging"), "Kilopbytes Paged Out"});
		VM_STAT_DECODE_SEGMENT.put(i++, new String[]{it.buildSegment("VMStat", "Paging"), "Kilopbytes Freed"});
		VM_STAT_DECODE_SEGMENT.put(i++, new String[]{it.buildSegment("VMStat", "Paging"), "Short Term Memory Shortfall (kb)"});
		VM_STAT_DECODE_SEGMENT.put(i++, new String[]{it.buildSegment("VMStat", "Paging"), "Pages Scanned By Clock Algorithm"});
		
		VM_STAT_DECODE_SEGMENT.put(i++, new String[]{it.buildSegment("VMStat", "Disk Operation Rate"), ""});
		VM_STAT_DECODE_SEGMENT.put(i++, new String[]{it.buildSegment("VMStat", "Disk Operation Rate"), ""});
		VM_STAT_DECODE_SEGMENT.put(i++, new String[]{it.buildSegment("VMStat", "Disk Operation Rate"), ""});
		VM_STAT_DECODE_SEGMENT.put(i++, new String[]{it.buildSegment("VMStat", "Disk Operation Rate"), ""});	
		
		VM_STAT_DECODE_SEGMENT.put(i++, new String[]{it.buildSegment("VMStat", "Faults"), "Non Clock Device Interrupts"});
		VM_STAT_DECODE_SEGMENT.put(i++, new String[]{it.buildSegment("VMStat", "Faults"), "System Calls"});
		VM_STAT_DECODE_SEGMENT.put(i++, new String[]{it.buildSegment("VMStat", "Faults"), "Context Switches"});
		
		VM_STAT_DECODE_SEGMENT.put(i++, new String[]{it.buildSegment("VMStat", "CPU Time"), "User Time"});
		VM_STAT_DECODE_SEGMENT.put(i++, new String[]{it.buildSegment("VMStat", "CPU Time"), "System Time"});
		VM_STAT_DECODE_SEGMENT.put(i++, new String[]{it.buildSegment("VMStat", "CPU Time"), "Idle Time"});
		
	}
	
	
	
	 
	
	/**
	 * Instantiates a new LinuxSARCollector and initializes the MXBeans.
	 * Traces the JIT name.
	 */
	public SolarisCollector() {
		super();
		initVMStatMap();
	}
	
	/**
	 * Initializes the VERSION and MODULE.
	 * @see com.heliosapm.wiex.server.collectors.BaseCollector#init()
	 */
	public void init() {
		VERSION = "$Revision: 1.2 $";
		MODULE = "SolarisCollector";
	}	
		

	
	/**
	 * Collects Linux host performance data from sar.
	 * Acquires JVM's PID.
	 * Collects JVM performance data from sar.
	 * Collects MXBean stats.
	 * @see com.heliosapm.wiex.server.collectors.BaseCollector#collect()
	 */
	@JMXOperation(description="Collects SAR Stats", expose=true, name="collect")
	public void collect() {
		baseSegment = tracer.buildSegment(segmentPrefix, false, "");
		try {			
			long start = System.currentTimeMillis();
//			if(pid==-1) {
//				try {
//					pid = Integer.parseInt(readPid().trim());
//					TracerFactory.getInstance().recordMetric(baseSegment, "JVM PID", "" + pid);
//				} catch (Exception e) {
//					pid = -1;
//				}
//				
//			}
			processCPUStats(issueOSCommand("sar -u 1 1"));
			processVmstat(issueOSCommand("vmstat 1 2"));
			long kstart = System.currentTimeMillis();
			processKstat(issueOSCommand("kstat -p"));
			long kelapsed = System.currentTimeMillis()-kstart;
			log.info("==============> KStats Elapsed:" + kelapsed + " ms.");
//			processCPUStats(issueOSCommand(" mpstat -a 1 1; mpstat -p 1 1"));
//			processCPUQueueStats(issueOSCommand("sar -q 1"));
//			processContextSwitches(issueOSCommand("sar -w 1"));
//			processNetworkDeviceStats(issueOSCommand("sar -n DEV 1"));
//			processNetworkDeviceErrorStats(issueOSCommand("sar -n EDEV 1"));
//			processNetworkSocketStats(issueOSCommand("sar -n SOCK 1"));
//			processMemoryStats(issueOSCommand("sar -r 1"));
//			if(pid!=-1) {
//				processJVMCPUStats(issueOSCommand("sar -x " + pid + " 1"));
//			}
//			readJVMProcStatus();
//
//			processFileSystemStats(issueOSCommand("df -kT"));
			collectTime = System.currentTimeMillis()-start;
			TracerFactory.getInstance().recordMetric(
					tracer.getStringBuilder().append(baseSegment).append(delim).append(MODULE).toString(), 
					"Collect Time", 
					collectTime);
		} catch (Exception e) {
			
		}
		
	}
	
	/**
	 * Issues the given string as an OS command and returns the standard output of the command.
	 * @param command An arbitrary OS shell command.
	 * @return The standard output of the executed command.
	 */
	protected String issueOSCommand(String command) {
		StringBuilder buff = new StringBuilder();
		InputStream reader = null;
		try {
			Process p = Runtime.getRuntime().exec(command);
			reader = new BufferedInputStream(p.getInputStream());			
			for (;;) {
				int c = reader.read();
				if (c == -1)
					break;
				buff.append((char) c);
			}
			return buff.toString();
		} catch (Exception e) {
			return null;
		} finally {
			try { reader.close(); } catch (Exception e) {}
		}
	}
	
	/**
	 * Traces CPU statistics for all CPUs on the host.
	 * Parses the whole output of <code>sar -u 1 1</code>
	 * Extracts one line for each host CPU plus <b>all</b> and traces: <ul>
	 * <li>system
	 * <li>nice
	 * <li>iowait
	 * <li>user
	 * <li>idle</ul> 
	 * First line is the summarized CPU stats, with each subsequent line representing one individual CPU.
	 * @param output The output of the OS command.
	 */
	protected void processCPUStats(String output) {
		if(output==null) return;
		String[] lines = output.split("\n");
		boolean isSummary = true;
		for(String s: lines) {
			if(!s.contains("SunOS") && !s.contains("usr") && s.contains(":") && s.trim().length() > 1) {
				String tmp = s.replaceAll("\\s+", "~") + "\n";
				if(isSummary) {
					processCPULine(tmp, "all");
					isSummary=false;
				} else {
					//String[] cols = s.split("~");
					//processCPULine(tmp, cols[0]);
				}
				                	                              
			}
		}
	}
	
	
	/**
	 * Traces stats output on the target host.
	 * Parses the last line output of of <code>vmstat 1 2</code>.
	 * Sample output:
	 * <pre>
		 procs     memory            page            disk          faults      cpu
		 r b w   swap  free  re  mf pi po fr de sr s6 s1 s4 s8   in   sy   cs us sy id
		 0 0 0 16813360 8775624 1694 2032 967 528 524 0 27 0 58 1 0 679 858 695 185 49 193
		 0 0 0 21254552 13543840 1 6 0  0  0  0  0  0  0  0  0  358 1416  553  1  0 99
	 * </pre>
	 * @param output
	 */
	protected void processVmstat(String output) {
		
		if(output==null) return;
		String[] lines = output.split("\n");
		String[] headers = lines[1].trim().split("\\s+");
		String line = lines[lines.length-1].trim();
		String[] values = line.split("\\s+");
		if(!diskHeadersSet) {
			int i = 12;
			VM_STAT_DECODE_SEGMENT.get(i)[1] = headers[i]; i++;
			VM_STAT_DECODE_SEGMENT.get(i)[1] = headers[i]; i++;
			VM_STAT_DECODE_SEGMENT.get(i)[1] = headers[i]; i++;
			VM_STAT_DECODE_SEGMENT.get(i)[1] = headers[i];
			// Prepend baseSegment
			for(String[] names: VM_STAT_DECODE_SEGMENT.values()) {
				names[0] = tracer.buildSegment(baseSegment, names[0]); 
			}
			diskHeadersSet=true;
		}
		String seg = null;
		String metric = null;
		long lval = 0L;
		int i = 0;
		for(String val: values) {
			try {
				seg = VM_STAT_DECODE_SEGMENT.get(i)[0];
				metric = VM_STAT_DECODE_SEGMENT.get(i)[1];
				lval = Long.parseLong(val);
				tracer.recordCounterMetric(seg, metric, lval);
			} catch (Exception e) {
				if(logErrors) {
					log.error("Failed to trace value [" + val + "] for [" + seg + "/" + metric + "]", e);
				}
			}
			i++;
		}
	}
	
	protected void processKstat(String output) {
		String[] fragment = null;
		String kMetricName = null;
		String[] nameVal = null;
		String lastVal = null;
		String metricType = null;
		float f = 0F;
		long l = 0L;
		String[] lines = output.split("\n");
		
		for(String line: lines) {
			try {
				fragment = line.split(":");
				kMetricName = line.split("\\s+")[0];
				metricType = isIncluded(kMetricName);
				if(metricType==null) continue;
				nameVal = fragment[3].replaceAll("\\s+", ":").split(":");
				if(line.endsWith("vmem")) continue;
				if(nameVal.length > 2) {
					lastVal = nameVal[nameVal.length-1];
					nameVal[1] = lastVal;
					nameVal[0] = fragment[3].split(lastVal)[0].trim();
				}
				
				if(nameVal[1].contains(".")) {
					f = Float.parseFloat(nameVal[1]);
					l = (long)f;
					nameVal[1] = "" + l;
				}
				SmartTracer.recordTrace(tracer.buildSegment(baseSegment, "kstats", fragment[0], fragment[1], fragment[2]), nameVal[0], nameVal[1], metricType);				
			} catch (Exception e) {
				//log.error("kstat failed:[" + line + "]", e);
			}
		}
	}
	
	/**
	 * Determines if a kmetric is included for tracing.
	 * @param kmetric The full kmetric name. 
	 * @return If the metric is included, the metric type is returned. Otherwise, returns null.
	 */
	protected String isIncluded(String kmetric) {
		if(deniedStats.contains(kmetric)) return null;
		if(acceptedStats.containsKey(kmetric)) return acceptedStats.get(kmetric);
		for(Entry<Pattern, String> entry: compiledPatterns.entrySet()) {
			Matcher m = entry.getKey().matcher(kmetric);
			if(m.matches()) {
				acceptedStats.put(kmetric, entry.getValue());
				return entry.getValue();
			}
		}
		deniedStats.add(kmetric);
		return null;
	}
	
	/**
	 * Parse helper for CPU stats.
	 * @param s A line of text from <code>sar -u 1 1</code>
	 * @param cpuName The name of the CPU for which stats are being read.
	 */
	protected void processCPULine(String s, String cpuName) {
		String[] cols = s.split("~");
		String segment = tracer.buildSegment(baseSegment,"CPU Summary",cpuName);
		float _user = 0F;
		float _system = 0F;
		float _iowait = 0F;
		float _idle = 0F;
		float _utilized = 0;
		
		try {
			_user = Float.parseFloat(cols[1]);
			tracer.recordCounterMetric(segment, "User", (int)_user);
			_system = Float.parseFloat(cols[2]);
			tracer.recordCounterMetric(segment, "System", (int)_system);
			_iowait = Float.parseFloat(cols[3]);
			tracer.recordCounterMetric(segment, "Wait", (int)_iowait);
			_idle = Float.parseFloat(cols[4]);
			tracer.recordCounterMetric(segment, "Idle", (int)_idle);
			_utilized = 100F-_idle;
			tracer.recordCounterMetric(segment, "Utilization", (int)_utilized);
			if(cpuName.equalsIgnoreCase("all")) {
				user = _user;
				system = _system;
				iowait = _iowait;
				idle = _idle;
				utilized = _utilized;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}	
	
	/**
	 * Traces CPU request queue and process list size for the host.
	 * Parses the output stream of the command <code>sar -q 1</code>.
	 * Traces the CPU request queue size and the process list size.
	 * @param output The output of the command <code>sar -q 1</code>.
	 */
	protected void processCPUQueueStats(String output) {
		if(output==null) return;
		try {
			String[] lines = output.split("\n");
			for(String s: lines) {
				if(s.contains("Average")) {
					String tmp = s.replaceAll("\\s+", "~") + "\n";
					String[] cols = tmp.split("~");
					queueSize = Float.parseFloat(cols[1]);
					tracer.recordCounterMetric(
							tracer.buildSegment(baseSegment, "CPU Summary"),							 
							"CPU Queue Size", 
							(int)queueSize);
					processListSize = Float.parseFloat(cols[2]);
					tracer.recordCounterMetric(
							tracer.buildSegment(baseSegment, "CPU Summary"), 
							"Process List Size", 
							(int)processListSize);
				}
			}
		} catch (Exception e) {}
	}	
	
	/**
	 * Traces general network statistics for all network devices.
	 * Parses the output stream of the command <code>sar -n DEV 1</code>.
	 * Traces the following for each device:<ul>
	 * <li>Packets Received/s
	 * <li>Packets Transmitted/s
	 * <li>Packets Transmitted/s
	 * <li>Bytes Transmitted/s
	 * <li>Compressed Packets Received/s
	 * <li>Compressed Packets Transmitted/s
	 * <li>Multicast Packets Received/s
	 * </ul>
	 * @param output The output of the command <code>sar -n DEV 1</code>.
	 */
	protected void processNetworkDeviceStats(String output) {
		if(output==null) return;
		try {
			String[] lines = output.split("\n");
			float val = 0F;
			String netInterface = null;
			for(String s: lines) {
				if(s.contains("Average") && !s.contains("IFACE")) {
					String tmp = s.replaceAll("\\s+", "~") + "\n";
					String[] cols = tmp.split("~");
					netInterface = tracer.buildSegment(baseSegment, "Network Devices", cols[1]); 
					val = Float.parseFloat(cols[2]);
					tracer.recordCounterMetric(netInterface, "Packets Received/s", (int)val);
					val = Float.parseFloat(cols[3]);
					tracer.recordCounterMetric(netInterface, "Packets Transmitted/s", (int)val);					
					val = Float.parseFloat(cols[4]);
					tracer.recordCounterMetric(netInterface, "Bytes Received/s", (int)val);					
					val = Float.parseFloat(cols[5]);
					tracer.recordCounterMetric(netInterface, "Bytes Transmitted/s", (int)val);					
					val = Float.parseFloat(cols[6]);
					tracer.recordCounterMetric(netInterface, "Compressed Packets Received/s", (int)val);					
					val = Float.parseFloat(cols[7]);
					tracer.recordCounterMetric(netInterface, "Compressed Packets Transmitted/s", (int)val);					
					val = Float.parseFloat(cols[8]);
					tracer.recordCounterMetric(netInterface, "Multicast Packets Received/s", (int)val);										
				}
			}
		} catch (Exception e) {}
	}
	
	/**
	 * Traces errors for all network devices.
	 * Parses the output stream of the command <code>sar -n EDEV 1</code>.
	 * Traces the following for each device:<ul>
	 * <li>Bad Packets Received/s
	 * <li>Transmission Errors/s
	 * <li>Transmission Collisions/s
	 * <li>Received Dropped Packets/s
	 * <li>Transmitted Dropped Packets/s
	 * <li>Carrier Errors/s
	 * <li>Frame Alignment Error/s
	 * <li>Receive FIFO Overrun Error/s
	 * <li>Transmit FIFO Overrun Error/s
	 * </ul>
	 * @param output The output of the command <code>sar -n EDEV 1</code>.
	 */
	protected void processNetworkDeviceErrorStats(String output) {
		if(output==null) return;
		try {
			String[] lines = output.split("\n");
			float val = 0F;
			String netInterface = null;
			for(String s: lines) {
				if(s.contains("Average") && !s.contains("IFACE")) {
					String tmp = s.replaceAll("\\s+", "~") + "\n";
					String[] cols = tmp.split("~");
					netInterface = tracer.buildSegment(baseSegment, "Network Devices", cols[1]);
					val = Float.parseFloat(cols[2]);
					tracer.recordCounterMetric(netInterface, "Bad Packets Received/s", (int)val);
					val = Float.parseFloat(cols[3]);
					tracer.recordCounterMetric(netInterface, "Transmission Errors/s", (int)val);					
					val = Float.parseFloat(cols[4]);
					tracer.recordCounterMetric(netInterface, "Transmission Collisions/s", (int)val);					
					val = Float.parseFloat(cols[5]);
					tracer.recordCounterMetric(netInterface, "Received Dropped Packets/s", (int)val);					
					val = Float.parseFloat(cols[6]);
					tracer.recordCounterMetric(netInterface, "Transmitted Dropped Packets/s", (int)val);					
					val = Float.parseFloat(cols[7]);
					tracer.recordCounterMetric(netInterface, "Carrier Errors/s", (int)val);					
					val = Float.parseFloat(cols[8]);
					tracer.recordCounterMetric(netInterface, "Frame Alignment Error/s", (int)val);
					val = Float.parseFloat(cols[9]);
					tracer.recordCounterMetric(netInterface, "Receive FIFO Overrun Error/s", (int)val);										
					val = Float.parseFloat(cols[10]);
					tracer.recordCounterMetric(netInterface, "Transmit FIFO Overrun Error/s", (int)val);										
					
				}
			}
		} catch (Exception e) {}
	}		
	
	/**
	 * Traces socket usage statistics for the host.
	 * Parses the output stream of the command <code>sar -n SOCK 1</code>.
	 * Traces the following for the host:<ul>
	 * <li>Total Used Sockets
	 * <li>TCP Sockets In Use
	 * <li>UDP Sockets In Use
	 * <li>Raw Sockets In Use
	 * <li>IP Fragments In Use
	 * </ul>
	 * @param output The output of the command <code>sar -n SOCK 1</code>.
	 */	
	protected void processNetworkSocketStats(String output) {
		if(output==null) return;
		try {
			String[] lines = output.split("\n");
			float val = 0F;
			String netInterface = null;
			for(String s: lines) {
				if(s.contains("Average")) {
					String tmp = s.replaceAll("\\s+", "~") + "\n";
					String[] cols = tmp.split("~");
					netInterface = tracer.buildSegment(baseSegment, "Network Devices");
					val = Float.parseFloat(cols[1]);
					tracer.recordCounterMetric(netInterface, "Total Used Sockets", (int)val);
					val = Float.parseFloat(cols[2]);
					tracer.recordCounterMetric(netInterface, "TCP Sockets In Use", (int)val);					
					val = Float.parseFloat(cols[3]);
					tracer.recordCounterMetric(netInterface, "UDP Sockets In Use", (int)val);					
					val = Float.parseFloat(cols[4]);
					tracer.recordCounterMetric(netInterface, "Raw Sockets In Use", (int)val);					
					val = Float.parseFloat(cols[5]);
					tracer.recordCounterMetric(netInterface, "IP Fragments In Use", (int)val);					
				}
			}
		} catch (Exception e) {}
	}			
	
	
	/**
	 * Traces Linux host file systemn usage stats.
	 * Parses the output stream of the command <code>df -kT</code>.
	 * Traces the following for the host:<ul>
	 * <li>File System Name
	 * <li>File System Type
	 * <li>File System Mount Name
	 * <li>Total 1K Blocks
	 * <li>Used 1K Blocks
	 * <li>Available 1K Blocks
	 * <li>Percentage Utilized
     * </ul> 
	 * @param output The output of the command <code>df -kT</code>.
	 */
	protected void processFileSystemStats(String output) {
		if(output==null) return;
		try {
			String[] lines = output.split("\n");
			float val = 0F;
			String fileSystem = null;
			for(String s: lines) {
				if(!s.contains("Filesystem")) {
					String tmp = s.replaceAll("\\s+", "~") + "\n";
					String[] cols = tmp.split("~");
					
					fileSystem = tracer.buildSegment(baseSegment, "File System", cols[0]);
					tracer.recordMetric(fileSystem, "File System Type", cols[1]);
					tracer.recordMetric(fileSystem, "Mounted On", cols[6]);
					
					val = Float.parseFloat(cols[2]);
					tracer.recordCounterMetric(fileSystem, "1K-Blocks", (int)val);				
					val = Float.parseFloat(cols[3]);
					tracer.recordCounterMetric(fileSystem, "Used", (int)val);
					val = Float.parseFloat(cols[4]);
					tracer.recordCounterMetric(fileSystem, "Available", (int)val);
					val = Float.parseFloat(cols[5].replace("%", "").trim());
					tracer.recordCounterMetric(fileSystem, "Percentage Used", (int)val);
				}
			}
		} catch (Exception e) {}
	}	
	
	/**
	 * Traces Linux host memory stats.
	 * Parses the output stream of the command <code>sar -r 1</code>.
	 * Traces the following for the host:<ul>
	 * <li>Free Memory/kb
	 * <li>Used Memory/kb
	 * <li>Memory In Use/%
	 * <li>Kernel Buffers/kb
	 * <li>Free Swap Space/kb
	 * <li>Used Swap Space/kb
	 * <li>Used Swap Space/%
	 * <li>Cached Swap Memory/kb
	 * </ul>
	 * @param output The output of the command <code>sar -r 1</code>.
	 */		
	protected void processMemoryStats(String output) {
		if(output==null) return;
		try {
			String[] lines = output.split("\n");
			float val = 0F;
			String netInterface = null;
			for(String s: lines) {
				if(s.contains("Average")) {
					String tmp = s.replaceAll("\\s+", "~") + "\n";
					String[] cols = tmp.split("~");
					netInterface = tracer.buildSegment(baseSegment, "Memory");
					val = Float.parseFloat(cols[1]);
					tracer.recordCounterMetric(netInterface, "Free Memory/kb", (int)val);
					val = Float.parseFloat(cols[2]);
					tracer.recordCounterMetric(netInterface, "Used Memory/kb", (int)val);					
					val = Float.parseFloat(cols[3]);
					tracer.recordCounterMetric(netInterface, "Memory In Use/%", (int)val);					
					val = Float.parseFloat(cols[4]);
					tracer.recordCounterMetric(netInterface, "Kernel Buffers/kb", (int)val);					
					val = Float.parseFloat(cols[5]);
					tracer.recordCounterMetric(netInterface, "Kernel Cache/kb", (int)val);
					val = Float.parseFloat(cols[6]);
					tracer.recordCounterMetric(netInterface, "Free Swap Space/kb", (int)val);
					val = Float.parseFloat(cols[7]);
					tracer.recordCounterMetric(netInterface, "Used Swap Space/kb", (int)val);
					val = Float.parseFloat(cols[8]);
					tracer.recordCounterMetric(netInterface, "Used Swap Space/%", (int)val);
					val = Float.parseFloat(cols[9]);
					tracer.recordCounterMetric(netInterface, "Cached Swap Memory/kb", (int)val);
					
				}
			}
		} catch (Exception e) {}
	}				
	

	/**
	 * Traces Linux context switches.
	 * Parses the output stream of the command <code>sar -w 1</code>.
	 * Traces the following for the host:<ul>
	 * <li>Context Switches/s
	 * </ul>
	 * @param output The output of the command <code>sar -w 1</code>.
	 */			
	protected void processContextSwitches(String output) {
		if(output==null) return;
		try {
			String[] lines = output.split("\n");
			for(String s: lines) {
				if(s.contains("Average")) {
					String tmp = s.replaceAll("\\s+", "~") + "\n";
					String[] cols = tmp.split("~");
					contextSwitches = Float.parseFloat(cols[1]);
					tracer.recordCounterMetric(tracer.buildSegment(baseSegment,"CPU Summary"), "Context Switches", (int)contextSwitches);
				}
			}
		} catch (Exception e) {}
	}		
	

	
	/**
	 * Traces CPU utilization statistics for the current JVM.
	 * Parses the output stream of the command <code>sar -x pid 1</code>.
	 * Traces the following for the current JVM process:<ul>
	 * <li>User CPU
	 * <li>System CPU
	 * <li>Swaps
	 * </ul>
	 * @param output The output of the command <code>sar -w 1</code>.
	 */
	protected void processJVMCPUStats(String output) {
		String jvmPrefix = tracer.buildSegment(baseSegment, false, "JVM");
		if(output==null) return;
		try {
			String[] lines = output.split("\n");
			for(String s: lines) {
				if(s.contains("Average")) {
					String tmp = s.replaceAll("\\s+", "~") + "\n";
					String[] cols = tmp.split("~");
					jvmUser = Float.parseFloat(cols[4]);
					tracer.recordCounterMetric(jvmPrefix, "User CPU", (int)jvmUser);
					jvmSystem = Float.parseFloat(cols[5]);
					tracer.recordCounterMetric(jvmPrefix, "System CPU", (int)jvmSystem);
					jvmSwaps = Float.parseFloat(cols[6]);
					tracer.recordCounterMetric(jvmPrefix, "Swaps", (int)jvmSwaps);					
				}
			}
		} catch (Exception e) {}
	}	
	
	
	/**
	 * Reads the contents of the target JVM's /proc/status file into a string. 
	 * @return A string representation of the target JVM's /proc/status file.
	 * @throws Exception
	 */
	protected String getProcStatus() throws Exception {
		BufferedInputStream bis = null;
		FileInputStream fis = null;
		try {
			fis = new FileInputStream("/proc/" + pid + "/status");
			bis = new BufferedInputStream(fis);
			StringBuilder buff = new StringBuilder();
			byte[] bytes = new byte[1024];
			int bytesRead = 0;
			while(true) {
				bytesRead = bis.read(bytes);
				if(bytesRead==-1) break;
				buff.append(new String(bytes, 0, bytesRead));				
			}
			String status = new String(bytes);
			return status;
		} finally {
			try { fis.close(); } catch (Exception e) {}
			try { bis.close(); } catch (Exception e) {}			
		}
	}
	
	/**
	 * Traces proc stats for the current JVM process.
	 * The stats are as follows:<ul>
	 * <li>SleepAVG
	 * <li>VmSize
	 * <li>VmLck
	 * <li>VmRSS
	 * <li>VMData
	 * <li>VMStk
	 * <li>VMExe
	 * <li>VMLib
	 * <li>Threads
	 * </ul>
	 */
	protected void readJVMProcStatus() {
		try {
			String status = getProcStatus();
			String[] lines = status.split("\n");
			float val = 0F;
			for(String line: lines) {
				String[] cols = line.split(":");
				if(cols[0].equalsIgnoreCase("SleepAVG")) {
					try {
						val = Float.parseFloat(cols[1].replace("%", "").trim());
						tracer.recordCounterMetric("JVM", "SleepAVG", (int)val);
					} catch (Exception e) {}
				} else if(cols[0].equalsIgnoreCase("VmSize")) {
					try {
						val = Float.parseFloat(cols[1].replace("kB", "").trim());
						tracer.recordCounterMetric("JVM", "VmSize", (int)val);
					} catch (Exception e) {}					
				} else if(cols[0].equalsIgnoreCase("VmLck")) {
					try {
						val = Float.parseFloat(cols[1].replace("kB", "").trim());
						tracer.recordCounterMetric("JVM", "VmLck", (int)val);
					} catch (Exception e) {}										
				} else if(cols[0].equalsIgnoreCase("VmRSS")) {
					try {
						val = Float.parseFloat(cols[1].replace("kB", "").trim());
						tracer.recordCounterMetric("JVM", "VmRSS", (int)val);
					} catch (Exception e) {}															
				} else if(cols[0].equalsIgnoreCase("VmData")) {
					try {
						val = Float.parseFloat(cols[1].replace("kB", "").trim());
						tracer.recordCounterMetric("JVM", "VmData", (int)val);
					} catch (Exception e) {}																				
				} else if(cols[0].equalsIgnoreCase("VmStk")) {
					try {
						val = Float.parseFloat(cols[1].replace("kB", "").trim());
						tracer.recordCounterMetric("JVM", "VmStk", (int)val);
					} catch (Exception e) {}																									
				} else if(cols[0].equalsIgnoreCase("VmExe")) {
					try {
						val = Float.parseFloat(cols[1].replace("kB", "").trim());
						tracer.recordCounterMetric("JVM", "VmExe", (int)val);
					} catch (Exception e) {}																														
				} else if(cols[0].equalsIgnoreCase("VmLib")) {
					try {
						val = Float.parseFloat(cols[1].replace("kB", "").trim());
						tracer.recordCounterMetric("JVM", "VmLib", (int)val);
					} catch (Exception e) {}																																			
				} else if(cols[0].equalsIgnoreCase("Threads")) {
					try {
						val = Float.parseFloat(cols[1].trim());
						tracer.recordCounterMetric("JVM", "Threads", (int)val);
					} catch (Exception e) {}																																								
				}
			}
		} catch (Exception e) {
			
		}
			
	}
	
	


	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SolarisCollector collector = new SolarisCollector();
		collector.setSegmentPrefixElements("EasyPayNet,Oracle,Solaris".split(","));
		//collector.setKnownHostsFile("");
		collector.collect();
		System.out.println(collector.toString());
		System.out.println("PID:" + collector.readPid());

	}

	/**
	 * JMX Attribute for average idle time across all CPUs.
	 * @return the idle
	 */
	@JMXAttribute(description="Percentage of time that the CPU or CPUs were idle and the system did not have an outstanding disk I/O request.", name="Idle")
	public float getIdle() {
		return idle;
	}

	/**
	 * JMX Attribute for average io wait time across all CPUs.
	 * @return the iowait
	 */
	@JMXAttribute(description="Percentage of time that the CPU or CPUs were idle during which the system had an outstanding disk I/O request.", name="IOWait")
	public float getIowait() {
		return iowait;
	}

	/**
	 * JMX Attribute for average nice time across all CPUs.
	 * @return the nice
	 */
	@JMXAttribute(description="Percentage of CPU utilization that occurred while executing at the user level with nice priority.", name="Nice")	
	public float getNice() {
		return nice;
	}

	/**
	 * JMX Attribute for average system time across all CPUs.
	 * @return the system
	 */
	@JMXAttribute(description="Percentage of CPU utilization that occurred while executing at the system level (kernel).", name="System")
	public float getSystem() {
		return system;
	}

	/**
	 * JMX Attribute for average user time across all CPUs.
	 * @return the user
	 */
	@JMXAttribute(description=" Percentage of CPU utilization that occurred while executing at the user level (application).", name="User")
	public float getUser() {
		return user;
	}

	/**
	 * JMX Attribute for average utilization across all CPUs.
	 * @return the utilized
	 */
	@JMXAttribute(description="The overall percentage CPU Utilization", name="Utilization")
	public float getUtilized() {
		return utilized;
	}
	
	/**
	 * Attempts to determine the process ID of the current JVM.
	 * @return A string representation of the JVM's process ID.
	 */
	@JMXOperation(description="Reports the VM's PID", expose=true, name="reportPid")	
	public String readPid() {
		InputStream reader = null;
		StringBuilder buff = new StringBuilder();
		try {		
			ProcessBuilder pb = new ProcessBuilder(new String[] {"/bin/bash", "-c", "\"\"echo $PPID\"\""});			
			pb.redirectErrorStream(true);
			Process p = pb.start();
			reader = new BufferedInputStream(p.getInputStream());			
			for (;;) {
				int c = reader.read();
				if (c == -1)
					break;
				buff.append((char) c);
			}
			//String[] cols = buff.toString().split(" ");
			//return cols[4].replace(":", "");
			return buff.toString();
			
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		} finally {
			try { reader.close(); } catch (Exception e) {}
		}
	}		
	
	
	
	

		/**
		 * @return the basic string representation of the CPU stats. 
		 * @see java.lang.Object#toString()
		 */
		public String toString() {
			StringBuilder buffer = new StringBuilder();
			buffer.append("Solaris Stats Collector[\n");
			buffer.append("\tidle = ").append(idle).append("\n");
			buffer.append("\tiowait = ").append(iowait).append("\n");
			buffer.append("\tnice = ").append(nice).append("\n");
			buffer.append("\tsystem = ").append(system).append("\n");
			buffer.append("\tuser = ").append(user).append("\n");
			buffer.append("\tutilized = ").append(utilized).append("\n");
			buffer.append("]").append("\n");
			return buffer.toString();
		}



	/**
	 * JMX Attribute for the current process list size.
	 * @return the processListSize
	 */
	@JMXAttribute(description="Number of processes in the process list.", name="ProcessListSize")	
	public float getProcessListSize() {
		return processListSize;
	}

	/**
	 * JMX Attribute for the CPU run queue size.
	 * @return the queueSize
	 */
	@JMXAttribute(description="Run queue length (number of processes waiting for run time).", name="QueueSize")
	public float getQueueSize() {
		return queueSize;
	}

	/**
	 * JMX Attribute for the current rate of context switches.
	 * @return the contextSwitches
	 */
	@JMXAttribute(description="Total number of context switches per second.", name="ContextSwitches")
	public float getContextSwitches() {
		return contextSwitches;
	}

	/**
	 * The currently determined PID for the current JVM.
	 * @return the pid
	 */
	@JMXAttribute(description="The process ID of this JVM", name="PID")
	public int getPid() {
		return pid;
	}

	/**
	 * The current rate of the JVM's swaps.
	 * @return the jvmSwaps
	 */
	@JMXAttribute(description="Number  of  pages from the JVM's process address spaces the system has swapped out per second.", name="JVMSwaps")
	public float getJvmSwaps() {
		return jvmSwaps;
	}

	/**
	 * JMX Attribute for the current JVM's system cpu time.
	 * @return the jvmSystem
	 */
	@JMXAttribute(description="Percentage of CPU used by the JVM while  executing  at  the  system  level(kernel).", name="JVMSystem")
	public float getJvmSystem() {
		return jvmSystem;
	}

	/**
	 * JMX Attribute for the current JVM's user cpu time.
	 * @return the jvmUser
	 */
	@JMXAttribute(description="Percentage  of  CPU  used  by  the  JVM while executing at the user level(application).", name="JVMUser")
	public float getJvmUser() {
		return jvmUser;
	}
	

}
