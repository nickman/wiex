/**
 * 
 */
package com.heliosapm.wiex.server.tracing.collectors.hibernate;

import javax.management.ObjectName;
import javax.naming.Context;
import javax.naming.InitialContext;

import org.hibernate.SessionFactory;
import org.hibernate.stat.CollectionStatistics;
import org.hibernate.stat.EntityStatistics;
import org.hibernate.stat.SecondLevelCacheStatistics;
import org.hibernate.stat.Statistics;

import com.heliosapm.wiex.jmx.dynamic.annotation.JMXAttribute;
import com.heliosapm.wiex.jmx.dynamic.annotation.JMXManagedObject;
import com.heliosapm.wiex.jmx.dynamic.annotation.JMXOperation;
import com.heliosapm.wiex.server.tracing.collectors.BaseCollector;

/**
 * <p>Title: HibernateCollector</p>
 * <p>Description: Collects Hibernate Statistics</p> 
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Company: Helios Development Group</p>
 * @author Whitehead
 * @version $Revision: 1.4 $
 */
@JMXManagedObject(annotated=true, declared=false)
public class HibernateCollector extends BaseCollector {

	/**	The JNDI Name of the target session factory */
	protected String sessionFactoryName = null;
	/**	The Name of the target session factory */
	protected String factoryName = null;
	/**	The Root segment name */
	protected String segmentRoot = null;
	
	/**	The target session factory */
	protected SessionFactory sessionFactory = null;
	/** The number of collections to take before reseting */
	protected int resetCount = 100;
	/** The number of collections taken since reset */
	protected int collectionsSinceReset = 0;
	
	
	
	
	
	/**
	 * Constructs a new HibernateCollector
	 */
	public HibernateCollector() {
		super();
	}
	
