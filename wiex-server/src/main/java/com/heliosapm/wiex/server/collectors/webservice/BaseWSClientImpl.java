package com.heliosapm.wiex.server.collectors.webservice;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.log4j.Logger;

/**
* <p>Title: BaseWSClientImpl</p>
* <p>Description: Base Abstract Class for all types of web service clients.</p> 
* <p>Copyright: Copyright (c) 2008</p>
* <p>Company: Helios Development Group</p>
* @author Sandeep Malhotra
*/
public abstract class BaseWSClientImpl implements BaseWSClient{

	protected URL wsdlURL = null;
	protected String host = null;
	protected int port;
	protected String pathToWSDL = null;
	protected String requestXMLLocation = null;
	protected String webMethod = null;
	protected String sslFactoryKey = null;
	protected HttpClient httpClient = null;
	protected PostMethod post = null;
	protected String requestXML = null; 
	protected RequestEntity entity = null;	
	protected static final String SOAP_ACTION = "SOAPAction";
	protected static final String MIME_TYPE = "text/xml; charset=ISO-8859-1";
	private static Logger log = Logger.getLogger(BaseWSClientImpl.class);
	protected int httpResponseCode;
	
	public BaseWSClientImpl(){
	}
	
	/**
	 * @param wsdl
	 * @param webMethod
	 * @param sslFactoryKey
	 */
	public BaseWSClientImpl(String wsdl, 
			String webMethod, String sslFactoryKey) {
		try{
			this.wsdlURL = new URL(wsdl);
		}catch(MalformedURLException muex){
			log.error("Bad wsdl URL provided [ " +wsdl+ " ]",muex);
		}
		this.host = wsdlURL.getHost();
		this.port = wsdlURL.getPort()== -1? wsdlURL.getDefaultPort(): wsdlURL.getPort();
		this.pathToWSDL = wsdlURL.getPath()+"?"+wsdlURL.getQuery();
		this.webMethod = webMethod;
		this.sslFactoryKey = sslFactoryKey;
	}	
	
	/**
	 * Abstract method that must be implemented by base classes
	 * @throws Exception
	 */
	protected abstract void initializeLink() throws Exception;
	
	/**
	 * Abstract method that must be implemented by base classes.
	 * This method is called Web Service Collector's collect 
	 * method on scheduled intervals.
	 * @throws Exception
	 */
	public abstract String pingWebService() throws Exception;


	/**
	 * @param requestXMLLocation the requestXMLLocation to set
	 */
	public void setRequestXML(String requestXML) {
		this.requestXML = requestXML;
	}

}
