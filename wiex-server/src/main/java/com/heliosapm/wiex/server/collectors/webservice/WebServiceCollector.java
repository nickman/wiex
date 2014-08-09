package com.heliosapm.wiex.server.collectors.webservice;

import java.util.Map.Entry;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.heliosapm.wiex.jmx.dynamic.annotation.JMXAttribute;
import com.heliosapm.wiex.jmx.dynamic.annotation.JMXManagedObject;
import com.heliosapm.wiex.jmx.dynamic.annotation.JMXOperation;
import com.heliosapm.wiex.server.collectors.BaseCollector;

/**
 * <p>Title: WebServiceCollector</p>
 * <p>Description: Collects statistics for any Web Service</p>
 * <p>Copyright: Copyright (c) 2008</p>
 * <p>Company: Helios Development Group</p>
 * @author Sandeep Malhotra
 */
@JMXManagedObject(annotated=true, declared=false)
public class WebServiceCollector extends BaseCollector {
	protected String connectionFactoryClassName=null;
	protected String requestXML = null;
	protected Class<?> connectionFactory = null;
	protected BaseWSClientImpl client = null;
	protected String successSubstringMatch = null;
	protected Pattern successPattern = null;
	
	public WebServiceCollector() {
		super();
	}

	@JMXOperation(description="Starts The Web Service Collector Service", expose=true, name="start")
	public void start() throws Exception {
		tokenizeConnectionProperties();
		initializeWSClient();
		successPattern = Pattern.compile(".*"+this.getSuccessSubstringMatch().trim()+".*");
		super.start();
	}

	protected void tokenizeConnectionProperties() {
		Properties p = new Properties();
		for(Entry<Object, Object> entry: connectionFactoryProperties.entrySet()) {
			p.put(entry.getKey(), formatName(entry.getValue().toString()).trim());
		}
		connectionFactoryProperties.clear();
		connectionFactoryProperties.putAll(p);
	}

	/**
	 * @throws Exception
	 */
	private void initializeWSClient() throws Exception {
		connectionFactory = Class.forName(connectionFactoryClassName, true, getClass().getClassLoader());
		WSClientConnectionFactory wsConnectionFactory = (WSClientConnectionFactory)connectionFactory.newInstance();
		wsConnectionFactory.setProperties(connectionFactoryProperties);
		client = wsConnectionFactory.generateClient();
		client.setRequestXML(requestXML);
        client.initializeLink();
	}

	/**
	 * Collect statistics for monitored web service
	 * @see com.heliosapm.wiex.server.collectors.BaseCollector#collect()
	 */
	@JMXOperation(description="Collects Web Service Statistics", expose=true, name="collect")
	public void collect() {
		long start = System.currentTimeMillis();
		boolean errors = false;
		String response = null;
		if(client!=null){
	    	try {
	    		response = client.pingWebService();
	    		log.debug("\n Response from web method [ "+client.webMethod+" ] at WSDL location [ "+client.wsdlURL+" ] \n\t" + response);
	    		log.info("\n HTTP Code from web method [ "+client.webMethod+" ] at WSDL location [ "+client.wsdlURL+" ] \t" + client.httpResponseCode);
	    		Matcher match = successPattern.matcher(response);
	    		if(match.matches()){
	    			tracer.recordMetric(segmentPrefix, "Avaliability", 1);
	    		}else{
	    			// Ignoring HTTP response code 200 and capturing others
	    			tracer.recordIntervalIncident(tracer.buildSegment(segmentPrefix, false, "Response Code"), client.httpResponseCode+"");
	    		}
	    		tracer.recordMetric(segmentPrefix, "Response Size", response.length());
	    	}catch(java.net.UnknownHostException uhex){
	    		//happens when host name is incorrect or unreachable
	    		errors = true;
	    		tracer.recordIntervalIncident(tracer.buildSegment(segmentPrefix, false, "Errors"), "Unknown Host");
	    		log.error(uhex);
	    	}catch(java.net.ConnectException cex){
	    		//happens usually when port is incorrect
	    		errors = true;
	    		tracer.recordIntervalIncident(tracer.buildSegment(segmentPrefix, false, "Errors"), "Connect");
	    		log.error(cex);
	    	}catch(Exception e){
	    		errors = true;
	    		tracer.recordIntervalIncident(tracer.buildSegment(segmentPrefix, false, "Errors"), "Generic");
	    		log.error(e);
	    	}
		}
		if(errors){
			tracer.recordMetric(segmentPrefix, "Avaliability", 0);
			tracer.recordMetric(segmentPrefix, "Size of Response", 0);
		}
		collectTime = System.currentTimeMillis() - start;
		tracer.recordMetric(segmentPrefix, "Collect Time", collectTime);
	}

	/**
	 * @see com.heliosapm.wiex.server.collectors.BaseCollector#init()
	 */
	public void init() {
		VERSION = "$Revision: 1.0 $";
		MODULE = "WebServiceCollector";
	}


	/**
	 * @return the connectionFactoryClassName
	 */
	@JMXAttribute(description="The WSClientConnectionFactory class name.", name="ConnectionFactoryClassName")
	public String getConnectionFactoryClassName() {
		return connectionFactoryClassName;
	}

	/**
	 * @return the requestXML
	 */
	@JMXAttribute(description="XML request for monitored web service", name="RequestXML")
	public String getRequestXML() {
		return requestXML;
	}

	/**
	 * @param connectionFactoryClassName the connectionFactoryClassName to set
	 */
	public void setConnectionFactoryClassName(String connectionFactoryClassName) {
		this.connectionFactoryClassName = connectionFactoryClassName;
	}

	/**
	 * @param requestXML the requestXML to set
	 */
	public void setRequestXML(String requestXML) {
		this.requestXML = requestXML;
	}

	/**
	 * The WSClientConnectionFactory  properties.
	 * @return the connectionFactoryProperties
	 */
	@JMXAttribute(description="The WSClientConnectionFactory properties.", name="ConnectionFactoryProperties")
	public Properties getConnectionFactoryProperties() {
		return connectionFactoryProperties;
	}

	/**
	 * Sets the WSClientConnectionFactory  properties.
	 * @param connectionFactoryProperties the connectionFactoryProperties to set
	 */
	public void setConnectionFactoryProperties(
			Properties connectionFactoryProperties) {
		this.connectionFactoryProperties = connectionFactoryProperties;
	}

	/**
	 * @return the successSubstringMatch
	 */
	@JMXAttribute(description="The pattern to match against web service response to determine success.", name="SuccessSubstringMatch")
	public String getSuccessSubstringMatch() {
		return successSubstringMatch;
	}

	/**
	 * @param successSubstringMatch the successSubstringMatch to set
	 */
	public void setSuccessSubstringMatch(String successSubstringMatch) {
		this.successSubstringMatch = successSubstringMatch;
	}

}
