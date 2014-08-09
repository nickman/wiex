/**
 * 
 */
package com.heliosapm.wiex.server.tracing.collectors.jdbc.cache;

/**
 * <p>Title: CachedResultSet</p>
 * <p>Description: Interface to define behaviour of a cached jdbc result set.</p> 
 * <p>Copyright: Copyright (c) 2007</p>
 * <p>Company: Helios Development Group</p>
 * @author Whitehead
 * @version $Revision$
 */

public interface CachedResultSet {
	
	public static final String DESCRIPTION = "Cached JDBC Result Set";
	public static final String REND_DESCRIPTION = "Rendered Cached JDBC Result Set";
	public static final String THIS_DESCRIPTION = "Cached JDBC Result Set Container";

	
	public String[][] getResultSet();
	public String getRenderedResultSet();
	public void setResultSet(String[][] resultSet);
	public String getName();
	public String getRendName();
	public String getDescription();
	public String getRendDescription();
	public long getElapsedTime();
	public long getPostProcessElapsedTime();
	public void setPostProcessElapsedTime(long time);
	public void setElapsedTime(long elapsedTime);
	public int getRowCount();
	public int getColumnCount();
	public String getThisName();
	public String getThisDescription();
	public long getLastChangeTime();
	
	
	
}

