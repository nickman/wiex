package com.heliosapm.wiex.server.collectors.webservice;

/**
* <p>Title: BaseWSClient</p>
* <p>Description: Interface for all types of web service clients.</p> 
* <p>Copyright: Copyright (c) 2008</p>
* <p>Company: Helios Development Group</p>
* @author Sandeep Malhotra
*/
public interface BaseWSClient {
	public String pingWebService() throws Exception;
}
