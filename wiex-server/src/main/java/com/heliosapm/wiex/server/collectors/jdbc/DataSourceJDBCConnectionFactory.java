package com.heliosapm.wiex.server.collectors.jdbc;

import java.sql.Connection;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

/**
 * <p>Title: DataSourceJDBCConnectionFactory</p>
 * <p>Description: Acquires a JDBC Connection from a JNDI resident data source.</p>
 * <p>Configuration Properties:<ul>
 * <li><code>jndi.datasource.name</code> The JNDI name of the target datasource. (In VM)
 * </ul>  
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Company: Helios Development Group</p>
 * @author Whitehead
 * @version $Revision: 1.1 $
 */

public class DataSourceJDBCConnectionFactory implements JDBCConnectionFactory {
	
	/**	The acquired data source from JNDI */
	protected DataSource dataSource = null;
	/** The JNDI name of the target data source */
	protected String dataSourceName = null;
	
	public static final String JNDI_DATASOURCE_NAME = "jndi.datasource.name";

	/**
	 * No Op.
	 * @see com.heliosapm.wiex.server.collectors.jdbc.JDBCConnectionFactory#close()
	 */
	public void close() {
		
	}

	/**
	 * Acquires and returns a JDBC Connection.
	 * @return A JDBC Connection
	 * @throws JDBCConnectionFactoryException
	 * @see com.heliosapm.wiex.server.collectors.jdbc.JDBCConnectionFactory#getJDBCConnection()
	 */
	public Connection getJDBCConnection() throws JDBCConnectionFactoryException {
		if(dataSource==null) {
			Context ctx = null;
			try {
				ctx = new InitialContext();
				dataSource = (DataSource)ctx.lookup(dataSourceName);
			} catch (Exception e) {
				throw new JDBCConnectionFactoryException("Failed to Acquire Connection", e);
			} finally {
				try { ctx.close(); } catch (Exception e) {}
			}
		}
		try {
			return dataSource.getConnection();
		} catch (Exception e) {
			throw new JDBCConnectionFactoryException("Failed to Acquire Connection", e);
		}
	}

	/**
	 * Sets the jndi name of the data source.
	 * @param properties
	 * @see com.heliosapm.wiex.server.collectors.jdbc.JDBCConnectionFactory#setProperties(java.util.Properties)
	 */
	public void setProperties(Properties properties) {
		dataSourceName = properties.getProperty(JNDI_DATASOURCE_NAME);

	}

}
