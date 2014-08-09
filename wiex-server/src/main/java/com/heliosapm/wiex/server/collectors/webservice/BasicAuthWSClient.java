package com.heliosapm.wiex.server.collectors.webservice;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.log4j.Logger;

/**
* <p>Title: BasicAuthWSClient</p>
* <p>Description: Client for web services secured with BASIC Authentication (credentials).</p> 
* <p>Copyright: Copyright (c) 2008</p>
* <p>Company: Helios Development Group</p>
* @author Sandeep Malhotra
*/
public class BasicAuthWSClient extends BaseWSClientImpl {
	private String userName = null;
	private String password = null;
	private static Logger log = Logger.getLogger(BasicAuthWSClient.class);
	
	public BasicAuthWSClient(String wsdl, String userName,
			String password, String webMethod,
			String sslFactoryKey) throws Exception {
		super(wsdl, webMethod, sslFactoryKey);
		this.userName = userName;
		this.password = password;
		log.info(this);
	}
	
	/**
	 * Initializes client with appropriate credentials for web service
	 */
	protected void initializeLink() throws Exception {

		new SSLContextUtil().registerContext(sslFactoryKey, port);
		// Setup HttpClient with appropriate credentials
	    httpClient = new HttpClient();
	    httpClient.getHostConfiguration().setHost(host, port, Protocol.getProtocol(sslFactoryKey));
	    Credentials credentials = new UsernamePasswordCredentials(userName,password);
	    httpClient.getState().setCredentials(new AuthScope(host, port), credentials);
	    
        post = new PostMethod(pathToWSDL);
        post.setDoAuthentication(true);
        entity = new StringRequestEntity(requestXML, BaseWSClientImpl.MIME_TYPE, null);
        post.setRequestEntity(entity);
	}
	
	/**
	 * Implementation of abstract method in parent class
	 */
	public String pingWebService() throws Exception{
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
	public String toString() {
	    final String TAB = "\n\t";
	    String retValue = "";
	    retValue = "\nBasicAuthWSClient ( " + TAB
	        + super.toString() + TAB
	        + "wsdlURL = " + this.wsdlURL + TAB
	        + "host = " + this.host + TAB
	        + "port = " + this.port + TAB
	        + "pathToWSDL = " + this.pathToWSDL + TAB
	        + "userName = " + this.userName + TAB
	        + "password = " + this.password + TAB
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
