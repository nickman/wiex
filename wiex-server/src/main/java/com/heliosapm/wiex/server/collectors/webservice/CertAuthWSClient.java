package com.heliosapm.wiex.server.collectors.webservice;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.log4j.Logger;

/**
* <p>Title: CertAuthWSClient</p>
* <p>Description: Client for web services secured with CLIENT-CERT Authentication 
* 				  (aka Mutual authentication).</p> 
* <p>Copyright: Copyright (c) 2008</p>
* <p>Company: Helios Development Group</p>
* @author Sandeep Malhotra
*/
public class CertAuthWSClient extends BaseWSClientImpl {

	private String keyStoreLocation = null;
	private String keyStorePassword = null;
	private static Logger log = Logger.getLogger(CertAuthWSClient.class);
	
	public CertAuthWSClient(String wsdl, 
			String keyStoreLocation, String keyStorePassword,
		    String webMethod, String sslFactoryKey) throws Exception {
		super(wsdl, webMethod, sslFactoryKey);
		this.keyStoreLocation = keyStoreLocation;
		this.keyStorePassword = keyStorePassword;
		log.info(this);
	}

	/**
	 * Initializes client with appropriate Keystore information for web service
	 */
	protected void initializeLink() throws Exception {

		new SSLContextUtil().registerContext(sslFactoryKey, port,
							  keyStoreLocation, keyStorePassword);
		// Setup HttpClient with keystore information
	    httpClient = new HttpClient();
	    httpClient.getHostConfiguration().setHost(host, port, Protocol.getProtocol(sslFactoryKey));
	    
        post = new PostMethod(pathToWSDL);
        post.setDoAuthentication(true);
        entity = new StringRequestEntity(requestXML, BaseWSClientImpl.MIME_TYPE, null);
        post.setRequestEntity(entity);
	}

	/**
	 * Implementation of abstract method in parent class
	 */
	public String pingWebService() throws Exception {
    	if (httpClient!=null || post!=null) {
    		try {
    			httpResponseCode = httpClient.executeMethod(post);
	            return post.getResponseBodyAsString();
            } catch (Exception ex) {
            	throw ex;
            }
    	}else{
    		throw new Exception ("Invalid state of HttpClient or PostMethod");
    	}
	}
	
	/**
	 * Constructs a <code>String</code> with all attributes
	 * in name = value format.
	 *
	 * @return a <code>String</code> representation 
	 * of this object.
	 */
	public String toString()
	{
	    final String TAB = "\n\t";
	    String retValue = "";
	    retValue = "\nMutualAuthWSClient ( " + TAB
	        + super.toString() + TAB
	        + "wsdlURL = " + this.wsdlURL + TAB
	        + "host = " + this.host + TAB
	        + "port = " + this.port + TAB
	        + "pathToWSDL = " + this.pathToWSDL + TAB
	        + "keyStoreLocation = " + this.keyStoreLocation + TAB
	        + "keyStorePassword = " + this.keyStorePassword + TAB
	        + "requestXMLLocation = " + this.requestXMLLocation + TAB
	        + "webMethod = " + this.webMethod + TAB
	        + "sslFactoryKey = " + this.sslFactoryKey + TAB
	        + "httpClient = " + this.httpClient + TAB
	        + "post = " + this.post + TAB
	        + "requestXML = " + this.requestXML + TAB
	        + "entity = " + this.entity + TAB
	        + " )";
	    return retValue;
	}
	
}
