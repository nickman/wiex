/**
 * 
 */
package com.heliosapm.wiex.server.collectors.tomcat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import javax.management.MalformedObjectNameException;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.heliosapm.wiex.jmx.dynamic.annotation.JMXAttribute;
import com.heliosapm.wiex.jmx.dynamic.annotation.JMXManagedObject;
import com.heliosapm.wiex.jmx.dynamic.annotation.JMXOperation;
import com.heliosapm.wiex.server.collectors.BaseCollector;

/**
 * <p>Title: TomcatThreadStatusCollector</p>
 * <p>Description: Collector for Tomcat HTTP and AJP Thread Pool Status </p> 
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Company: Helios Development Group</p>
 * @author Dubeys
 * @author Whitehead
 * @version $Revision: 1.3 $
 * Needs to wait on JMX notification for "jboss.tomcat.connectors.started" from jboss.web:service=WebServer
 */
@JMXManagedObject(annotated=true, declared=false)
public class TomcatThreadStatusCollector extends BaseCollector implements NotificationListener {

	/**	The host name to get stats from */
	protected String hostName = null;
	/**	The Tomcat HTTP Port */
	protected int httpPort = 0;
	/**	The Tomcat AJP Port  */
	protected int ajpPort = 0;
	/**	The Tomcat HTTP Connector Name */
	protected String httpConnectorName = null;
	/**	The Tomcat AJP Connector Name */
	protected String ajpConnectorName = null;
	
	
	/** Maximum Threads in HTTP Thread Pool	 */
	protected int httpMaxThreads = 0;
	/**	Minimum Spare Threads in HTTP Thread Pool */
	protected int httpMinSpareThreads = 0;
	/**	Maximum Spare Threads in HTTP Thread Pool */
	protected int httpMaxSpareThreads = 0;
	/**	Current Number of Threads in HTTP Pool */
	protected int httpCurrentThreadCount = 0;
	/**	Current Number of Busy Threads in HTTP Pool */
	protected int httpCurrentThreadsBusy = 0;
	/**	Longest Request Time for HTTP Request */
	protected long httpMaxTime = 0L;
	/**	Total Request Time for HTTP Requests */
	protected long httpProcessingTime = 0L;
	/**	Total Number of HTTP Requests */
	protected long httpRequestCount = 0L;
	/**	Average Request Time for HTTP Requests */
	protected long httpAverageRequestTime = 0L;
	/**	Total Number of HTTP Requests In Last Interval */
	protected long httpRequestCountDelta = 0L;
	/**	Total Number of HTTP Errors */
	protected long httpErrorCount = 0L;
	/**	Total Bytes Received by HTTP Threads */
	protected long httpBytesReceived = 0L;
	/**	Total Bytes Sent by HTTP Threads */
	protected long httpBytesSent= 0L;
	/** HTTP Threads in Parse & Prepare Request State */
	protected int httpThreadStatusP = 0;
	/** HTTP Threads in Service State */
	protected int httpThreadStatusS = 0;
	/** HTTP Threads in Finishing State */
	protected int httpThreadStatusF = 0;
	/** HTTP Threads in Ready State */
	protected int httpThreadStatusR = 0;
	/** HTTP Threads in Keep Alive State */
	protected int httpThreadStatusK = 0;
	
	
	
	/** Maximum Threads in AJP Thread Pool	 */
	protected int ajpMaxThreads = 0;
	/**	Minimum Spare Threads in AJP Thread Pool */
	protected int ajpMinSpareThreads = 0;
	/**	Maximum Spare Threads in AJP Thread Pool */
	protected int ajpMaxSpareThreads = 0;
	/**	Current Number of Threads in AJP Pool */
	protected int ajpCurrentThreadCount = 0;
	/**	Current Number of Busy Threads in AJP Pool */
	protected int ajpCurrentThreadsBusy = 0;
	/**	Longest Request Time for AJP Request */
	protected long ajpMaxTime = 0L;
	/**	Total Request Time for AJP Requests */
	protected long ajpProcessingTime = 0L;
	/**	Total Request Time for AJP Requests In Last Interval */
	protected long ajpProcessingTimeDelta = 0L;
	/**	Total Number of AJP Requests */
	protected long ajpRequestCount = 0L;
	/**	Average Request Time for AJP Requests */
	protected long ajpAverageRequestTime = 0L;
	/**	Average Request Time for AJP Requests In Last Interval*/
	protected long ajpAverageRequestTimeDelta = 0L;	
	/**	Total Number of AJP Requests In Last Interval */
	protected long ajpRequestCountDelta = 0L;
	/**	Total Number of AJP Errors */
	protected long ajpErrorCount = 0L;
	/**	Total Number of AJP Requests In Last Interval */
	protected long ajpErrorCountDelta = 0L;
	/**	Total Bytes Received by AJP Threads */
	protected long ajpBytesReceived = 0L;
	/**	Total Bytes Received by AJP Threads In Last Interval */
	protected long ajpBytesReceivedDelta = 0L;
	/**	Total Bytes Sent by AJP Threads */
	protected long ajpBytesSent= 0L;
	/**	Total Bytes Sent by AJP Threads In Last Interval */
	protected long ajpBytesSentDelta = 0L;
	/** AJP Threads in Parse & Prepare Request State */
	protected int ajpThreadStatusP = 0;
	/** AJP Threads in Service State */
	protected int ajpThreadStatusS = 0;
	/** AJP Threads in Finishing State */
	protected int ajpThreadStatusF = 0;
	/** AJP Threads in Ready State */
	protected int ajpThreadStatusR = 0;
	/** AJP Threads in Keep Alive State */
	protected int ajpThreadStatusK = 0;
	/** The root tracer segment */
	protected String rootSegment = null;
	/**	An instance of an XPath */
	protected XPath xpath = null;	
	/**	The XPath expression that extracts the Tomcat connectors from the status XML document */
	protected XPathExpression xpathExpression = null;
	/** Indicated if the startup notification from the tomcat web server has been received */
	protected boolean isNotified = false;
	/** The ObjectName of the WebServer */
	protected ObjectName webServerObjectName = null;
	/** The last XML Document Parsed */
	protected String lastXMLDoc = null;
	/** Indicates if static config data has been read for HTTP*/
	protected boolean isHttpConfigRead = false;
	/** Indicates if static config data has been read for AJP*/
	protected boolean isAjpConfigRead = false;
	
	
	
