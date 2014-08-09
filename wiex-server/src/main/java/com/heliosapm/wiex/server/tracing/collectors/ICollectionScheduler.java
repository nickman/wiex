package com.heliosapm.wiex.server.tracing.collectors;

import java.util.Date;
import java.util.TimerTask;

import com.heliosapm.wiex.jmx.dynamic.annotation.JMXOperation;

public interface ICollectionScheduler {

	/**
	 * Terminates this timer, discarding any currently scheduled tasks. 
	 * @see java.util.Timer#cancel()
	 */
	public abstract void cancel();

	/**
	 * Removes all cancelled tasks from this timer's task queue. 
	 * @return the number of tasks removed from the queue.
	 * @see java.util.Timer#purge()
	 */
	public abstract int purge();

	/**
	 * Schedules the specified task for repeated fixed-delay execution, beginning at the specified time. 
	 * @param task task to be scheduled.
	 * @param firstTime First time at which task is to be executed.
	 * @param period time in milliseconds between successive task executions.
	 * @see java.util.Timer#schedule(java.util.TimerTask, java.util.Date, long)
	 */	
	public abstract void schedule1(TimerTask task, Date firstTime, long period);

	/**
	 * Schedules the specified task for execution at the specified time. 
	 * @param task task to be scheduled.
	 * @param time time at which task is to be executed.
	 * @see java.util.Timer#schedule(java.util.TimerTask, java.util.Date)
	 */	
	public abstract void schedule2(TimerTask task, Date time);

	/**
	 * @param task task to be scheduled.
	 * @param delay
	 * @param period time in milliseconds between successive task executions.
	 * @see java.util.Timer#schedule(java.util.TimerTask, long, long)
	 */
	public abstract void schedule(TimerTask task, long delay, long period);

	/**
	 * @param task task to be scheduled.
	 * @param delay
	 * @see java.util.Timer#schedule(java.util.TimerTask, long)
	 */
	public abstract void schedule4(TimerTask task, long delay);

	/**
	 * Schedules the specified task for repeated fixed-rate execution, beginning at the specified time.
	 * @param task task to be scheduled.
	 * @param firstTime First time at which task is to be executed.
	 * @param period time in milliseconds between successive task executions.
	 * @see java.util.Timer#scheduleAtFixedRate(java.util.TimerTask, java.util.Date, long)
	 */
	public abstract void scheduleAtFixedRate1(TimerTask task, Date firstTime,
			long period);

	/**
	 * Schedules the specified task for repeated fixed-rate execution, beginning after the specified delay.
	 * @param task task to be scheduled.
	 * @param delay
	 * @param period time in milliseconds between successive task executions.
	 * @see java.util.Timer#scheduleAtFixedRate(java.util.TimerTask, long, long)
	 */
	public abstract void scheduleAtFixedRate(TimerTask task, long delay,
			long period);

}