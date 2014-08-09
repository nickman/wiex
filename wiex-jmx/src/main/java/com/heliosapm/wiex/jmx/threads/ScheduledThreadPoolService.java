/**
 * 
 */
package com.heliosapm.wiex.jmx.threads;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.heliosapm.wiex.jmx.dynamic.annotation.JMXManagedObject;
import com.heliosapm.wiex.jmx.dynamic.annotation.JMXOperation;
import com.heliosapm.wiex.jmx.dynamic.annotation.JMXOperationParameter;

/**
 * <p>Title: ScheduledThreadPoolService</p>
 * <p>Description: A JMX MBean wrapped ScheduledThreadPoolService</p> 
 * <p>Copyright: Copyright (c) 2007</p>
 * <p>Organization: Helios Development Group</p>
 * @author Whitehead
 * @version $Revision: 1.1 $
 */
@JMXManagedObject(declared=false, annotated=true)
public class ScheduledThreadPoolService extends ThreadPoolService implements IScheduledThreadPoolService {

	/**
	 * Instantiates a new ScheduledThreadPoolService
	 */
	public ScheduledThreadPoolService() {
		super();
	}

	
	/**
	 * @throws Exception
	 * @see com.heliosapm.wiex.jmx.threads.IScheduledThreadPoolService#start()
	 */
	@Override
	@JMXOperation(description="Starts the Service", name="start")
	public void start() throws Exception {
		if(isRunning) return;
		log.info("\n\t================================\n\tStarting Service" + objectName + "\n\t================================");
		if(threadGroupName==null) {
			threadGroupName = objectName.toString();
		}
		log.info("Thread Group Name:" + threadGroupName);
		threadGroup = new ThreadGroup(threadGroupName);
		if(threadNamePrefix==null) {
			threadNamePrefix = objectName.toString();
		}
		log.info("Thread Name Prefix:" + threadNamePrefix);
		setDiscardPolicy();
		log.info("Discard Policy:" + handler.getClass().getName());		
		log.info("Thread Keep Alive Time:" + threadKeepAliveTime);
		log.info("Core Pool Size:" + corePoolSize);
		threadPool = new ScheduledThreadPoolExecutor(corePoolSize, this, handler);
		((ScheduledThreadPoolExecutor)threadPool).setContinueExistingPeriodicTasksAfterShutdownPolicy(false);
		((ScheduledThreadPoolExecutor)threadPool).setExecuteExistingDelayedTasksAfterShutdownPolicy(false);		
		int prestarted = 0;
		for(int i = 0; i < prestartThreads; i++) {
			if(threadPool.prestartCoreThread()) prestarted++;
		}
		log.info("Prestarted " + prestarted + " Threads.");
		isRunning = true;
		log.info("\n\t================================\n\tStarted Service" + objectName + "\n\t================================");
	}	
	

	
	
	
	
	/**
	 * Returns the size of the task queue used by this executor. 
	 * @return the size of the queue.
	 */
	@JMXOperation(description="The size of the task queue used by this executor.", name="QueueSize")
	public int getTaskQueueDepth() {
		return ((ScheduledThreadPoolExecutor)threadPool).getQueue().size();
	}
	
	/**
	 * @param task
	 * @return
	 * @see com.heliosapm.wiex.jmx.threads.IScheduledThreadPoolService#remove(java.lang.Runnable)
	 */
	@JMXOperation(description="Removes this task from the pool", name="remove")
	public boolean remove(@JMXOperationParameter(description="The task to remove", name="task") Runnable task) {
		return ((ScheduledThreadPoolExecutor)threadPool).remove(task);
	}
	
	/**
	 * @param <V>
	 * @param callable
	 * @param delay
	 * @param unit
	 * @return
	 * @see com.heliosapm.wiex.jmx.threads.IScheduledThreadPoolService#scheduleCallable(java.util.concurrent.Callable, long, java.util.concurrent.TimeUnit)
	 */
	@JMXOperation(description="Creates and executes a ScheduledFuture that becomes enabled after the given delay.", name="scheduleCallable")
	public <V> ScheduledFuture<V> scheduleCallable(
		@JMXOperationParameter(description="The function to execute.", name="callable") Callable<V> callable, 
		@JMXOperationParameter(description="The time from now to delay execution.", name="delay") long delay, 
		@JMXOperationParameter(description="The time unit of the delay parameter.", name="unit") TimeUnit unit) {
		return ((ScheduledThreadPoolExecutor)threadPool).schedule(callable, delay, unit);
	}
	
	/**
	 * @param command
	 * @param delay
	 * @param unit
	 * @return
	 * @see com.heliosapm.wiex.jmx.threads.IScheduledThreadPoolService#scheduleRunnable(java.lang.Runnable, long, java.util.concurrent.TimeUnit)
	 */
	@JMXOperation(description="Creates and executes a one-shot action that becomes enabled after the given delay.", name="scheduleRunnable")
	public  ScheduledFuture<?> scheduleRunnable(
		@JMXOperationParameter(description="The task to execute.", name="command") Runnable command, 
		@JMXOperationParameter(description="The time from now to delay execution.", name="delay") long delay, 
		@JMXOperationParameter(description="The time unit of the delay parameter.", name="unit") TimeUnit unit) {
		return ((ScheduledThreadPoolExecutor)threadPool).schedule(command, delay, unit);
	}
	