	public static final String THREAD_INFO = "threadInfo"; 
	public static final String REQUEST_INFO = "requestInfo";
	public static final String WORKERS = "workers";
	public final String TOMCAT_CONNECTORS_STARTED  = "jboss.tomcat.connectors.started";
		
	
	/**
	 * Instantiates a new TomcatThreadStatusCollector.
	 * @throws XPathExpressionException 
	 * @throws NullPointerException 
	 * @throws MalformedObjectNameException 
	 */
	public TomcatThreadStatusCollector() throws XPathExpressionException, MalformedObjectNameException, NullPointerException {
		super();
		xpath = XPathFactory.newInstance().newXPath();
		xpathExpression = xpath.compile("/status/connector");
		webServerObjectName = new ObjectName("jboss.web:service=WebServer");
	}
	
	/**
	 * Initializes the VERSION and MODULE.
	 * @see com.heliosapm.wiex.server.collectors.BaseCollector#init()
	 */
	public void init() {
		VERSION = "$Revision: 1.3 $";
		MODULE = "TomcatThreadStatusCollector";
	}	
		
	
	/**
	 * @throws Exception
	 * @see com.heliosapm.wiex.server.collectors.BaseCollector#start()
	 */
	@Override
	@JMXOperation(description="Starts The Service", expose=true, name="start")
	public void start() throws Exception {
		super.start();
		rootSegment = tracer.buildSegment(segmentPrefix, false, "Tomcat", "ThreadPools");
		if(isWebServerStarted()) {
			isNotified=true;
		} else {
			// register for notification
			mbeanServer.addNotificationListener(webServerObjectName, this, null, null);
		}
	}
	
	/**
	 * Stops the collector's polling.
	 */
	@Override
	@JMXOperation(description="Stops The Service", expose=true, name="stop")	
	public void stop() {
		super.stop();
		try {
			mbeanServer.removeNotificationListener(webServerObjectName, this);
		} catch (Exception e) {
			
		}
	}			
	
	/**
	 * Handles and inspects any JMX notification recieved.
	 * If the notification indicates the web server has started or stopped, it will set the <code>isNotified</code> boolean accordingly.
	 * @param notification The JMX Notification.
	 * @param handback The object handback.
	 */
	public void handleNotification(Notification notification, Object handback) {
		log.info("Received Notification:" + notification.getType() + "/" + notification.getMessage());
		if(notification.getType().equalsIgnoreCase(TOMCAT_CONNECTORS_STARTED)) isNotified = true;
	}
	
	
	/**
	 * Readss the StateString attribute of the Tomcat Web Server MBean and returns true if it is started.
	 * Returns false for any other outcome.
	 * @return boolean indicating if tomcat is started.
	 */
	protected boolean isWebServerStarted() {
		try {
			String state = (String)mbeanServer.getAttribute(webServerObjectName, "StateString");
			return (state != null && state.equalsIgnoreCase("Started")); 
		} catch (Exception e) {
			return false;
		}
		
	}

	/**
	 * Collects Tomcat Web Server HTTP and AJP Thread Pool Status & Metrics.
	 * @see com.heliosapm.wiex.server.collectors.BaseCollector#collect()
	 */	
	@Override
	@JMXOperation(description="Collects Tomcat Stats", expose=true, name="collect")
	public void collect() {
		if(!isNotified) {
			if(isWebServerStarted()) {
				isNotified=true;
			} else {
				log.info("WebServer not started. No Reading Taken");
				return;
			}
		}
		long start = System.currentTimeMillis();
		InputStream is = null;
		InputSource insource = null;
		boolean isHttp = false;
		try {
			insource = new InputSource(getStream());
			NodeList  nodelist = (NodeList) xpathExpression.evaluate(insource, XPathConstants.NODESET);
			for(int i = 0; i < nodelist.getLength(); i++) {
				String connectorName = nodelist.item(i).getAttributes().getNamedItem("name").getNodeValue();
				if(connectorName.endsWith("" + httpPort)) {
					httpConnectorName = connectorName;
					isHttp = true;
				}
				if(connectorName.endsWith("" + ajpPort)) {
					ajpConnectorName = connectorName;
					isHttp = false;
				}
				NodeList connectorNodes = nodelist.item(i).getChildNodes();
				for(int x = 0; x < connectorNodes.getLength(); x++) {
					
					String nodeType = connectorNodes.item(x).getNodeName();
					if(nodeType.equals(THREAD_INFO)) {
						processThreadInfo(connectorNodes.item(x), isHttp);
						if(isHttp) {
							if(!isHttpConfigRead) processThreadInfoConfig(connectorNodes.item(x), isHttp);
						} else {
							if(!isAjpConfigRead) processThreadInfoConfig(connectorNodes.item(x), isHttp);
						}						
					} else if (nodeType.equals(REQUEST_INFO)) {
						processRequestInfo(connectorNodes.item(x), isHttp);
					} else if (nodeType.equals(WORKERS)) {
						processWorkers(connectorNodes.item(x), isHttp);
					} else {
						// Noop
					}
				}
			}
			collectTime = System.currentTimeMillis()-start;
		} catch (Exception e) {
			if(logErrors) log.warn("Tomcat Collection Failure:" + e);
		} finally {
			try { is.close(); } catch (Exception e) {}
		}
		

	}
	

	
	
	  
	/**
	 * Opens an input stream from the Tomcat XML Status Page.
	 * @return An XML Input Stream.
	 * @throws Exception
	 */
	protected InputStream getStream() throws Exception {
		URL url=new URL( "http", hostName, httpPort, "/status?XML=true");
		URLConnection urlc=url.openConnection();
		InputStream inputstream=urlc.getInputStream();
		ByteArrayOutputStream baos = new  ByteArrayOutputStream();
		byte[] buffer = new byte[8192];
		int bytesRead = 0;
		while(true) {
			bytesRead = inputstream.read(buffer);
			if(bytesRead==-1) break;
			baos.write(buffer, 0, bytesRead);
		}		
		byte[] documentBytes = baos.toByteArray();
		ByteArrayInputStream bis = new ByteArrayInputStream(documentBytes);
		lastXMLDoc = new String(documentBytes);		
		return bis;
	}
	
