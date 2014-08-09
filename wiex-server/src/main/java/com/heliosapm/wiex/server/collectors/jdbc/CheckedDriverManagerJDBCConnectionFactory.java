/**
 * 
 */
package com.heliosapm.wiex.server.collectors.jdbc;

import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Date;
import java.util.Properties;

import com.heliosapm.wiex.tracing.tracing.ITracer;
import com.heliosapm.wiex.tracing.tracing.TracerFactory;

/**
 * <p>Title: CheckedDriverManagerJDBCConnectionFactory</p>
 * <p>Description: DriverManager Connection Factory that is checked by a timeout thread to alert if a connection request stalls indefinitely.
 * The timeout should be defined in the configuration properties under the key <code>check.connect.timeout</code> or will default to 10,000 ms.
 * If the connection request times out, an interval incident will be traced to the metric name <code>Connection Timeout</code> under the segment
 * defined by the config property <code>check.connect.tracing.segment</code>. The segment should be specified in comma separated subsegments.
 * e.g. <code>Oracle,TelProd,Connections</code></p> 
 * <p>Copyright: Copyright (c) 2007</p>
 * <p>Company: Helios Development Group</p>
 * @author Whitehead
 * @version $Revision: 1.1 $
 */

public class CheckedDriverManagerJDBCConnectionFactory extends DriverManagerJDBCConnectionFactory implements Thread.UncaughtExceptionHandler {
	
	/**	Property key for time out */
	public static final String JDBC_CHECK_TIMEOUT = "check.connect.timeout";
	/**	Property key for connector interrupt */
	public static final String JDBC_CONNECTOR_INTERRUPT = "check.connect.interrupt";
	/**	Property key for connector debug */
	public static final String JDBC_CONNECTOR_DEBUG = "check.connect.debug";
	
	/**	Property key for tracing segment name */
	public static final String CHECK_CONNECT_TRACING_SEGMENT = "check.connect.tracing.segment";
	/**	Property key for tracing segment name */
	public static final String JDBC_CHECK_TIMEOUT_DEFAULT = "10000";
	/**	Turns on some additional logging to System.out when set to true */
	protected boolean DEBUG = false;
	
	/**	The check thread time out */
	protected long timeOut = 0L;
	/**	The tracing segment */
	protected String tracingSegment = null;
	/** The tracer */
	protected ITracer tracer = null;
	/** If true, then interrupt the connecting thread on timeout */
	protected boolean interruptConnector = false;
	
	/**
	 * Simple constructor. Calls super and then configures the tracer.
	 */
	public CheckedDriverManagerJDBCConnectionFactory() {
		super();
		tracer = TracerFactory.getInstance();
	}
	
	
	/**
	 * Acquires and returns a JDBC connection from the <code>java.sql.DriverManager</code>.
	 * A time thread is started to check that the connection is made within the timeout period.
	 * @return A JDBC Connection
	 * @throws JDBCConnectionFactoryException
	 * @see com.heliosapm.wiex.server.collectors.jdbc.JDBCConnectionFactory#getJDBCConnection()
	 */
	public Connection getJDBCConnection() throws JDBCConnectionFactoryException, JDBCConnectionFactoryTimeOutException {
		if(!isRegistered) {
			try {
				Class.forName(jdbcDriver);
				DriverManager.setLoginTimeout((int)timeOut/1000);
				isRegistered = true;
			} catch (Exception e) {
				throw new JDBCConnectionFactoryException("Failed to load driver:" + jdbcDriver, e);
			}
		}
		try {
			ConnectionCheckThread cct = new ConnectionCheckThread(timeOut, this, Thread.currentThread(), interruptConnector);
			cct.start();
			Connection conn = DriverManager.getConnection(jdbcUrl, connectionProperties);
			tracer.recordIntervalIncident(tracingSegment, "Connection Acquired");
			if(!cct.isSleeping()) {
				throw new JDBCConnectionFactoryTimeOutException();
			}
			cct.setComplete(true);
			cct.interrupt();
			return conn;
		} catch (Throwable e) {			
			if(DEBUG) e.printStackTrace();
			String errSeg = tracer.buildSegment(tracingSegment, false, "Errors");
			tracer.recordIntervalIncident(errSeg, e.getMessage());
			if(Thread.currentThread().isInterrupted()) {
				String traceSeg = tracer.buildSegment(tracingSegment, false, "Interrupted");
				tracer.recordIntervalIncident(traceSeg, "Connection Timeout");				
				tracer.recordCounterMetric(tracingSegment, "Availability", 0);
				tracer.recordCounterMetric(tracingSegment, "Connection Time", -1L);
				tracer.recordCounterMetric(tracingSegment, "Query Time", -1L);
			}
			throw new JDBCConnectionFactoryException("Failed to acquire connection", e);
		}
	}
	
