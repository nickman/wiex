/**
 * 
 */
package com.heliosapm.wiex.server.tracing.collectors.network;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import com.heliosapm.wiex.jmx.dynamic.annotation.JMXAttribute;
import com.heliosapm.wiex.jmx.dynamic.annotation.JMXManagedObject;
import com.heliosapm.wiex.jmx.dynamic.annotation.JMXOperation;
import com.heliosapm.wiex.server.tracing.collectors.BaseCollector;

/**
 * <p>Title: PingCollector</p>
 * <p>Description: Ping Time Collector.</p>
 * <p>Configuration is IP Address|HostName 
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Company: Helios Development Group</p>
 * @author Whitehead
 * @version $Revision: 1.3 $
 */
@JMXManagedObject(annotated=true, declared=false)
public class PingCollector extends BaseCollector {
	
	/**	true if platform is windows */
	protected boolean isWindows = true;
	/** A list of servers to ping */
	protected List<String> servers = new ArrayList<String>();
	/** The packet size of the pings. Defaults to 32 */
	protected int packetSize = 32;
	/** The number of packets to be sent. Defaults to 2 */
	protected int packetCount = 2;
	/** The timeout in seconds for each request. Defaults to 5 */
	protected int timeout = 5;
	
	
	
	
	
	/**
	 * Instantiates a new PingCollector.
	 */
	public PingCollector() {
		super();
		isWindows = System.getProperty("os.name").toUpperCase().contains("WINDOWS");
		frequency = 60000;					
	}
	
	/**
	 * Initializes the VERSION and MODULE.
	 * @see com.heliosapm.wiex.server.collectors.BaseCollector#init()
	 */
	public void init() {
		VERSION = "$Revision: 1.3 $";
		MODULE = "PingCollector";
	}		
	
	
	/**
     * Issues an OS Ping to the specified address.
	 * @param ipAddress The IP Address to ping.
	 * @return A string representing the native ping request output.
	 * @throws Exception
	 */
	public String ping(String ipAddress) throws Exception {
		StringBuilder buff = new StringBuilder();
		InputStream reader = null;
		try {
			ProcessBuilder pb = null;
			if(isWindows) {
				pb = new ProcessBuilder("ping.exe", "-n", "" + packetCount, "-l", "" + packetSize, "-w", "" + (timeout*1000), ipAddress);
			} else {
				pb = new ProcessBuilder("/bin/ping", "-c", "" + packetCount, "-s", "" + packetSize, "-W", "" + timeout, ipAddress);
			}
			Process p = pb.start();
			reader = new BufferedInputStream(p.getInputStream());			
			for (;;) {
				int c = reader.read();
				if (c == -1)
					break;
				buff.append((char) c);
			}
			return buff.toString();
		} finally {
			try { reader.close(); } catch (Exception e) {}
		}
	}
	
	/**
     * Processes the output of a Windows Ping
	 * @param output The output of the ping command
     * @param hostName The hostName that was pinged.
	 */
	public void processWindowsPing(String output, String hostName) {
		StringReader reader = new StringReader(output);
		BufferedReader br = new BufferedReader(reader);
		String line = null;
		try {
			while((line=br.readLine().trim())!=null) {
				if(line.startsWith("Minimum")) {
					String readings[] = line.split(",");
					for(String reading: readings) {
						try {
							String readingFragments[] = reading.replaceAll("\\s+", "").split("=");
							String timeType = readingFragments[0];
							long value = Long.parseLong(readingFragments[1].substring(0, readingFragments[1].indexOf("ms")));						 
							tracer.recordCounterMetric(tracer.buildSegment(segmentPrefix, false, hostName), timeType, value);
							//log(tracer.buildSegment(segmentPrefix, false, hostName) + timeType + value); 
						} catch (Exception e) {}						
					}
				} else if(line.startsWith("Packets:")) {
					try {
						int value = Integer.parseInt(line.split("\\(")[1].split("\\)")[0].split("%")[0]);
						tracer.recordCounterMetric(tracer.buildSegment(segmentPrefix, false, hostName), "Packet Loss", value);
						if(value==100) {
							tracer.recordCounterMetric(tracer.buildSegment(segmentPrefix, false, hostName), "Minimum", -1);
							tracer.recordCounterMetric(tracer.buildSegment(segmentPrefix, false, hostName), "Maximum", -1);
							tracer.recordCounterMetric(tracer.buildSegment(segmentPrefix, false, hostName), "Average", -1);
						}
						//log(tracer.buildSegment(segmentPrefix, false, hostName) + "Packet Loss" + value);
					} catch (Exception e) {}
				}			
			}
		} catch (Exception e) {
			// Noop
		}		
	}
	
