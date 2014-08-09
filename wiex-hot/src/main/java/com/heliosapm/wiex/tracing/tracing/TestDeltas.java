/**
 * 
 */
package com.heliosapm.wiex.tracing.tracing;


import java.lang.management.*;
import java.util.Iterator;
import java.util.List;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
/**
 * <p>Title: TestDeltas</p>
 * <p>Description: Test for Delta Tracing</p> 
 * <p>Copyright: Copyright (c) 2007</p>
 * <p>Organization: Helios Development Group</p>
 * @author Whitehead
 * @version $Revision$
 */

public class TestDeltas {
	public static void main(String[] args) {
		log("Delta Tracing Test");		
		
		CompilationMXBean compilationMXBean = ManagementFactory.getCompilationMXBean();
		List<GarbageCollectorMXBean> garbageCollectorMXBeans = ManagementFactory.getGarbageCollectorMXBeans();
		printMBeanServerDomains();
		ITracer tracer = TracerFactory.getInstance();
		String segment = tracer.buildSegment("Test", "Deltas");
		log("Tracing Type:" + tracer.getClass().getName());
		log("Segment:" + segment);
		log("Starting Trace....");
		
		for(int i = 0; i < 10000; i++) {
			long collections = 0;
			long collectionTime = 0;
			long compileTime = 0;
			for(GarbageCollectorMXBean gcBean: garbageCollectorMXBeans) {
				collections = collections + gcBean.getCollectionCount();
				collectionTime = collectionTime + gcBean.getCollectionTime();
			}
			tracer.recordCounterMetricDelta(segment, "GC Collections", collections);
			log("Traced GC Collections:" + collections);
			tracer.recordCounterMetricDelta(segment, "GC Time", collectionTime);
			log("Traced GC Time:" + collectionTime);
			compileTime = compilationMXBean.getTotalCompilationTime();
			tracer.recordMetricDelta(segment, "Compile Time", compileTime);
			log("Traced Compile Time:" + compileTime);
			log("=============================");
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	
	public static void log(Object message) {
		System.out.println(message);
	}
	
	public static void printMBeanServerDomains() {
		Iterator iterator = MBeanServerFactory.findMBeanServer(null).iterator();
		while(iterator.hasNext()) {
			MBeanServer mbeanServer = (MBeanServer)iterator.next();
			log("Domain:" + mbeanServer.getDefaultDomain());
		}
	}
}
