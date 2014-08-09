/**
 * 
 */
package com.heliosapm.wiex.server.tracing.collectors.network;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.heliosapm.wiex.jmx.dynamic.annotation.JMXAttribute;
import com.heliosapm.wiex.jmx.dynamic.annotation.JMXManagedObject;
import com.heliosapm.wiex.jmx.dynamic.annotation.JMXOperation;
import com.heliosapm.wiex.server.tracing.collectors.BaseCollector;

/**
 * <p>Title: URLCheckCollector</p>
 * <p>Description: Checks URL end points and traces availability.</p> 
 * <p>Copyright: Copyright (c) 2007</p>
 * <p>Company: Helios Development Group</p>
 * @author Whitehead
 * @version $Revision: 1.3 $
 */
@JMXManagedObject(annotated=true, declared=false)
public class URLCheckCollector extends BaseCollector {
	
	
	protected List<URLCheck> urlCheckList = new ArrayList<URLCheck>();
	
	static {
		// Set the default HostName verifier 
		javax.net.ssl.HttpsURLConnection.setDefaultHostnameVerifier(new NonValidatingSSLHostNameVerifier());
	}

	/**
	 * Instantiates a new URLCheckCollector.
	 */
	public URLCheckCollector() {
		super();
	}

	/**
	 * Accesses each of the configured end-point URLs and traces the availability.
	 * @see com.heliosapm.wiex.server.collectors.BaseCollector#collect()
	 */
	@Override
	@JMXOperation(description="Collects URL Stats", expose=true, name="collect")
	public void collect() {
		long startTime = System.currentTimeMillis();
		for(URLCheck urlCheck: urlCheckList) {
			long size = 0;
			URL url = urlCheck.getUrl();
			BufferedReader bin = null;
			long start = System.currentTimeMillis();
			Date currentDate = new Date(start);
			int responseCode = -1;
			try {
				HttpURLConnection conn = (HttpURLConnection)url.openConnection();
				conn.setConnectTimeout(urlCheck.getTimeout());
				conn.setReadTimeout(urlCheck.getTimeout());
				conn.connect();
				responseCode = conn.getResponseCode();
				if(responseCode != HttpURLConnection.HTTP_OK) {
					tracer.recordMetric(tracer.buildSegment(segmentPrefix, false, urlCheck.getName()), "Response Code " + responseCode, 1);
					throw new Exception(urlCheck.getName() + " Returned Failure Response Code:" + responseCode);
				}
				bin = new BufferedReader(new InputStreamReader(conn.getInputStream()));
				char[] sizeBuffer = new char[urlCheck.getBufferSize()];
				StringBuilder buffer = new StringBuilder();
				while (true) {
					int bytesRead = bin.read(sizeBuffer);
					if(bytesRead==-1) break;
					buffer.append(sizeBuffer, 0, bytesRead);
					size += bytesRead;
				}
				bin.close();
				Pattern pattern = urlCheck.getContentCheckPattern();
				if(pattern!=null) {
					Matcher m = pattern.matcher(buffer.toString());
					if(!m.find()) {
						tracer.recordMetric(tracer.buildSegment(segmentPrefix, false, urlCheck.getName()), "Content Match Failure", 1);
						throw new Exception("Pattern \"" + urlCheck.getContentCheck() + "\" not located in returned content");
					}
				}
				long end = System.currentTimeMillis() - start;				
				tracer.recordCounterMetric(tracer.buildSegment(segmentPrefix, false, urlCheck.getName()), "Availability", 1);					
				tracer.recordCounterMetric(tracer.buildSegment(segmentPrefix, false, urlCheck.getName()), "Response Time", end);
				urlCheck.setAvailable(true);				
				urlCheck.setLastElapsedTime(end);
				urlCheck.setLastGoodCheckTime(currentDate);
				urlCheck.incrementGoodCheckCount();
			} catch (Exception e) {
				urlCheck.setAvailable(false);
				urlCheck.setLastBadCheckTime(currentDate);
				urlCheck.incrementBadCheckCount();
				tracer.recordCounterMetric(tracer.buildSegment(segmentPrefix, false, urlCheck.getName()), "Availability", 0);
				if(logErrors) {
					log.error("Exception Testing URL[" + url + "]", e);
				}
			} finally {
				try { bin.close(); } catch (Exception e) {}
				urlCheck.setCheckCount(urlCheck.getCheckCount()+1);
				urlCheck.setLastCheckTime(currentDate);
			}
		}
		
		collectTime = System.currentTimeMillis() - startTime;
		if(traceCollectionTime) {
			tracer.recordMetric(segmentPrefix, "URLChecker Collection Time", collectTime); 
		}
	}
	



	/**
	 * Basic init.
	 * @see com.heliosapm.wiex.server.collectors.BaseCollector#init()
	 */
	@Override
	public void init() {
		VERSION = "$Revision: 1.3 $";
		MODULE = "URLCheckCollector";
	}
	
	
	/**
	 * Place holder NoOp.
	 * @return A Null
	 */	
	@JMXAttribute(description="Adds a new URL (Legacy)", expose=true, name="URL")
	public String getURL() {
		return null;
	}
	
