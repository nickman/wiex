
package com.heliosapm.wiex.tracing.tracing;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import javax.management.ObjectName;

import org.apache.log4j.BasicConfigurator;
import org.cliffc.high_scale_lib.Counter;
import org.cliffc.high_scale_lib.NonBlockingHashMap;

import com.heliosapm.wiex.jmx.JMXHelper;
import com.heliosapm.wiex.jmx.dynamic.ManagedObjectDynamicMBean;
import com.heliosapm.wiex.jmx.dynamic.annotation.JMXAttribute;
import com.heliosapm.wiex.jmx.dynamic.annotation.JMXManagedObject;
import com.heliosapm.wiex.jmx.util.MBeanServerHelper;
import com.heliosapm.wiex.tracing.helpers.ConfigurationHelper;
import com.heliosapm.wiex.tracing.tracing.thread.ThreadStats;

/**
 * <p>Title: OpenTSDBTracer</p>
 * <p>Description: Tracer for <a href="http://opentsdb.net">OpenTSDB that uses the telnet put protocol to transmit traces.</a></p>
 * <p>Configure with these system properties: <ul>
 * 	<li><b>wiex.tsdb.host</b>: The OpenTSDB host or IP address. Default is <b>localhost</b></li>
 *  <li><b>wiex.tsdb.port</b>: The OpenTSDB telnet TCP listening port. Default is <b>4242</b></li>
 * </ul></p>
 * <p>Format of submission is: <pre>
 * 	put $metric $now $value host=$HOST key1=value1 key2=value2 ..... keyN=valueN 
 * </pre></p>
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>com.heliosapm.wiex.tracing.tracing.OpenTSDBTracer</code></p>
 * TODO:
 * need to make sure host/agent etc are put in.  (Collector ?)
 * buildSegment should concat pairs into a tag, then append with a space
 */
@JMXManagedObject(annotated=true)
public class OpenTSDBTracer extends AbstractTracer implements Runnable {
	/** The socket to transmit to OpenTSDB */
	protected volatile Socket socket = null;
	/** The OpenTSDB host name or IP address */
	protected final String tsdbHost;
	/** The OpenTSDB telnet TCP listening port */
	protected final int tsdbPort;
	/** The offline metric persistence file name */
	protected String metricFileName = null;	
	/** The offline metric persistence file */
	protected File metricFile = null;	
	/** The size of the flush queue */
	protected final int queueSizeConfig;

	
	/** Indicates if we're tracing the timestamp using seconds or milliseconds */
	protected final boolean traceUsingMillis;
	
	
	/** The flush queue */
	protected final BlockingQueue<byte[]> flushQueue;
	/** The flush thread */
	protected final Thread flushThread;
	/** The flush thread run indicator */
	protected volatile boolean keepRunning = true;
	
	/** Socket connected indicator */
	protected final AtomicBoolean connected = new AtomicBoolean(false);
	/** The cumulative count of uneven segment build requests */
	protected final AtomicLong unevenSegmentRequestCount = new AtomicLong();
	/** The cumulative count of drop trace requests that occurs when the queue will not accept more entries */
	protected final AtomicLong metricDrops = new AtomicLong();
	/** The total number of metrics traced */
	protected final AtomicLong metricsTraced = new AtomicLong();
	/** The total number of metrics flushed */
	protected final AtomicLong metricsFlushed = new AtomicLong();

	
	
	/** Incident counter */
	protected final NonBlockingHashMap<String, Counter> incidentCounters = new NonBlockingHashMap<String, Counter>(1024); 
	/** Period counters, reset every xxx in a while */
	protected final NonBlockingHashMap<String, Counter> periodCounters = new NonBlockingHashMap<String, Counter>(1024); 
	
	/** The JVM's temp file directory */
	public static final String TMP_DIR = System.getProperty("java.io.tmpdir");
	
	/** The default opentsdb host */
	public static final String DEFAULT_TSDB_HOST = "localhost";
	/** The default opentsdb port */
	public static final int DEFAULT_TSDB_PORT = 4242;
	/** The default opentsdb flush queue size */
	public static final int DEFAULT_TSDB_FLUSHQ_SIZE = 1024;
	
