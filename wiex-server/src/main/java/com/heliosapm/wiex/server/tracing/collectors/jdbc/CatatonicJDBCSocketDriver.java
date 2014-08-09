/**
 * 
 */
package com.heliosapm.wiex.server.tracing.collectors.jdbc;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

/**
 * <p>Title: CatatonicJDBCSocketDriver</p>
 * <p>Description: An extension of the catatonic driver that listens on a sleepy socket to emulate a slow connect instead of using Thread.sleep.</p> 
 * <p>Copyright: Copyright (c) 2007</p>
 * <p>Company: Helios Development Group</p>
 * @author Whitehead
 * @version $Revision: 1.1 $
 */

	

public class CatatonicJDBCSocketDriver extends CatatonicJDBCDriver {
	protected int port = 0;
	protected String host = null;
	protected int timeOut = 0;
	
	public Connection connect(String url, Properties info) throws SQLException {
		port = Integer.parseInt(info.getProperty("catatonic.port", "9950"));
		host = info.getProperty("catatonic.host", "localhost");
		timeOut = Integer.parseInt(info.getProperty("sleep.time", "100000"));
		Socket socket = new Socket();
		try {
			socket.setSoTimeout(timeOut);
			InetSocketAddress address = new InetSocketAddress(host, port); 
			socket.connect(address, timeOut);
			socket.getInputStream().read();
		} catch (java.net.SocketTimeoutException ste) {
			
		} catch (Exception e) {
			throw new SQLException("Failed to listen on sleepy socket[" + host + ":" + port + "]:" + e);
		} finally {
			try {socket.close();} catch (Exception e){}
		}
		return new CatatonicJDBCConnection();
	}
}