	/**
	 * Command line tester for the Tomcat Thread Pool Monitoring Service.
	 * Arguments:<ol>
	 * <li>Host Name or IP Address
	 * <li>The HTTP Port
	 * <li>The AJP Port
	 * </ol>
	 * @param args
	 * @throws XPathExpressionException
	 * @throws NullPointerException 
	 * @throws MalformedObjectNameException 
	 */
	public static void main(String[] args) throws XPathExpressionException, MalformedObjectNameException, NullPointerException {
		TomcatThreadStatusCollector collector = new TomcatThreadStatusCollector();
		collector.setAjpPort(Integer.parseInt(args[2]));		
		collector.setHttpPort(Integer.parseInt(args[1]));
		collector.setHostName(args[0]);
		InputStream is = null;
		InputSource insource = null;
		boolean isHttp = false;
		XPath xpath = XPathFactory.newInstance().newXPath();
		XPathExpression xpathExpression = xpath.compile("/status/connector");
		long start = System.currentTimeMillis();
		try {
			insource = new InputSource(collector.getStream());
			NodeList  nodelist = (NodeList) xpathExpression.evaluate(insource, XPathConstants.NODESET);
			log("Located " + nodelist.getLength() + " Connectors");
			for(int i = 0; i < nodelist.getLength(); i++) {
				log("Connector:" + i);
				String connectorName = nodelist.item(i).getAttributes().getNamedItem("name").getNodeValue();
				log("Connector Name:" + connectorName);
				if(connectorName.endsWith("" + collector.httpPort)) {
					collector.httpConnectorName = connectorName;
					isHttp = true;
				}
				if(connectorName.endsWith("" + collector.ajpPort)) {
					collector.ajpConnectorName = connectorName;
					isHttp = false;
				}
				NodeList connectorNodes = nodelist.item(i).getChildNodes();
				for(int x = 0; x < connectorNodes.getLength(); x++) {
					
					String nodeType = connectorNodes.item(x).getNodeName();
					if(nodeType.equals(THREAD_INFO)) {
						collector.processThreadInfo(connectorNodes.item(x), isHttp);
					} else if (nodeType.equals(REQUEST_INFO)) {
						collector.processRequestInfo(connectorNodes.item(x), isHttp);
					} else if (nodeType.equals(WORKERS)) {
						collector.processWorkers(connectorNodes.item(x), isHttp);
					} else {
						// Noop
					}
				}
			}
			collector.collectTime = System.currentTimeMillis()-start;
			log(collector);
			
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try { is.close(); } catch (Exception e) {}
		}
		
	}
	
	/**
	 * Returns the value of the named attribute in the passed node as an int.
	 * @param node The node to look for the attribute in.
	 * @param name The name of the attribute.
	 * @return The int value of the attribute
	 */
	protected int getNodeAttributeInt(Node node, String name) {
		return Integer.parseInt(node.getAttributes().getNamedItem(name).getNodeValue());
	}
	
	/**
	 * Returns the value of the named attribute in the passed node as a long.
	 * @param node The node to look for the attribute in.
	 * @param name The name of the attribute.
	 * @return The long value of the attribute
	 */
	protected  long getNodeAttributeLong(Node node, String name) {
		return Long.parseLong(node.getAttributes().getNamedItem(name).getNodeValue());
	}
	
	/**
	 * Counts the number of nodes in the node list with the named attribute equal to the passed value.
	 * @param nodeList The node list to count from.
	 * @param name The name of the attribute.
	 * @param value The value of the attribute to match.
	 * @return The number of matching nodes.
	 */
	protected int countNodesWithAttributeValue(NodeList nodeList, String name, String value) {
		int cnt = 0;
		for(int i = 0; i < nodeList.getLength(); i++) {
			if(nodeList.item(i).getAttributes().getNamedItem(name).getNodeValue().equals(value)) cnt++;
		}
		return cnt;
	}
	
	
	/**
	 * Processes ThreadInfo for the connector.
	 * @param node The node representing the ThreadInfo element for the current connector.
	 * @param isHttp true if connector is HTTP, false if it is AJP
	 */
	protected void processThreadInfo(Node node, boolean isHttp) {
		try {
			setCurrentThreadCount(getNodeAttributeInt(node, "currentThreadCount"), isHttp);
			setCurrentThreadsBusy(getNodeAttributeInt(node, "currentThreadsBusy"), isHttp);
		} catch (Exception e) {
			if(logErrors) log.error("processRequestInfo Failed:", e);
		}
	}
	
	/**
	 * Processes static thread pool configuration.
	 * Only runs once per collector start.
	 * @param node The node representing the ThreadInfo element for the current connector.
	 * @param isHttp true if connector is HTTP, false if it is AJP
	 */
	protected void processThreadInfoConfig(Node node, boolean isHttp) {
		try {
			setMaxThreads(getNodeAttributeInt(node, "maxThreads"), isHttp);
			setMinSpareThreads(getNodeAttributeInt(node, "minSpareThreads"), isHttp);
			setMaxSpareThreads(getNodeAttributeInt(node, "maxSpareThreads"), isHttp);
			if(isHttp) isHttpConfigRead=true;
			else isAjpConfigRead=true;			
		} catch (Exception e) {
			if(logErrors) log.error("processThreadInfoConfig Failed:", e);
		}
	}
	
	
	
	/**
	 * Processes RequestInfo for the connector
	 * @param node The node representing the RequestInfo element for the current connector.
	 * @param isHttp true if connector is HTTP, false if it is AJP
	 */
	protected void processRequestInfo(Node node, boolean isHttp) {
		try {
			if(log.isDebugEnabled())log.debug("Processing Request Info. HTTP?:" + isHttp);
			long tmp = 0L;
			long dtmp = 0L;
			setMaxTime(getNodeAttributeLong(node, "maxTime"), isHttp);
			tmp = getNodeAttributeLong(node, "processingTime");
			dtmp = tmp - ((isHttp) ? getHttpProcessingTime() : getAjpProcessingTime());
			setProcessingTime(tmp, isHttp);
			setProcessingTimeDelta(dtmp, isHttp);
			
			tmp = getNodeAttributeLong(node, "requestCount");
			dtmp = tmp - ((isHttp) ? getHttpRequestCount() : getAjpRequestCount());
			setRequestCount(tmp, isHttp);
			setRequestCountDelta(dtmp, isHttp);
			
			tmp = getNodeAttributeLong(node, "errorCount");
			dtmp = tmp - ((isHttp) ? getHttpErrorCount() : getAjpErrorCount());
			setErrorCount(tmp, isHttp);
			setErrorCountDelta(dtmp, isHttp);
			
			tmp = getNodeAttributeLong(node, "bytesReceived");
			dtmp = tmp - ((isHttp) ? getHttpBytesReceived() : getAjpBytesReceived());
			setBytesReceived(tmp, isHttp);
			setBytesReceivedDelta(dtmp, isHttp);
			
			tmp = getNodeAttributeLong(node, "bytesSent");
			dtmp = tmp - ((isHttp) ? getHttpBytesSent() : getAjpBytesSent());
			setBytesSent(tmp, isHttp);
			setBytesSentDelta(dtmp, isHttp);
			
			try {
				tmp = ((isHttp) ? getHttpProcessingTime() : getAjpProcessingTime()) / ((isHttp) ? getHttpRequestCount() : getAjpRequestCount());
				setAverageRequestTime(tmp, isHttp);
			} catch (ArithmeticException ae) {
				setAverageRequestTime(0, isHttp);
			}
			
//			try {
//				tmp = ((isHttp) ? getHttpProcessingTimeDelta() : getAjpProcessingTimeDelta()) / ((isHttp) ? getHttpRequestCountDelta() : getAjpRequestCountDelta());
//				setAverageRequestTimeDelta(tmp, isHttp);
//			} catch (ArithmeticException ae) {
//				setAverageRequestTimeDelta(0, isHttp);
//			}
			
		} catch (Exception e) {
			if(logErrors) log.error("processRequestInfo Failed:", e);
			if(logErrors) log("processRequestInfo Error:" + e);
		}
	}
	
