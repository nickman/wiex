import javax.management.*;
import javax.naming.*;
import javax.management.openmbean.*;

/*
	Scriptlet to parse and trace the composite data type representing the memory pools in the JVM.
	Sep 24, 2007
	Whitehead
*/

String segment = stateMap.get("Segment-" + remoteObjectName.toString());
if(segment==null) {
	String memoryType = remoteMBeanServer.getAttribute(remoteObjectName, "Type");
	String poolName = remoteObjectName.getKeyProperty("name");
	segment = tracer.buildSegment(segmentPrefix, true, "Memory Pools", memoryType, poolName);
	stateMap.put("Segment-" + remoteObjectName.toString(), segment);
}

//println "\n\t[memoryTypeTrace.groovy] Tracing [${remoteObjectName}] Segment:${segment}"

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

