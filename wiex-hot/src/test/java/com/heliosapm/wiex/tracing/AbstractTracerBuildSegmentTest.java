/**
 * 
 */
package com.heliosapm.wiex.tracing;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.heliosapm.wiex.tracing.tracing.ITracer;
import com.heliosapm.wiex.tracing.tracing.TracerFactory;

/**
 * <p>Title: AbstractTracerBuildSegmentTest</p>
 * <p>Description: </p> 
 * <p>Copyright: Copyright (c) 2007</p>
 * <p>Organization: Helios Development Group</p>
 * @author Whitehead
 * @version $Revision: 1.1 $
 */

public class AbstractTracerBuildSegmentTest {
	
	String delimeter = "";
	
	ITracer tracer = TracerFactory.getInstance();
	String prefixA = "Foo";
	String prefixB = "Bar";
	String base = "XBASE";

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		log("Tracer Type:" + TracerFactory.getInstance().getClass().getName());
	}
	
	public static void log(Object message) {
		System.out.println(message);
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		
	}

	@Test
	public void testNullPrefix() {
		String s = tracer.buildSegment((String[])null);
		assertTrue(s.equals(""));
	}
	
	@Test
	public void testZeroLengthString() {
		String s = tracer.buildSegment("");
		assertTrue(s.equals(""));
	}
	
	@Test
	public void testZeroLengthArray() {
		String s = tracer.buildSegment(new String[]{});
		assertTrue(s.equals(""));
	}
	
	@Test
	public void testSinglePrefix() {
		String s = tracer.buildSegment(prefixA);
		assertTrue(s.equals(prefixA));
	}
	
	
	@Test
	public void testSingleArrayPrefix() {
		String s = tracer.buildSegment(new String[]{prefixA});
		assertTrue(s.equals(prefixA));
	}
		
	@Test
	public void testSingleArrayNullPrefix() {
		String s = tracer.buildSegment(new String[]{null});
		assertTrue(s.equals(""));
	}	
	
	@Test
	public void testMultipleArrayNullPrefix() {
		String s = tracer.buildSegment(new String[]{null, null});
		assertTrue(s.equals(""));
	}
	
	@Test
	public void testMultipleNullPrefix() {
		String s = tracer.buildSegment(null, null, null);
		assertTrue(s.equals(""));
	}	
	
	@Test
	public void testMultiplePrefix() {
		String s = tracer.buildSegment(prefixA, prefixB);
		assertTrue(s.equals(prefixA + tracer.getSegmentDelimeter() + prefixB));
	}
	
	@Test
	public void testMultipleArrayPrefix() {
		String s = tracer.buildSegment(new String[]{prefixA, prefixB});
		assertTrue(s.equals(prefixA + tracer.getSegmentDelimeter() + prefixB));
	}
	
	@Test
	public void testMultiplePrefixWithNull() {
		String s = tracer.buildSegment(prefixA, null, prefixB);
		assertTrue(s.equals(prefixA + tracer.getSegmentDelimeter() + prefixB));
	}
	
	@Test
	public void testMultipleArrayPrefixWithNull() {
		String s = tracer.buildSegment(new String[]{prefixA, null, prefixB});
		assertTrue(s.equals(prefixA + tracer.getSegmentDelimeter() + prefixB));
	}
		
	@Test
	public void testNullPrefixNoLTD() {
		String s = tracer.buildSegment(false, (String[])null);
		assertTrue(s.equals(""));
	}
	
	@Test
	public void testZeroLengthStringNoLTD() {
		String s = tracer.buildSegment(false, "");
		assertTrue(s.equals(""));
	}
	
	@Test
	public void testZeroLengthArrayNoLTD() {
		String s = tracer.buildSegment(false, new String[]{});
		assertTrue(s.equals(""));
	}
	
	@Test
	public void testSinglePrefixNoLTD() {
		String s = tracer.buildSegment(false, prefixA);
		assertTrue(s.equals(prefixA));
	}
	
	
	@Test
	public void testSingleArrayPrefixNoLTD() {
		String s = tracer.buildSegment(false, new String[]{prefixA});
		assertTrue(s.equals(prefixA));
	}
		
	@Test
	public void testSingleArrayNullPrefixNoLTD() {
		String s = tracer.buildSegment(false, new String[]{null});
		assertTrue(s.equals(""));
	}	
	
	@Test
	public void testMultipleArrayNullPrefixNoLTD() {
		String s = tracer.buildSegment(false, new String[]{null, null});
		assertTrue(s.equals(""));
	}
	
	@Test
	public void testMultipleNullPrefixNoLTD() {
		String s = tracer.buildSegment(false, null, null, null);
		assertTrue(s.equals(""));
	}	
	
	@Test
	public void testMultiplePrefixNoLTD() {
		String s = tracer.buildSegment(false, prefixA, prefixB);
		assertTrue(s.equals(prefixA + tracer.getSegmentDelimeter() + prefixB));
	}
	
	@Test
	public void testMultipleArrayPrefixNoLTD() {
		String s = tracer.buildSegment(false, new String[]{prefixA, prefixB});
		assertTrue(s.equals(prefixA + tracer.getSegmentDelimeter() + prefixB));
	}
	
	@Test
	public void testMultiplePrefixWithNullNoLTD() {
		String s = tracer.buildSegment(false, prefixA, null, prefixB);
		assertTrue(s.equals(prefixA + tracer.getSegmentDelimeter() + prefixB));
	}
	
	@Test
	public void testMultipleArrayPrefixWithNullNoLTD() {
		String s = tracer.buildSegment(false, new String[]{prefixA, null, prefixB});
		assertTrue(s.equals(prefixA + tracer.getSegmentDelimeter() + prefixB));
	}
	
	@Test
	public void testNullPrefixLTD() {
		String s = tracer.buildSegment(true, (String[])null);
		assertTrue(s.equals(""));
	}
	
	@Test
	public void testZeroLengthStringLTD() {
		String s = tracer.buildSegment(true, "");
		assertTrue(s.equals(""));
	}
	
	@Test
	public void testZeroLengthArrayLTD() {
		String s = tracer.buildSegment(true, new String[]{});
		assertTrue(s.equals(""));
	}
	
	@Test
	public void testSinglePrefixLTD() {
		String s = tracer.buildSegment(true, prefixA);
		assertTrue(s.equals(prefixA + tracer.getSegmentDelimeter()));
	}
	
	
	@Test
	public void testSingleArrayPrefixLTD() {
		String s = tracer.buildSegment(true, new String[]{prefixA});
		assertTrue(s.equals(prefixA + tracer.getSegmentDelimeter()));
	}
		
	@Test
	public void testSingleArrayNullPrefixLTD() {
		String s = tracer.buildSegment(true, new String[]{null});
		assertTrue(s.equals(""));
	}	
	
	@Test
	public void testMultipleArrayNullPrefixLTD() {
		String s = tracer.buildSegment(true, new String[]{null, null});
		assertTrue(s.equals(""));
	}
	
	@Test
	public void testMultipleNullPrefixLTD() {
		String s = tracer.buildSegment(true, null, null, null);
		assertTrue(s.equals(""));
	}	
	
	@Test
	public void testMultiplePrefixLTD() {
		String s = tracer.buildSegment(true, prefixA, prefixB);
		assertTrue(s.equals(prefixA + tracer.getSegmentDelimeter() + prefixB + tracer.getSegmentDelimeter()));
	}
	
	@Test
	public void testMultipleArrayPrefixLTD() {
		String s = tracer.buildSegment(true, new String[]{prefixA, prefixB});
		assertTrue(s.equals(prefixA + tracer.getSegmentDelimeter() + prefixB + tracer.getSegmentDelimeter()));
	}
	
	@Test
	public void testMultiplePrefixWithNullLTD() {
		String s = tracer.buildSegment(true, prefixA, null, prefixB);
		assertTrue(s.equals(prefixA + tracer.getSegmentDelimeter() + prefixB + tracer.getSegmentDelimeter()));
	}
	
	@Test
	public void testMultipleArrayPrefixWithNullLTD() {
		String s = tracer.buildSegment(true, new String[]{prefixA, null, prefixB});
		assertTrue(s.equals(prefixA + tracer.getSegmentDelimeter() + prefixB + tracer.getSegmentDelimeter()));
	}
	
	@Test
	public void testNullPrefixWithBaseLTD() {
		String s = tracer.buildSegment(base, true, (String[])null);
		assertTrue(s.equals(base + tracer.getSegmentDelimeter()));
	}
	
	@Test
	public void testZeroLengthStringWithBaseLTD() {
		String s = tracer.buildSegment(base, true, "");
		assertTrue(s.equals(base + tracer.getSegmentDelimeter()));
	}
	
	@Test
	public void testZeroLengthArrayWithBaseLTD() {
		String s = tracer.buildSegment(base, true, new String[]{});
		assertTrue(s.equals(base + tracer.getSegmentDelimeter()));
	}
	
	@Test
	public void testSinglePrefixWithBaseLTD() {
		String s = tracer.buildSegment(base, true, prefixA);
		assertTrue(s.equals(base + tracer.getSegmentDelimeter() + prefixA + tracer.getSegmentDelimeter()));
	}
	
	
	@Test
	public void testSingleArrayPrefixWithBaseLTD() {
		String s = tracer.buildSegment(base, true, new String[]{prefixA});
		assertTrue(s.equals(base + tracer.getSegmentDelimeter() + prefixA + tracer.getSegmentDelimeter()));
	}
		
	@Test
	public void testSingleArrayNullPrefixWithBaseLTD() {
		String s = tracer.buildSegment(base, true, new String[]{null});
		assertTrue(s.equals(base + tracer.getSegmentDelimeter()));
	}	
	
	@Test
	public void testMultipleArrayNullPrefixWithBaseLTD() {
		String s = tracer.buildSegment(base, true, new String[]{null, null});
		assertTrue(s.equals(base + tracer.getSegmentDelimeter()));
	}
	
	@Test
	public void testMultipleNullPrefixWithBaseLTD() {
		String s = tracer.buildSegment(base, true, null, null, null);
		assertTrue(s.equals(base + tracer.getSegmentDelimeter()));
	}	
	
	@Test
	public void testMultiplePrefixWithBaseLTD() {
		String s = tracer.buildSegment(base, true, prefixA, prefixB);
		assertTrue(s.equals(base + tracer.getSegmentDelimeter() + prefixA + tracer.getSegmentDelimeter() + prefixB + tracer.getSegmentDelimeter()));
	}
	
	@Test
	public void testMultipleArrayPrefixWithBaseLTD() {
		String s = tracer.buildSegment(base, true, new String[]{prefixA, prefixB});
		assertTrue(s.equals(base + tracer.getSegmentDelimeter() + prefixA + tracer.getSegmentDelimeter() + prefixB + tracer.getSegmentDelimeter()));
	}
	
	@Test
	public void testMultiplePrefixWithNullWithBaseLTD() {
		String s = tracer.buildSegment(base, true, prefixA, null, prefixB);
		assertTrue(s.equals(base + tracer.getSegmentDelimeter() + prefixA + tracer.getSegmentDelimeter() + prefixB + tracer.getSegmentDelimeter()));
	}
	
	@Test
	public void testMultipleArrayPrefixWithNullWithBaseLTD() {
		String s = tracer.buildSegment(base, true, new String[]{prefixA, null, prefixB});
		assertTrue(s.equals(base + tracer.getSegmentDelimeter() + prefixA + tracer.getSegmentDelimeter() + prefixB + tracer.getSegmentDelimeter()));
	}
	
	@Test
	public void testNullPrefixWithNullBaseLTD() {
		String s = tracer.buildSegment(null, true, (String[])null);
		assertTrue(s.equals(""));
	}
	
	@Test
	public void testZeroLengthStringWithNullBaseLTD() {
		String s = tracer.buildSegment(null, true, "");
		assertTrue(s.equals(""));
	}
	
	@Test
	public void testZeroLengthArrayWithNullBaseLTD() {		
		String s = tracer.buildSegment(null, true, new String[]{});
		assertTrue(s.equals(""));
	}
	
	@Test
	public void testSinglePrefixWithNullBaseLTD() {
		String s = tracer.buildSegment(null, true, prefixA);
		assertTrue(s.equals(prefixA + tracer.getSegmentDelimeter()));
	}
	
	
	@Test
	public void testSingleArrayPrefixWithNullBaseLTD() {
		String s = tracer.buildSegment(null, true, new String[]{prefixA});
		assertTrue(s.equals(prefixA + tracer.getSegmentDelimeter()));
	}
		
	@Test
	public void testSingleArrayNullPrefixWithNullBaseLTD() {
		String s = tracer.buildSegment(null, true, new String[]{null});
		assertTrue(s.equals(""));
	}	
	
	@Test
	public void testMultipleArrayNullPrefixWithNullBaseLTD() {
		String s = tracer.buildSegment(null, true, new String[]{null, null});
		assertTrue(s.equals(""));
	}
	
	@Test
	public void testMultipleNullPrefixWithNullBaseLTD() {
		String s = tracer.buildSegment(null, true, null, null, null);
		assertTrue(s.equals(""));
	}	
	
	@Test
	public void testMultiplePrefixWithNullBaseLTD() {
		String s = tracer.buildSegment(null, true, prefixA, prefixB);
		assertTrue(s.equals(prefixA + tracer.getSegmentDelimeter() + prefixB + tracer.getSegmentDelimeter()));
	}
	
	@Test
	public void testMultipleArrayPrefixWithNullBaseLTD() {
		String s = tracer.buildSegment(null, true, new String[]{prefixA, prefixB});
		assertTrue(s.equals(prefixA + tracer.getSegmentDelimeter() + prefixB + tracer.getSegmentDelimeter()));
	}
	
	@Test
	public void testMultiplePrefixWithNullWithNullBaseLTD() {
		String s = tracer.buildSegment(null, true, prefixA, null, prefixB);
		assertTrue(s.equals(prefixA + tracer.getSegmentDelimeter() + prefixB + tracer.getSegmentDelimeter()));
	}
	
	@Test
	public void testMultipleArrayPrefixWithNullWithNullBaseLTD() {
		String s = tracer.buildSegment(null, true, new String[]{prefixA, null, prefixB});
		assertTrue(s.equals(prefixA + tracer.getSegmentDelimeter() + prefixB + tracer.getSegmentDelimeter()));
	}
	
	@Test
	public void testNullPrefixWithBaseNoLTD() {
		String s = tracer.buildSegment(base, false, (String[])null);
		assertTrue(s.equals(base));
	}

	@Test
	public void testZeroLengthStringWithBaseNoLTD() {
		String s = tracer.buildSegment(base, false, "");
		assertTrue(s.equals(base));
	}

	@Test
	public void testZeroLengthArrayWithBaseNoLTD() {
		String s = tracer.buildSegment(false, new String[]{});
		assertTrue(s.equals(""));
	}

	@Test
	public void testSinglePrefixWithBaseNoLTD() {
		String s = tracer.buildSegment(base, false, prefixA);
		assertTrue(s.equals(base + tracer.getSegmentDelimeter() + prefixA));
	}


	@Test
	public void testSingleArrayPrefixWithBaseNoLTD() {
		String s = tracer.buildSegment(base, false, new String[]{prefixA});
		assertTrue(s.equals(base + tracer.getSegmentDelimeter() + prefixA));
	}

	@Test
	public void testSingleArrayNullPrefixWithBaseNoLTD() {
		String s = tracer.buildSegment(base, false, new String[]{null});
		assertTrue(s.equals(base));
	}

	@Test
	public void testMultipleArrayNullPrefixWithBaseNoLTD() {
		String s = tracer.buildSegment(base, false, new String[]{null, null});
		assertTrue(s.equals(base));
	}

	@Test
	public void testMultipleNullPrefixWithBaseNoLTD() {
		String s = tracer.buildSegment(base, false, null, null, null);
		assertTrue(s.equals(base));
	}

	@Test
	public void testMultiplePrefixWithBaseNoLTD() {
		String s = tracer.buildSegment(base, false, prefixA, prefixB);
		assertTrue(s.equals(base + tracer.getSegmentDelimeter() + prefixA + tracer.getSegmentDelimeter() + prefixB));
	}

	@Test
	public void testMultipleArrayPrefixWithBaseNoLTD() {
		String s = tracer.buildSegment(base, false, new String[]{prefixA, prefixB});
		assertTrue(s.equals(base + tracer.getSegmentDelimeter() + prefixA + tracer.getSegmentDelimeter() + prefixB));
	}

	@Test
	public void testMultiplePrefixWithNullWithBaseNoLTD() {
		String s = tracer.buildSegment(base, false, prefixA, null, prefixB);
		assertTrue(s.equals(base + tracer.getSegmentDelimeter() + prefixA + tracer.getSegmentDelimeter() + prefixB));
	}

	@Test
	public void testMultipleArrayPrefixWithNullWithBaseNoLTD() {
		String s = tracer.buildSegment(base, false, new String[]{prefixA, null, prefixB});
		assertTrue(s.equals(base + tracer.getSegmentDelimeter() + prefixA + tracer.getSegmentDelimeter() + prefixB));
	}
	

}
