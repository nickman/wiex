/**
 * 
 */
package com.heliosapm.wiex.tracing.tracing.jmx.metrics;

import java.util.concurrent.atomic.AtomicLong;

import com.heliosapm.wiex.jmx.dynamic.annotation.JMXAttribute;



/**
 * <p>Title: AbstractMetric</p>
 * <p>Description: Base class for all JMX Managed Object Metrics</p> 
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Organization: Helios Development Group</p>
 * @author Whitehead
 * @version $Revision: 1.1 $
 */

public abstract class AbstractMetric {

	public static final long INTERVAL = 15000L;
	
	protected AtomicLong readings = new AtomicLong(0L);
	
	/**The name of the metric */
	protected String metricName = null;
	
	
	/**
	 * Instantiates a new Metric with the passed name.
	 * @param name The name of the metric.
	 */
	public AbstractMetric(String name) {
		metricName = name;
	}
	
	/**
	 * The description of the metric.
	 * @return The metric description.
	 */	
	public abstract String getDescription();

	/**
	 * @return the metricName
	 */
	@JMXAttribute(expose=true, name="getMetricName", description="getDescription", introspect=true)	
	public String getMetricName() {
		return metricName;
	}

	/**
	 * @param metricName the metricName to set
	 */
	public void setMetricName(String metricName) {
		this.metricName = metricName;
	}
	
	
	public String getOperName() {
		return "Update " + metricName;
	}
	
	public String getOperDesc() {
		return "Updates the value for " + metricName;
	}
	
	public String getLowMetricName() {
		return "Low " + metricName;
	}
	
	public String getHighMetricName() {
		return "High " + metricName;
	}
	
	public String getLowDescription() {
		return "The Low Value In The Interval for " + metricName;
	}
	
	public String getHighDescription() {
		return "The High Value In The Interval for " + metricName;
	}
	

	// @JMXAttribute(expose=true, name="getLowMetricName", description="getLowDescription", introspect=true )
}
