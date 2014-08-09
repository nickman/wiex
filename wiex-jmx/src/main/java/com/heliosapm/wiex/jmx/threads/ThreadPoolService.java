/**
 * 
 */
package com.heliosapm.wiex.jmx.threads;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

import com.heliosapm.wiex.jmx.dynamic.ManagedObjectDynamicMBean;
import com.heliosapm.wiex.jmx.dynamic.annotation.JMXAttribute;
import com.heliosapm.wiex.jmx.dynamic.annotation.JMXManagedObject;
import com.heliosapm.wiex.jmx.dynamic.annotation.JMXOperation;
import com.heliosapm.wiex.jmx.dynamic.annotation.JMXOperationParameter;

/**
 * <p>Title: ThreadPoolService</p>
 * <p>Description: A ThreadPoolExecutor Service to provide a centralized and customizable ThreadPool.</p> 
 * <p>Copyright: Copyright (c) 2007</p>
 * <p>Organization: Helios Development Group</p>
 * @author Whitehead
 * @version $Revision: 1.2 $
 */

@JMXManagedObject(declared=true, annotated=true)
public class ThreadPoolService extends ManagedObjectDynamicMBean implements IThreadPoolService, ThreadFactory {
	/** The thread pool that handles the incoming traces from the data queue.*/
	protected ThreadPoolExecutor threadPool = null;
	/** The backing queue for the thread pool. */
	protected BlockingQueue<Runnable> requestQueue = null; 
	/** The capacity of the thread pool backing queue. Defaults to 10000 */
	protected int queueCapacity = 10000;
	/** The core pool size. Defaults to 10 */
	protected int corePoolSize = 10;
	/** The maximum pool size. Defaults to 10 */
	protected int maxPoolSize = 10;
	/** Maximum Thread Keep Alive Time in ms. Defaults to 10000 */
	protected long threadKeepAliveTime = 10000;
	/** Specifies if rejected submissions on account of a full queue are discarded, or causes the oldest task to be discarded to allow the latest */
	protected boolean discardOldest = false;
	/** The name of the thread group backing the pool. Defaults to <JMX Object Name>-ThreadGroup */
	protected String threadGroupName = null;
	/** The thread name prefix for threads generated to back the thread pool. Defaults to  <JMX Object Name>-Thread# */
	protected String threadNamePrefix = null;
	/** Indicates if threads backing the thread pool should be daemon threads. Defaults to true. */
	protected boolean daemonThreads = true;
	/** Specifies a customized stack size for threads created in the thread pool. Uses JVM Default if not specified. */
	protected int threadStackSize = -1;
	/** The ThreadGroup that the pool's threads a grouped in. */
	protected ThreadGroup threadGroup = null;
	/** The priority of the threads in the thread pool. Defaults to <code>Thread.NORM_PRIORITY</code> */
	protected int threadPriority = Thread.NORM_PRIORITY;
	/** The threadPool shutdown time in ms. that allows tasks in the queue to finish before discarding them and finishing the stop */
	protected long shutdownTime = 1;
	/** The number of threads to prestart. Defaults to zero. */
	protected int prestartThreads = 0;
	
	protected StringBuilder messages = new StringBuilder();
	
	protected Logger log = Logger.getLogger(getClass().getName());
	
	protected static AtomicLong threadSequence = new AtomicLong();
	
	protected boolean isRunning = false;
	
	protected RejectedExecutionHandler handler = null;
	
	

	/**
	 * Constructs a new ThreadPoolService.
	 */
	public ThreadPoolService() {
		reflectObject(this);
		log.info("Instantiated " + this.getClass().getName());
	}
	
