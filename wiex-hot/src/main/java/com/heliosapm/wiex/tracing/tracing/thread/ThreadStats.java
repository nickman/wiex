package com.heliosapm.wiex.tracing.tracing.thread;

import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;

/**
 * <p>Title: ThreadStats</p>
 * <p>Description: Holds one set of Java 1.5+ Thread Stats</p>
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Organization: Helios Development Group</p>
 * @author Whitehead
 * @version $Revision: 1.2 $ 
 */
public class ThreadStats {
	
	/** The CPU time in nanoseconds used by a thread */
	protected long cpuTime = 0L;
	/** The User CPU time in nanoseconds used by a thread */
	protected long userCpuTime = 0L;
	/** The current time in millseconds */
	protected long elapsedTime = 0L;
	/** The number of times a thread has waited */
	protected long waitTime = 0L;
	/** The total amount of time a thread has waited */
	protected long waitCount = 0L;
	/** The number of times a thread has been blocked */
	protected long blockTime = 0L;
	/** The total amount of time a thread has been blocked */
	protected long blockCount = 0L;
	/** Indicates if the ThreadStats has been diffed */
	protected boolean isDiffed = false;
	
	public static final String CPU = "CPU Time (ns.)";
	public static final String USER_CPU = "User CPU Time (ns.)";
	public static final String ELAPSED = "Elapsed Time (ms.)";
	public static final String WAIT_TIME = "Wait Time (ms.)";
	public static final String BLOCK_TIME = "Block Time (ms.)";
	public static final String WAIT_COUNT = "Wait Count";
	public static final String BLOCK_COUNT = "Block Count";
	
	
	/**
	 * Calculates the difference between one reading and another.
	 * @param l1
	 * @param l2
	 * @return The delta between l1 and l2.
	 */
	protected long diff(long l1, long l2) {
		long temp =  l2-l1;
		return temp;
	}
	
	
	/**
	 * Updates the current ThreadStats to be set to the delta between
	 * these readings and those of a passed in ThreadStats.
	 * @param ts
	 */
	public void delta(ThreadStats ts) {
		
		this.cpuTime = diff(this.cpuTime, ts.cpuTime);
		this.userCpuTime = diff(this.userCpuTime, ts.userCpuTime);
		
		this.elapsedTime = diff(this.elapsedTime, ts.elapsedTime);
		
		this.waitTime = diff(this.waitTime, ts.waitTime);
		this.waitCount = diff(this.waitCount, ts.waitCount);
		
		this.blockTime = diff(this.blockTime, ts.blockTime);
		this.blockCount = diff(this.blockCount, ts.blockCount);
		
		isDiffed = true;
		
	}
	
	/**
	 * Updates the thread stats with current values.
	 * This undiffs the ThreadStatus.
	 * @param threadMXBean A reference to the current VM's ThreadMXBean.
	 */
	public void update(ThreadMXBean threadMXBean) {
		ThreadInfo ti = threadMXBean.getThreadInfo(Thread.currentThread().getId());
		elapsedTime = System.currentTimeMillis();
		waitCount = ti.getWaitedCount();	
		blockCount = ti.getBlockedCount();
		if(threadMXBean.isThreadContentionMonitoringEnabled()) {		
			waitTime = ti.getWaitedTime();
			blockTime = ti.getBlockedTime();
		} else {
			waitTime = 0;
			blockTime = 0;			
		}
		if(threadMXBean.isThreadCpuTimeEnabled()) {
			userCpuTime = threadMXBean.getCurrentThreadUserTime();
			cpuTime = threadMXBean.getCurrentThreadCpuTime();
		} else {
			userCpuTime = 0;
			cpuTime = 0;			
		}
		
		isDiffed = false;
	}
	
	/**
	 * Creates a new ThreadStats and populates current point in time thread readings for the current thread.
	 * @param threadMXBean A reference to the current VM's ThreadMXBean
	 * @return An undiffed current ThreadStats
	 */
	public static ThreadStats getInstance(ThreadMXBean threadMXBean) {
		return getInstance(threadMXBean, Thread.currentThread().getId());
	}
	
	/**
	 * Creates a new ThreadStats and populates current point in time thread readings for the thread Id passed in.
	 * @param threadMXBean A reference to the current VM's ThreadMXBean
	 * @param threadId The thrad Id of the thread to collects stats for.
	 * @return An undiffed current ThreadStats for the passed threadId.
	 */
	public static ThreadStats getInstance(ThreadMXBean threadMXBean, long threadId) {
		ThreadStats ts = new ThreadStats();
		ThreadInfo ti = threadMXBean.getThreadInfo(threadId);
		ts.setElapsedTime(System.currentTimeMillis());
		ts.setWaitCount(ti.getWaitedCount());	
		ts.setBlockCount(ti.getBlockedCount());
		if(threadMXBean.isThreadContentionMonitoringEnabled()) {		
			ts.setWaitTime(ti.getWaitedTime());
			ts.setBlockTime(ti.getBlockedTime());
		}
		if(threadMXBean.isThreadCpuTimeEnabled()) {
			ts.setUserCpuTime(threadMXBean.getThreadUserTime(threadId));
			ts.setCpuTime(threadMXBean.getThreadCpuTime(threadId));
		}						
		return ts;		
	}
	
	
	/**
	 * The number of times a thread has been blocked.
	 * @return the block count of the thread
	 */
	public long getBlockCount() {
		return blockCount;
	}
		
