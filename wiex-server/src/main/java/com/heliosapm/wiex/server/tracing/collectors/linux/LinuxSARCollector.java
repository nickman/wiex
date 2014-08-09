
package com.heliosapm.wiex.server.tracing.collectors.linux;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.heliosapm.wiex.jmx.dynamic.annotation.JMXAttribute;
import com.heliosapm.wiex.jmx.dynamic.annotation.JMXManagedObject;
import com.heliosapm.wiex.jmx.dynamic.annotation.JMXOperation;
import com.heliosapm.wiex.server.tracing.collectors.BaseCollector;
import com.heliosapm.wiex.tracing.tracing.TracerFactory;

/**
 * <p>Title: LinuxSARCollector</p>
 * <p>Description: Collects SAR Resource Utilization Data for Linux using <code>sar</code></p> 
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Company: Helios Development Group</p>
 * @author Whitehead
 * @version $Revision: 1.8 $
 */
@JMXManagedObject(annotated=true, declared=false)
public class LinuxSARCollector extends BaseCollector {
	
	
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
	/**The total number of open files */
	protected int openFiles = 0;
	
	/**The process id for the JVM */
	protected int pid = -1;
	/** Process Locators <code><Description, Command></code> */
	protected Map<String, String> processLocators = new HashMap<String, String>();
	/** Directory Sizers <code><Description, Directory Name></code> */
	protected Map<String, String> dirSizers = new HashMap<String, String>();
	/** Port Testers <code><Description, Address Port></code> */
	protected Map<String, String> portTesters = new HashMap<String, String>();
	/** Pattern Matcher to diferentiate between address and host names */
	protected static Pattern ipAddressPattern = Pattern.compile("(([0-2]?[0-5]?[0-5]\\.[0-2]?[0-5]?[0-5]\\.[0-2]?[0-5]?[0-5]\\.[0-2]?[0-5]?[0-5])|((([a-zA-Z0-9\\-]))))");
	
	
	
	

	/**
	 * Instantiates a new LinuxSARCollector and initializes the MXBeans.
	 * Traces the JIT name.
	 */
	public LinuxSARCollector() {
		super();
	}
	
	/**
	 * Initializes the VERSION and MODULE.
	 * @see com.heliosapm.wiex.server.collectors.BaseCollector#init()
	 */
	public void init() {
		VERSION = "$Revision: 1.8 $";
		MODULE = "LinuxSARCollector";
	}		

	
	/**
	 * Collects Linux host performance data from sar.
	 * Acquires JVM's PID.
	 * Collects JVM performance data from sar.
	 * Collects MXBean stats.
	 * @see com.heliosapm.wiex.server.collectors.BaseCollector#collect()
	 */
	@JMXOperation(description="Collects Linux Stats", expose=true, name="collect")
	public void collect() {
		try {			
			long start = System.currentTimeMillis();
			if(pid==-1) {
				try {
					pid = Integer.parseInt(readPid().trim());
					TracerFactory.getInstance().recordMetric(segmentPrefix, "JVM PID", "" + pid);
				} catch (Exception e) {
					pid = -1;
				}
				
			}
			processCPUStats(issueOSCommand("sar -P ALL 1"));
			processCPUQueueStats(issueOSCommand("sar -q 1"));
			processContextSwitches(issueOSCommand("sar -w 1"));
			processNetworkDeviceStats(issueOSCommand("sar -n DEV 1"));
			processNetworkDeviceErrorStats(issueOSCommand("sar -n EDEV 1"));
			processNetworkSocketStats(issueOSCommand("sar -n SOCK 1"));
			processMemoryStats(issueOSCommand("sar -r 1"));
			if(pid!=-1) {
				processJVMCPUStats(issueOSCommand("sar -x " + pid + " 1"));
			}
			readJVMProcStatus();

			processFileSystemStats(issueOSCommand("df -kT"));
			try {
				processDirSizerList();
			} catch (Exception e) {}
			try {
				processPortTesterList();
			} catch (Exception e) {}
			
			try {
				processPidList();
			} catch (Exception e) {}
			
			processOpenSystemFiles(shell("/usr/bin/lsof | wc -l"));
			
			collectTime = System.currentTimeMillis()-start;
			if(traceCollectionTime) {
				tracer.recordMetric(tracer.buildSegment(segmentPrefix, false, MODULE), "Collect Time", collectTime);
			}
		} catch (Exception e) {
			
		}
		
	}
	
