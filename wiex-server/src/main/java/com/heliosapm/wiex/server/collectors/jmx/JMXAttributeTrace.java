/**
 * 
 */
package com.heliosapm.wiex.server.collectors.jmx;

import java.util.ArrayList;
import java.util.List;

import javax.management.ObjectName;

/**
 * <p>Title: JMXAttributeTrace</p>
 * <p>Description: Simple POJO for containing a JMX Attribute Trace</p> 
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Company: Helios Development Group</p>
 * @author Whitehead
 * @version $Revision: 1.11 $
 */

public class JMXAttributeTrace {

	protected ObjectName objectName = null;
	protected String attributeName = null;
	protected String traceName = null;
	protected String tracerType = null;
	protected String metricName = null;
	protected SimpleObjectTracer simpleObjectTracer = null;
	protected List<ObjectTracer> objectTracers = new ArrayList<ObjectTracer>();
	protected boolean mandatory = false;
	protected String defaultValue = null;
	protected boolean groovyTracers = false;
	/**
	 * @return the attributeName
	 */
	public String getAttributeName() {
		return attributeName;
	}
	/**
	 * @param attributeName the attributeName to set
	 */
	public void setAttributeName(String attributeName) {
		this.attributeName = attributeName;
	}
	/**
	 * @return the objectName
	 */
	public ObjectName getObjectName() {
		return objectName;
	}
	/**
	 * @param objectName the objectName to set
	 */
	public void setObjectName(ObjectName objectName) {
		this.objectName = objectName;
	}
	/**
	 * @return the traceName
	 */
	public String getTraceName() {
		return traceName;
	}
	/**
	 * @param traceName the traceName to set
	 */
	public void setTraceName(String traceName) {
		this.traceName = traceName;
	}
	/**
	 * Creates a new JMXAttributeTrace with a default tracer type of "LONG" 
	 * @param objectName
	 * @param attributeName
	 * @param traceName
	 */
	public JMXAttributeTrace(ObjectName objectName, String attributeName, String traceName) {		
		this.objectName = objectName;
		this.attributeName = attributeName;
		this.traceName = traceName;
		this.tracerType = "LONG";
	}
	
	/**
	 * Creates a new JMXAttributeTrace
	 * @param objectName
	 * @param attributeName
	 * @param traceName
	 * @param tracerType
	 */
	public JMXAttributeTrace(ObjectName objectName, String attributeName, String traceName, String tracerType) {		
		this.objectName = objectName;
		this.attributeName = attributeName;
		this.traceName = traceName;
		this.tracerType = tracerType;
	}
	
	
	/**
	 * Creates a new JMXAttributeTrace
	 * @param objectName
	 * @param attributeName
	 * @param traceName
	 * @param tracerType
	 * @param metricName
	 */
	public JMXAttributeTrace(ObjectName objectName, String attributeName, String traceName, String tracerType, String metricName) {		
		this.objectName = objectName;
		this.attributeName = attributeName;
		this.traceName = traceName;
		this.tracerType = tracerType;
		this.metricName = metricName;
	}
	
	

	/**
	 * Renders a readable string of the attribute trace.
	 * @return A readable string representing the attribute trace.
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
			StringBuilder buffer = new StringBuilder();
			buffer.append("JMXAttributeTrace[");
			buffer.append("attributeName = ").append(attributeName);
			buffer.append(" objectName = ").append(objectName);
			buffer.append(" traceName = ").append(traceName);
			buffer.append(" metricName = ").append(getMetricName());
			buffer.append(" tracerType = ").append(tracerType);
			if(defaultValue!=null) buffer.append(" defaultValue = ").append(defaultValue);
			
			if(simpleObjectTracer!=null) buffer.append(" simpleTracer = ").append(simpleObjectTracer.getClass().getName());
			if(objectTracers.size() >0) {
				buffer.append(" objectTracers = ");
				for(ObjectTracer tracer: objectTracers) {
					buffer.append(tracer.getClass().getName()).append(",");
				}				
			}
			buffer.append("]");
			return buffer.toString();
		}
	/**
	 * @return the tracerType
	 */
	public String getTracerType() {
		return tracerType;
	}
	/**
	 * @param tracerType the tracerType to set
	 */
	public void setTracerType(String tracerType) {
		this.tracerType = tracerType;
	}
	/**
	 * @return the metricName
	 */
	public String getMetricName() {
		if(metricName==null) return attributeName;
		else return metricName;
	}
	/**
	 * @param metricName the metricName to set
	 */
	public void setMetricName(String metricName) {
		this.metricName = metricName;
	}
	/**
	 * @return the simpleObjectTracer
	 */
	public SimpleObjectTracer getSimpleObjectTracer() {
		return simpleObjectTracer;
	}
	/**
	 * @param simpleObjectTracer the simpleObjectTracer to set
	 */
	public void setSimpleObjectTracer(SimpleObjectTracer simpleObjectTracer) {
		this.simpleObjectTracer = simpleObjectTracer;
	}
	/**
	 * @return the objectTracer
	 */
	public List<ObjectTracer> getObjectTracers() {
		return objectTracers;
	}
	/**
	 * @param objectTracer the objectTracer to set
	 */
	public void addObjectTracer(ObjectTracer objectTracer) {
		objectTracers.add(objectTracer);
	}
	
	/**
	 * Adds a list of object tracers.
	 * @param objectTracers
	 */
	public void addObjectTracers(List<ObjectTracer> objectTracers) {
		this.objectTracers.addAll(objectTracers);
	}
	/**
	 * @return the mandatory
	 */
	public boolean isMandatory() {
		return mandatory;
	}
	/**
	 * @param mandatory the mandatory to set
	 */
	public void setMandatory(boolean mandatory) {
		this.mandatory = mandatory;
	}
	/**
	 * @return the defaultValue
	 */
	public String getDefaultValue() {
		return defaultValue;
	}
	/**
	 * @param defaultValue the defaultValue to set
	 */
	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}
	/**
	 * @return the groovyTracers
	 */
	public boolean isGroovyTracers() {
		return groovyTracers;
	}
	/**
	 * @param groovyTracers the groovyTracers to set
	 */
	public void setGroovyTracers(boolean groovyTracers) {
		this.groovyTracers = groovyTracers;
	}
	
}