	/**
	 * Counts the number of threads in the pool in each state.
	 * @param node The workers node.
	 * @param isHttp true if connector is HTTP, false if it is AJP
	 */
	protected void processWorkers(Node node, boolean isHttp) {
		try {
			if(log.isDebugEnabled())log.debug("Processing Workers. HTTP?:" + isHttp);
			NodeList nodeList = node.getChildNodes();
			setThreadStatusP(countNodesWithAttributeValue(nodeList, "stage", "P"), isHttp);
			setThreadStatusS(countNodesWithAttributeValue(nodeList, "stage", "S"), isHttp);
			setThreadStatusF(countNodesWithAttributeValue(nodeList, "stage", "F"), isHttp);
			setThreadStatusR(countNodesWithAttributeValue(nodeList, "stage", "R"), isHttp);
			setThreadStatusK(countNodesWithAttributeValue(nodeList, "stage", "K"), isHttp);
		} catch (Exception e) {
			if(logErrors) log.error("processWorkers Failed:", e);
			if(logErrors) log("processWorkers Error:" + e);
			
		}
	}	
	
	
	public static void log(Object message) {
		System.out.println(message);
	}
	

	/**
	 * The Tomcat AJP Port
	 * @return the ajpPort
	 */
	@JMXAttribute(description="The Tomcat AJP Port.", name="AJPPort")
	public int getAjpPort() {
		return ajpPort;
	}

	/**
	 * Sets the Tomcat AJP Port.
	 * @param ajpPort the ajpPort to set
	 */
	public void setAjpPort(int ajpPort) {
		this.ajpPort = ajpPort;
	}

	/**
	 * The Tomcat Host Name.
	 * @return the hostName
	 */
	@JMXAttribute(description="The Tomcat Host Name.", name="TomcatHost")
	public String getHostName() {
		return hostName;
	}

	/**
	 * Sets the Tomcat Host Name.
	 * @param hostName the hostName to set
	 */
	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

	/**
	 * The Tomcat HTTP Port.
	 * @return the httpPort
	 */
	@JMXAttribute(description="The Tomcat HTTP Port.", name="HTTPPort")
	public int getHttpPort() {
		return httpPort;
	}

	/**
	 * Sets the Tomcat HTTP Port.
	 * @param httpPort the httpPort to set
	 */
	public void setHttpPort(int httpPort) {
		this.httpPort = httpPort;
	}

	/**
	 * The AJP Connector Name.
	 * @return the ajpConnectorName
	 */
	@JMXAttribute(description="The AJP Connector Name.", name="AJPConnectorName")
	public String getAjpConnectorName() {
		return ajpConnectorName;
	}

	/**
	 * The HTTP Connector Name.
	 * @return the httpConnectorName
	 */
	@JMXAttribute(description="The HTTP Connector Name.", name="HTTPConnectorName")
	public String getHttpConnectorName() {
		return httpConnectorName;
	}

	    /**
	 * The average request time of AJP Requests.
	 * @return the ajpAverageRequestTime
	 */
	@JMXAttribute(description="The average request time of AJP Requests.", name="AJP Average Request Time")
	public long getAjpAverageRequestTime() {
		return ajpAverageRequestTime;
	}

	/**
	 * The average request time of AJP Requests in the last interval.
	 * @return the ajpAverageRequestTimeDelta
	 */
	@JMXAttribute(description="The average request time of AJP Requests in the last interval.", name="AJP Average Request Time Delta")
	public long getAjpAverageRequestTimeDelta() {
		return ajpAverageRequestTimeDelta;
	}

	/**
	 * The total number of bytes received by AJP Requests.
	 * @return the ajpBytesReceived
	 */
	@JMXAttribute(description="The total number of bytes received by AJP Requests.", name="AJP Total Bytes Received")
	public long getAjpBytesReceived() {
		return ajpBytesReceived;
	}

	/**
	 * The total number of bytes received by AJP Requests in the last interval.
	 * @return the ajpBytesReceivedDelta
	 */
	@JMXAttribute(description="The total number of bytes received by AJP Requests in the last interval.", name="AJP Total Bytes Received Delta")
	public long getAjpBytesReceivedDelta() {
		return ajpBytesReceivedDelta;
	}

	/**
	 * The total number of bytes sent by AJP Threads.
	 * @return the ajpBytesSent
	 */
	@JMXAttribute(description="The total number of bytes sent by AJP Threads.", name="AJP Total Bytes Sent")
	public long getAjpBytesSent() {
		return ajpBytesSent;
	}

	/**
	 * The total number of bytes sent by AJP Threads in the last interval.
	 * @return the ajpBytesSentDelta
	 */
	@JMXAttribute(description="The total number of bytes sent by AJP Threads in the last interval.", name="AJP Total Bytes Sent Delta")
	public long getAjpBytesSentDelta() {
		return ajpBytesSentDelta;
	}

	/**
	 * The current number of AJP Threads.
	 * @return the ajpCurrentThreadCount
	 */
	@JMXAttribute(description="The current number of AJP Threads.", name="AJP Thread Count")
	public int getAjpCurrentThreadCount() {
		return ajpCurrentThreadCount;
	}

	/**
	 * The current number of busy AJP Threads.
	 * @return the ajpCurrentThreadsBusy
	 */
	@JMXAttribute(description="The current number of busy AJP Threads.", name="AJP Busy Thread Count")
	public int getAjpCurrentThreadsBusy() {
		return ajpCurrentThreadsBusy;
	}

	/**
	 * The total number of AJP Errors.
	 * @return the ajpErrorCount
	 */
	@JMXAttribute(description="The total number of AJP Errors.", name="AJP Error Count")
	public long getAjpErrorCount() {
		return ajpErrorCount;
	}

	/**
	 * The total number of AJP Errors in the last interval.
	 * @return the ajpErrorCountDelta
	 */
	@JMXAttribute(description="The total number of AJP Errors in the last interval.", name="AJP Error Count Delta")
	public long getAjpErrorCountDelta() {
		return ajpErrorCountDelta;
	}

