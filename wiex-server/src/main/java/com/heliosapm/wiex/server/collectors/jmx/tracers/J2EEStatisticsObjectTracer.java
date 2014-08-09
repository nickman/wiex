/**
 * 
 */
package com.heliosapm.wiex.server.collectors.jmx.tracers;

import java.util.ArrayList;
import java.util.List;

import javax.management.j2ee.statistics.CountStatistic;
import javax.management.j2ee.statistics.RangeStatistic;
import javax.management.j2ee.statistics.Statistic;
import javax.management.j2ee.statistics.Stats;
import javax.management.j2ee.statistics.TimeStatistic;

import com.heliosapm.wiex.server.collectors.BaseCollector;
import com.heliosapm.wiex.server.collectors.jmx.AbstractObjectTracer;
import com.heliosapm.wiex.server.collectors.jmx.RenderedTrace;

/**
 * <p>Title: J2EEStatisticsObjectTracer</p>
 * <p>Description: Renders stats for a <code>javax.management.j2ee.statistics.Stats</code> object.</p> 
 * <p>Copyright: Copyright (c) 2007</p>
 *  * <p>Company: Helios Development Group</p>
 * @author Whitehead
 * @version $Revision: 1.2 $
 */

public class J2EEStatisticsObjectTracer extends AbstractObjectTracer {

	/**
	 * Renders stats for a <code>javax.management.j2ee.statistics.Stats</code> object.
	 * @param obj An instance of a <code>javax.management.j2ee.statistics.Stats</code>
	 * @return A list of <code>RenderedTrace</code>s.
	 * @see com.adp.sbs.metrics.tracing.collectors.jmx.ObjectTracer#renderTracingValue(java.lang.Object)
	 */
	public List<RenderedTrace> renderTracingValue(Object ... args) {
		List<RenderedTrace> renderedTraces = new ArrayList<RenderedTrace>();
		String prefix = (String)properties.get(SEGMENT_SUFFIX);
		boolean traceTimeStats = ("TRUE".equalsIgnoreCase((String)properties.get("trace.ejb.timed.stats")));
		Stats stats = (Stats)args[0];
		Statistic[] statistics = stats.getStatistics();
		String[] statNames = stats.getStatisticNames();
		for(String statName: statNames) {
			Statistic statistic = stats.getStatistic(statName);
			if(statistic instanceof RangeStatistic) {
				renderedTraces.add(new RenderedTrace(prefix, statistic.getName() + " Current", BaseCollector.METRIC_TYPE_COUNTER_LONG, "" + ((RangeStatistic)statistic).getCurrent()));
				renderedTraces.add(new RenderedTrace(prefix, statistic.getName()  + " High", BaseCollector.METRIC_TYPE_COUNTER_LONG, "" + ((RangeStatistic)statistic).getHighWaterMark()));
				renderedTraces.add(new RenderedTrace(prefix, statistic.getName() + "Low ", BaseCollector.METRIC_TYPE_COUNTER_LONG, "" + ((RangeStatistic)statistic).getLowWaterMark()));				
			} else if(statistic instanceof CountStatistic){
				renderedTraces.add(new RenderedTrace(prefix, statistic.getName() + " Rate", BaseCollector.METRIC_TYPE_COUNTER_DELTA_LONG, "" + ((CountStatistic)statistic).getCount()));
			} else if(statistic instanceof TimeStatistic){
				renderedTraces.add(new RenderedTrace(prefix, statistic.getName() + " Rate", BaseCollector.METRIC_TYPE_COUNTER_DELTA_LONG, "" + ((TimeStatistic)statistic).getCount()));
				renderedTraces.add(new RenderedTrace(prefix, statistic.getName() + " Max Time", BaseCollector.METRIC_TYPE_COUNTER_LONG, "" + ((TimeStatistic)statistic).getMaxTime()));
				renderedTraces.add(new RenderedTrace(prefix, statistic.getName() + " Min Time", BaseCollector.METRIC_TYPE_COUNTER_LONG, "" + ((TimeStatistic)statistic).getMinTime()));
				long count = ((TimeStatistic)statistic).getCount();
				long totalTime = ((TimeStatistic)statistic).getTotalTime();
				long avgTime = 0L;
				if(totalTime > 0 && count > 0) {
					avgTime = totalTime / count;
					renderedTraces.add(new RenderedTrace(prefix, statName + " Avg Time", BaseCollector.METRIC_TYPE_COUNTER_LONG, "" + avgTime));
				}								
			}				
		}
		return renderedTraces;
	}


}
