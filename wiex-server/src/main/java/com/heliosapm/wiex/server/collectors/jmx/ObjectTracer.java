/**
 * 
 */
package com.heliosapm.wiex.server.collectors.jmx;

import java.util.List;

/**
 * <p>Title: ObjectTracer</p>
 * <p>Description: Interface defining the behaviour of object tracers which take a JMX attribute of an arbitrary type and render metrics from it.</p> 
 * <p>Copyright: Copyright (c) 2007</p>
 * <p>Company: Helios Development Group</p>
 * @author Whitehead
 * @version $Revision: 1.2 $
 */

public interface ObjectTracer {
	public static final String SEGMENT_SUFFIX = "segment.suffix";
	public static final String TRACER = "tracer";
	public List<RenderedTrace> renderTracingValue(Object ... args);
	public void setProperty(String name, Object value);
}
