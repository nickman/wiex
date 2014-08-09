import javax.management.*;
import javax.naming.*;
import javax.management.openmbean.*;

/*
	Scriptlet to parse and trace JBoss Cache Stats
	Sep 25, 2007
	Whitehead
*/
long start = System.currentTimeMillis();
String segment = stateMap.get("Segment-" + remoteObjectName.toString());
Integer resetCounter = stateMap.get("ResetCounter");
Integer resetFrequency = new Integer(stateMap.get("resetFrequency").toString());
if(resetCounter == null) resetCounter = 0;
if(segment==null) {
	String cacheName = remoteObjectName.getKeyProperty("service");
	segment = tracer.buildSegment(segmentPrefix, false, "JBossCache", cacheName, "Stats");
	stateMap.put("Segment-" + remoteObjectName.toString(), segment);
}

//println "\n\n\t[jbosscache.groovy] Tracing [${remoteObjectName.toString()}] ${segment}, Reset:${resetCounter} / ${resetFrequency}\n\n"

AttributeList attributeList = remoteMBeanServer.getAttributes(remoteObjectName, (String[])["AverageWriteTime", "AverageReadTime", "Stores", "HitMissRatio", "Misses", "Evictions", "ReadWriteRatio", "NumberOfNodes", "NumberOfAttributes"]);
Map attrMap = new HashMap();
attributeList.each() { attr -> if(attr.getValue()!=null) attrMap.put(attr.getName(), attr.getValue()); }	
if(attrMap.get("AverageWriteTime")!=null) tracer.recordCounterMetric(segment, "Average Write Time", (long)attrMap.get("AverageWriteTime"));
if(attrMap.get("AverageReadTime")!=null) tracer.recordCounterMetric(segment, "Average Read Time", (long)attrMap.get("AverageReadTime"));
if(attrMap.get("Stores")!=null) tracer.recordCounterMetricDelta(segment, "Stores", (long)attrMap.get("Stores"));
if(attrMap.get("Misses")!=null) tracer.recordCounterMetricDelta(segment, "Misses", (long)attrMap.get("Misses"));
if(attrMap.get("Evictions")!=null) tracer.recordCounterMetricDelta(segment, "Evictions", (long)attrMap.get("Evictions"));
if(attrMap.get("NumberOfAttributes")!=null) tracer.recordCounterMetric(segment, "NumberOfAttributes", (long)attrMap.get("NumberOfAttributes"));
if(attrMap.get("NumberOfNodes")!=null) tracer.recordCounterMetric(segment, "NumberOfNodes", (long)attrMap.get("NumberOfNodes"));
if(attrMap.get("HitMissRatio")!=null) tracer.recordCounterMetric(segment, "HitMissRatio", (int)(attrMap.get("HitMissRatio") * 100).intValue());
if(attrMap.get("ReadWriteRatio")!=null) tracer.recordCounterMetric(segment, "ReadWriteRatio", (int)(attrMap.get("ReadWriteRatio") * 100).intValue());
resetCounter++;
if(resetCounter>resetFrequency) {
	resetCounter = 0;
	long start1 = System.currentTimeMillis();
	remoteMBeanServer.invoke(remoteObjectName, "resetStatistics", (Object[])[], (String[])[]);
	long elapsed1 = System.currentTimeMillis() - start1;
	tracer.recordMetric(segment, "ResetStatistics", elapsed1);
}
stateMap.put("ResetCounter", resetCounter);
long elapsed = System.currentTimeMillis() - start;
tracer.recordMetric(segment, "Collection Time", elapsed);




