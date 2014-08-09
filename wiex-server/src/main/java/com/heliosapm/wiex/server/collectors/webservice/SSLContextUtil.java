package com.heliosapm.wiex.server.collectors.webservice;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import org.apache.commons.httpclient.contrib.ssl.EasySSLProtocolSocketFactory;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.apache.commons.ssl.KeyMaterial;
import org.apache.log4j.Logger;

/**
* <p>Title: SSLContextUtil</p>
* <p>Description: Utility Class that enables WS Clients to register SSLContext for their SEI
* 	 using a unique sslContextKey provided through configuration file.</p> 
* <p>Copyright: Copyright (c) 2008</p>
* <p>Company: Helios Development Group</p>
* @author Sandeep Malhotra
*/
public class SSLContextUtil {
	
	private static Logger log = Logger.getLogger(SSLContextUtil.class);
	private static final String PROTOCOL_SCHEME = "https";
	/**
	 * Used to register context for client with BASIC authentication.
	 * No keystore information is needed in this case.
	 * @param contextKey
	 * @param port
	 * @return
	 */
	public void registerContext(String sslFactoryKey, int port) throws Exception {
		if(port < 0 || sslFactoryKey == null || sslFactoryKey.trim()=="") {
			//Change to custom Exception class later
			log.error("Invalid port or empty key provided.");
			throw new Exception("Invalid port or empty key provided."); 
		}
		try{
			Protocol.getProtocol(sslFactoryKey);
		}catch(IllegalStateException isex){
			// register Protocol for the first time
			EasySSLProtocolSocketFactory easySSLPSFactory = new EasySSLProtocolSocketFactory();
			Protocol httpsProtocol = new Protocol( PROTOCOL_SCHEME, ( ProtocolSocketFactory ) easySSLPSFactory, port );
			Protocol.registerProtocol( sslFactoryKey, httpsProtocol );
		}
	}
	
	/**
	 * Used to register context for client with CLIENT-CERT/Mutual authentication.
	 * @param contextKey
	 * @param port
	 * @param kStore
	 * @param kStorePassword
	 */
	public void registerContext(String sslFactoryKey, int port, 
								   String kStore, String kStorePassword) throws Exception{
		if( port < 0 || sslFactoryKey == null || sslFactoryKey.trim()=="") {
			//Change to custom Exception class later
			log.error("Invalid port or empty key provided.");
			throw new Exception("Invalid port or context key."); 
		}
		try{
			Protocol.getProtocol(sslFactoryKey);
		}catch(IllegalStateException isex){
			// register Protocol for the first time
			EasySSLProtocolSocketFactory easySSLPSFactory = new EasySSLProtocolSocketFactory();
			loadKeystore(easySSLPSFactory, kStore, kStorePassword);
			Protocol httpsProtocol = new Protocol( PROTOCOL_SCHEME, ( ProtocolSocketFactory ) easySSLPSFactory, port );
			Protocol.registerProtocol( sslFactoryKey, httpsProtocol );
		}
	}
	
	/**
	 * @param easySSL
	 * @param keyStoreLocation
	 * @param keyStorePassword
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 * @throws KeyStoreException
	 * @throws KeyManagementException
	 * @throws CertificateException
	 */
	private void loadKeystore( EasySSLProtocolSocketFactory easySSL, String keyStoreLocation, String keyStorePassword) throws IOException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException, CertificateException
	{
		String keyStore = keyStoreLocation;
		keyStore = keyStore != null ? keyStore.trim() : "";
		String pass = keyStorePassword;
		char[] pwd = pass.toCharArray();
		if( !"".equals( keyStore ) ) {
			File f = new File( keyStore );
			if( f.exists() ) {
				KeyMaterial km = null;
				try {
					km = new KeyMaterial( keyStore, pwd );
					log.info( "Keystore location is: " + keyStore + "" );
				} catch( GeneralSecurityException gse )	{
					log.error( gse );
				}
				if( km != null ) {
					easySSL.setKeyMaterial( km );
				}
			}
		} else {
			easySSL.setKeyMaterial( null );
		}
	}
}