	/**
	 * Initializes the VERSION and MODULE.
	 * @see com.heliosapm.wiex.server.collectors.BaseCollector#init()
	 */
	public void init() {
		VERSION = "$Revision: 1.4 $";
		MODULE = "HibernateCollector";
	}		
	
	
	/**
	 * 
	 * @see com.heliosapm.wiex.server.collectors.BaseCollector#collect()
	 */
	@Override
	@JMXOperation(description="Collects Hibernate Stats", expose=true, name="collect")
	public void collect() {
		long start = System.currentTimeMillis();
		Statistics statistics = null;
		CollectionStatistics cs = null;
		EntityStatistics es = null;
		String collectionRoot = null;
		String collectionName = null;
		long hitCount = 0L;
		long missCount = 0L;
		long hitRatio = 0L;

		try {
			if(sessionFactory==null) {
				Context ctx = null;
				try {
					ctx = new InitialContext();
					sessionFactory = (SessionFactory)ctx.lookup(sessionFactoryName);
				} catch (Exception e) {
					// TODO: handle exception
				} finally {
					try { ctx.close(); } catch (Exception e) {}
				}
			}
			if(sessionFactory==null) return;
			statistics = sessionFactory.getStatistics();
			if(!statistics.isStatisticsEnabled()) {
				statistics.setStatisticsEnabled(true);
			}
			tracer.recordCounterMetric(segmentRoot, "CloseStatementCount", statistics.getCloseStatementCount());
			tracer.recordCounterMetric(segmentRoot, "CollectionFetchCount", statistics.getCollectionFetchCount());
			tracer.recordCounterMetric(segmentRoot, "CollectionLoadCount", statistics.getCollectionLoadCount());
			tracer.recordCounterMetric(segmentRoot, "CollectionRecreateCount", statistics.getCollectionRecreateCount());
			tracer.recordCounterMetric(segmentRoot, "CollectionRemoveCount", statistics.getCollectionRemoveCount());			
			String[] roles = statistics.getCollectionRoleNames();
			if(roles!=null) {
				collectionRoot = tracer.buildSegment(false, segmentRoot,"CollectionStatistics");
				for(String roleName: roles) {
					cs = statistics.getCollectionStatistics(roleName);
					collectionName = tracer.buildSegment(collectionRoot , roleName);
					//tracer.recordMetric(collectionName, "Category", cs.getCategoryName());
					tracer.recordCounterMetric(collectionName, "FetchCount", cs.getFetchCount());
					tracer.recordCounterMetric(collectionName, "LoadCount", cs.getLoadCount());
					tracer.recordCounterMetric(collectionName, "RecreateCount", cs.getRecreateCount());
					tracer.recordCounterMetric(collectionName, "RemoveCount", cs.getRemoveCount());
					tracer.recordCounterMetric(collectionName, "UpdateCount", cs.getUpdateCount());
				}
			}
			tracer.recordCounterMetric(segmentRoot, "CollectionUpdateCount", statistics.getCollectionUpdateCount());
			tracer.recordCounterMetric(segmentRoot, "ConnectCount", statistics.getConnectCount());
			tracer.recordCounterMetric(segmentRoot, "EntityDeleteCount", statistics.getEntityDeleteCount());
			tracer.recordCounterMetric(segmentRoot, "EntityFetchCount", statistics.getEntityFetchCount());
			tracer.recordCounterMetric(segmentRoot, "EntityInsertCount", statistics.getEntityInsertCount());
			tracer.recordCounterMetric(segmentRoot, "EntityLoadCount", statistics.getEntityLoadCount());
			String[] entities = statistics.getEntityNames();
			if(entities!=null) {
				collectionRoot = tracer.buildSegment(false, segmentRoot,"EntityStatistics");
				for(String entityName: entities) {
					es = statistics.getEntityStatistics(entityName);					
					collectionName = tracer.buildSegment(collectionRoot , entityName);
					//tracer.recordMetric(entityName, "Category", es.getCategoryName());
					tracer.recordCounterMetric(collectionName, "DeleteCount", es.getDeleteCount());
					tracer.recordCounterMetric(collectionName, "FetchCount", es.getFetchCount());
					tracer.recordCounterMetric(collectionName, "InsertCount", es.getInsertCount());
					tracer.recordCounterMetric(collectionName, "LoadCount", es.getLoadCount());
					//tracer.recordCounterMetric(collectionName, "OptimisticFailureCount", es.getOptimisticFailureCount());				
					tracer.recordCounterMetric(collectionName, "UpdateCount", es.getUpdateCount());
				}
			}
			tracer.recordCounterMetric(segmentRoot, "EntityUpdateCount", statistics.getEntityUpdateCount());
			tracer.recordCounterMetric(segmentRoot, "FlushCount", statistics.getFlushCount());
			//tracer.recordCounterMetric(segmentRoot, "OptimisticFailureCount", statistics.getOptimisticFailureCount());
			tracer.recordCounterMetric(segmentRoot, "PrepareStatementCount", statistics.getPrepareStatementCount());
			hitCount = statistics.getQueryCacheHitCount();
			missCount = statistics.getQueryCacheMissCount();
			try {
				hitRatio = hitCount / (hitCount + missCount);
			} catch (Exception e) {
				hitRatio = 0;
			}
			
			tracer.recordCounterMetric(segmentRoot, "QueryCacheHitCount", hitCount);			
			tracer.recordCounterMetric(segmentRoot, "QueryCacheMissCount", missCount);
			tracer.recordCounterMetric(segmentRoot, "QueryCacheHitRatio", hitRatio);			
			tracer.recordCounterMetric(segmentRoot, "QueryCachePutCount", statistics.getQueryCachePutCount());
			tracer.recordCounterMetric(segmentRoot, "QueryExecutionCount", statistics.getQueryExecutionCount());
			tracer.recordCounterMetric(segmentRoot, "QueryExecutionMaxTime", statistics.getQueryExecutionMaxTime());
//			String slowestSQL = statistics.getQueryExecutionMaxTimeQueryString();
//			if(slowestSQL != null) {
//				collectionRoot = tracer.getStringBuilder().append(segmentRoot).append(delim).append("SlowestSQL").toString();
//				tracer.recordMetric(collectionRoot, "QueryExecutionMaxTimeQueryString", slowestSQL);
//				QueryStatistics qs = statistics.getQueryStatistics(slowestSQL);
//				tracer.recordCounterMetric(collectionRoot, "CacheHitCount", qs.getCacheHitCount());
//				tracer.recordCounterMetric(collectionRoot, "CacheMissCount", qs.getCacheMissCount());
//				tracer.recordCounterMetric(collectionRoot, "CachePutCount", qs.getCachePutCount());
//				tracer.recordCounterMetric(collectionRoot, "ExecutionAvgTime", qs.getExecutionAvgTime());
//				tracer.recordCounterMetric(collectionRoot, "ExecutionCount", qs.getExecutionCount());
//				tracer.recordCounterMetric(collectionRoot, "ExecutionMaxTime", qs.getExecutionMaxTime());
//				tracer.recordCounterMetric(collectionRoot, "ExecutionMinTime", qs.getExecutionMinTime());
//				tracer.recordCounterMetric(collectionRoot, "ExecutionRowCount", qs.getExecutionRowCount());
//			}
			hitCount = statistics.getSecondLevelCacheHitCount();
			missCount = statistics.getSecondLevelCacheMissCount();
			try {
				hitRatio = hitCount / (hitCount + missCount);
			} catch (Exception e) {
				hitRatio = 0;
			}			
			tracer.recordCounterMetric(segmentRoot, "SecondLevelCacheHitCount", hitCount);
			tracer.recordCounterMetric(segmentRoot, "SecondLevelCacheMissCount", missCount);
			tracer.recordCounterMetric(segmentRoot, "SecondLevelCacheHitRatio", hitRatio);
			tracer.recordCounterMetric(segmentRoot, "SecondLevelCachePutCount", statistics.getSecondLevelCachePutCount());
			collectionRoot = tracer.buildSegment(false, segmentRoot,"SecondLevelCacheRegions");
			String[] regions = statistics.getSecondLevelCacheRegionNames();
			long tmp = 0;
			long totalSSMemory = 0;
			if(regions!=null) {
				for(String region: regions) {
					collectionName = tracer.buildSegment(collectionRoot , region);
					SecondLevelCacheStatistics ss = statistics.getSecondLevelCacheStatistics(region);
					try {
						tracer.recordCounterMetric(collectionName, "ElementCountInMemory", ss.getElementCountInMemory());
					} catch (Exception e) {}
					try {
						tracer.recordCounterMetric(collectionName, "ElementCountOnDisk", ss.getElementCountOnDisk());
					} catch (Exception e) {}
					
					
					hitCount = ss.getHitCount();
					missCount = ss.getMissCount();
					try {
						hitRatio = hitCount / (hitCount + missCount);
					} catch (Exception e) {
						hitRatio = 0;
					}
					tracer.recordCounterMetric(collectionName, "HitCount", hitCount);
					tracer.recordCounterMetric(collectionName, "MissCount", missCount);
					tracer.recordCounterMetric(collectionName, "HitRatio", hitRatio);
					tracer.recordCounterMetric(collectionName, "PutCount", ss.getPutCount());
					try {						
						tmp = ss.getSizeInMemory();
						tracer.recordCounterMetric(collectionName, "SizeInMemory", tmp);
						totalSSMemory+=tmp;				
					} catch (Exception e) {}
										
				}
				try {
					tracer.recordCounterMetric(segmentRoot, "SecondLevelCacheMemoryUsage", totalSSMemory);
				} catch (Exception e) {}								
			}
			tracer.recordCounterMetric(segmentRoot, "SessionCloseCount", statistics.getSessionCloseCount());
			tracer.recordCounterMetric(segmentRoot, "SessionOpenCount", statistics.getSessionOpenCount());
			// The next two metrics do not reset on statistics.clear() so we need to use a delta.
			long tx = statistics.getSuccessfulTransactionCount();
			long stx = statistics.getTransactionCount();
			tracer.recordCounterMetricDelta(segmentRoot, "SuccessfulTransactionCount", stx);
			tracer.recordCounterMetricDelta(segmentRoot, "TransactionCount", tx);
			tracer.recordCounterMetricDelta(segmentRoot, "OtherTransactionCount", tx-stx);
			
			tracer.recordMetric(segmentRoot, "CollectionTime", System.currentTimeMillis()-start);
			collectionsSinceReset++;
			if(collectionsSinceReset==resetCount) {
				collectionsSinceReset=0;
				statistics.clear();
			}
			
		} catch (Throwable e) {
			e.printStackTrace();
		} finally {
			collectTime = System.currentTimeMillis()-start;
			tracer.recordMetric(segmentRoot, "CollectionTime", collectTime);
		}

	}