	/**
	 * Iterates through the pidLocator list and executes tracing for each one. 
	 */
	public void processPidList() {
		for(Entry<String, String> entry: processLocators.entrySet()) {
			String descr = entry.getKey();
			traceResourcesForPids(getPidsFromPidLocator(entry), descr);
		}
	}
	
	/**
	 * Iterates through the dirSizer list and executes tracing for each one. 
	 */
	public void processDirSizerList() {
		for(Entry<String, String> entry: dirSizers.entrySet()) {
			String descr = entry.getKey();
			String value = entry.getValue();
			traceDirSizes(value, descr);
		}
	}
	
	/**
	 * Iterates through the portTester list and executes tracing for each one. 
	 */
	public void processPortTesterList() {
		for(Entry<String, String> entry: portTesters.entrySet()) {
			String descr = entry.getKey();
			String value = entry.getValue();
			tracePortTester(value, descr);
		}
	}	
	
	
	
	
	/**
	 * Traces the size and delta size for the passed directory.
	 * @param directory The name of the OS file system directory
	 * @param description The description of the directory
	 */
	public void traceDirSizes(String directory, String description) {
		String sizes = shell("du -b " + directory + " | awk '{print $1}'");
		long totalSize = 0;
		for(String size: sizes.split("\\n")) {
			try {
				totalSize = totalSize + Long.parseLong(size);
			} catch (Exception e) {}
		}
		String category = tracer.buildSegment(segmentPrefix, false, "Directories", description);
		tracer.recordCounterMetric(category, "Size", totalSize);
		tracer.recordCounterMetricDelta(category, "Growth", totalSize);		
	}
	
