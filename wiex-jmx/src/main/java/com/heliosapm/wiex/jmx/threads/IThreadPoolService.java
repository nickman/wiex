package com.heliosapm.wiex.jmx.threads;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;

import com.heliosapm.wiex.jmx.dynamic.annotation.JMXAttribute;
import com.heliosapm.wiex.jmx.dynamic.annotation.JMXOperation;

/**
 * <p>Title: IThreadPoolService</p>
 * <p>Description: Interface defining the ThreadPoolService class. Provided so that MBeanServerDelegates can be created for this MBean.</p> 
 * <p>Copyright: Copyright (c) 2007</p>
 * <p>Organization: Helios Development Group</p>
 * @author Whitehead
 * @version $Revision: 1.1 $
 */

public interface IThreadPoolService extends ExecutorService {

	/**
	 * Starts the ThreadPoolService
	 * @throws Exception
	 */
	@JMXOperation(description = "Starts the Service", name = "start")
	public abstract void start() throws Exception;

	/**
	 * Stops the ThreadPoolService
	 */
	@JMXOperation(description = "Stops the Service", name = "stop")
	public abstract void stop();

	/**
	 * Logs a test message to the message log through a scheduled task in the thread pool
	 * @param message The message to log.
	 * @param sleepTime The sleep time after the log in ms.
	 */
	@JMXOperation(description = "Test Task", name = "test")
	public abstract void test(final String message, final long sleepTime);

	/**
	 * Adds a message to the message log.
	 * @param message The message.
	 */
	public abstract void addMessage(String message);

	/**
	 * Displays the message log.
	 * @return the message log
	 */
	@JMXOperation(description = "Displays Messages", name = "PrintMessageLog")
	public abstract String printMessageLog();

	/**
	 * Creates a new thread for the thread pool.
	 * @param r The runnable that will be executed by the thread.
	 * @return A Thread.
	 * @see java.util.concurrent.ThreadFactory#newThread(java.lang.Runnable)
	 */
	public abstract Thread newThread(Runnable r);

	/**
	 * Gets the core number of threads.
	 * @return the corePoolSize
	 */
	@JMXAttribute(description = "The Core Pool Size of the Thread Pool", name = "CorePoolSize")
	public abstract int getCorePoolSize();

	/**
	 * Sets the core number of threads.
	 * This sets the initial core size on service startup and then updates the core size in the running pool. 
	 * If the new value is smaller than the current value, excess existing threads will be terminated when they next become idle. 
	 * If larger, new threads will, if needed, be started to execute any queued tasks. 
	 * @param corePoolSize the corePoolSize to set
	 */
	public abstract void setCorePoolSize(int corePoolSize);

	/**
	 * Returns true if the pool's backing threads are daemons. 
	 * @return if pool's threads are daemon threads; false otherwise.
	 */
	@JMXAttribute(description = "The pool backing threads daemon setting.", name = "DaemonThreads")
	public abstract boolean getDaemonThreads();

	/**
	 * Marks the pool's backing threads as either daemon threads or user threads.
	 * If the value is changed once the pool has started, future created threads will adopt the new setting.
	 * @param daemonThreads if true, pool threads are marked as daemon threads.
	 */
	public abstract void setDaemonThreads(boolean daemonThreads);

	/**
	 * Returns the discard policy for the thread pool.
	 * @return true for discard oldest, false for discard new silently.
	 */
	@JMXAttribute(description = "The discard policy for a full work queue state.", name = "DiscardOldest")
	public abstract boolean getDiscardOldest();

	/**
	 * Sets the pool work queue discard policy.
	 * Defines the behaviour of the thread pools task acceptance when the work queue is full.
	 * If true, the work queue will discard the oldest task to make room for the new one.
	 * If false, the new task will be silently discarded. 
	 * @param discardOldest true for discard oldest, false for discard new silently.
	 */
	public abstract void setDiscardOldest(boolean discardOldest);

	/**
	 * Returns the maximum allowed number of threads in the thread pool.
	 * @return the maximum allowed number of threads.
	 */
	@JMXAttribute(description = "The Maximum Pool Size of the Thread Pool", name = "MaxPoolSize")
	public abstract int getMaxPoolSize();

	/** 
	 * Sets the maximum allowed number of threads.
	 * This sets the initial maximum pool size on service startup and then updates the maximum size in the running pool. 
	 * If the new value is smaller than the current value, excess existing threads will be terminated when they next become idle.   
	 * @param maxPoolSize the maximum pool size
	 */
	public abstract void setMaxPoolSize(int maxPoolSize);

	/**
	 * The fixed capacity of the backing work queue for the thread pool. 
	 * @return the queue capacity
	 */
	@JMXAttribute(description = "The Thread Pool Work Queue Capacity", name = "WorkQueueCapacity")
	public abstract int getQueueCapacity();

	/**
	 * Sets the fixed capacity of the backing work queue for the thread pool.
	 * @param queueCapacity the queueCapacity to set
	 */
	public abstract void setQueueCapacity(int queueCapacity);

	/**
	 * The name of the thread group for the thread pool backing threads.
	 * @return the thread group name
	 */
	@JMXAttribute(description = "The Thread Group Name", name = "ThreadGroupName")
	public abstract String getThreadGroupName();

	/**
	 * Sets the name of the thread group for the thread pool backing threads.
	 * @param threadGroupName the thread Group Name
	 */
	public abstract void setThreadGroupName(String threadGroupName);