	/**
	 * @return the sessionFactory
	 */
	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}


	/**
	 * @param sessionFactory the sessionFactory to set
	 */
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}


	/**
	 * @return the sessionFactoryName
	 */
	@JMXAttribute(description="Sets the JNDI Name of the target session factory.", name="SessionFactory")
	public String getSessionFactoryName() {
		return sessionFactoryName;
	}


	/**
	 * @param sessionFactoryName the sessionFactoryName to set
	 */
	public void setSessionFactoryName(String sessionFactoryName) {
		this.sessionFactoryName = sessionFactoryName;
	}
	
	/**
	 * @param objectName the objectName to set
	 */
	public void setObjectName(ObjectName objectName) {
		super.setObjectName(objectName);
		try {factoryName = objectName.getKeyProperty("name");} catch (Exception e) {}		
		if(factoryName==null) {
			String[] fragments = sessionFactoryName.split("/");
			if(fragments.length==0) factoryName = sessionFactoryName;
			else factoryName = fragments[fragments.length-1];			  		
		}
		segmentRoot = tracer.buildSegment("Hibernate","Session Factories",factoryName);
	}
		


	/**
	 * @return the resetCount
	 */
	@JMXAttribute(description="The number of collections to take before stats reset.", name="ResetCount")
	public int getResetCount() {
		return resetCount;
	}


	/**
	 * @param resetCount the resetCount to set
	 */
	public void setResetCount(int resetCount) {
		this.resetCount = resetCount;
	}


	/**
	 * @return the collectionsSinceReset
	 */
	@JMXAttribute(description="The number of collections since stats reset.", name="Collections")
	public int getCollectionsSinceReset() {
		return collectionsSinceReset;
	}

}
