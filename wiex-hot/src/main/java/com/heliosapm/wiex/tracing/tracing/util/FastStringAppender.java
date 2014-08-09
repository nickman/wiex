package com.heliosapm.wiex.tracing.tracing.util;

import java.util.HashMap;
import java.util.Map;



/**
 * <p>Title: FastStringAppender</p>
 * <p>Description: A performance enhanced String appender and renderer. Caches references to built strings for fast lookup.</p>
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Organization: Helios Development Group</p>
 * @author Jeff Drost
 * @author Whitehead
 * @version $Revision: 1.1 $ 
 */
public class FastStringAppender {
	// this cache will hold a maximum of 10000 string pairs.
	private static final Map<String, Map<String, String>> primaryMap = new BoundedHashMap<String, Map<String, String>>();
	private String value;

	public FastStringAppender(String value) {
		this.value = value;
	}

	private static String concat(String primary, String secondary) {

		Map<String, String> secondaryMap = primaryMap.get(primary);

		if (secondaryMap == null) {
			secondaryMap = new BoundedHashMap<String, String>();
			primaryMap.put(primary, secondaryMap);
		}

		String result = secondaryMap.get(secondary);

		if (result == null) {

			result = primary + secondary;  // here it is.. the expensive shizzle
			secondaryMap.put(secondary, result);

		}

		return result;
	}

	private static class BoundedHashMap<K,V> extends HashMap<K,V> {

		private static final long serialVersionUID = 1080494845352449283L;

		public BoundedHashMap() {
			super(16, 0.3f);
		}

		public V put(K key, V value) {
			if (size() > 10000) {
				// this isn't the best strategy...
				clear();
			}

			return super.put(key, value);
		}
	}

	public FastStringAppender append(String string) {
		value = concat(value, string);
		return this;
	}

	public String getValue() {
		return value;
	}

	public String toString() {
		return value;
	}

}