	/**
	 * The configured maximum number of spare AJP threads.
	 * @return the ajpMaxSpareThreads
	 */
	@JMXAttribute(description="The configured maximum number of spare AJP threads.", name="AJP Max Spare Threads")
	public int getAjpMaxSpareThreads() {
		return ajpMaxSpareThreads;
	}

	/**
	 * The configured maximum number of AJP threads.
	 * @return the ajpMaxThreads
	 */
	@JMXAttribute(description="The configured maximum number of AJP threads.", name="AJP Max Threads")
	public int getAjpMaxThreads() {
		return ajpMaxThreads;
	}

	/**
	 * The maximum recorded elapsed time for an AJP request.
	 * @return the ajpMaxTime
	 */
	@JMXAttribute(description="The maximum recorded elapsed time for an AJP request.", name="AJP Max Time")
	public long getAjpMaxTime() {
		return ajpMaxTime;
	}

	/**
	 * The configured minimum number of spare AJP threads.
	 * @return the ajpMinSpareThreads
	 */
	@JMXAttribute(description="The configured minimum number of spare AJP threads.", name="AJP Min Spare Threads")
	public int getAjpMinSpareThreads() {
		return ajpMinSpareThreads;
	}

	/**
	 * The total AJP request processing time.
	 * @return the ajpProcessingTime
	 */
	@JMXAttribute(description="The total AJP request processing time.", name="AJP Processing Time")
	public long getAjpProcessingTime() {
		return ajpProcessingTime;
	}

	/**
	 * The total AJP request processing time in the last interval.
	 * @return the ajpProcessingTimeDelta
	 */
	@JMXAttribute(description="The total AJP request processing time in the last interval.", name="AJP Processing Time Delta")
	public long getAjpProcessingTimeDelta() {
		return ajpProcessingTimeDelta;
	}

	/**
	 * The total number of AJP requests.
	 * @return the ajpRequestCount
	 */
	@JMXAttribute(description="The total number of AJP requests.", name="AJP Request Count")
	public long getAjpRequestCount() {
		return ajpRequestCount;
	}

	/**
	 * The total number of AJP requests in the last interval.
	 * @return the ajpRequestCountDelta
	 */
	@JMXAttribute(description="The total number of AJP requests in the last interval.", name="AJP Request Count Delta")
	public long getAjpRequestCountDelta() {
		return ajpRequestCountDelta;
	}

	/**
	 * The number of AJP threads in <code>Finishing</code> state.
	 * @return the ajpThreadStatusF
	 */
	@JMXAttribute(description="The number of AJP threads in Finishing state.", name="AJP Threads F")
	public int getAjpThreadStatusF() {
		return ajpThreadStatusF;
	}

	/**
	 * The number of AJP threads in <code>Keep Alive</code> state.
	 * @return the ajpThreadStatusK
	 */
	@JMXAttribute(description="The number of AJP threads in Keep Alive state.", name="AJP Threads K")
	public int getAjpThreadStatusK() {
		return ajpThreadStatusK;
	}

	/**
	 * The number of AJP threads in <code>Preparing and Parsing</code> state.
	 * @return the ajpThreadStatusP
	 */
	@JMXAttribute(description="The number of AJP threads in Preparing and Parsing state.", name="AJP Threads P")
	public int getAjpThreadStatusP() {
		return ajpThreadStatusP;
	}

	/**
	 * The number of AJP threads in <code>Ready</code> state.
	 * @return the ajpThreadStatusR
	 */
	@JMXAttribute(description="The number of AJP threads in Ready state.", name="AJP Threads R")
	public int getAjpThreadStatusR() {
		return ajpThreadStatusR;
	}

	/**
	 * The number of AJP threads in <code>Service</code> state.
	 * @return the ajpThreadStatusS
	 */
	@JMXAttribute(description="The number of AJP threads in Service state.", name="AJP Threads S")
	public int getAjpThreadStatusS() {
		return ajpThreadStatusS;
	}

	/**
	 * The average request time of HTTP Requests.
	 * @return the httpAverageRequestTime
	 */
	@JMXAttribute(description="The average request time of HTTP Requests.", name="HTTP Average Request Time")
	public long getHttpAverageRequestTime() {
		return httpAverageRequestTime;
	}


	/**
	 * The total number of bytes received by HTTP Requests.
	 * @return the httpBytesReceived
	 */
	@JMXAttribute(description="The total number of bytes received by HTTP Requests.", name="HTTP Total Bytes Received")
	public long getHttpBytesReceived() {
		return httpBytesReceived;
	}


	/**
	 * The total number of bytes sent by HTTP Threads.
	 * @return the httpBytesSent
	 */
	@JMXAttribute(description="The total number of bytes sent by HTTP Threads.", name="HTTP Total Bytes Sent")
	public long getHttpBytesSent() {
		return httpBytesSent;
	}


	/**
	 * The current number of HTTP Threads.
	 * @return the httpCurrentThreadCount
	 */
	@JMXAttribute(description="The current number of HTTP Threads.", name="HTTP Thread Count")
	public int getHttpCurrentThreadCount() {
		return httpCurrentThreadCount;
	}

	/**
	 * The current number of busy HTTP Threads.
	 * @return the httpCurrentThreadsBusy
	 */
	@JMXAttribute(description="The current number of busy HTTP Threads.", name="HTTP Busy Thread Count")
	public int getHttpCurrentThreadsBusy() {
		return httpCurrentThreadsBusy;
	}

	/**
	 * The total number of HTTP Errors.
	 * @return the httpErrorCount
	 */
	@JMXAttribute(description="The total number of HTTP Errors.", name="HTTP Error Count")
	public long getHttpErrorCount() {
		return httpErrorCount;
	}


	/**
	 * The configured maximum number of spare HTTP threads.
	 * @return the httpMaxSpareThreads
	 */
	@JMXAttribute(description="The configured maximum number of spare HTTP threads.", name="HTTP Max Spare Threads")
	public int getHttpMaxSpareThreads() {
		return httpMaxSpareThreads;
	}

	/**
	 * The configured maximum number of HTTP threads.
	 * @return the httpMaxThreads
	 */
	@JMXAttribute(description="The configured maximum number of HTTP threads.", name="HTTP Max Threads")
	public int getHttpMaxThreads() {
		return httpMaxThreads;
	}

	/**
	 * The maximum recorded elapsed time for an HTTP request.
	 * @return the httpMaxTime
	 */
	@JMXAttribute(description="The maximum recorded elapsed time for an HTTP request.", name="HTTP Max Time")
	public long getHttpMaxTime() {
		return httpMaxTime;
	}