	/** The opentsdb host system prop name */
	public static final String TSDB_HOST_PROP = "wiex.tsdb.host";
	/** The opentsdb port system prop name */
	public static final String TSDB_PORT_PROP = "wiex.tsdb.port";
	/** The opentsdb opentsdb flush queue size prop name */
	public static final String TSDB_FLUSHQ_SIZE_PROP = "wiex.tsdb.flushq.size";
	
	/** The default opentsdb offline metric persistence file name */
	public static final String DEFAULT_TSDB_PERSIST_FILE = TMP_DIR + (TMP_DIR.endsWith(File.pathSeparator) ? "" : File.pathSeparator) + "wiex-opentsdb-metrics.gzip";
	/** The opentsdb offline metric persistence file name property */
	public static final String TSDB_PERSIST_FILE_PROP = "wiex.tsdb.tmpfile"; 	
	/** The opentsdb trace in millis property */
	public static final String TSDB_TRACE_IN_MS_PROP = "wiex.tsdb.trace.ms";
	
	/** The OpenTSDB metric anf segment delimeter */
	public static final String DELIM = " ";
	
	/** The OpenTSDBTracer JMX ObjectName */
	public static final ObjectName OBJECT_NAME = JMXHelper.objectName("com.heliosapm.wiex.tracing:service=" + OpenTSDBTracer.class.getSimpleName());
	
	
	/** 
	 * The format of an OpenTSDB put request: <ol>
	 * <li>The metric name</li>
	 * <li>The current timestamp</li>
	 * <li>The value to trace</li>
	 * <li>The tags</li>
	 * </ol>
	 * */
	public static final String PUT_FORMAT = "put %s %s %s %s\n";
	
	/** Metric name and tags only format */
	public static final String KEY_FORMAT = "%s %s";
	
	private OpenTSDBTracer(int i) {
		tsdbHost = null;
		tsdbPort = -1;
		traceUsingMillis = false;
		queueSizeConfig = -1;		
		flushQueue = null;
		flushThread = null;
	}
	
	/**
	 * Creates a new OpenTSDBTracer
	 */
	public OpenTSDBTracer() {
		tsdbHost = ConfigurationHelper.getSystemThenEnvProperty(TSDB_HOST_PROP, DEFAULT_TSDB_HOST);
		tsdbPort = ConfigurationHelper.getIntSystemThenEnvProperty(TSDB_PORT_PROP, DEFAULT_TSDB_PORT);
		traceUsingMillis = ConfigurationHelper.getBooleanSystemThenEnvProperty(TSDB_TRACE_IN_MS_PROP, false);
		queueSizeConfig = ConfigurationHelper.getIntSystemThenEnvProperty(TSDB_FLUSHQ_SIZE_PROP, DEFAULT_TSDB_FLUSHQ_SIZE);		
		flushQueue = new ArrayBlockingQueue<byte[]>(queueSizeConfig, false);
		flushThread = new Thread(this, "OpenTSDBTracerFlushThread");
		flushThread.setDaemon(true);
		flushThread.start();
		connectSocket();
		try {
			ManagedObjectDynamicMBean mbean = new ManagedObjectDynamicMBean(this);
			MBeanServerHelper.getMBeanServer().registerMBean(mbean, OBJECT_NAME);
		} catch (Exception ex) {
			log.warn("Failed to register JMX Interface", ex);
		}
		log.info(String.format("\n\t======================================================\n\tInitialized OpenTSDB Tracer\n\tHost: %s\n\tPort: %s\n\t======================================================\n", tsdbHost, tsdbPort));
	}
	
