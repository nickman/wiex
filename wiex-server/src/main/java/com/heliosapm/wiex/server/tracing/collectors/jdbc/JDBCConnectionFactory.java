/**
 * 
 */
package com.heliosapm.wiex.server.tracing.collectors.jdbc;

import java.sql.Connection;
import java.util.Properties;

/**
 * <p>Title: JDBCConnectionFactory</p>
 * <p>Description: Interface defining behaviour for JDBCConnectionFactories for acquring JDBC Connections.
 * Two current options are raw connections and JNDI data sources.<p>
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Company: Helios Development Group</p>
 * @author Whitehead
 * @version $Revision: 1.1 $
 */

public interface JDBCConnectionFactory {
	
	/**
	 * Acquires and returns a JDBC Connection.
	 * @return A JDBC Connection.
	 * @throws JDBCConnectionFactoryException
	 */
	public Connection getJDBCConnection() throws JDBCConnectionFactoryException;
		
	
	
	/**
	 * Sets the configuration parameters for the connection factory.
	 * @param properties The configuration parameters.
	 */
	public void setProperties(Properties properties);	
	
	/**
	 * Closes any resources opened and held by the factory.
	 */
	public void close();

}
