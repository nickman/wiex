/**
 * 
 */
package com.heliosapm.wiex.server.tracing.collectors;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

import com.heliosapm.wiex.jmx.dynamic.ManagedObjectDynamicMBean;
import com.heliosapm.wiex.jmx.dynamic.annotation.JMXAttribute;
import com.heliosapm.wiex.jmx.dynamic.annotation.JMXManagedObject;
import com.heliosapm.wiex.jmx.dynamic.annotation.JMXOperation;

/**
 * <p>Title: CollectionScheduler</p>
 * <p>Description: A JMX MBean wrapped Timer and a client to access it.</p> 
 * <p>Copyright: Copyright (c) 2007</p>
 * <p>Company: Helios Development Group</p>
 * @author Whitehead
 * @version $Revision: 1.1 $
 */
@JMXManagedObject(annotated=true, declared=false)
public class CollectionScheduler extends ManagedObjectDynamicMBean implements ICollectionScheduler {
	protected Timer timer = null;
	protected AtomicInteger scheduledTasks = new AtomicInteger(0);
	
	public CollectionScheduler() {
		super();
		timer = new Timer("Collection Service Polling Timer-" + this.getClass().getClassLoader().toString(), true);
		reflectObject(this);
	}

	/**
	 * 
	 * @see com.heliosapm.wiex.server.collectors.ICollectionScheduler#cancel()
	 */
	@JMXOperation(description="Terminates this timer, discarding any currently scheduled tasks.", expose=true, name="cancel")
	public void cancel() {
		timer.cancel();
	}

	/**
	 * @return
	 * @see com.heliosapm.wiex.server.collectors.ICollectionScheduler#purge()
	 */
	@JMXOperation(description="Removes all cancelled tasks from this timer's task queue.", expose=true, name="purge")
	public int purge() {
		return timer.purge();
	}

	/**
	 * @param task
	 * @param firstTime
	 * @param period
	 * @see com.heliosapm.wiex.server.collectors.ICollectionScheduler#schedule(java.util.TimerTask, java.util.Date, long)
	 */
	@JMXOperation(description="Schedules the specified task for repeated fixed-delay execution, beginning at the specified time. ", expose=true, name="schedule1")
	public void schedule1(TimerTask task, Date firstTime, long period) {
		scheduledTasks.incrementAndGet();
		timer.schedule(task, firstTime, period);
	}

	/**
	 * @param task
	 * @param time
	 * @see com.heliosapm.wiex.server.collectors.ICollectionScheduler#schedule(java.util.TimerTask, java.util.Date)
	 */
	@JMXOperation(description="Schedules the specified task for repeated fixed-delay execution, beginning at the specified time. ", expose=true, name="schedule2")
	public void schedule2(TimerTask task, Date time) {
		scheduledTasks.incrementAndGet();
		timer.schedule(task, time);
	}

	/**
	 * @param task
	 * @param delay
	 * @param period
	 * @see com.heliosapm.wiex.server.collectors.ICollectionScheduler#schedule(java.util.TimerTask, long, long)
	 */
	@JMXOperation(description="Schedules the specified task for repeated fixed-delay execution, beginning at the specified time. ", expose=true, name="schedule")
	public void schedule(TimerTask task, long delay, long period) {
		scheduledTasks.incrementAndGet();
		timer.schedule(task, delay, period);
	}

	/**
	 * @param task
	 * @param delay
	 * @see com.heliosapm.wiex.server.collectors.ICollectionScheduler#schedule(java.util.TimerTask, long)
	 */
	@JMXOperation(description="Schedules the specified task for repeated fixed-delay execution, beginning at the specified time. ", expose=true, name="schedule4")
	public void schedule4(TimerTask task, long delay) {
		scheduledTasks.incrementAndGet();
		timer.schedule(task, delay);
	}

	/**
	 * @param task
	 * @param firstTime
	 * @param period
	 * @see com.heliosapm.wiex.server.collectors.ICollectionScheduler#scheduleAtFixedRate(java.util.TimerTask, java.util.Date, long)
	 */
	@JMXOperation(description="Schedules the specified task for repeated fixed-rate execution, beginning at the specified time.", expose=true, name="scheduleAtFixedRate1")
	public void scheduleAtFixedRate1(TimerTask task, Date firstTime, long period) {
		scheduledTasks.incrementAndGet();
		timer.scheduleAtFixedRate(task, firstTime, period);
	}

	/**
	 * @param task
	 * @param delay
	 * @param period
	 * @see com.heliosapm.wiex.server.collectors.ICollectionScheduler#scheduleAtFixedRate(java.util.TimerTask, long, long)
	 */
	@JMXOperation(description="Schedules the specified task for repeated fixed-rate execution, beginning after the specified delay.", expose=true, name="scheduleAtFixedRate")
	public void scheduleAtFixedRate(TimerTask task, long delay, long period) {
		scheduledTasks.incrementAndGet();
		timer.scheduleAtFixedRate(task, delay, period);
	}

	/**
	 * The number of scheduling requests
	 * @return the scheduledTasks
	 */
	@JMXAttribute(description="The number of scheduling requests", expose=true, name="ScheduleRequestCount")
	public int getScheduledTasks() {
		return scheduledTasks.intValue();
	}
	
	
}
