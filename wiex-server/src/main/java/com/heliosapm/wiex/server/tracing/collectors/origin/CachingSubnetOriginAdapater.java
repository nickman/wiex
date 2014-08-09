/**
 * 
 */
package com.heliosapm.wiex.server.tracing.collectors.origin;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>Title: CachingSubnetOriginAdapater</p>
 * <p>Description: Extension of <code>SubnetOriginAdapater</code> that caches the decodes and rewrites.</p> 
 * <p>Copyright: Copyright (c) 2007</p>
 * <p>Company: Helios Development Group</p>
 * @author Whitehead
 * @version $Revision: 1.1 $
 */

public class CachingSubnetOriginAdapater extends SubnetOriginAdapater {
	
	/** The origin decode cache */
	protected Map<String, String> decodes = null;
	/** The rewriten category cache */
	protected Map<String, String> rewrites = null;
	
	/** A null marker */
	protected static final String NULL = "N";
	
	/**
	 * Creates a new CachingSubnetOriginAdapater with the default sized decode and 20000 rewrite hash map.
	 */
	public CachingSubnetOriginAdapater() {
		decodes = new ConcurrentHashMap<String, String>();
		rewrites = new ConcurrentHashMap<String, String>(20000);
	}
	
	/**
	 * Creates a new CachingSubnetOriginAdapater with the defined hash maps initial capacity.
	 * @param initialCapacity Initial Capacity of the caching hash map.
	 */
	public CachingSubnetOriginAdapater(int initialCapacity) {
		decodes = new ConcurrentHashMap<String, String>(initialCapacity);
		rewrites = new ConcurrentHashMap<String, String>(initialCapacity);
	}

	/**
	 * Creates a new CachingSubnetOriginAdapater with extended options for the concurrent hashmap.
	 * @param initialCapacity Initial Capacity of the caching hash map.
	 * @param loadFactor the load factor threshold, used to control resizing. Resizing may be performed when the average number of elements per bin exceeds this threshold.
	 * @param concurrencyLevel the estimated number of concurrently updating threads. The implementation performs internal sizing to try to accommodate this many threads.
	 */
	public CachingSubnetOriginAdapater(int initialCapacity, float loadFactor, int concurrencyLevel) {
		decodes = new ConcurrentHashMap<String, String>(initialCapacity, loadFactor, concurrencyLevel);
		rewrites = new ConcurrentHashMap<String, String>(initialCapacity, loadFactor, concurrencyLevel);
	}
	
	/**
	 * Returns the lowest subnet of the passed IP address.
	 * Returns a null if an error occurs during decode.
	 * Checks the cache to see if the value has already been decoded.
	 * If not, it is decoded, then cached.
	 * @param ipAddress The originating IP address of a metric submission.
	 * @return The lowest subnet of the passed IP address.
	 * @see com.heliosapm.wiex.server.collectors.origin.OriginAdapter#decodeOrigin(java.lang.String)
	 */
	public String decodeOrigin(String ipAddress) {
		String decode = decodes.get(ipAddress);		
		if(decode==null) {
			decode = super.decodeOrigin(ipAddress);
			if(decode==null) decodes.put(ipAddress, NULL);
			else decodes.put(ipAddress, decode);
		} else if(NULL.equalsIgnoreCase(decode)) {
			decode = null;
		}
		return decode;
	}
	
	/**
	 * Returns the category unmodified but cached.
	 * @param category The tracing category 
	 * @param delimeter The tracer's delimeter.
	 * @return The passed category.
	 */	
	public String rewriteCategory(String category, String delimeter) {
			if(!rewrites.containsKey(category)) rewrites.put(category, category);
			return category;
	}	
}
