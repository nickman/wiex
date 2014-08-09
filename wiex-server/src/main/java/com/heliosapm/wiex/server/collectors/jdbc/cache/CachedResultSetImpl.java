/**
 * 
 */
package com.heliosapm.wiex.server.collectors.jdbc.cache;

import java.io.Serializable;

import org.apache.log4j.Logger;

import com.heliosapm.wiex.jmx.dynamic.annotation.JMXAttribute;
import com.heliosapm.wiex.jmx.dynamic.annotation.JMXManagedObject;
//import com.heliosapm.wiex.jmx.dynamic.annotation.JMXOperation;

/**
 * <p>Title: CachedResultSetImpl</p>
 * <p>Description: Simple concrete implementation of CachedResultSet </p> 
 * <p>Copyright: Copyright (c) 2007</p>
 * <p>Company: Helios Development Group</p>
 * @author Whitehead
 * @version $Revision$
 */

@JMXManagedObject (annotated=true, declared=false)
public class CachedResultSetImpl implements CachedResultSet, Serializable {
	
	
	private static final long serialVersionUID = 5855994342616870094L;
	protected String[][] resultSet = null;
	protected String name = null;
	protected long elapsedTime = 0;
	protected long postProcessElapsedTime = 1;
	protected int rowCount = 0;
	protected int columnCount = 0;
	protected Logger log = null;
	protected long lastChangeTime = 0L;
	

	
	/**
	 * @return the elapsedTime
	 */
	public long getElapsedTime() {
		return elapsedTime;
	}

	/**
	 * @param elapsedTime the elapsedTime to set
	 */
	public void setElapsedTime(long elapsedTime) {
		this.elapsedTime = elapsedTime;
	}

	/**
	 * @return the rowCount
	 */
	public int getRowCount() {
		return rowCount;
	}

	/**
	 * Creates a new CachedResultSetImpl
	 * @param name The name of the cached result set.
	 */
	public CachedResultSetImpl(String name) {
		this.name = name;
		log = Logger.getLogger(this.getClass());
	}

	/**
	 * @return the resultSet
	 */
	@JMXAttribute (introspect=true, name="getName", description="getDescription")
	public synchronized String[][] getResultSet() {
		return resultSet;
	}
	
	/**
	 * @return the rendered resultSet
	 */
	@JMXAttribute (introspect=true, name="getRendName", description="getRendDescription")
	public String getRenderedResultSet() {
		return renderResultSet();
	}
	
	/**
	 * Returns a reference to this object.
	 * @return
	 */
	@JMXAttribute (introspect=true, name="getThisName", description="getThisDescription")
	public CachedResultSet getCachedResultSet() {
		return this;
	}
	
	/**
	 * Initializes this object from a passed CachedResultSet.
	 * @param resultSet
	 */
	public synchronized void setCachedResultSet(CachedResultSet resultSet) {
		this.setResultSet(resultSet.getResultSet());		
		this.name = resultSet.getName();
		this.elapsedTime = resultSet.getElapsedTime();
	}
	
	
	/**
	 * @param resultSet the resultSet to set
	 */
	public void setResultSet(String[][] resultSet) {
		this.resultSet = resultSet;
		if(this.resultSet==null || this.resultSet.length < 1) {
			rowCount = 0;
			columnCount = 0;
		}
		else {
			rowCount = resultSet.length;
			if(resultSet[0]==null || resultSet[0].length < 1) {
				columnCount = 0;
			} else {
				columnCount = resultSet[0].length;
			}
			lastChangeTime = System.currentTimeMillis();
		}
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * @return the name
	 */
	public String getThisName() {
		return name + "Container";
	}
	
	/**
	 * @return
	 */
	public String getThisDescription() {
		return THIS_DESCRIPTION;
	}
	
	
	/**
	 * @return the operation name for retrieving a result set using a renderer.
	 */
	public String getRendName() {
		return "rendered" + name;
	}	
	
	public String getDescription() {
		return DESCRIPTION;
	}
	
	public String getRendDescription() {
		return REND_DESCRIPTION;
	}
	
	public String toString() {		
		StringBuilder buff = new StringBuilder("Cached Result Set:");
		buff.append("Name:").append(name);
		buff.append(" Rows:").append(rowCount);
		buff.append(" Columns:").append(columnCount);
		buff.append(" Elapsed:").append(elapsedTime);
		return buff.toString();
	}
	
	/**
	 * Renders the current result set into an HTML table.
	 * @return
	 */
	protected synchronized String renderResultSet() {
		StringBuilder buff = new StringBuilder("<table border=\"1\">");
		if(resultSet==null || resultSet.length < 1 || resultSet[0].length < 1) {
			buff.append("</table>");
			return buff.toString();
		}
		// Generate Details
		for(int i = 0; i < resultSet.length; i++) {
			buff.append("<tr>");
			for(int x = 0; x < resultSet[i].length; x++) {
				if(i==0) {
					buff.append("<th>").append(resultSet[i][x]).append("</th>");
				} else {
					buff.append("<td>").append(resultSet[i][x]).append("</td>");
				}
				
			}
			buff.append("</tr>");
		}
		buff.append("</table>");
		buff.append("<p><font size=\"-2\"><i>Rows:").append(rowCount);
		buff.append("&nbsp;Elapsed Time:").append(elapsedTime).append(" ms.");
		if(postProcessElapsedTime != -1) {
			buff.append("&nbsp;PP Time:").append(postProcessElapsedTime).append(" ms.");
		}
		buff.append("</i></font></p>");
		
		return buff.toString();
	}

	/**
	 * @return the columnCount
	 */
	public int getColumnCount() {
		return columnCount;
	}

	public long getPostProcessElapsedTime() {
		return postProcessElapsedTime;		
	}

	public void setPostProcessElapsedTime(long time) {
		postProcessElapsedTime = time;
		
	}
	
	@JMXAttribute (introspect=true, name="getLastChangeTimeName", description="getLastChangeTimeDescription")
	public long getLastChangeTime() {
		return lastChangeTime;
	}
	
	public String getLastChangeTimeName() {
		return name + "LastChangeTime";
	}
	
	public String getLastChangeTimeDescription() {
		return "Time Stamp of Last Change Time for " + name;
	}	
	
}