	/**
	 * @param command
	 * @param initialDelay
	 * @param period
	 * @param unit
	 * @return
	 * @see com.heliosapm.wiex.jmx.threads.IScheduledThreadPoolService#scheduleAtFixedRate(java.lang.Runnable, long, long, java.util.concurrent.TimeUnit)
	 */
	@JMXOperation(description="Creates and executes a periodic action that becomes enabled first after the given initial delay", name="scheduleAtFixedRate")
	public ScheduledFuture<?> scheduleAtFixedRate(
		@JMXOperationParameter(description="The task to execute.", name="command") Runnable command, 
		@JMXOperationParameter(description="The time to delay first execution.", name="delay") long initialDelay, 
		@JMXOperationParameter(description="The period between successive executions.", name="period") long period, 
		@JMXOperationParameter(description="The time unit of the delay parameter.", name="unit") TimeUnit unit) {
		return ((ScheduledThreadPoolExecutor)threadPool).scheduleAtFixedRate(command, initialDelay, period, unit);
	}
	
	/**
	 * @param command
	 * @param initialDelay
	 * @param period
	 * @param unit
	 * @return
	 * @see com.heliosapm.wiex.jmx.threads.IScheduledThreadPoolService#scheduleWithFixedDelay(java.lang.Runnable, long, long, java.util.concurrent.TimeUnit)
	 */
	@JMXOperation(description="Creates and executes a periodic action that becomes enabled first after the given initial delay", name="scheduleWithFixedDelay")
	public  ScheduledFuture<?>  scheduleWithFixedDelay(
		@JMXOperationParameter(description="The task to execute.", name="command") Runnable command, 
		@JMXOperationParameter(description="The time to delay first execution.", name="delay") long initialDelay, 
		@JMXOperationParameter(description="The period between successive executions.", name="period") long period, 
		@JMXOperationParameter(description="The time unit of the delay parameter.", name="unit") TimeUnit unit) {
		return ((ScheduledThreadPoolExecutor)threadPool).scheduleWithFixedDelay(command, initialDelay, period, unit);
	}
	
	/**
	 * @param <T>
	 * @param task
	 * @return
	 * @see com.heliosapm.wiex.jmx.threads.IScheduledThreadPoolService#submitCallable(java.util.concurrent.Callable)
	 */
	@JMXOperation(description="Submits a value-returning task for execution and returns a Future representing the pending results of the task.", name="submitCallable")
	public <T>Future<T> submitCallable(@JMXOperationParameter(description="The task to submit.", name="callable") Callable<T> task) {
		return ((ScheduledThreadPoolExecutor)threadPool).submit(task);
	}
	
	/**
	 * @param task
	 * @return
	 * @see com.heliosapm.wiex.jmx.threads.IScheduledThreadPoolService#submitRunnable(java.lang.Runnable)
	 */
	@JMXOperation(description="Submits a Runnable task for execution and returns a Future representing that task.", name="submitRunnable")
	public Future<?> submitRunnable(@JMXOperationParameter(description="The task to submit.", name="callable") Runnable task) {
		return ((ScheduledThreadPoolExecutor)threadPool).submit(task);
	}
	
	
	/**
	 * @param <T>
	 * @param task
	 * @param result
	 * @return
	 * @see com.heliosapm.wiex.jmx.threads.IScheduledThreadPoolService#submit(java.lang.Runnable, T)
	 */
	@JMXOperation(description="Submits a Runnable task for execution and returns a Future representing that task that will upon completion return the given result.", name="submit")
	public <T>Future<T> submit(
			@JMXOperationParameter(description="The task to submit.", name="callable") Runnable task,
			@JMXOperationParameter(description="The result to return.", name="result") T result) {
		return ((ScheduledThreadPoolExecutor)threadPool).submit(task, result);
	}


	/**
	 * @param command
	 * @param delay
	 * @param unit
	 * @return
	 * @see java.util.concurrent.ScheduledExecutorService#schedule(java.lang.Runnable, long, java.util.concurrent.TimeUnit)
	 */
	public ScheduledFuture<?> schedule(Runnable command, long delay,
			TimeUnit unit) {
		return ((ScheduledThreadPoolExecutor)threadPool).schedule(command, delay, unit);		
	}


	/**
	 * @param <V>
	 * @param callable
	 * @param delay
	 * @param unit
	 * @return
	 * @see java.util.concurrent.ScheduledExecutorService#schedule(java.util.concurrent.Callable, long, java.util.concurrent.TimeUnit)
	 */
	public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay,
			TimeUnit unit) {
		return ((ScheduledThreadPoolExecutor)threadPool).schedule(callable, delay, unit);
	}
		

}