	/**
	 * Traces 1 if port can be connected to, 0 if it cannot.
	 * @param addressPort The IP address and port 
	 * @param description The description of the socket.
	 */
	public void tracePortTester(String addressPort, String description) {
		String targetModifier = null;
		String[] sp = addressPort.split("\\s+");
		Matcher matcher = ipAddressPattern.matcher(sp[0]);
		if(matcher.matches()) {
			targetModifier = "-n " + addressPort;
		} else {
			targetModifier = addressPort;
		}
		String result = shell("echo Hello | netcat -v -w 2 " + targetModifier);
		String category = tracer.buildSegment(segmentPrefix, false, "PortChecks", description);
		if(result.contains("open")) {
			tracer.recordCounterMetric(category, "Open", 1);
		} else {
			tracer.recordCounterMetric(category, "Open", 0);
		}
		// connects: (UNKNOWN) [172.18.106.46] 50000 (?) open
		// fails: (UNKNOWN) [172.18.106.46] 50001 (?) : Connection refused
	}
	
	
	/**
	 * Iterates through a list of pids and collects/traces the following aggregated stats for all Pids in the list:<ul>
	 * <li>Percentage CPU Utilization from <code>ps aux</code></li>
	 * <li>Percentage Memory Utilization from <code>ps aux</code></li>
	 * <li>Shared Memory Utilization from <code>pmap -d</code></li>
	 * <li>Private Memory Utilization from <code>pmap -d</code></li>
	 * <li>Mapped Memory Utilization from <code>pmap -d</code></li>
	 * <li>Open Files from <code>/usr/bin/lsof -p</code></li>
	 * </ul>
	 * @param pids A list of process Ids.
	 * @param description The description of the pid list.
	 */
	public void traceResourcesForPids(List<String> pids, String description) {
		float cpuUtil = 0F;
		float memUtil = 0F;
		int memSharedUtil = 0;
		int memPrivateUtil = 0;
		int memMappedUtil = 0;
		String psAux[] = null;
		int[] memStats = null;
		int located = 0;
		String lsofOut = null;
		int pidOpenFiles = 0;
		int pidLISTENFiles = 0;
		int pidCLOSED_WAITINGFiles = 0;
		int pidESTABLISHEDFiles = 0;
		int pidFIFOFiles = 0;
		for(String locatedpid: pids) {
			try {
				Integer.parseInt(locatedpid);
				// ps aux output
				psAux = shell("/bin/ps aux | grep -v grep | grep " + locatedpid + " | awk '{print $3 , $4}'").split("\\s+");
				cpuUtil = cpuUtil + Float.parseFloat(psAux[0]);
				memUtil = memUtil + Float.parseFloat(psAux[1]);
				// pmap output
				// format will be "mapped: 20848K    writeable/private: 628K    shared: 0K"			
				memStats = unsparseArray(shell("/usr/bin/pmap -d " + locatedpid + " | grep mapped | grep shared | grep writeable").split("[a-z,A-Z,//,:]"));
				memMappedUtil = memMappedUtil + memStats[0];
				memPrivateUtil = memPrivateUtil + memStats[1];
				memSharedUtil = memSharedUtil + memStats[2];
				// lsof for open file
				pidOpenFiles = pidOpenFiles + getOpenFiles(locatedpid, "");
				pidLISTENFiles = pidLISTENFiles + getOpenFiles(locatedpid, " | grep LISTEN ");
				pidCLOSED_WAITINGFiles = pidCLOSED_WAITINGFiles + getOpenFiles(locatedpid, " | grep CLOSE_WAIT ");
				pidESTABLISHEDFiles = pidESTABLISHEDFiles + getOpenFiles(locatedpid, " | grep ESTABLISHED ");
				pidFIFOFiles = pidFIFOFiles + getOpenFiles(locatedpid, " | grep FIFO ");
				located++;
			} catch (Exception e) {
				
			}
		}
		// must have located at least one pid
		if(located > 0) {
			String category = tracer.buildSegment(segmentPrefix, false, "Processes", description);
			String openFiles = tracer.buildSegment(category, false, "Open Files");
			tracer.recordCounterMetric(category, "Percent CPU Utilization", (int)cpuUtil);
			tracer.recordCounterMetric(category, "Percent Memory Utilization", (int)memUtil);		
			tracer.recordCounterMetric(category, "Shared Memory Utilization (K)", (int)memSharedUtil);
			tracer.recordCounterMetric(category, "Private Memory Utilization (K)", (int)memPrivateUtil);
			tracer.recordCounterMetric(category, "Mapped Memory Utilization (K)", (int)memMappedUtil);
			tracer.recordCounterMetric(openFiles, "Total", pidOpenFiles);
			tracer.recordCounterMetric(openFiles, "Listen", pidLISTENFiles);
			tracer.recordCounterMetric(openFiles, "ClosedWaiting", pidCLOSED_WAITINGFiles);
			tracer.recordCounterMetric(openFiles, "Established", pidESTABLISHEDFiles);
			tracer.recordCounterMetric(openFiles, "FIFO", pidFIFOFiles);
			tracer.recordCounterMetric(category, "Process Count", located);
		}

	}
	
	protected int getOpenFiles(String locatedpid, String qualifier) {
		try {
			String lsofOut = shell("/usr/bin/lsof -p " + locatedpid + qualifier + " | wc -l ").replace("\n", "");
			return Integer.parseInt(lsofOut);
		} catch (Exception e) {
			if(logErrors) {
				log.error("Failed to getOpenFiles with qualifier [" + qualifier + "]", e);				
			}
			return 0;
		}
	}
	
	public static void log(Object obj) {
		System.out.println(obj);
	}
	
	/**
	 * Takes a sparsely populated String array and returns a dense array of integers representing any numbers found in the string array.
	 * @param arr A String array
	 * @return An array of ints.
	 */
	protected int[] unsparseArray(String[] arr) {
		ArrayList<Integer> npids = new ArrayList<Integer>();
		for(String s: arr) {
			try {
				npids.add(Integer.parseInt(s.trim()));
			} catch (Exception e) {}
		}
		int[] pids = new int[npids.size()];
		for(int i = 0; i < npids.size(); i++) {
			pids[i] = npids.get(i);
		}
		return pids;
	}
	
