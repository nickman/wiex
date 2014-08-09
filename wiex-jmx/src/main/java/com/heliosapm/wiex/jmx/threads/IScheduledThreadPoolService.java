package com.heliosapm.wiex.jmx.threads;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.heliosapm.wiex.jmx.dynamic.annotation.JMXOperation;
import com.heliosapm.wiex.jmx.dynamic.annotation.JMXOperationParameter;

/**
 * <p>Title: IScheduledThreadPoolService</p>
 * <p>Description: Interface defining the ScheduledThreadPoolService class. Provided so that MBeanServerDelegates can be created for this MBean.</p> 
 * <p>Copyright: Copyright (c) 2007</p>
 * <p>Organization: Helios Development Group</p>
 * @author Whitehead
 * @version $Revision: 1.1 $
 */

public interface IScheduledThreadPoolService extends ScheduledExecutorService {

	/**
	 * Starts the ScheduledThreadPoolService
	 * @throws Exception
	 */
	@JMXOperation(description = "Starts the Service", name = "start")
	public abstract void start() throws Exception;

	/**
	 * Removes this task from the executor's internal queue if it is present, thus causing it not to be run if it has not already started.
	 * @param task the task to remove
	 * @return true if the task was removed
	 */
	@JMXOperation(description = "Removes this task from the pool", name = "remove")
	public abstract boolean remove(
			@JMXOperationParameter(description = "The task to remove", name = "task")
			Runnable task);

	/**
	 * Creates and executes a ScheduledFuture that becomes enabled after the given delay.
	 * @param callable the function to execute.
	 * @param delay the time from now to delay execution.
	 * @param unit the time unit of the delay parameter.
	 * @return a ScheduledFuture that can be used to extract result or cancel.
	 */
	@JMXOperation(description = "Creates and executes a ScheduledFuture that becomes enabled after the given delay.", name = "scheduleCallable")
	public abstract <V> ScheduledFuture<V> scheduleCallable(
			@JMXOperationParameter(description = "The function to execute.", name = "callable")
			Callable<V> callable,
			@JMXOperationParameter(description = "The time from now to delay execution.", name = "delay")
			long delay,
			@JMXOperationParameter(description = "The time unit of the delay parameter.", name = "unit")
			TimeUnit unit);

	/**
	 * Creates and executes a one-shot action that becomes enabled after the given delay.
	 * @param command the task to execute.
	 * @param delay the time from now to delay execution.
	 * @param unit the time unit of the delay parameter.
	 * @return a Future representing pending completion of the task, and whose get() method will return null  upon completion.
	 */
	@JMXOperation(description = "Creates and executes a one-shot action that becomes enabled after the given delay.", name = "scheduleRunnable")
	public abstract ScheduledFuture<?> scheduleRunnable(
			@JMXOperationParameter(description = "The task to execute.", name = "command")
			Runnable command,
			@JMXOperationParameter(description = "The time from now to delay execution.", name = "delay")
			long delay,
			@JMXOperationParameter(description = "The time unit of the delay parameter.", name = "unit")
			TimeUnit unit);

	/**
	 * Creates and executes a periodic action that becomes enabled first after 
	 * the given initial delay, and subsequently with the given period; 
	 * that is executions will commence after initialDelay then initialDelay+period, then initialDelay + 2 * period, and so on.
	 * @param command the task to execute.
	 * @param initialDelay the time to delay first execution.
	 * @param period the period between successive executions.
	 * @param unit the time unit of the initialDelay and period parameters
	 * @return a Future representing pending completion of the task, and whose get() method will throw an exception upon cancellation.
	 */
	@JMXOperation(description = "Creates and executes a periodic action that becomes enabled first after the given initial delay", name = "scheduleAtFixedRate")
	public abstract ScheduledFuture<?> scheduleAtFixedRate(
			@JMXOperationParameter(description = "The task to execute.", name = "command")
			Runnable command,
			@JMXOperationParameter(description = "The time to delay first execution.", name = "delay")
			long initialDelay,
			@JMXOperationParameter(description = "The period between successive executions.", name = "period")
			long period,
			@JMXOperationParameter(description = "The time unit of the delay parameter.", name = "unit")
			TimeUnit unit);

	/**
	 * Creates and executes a periodic action that becomes enabled first after the given initial delay, 
	 * and subsequently with the given delay between the termination of one execution and the commencement of the next.
	 * @param command
	 * @param initialDelay
	 * @param period
	 * @param unit
	 * @return
	 */
	@JMXOperation(description = "Creates and executes a periodic action that becomes enabled first after the given initial delay", name = "scheduleWithFixedDelay")
	public abstract ScheduledFuture<?> scheduleWithFixedDelay(
			@JMXOperationParameter(description = "The task to execute.", name = "command")
			Runnable command,
			@JMXOperationParameter(description = "The time to delay first execution.", name = "delay")
			long initialDelay,
			@JMXOperationParameter(description = "The period between successive executions.", name = "period")
			long period,
			@JMXOperationParameter(description = "The time unit of the delay parameter.", name = "unit")
			TimeUnit unit);

	/**
	 * Submits a value-returning task for execution and returns a Future representing the pending results of the task.
	 * @param task the task to submit
	 * @return a Future representing pending completion of the task
	 */
	@JMXOperation(description = "Submits a value-returning task for execution and returns a Future representing the pending results of the task.", name = "submitCallable")
	public abstract <T> Future<T> submitCallable(
			@JMXOperationParameter(description = "The task to submit.", name = "callable")
			Callable<T> task);

	/**
	 * Submits a Runnable task for execution and returns a Future representing that task.
	 * @param task the task to submit 
	 * @return a Future representing pending completion of the task, and whose get() method will return null  upon completion.
	 */
	@JMXOperation(description = "Submits a Runnable task for execution and returns a Future representing that task.", name = "submitRunnable")
	public abstract Future<?> submitRunnable(
			@JMXOperationParameter(description = "The task to submit.", name = "callable")
			Runnable task);

	/**
	 * Submits a Runnable task for execution and returns a Future representing that task that will upon completion return the given result.
	 * @param task the task to submit
	 * @param result the result to return
	 * @return a Future representing pending completion of the task, and whose get() method will return the given result upon completion.
	 */
	@JMXOperation(description = "Submits a Runnable task for execution and returns a Future representing that task that will upon completion return the given result.", name = "submit")
	public abstract <T> Future<T> submit(
			@JMXOperationParameter(description = "The task to submit.", name = "callable")
			Runnable task,
			@JMXOperationParameter(description = "The result to return.", name = "result")
			T result);

}