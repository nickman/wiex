/**
 * 
 */
package com.heliosapm.wiex.server.collectors.network;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

/**
 * <p>Title: NonValidatingSSLHostNameVerifier</p>
 * <p>Description: A tolerant host name verifier for https that ignores the fact that the cert host name does not match the host name in the URL</p> 
 * <p>Copyright: Copyright (c) 2007</p>
 * <p>Company: Helios Development Group</p>
 * @author Whitehead
 * @version $Revision$
 */

public class NonValidatingSSLHostNameVerifier implements HostnameVerifier {

	/**
	 * @param arg0
	 * @param arg1
	 * @return
	 * @see javax.net.ssl.HostnameVerifier#verify(java.lang.String, javax.net.ssl.SSLSession)
	 */
	public boolean verify(String arg0, SSLSession arg1) {
		return true;
	}

}
