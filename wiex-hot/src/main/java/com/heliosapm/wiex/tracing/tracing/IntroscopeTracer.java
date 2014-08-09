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

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import com.heliosapm.wiex.tracing.aop.DynaClassFactory;
import com.heliosapm.wiex.tracing.tracing.thread.ThreadStats;

/**
 * <p>Title: IntroscopeTracer</p>
 * <p>Description: </p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>com.heliosapm.wiex.tracing.tracing.IntroscopeTracer</code></p>
 */

public class IntroscopeTracer extends AbstractTracer {
	/** The introscope adapter */
	IntroscopeAdapter adapter = loadAgent();
	/** The IntroscopeAgent classloader */
	protected static ClassLoader agentLoader;	
	/** The introscope agent's jar location */
	protected static String agentJarLocation = null;
	
	/** The manifest entry key that identifies a jar as the introscope agent */
	protected static final String WILY_MANIFEST_KEY = "com-wily-Name";
	/** The manifest entry value that identifies a jar as the introscope agent */
	protected static final String WILY_MANIFEST_VALUE = "Introscope Agent";


	
	/** The wily tracer segment delimiter */
	public static final String SEGMENT_DELIM = "|";
	/** The wily tracer metric delimiter */
	public static final String METRIC_DELIM = ":";
	/**
	 * Creates a new IntroscopeTracer
	 */
	public IntroscopeTracer() {
		
	}

	/**
	 * {@inheritDoc}
	 * @see com.heliosapm.wiex.tracing.tracing.ITracer#getSegmentDelimeter()
	 */
	@Override
	public String getSegmentDelimeter() {
		return SEGMENT_DELIM; 
	}

	/**
	 * {@inheritDoc}
	 * @see com.heliosapm.wiex.tracing.tracing.ITracer#getMetricDelimeter()
	 */
	@Override
	public String getMetricDelimeter() {
		return METRIC_DELIM;
	}

	/**
	 * {@inheritDoc}
	 * @see com.heliosapm.wiex.tracing.tracing.ITracer#recordMetric(java.lang.String, java.lang.String, long)
	 */
	@Override
	public void recordMetric(String segment, String metric, long value) {
		adapter.recordDataPoint(String.format("%s%s%s", segment, METRIC_DELIM, metric), value);

	}

	/**
	 * {@inheritDoc}
	 * @see com.heliosapm.wiex.tracing.tracing.ITracer#recordMetric(java.lang.String, java.lang.String, int)
	 */
	@Override
	public void recordMetric(String segment, String metric, int value) {
		adapter.recordDataPoint(String.format("%s%s%s", segment, METRIC_DELIM, metric), value);

	}

	/**
	 * {@inheritDoc}
	 * @see com.heliosapm.wiex.tracing.tracing.ITracer#recordMetric(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void recordMetric(String segment, String metric, String value) {
		adapter.recordDataPoint(String.format("%s%s%s", segment, METRIC_DELIM, metric), value);
	}

	/**
	 * {@inheritDoc}
	 * @see com.heliosapm.wiex.tracing.tracing.ITracer#recordMetric(java.lang.String, java.lang.String)
	 */
	@Override
	public void recordMetric(String segment, String metric) {
		adapter.recordIncident(String.format("%s%s%s", segment, METRIC_DELIM, metric));
	}

	/**
	 * {@inheritDoc}
	 * @see com.heliosapm.wiex.tracing.tracing.ITracer#recordMetricIncidents(java.lang.String, java.lang.String, int)
	 */
	@Override
	public void recordMetricIncidents(String segment, String metric, int incidents) {
		adapter.recordIncident(String.format("%s%s%s", segment, METRIC_DELIM, metric), incidents);

	}

	/**
	 * {@inheritDoc}
	 * @see com.heliosapm.wiex.tracing.tracing.ITracer#recordTimeStamp(java.lang.String, java.lang.String, long)
	 */
	@Override
	public void recordTimeStamp(String segment, String metric, long timestamp) {
		adapter.recordTimeStamp(String.format("%s%s%s", segment, METRIC_DELIM, metric), timestamp);

	}

	/**
	 * {@inheritDoc}
	 * @see com.heliosapm.wiex.tracing.tracing.ITracer#recordCounterMetric(java.lang.String, java.lang.String, long)
	 */
	@Override
	public void recordCounterMetric(String segment, String metric, long value) {
		adapter.recordCurrentValue(String.format("%s%s%s", segment, METRIC_DELIM, metric), value);
	}