	/**
	 * Adds a URL to be checked.
	 * Added for backward compatibility.
	 * @param urlStr
	 */
	public void setURL(String urlStr) {
		try {
			URL url = new URL(urlStr);
			String name = url.getHost() + "-" + url.getPath();
			URLCheck urlCheck = new URLCheck(5000, url, name);
			urlCheckList.add(urlCheck);
		} catch (Exception e) {
			if(logErrors) {
				log.error("Exception adding URL " + urlStr, e);
			}
		}
	}
	
	/**
	 * Place holder NoOp.
	 * @return A Null
	 */
	@JMXAttribute(description="Adds a new URLCheck", expose=true, name="URLChecks")
	public Element getURLChecks() {
		return null;
	}
	
	/**
	 * Creates a new URLCheck.
	 * @param xml
	 */	
	public void setURLChecks(Element xml) {
		String nodeName = null;
		int timeout = 0;
		URL url = null;
		String name = null;
		NodeList nodeList = xml.getChildNodes();
		Node urlCheckNode = null;
		Node contentCheckNode = null;
		NodeList urlCheckNodeChildren = null;
		for(int i = 0; i < nodeList.getLength(); i++) {
			urlCheckNode = nodeList.item(i);
			nodeName = nodeList.item(i).getNodeName();
			if("URLCheck".equalsIgnoreCase(nodeName)) {
				try {
					timeout = Integer.parseInt(nodeList.item(i).getAttributes().getNamedItem("timeout").getNodeValue());
					url = new URL(nodeList.item(i).getAttributes().getNamedItem("url").getNodeValue());
					name = nodeList.item(i).getAttributes().getNamedItem("name").getNodeValue();
					URLCheck urlCheck = new URLCheck(timeout, url, name);
					try {
						int size = Integer.parseInt(nodeList.item(i).getAttributes().getNamedItem("buffersize").getNodeValue());
						urlCheck.setBufferSize(size);
					} catch (Exception e) {
						// uses default
					}
					urlCheckNodeChildren = urlCheckNode.getChildNodes();
					for(int x = 0; x < urlCheckNodeChildren.getLength(); x++) {
						contentCheckNode = urlCheckNodeChildren.item(x);
						nodeName = urlCheckNodeChildren.item(x).getNodeName();
						if("ContentCheck".equals(nodeName)) {
							String contentCheck = contentCheckNode.getChildNodes().item(0).getNodeValue();
							urlCheck.setContentCheck(contentCheck);
						}
					}					
					urlCheckList.add(urlCheck);
				} catch (Exception e) {
					if(logErrors) {
						log.error("Failed to process URLCheck parse", e);
					}
				}				
			}
		}	
	}
	
	
	/**
	 * Generates a tabular report of the registered URLChecks.
	 * @return An HTML String.
	 */
	@JMXOperation(description="Prints a report of registered URL Checks", expose=true, name="PrintURLChecks")
	public String reportURLChecks() {
		StringBuilder buff = new StringBuilder("<table border=\"1\">");
		buff.append("<TR><TH>Name</TH><TH>URL</TH><TH>Timeout</TH><TH>Content Check</TH><TH>Last Check</TH><TH>Last Good Check</TH><TH>Last Bad Check</TH><TH>Last Elapsed</TH><TH>Good Check Count</TH><TH>Bad Check Count</TH><TH>Available</TH><TH>Buffer Size</TH></TR>");
		for(URLCheck urlCheck: urlCheckList) {
			buff.append("<TR>");
			buff.append("<TD>").append(urlCheck.getName()).append("</TD>");
			buff.append("<TD>").append(urlCheck.getUrl()).append("</TD>");
			buff.append("<TD>").append(urlCheck.getTimeout()).append("</TD>");
			buff.append("<TD>").append(urlCheck.getContentCheck()==null ? "" : urlCheck.getContentCheck()).append("</TD>");
			buff.append("<TD>").append(urlCheck.getLastCheckTime()).append("</TD>");
			buff.append("<TD>").append(urlCheck.getLastGoodCheckTime()).append("</TD>");
			buff.append("<TD>").append(urlCheck.getLastBadCheckTime()).append("</TD>");
			buff.append("<TD>").append(urlCheck.getLastElapsedTime()).append("</TD>");
			buff.append("<TD>").append(urlCheck.getGoodCheckCount()).append("</TD>");
			buff.append("<TD>").append(urlCheck.getBadCheckCount()).append("</TD>");
			buff.append("<TD>").append(urlCheck.isAvailable()).append("</TD>");
			buff.append("<TD>").append(urlCheck.getBufferSize()).append("</TD>");
			buff.append("</TR>");
		}
		buff.append("</table>");
		return buff.toString();
	}
	
	


}