	/**
	 * The configured minimum number of spare HTTP threads.
	 * @return the httpMinSpareThreads
	 */
	@JMXAttribute(description="The configured minimum number of spare HTTP threads.", name="HTTP Min Spare Threads")
	public int getHttpMinSpareThreads() {
		return httpMinSpareThreads;
	}

	/**
	 * The total HTTP request processing time.
	 * @return the httpProcessingTime
	 */
	@JMXAttribute(description="The total HTTP request processing time.", name="HTTP Processing Time")
	public long getHttpProcessingTime() {
		return httpProcessingTime;
	}


	/**
	 * The total number of HTTP requests.
	 * @return the httpRequestCount
	 */
	@JMXAttribute(description="The total number of HTTP requests.", name="HTTP Request Count")
	public long getHttpRequestCount() {
		return httpRequestCount;
	}

	/**
	 * The total number of HTTP requests in the last interval.
	 * @return the httpRequestCountDelta
	 */
	@JMXAttribute(description="The total number of HTTP requests in the last interval.", name="HTTP Request Count Delta")
	public long getHttpRequestCountDelta() {
		return httpRequestCountDelta;
	}

	/**
	 * The number of HTTP threads in <code>Finishing</code> state.
	 * @return the httpThreadStatusF
	 */
	@JMXAttribute(description="The number of HTTP threads in Finishing state.", name="HTTP Threads F")
	public int getHttpThreadStatusF() {
		return httpThreadStatusF;
	}

	/**
	 * The number of HTTP threads in <code>Keep Alive</code> state.
	 * @return the httpThreadStatusK
	 */
	@JMXAttribute(description="The number of HTTP threads in Keep Alive state.", name="HTTP Threads K")
	public int getHttpThreadStatusK() {
		return httpThreadStatusK;
	}

	/**
	 * The number of HTTP threads in <code>Preparing and Parsing</code> state.
	 * @return the httpThreadStatusP
	 */
	@JMXAttribute(description="The number of HTTP threads in Preparing and Parsing state.", name="HTTP Threads P")
	public int getHttpThreadStatusP() {
		return httpThreadStatusP;
	}

	/**
	 * The number of HTTP threads in <code>Ready</code> state.
	 * @return the httpThreadStatusR
	 */
	@JMXAttribute(description="The number of HTTP threads in Ready state.", name="HTTP Threads R")
	public int getHttpThreadStatusR() {
		return httpThreadStatusR;
	}

	/**
	 * The number of HTTP threads in <code>Service</code> state.
	 * @return the httpThreadStatusS
	 */
	@JMXAttribute(description="The number of HTTP threads in Service state.", name="HTTP Threads S")
	public int getHttpThreadStatusS() {
		return httpThreadStatusS;
	}

	/**
	 * @param averageRequestTime the AverageRequestTime to set
	 * @param http true if the thread type is HTTP, false if it is AJP
	 */
	public void setAverageRequestTime(long averageRequestTime ,boolean http) {
		if(http) httpAverageRequestTime = averageRequestTime;
		else ajpAverageRequestTime = averageRequestTime;
	}

	/**
	 * @param averageRequestTimeDelta the AverageRequestTimeDelta to set
	 * @param http true if the thread type is HTTP, false if it is AJP
	 */
	public void setAverageRequestTimeDelta(long averageRequestTimeDelta ,boolean http) {
//		if(http) httpAverageRequestTimeDelta = averageRequestTimeDelta;
//		else ajpAverageRequestTimeDelta = averageRequestTimeDelta;
		tracer.recordCounterMetric(tracer.buildSegment(rootSegment, false, (http ? "HTTP" : "AJP")), "Average Request Time", averageRequestTimeDelta);
	}

	/**
	 * @param bytesReceived the BytesReceived to set
	 * @param http true if the thread type is HTTP, false if it is AJP
	 */
	public void setBytesReceived(long bytesReceived ,boolean http) {
		if(http) httpBytesReceived = bytesReceived;
		else ajpBytesReceived = bytesReceived;
	}

	/**
	 * @param bytesReceivedDelta the BytesReceivedDelta to set
	 * @param http true if the thread type is HTTP, false if it is AJP
	 */
	public void setBytesReceivedDelta(long bytesReceivedDelta ,boolean http) {
//		if(http) httpBytesReceivedDelta = bytesReceivedDelta;
//		else ajpBytesReceivedDelta = bytesReceivedDelta;
		tracer.recordCounterMetric(tracer.buildSegment(rootSegment, false, (http ? "HTTP" : "AJP")), "Bytes Received", bytesReceivedDelta);
	}

	/**
	 * @param bytesSent the BytesSent to set
	 * @param http true if the thread type is HTTP, false if it is AJP
	 */
	public void setBytesSent(long bytesSent ,boolean http) {
		if(http) httpBytesSent = bytesSent;
		else ajpBytesSent = bytesSent;
	}

	/**
	 * @param bytesSentDelta the BytesSentDelta to set
	 * @param http true if the thread type is HTTP, false if it is AJP
	 */
	public void setBytesSentDelta(long bytesSentDelta ,boolean http) {
//		if(http) httpBytesSentDelta = bytesSentDelta;
//		else ajpBytesSentDelta = bytesSentDelta;
		tracer.recordCounterMetric(tracer.buildSegment(rootSegment, false, (http ? "HTTP" : "AJP")), "Bytes Sent", bytesSentDelta);
	}


	/**
	 * @param currentThreadCount the CurrentThreadCount to set
	 * @param http true if the thread type is HTTP, false if it is AJP
	 */
	public void setCurrentThreadCount(int currentThreadCount ,boolean http) {
		if(http) httpCurrentThreadCount = currentThreadCount;
		else ajpCurrentThreadCount = currentThreadCount;
		tracer.recordCounterMetric(tracer.buildSegment(rootSegment, false, (http ? "HTTP" : "AJP")), "Thread Count", currentThreadCount);
	}

	/**
	 * @param currentThreadsBusy the CurrentThreadsBusy to set
	 * @param http true if the thread type is HTTP, false if it is AJP
	 */
	public void setCurrentThreadsBusy(int currentThreadsBusy ,boolean http) {
		if(http) httpCurrentThreadsBusy = currentThreadsBusy;
		else ajpCurrentThreadsBusy = currentThreadsBusy;
		tracer.recordCounterMetric(tracer.buildSegment(rootSegment, false, (http ? "HTTP" : "AJP")), "Busy Thread Count", currentThreadsBusy);
	}

	/**
	 * @param errorCount the ErrorCount to set
	 * @param http true if the thread type is HTTP, false if it is AJP
	 */
	public void setErrorCount(long errorCount ,boolean http) {
		if(http) httpErrorCount = errorCount;
		else ajpErrorCount = errorCount;
	}

