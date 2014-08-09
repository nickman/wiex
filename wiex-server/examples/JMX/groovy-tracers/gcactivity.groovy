import javax.management.*;
import javax.naming.*;
import javax.management.openmbean.*;

/*
	Scriptlet to parse and trace GC activity.
	Sep 24, 2007
	Whitehead
*/

String segment = stateMap.get("Segment");
if(segment==null) {
	String gcName = remoteObjectName.getKeyProperty("name");
	segment = tracer.buildSegment(segmentPrefix, true, "GC", gcName);
	stateMap.put("Segment", segment);
}

//println "Tracing Segment:${segment}"

CompositeData usage = remoteMBeanServer.getAttribute(remoteObjectName, "Usage");
CompositeData collectionUsage = remoteMBeanServer.getAttribute(remoteObjectName, "CollectionUsage");

if(usage!=null) {
	usage.getCompositeType().keySet().each() { key ->
		//println "Tracing ${segment}|${key}"
		tracer.recordCounterMetric(segment + "Usage", key, usage.get(key));
	}
}

if(collectionUsage!=null) {
	collectionUsage.getCompositeType().keySet().each() { key ->
		//println "Tracing ${segment}|${key}"
		tracer.recordCounterMetric(segment + "Collections", key, collectionUsage.get(key));
	}
}