	/**
	 * Sets the factories properties using the super implementation, then configures the time out and tracing segment.
	 * @param properties
	 * @see com.heliosapm.wiex.server.collectors.jdbc.DriverManagerJDBCConnectionFactory#setProperties(java.util.Properties)
	 */
	@Override
	public void setProperties(Properties properties) {
		super.setProperties(properties);
		String to = connectionProperties.getProperty(JDBC_CHECK_TIMEOUT, JDBC_CHECK_TIMEOUT_DEFAULT);			
		timeOut = Long.parseLong(to);
		interruptConnector = properties.getProperty(JDBC_CONNECTOR_INTERRUPT, "false").equalsIgnoreCase("true");
		DEBUG = properties.getProperty(JDBC_CONNECTOR_DEBUG, "false").equalsIgnoreCase("true");
		String[] fragments = connectionProperties.getProperty(CHECK_CONNECT_TRACING_SEGMENT).split(",");
		if(fragments==null || fragments.length < 1) fragments = new String[]{"Connection Testing", jdbcUrl};
		tracingSegment = tracer.buildSegment(fragments);		
	}

	/**
	 * Thrown when the connection timeout check thread expires.
	 * @param t
	 * @param e
	 * @see java.lang.Thread$UncaughtExceptionHandler#uncaughtException(java.lang.Thread, java.lang.Throwable)
	 */
	public void uncaughtException(Thread t, Throwable e) {
		if(e instanceof ConnectionTimeOutException) {			
			ConnectionTimeOutException ctoe = (ConnectionTimeOutException)e;
			String traceSeg = tracer.buildSegment(tracingSegment, false, ctoe.getConnectingThread().getState().toString());
			tracer.recordIntervalIncident(traceSeg, "Connection Timeout");
			if(DEBUG) {
				// =======================================
				// For testing only
				// =======================================
				StringBuilder buff = new StringBuilder();
				buff.append("\nConnection Timeout Detected.");
				buff.append("\n\tURL:").append(jdbcUrl);
				buff.append("\n\tTimeout:").append(timeOut);
				buff.append("\n\tThread State:").append(ctoe.getConnectingThread().getState());
				buff.append("\n\tCurrent Thread:").append(Thread.currentThread().getName());
				buff.append("\n\tInterrupt Connector:").append(interruptConnector);
				buff.append("\n\tConnecting Thread:").append(ctoe.getConnectingThread().getName());
				
				buff.append("\n\tTracing Segment:").append(traceSeg);
				log(buff.toString());
			}
			// =======================================
			// ctoe.getConnectingThread().interrupt();
		} else {
			// some other exception occured that we did not expect.
			tracer.recordIntervalIncident(tracingSegment, "Exception");
			if(DEBUG) {
				// =======================================
				// For testing only
				// =======================================
				StringBuilder buff = new StringBuilder();
				buff.append("\nConnection Timeout Exception Detected.");
				buff.append("\n\tURL:").append(jdbcUrl);
				buff.append("\n\tTimeout:").append(timeOut);
				buff.append("\n\tTracing Segment:").append(tracingSegment);
				buff.append("\n\tException:").append(e);
				log(buff.toString());
			}
			// =======================================
			
		}
	}	
	
	public static void log(Object message) {
		System.out.println("[" + new Date() + "]:" + message);
	}
	
	/**
	 * Parameterles main to run a command line test of the class.
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		
		log("Test CheckedDriverManagerJDBCConnectionFactory");
		Properties p = new Properties();
		//p.put(JDBC_DRIVER, CatatonicJDBCDriver.class.getName());
		p.put(JDBC_DRIVER, CatatonicJDBCSocketDriver.class.getName());		
		p.put(JDBC_URL, "catatonic://sleep.for.a.long.time");
		p.put(JDBC_USER, "");
		p.put(JDBC_PASSWORD,  "");
		p.put(JDBC_CHECK_TIMEOUT, "5000");
		p.put("sleep.time", "7000");
		p.put(CHECK_CONNECT_TRACING_SEGMENT, "Catatonic,Connection Test");
		DriverManager.registerDriver(CatatonicJDBCSocketDriver.class.newInstance());
		DriverManager.setLogWriter(new PrintWriter(System.out));
		CheckedDriverManagerJDBCConnectionFactory factory = new CheckedDriverManagerJDBCConnectionFactory();
		factory.DEBUG=true;
		factory.setProperties(p);
		ServerSocket socket = new ServerSocket();
		InetSocketAddress address = new InetSocketAddress("localhost", 9950);
		socket.bind(address);
		log("Factory Created");
		try {
			log("Issuing Connection Request");
			factory.getJDBCConnection();
			log("Connection Request Complete");
		} catch (Exception e) {
			log("Connection Acquisition Failure");
			e.printStackTrace();
		}
		
		
	}

}
