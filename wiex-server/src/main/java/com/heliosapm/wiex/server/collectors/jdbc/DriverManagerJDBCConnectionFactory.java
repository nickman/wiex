/**
 * 
 */
package com.heliosapm.wiex.server.collectors.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;
import java.util.Map.Entry;

/**
 * <p>Title: DriverManagerJDBCConnectionFactory</p>
 * <p>Description: Acquires a new JDBC connection from the <code>java.sql.DriverManager</code>.</p>
 * <p>Configuration Properties:<ul>
 * <li><code>jdbc.driver</code> The name of the JDBC Driver Class.
 * <li><code>jdbc.url</code> The JDBC Connection URL.
 * <li><code>jdbc.user</code> The Database user name.
 * <li><code>jdbc.password</code> The Database user password.
 * <li>Any properties prefixed with <code>sbstracing.jdbc.property</code> will be passed as a connection property.
 * </ul>  
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Company: Helios Development Group</p>
 * @author Whitehead
 * @version $Revision: 1.3 $
 */

public class DriverManagerJDBCConnectionFactory implements
		JDBCConnectionFactory {
	
	public static final String JDBC_DRIVER = "jdbc.driver";
	public static final String JDBC_URL = "jdbc.url";
	public static final String JDBC_USER = "jdbc.user";
	public static final String JDBC_PASSWORD = "jdbc.password";
	public static final String JDBC_CONNECTION_PROPERTY_PREFIX = "sbstracing.jdbc.property";

	protected String jdbcDriver = null;
	protected String jdbcUrl = null;
	protected String jdbcUser = null;
	protected String jdbcPassword = null;
	protected Properties connectionProperties = new Properties();
	protected boolean isRegistered = false;
	
	/**
	 * No Op.
	 * @see com.heliosapm.wiex.server.collectors.jdbc.JDBCConnectionFactory#close()
	 */
	public void close() {

	}

	/**
	 * Acquires and returns a JDBC connection from the <code>java.sql.DriverManager</code>.
	 * @return A JDBC Connection
	 * @throws JDBCConnectionFactoryException
	 * @see com.heliosapm.wiex.server.collectors.jdbc.JDBCConnectionFactory#getJDBCConnection()
	 */
	public Connection getJDBCConnection() throws JDBCConnectionFactoryException {
		if(!isRegistered) {
			try {
				Class.forName(jdbcDriver);
				isRegistered = true;
			} catch (Exception e) {
				throw new JDBCConnectionFactoryException("Failed to load driver:" + jdbcDriver, e);
			}
		}
		try {
			return DriverManager.getConnection(jdbcUrl, connectionProperties);
		} catch (Exception e) {
			throw new JDBCConnectionFactoryException("Failed to acquire connection", e);
		}
	}

	/**
	 * @param properties
	 * @see com.heliosapm.wiex.server.collectors.jdbc.JDBCConnectionFactory#setProperties(java.util.Properties)
	 */
	public void setProperties(Properties properties) {
		jdbcDriver = properties.getProperty(JDBC_DRIVER);
		jdbcUrl = properties.getProperty(JDBC_URL);
		jdbcUser = properties.getProperty(JDBC_USER);
		jdbcPassword = properties.getProperty(JDBC_PASSWORD);
		connectionProperties.clear();
		connectionProperties.putAll(properties);
		connectionProperties.put("user", jdbcUser);
		connectionProperties.put("password", jdbcPassword);
	}
	

	

}
