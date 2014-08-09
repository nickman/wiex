/**
 * 
 */
package com.heliosapm.wiex.server.tracing.collectors.groovy;

import groovy.lang.GroovyShell;

/**
 * <p>Title: ScriptEvaluation</p>
 * <p>Description: A container for a Groovy Script Evaluation.</p> 
 * <p>Copyright: Copyright (c) 2007</p>
 * <p>Company: Helios Development Group</p>
 * @author Whitehead
 * @version $Revision: 1.2 $
 */

public class ScriptEvaluation {
	protected String groovyText = null;
	protected String metricName = null;
	protected String metricType = null;
	protected GroovyShell shell = null;
	protected String[] segment = null;
	/**
	 * @param groovyText
	 * @param metricName
	 * @param metricType
	 * @param shell
	 */
	public ScriptEvaluation(String groovyText, String metricName, String metricType, String[] segment, GroovyShell shell) {
		this.groovyText = groovyText;
		this.metricName = metricName;
		this.metricType = metricType;
		this.segment = segment;
		this.shell = shell;
	}
	/**
	 * @return the groovyText
	 */
	public String getGroovyText() {
		return groovyText;
	}
	/**
	 * @param groovyText the groovyText to set
	 */
	public void setGroovyText(String groovyText) {
		this.groovyText = groovyText;
	}
	/**
	 * @return the metricName
	 */
	public String getMetricName() {
		return metricName;
	}
	/**
	 * @param metricName the metricName to set
	 */
	public void setMetricName(String metricName) {
		this.metricName = metricName;
	}
	/**
	 * @return the metricType
	 */
	public String getMetricType() {
		return metricType;
	}
	/**
	 * @param metricType the metricType to set
	 */
	public void setMetricType(String metricType) {
		this.metricType = metricType;
	}
	/**
	 * @return the shell
	 */
	public GroovyShell getShell() {
		return shell;
	}
	/**
	 * @param shell the shell to set
	 */
	public void setShell(GroovyShell shell) {
		this.shell = shell;
	}
	/**
	 * Constructs a <code>String</code> with all attributes
	 * in name = value format.
	 *
	 * @return a <code>String</code> representation 
	 * of this object.
	 */
	public String toString()
	{
	    final String TAB = "    \n\t";	
	    StringBuilder retValue = new StringBuilder();	    
	    retValue.append("ScriptEvaluation ( \n\t")	        
	        .append("groovyText = ").append(this.groovyText).append(TAB)
	        .append("metricName = ").append(this.metricName).append(TAB)
	        .append("metricType = ").append(this.metricType).append(TAB)
	        .append(" )");	    
	    return retValue.toString();
	}
	/**
	 * @return the segment
	 */
	public String[] getSegment() {
		return segment;
	}
	/**
	 * @param segment the segment to set
	 */
	public void setSegment(String[] segment) {
		this.segment = segment;
	}
	
}