	/**
	 * @param errorCountDelta the ErrorCountDelta to set
	 * @param http true if the thread type is HTTP, false if it is AJP
	 */
	public void setErrorCountDelta(long errorCountDelta ,boolean http) {
//		if(http) httpErrorCountDelta = errorCountDelta;
//		else ajpErrorCountDelta = errorCountDelta;
		tracer.recordCounterMetric(tracer.buildSegment(rootSegment, false, (http ? "HTTP" : "AJP")), "Error Count", errorCountDelta);
	}

	/**
	 * @param maxSpareThreads the MaxSpareThreads to set
	 * @param http true if the thread type is HTTP, false if it is AJP
	 */
	public void setMaxSpareThreads(int maxSpareThreads ,boolean http) {
		if(http) httpMaxSpareThreads = maxSpareThreads;
		else ajpMaxSpareThreads = maxSpareThreads;
		tracer.recordCounterMetric(tracer.buildSegment(rootSegment, false, (http ? "HTTP" : "AJP"), "Config"), "Max Spare Threads", maxSpareThreads);
	}

	/**
	 * @param maxThreads the MaxThreads to set
	 * @param http true if the thread type is HTTP, false if it is AJP
	 */
	public void setMaxThreads(int maxThreads ,boolean http) {
		if(http) httpMaxThreads = maxThreads;
		else ajpMaxThreads = maxThreads;
		tracer.recordCounterMetric(tracer.buildSegment(rootSegment, false, (http ? "HTTP" : "AJP"), "Config"), "Max Threads", maxThreads);
	}

	/**
	 * @param maxTime the MaxTime to set
	 * @param http true if the thread type is HTTP, false if it is AJP
	 */
	public void setMaxTime(long maxTime ,boolean http) {
		if(http) httpMaxTime = maxTime;
		else ajpMaxTime = maxTime;
		tracer.recordCounterMetric(tracer.buildSegment(rootSegment, false, (http ? "HTTP" : "AJP")), "Max Request Time", maxTime);
	}

	/**
	 * @param minSpareThreads the MinSpareThreads to set
	 * @param http true if the thread type is HTTP, false if it is AJP
	 */
	public void setMinSpareThreads(int minSpareThreads ,boolean http) {
		if(http) httpMinSpareThreads = minSpareThreads;
		else ajpMinSpareThreads = minSpareThreads;
		tracer.recordCounterMetric(tracer.buildSegment(rootSegment, false, (http ? "HTTP" : "AJP"), "Config"), "Min Spare Threads", minSpareThreads);
	}

	/**
	 * @param processingTime the ProcessingTime to set
	 * @param http true if the thread type is HTTP, false if it is AJP
	 */
	public void setProcessingTime(long processingTime ,boolean http) {
		if(http) httpProcessingTime = processingTime;
		else ajpProcessingTime = processingTime;
	}

	/**
	 * @param processingTimeDelta the ProcessingTimeDelta to set
	 * @param http true if the thread type is HTTP, false if it is AJP
	 */
	public void setProcessingTimeDelta(long processingTimeDelta ,boolean http) {
//		if(http) httpProcessingTimeDelta = processingTimeDelta;
//		else ajpProcessingTimeDelta = processingTimeDelta;
		tracer.recordCounterMetric(tracer.buildSegment(rootSegment, false, (http ? "HTTP" : "AJP")), "Total Processing Time", processingTimeDelta);
	}

	/**
	 * @param requestCount the RequestCount to set
	 * @param http true if the thread type is HTTP, false if it is AJP
	 */
	public void setRequestCount(long requestCount ,boolean http) {
		if(http) httpRequestCount = requestCount;
		else ajpRequestCount = requestCount;
	}

	/**
	 * @param requestCountDelta the RequestCountDelta to set
	 * @param http true if the thread type is HTTP, false if it is AJP
	 */
	public void setRequestCountDelta(long requestCountDelta ,boolean http) {
		if(http) httpRequestCountDelta = requestCountDelta;
		else ajpRequestCountDelta = requestCountDelta;
		tracer.recordCounterMetric(tracer.buildSegment(rootSegment, false, (http ? "HTTP" : "AJP")), "Total Request Count", requestCountDelta);
	}

	/**
	 * @param threadStatusF the ThreadStatusF to set
	 * @param http true if the thread type is HTTP, false if it is AJP
	 */
	public void setThreadStatusF(int threadStatusF ,boolean http) {
		if(http) httpThreadStatusF = threadStatusF;
		else ajpThreadStatusF = threadStatusF;
		tracer.recordCounterMetric(tracer.buildSegment(rootSegment, false, "Thread State", (http ? "HTTP" : "AJP")), "Finish", threadStatusF);
	}

	/**
	 * @param threadStatusK the ThreadStatusK to set
	 * @param http true if the thread type is HTTP, false if it is AJP
	 */
	public void setThreadStatusK(int threadStatusK ,boolean http) {
		if(http) httpThreadStatusK = threadStatusK;
		else ajpThreadStatusK = threadStatusK;
		tracer.recordCounterMetric(tracer.buildSegment(rootSegment, false, "Thread State", (http ? "HTTP" : "AJP")), "Keep Alive", threadStatusK);
	}

	/**
	 * @param threadStatusP the ThreadStatusP to set
	 * @param http true if the thread type is HTTP, false if it is AJP
	 */
	public void setThreadStatusP(int threadStatusP ,boolean http) {
		if(http) httpThreadStatusP = threadStatusP;
		else ajpThreadStatusP = threadStatusP;
		tracer.recordCounterMetric(tracer.buildSegment(rootSegment, false, "Thread State", (http ? "HTTP" : "AJP")), "Prepare and Parse", threadStatusP);
	}

	/**
	 * @param threadStatusR the ThreadStatusR to set
	 * @param http true if the thread type is HTTP, false if it is AJP
	 */
	public void setThreadStatusR(int threadStatusR ,boolean http) {
		if(http) httpThreadStatusR = threadStatusR;
		else ajpThreadStatusR = threadStatusR;
		tracer.recordCounterMetric(tracer.buildSegment(rootSegment, false, "Thread State", (http ? "HTTP" : "AJP")), "Receive", threadStatusR);
	}

	/**
	 * @param threadStatusS the ThreadStatusS to set
	 * @param http true if the thread type is HTTP, false if it is AJP
	 */
	public void setThreadStatusS(int threadStatusS ,boolean http) {
		if(http) httpThreadStatusS = threadStatusS;
		else ajpThreadStatusS = threadStatusS;
		tracer.recordCounterMetric(tracer.buildSegment(rootSegment, false, "Thread State", (http ? "HTTP" : "AJP")), "Service", threadStatusS);
	}

