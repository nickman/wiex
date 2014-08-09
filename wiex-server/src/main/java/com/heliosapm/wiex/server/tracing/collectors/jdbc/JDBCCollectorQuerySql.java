/**
 * 
 */
package com.heliosapm.wiex.server.tracing.collectors.jdbc;

/**
 * <p>Title: JDBCCollectorQuerySql</p>
 * <p>Description: Container for Query sql and options.</p> 
 * <p>Copyright: Copyright (c) 2007</p>
 * <p>Company: Helios Development Group</p>
 * @author Whitehead
 * @version $Revision: 1.7 $
 */
public class JDBCCollectorQuerySql {
	/** The SQL to run */
	protected String sql = null;
	/** false if bind variables and PreparedStatements are not supported or used in this query */
	protected boolean useBinds = true;
	
	/**
	 * Creates a new JDBCCollectorQuerySql with a useBinds default of true and no Sql. 
	 */
	public JDBCCollectorQuerySql() {
		
	}
	
	/**
	 * Creates a new JDBCCollectorQuerySql with a useBinds default of true.
	 * @param sql The sql of the query to run.
	 */
	public JDBCCollectorQuerySql(String sql) {
		this.sql = sql;
	}
	
	/**
	 * Creates a new JDBCCollectorQuerySql.
	 * @param sql The sql of the query to run.
	 */
	public JDBCCollectorQuerySql(String sql, boolean useBinds) {
		this.sql = sql;
		this.useBinds = useBinds;
	}
	
	
}