	/**
	 * {@inheritDoc}
	 * @see com.heliosapm.wiex.tracing.tracing.ITracer#recordCounterMetric(java.lang.String, java.lang.String, int)
	 */
	@Override
	public void recordCounterMetric(String segment, String metric, int value) {
		adapter.recordCurrentValue(String.format("%s%s%s", segment, METRIC_DELIM, metric), value);
	}

	/**
	 * {@inheritDoc}
	 * @see com.heliosapm.wiex.tracing.tracing.ITracer#recordCounterMetricAdd(java.lang.String, java.lang.String, long)
	 */
	@Override
	public void recordCounterMetricAdd(String segment, String metric, long value) {		
		adapter.addDataPoint(String.format("%s%s%s", segment, METRIC_DELIM, metric), value);
	}

	/**
	 * {@inheritDoc}
	 * @see com.heliosapm.wiex.tracing.tracing.ITracer#recordCounterMetricAdd(java.lang.String, java.lang.String, int)
	 */
	@Override
	public void recordCounterMetricAdd(String segment, String metric, int value) {
		adapter.addDataPoint(String.format("%s%s%s", segment, METRIC_DELIM, metric), value);
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
			recordMetric(segment, "Invocations per ms.");
		} catch (Throwable e) {}
	}

	/**
	 * It loads agent's classes in the new classloader and dynamically creates an instance 
	 * of IntroscopeAdapter class. 
	 */
	private static IntroscopeAdapter loadAgent() {
		ClassLoader[] cls = null;		
		if(isAgentAccessible()) {
			cls = new ClassLoader[]{};
			agentLoader = Thread.currentThread().getContextClassLoader();
		} else {
			agentLoader = getAgentClassLoader(agentJarLocation);
			if(agentLoader==null) {
				log.warn("The Introscope Agent is not in the class path and could not be located from the config [" + agentJarLocation + "]. The IntroscopeTracingBridge is inactive");
				return null;
			}
			cls = new ClassLoader[]{agentLoader};
		}
		ClassLoader currentCl = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader(agentLoader);
		try {
			return (IntroscopeAdapter)DynaClassFactory.generateClassInstance(
					IntroscopeTracerAdapter.class.getPackage().getName() + ".TracerInstance", 
					IntroscopeTracerAdapter.class, cls);
		} finally {
			Thread.currentThread().setContextClassLoader(currentCl);
		}		
	}

	/**
	 * Determines if the Introscope agent is accessible in the current classpath
	 * @return true if it is, false otherwise
	 */
	public static boolean isAgentAccessible() {
		try {
			Class.forName("com.wily.introscope.agent.IAgent");
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	
	/**
	 * Creates a classloader for the agent at the passed location
	 * @param agentLocation The file name of the agent
	 * @return An Introscope agent jar classloader or null if it could not be created
	 */
	protected static ClassLoader getAgentClassLoader(String agentLocation) {
		if(agentLocation==null) return null;
		URL agentUrl = null;
		File agentFile = null;
		if(isValidURL(agentLocation)) {
			try { agentFile =new File(new URL(agentLocation).getFile()); } catch (Exception e) { 
				log.warn("Unexpected error creating URL/File from [" + agentLocation + "]", e);
				return null;
			} 
		} else {
			agentFile = new File(agentLocation);
		}
		if(agentFile.exists() && isWilyJar(agentFile.getAbsolutePath())) {
			try { 
				agentUrl = agentFile.toURI().toURL();
				return new URLClassLoader(new URL[]{agentUrl}, Thread.currentThread().getContextClassLoader());
			} catch (Exception e) {
				log.warn("Unexpected error creating URL/File from [" + agentLocation + "]", e);
				return null;
			}
		} else {
			log.warn("Failed to resolve Introscope Agent JAR from [" + agentLocation + "]", new Throwable());
			return null;
		}
	}
	
	private static boolean isValidURL(String url) {
		try {
			URL u = new URL(url);
			return true;
		} catch (Exception x) {
			return false;
		}
	}
	
	/**
	 * Determines if the passed string represents the Introscope Agent Jar
	 * @param jarName The file name
	 * @return true if the name is the agent jar
	 */
	public static boolean isWilyJar(String jarName) {
		JarFile jarFile = null;
		if(jarName==null) return false;
		try {
			jarFile = new JarFile(jarName);
			Manifest manifest = jarFile.getManifest();
			if(manifest==null) return false;
			Attributes attrs = manifest.getMainAttributes();
			if(attrs==null) return false;
			String value = attrs.getValue(WILY_MANIFEST_KEY);
			if(value==null) return false;
			return (value.trim().equals(WILY_MANIFEST_VALUE));
		} catch (Exception e) {
			return false;
		} finally {
			try { jarFile.close(); } catch (Exception e) {}
		}
	}	
	
	
}
