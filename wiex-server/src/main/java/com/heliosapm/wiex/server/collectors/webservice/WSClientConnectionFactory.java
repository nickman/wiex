package com.heliosapm.wiex.server.collectors.webservice;

import java.util.Properties;

/**
 * <p>Title: WSClientConnectionFactory</p>
 * <p>Description: Factory class to hand over appropriate instance of web service 
 * 	  client based on it's Authentication type</p>
 * <p>Copyright: Copyright (c) 2008</p>
 * <p>Company: Helios Development Group</p>
 * @author Sandeep Malhotra
 */
public class WSClientConnectionFactory {
	
	private static final String AUTH_BASIC = "BASIC";
	private static final String AUTH_CLIENT_CERT = "CLIENT-CERT";
	private static final String AUTH_NONE = "None";
	protected Properties wsProperties = new Properties(); 
	
	/**
	 * @return Instance of appropriate client for monitored web service
	 * @throws Exception
	 */
	public BaseWSClientImpl generateClient() throws Exception{
		BaseWSClientImpl client = null;
		String authMethod = wsProperties.getProperty("authMethod") == null ? "" : wsProperties.getProperty("authMethod");
		if(authMethod.equalsIgnoreCase(AUTH_BASIC)){
			client = new BasicAuthWSClient(
								wsProperties.getProperty("wsdlURL"),
								wsProperties.getProperty("userName"),
								wsProperties.getProperty("password"),
								wsProperties.getProperty("pingMethod"),
								wsProperties.getProperty("sslFactoryKey")
					);
		}else if(authMethod.equalsIgnoreCase(AUTH_CLIENT_CERT)){
			client = new CertAuthWSClient(
								wsProperties.getProperty("wsdlURL"),
								wsProperties.getProperty("keyStore"),
								wsProperties.getProperty("keyStorePassword"),
								wsProperties.getProperty("pingMethod"),
								wsProperties.getProperty("sslFactoryKey")
					);
		}else if(authMethod.equalsIgnoreCase(AUTH_NONE)){
			client = new NoAuthWSClient();
		}
		return client;
	}

	/**
	 * @param wsProperties the wsProperties to set
	 */
	public void setProperties(Properties wsProperties) {
		this.wsProperties.clear();
		this.wsProperties.putAll(wsProperties);
	}
}
