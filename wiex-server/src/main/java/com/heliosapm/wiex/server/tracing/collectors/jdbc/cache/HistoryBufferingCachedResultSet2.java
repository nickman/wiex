/**
 * 
 */
package com.heliosapm.wiex.server.tracing.collectors.jdbc.cache;


import com.heliosapm.wiex.jmx.dynamic.annotation.JMXManagedObject;

/**
 * <p>Title: HistoryBufferingCachedResultSet2</p>
 * <p>Description: Alternate version of HistoryBufferingCachedResultSet that uses a long instead of a date string to index the histoiry.</p> 
 * <p>Copyright: Copyright (c) 2007</p>
 * <p>Company: Helios Development Group</p>
 * @author Whitehead
 * @version $Revision$
 */
@JMXManagedObject (annotated=true, declared=false)
public class HistoryBufferingCachedResultSet2 extends
		HistoryBufferingCachedResultSet {
	
	
	/**	 */
	private static final long serialVersionUID = 442419122518819322L;

	/**
	 * Create a new HistoryBufferingCachedResultSetHistoryBufferingCachedResultSet2
	 * @param name The name of the cached result set.
	 */
	public HistoryBufferingCachedResultSet2(String name) {
		super(name);		
	}
	
	/**
	 * Sets the result set and buffers history.
	 * @param resultSet the resultSet to set
	 */
	@Override
	public synchronized void setResultSet(String[][] resultSet) {		
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
		// ========================================================
		if(resultSet==null || resultSet.length<1 || resultSet[0]==null || resultSet[0].length < 1) return;
		long dt = System.currentTimeMillis();
	
		if(history.size()==historySize) {
			String key = history.firstKey().toString();
			history.remove(key);				
		}
		if(history.size() < 1 && header==null) {
			header = new String[resultSet[0].length+1];
			header[0] = "Timestamp";
			for(int i = 0; i < resultSet[0].length; i++) {
				header[i+1] = resultSet[0][i];
			}
		}
		String[][] nResultSet = new String[resultSet.length-1][];
		for(int i = 1; i < resultSet.length; i++) {
			nResultSet[i-1] = resultSet[i];
		}			
		history.put("" + dt, nResultSet);
		
	}
		

}