	/**
	 * Sets the block count for a thread.
	 * @param blockCount The number of times a thread has been blocked.
	 */
	public void setBlockCount(long blockCount) {
		this.blockCount = blockCount;		
	}
	
	/**
	 * The amount of time in ms a thread has been blocked.
	 * @return the block time of the thread
	 */
	public long getBlockTime() {
		return blockTime;
	}
	
	/**
	 * Sets the block time in ms. for a thread.
	 * @param blockTime The amount of time a thread has been blocked.
	 */
	public void setBlockTime(long blockTime) {
		this.blockTime = blockTime;
	}
	
	/**
	 * Gets the amount of CPU time in nanoseconds that the thread has used.
	 * @return The CPU time of the thread.
	 */
	public long getCpuTime() {
		return cpuTime;
	}
	/**
	 * Sets the amount of CPU time in nanoseconds that the thread has used.
	 * @param cpuTime The CPU time of the thread.
	 */
	public void setCpuTime(long cpuTime) {
		this.cpuTime = cpuTime;
	}
	
	
	/**
	 * Returns the timestamp of the thread readings before a diff.
	 * Returns the elapsed time between reafings after a diff.
	 * @return A time stamp or elapsed time.
	 */
	public long getElapsedTime() {
		return elapsedTime;
	}
	
	/**
	 * Sets the timestamp of the thread readings before a diff.
	 * @param elapsedTime A current time stamp.
	 */	
	public void setElapsedTime(long elapsedTime) {
		this.elapsedTime = elapsedTime;
	}

	/**
	 * Gets the amount of User CPU time in nanoseconds that the thread has used.
	 * @return The User CPU time of the thread.
	 */	
	public long getUserCpuTime() {
		return userCpuTime;
	}
	
	/**
	 * Sets the amount of User CPU time in nanoseconds that the thread has used.
	 * @param userCpuTime The User CPU time of the thread.
	 */	
	public void setUserCpuTime(long userCpuTime) {
		this.userCpuTime = userCpuTime;
	}
	
	
	/**
	 * The number of times a thread has waited.
	 * @return the wait count of the thread
	 */	
	public long getWaitCount() {
		return waitCount;
	}
	
	/**
	 * Sets the wait count for a thread.
	 * @param waitCount The number of times a thread has waited.
	 */	
	public void setWaitCount(long waitCount) {
		this.waitCount = waitCount;
	}
	
	/**
	 * The amount of time in ms a thread has waited.
	 * @return the wait time of the thread
	 */	
	public long getWaitTime() {
		return waitTime;
	}

	/**
	 * Sets the wait time in ms. for a thread.
	 * @param waitTime The amount of time a thread has waited.
	 */	
	public void setWaitTime(long waitTime) {
		this.waitTime = waitTime;
	}


	/**
	 * Returns true if the ThreadStats has been diffed.
	 * If this is false, the values are single point in time readings.
	 * If this is true, the values are deltas between two seperate readings.
	 * @return isDiffed true if readings are deltas, false if they are absolute.
	 */
	public boolean isDiffed() {
		return isDiffed;
	}


	/**
	 * Sets the diff status of the ThreadStats.
	 * @param isDiffed true if readings are deltas, false if they are absolute.
	 */
	public void setDiffed(boolean isDiffed) {
		this.isDiffed = isDiffed;
	}


	/**
	 * Generates a readable representation of the current ThreadStats. 
	 * @return A readable string.
	 */
	public String toString() {
		StringBuilder buffer = new StringBuilder("ThreadStats[");		
		buffer.append("\n\tisDiffed = ").append(isDiffed);
		buffer.append("\n\tblockCount = ").append(blockCount);
		buffer.append("\n\tblockTime = ").append(blockTime);
		buffer.append("\n\tcpuTime = ").append(cpuTime);
		buffer.append("\n\telapsedTime = ").append(elapsedTime);		
		buffer.append("\n\tuserCpuTime = ").append(userCpuTime);
		buffer.append("\n\twaitCount = ").append(waitCount);
		buffer.append("\n\twaitTime = ").append(waitTime);
		buffer.append("\n]");
		return buffer.toString();
	}
}