	/**
	 * Issues a ps command to get a listing of the matching pids.
	 * @param pidLocator A grep String to filter against a <code>ps -ef</code> command.
	 * @return A list of matching pids
	 */
	public List<String> getPidsFromPidLocator(Entry<String, String> pidLocator) {
		List<String> pids = new ArrayList<String>();
		String psListing = shell("/bin/ps -ef | grep -v grep | grep " + pidLocator.getValue() + " | awk '{print $2}'");		
		for(String pid: psListing.split("\\n")) {
			try {
				pids.add("" + Integer.parseInt(pid.trim()));
			} catch (Exception e) {}
		}
		return pids;
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
	 * Parses the whole output of <code>sar -P ALL 1</code>
	 * Extracts one line for each host CPU plus <b>all</b> and traces: <ul>
	 * <li>system
	 * <li>nice
	 * <li>iowait
	 * <li>user
	 * <li>idle</ul> 
	 * First line is the summarized CPU stats, with each subsequent line representing one individual CPU.
	 * @param output The output of the OS command.
	 */
    protected void processCPUStats(String output)
    {
        if(output == null)
            return;
        String lines[] = output.split("\n");
        String arr$[] = lines;
        int len$ = arr$.length;
        for(int i$ = 0; i$ < len$; i$++)
        {
            String s = arr$[i$];
            if(s.contains("Average") && !s.contains("CPU"))
            {
                String tmp = (new StringBuilder()).append(s.replaceAll("\\s+", "~")).append("\n").toString();
                processCPULine(tmp);
            }
        }
    }

	
	/**
	 * Parse helper for CPU stats.
	 * @param s A line of text from <code>sar -P ALL 1</code>
	 */
    protected void processCPULine(String s)
    {
        String cols[] = s.split("~");
        
        String segment = tracer.buildSegment(segmentPrefix, "CPU Summary", cols[1]);
        float _user = 0.0F;
        float _nice = 0.0F;
        float _system = 0.0F;
        float _iowait = 0.0F;
        float _idle = 0.0F;
        float _utilized = 0.0F;
        try
        {
            _user = Float.parseFloat(cols[2]);
            tracer.recordCounterMetric(segment, "User", (int)_user);
            _nice = Float.parseFloat(cols[3]);
            tracer.recordCounterMetric(segment, "Nice", (int)_nice);
            _system = Float.parseFloat(cols[4]);
            tracer.recordCounterMetric(segment, "System", (int)_system);
            _iowait = Float.parseFloat(cols[5]);
            tracer.recordCounterMetric(segment, "IOWait", (int)_iowait);
            _idle = Float.parseFloat(cols[6]);
            tracer.recordCounterMetric(segment, "Idle", (int)_idle);
            _utilized = 100F - _idle;
            tracer.recordCounterMetric(segment, "Utilization", (int)_utilized);
            if(cols[1].equalsIgnoreCase("all"))
            {
                user = _user;
                nice = _nice;
                system = _system;
                iowait = _iowait;
                idle = _idle;
                utilized = _utilized;
            }
        }
        catch(Exception e)
        {
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
							tracer.buildSegment(segmentPrefix, "CPU Summary"),							 
							"CPU Queue Size", 
							(int)queueSize);
					processListSize = Float.parseFloat(cols[2]);
					tracer.recordCounterMetric(
							tracer.buildSegment(segmentPrefix, "CPU Summary"), 
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
					netInterface = tracer.buildSegment(segmentPrefix, "Network Devices", cols[1]); 
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
					netInterface = tracer.buildSegment(segmentPrefix, "Network Devices", cols[1]);
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
					netInterface = tracer.buildSegment(segmentPrefix, "Network Devices");
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
					
					fileSystem = tracer.buildSegment(segmentPrefix, "File System", cols[0]);
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
	 * Traces the total number of open files on the system.
	 * @param output
	 */
	protected void processOpenSystemFiles(String output) {
		if(output==null) return;
		
		try {
			String fileSystem = tracer.buildSegment(segmentPrefix, "File System");
			tracer.recordCounterMetric(fileSystem, "Total Open Files", Integer.parseInt(output.replace("\n", "")));
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
					netInterface = tracer.buildSegment(segmentPrefix, "Memory");
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
					tracer.recordCounterMetric(tracer.buildSegment(segmentPrefix,"CPU Summary"), "Context Switches", (int)contextSwitches);
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
		String jvmPrefix = tracer.buildSegment(segmentPrefix, false, "JVM");
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
		if(pid==-1) return;
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
		LinuxSARCollector collector = new LinuxSARCollector();
		collector.setDirSizer(args[0]);
		collector.setProcessLocator(args[1]);
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
	 * Advanced shell commands
	 * @return A shell command output
	 */
	@JMXOperation(description="Executes a shell command", expose=true, name="shell")	
	public String shell(String command) {
		InputStream reader = null;
		StringBuilder buff = new StringBuilder();
		try {		
			ProcessBuilder pb = new ProcessBuilder(new String[] {"/bin/bash", "-c", "\"\"" + command + "\"\""});			
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
			buffer.append("LinuxCPUCollector[\n");
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
	
	/**
	 * Gets the map of processLocators.
	 * @return the processLocators
	 */
	@JMXAttribute(description="Gets the map of pidLocators.", name="ProcessLocators")
	public Map<String, String> getPidLocators() {
		return processLocators;
	}
	
	

	/**
	 * Adds a processLocator to the processLocators map.
	 * String should be <code>Description</code>~<code>Command</code>.
	 * @param processLocator
	 */
	public void setProcessLocator(String processLocator) {
		String[] pls = processLocator.split("~");
		processLocators.put(pls[0], pls[1]);
	}
	
	/**
	 * No Op
	 * @return A VOID
	 */
	@JMXAttribute(description="Sets a Process Locator (Description~Filter)", name="ProcessLocator")
	public String getProcessLocator() {
		return "VOID";
	}
	
	//==============================================================================================
	
	/**
	 * Gets the map of directory sizers.
	 * @return the directory sizers
	 */
	@JMXAttribute(description="Gets the map of directory sizers.", name="DirectorySizers")
	public Map<String, String> getDirSizers() {
		return dirSizers;
	}
	
	/*
	 * <attribute name="PortTester">TeleNet MQSeries 1~172.18.106.46 50000</attribute>			
	 */ 
	
	/**
	 * Gets the map of port testers.
	 * @return the port testers
	 */
	@JMXAttribute(description="Gets the map of port testers.", name="PortTesters")
	public Map<String, String> getPortTesters() {
		return portTesters;
	}
	
	/**
	 * No Op
	 * @return A VOID
	 */
	@JMXAttribute(description="Sets a Port Tester(Description~Address Port)", name="PortTester")
	public String getPortTester() {
		return "VOID";
	}
	
	/**
	 * Adds a porttester to the portTesters map.
	 * String should be <code>Description</code>~<code>Address Port</code>.
	 * @param dirSizer
	 */
	public void setPortTester(String portTester) {
		String[] pt = portTester.split("~");
		portTesters.put(pt[0], pt[1]);
	}	
	

	/**
	 * Adds a dirSizer to the dirSizers map.
	 * String should be <code>Description</code>~<code>Directory</code>.
	 * @param dirSizer
	 */
	public void setDirSizer(String dirSizer) {
		String[] dls = dirSizer.split("~");
		dirSizers.put(dls[0], dls[1]);
	}
	
	/**
	 * No Op
	 * @return A VOID
	 */
	@JMXAttribute(description="Sets a Dir Sizer (Description~Directory)", name="DirectorySizer")
	public String getDirSizer() {
		return "VOID";
	}
	
	
	/**
	 * Generates a report of process locators.
	 * @return A string report of process locators.
	 */
	@JMXOperation(description="Generates a report of pid locators", expose=true, name="reportProcessLocators")
	public String reportPidLocators() {
		StringBuilder buff = new StringBuilder("Process Locators");
		for(Entry entry: processLocators.entrySet()) {
			buff.append("\n\t").append(entry.getKey()).append("\t-\t").append(entry.getValue());
		}
		return buff.toString();
	}
	
	/**
	 * Generates a report of directory sizers.
	 * @return A string report of dir sizers.
	 */
	@JMXOperation(description="Generates a report of directory sizers", expose=true, name="reportDirSizers")
	public String reportDirSizers() {
		StringBuilder buff = new StringBuilder("Directory Sizers");
		for(Entry entry: dirSizers.entrySet()) {
			buff.append("\n\t").append(entry.getKey()).append("\t-\t").append(entry.getValue());
		}
		return buff.toString();
	}

	/**
	 * The number of open files for the whole host.
	 * @return the openFiles
	 */
	@JMXAttribute(description="The number of open files on the host", name="OpenFiles")
	public int getOpenFiles() {
		return openFiles;
	}

	
	

}
