/**
 * @Author: Sandeep Malhotra
 */
package com.heliosapm.wiex.server.collectors.webservice;

import org.apache.log4j.Logger;


/**
* <p>Title: NoAuthWSClient</p>
* <p>Description: Client for web services secured with no Authentication. </p> 
* <p>Copyright: Copyright (c) 2008</p>
* <p>Company: Helios Development Group</p>
* @author Sandeep Malhotra
*/
public class NoAuthWSClient extends BaseWSClientImpl {

	private static Logger log = Logger.getLogger(NoAuthWSClient.class);
	public NoAuthWSClient(String wsdl, 
							String webMethod) throws Exception {
		super(wsdl, webMethod, null);
		initializeLink();
		log.info(this);
	}
	
	public NoAuthWSClient() {
		// TODO Auto-generated constructor stub
	}

	protected void initializeLink() throws Exception {

	}
	
	public String pingWebService() throws Exception {
		return "";
	}
	
}