	/**
     * Processes the output of a Linux Ping
	 * @param output The output of the ping command
     * @param hostName The hostName that was pinged.
	 */
	public void processLinuxPing(String output, String hostName) {
		StringReader reader = new StringReader(output);
		BufferedReader br = new BufferedReader(reader);
		String line = null;
		try {
			while((line=br.readLine())!=null) {
				line = line.trim();
				if(line.startsWith("rtt")) {
					String readings[] = line.split(" = ");
					String[] values = readings[1].split("/");
					long value = 0L; 
					try {
						value = (long)Float.parseFloat(values[0]);
						tracer.recordCounterMetric(tracer.buildSegment(segmentPrefix, false, hostName), "Minimum", value);
						//log(tracer.buildSegment(segmentPrefix, false, hostName) + "Min:" + value); 											
					} catch (Exception e)  {}
					try {
						value = (long)Float.parseFloat(values[1]);
						tracer.recordCounterMetric(tracer.buildSegment(segmentPrefix, false, hostName), "Average", value);
						//log(tracer.buildSegment(segmentPrefix, false, hostName) + "Avg:" + value);											
					} catch (Exception e)  {}		
					try {
						value = (long)Float.parseFloat(values[2]);
						tracer.recordCounterMetric(tracer.buildSegment(segmentPrefix, false, hostName), "Maximum", value);
						//log(tracer.buildSegment(segmentPrefix, false, hostName) + "Max:" + value);											
					} catch (Exception e)  {}		
				} else if(line.contains("packets transmitted")) {
					try {
						int value = Integer.parseInt(line.split(",")[2].trim().split("\\s+")[0].replaceAll("%",""));						
						tracer.recordCounterMetric(tracer.buildSegment(segmentPrefix, false, hostName), "Packet Loss", value);
						if(value==100) {
							tracer.recordCounterMetric(tracer.buildSegment(segmentPrefix, false, hostName), "Minimum", -1);
							tracer.recordCounterMetric(tracer.buildSegment(segmentPrefix, false, hostName), "Maximum", -1);
							tracer.recordCounterMetric(tracer.buildSegment(segmentPrefix, false, hostName), "Average", -1);
						}						
						//log(tracer.buildSegment(segmentPrefix, false, hostName) + "Loss:" + value);
					} catch (Exception e) {}
				}			
			}
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}	

	/**
	 * 
	 * @see com.heliosapm.wiex.server.collectors.BaseCollector#collect()
	 */
	@Override
	@JMXOperation(description="Collects and traces Ping Data", expose=true, name="collect")
	public void collect() {
		long start = System.currentTimeMillis();
		for(String s: servers) {
			String[] fragment = s.split("\\|");
			String ip = fragment[0];
			String name = fragment[1];
			try {
				String output = ping(ip);
				if(isWindows) {
					processWindowsPing(output, name);
				} else {
					processLinuxPing(output, name);
				}
			} catch (Exception e) {
				if(logErrors) {
					log.error("Ping Processing Exception", e);
				}
			}
			
		}
		collectTime = System.currentTimeMillis()-start;
		if(traceCollectionTime) {
			tracer.recordMetric(tracer.buildSegment(segmentPrefix, false, MODULE), "Collect Time", collectTime);
		}
	}
	
	
	public static void log(Object message) {
		System.out.println(message);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		PingCollector pc = new PingCollector();
		String s = pc.ping("10.54.6.48");
		log(s);
		log("===================");
		if(pc.isWindows) {
			pc.processWindowsPing(s, "AS10");
		} else {
			pc.processLinuxPing(s, "AS10");
		}

	}
	
	/**
     * No Op.
	 * @return void.
	 */
	@JMXAttribute(description="A Server to Ping", name="Server")
	public String getServer() {
		return "VOID";
	}

	/**
     * Adds a new server to be pinged.
     * Pattern is IP Address | Host Name
	 * @param s
	 */
	public void setServer(String s) {
		servers.add(s);
	}
	
	/**
     * Generates a pinged server report.
	 * @return A string report.
	 */
	@JMXOperation(description="Generates a pinged server report.", expose=true, name="reportServers")
	public String reportServers() {
		StringBuilder buff = new StringBuilder();
		for(String s: servers) {
			String[] fragment = s.split("\\|");
			buff.append("IP Address:").append(fragment[0]).append("\n");
			buff.append("Host Name:").append(fragment[1]).append("\n");
			buff.append("===============================================\n");
		}
		return buff.toString();
	}


	/**
     * The number of packets to send.
	 * @return the packetCount
	 */
	@JMXAttribute(description="The number of packets to send.", name="PacketCount")
	public int getPacketCount() {
		return packetCount;
	}


	/**
     * Sets the number of packets to send.
	 * @param packetCount the packetCount to set
	 */
	public void setPacketCount(int packetCount) {
		this.packetCount = packetCount;
	}


	/**
     * The number of packets to be sent.
	 * @return the packetSize
	 */
	@JMXAttribute(description="The size of packets to send.", name="PacketSize")
	public int getPacketSize() {
		return packetSize;
	}


	/**
     * Sets the number of packets to be sent.
	 * @param packetSize the packetSize to set
	 */
	public void setPacketSize(int packetSize) {
		this.packetSize = packetSize;
	}


	/**
     * The timeout in seconds for each packet request.
	 * @return the timeout
	 */
	@JMXAttribute(description="The timeout in seconds for each packet request.", name="Timeout")
	public int getTimeout() {
		return timeout;
	}


	/**
     * Sets the timeout in seconds for each packet request.
	 * @param timeout the timeout to set
	 */
	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}
	
	

}