	/**
	 * @throws Exception
	 * @see com.heliosapm.wiex.jmx.threads.IThreadPoolService#start()
	 */
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
		requestQueue = new ArrayBlockingQueue<Runnable>(queueCapacity);
		log.info("Work Queue Size:" + queueCapacity);
		log.info("Thread Keep Alive Time:" + threadKeepAliveTime);
		log.info("Core Pool Size:" + corePoolSize);
		log.info("Maximum Pool Size:" + maxPoolSize);
		threadPool = new ThreadPoolExecutor(corePoolSize, maxPoolSize, threadKeepAliveTime, TimeUnit.MILLISECONDS, requestQueue, this, handler);
		int prestarted = 0;
		for(int i = 0; i < prestartThreads; i++) {
			if(threadPool.prestartCoreThread()) prestarted++;
		}
		log.info("Prestarted " + prestarted + " Threads.");
		isRunning = true;
		log.info("\n\t================================\n\tStarted Service" + objectName + "\n\t================================");
	}
	
	/**
	 * Sets the discard policy in the threadPool
	 */
	protected void setDiscardPolicy() {
		if(discardOldest) {
			handler = new ThreadPoolExecutor.DiscardOldestPolicy();
		} else {
			handler = new ThreadPoolExecutor.DiscardPolicy();
		}		
	}
	
	/**
	 * 
	 * @see com.heliosapm.wiex.jmx.threads.IThreadPoolService#stop()
	 */
	@JMXOperation(description="Stops the Service", name="stop")
	public void stop() {
		if(!isRunning) return;
		log.info("\n\t================================\n\tStopping Service" + objectName + "\n\t================================");
		boolean cleanShutdown = false;
		int discardedTasks = 0;
		try {
			threadPool.shutdown();
			try {
				log.info("\n\tWaiting " + shutdownTime + " ms. for tasks to complete.");
				if(threadPool.awaitTermination(shutdownTime, TimeUnit.MILLISECONDS)) {
					cleanShutdown = true;
				} else {
					cleanShutdown = false;
				}
			} catch (InterruptedException e) {
				cleanShutdown = false;
			}
		} finally {
			
		}
		if(threadPool.isTerminated()) {
			log.info("\n\tThreadPool Clean Shutdown.");
		} else {
			discardedTasks = threadPool.shutdownNow().size();
			log.info("\n\tThreadPool Shutdown Time Out. Discarded " + discardedTasks + " Tasks.");
		}
		
		isRunning = false;
		log.info("\n\t================================\n\tStopped Service" + objectName + "\n\t================================");
	}
	
	/**
	 * @param message
	 * @param sleepTime
	 * @see com.heliosapm.wiex.jmx.threads.IThreadPoolService#test(java.lang.String, long)
	 */
	@JMXOperation(description="Test Task", name="test")
	public void test(final String message, final long sleepTime) {
		final IThreadPoolService it = this;
		Thread t = new Thread() {
			public void run() {
				it.addMessage(Thread.currentThread().getName() + ":" + message);
				try { Thread.sleep(sleepTime); } catch (InterruptedException e) {}
			}
		};
		threadPool.execute(t);
	}
	
	/**
	 * @param message
	 * @see com.heliosapm.wiex.jmx.threads.IThreadPoolService#addMessage(java.lang.String)
	 */
	public synchronized void addMessage(String message) {
		messages.append("\n").append(message);
	}
	
	/**
	 * @return
	 * @see com.heliosapm.wiex.jmx.threads.IThreadPoolService#printMessageLog()
	 */
	@JMXOperation(description="Displays Messages", name="PrintMessageLog")
	public String printMessageLog() {
		return messages.toString();
	}


	/**
	 * @param r
	 * @return
	 * @see com.heliosapm.wiex.jmx.threads.IThreadPoolService#newThread(java.lang.Runnable)
	 */
	public Thread newThread(Runnable r) {
		Thread t = null;
		if(threadStackSize >0) {
			t = new Thread(threadGroup, r, threadNamePrefix + "-Thread#" + threadSequence.incrementAndGet(), threadStackSize);
		} else {
			t = new Thread(threadGroup, r, threadNamePrefix  + "-Thread#" +  threadSequence.incrementAndGet());
		}		
		t.setPriority(threadPriority);
		t.setDaemon(daemonThreads);
		return t;
	}

	/**
	 * @return
	 * @see com.heliosapm.wiex.jmx.threads.IThreadPoolService#getCorePoolSize()
	 */
	@JMXAttribute(description="The Core Pool Size of the Thread Pool", name="CorePoolSize")
	public int getCorePoolSize() {
		return corePoolSize;
	}

	/**
	 * @param corePoolSize
	 * @see com.heliosapm.wiex.jmx.threads.IThreadPoolService#setCorePoolSize(int)
	 */
	public void setCorePoolSize(int corePoolSize) {
		this.corePoolSize = corePoolSize;
		if(isRunning) threadPool.setCorePoolSize(corePoolSize);
	}

	/**
	 * @return
	 * @see com.heliosapm.wiex.jmx.threads.IThreadPoolService#getDaemonThreads()
	 */
	@JMXAttribute(description="The pool backing threads daemon setting.", name="DaemonThreads")
	public boolean getDaemonThreads() {
		return daemonThreads;
	}

	/**
	 * @param daemonThreads
	 * @see com.heliosapm.wiex.jmx.threads.IThreadPoolService#setDaemonThreads(boolean)
	 */
	public void setDaemonThreads(boolean daemonThreads) {
		this.daemonThreads = daemonThreads;
	}

	/**
	 * @return
	 * @see com.heliosapm.wiex.jmx.threads.IThreadPoolService#getDiscardOldest()
	 */
	@JMXAttribute(description="The discard policy for a full work queue state.", name="DiscardOldest")
	public boolean getDiscardOldest() {
		return discardOldest;
	}

	/**
	 * @param discardOldest
	 * @see com.heliosapm.wiex.jmx.threads.IThreadPoolService#setDiscardOldest(boolean)
	 */
	public void setDiscardOldest(boolean discardOldest) {
		this.discardOldest = discardOldest;
		if(isRunning) setDiscardPolicy();
	}

	/**
	 * @return
	 * @see com.heliosapm.wiex.jmx.threads.IThreadPoolService#getMaxPoolSize()
	 */
	@JMXAttribute(description="The Maximum Pool Size of the Thread Pool", name="MaxPoolSize")
	public int getMaxPoolSize() {
		return maxPoolSize;
	}

	/**
	 * @param maxPoolSize
	 * @see com.heliosapm.wiex.jmx.threads.IThreadPoolService#setMaxPoolSize(int)
	 */
	public void setMaxPoolSize(int maxPoolSize) {
		this.maxPoolSize = maxPoolSize;
		if(isRunning) threadPool.setMaximumPoolSize(maxPoolSize);
	}

	/**
	 * @return
	 * @see com.heliosapm.wiex.jmx.threads.IThreadPoolService#getQueueCapacity()
	 */
	@JMXAttribute(description="The Thread Pool Work Queue Capacity", name="WorkQueueCapacity")
	public int getQueueCapacity() {
		return queueCapacity;
	}

	/**
	 * @param queueCapacity
	 * @see com.heliosapm.wiex.jmx.threads.IThreadPoolService#setQueueCapacity(int)
	 */
	public void setQueueCapacity(int queueCapacity) {
		this.queueCapacity = queueCapacity;
	}

	/**
	 * @return
	 * @see com.heliosapm.wiex.jmx.threads.IThreadPoolService#getThreadGroupName()
	 */
	@JMXAttribute(description="The Thread Group Name", name="ThreadGroupName")
	public String getThreadGroupName() {
		return threadGroupName;
	}

	/**
	 * @param threadGroupName
	 * @see com.heliosapm.wiex.jmx.threads.IThreadPoolService#setThreadGroupName(java.lang.String)
	 */
	public void setThreadGroupName(String threadGroupName) {
		this.threadGroupName = threadGroupName;
	}

	/**
	 * @return
	 * @see com.heliosapm.wiex.jmx.threads.IThreadPoolService#getThreadKeepAliveTime()
	 */
	@JMXAttribute(description="The Keep Alive Time for threads above the core count in ms.", name="ThreadKeepAlive")
	public long getThreadKeepAliveTime() {
		return threadKeepAliveTime;
	}

	/**
	 * @param threadKeepAliveTime
	 * @see com.heliosapm.wiex.jmx.threads.IThreadPoolService#setThreadKeepAliveTime(long)
	 */	
	public void setThreadKeepAliveTime(long threadKeepAliveTime) {
		this.threadKeepAliveTime = threadKeepAliveTime;
		if(isRunning) threadPool.setKeepAliveTime(threadKeepAliveTime, TimeUnit.MILLISECONDS);
	}

	/**
	 * @return
	 * @see com.heliosapm.wiex.jmx.threads.IThreadPoolService#getThreadNamePrefix()
	 */
	@JMXAttribute(description="The prefix to the thread names in the thread pool.", name="ThreadNamePrefix")
	public String getThreadNamePrefix() {
		return threadNamePrefix;
	}

	/**
	 * @param threadNamePrefix
	 * @see com.heliosapm.wiex.jmx.threads.IThreadPoolService#setThreadNamePrefix(java.lang.String)
	 */
	public void setThreadNamePrefix(String threadNamePrefix) {
		this.threadNamePrefix = threadNamePrefix;
	}

	/**
	 * @return
	 * @see com.heliosapm.wiex.jmx.threads.IThreadPoolService#getThreadStackSize()
	 */
	@JMXAttribute(description="The size in bytes of the stack allocated to threads created for the thread pool.", name="ThreadStackSize")
	public int getThreadStackSize() {
		return threadStackSize;
	}

	/**
	 * @param threadStackSize
	 * @see com.heliosapm.wiex.jmx.threads.IThreadPoolService#setThreadStackSize(int)
	 */
	public void setThreadStackSize(int threadStackSize) {
		this.threadStackSize = threadStackSize;
	}

	/**
	 * @return
	 * @see com.heliosapm.wiex.jmx.threads.IThreadPoolService#getThreadPool()
	 */
	@JMXAttribute(description="The thread pool backing the service.", name="ThreadPool")
	public ThreadPoolExecutor getThreadPool() {
		return threadPool;
	}

	/**
	 * @return
	 * @see com.heliosapm.wiex.jmx.threads.IThreadPoolService#getThreadPriority()
	 */
	@JMXAttribute(description="The priority of the threads backing the thread pool.", name="ThreadPriority")	
	public int getThreadPriority() {
		return threadPriority;
	}

	/**
	 * @param threadPriority
	 * @see com.heliosapm.wiex.jmx.threads.IThreadPoolService#setThreadPriority(int)
	 */
	public void setThreadPriority(int threadPriority) {
		this.threadPriority = threadPriority;
	}
	
	/**
	 * @return
	 * @see com.heliosapm.wiex.jmx.threads.IThreadPoolService#getActiveThreadCount()
	 */
	@JMXAttribute(description="An estimate of the number of active threads in the thread group backing the thread pool.", name="ActiveThreadCount")
	public int getActiveThreadCount() {
		if(isRunning) {
			return threadGroup.activeCount();
		} else {
			return 0;
		}
	}
	
	/**
	 * @return
	 * @see com.heliosapm.wiex.jmx.threads.IThreadPoolService#getCompletedTaskCount()
	 */
	@JMXAttribute(description="The approximate total number of tasks that have completed execution.", name="CompletedTasks")
	public long getCompletedTaskCount() {
		if(isRunning) {
			return threadPool.getCompletedTaskCount();
		} else {
			return 0;
		}
	}
	
	/**
	 * @return
	 * @see com.heliosapm.wiex.jmx.threads.IThreadPoolService#getLargestPoolSize()
	 */
	@JMXAttribute(description="The largest number of threads that have ever simultaneously been in the pool.", name="LargestPoolSize")
	public int getLargestPoolSize() {
		if(isRunning) {
			return threadPool.getLargestPoolSize();
		} else {
			return 0;
		}		
	}
	
	/**
	 * @return
	 * @see com.heliosapm.wiex.jmx.threads.IThreadPoolService#getCurrentPoolSize()
	 */
	@JMXAttribute(description="The current number of threads in the pool.", name="CurrentPoolSize")
	public int getCurrentPoolSize() {
		if(isRunning) {
			return threadPool.getPoolSize();
		} else {
			return 0;
		}				
	}
	
	/**
	 * @return
	 * @see com.heliosapm.wiex.jmx.threads.IThreadPoolService#getTaskCount()
	 */
	@JMXAttribute(description="The approximate total number of tasks that have been scheduled for execution.", name="TaskCount")
	public long getTaskCount() {
		if(isRunning) {
			return threadPool.getTaskCount();
		} else {
			return 0;
		}						
	}
	
	/**
	 * @return
	 * @see com.heliosapm.wiex.jmx.threads.IThreadPoolService#getRunning()
	 */
	@JMXAttribute(description="True if the thread pool is running.", name="Running")
	public boolean getRunning() {
		return isRunning;
	}
	
	/**
	 * @return
	 * @see com.heliosapm.wiex.jmx.threads.IThreadPoolService#getQueuedTaskCount()
	 */
	@JMXAttribute(description="The number of tasks in the request queue.", name="QueuedTaskCount")
	public int getQueuedTaskCount() {
		if(requestQueue!=null) {
			return requestQueue.size();
		} else {
			return 0;
		}
	}
	
	
	
	/**
	 * 
	 * @see com.heliosapm.wiex.jmx.threads.IThreadPoolService#purgeFutures()
	 */
	@JMXOperation(description="Purges all cancelled Future Tasks", name="purgeFutures")
	public void purgeFutures() {
		if(isRunning) {
			threadPool.purge();
		}		
	}
	
	/**
	 * 
	 * @see com.heliosapm.wiex.jmx.threads.IThreadPoolService#purgeQueuedTasks()
	 */
	@JMXOperation(description="Purges all scheduled tasks in the queue", name="purgeQueue")
	public void purgeQueuedTasks() {
		if(requestQueue != null) {
			requestQueue.clear();
		}
	}

	/**
	 * @return
	 * @see com.heliosapm.wiex.jmx.threads.IThreadPoolService#getShutdownTime()
	 */
	@JMXAttribute(description="The shutdown time for the thread pool.", name="ShutdownTime")
	public long getShutdownTime() {
		return shutdownTime;
	}

	/**
	 * @param shutdownTime
	 * @see com.heliosapm.wiex.jmx.threads.IThreadPoolService#setShutdownTime(long)
	 */
	public void setShutdownTime(long shutdownTime) {
		this.shutdownTime = shutdownTime;
	}

	/**
	 * @return
	 * @see com.heliosapm.wiex.jmx.threads.IThreadPoolService#getPrestartThreads()
	 */
	@JMXAttribute(description="The number of threads to prestart.", name="PrestartThreads")
	public int getPrestartThreads() {
		return prestartThreads;
	}

	/**
	 * @param prestartThreads
	 * @see com.heliosapm.wiex.jmx.threads.IThreadPoolService#setPrestartThreads(int)
	 */
	public void setPrestartThreads(int prestartThreads) {
		this.prestartThreads = prestartThreads;
	}

	/**
	 * Blocks until all tasks have completed execution after a shutdown request, or the timeout occurs, or the current thread is interrupted, whichever happens first.
	 * @param timeout the maximum time to wait
	 * @param unit the time unit of the timeout argument
	 * @return true if this executor terminated and false  if the timeout elapsed before termination
	 * @throws InterruptedException
	 * @see java.util.concurrent.ExecutorService#awaitTermination(long, java.util.concurrent.TimeUnit)
	 */
	public boolean awaitTermination(long timeout, TimeUnit unit)
			throws InterruptedException {
		return threadPool.awaitTermination(timeout, unit);		
	}


	/**
	 * @return
	 * @see java.util.concurrent.ExecutorService#isShutdown()
	 */
	public boolean isShutdown() {
		return threadPool.isShutdown();
	}

	/**
	 * @return
	 * @see java.util.concurrent.ExecutorService#isTerminated()
	 */
	public boolean isTerminated() {
		return threadPool.isTerminated();
	}

	/**
	 * 
	 * @see java.util.concurrent.ExecutorService#shutdown()
	 */
	public void shutdown() {
		threadPool.shutdown();		
	}

	/**
	 * @return
	 * @see java.util.concurrent.ExecutorService#shutdownNow()
	 */
	public List<Runnable> shutdownNow() {
		return threadPool.shutdownNow();
	}

	/**
	 * Submits a value-returning task for execution and returns a Future representing the pending results of the task.
	 * If you would like to immediately block waiting for a task, you can use constructions of the form result = exec.submit(aCallable).get();
	 * Note: The Executors class includes a set of methods that can convert some other common closure-like objects, for example, PrivilegedAction to Callable form so they can be submitted.
	 * @param task<T> the task to submit
	 * @return a Future representing pending completion of the task
	 * @see java.util.concurrent.ExecutorService#submit(java.util.concurrent.Callable)
	 * @throws RejectedExecutionException - if task cannot be scheduled for execution
	 * @throws NullPointerException - if task null
	 */
	@JMXOperation(description="Submits a value-returning task for execution and returns a Future representing the pending results of the task.", name="submit")
	public <T> Future<T> submit(
			@JMXOperationParameter(description="The task to submit.", name="task") Callable<T> task
			) {
		return threadPool.submit(task);
	}

	/**
	 * @param task
	 * @return
	 * @see java.util.concurrent.ExecutorService#submit(java.lang.Runnable)
	 */
	public Future<?> submit(Runnable task) {
		return threadPool.submit(task);
	}

	/**
	 * @param <T>
	 * @param task
	 * @param result
	 * @return
	 * @see java.util.concurrent.ExecutorService#submit(java.lang.Runnable, java.lang.Object)
	 */
	public <T> Future<T> submit(Runnable task, T result) {
		return threadPool.submit(task, result);
	}
	

	/**
	 * @param command
	 * @see java.util.concurrent.Executor#execute(java.lang.Runnable)
	 */
	public void execute(Runnable command) {
		threadPool.execute(command);
	}

	@Override
	public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks)
			throws InterruptedException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
		return threadPool.invokeAll(tasks, timeout, unit);
	}

	@Override
	public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
		return threadPool.invokeAny(tasks);
	}

	@Override
	public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
		return threadPool.invokeAny(tasks, timeout, unit);
	}

}