	public void run() {
		log.info("Flush Thread Started");
		final Set<byte[]> traces = new LinkedHashSet<byte[]>(128);
		while(keepRunning) {
			if(!connected.get()) {
				try { Thread.currentThread().join(5000); } catch (Exception ex) {}
			}
			try {
				byte[] trace = flushQueue.poll(2000, TimeUnit.MILLISECONDS);
				if(trace!=null) {
					traces.add(trace);
					flushQueue.drainTo(traces, 127);
					for(byte[] b: traces) {
						socket.getOutputStream().write(b);
					}
					metricsFlushed.addAndGet(traces.size());
					traces.clear();
				}
			} catch (IOException ix) {
				forceSocketClosed(true);
			} catch (InterruptedException iex) {
				if(!keepRunning) break;
				if(Thread.interrupted()) Thread.interrupted();				
			} catch (Exception ex) {
				if(!keepRunning) break;
			}
		}
	}
	
	
	protected Counter getIncidentCounter(String segments, String metric) {
		final String key = String.format(KEY_FORMAT, metric, segments);
		return incidentCounters.putIfAbsent(key, new Counter());
	}
	
	protected Counter getPeriodCounter(String segments, String metric) {
		final String key = String.format(KEY_FORMAT, metric, segments);
		return periodCounters.putIfAbsent(key, new Counter());
	}
	
	
	/**
	 * Returns the tsdb trace timestamp
	 * @return the time in ms. or seconds
	 */
	protected final long time() {
		return traceUsingMillis ? System.currentTimeMillis() : TimeUnit.SECONDS.convert(System.currentTimeMillis(), TimeUnit.MILLISECONDS);
	}
	
	/**
	 * Indicates if the socket is currently connected
	 * @return true if the socket is currently connected, false otherwise
	 */
	@JMXAttribute(name="Conected", description="Indicates if the socket is currently connected")
	public boolean isConnected() {
		return connected.get();
	}
	
	/**
	 * Schedules a reconnect
	 */
	protected void scheduleReconnect() {
		scheduler.schedule(new Runnable() {
			@Override
			public void run() {				
				if(!connectSocket()) {
					scheduleReconnect();
				}
			}
		}, 10000, TimeUnit.MILLISECONDS);
	}
	
	/**
	 * Forcibly closes and nulls the socket
	 * @param scheduleReconnect true to schedule a reconnect, false otherwise
	 */
	protected void forceSocketClosed(boolean scheduleReconnect) {
		connected.set(false);
		try {
			socket.close();
		} catch (Exception x) {/* No Op */}
		socket = null;
		if(scheduleReconnect) {
			scheduleReconnect();
		}
	}
	
	/**
	 * Connects the socket if not connected
	 * @return true if the socket is now connected, false otherwise
	 */
	protected boolean connectSocket() {
		if(socket!=null) {
			if(socket.isConnected()) {
				connected.set(true);
				return true;
			}
			connected.set(false);
			try {
				socket.close();
				socket = null;
				return connectSocket();
			} catch (Exception x) {
				connected.set(false);
				return false;
			}
		}
		socket = new Socket();
		try {
			socket.setKeepAlive(true);
			socket.setReuseAddress(true);
			socket.setSendBufferSize(8192 * 10);
			socket.setSoLinger(false, 1);
			socket.setSoTimeout(1000);
			socket.setTcpNoDelay(false);
			socket.connect(new InetSocketAddress(tsdbHost, tsdbPort));
			connected.set(true);
			return true;
		} catch (Exception ex) {
			log.error("Failed to connect OpenTSDBTracer to [" + tsdbHost + ":" + tsdbPort + "]", ex);
			socket = null;
			connected.set(false);
			return false;
		}
	}

	/**
	 * Creates a concatenated segment string.
	 * Interleaves delimeters between each provided 
	 * segment and returns the completed string.
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
	 * @param leaveTrailingDelim If true, the trailing delimeter will be left in. Otherwise, it will be removed.
	 * @param segments An array of segments to interleave.
	 * @return The full segment.
	 */
	public String buildSegment(boolean leaveTrailingDelim, String...segments) {
		return buildSegment(null, leaveTrailingDelim, segments);		
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
		final int len;
		if(segments==null || (len = segments.length) ==0) return leaveTrailingDelim ? DELIM : "";
		if(len %2 != 0) {
			log.warn("Uneven: " + Arrays.toString(segments));
			unevenSegmentRequestCount.incrementAndGet();
			return DELIM;
		}
		StringBuilder b = getStringBuilder(base==null ? "" : base).append(DELIM);
		for(int i = 0; i < len; i++) {
			b.append(segments[i].trim()).append("=").append(segments[i += 1].trim());
		}
		return b.append(DELIM).toString(); 				
	}
	