	   /**
		 * Renders a readable representation of the current service.
		 * @return A readable string.
		 */
	@JMXOperation(description="Renders the state of the service in a readable string", expose=true, name="toString")
		public String toString() {
			StringBuffer buffer = new StringBuffer();
			buffer.append("TomcatThreadStatusCollector[");
			buffer.append("\nHostName = ").append(hostName);
			buffer.append("\nAJP Pool:");
			buffer.append("\nPort:").append(ajpPort);
			buffer.append("\nConnector Name:").append(ajpConnectorName);
			buffer.append("\n\tajpAverageRequestTime = ").append(ajpAverageRequestTime);
			buffer.append("\n\tajpAverageRequestTimeDelta = ").append(ajpAverageRequestTimeDelta);
			buffer.append("\n\tajpBytesReceived = ").append(ajpBytesReceived);
			buffer.append("\n\tajpBytesReceivedDelta = ").append(ajpBytesReceivedDelta);
			buffer.append("\n\tajpBytesSent = ").append(ajpBytesSent);
			buffer.append("\n\tajpBytesSentDelta = ").append(ajpBytesSentDelta);
			buffer.append("\n\tajpCurrentThreadCount = ").append(ajpCurrentThreadCount);
			buffer.append("\n\tajpCurrentThreadsBusy = ").append(ajpCurrentThreadsBusy);
			buffer.append("\n\tajpErrorCount = ").append(ajpErrorCount);
			buffer.append("\n\tajpErrorCountDelta = ").append(ajpErrorCountDelta);
			buffer.append("\n\tajpMaxSpareThreads = ").append(ajpMaxSpareThreads);
			buffer.append("\n\tajpMaxThreads = ").append(ajpMaxThreads);
			buffer.append("\n\tajpMaxTime = ").append(ajpMaxTime);
			buffer.append("\n\tajpMinSpareThreads = ").append(ajpMinSpareThreads);			
			buffer.append("\n\tajpProcessingTime = ").append(ajpProcessingTime);
			buffer.append("\n\tajpProcessingTimeDelta = ").append(ajpProcessingTimeDelta);
			buffer.append("\n\tajpRequestCount = ").append(ajpRequestCount);
			buffer.append("\n\tajpRequestCountDelta = ").append(ajpRequestCountDelta);
			buffer.append("\n\tajpThreadStatusF = ").append(ajpThreadStatusF);
			buffer.append("\n\tajpThreadStatusK = ").append(ajpThreadStatusK);
			buffer.append("\n\tajpThreadStatusP = ").append(ajpThreadStatusP);
			buffer.append("\n\tajpThreadStatusR = ").append(ajpThreadStatusR);
			buffer.append("\n\tajpThreadStatusS = ").append(ajpThreadStatusS);
			buffer.append("\nHTTP Pool:");
			buffer.append("\nPort:").append(httpPort);
			buffer.append("\nConnector Name:").append(httpConnectorName);
			buffer.append("\n\thttpAverageRequestTime = ").append(httpAverageRequestTime);
//			buffer.append("\n\thttpAverageRequestTimeDelta = ").append(httpAverageRequestTimeDelta);
			buffer.append("\n\thttpBytesReceived = ").append(httpBytesReceived);
//			buffer.append("\n\thttpBytesReceivedDelta = ").append(httpBytesReceivedDelta);
			buffer.append("\n\thttpBytesSent = ").append(httpBytesSent);
//			buffer.append("\n\thttpBytesSentDelta = ").append(httpBytesSentDelta);
			buffer.append("\n\thttpCurrentThreadCount = ").append(httpCurrentThreadCount);
			buffer.append("\n\thttpCurrentThreadsBusy = ").append(httpCurrentThreadsBusy);
			buffer.append("\n\thttpErrorCount = ").append(httpErrorCount);
//			buffer.append("\n\thttpErrorCountDelta = ").append(httpErrorCountDelta);
			buffer.append("\n\thttpMaxSpareThreads = ").append(httpMaxSpareThreads);
			buffer.append("\n\thttpMaxThreads = ").append(httpMaxThreads);
			buffer.append("\n\thttpMaxTime = ").append(httpMaxTime);
			buffer.append("\n\thttpMinSpareThreads = ").append(httpMinSpareThreads);
			buffer.append("\n\thttpProcessingTime = ").append(httpProcessingTime);
//			buffer.append("\n\thttpProcessingTimeDelta = ").append(httpProcessingTimeDelta);
			buffer.append("\n\thttpRequestCount = ").append(httpRequestCount);
			buffer.append("\n\thttpRequestCountDelta = ").append(httpRequestCountDelta);
			buffer.append("\n\thttpThreadStatusF = ").append(httpThreadStatusF);
			buffer.append("\n\thttpThreadStatusK = ").append(httpThreadStatusK);
			buffer.append("\n\thttpThreadStatusP = ").append(httpThreadStatusP);
			buffer.append("\n\thttpThreadStatusR = ").append(httpThreadStatusR);
			buffer.append("\n\thttpThreadStatusS = ").append(httpThreadStatusS);
			buffer.append("\nCollect Time:").append(collectTime);
			buffer.append("\n]");
			return buffer.toString();
		}

	/**
	 * Indicates if the startup notification has been received from the web server.
	 * @return the isNotified
	 */
	@JMXAttribute(description="Indicates if the startup notification has been received from the web server.", name="Tomcat Startup Notification")
	public boolean isNotified() {
		return isNotified;
	}

	/**
	 * Overrides if the startup notification has been received from the web server.
	 * @param isNotified the isNotified to set
	 */
	public void setNotified(boolean isNotified) {
		this.isNotified = isNotified;
	}

	/**
	 * The JMX ObjectName of the Tomcat Web Server MBean.
	 * @return the webServerObjectName
	 */
	@JMXAttribute(description="The JMX ObjectName of the Tomcat Web Server MBean.", name="TomcatMBean")
	public ObjectName getWebServerObjectName() {
		return webServerObjectName;
	}

	/**
	 * Sets the JMX ObjectName of the Tomcat Web Server MBean.
	 * @param webServerObjectName the webServerObjectName to set
	 */
	public void setWebServerObjectName(ObjectName webServerObjectName) {
		this.webServerObjectName = webServerObjectName;
	}

	/**
	 * The last XML Document Parsed.
	 * @return the lastXMLDoc
	 */
	@JMXOperation(description="Displays the last XML Document Parsed.", expose=true, name="LastXMLDoc")
	public String displayLastXMLDoc() {
		return lastXMLDoc;
	}


}
