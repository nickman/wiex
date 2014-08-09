/**
 * 
 */
package com.heliosapm.wiex.tracing.tracing.comparators;

import java.util.Comparator;

/**
 * <p>Title: DescendingLongComparator</p>
 * <p>Description: Sorts Longs from high to low</p> 
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Organization: Helios Development Group</p>
 * @author Whitehead
 * @version $Revision: 1.2 $
 */

public class DescendingLongComparator implements Comparator<Long> {

	/**
	 * Sorts a collection of longs from high to low
	 * @param long1 The first Long
	 * @param long2 The second Long
	 * @return A positive int if o1 is less than or equal to o2. Otherwise, a negative int.
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	public int compare(Long long1, Long long2) {
			if(long1.longValue() <= long2.longValue()) return 1;
			else return -1;
	}

}