	public static void main(String[] args) {
		BasicConfigurator.configure();
		OpenTSDBTracer t = new OpenTSDBTracer(1);
		System.out.println(t.buildSegment("Foo", false, "A", "B"));
	}
	
	
	/**
	 * {@inheritDoc}
	 * @see com.heliosapm.wiex.tracing.tracing.ITracer#getSegmentDelimeter()
	 */
	@Override
	public String getSegmentDelimeter() {
		return DELIM;
	}

	/**
	 * {@inheritDoc}
	 * @see com.heliosapm.wiex.tracing.tracing.ITracer#getMetricDelimeter()
	 */
	@Override
	public String getMetricDelimeter() {
		return DELIM;
	}
	
	/**
	 * Offers a full put request to the flush queue 
	 * @param putExpr an OpenTSDB put request
	 */
	public void record(String putExpr) {
		log.info(putExpr);
		metricsTraced.incrementAndGet();
		if(flushQueue.offer(putExpr.trim().getBytes())) {
			metricDrops.incrementAndGet();
		}
	}

	/**
	 * {@inheritDoc}
	 * @see com.heliosapm.wiex.tracing.tracing.ITracer#recordMetric(java.lang.String, java.lang.String, long)
	 */
	@Override
	public void recordMetric(String segment, String metric, long value) {
		record(String.format(PUT_FORMAT, metric, time(), value, segment));
	}
	

	/**
	 * {@inheritDoc}
	 * @see com.heliosapm.wiex.tracing.tracing.ITracer#recordMetric(java.lang.String, java.lang.String, int)
	 */
	@Override
	public void recordMetric(String segment, String metric, int value) {
		record(String.format(PUT_FORMAT, metric, time(), value, segment));
	}