	/**
	 * The keep alive time in ms. for threads above the core size.
	 * @return the threadKeepAliveTime
	 */
	@JMXAttribute(description = "The Keep Alive Time for threads above the core count in ms.", name = "ThreadKeepAlive")
	public abstract long getThreadKeepAliveTime();

	/**
	 * When the number of threads in the pool is greater than the core, 
	 * this is the maximum time that excess idle threads will wait for new tasks before terminating. 
	 * @param threadKeepAliveTime the threadKeepAliveTime to set
	 */
	public abstract void setThreadKeepAliveTime(long threadKeepAliveTime);

	/**
	 * The prefix to the thread names in the thread pool.
	 * @return the threadNamePrefix
	 */
	@JMXAttribute(description = "The prefix to the thread names in the thread pool.", name = "ThreadNamePrefix")
	public abstract String getThreadNamePrefix();

	/**
	 * Sets the prefix to the thread names in the thread pool.
	 * @param threadNamePrefix the threadNamePrefix to set
	 */
	public abstract void setThreadNamePrefix(String threadNamePrefix);

	/**
	 * The size in bytes of the stack allocated to threads created for the thread pool. 
	 * @return the threadStackSize
	 */
	@JMXAttribute(description = "The size in bytes of the stack allocated to threads created for the thread pool.", name = "ThreadStackSize")
	public abstract int getThreadStackSize();

	/**
	 * Sets the size in bytes of the stack allocated to threads created for the thread pool.
	 * If the value is -1, the platform default is used.
	 * @param threadStackSize the threadStackSize to set
	 */
	public abstract void setThreadStackSize(int threadStackSize);

	/**
	 * The thread pool backing the service.
	 * @return the threadPool
	 */
	@JMXAttribute(description = "The thread pool backing the service.", name = "ThreadPool")
	public abstract ThreadPoolExecutor getThreadPool();

	/**
	 * The priority of the threads in the thread pool. Defaults to <code>Thread.NORM_PRIORITY</code>.
	 * @return the threadPriority
	 */
	@JMXAttribute(description = "The priority of the threads backing the thread pool.", name = "ThreadPriority")
	public abstract int getThreadPriority();

	/**
	 * Sets the priority of the threads in the thread pool. Defaults to <code>Thread.NORM_PRIORITY</code>
	 * @param threadPriority the threadPriority to set
	 */
	public abstract void setThreadPriority(int threadPriority);

	/**
	 * Returns an estimate of the number of active threads in the thread group backing the thread pool.
	 * @return active Thread count.
	 */
	@JMXAttribute(description = "An estimate of the number of active threads in the thread group backing the thread pool.", name = "ActiveThreadCount")
	public abstract int getActiveThreadCount();

	/**
	 * Returns the approximate total number of tasks that have completed execution. 
	 * Because the states of tasks and threads may change dynamically during computation, 
	 * the returned value is only an approximation, but one that does not ever decrease across successive calls.
	 * @return the number of tasks
	 */
	@JMXAttribute(description = "The approximate total number of tasks that have completed execution.", name = "CompletedTasks")
	public abstract long getCompletedTaskCount();

	/**
	 * Returns the largest number of threads that have ever simultaneously been in the pool.
	 * @return the number of threads
	 */
	@JMXAttribute(description = "The largest number of threads that have ever simultaneously been in the pool.", name = "LargestPoolSize")
	public abstract int getLargestPoolSize();

	/**
	 * Returns the current number of threads in the pool.
	 * @return the number of threads
	 */
	@JMXAttribute(description = "The current number of threads in the pool.", name = "CurrentPoolSize")
	public abstract int getCurrentPoolSize();

	/**
	 * Returns the approximate total number of tasks that have been scheduled for execution.
	 * @return the number of tasks
	 */
	@JMXAttribute(description = "The approximate total number of tasks that have been scheduled for execution.", name = "TaskCount")
	public abstract long getTaskCount();

	/**
	 * Returns true if the thread pool service is running.
	 * @return true if running
	 */
	@JMXAttribute(description = "True if the thread pool is running.", name = "Running")
	public abstract boolean getRunning();

	/**
	 * Returns the number of tasks in the request queue.
	 * @return the number of tasks.
	 */
	@JMXAttribute(description = "The number of tasks in the request queue.", name = "QueuedTaskCount")
	public abstract int getQueuedTaskCount();

	/**
	 * Tries to remove from the work queue all <code>Future</code> tasks that have been cancelled.
	 */
	@JMXOperation(description = "Purges all cancelled Future Tasks", name = "purgeFutures")
	public abstract void purgeFutures();

	/**
	 * Clears the currently queued tasks.
	 */
	@JMXOperation(description = "Purges all scheduled tasks in the queue", name = "purgeQueue")
	public abstract void purgeQueuedTasks();

	/**
	 * The time that the threadPool is allowed to attempt to flush remaing queued tasks after the stop command.
	 * Once this time expires, the remaining tasks will be discarded.
	 * @return the shutdownTime
	 */
	@JMXAttribute(description = "The shutdown time for the thread pool.", name = "ShutdownTime")
	public abstract long getShutdownTime();

	/**
	 * Sets the shutdown time.
	 * @param shutdownTime the shutdownTime to set
	 */
	public abstract void setShutdownTime(long shutdownTime);

	/**
	 * The number of threads to prestart. 
	 * @return the prestartThreads
	 */
	@JMXAttribute(description = "The number of threads to prestart.", name = "PrestartThreads")
	public abstract int getPrestartThreads();

	/**
	 * Sets the number of threads to prestart.
	 * @param prestartThreads the prestartThreads to set
	 */
	public abstract void setPrestartThreads(int prestartThreads);

}