class URLCheck {
	protected int timeout = 0;
	protected URL url = null;
	protected String name = null;
	protected Date lastCheckTime = null;
	protected Date lastGoodCheckTime = null;
	protected Date lastBadCheckTime = null;
	protected long lastElapsedTime = 0;
	protected int checkCount = 0;
	protected int goodCheckCount = 0;
	protected int badCheckCount = 0;
	boolean available = false;
	protected int bufferSize = 1024;
	protected String contentCheck = null;
	protected Pattern contentCheckPattern = null;
	/**
	 * @param timeout
	 * @param url
	 * @param name
	 */
	public URLCheck(int timeout, URL url, String name) {
		this.timeout = timeout;
		this.url = url;
		this.name = name;
	}
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @return the timeout
	 */
	public int getTimeout() {
		return timeout;
	}
	/**
	 * @return the url
	 */
	public URL getUrl() {
		return url;
	}
	/**
	 * @return the available
	 */
	public boolean isAvailable() {
		return available;
	}
	/**
	 * @param available the available to set
	 */
	public void setAvailable(boolean available) {
		this.available = available;
	}
	/**
	 * @return the checkCount
	 */
	public int getCheckCount() {
		return checkCount;
	}
	/**
	 * @param checkCount the checkCount to set
	 */
	public void setCheckCount(int checkCount) {
		this.checkCount = checkCount;
	}
	/**
	 * @return the lastCheckTime
	 */
	public Date getLastCheckTime() {
		return lastCheckTime;
	}
	/**
	 * @param lastCheckTime the lastCheckTime to set
	 */
	public void setLastCheckTime(Date lastCheckTime) {
		this.lastCheckTime = lastCheckTime;
	}
	/**
	 * @return the lastElapsedTime
	 */
	public long getLastElapsedTime() {
		return lastElapsedTime;
	}
	/**
	 * @param lastElapsedTime the lastElapsedTime to set
	 */
	public void setLastElapsedTime(long lastElapsedTime) {
		this.lastElapsedTime = lastElapsedTime;
	}
	/**
	 * @return the bufferSize
	 */
	public int getBufferSize() {
		return bufferSize;
	}
	
	/**
	 * @param size
	 */
	public void setBufferSize(int size) {
		bufferSize = size;
	}
	/**
	 * @return the lastBadCheckTime
	 */
	public Date getLastBadCheckTime() {
		return lastBadCheckTime;
	}
	/**
	 * @param lastBadCheckTime the lastBadCheckTime to set
	 */
	public void setLastBadCheckTime(Date lastBadCheckTime) {
		this.lastBadCheckTime = lastBadCheckTime;
	}
	/**
	 * @return the lastGoodCheckTime
	 */
	public Date getLastGoodCheckTime() {
		return lastGoodCheckTime;
	}
	/**
	 * @param lastGoodCheckTime the lastGoodCheckTime to set
	 */
	public void setLastGoodCheckTime(Date lastGoodCheckTime) {
		this.lastGoodCheckTime = lastGoodCheckTime;
	}
	/**
	 * @return the badCheckCount
	 */
	public int getBadCheckCount() {
		return badCheckCount;
	}
	/**
	 * Increments the bad check count.
	 */
	public void incrementBadCheckCount() {
		badCheckCount++;
	}
	/**
	 * @return the goodCheckCount
	 */
	public int getGoodCheckCount() {
		return goodCheckCount;
	}
	/**
	 * Increments the good check count.
	 */
	public void incrementGoodCheckCount() {
		goodCheckCount++;
	}
	/**
	 * @return the contentCheck
	 */
	public String getContentCheck() {
		return contentCheck;
	}
	/**
	 * @param contentCheck the contentCheck to set
	 */
	public void setContentCheck(String contentCheck) {
		this.contentCheck = contentCheck;
		if(contentCheck!=null) {
			contentCheckPattern = Pattern.compile(contentCheck);
		}
	}
	/**
	 * @return
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + bufferSize;
		result = PRIME * result + ((contentCheck == null) ? 0 : contentCheck.hashCode());
		result = PRIME * result + ((name == null) ? 0 : name.hashCode());
		result = PRIME * result + timeout;
		result = PRIME * result + ((url == null) ? 0 : url.hashCode());
		return result;
	}
	/**
	 * @param obj
	 * @return
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final URLCheck other = (URLCheck) obj;
		if (bufferSize != other.bufferSize)
			return false;
		if (contentCheck == null) {
			if (other.contentCheck != null)
				return false;
		} else if (!contentCheck.equals(other.contentCheck))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (timeout != other.timeout)
			return false;
		if (url == null) {
			if (other.url != null)
				return false;
		} else if (!url.equals(other.url))
			return false;
		return true;
	}
	/**
	 * @return the contentCheckPattern
	 */
	public Pattern getContentCheckPattern() {
		return contentCheckPattern;
	}
}


/*
import java.net.*;

URL url = new URL("http://AS15:8080/jmx-console");
URLConnection conn = url.openConnection();
conn.setConnectTimeout(1000);
conn.connect();
BufferedReader bin = new BufferedReader(new InputStreamReader(conn.getInputStream()));
String str;
while ((str = bin.readLine()) != null) {
	println "${str}"
}
bin.close();
*/