	/**
	 * {@inheritDoc}
	 * @see com.heliosapm.wiex.tracing.tracing.ITracer#recordMetric(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void recordMetric(String segment, String metric, String value) {
		/* No Op */   /*  Annotation ? */
	}

	/**
	 * {@inheritDoc}
	 * @see com.heliosapm.wiex.tracing.tracing.ITracer#recordMetric(java.lang.String, java.lang.String)
	 */
	@Override
	public void recordMetric(String segment, String metric) {
		Counter ctr = getPeriodCounter(segment, metric);
		ctr.increment();
		final long value = ctr.get(); 
		record(String.format(PUT_FORMAT, metric, time(), value, segment));

	}

	/**
	 * {@inheritDoc}
	 * @see com.heliosapm.wiex.tracing.tracing.ITracer#recordMetricIncidents(java.lang.String, java.lang.String, int)
	 */
	@Override
	public void recordMetricIncidents(String segment, String metric, int incidents) {
		Counter ctr = getPeriodCounter(segment, metric);
		ctr.add(incidents);
		final long value = ctr.get(); 
		record(String.format(PUT_FORMAT, metric, time(), value, segment));
	}

	/**
	 * {@inheritDoc}
	 * @see com.heliosapm.wiex.tracing.tracing.ITracer#recordTimeStamp(java.lang.String, java.lang.String, long)
	 */
	@Override
	public void recordTimeStamp(String segment, String metric, long timestamp) {
		record(String.format(PUT_FORMAT, metric, time(), timestamp, segment));

	}

	/**
	 * {@inheritDoc}
	 * @see com.heliosapm.wiex.tracing.tracing.ITracer#recordCounterMetric(java.lang.String, java.lang.String, long)
	 */
	@Override
	public void recordCounterMetric(String segment, String metric, long value) {
		record(String.format(PUT_FORMAT, metric, time(), value, segment));
	}

	/**
	 * {@inheritDoc}
	 * @see com.heliosapm.wiex.tracing.tracing.ITracer#recordCounterMetric(java.lang.String, java.lang.String, int)
	 */
	@Override
	public void recordCounterMetric(String segment, String metric, int value) {
		record(String.format(PUT_FORMAT, metric, time(), value, segment));

	}

	/**
	 * {@inheritDoc}
	 * @see com.heliosapm.wiex.tracing.tracing.ITracer#recordCounterMetricAdd(java.lang.String, java.lang.String, long)
	 */
	@Override
	public void recordCounterMetricAdd(String segment, String metric, long value) {
		record(String.format(PUT_FORMAT, metric, time(), value, segment));
	}

	/**
	 * {@inheritDoc}
	 * @see com.heliosapm.wiex.tracing.tracing.ITracer#recordCounterMetricAdd(java.lang.String, java.lang.String, int)
	 */
	@Override
	public void recordCounterMetricAdd(String segment, String metric, int value) {
		record(String.format(PUT_FORMAT, metric, time(), value, segment));

	}

	/**
	 * {@inheritDoc}
	 * @see com.heliosapm.wiex.tracing.tracing.ITracer#recordMetric(java.lang.String, com.heliosapm.wiex.tracing.tracing.thread.ThreadStats)
	 */
	@Override
	public void recordMetric(String segment, ThreadStats ts) {
		try {
			recordMetric(segment, ThreadStats.ELAPSED, ts.getElapsedTime());
			recordMetric(segment, ThreadStats.BLOCK_COUNT, ts.getBlockCount());
			recordMetric(segment, ThreadStats.WAIT_COUNT, ts.getWaitCount());
			recordMetric(segment, ThreadStats.BLOCK_TIME, ts.getBlockTime());
			recordMetric(segment, ThreadStats.WAIT_TIME, ts.getWaitTime());
			recordMetric(segment, ThreadStats.CPU, ts.getCpuTime());
			recordMetric(segment, ThreadStats.USER_CPU, ts.getUserCpuTime());			
		} catch (Throwable e) {/* No Op */}


	}

	/**
	 * Returns the metric file name where metrics are persisted if OpenTSDB goes off line
	 * @return the metric file name
	 */
	@JMXAttribute(name="MetricFileName", description="The metric file name where metrics are persisted if OpenTSDB goes off line")
	public String getMetricFileName() {
		return metricFileName;
	}

	/**
	 * Indicates if millisecond timestamps are enabled. If false, seconds are used.
	 * @return true for milliseconds, false for seconds
	 */
	@JMXAttribute(name="TraceUsingMillis", description="Indicates if millisecond timestamps are enabled. If false, seconds are used.")
	public boolean isTraceUsingMillis() {
		return traceUsingMillis;
	}

	/**
	 * Returns the total number of metric drops
	 * @return the total number of metric drops
	 */
	@JMXAttribute(name="MetricDrops", description="The total number of metric drops")
	public long getMetricDrops() {
		return metricDrops.get();
	}

	/**
	 * Returns the configured flush queue size
	 * @return the configured flush queue size
	 */
	@JMXAttribute(name="QueueSizeConfig", description="The configured flush queue size")
	public final int getQueueSizeConfig() {
		return queueSizeConfig;
	}
	
	/**
	 * Returns the number of pending items in the flush queue
	 * @return the number of pending items in the flush queue
	 */
	@JMXAttribute(name="QueueDepth", description="The number of pending items in the flush queue")
	public final int getQueueDepth() {
		return flushQueue.size();
	}

	/**
	 * Returns the total number of metrics traced
	 * @return the total number of metrics traced
	 */
	@JMXAttribute(name="MetricsTraced", description="The total number of metrics traced")
	public final long getMetricsTraced() {
		return metricsTraced.get();
	}

	/**
	 * Returns the total number of metrics flushed
	 * @return the total number of metrics flushed
	 */
	@JMXAttribute(name="MetricsFlushed", description="The total number of metrics flushed")
	public final long getMetricsFlushed() {
		return metricsFlushed.get();
	}
	

}
