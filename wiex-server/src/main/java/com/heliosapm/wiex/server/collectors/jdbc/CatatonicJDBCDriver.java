/**
 * 
 */
package com.heliosapm.wiex.server.collectors.jdbc;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * <p>Title: CatatonicJDBCDriver</p>
 * <p>Description: A test JDBC Driver implementation that sleeps for a timed period to emulate infinte connection request times.</p> 
 * <p>Copyright: Copyright (c) 2007</p>
 * <p>Company: Helios Development Group</p>
 * @author Whitehead
 * @version $Revision: 1.1 $
 */

public class CatatonicJDBCDriver implements Driver {

	/**
	 * @param url
	 * @return
	 * @throws SQLException
	 * @see java.sql.Driver#acceptsURL(java.lang.String)
	 */
	public boolean acceptsURL(String url) throws SQLException {
		return true;
	}

	/**
	 * Sleeps for a period of time and then returns null.
	 * The sleep time can be passed in as a property <code>sleep.time</code> or defaults to 100,000 ms.
	 * @param url
	 * @param info
	 * @return
	 * @throws SQLException
	 * @see java.sql.Driver#connect(java.lang.String, java.util.Properties)
	 */
	public Connection connect(String url, Properties info) throws SQLException {
		String to = info.getProperty("sleep.time", "100000");
		long sleepTime = Long.parseLong(to);
		try {
			Thread.sleep(sleepTime);
		} catch (Exception e) {
			throw new SQLException("Failed to sleep for " + sleepTime + " ms. :" + e);
		}
		return new CatatonicJDBCConnection();
	}

	/**
	 * @return
	 * @see java.sql.Driver#getMajorVersion()
	 */
	public int getMajorVersion() {
		return 1;
	}

	/**
	 * @return
	 * @see java.sql.Driver#getMinorVersion()
	 */
	public int getMinorVersion() {
		return 0;
	}

	/**
	 * @param url
	 * @param info
	 * @return
	 * @throws SQLException
	 * @see java.sql.Driver#getPropertyInfo(java.lang.String, java.util.Properties)
	 */
	public DriverPropertyInfo[] getPropertyInfo(String url, Properties info)
			throws SQLException {
		return new DriverPropertyInfo[]{};
	}

	/**
	 * @return
	 * @see java.sql.Driver#jdbcCompliant()
	 */
	public boolean jdbcCompliant() {
		return true;
	}

	@Override
	public Logger getParentLogger() throws SQLFeatureNotSupportedException {
		// TODO Auto-generated method stub
		return null;
	}

}
