/**
 * Helios, OpenSource Monitoring
 * Brought to you by the Helios Development Group
 *
 * Copyright 2007, Helios Development Group and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org. 
 *
 */
package com.heliosapm.wiex.tracing.tracing;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import com.heliosapm.wiex.jmx.dynamic.annotation.JMXAttribute;
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
 */

public class OpenTSDBTracer extends AbstractTracer implements Runnable {
	/** The socket to transmit to OpenTSDB */
	protected volatile Socket socket = null;
	/** The OpenTSDB host name or IP address */
	protected final String tsdbHost;
	/** The OpenTSDB telnet TCP listening port */
	protected final int tsdbPort;
	
	/** The flush queue */
	protected final BlockingQueue<byte[]> flushQueue = new ArrayBlockingQueue<byte[]>(1024, false);
	/** The flush thread */
	protected final Thread flushThread;
	/** The flush thread run indicator */
	protected volatile boolean keepRunning = true;
	
	/** Socket connected indicator */
	protected final AtomicBoolean connected = new AtomicBoolean(false);

	
	
	/** The default opentsdb host */
	public static final String DEFAULT_TSDB_HOST = "localhost";
	/** The default opentsdb port */
	public static final int DEFAULT_TSDB_PORT = 4242;
	/** The opentsdb host system prop name */
	public static final String TSDB_HOST_PROP = "wiex.tsdb.host";
	/** The opentsdb port system prop name */
	public static final String TSDB_PORT_PROP = "wiex.tsdb.port";
	
	/**
	 * Creates a new OpenTSDBTracer
	 */
	public OpenTSDBTracer() {
		tsdbHost = ConfigurationHelper.getSystemThenEnvProperty(TSDB_HOST_PROP, DEFAULT_TSDB_HOST);
		tsdbPort = ConfigurationHelper.getIntSystemThenEnvProperty(TSDB_PORT_PROP, DEFAULT_TSDB_PORT);		
		flushThread = new Thread(this, "OpenTSDBTracerFlushThread");
		flushThread.setDaemon(true);
		flushThread.start();
		log.info(String.format("\n\t======================================================\n\tInitialized OpenTSDB Tracer\n\tHost: %s\n\tPort: %s\n\t======================================================\n", tsdbHost, tsdbPort));
	}
	
	public void run() {
		log.info("Flush Thread Started");
		final Set<byte[]> traces = new LinkedHashSet<byte[]>(128);
		while(keepRunning) {
			try {
				byte[] trace = flushQueue.poll(2000, TimeUnit.MILLISECONDS);
				if(trace!=null) {
					traces.add(trace);
					flushQueue.drainTo(traces, 127);
					for(byte[] b: traces) {
						socket.getOutputStream().write(b);
					}
					traces.clear();
				}
			} catch (IOException ix) {
				
			} catch (InterruptedException iex) {
				
			} catch (Exception ex) {
				if(!keepRunning) break;
			}
		}
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
		} else {
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
	}

	/**
	 * {@inheritDoc}
	 * @see com.heliosapm.wiex.tracing.tracing.ITracer#getSegmentDelimeter()
	 */
	@Override
	public String getSegmentDelimeter() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * {@inheritDoc}
	 * @see com.heliosapm.wiex.tracing.tracing.ITracer#getMetricDelimeter()
	 */
	@Override
	public String getMetricDelimeter() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * {@inheritDoc}
	 * @see com.heliosapm.wiex.tracing.tracing.ITracer#recordMetric(java.lang.String, java.lang.String, long)
	 */
	@Override
	public void recordMetric(String segment, String metric, long value) {
		// TODO Auto-generated method stub

	}

	/**
	 * {@inheritDoc}
	 * @see com.heliosapm.wiex.tracing.tracing.ITracer#recordMetric(java.lang.String, java.lang.String, int)
	 */
	@Override
	public void recordMetric(String segment, String metric, int value) {
		// TODO Auto-generated method stub

	}

	/**
	 * {@inheritDoc}
	 * @see com.heliosapm.wiex.tracing.tracing.ITracer#recordMetric(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void recordMetric(String segment, String metric, String value) {
		// TODO Auto-generated method stub

	}

	/**
	 * {@inheritDoc}
	 * @see com.heliosapm.wiex.tracing.tracing.ITracer#recordMetric(java.lang.String, java.lang.String)
	 */
	@Override
	public void recordMetric(String segment, String metric) {
		// TODO Auto-generated method stub

	}

	/**
	 * {@inheritDoc}
	 * @see com.heliosapm.wiex.tracing.tracing.ITracer#recordMetricIncidents(java.lang.String, java.lang.String, int)
	 */
	@Override
	public void recordMetricIncidents(String segment, String metric,
			int incidents) {
		// TODO Auto-generated method stub

	}

	/**
	 * {@inheritDoc}
	 * @see com.heliosapm.wiex.tracing.tracing.ITracer#recordTimeStamp(java.lang.String, java.lang.String, long)
	 */
	@Override
	public void recordTimeStamp(String segment, String metric, long timestamp) {
		// TODO Auto-generated method stub

	}

	/**
	 * {@inheritDoc}
	 * @see com.heliosapm.wiex.tracing.tracing.ITracer#recordCounterMetric(java.lang.String, java.lang.String, long)
	 */
	@Override
	public void recordCounterMetric(String segment, String metric, long value) {
		// TODO Auto-generated method stub

	}

	/**
	 * {@inheritDoc}
	 * @see com.heliosapm.wiex.tracing.tracing.ITracer#recordCounterMetric(java.lang.String, java.lang.String, int)
	 */
	@Override
	public void recordCounterMetric(String segment, String metric, int value) {
		// TODO Auto-generated method stub

	}

	/**
	 * {@inheritDoc}
	 * @see com.heliosapm.wiex.tracing.tracing.ITracer#recordCounterMetricAdd(java.lang.String, java.lang.String, long)
	 */
	@Override
	public void recordCounterMetricAdd(String segment, String metric, long value) {
		// TODO Auto-generated method stub

	}

	/**
	 * {@inheritDoc}
	 * @see com.heliosapm.wiex.tracing.tracing.ITracer#recordCounterMetricAdd(java.lang.String, java.lang.String, int)
	 */
	@Override
	public void recordCounterMetricAdd(String segment, String metric, int value) {
		// TODO Auto-generated method stub

	}

	/**
	 * {@inheritDoc}
	 * @see com.heliosapm.wiex.tracing.tracing.ITracer#recordMetric(java.lang.String, com.heliosapm.wiex.tracing.tracing.thread.ThreadStats)
	 */
	@Override
	public void recordMetric(String segment, ThreadStats ts) {
		// TODO Auto-generated method stub

	}

}
