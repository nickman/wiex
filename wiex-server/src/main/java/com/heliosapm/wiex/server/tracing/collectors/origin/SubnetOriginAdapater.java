/**
 * 
 */
package com.heliosapm.wiex.server.tracing.collectors.origin;

/**
 * <p>Title: SubnetOriginAdapater</p>
 * <p>Description: Returns the lowest subnet of the passed IP address.
 * For example, a passed IP address of <code>192.168.34.82</code> would return <code>192.168.34</code>.</p> 
 * <p>Copyright: Copyright (c) 2007</p>
 * <p>Company: Helios Development Group</p>
 * @author Whitehead
 * @version $Revision: 1.1 $
 */

public class SubnetOriginAdapater extends BasicOriginAdapter {

	/**
	 * Returns the lowest subnet of the passed IP address.
	 * Returns a null if an error occurs during decode.
	 * @param ipAddress The originating IP address of a metric submission.
	 * @return The lowest subnet of the passed IP address.
	 * @see com.heliosapm.wiex.server.collectors.origin.OriginAdapter#decodeOrigin(java.lang.String)
	 */
	public String decodeOrigin(String ipAddress) {
		StringBuilder buff = new StringBuilder();
		String[] ipFragments = ipAddress.split("\\.");
		if(ipFragments.length != 4) return null;
		else {
			for(int i = 0; i < 3; i++) {
				buff.append(ipFragments[i]);
				if(i<2) buff.append(".");
			}
		}
		return buff.toString();
	}
	
	public static void main(String[] args) {
		SubnetOriginAdapater soa = new SubnetOriginAdapater();
		String ip = "192.168.34.87";
		log("Subnet:" + soa.decodeOrigin(ip));
		ip = "192.168.34";
		log("Subnet:" + soa.decodeOrigin(ip));
		ip = "192.168.34.23.45";
		log("Subnet:" + soa.decodeOrigin(ip));
		ip = "1.1.1.1";
		log("Subnet:" + soa.decodeOrigin(ip));
		
	}
	
	public static void log(Object message) {
		System.out.println(message);
	}
	
}
