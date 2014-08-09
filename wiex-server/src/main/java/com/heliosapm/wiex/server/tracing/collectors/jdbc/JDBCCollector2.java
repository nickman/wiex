package com.heliosapm.wiex.server.tracing.collectors.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.management.Attribute;

import com.heliosapm.wiex.jmx.dynamic.annotation.JMXAttribute;
import com.heliosapm.wiex.jmx.dynamic.annotation.JMXManagedObject;
import com.heliosapm.wiex.jmx.dynamic.annotation.JMXOperation;
import com.heliosapm.wiex.server.tracing.collectors.jdbc.cache.CachedResultSet;


/**
 * <p>Title: JDBCCollector2</p>
 * <p>Description: Collector for tracing data from a JDBC sourced database with advanced options and multi-row capability.</p> 
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Company: Helios Development Group</p>
 * @author Whitehead
 * @version $Revision: 1.7 $
 */
@JMXManagedObject(annotated=true, declared=false)
public class JDBCCollector2 extends JDBCCollector {
	
	protected Map<String, Object> scopeState = new ConcurrentHashMap<String, Object>();
	protected Set<String> scopeTracking = new HashSet<String>();
	
	
	/**
	 * Instantiates a new JDBCCollector2
	 */
	public JDBCCollector2() {
		super();	
	}	
	
	/**
	 * Initializes the VERSION and MODULE.
	 * @see com.heliosapm.wiex.server.collectors.BaseCollector#init()
	 */
	public void init() {
		VERSION = "$Revision: 2.0 $";
		MODULE = "JDBCCollector2";
	}	
	
	/**
	 * Tests availability of the data source using the configured sql.
	 * Returns:<ol>
	 * <li>1 if susccessful
	 * <li>0 if failed
	 * <li>2 if not configured
	 * </ol>
	 * @return A result code
	 */
	public int testAvailability() {
		int result = 0;
		Statement st = null;
		ResultSet rset = null;
		Connection conn = null;
		if(availabilitySQL==null) return 2;
		String segment = tracer.buildSegment(availabilitySegment);
		try {
			long start = System.currentTimeMillis();
			conn = getConnection();
			long elapsed = System.currentTimeMillis() - start;
			tracer.recordCounterMetric(segment, "Connection Time", elapsed);
			start = System.currentTimeMillis();
			st = conn.createStatement();
			rset = st.executeQuery(availabilitySQL);
			while(rset.next()) {}
			elapsed = System.currentTimeMillis() - start;
			tracer.recordCounterMetric(segment, "Query Time", elapsed);
			tracer.recordCounterMetric(segment, "Availability", 1);
			return 1;
		} catch (Exception e) {
			tracer.recordCounterMetric(segment, "Availability", 0);
			tracer.recordCounterMetric(segment, "Connection Time", -1L);
			tracer.recordCounterMetric(segment, "Query Time", -1L);
			if(logErrors) log.warn("Availability Failure On " + objectName.toString() + ":" + e);
			return 0;
		} finally {
			try { rset.close(); } catch (Exception e){}
			try { st.close(); } catch (Exception e){}
			try { conn.close(); } catch (Exception e){}
		}		
	}
	
	/**
	 * Iterates the set of SQLMappings and returns true if one is located that requires a cache result.
	 * @param mappings
	 * @return
	 */
	protected boolean containsCacheRequest(Set<SQLMapping> mappings) {		
		for(SQLMapping mapping: mappings) {
			if(mapping.isCacheResult()) return true;
		}
		return false;
	}
	
	/**
	 * Collects and traces JDBC Data.
	 * Currently only supports one row of data.
	 * @see com.heliosapm.wiex.server.collectors.BaseCollector#collect()
	 */
	@Override
	@JMXOperation(description="Collects and traces JDBC Data", expose=true, name="collect")
	public void collect() {
		long start = System.currentTimeMillis();
		testAvailability();
		scopeTracking.clear();
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rset = null;
		ResultSetMetaData rsmd = null;
		String sql = null;
		Set<SQLMapping> sqlTraces = null;
		String[] results = null;
		boolean requiresCache = false;
		Map<SQLMapping, StringBuilder> flattenMap = new HashMap<SQLMapping, StringBuilder>();
		try {
			conn = getConnection();
			for (Entry<String, Set<SQLMapping>> entry : sqlMappings.entrySet()) {
				try {
					sql = entry.getKey();
					sqlTraces = entry.getValue();
					requiresCache = containsCacheRequest(sqlTraces);
					
					ps = conn.prepareStatement(sql);

					if (bindVariables.get(sql).size() > 0) {
						for (BindVariable bind : bindVariables.get(sql)) {
							try {
								bind.bind(ps);
							} catch (Exception e) {
								try {
									ps.close();
								} catch (Exception e2) {
								}
								continue;
							}
						}
					}
					rset = ps.executeQuery();
					rsmd = rset.getMetaData();
					ArrayList<String[]> rowSet = new ArrayList<String[]>();
					String[][] cachedRowSet = null;
					String[] header = null;
					if(requiresCache) {
						header = new String[rsmd.getColumnCount()];
						for(int i = 0; i < rsmd.getColumnCount(); i++) {
							header[i] = rsmd.getColumnName(i+1);
						}
						rowSet.add(header);
					}
					
					while (rset.next()) {
						results = new String[rsmd.getColumnCount()];
						for (int i = 0; i < rsmd.getColumnCount(); i++) {
							// the two replaces clean the value for tracing metric names and segments.
							// this is Introscope specific and needs to be replaced with a call to the tracer.
							results[i] = rset.getString(i + 1).replace(":", ";").replace("|", "/");							
						}						
						if(requiresCache) {
							rowSet.add(results);
						}
						// loop through mappings and pass in results to each
						for (SQLMapping sqlTrace : sqlTraces) {
							if(sqlTrace.isFlatten()) {
								StringBuilder buff = flattenMap.get(sqlTrace);
								if(buff==null) {
									buff = new StringBuilder();
									flattenMap.put(sqlTrace, buff);
								}
								buff.append(results[sqlTrace.getColumn()]).append(sqlTrace.getFlatten());
								//buff.append(sqlTrace.getFlatten()).append(results[sqlTrace.getColumn()]);
								continue;
							}
							try {
								if(sqlTrace.isTraceDefined()) {
									String tmpCategory = tracer.buildSegment(segmentPrefix,false,sqlTrace.getMetricSegment(results, header));
									String tmpMetricName = sqlTrace.getMetricName(results, header);
									String tmpCounterType = sqlTrace.getCounterType(results, header); 
									recordTrace(
											tmpCategory,
											tmpMetricName,
											results[sqlTrace.getColumn()], 
											tmpCounterType
									);
									if(sqlTrace.isScoped()) {
										traceScope(tmpCategory, tmpMetricName, tmpCounterType, sqlTrace.getScopeResetValue());
									}
									
									if (sqlTrace.isAttributeDefined()) {
										update(sqlTrace.getAttributeName(results, header),
												results[sqlTrace.getColumn()]);
									}
								}
							} catch (Throwable te) {
								if(logErrors) log.error("Failed to record trace for:"
										+ sqlTrace + "\nException was:" + te,
										te);
							}
						}
					}
					long loopElapsed = System.currentTimeMillis() - start;
					// Now we have a full result set, so if we are caching it anywhere,
					// we loop back through the traces and store the result set where applicable.
					if(requiresCache) {
						cachedRowSet = new String[rowSet.size()][];
						for(int i = 0; i < rowSet.size(); i++) {
							cachedRowSet[i] = rowSet.get(i);
						}						
						for (SQLMapping sqlTrace : sqlTraces) {
							if(sqlTrace.isCacheResult()) {
								String attrName = sqlTrace.getCacheResultAttributeName() + "Container";
								CachedResultSet crs = (CachedResultSet)mbeanServer.getAttribute(sqlTrace.getCacheResultObjectName(), attrName);								
								if(sqlTrace.getPostProcessorURL() != null) {
									// this means a post processing URL has been defined.
									if(sqlTrace.isModified()) {
										// the groovy source has been modified so we should recompile
										sqlTrace.compilePostProcessor();
									}
									cachedRowSet = (String[][])sqlTrace.invokePostProcessor(cachedRowSet);  
									crs.setPostProcessElapsedTime(sqlTrace.getPostProcessorElapsedTime());
								}
								crs.setResultSet(cachedRowSet);
								loopElapsed = System.currentTimeMillis() - start;
								crs.setElapsedTime(loopElapsed);
								mbeanServer.setAttribute(sqlTrace.getCacheResultObjectName(), new Attribute(attrName, crs));
							}							
						}
					}
					
					// Trace all flattened values
					for(Entry<SQLMapping, StringBuilder> fentry: flattenMap.entrySet()) {
						SQLMapping sqlTrace = fentry.getKey();
						String flattenedValue = fentry.getValue().toString();
						String tmpCategory = tracer.buildSegment(segmentPrefix,false,sqlTrace.getMetricSegment(results, header));
						String tmpMetricName = sqlTrace.getMetricName(results, header);
						recordTrace(
								tmpCategory,
								tmpMetricName,
								flattenedValue, 
								METRIC_TYPE_STRING
						);						
						if(sqlTrace.isScoped()) {
							traceScope(tmpCategory, tmpMetricName, METRIC_TYPE_STRING, sqlTrace.getScopeResetValue());
						}
						
						if (sqlTrace.isAttributeDefined()) {
							update(sqlTrace.getAttributeName(results, header), flattenedValue);
						}						
					}
				} catch (Throwable e) {
					if(logErrors) log.error("Failed to process query:" + sql + ":" + e);
				} finally {
					try {
						rset.close();
					} catch (Exception e) {
					}
					try {
						ps.close();
					} catch (Exception e) {
					}
				}
			}
		} catch (Exception e) {
			if(logErrors) log.error("JDBC2 Collection Error:" + e);
		} finally {
			try {
				rset.close();
			} catch (Exception e) {
			}
			try {
				ps.close();
			} catch (Exception e) {
			}
			try {
				conn.close();
			} catch (Exception e) {
			}
		}
		processScope();
		collectTime = System.currentTimeMillis() - start;
		if(traceCollectionTime) {
			tracer.recordMetric(tracer.buildSegment(segmentPrefix, MODULE), "Collect Time", collectTime);
		}
	}
	
	/**
	 * Returns the number of entries in scope management.
	 * @return The number of managed scopes.
	 */
	@JMXAttribute(description="The number of entries in scope management", name="ScopedEntryCount")
	public int getScopedEntryCount() {
		return scopeTracking.size();
	}
	
	/**
	 * Stores a trace into local state.
	 * @param category
	 * @param name 
	 * @param type
	 * @param value
	 */
	protected void traceScope(String category, String name, String type, String value) {
		StringBuilder buff = new StringBuilder(category);
		buff.append("~");
		buff.append(name);
		buff.append("~");
		buff.append(type);
		scopeState.put(buff.toString(), value);
		scopeTracking.add(buff.toString());
	}
	
	/**
	 * Iterates through each scoped entry and resets any entries that were not processed in the last collection.
	 */
	protected void processScope() {
		Set<String> markedForRemoval = new HashSet<String>();
		// iterate through all scoped metrics
		for(Entry<String, Object> entry: scopeState.entrySet()) {
			// check to see if the key is in scopeTracking
			if(!scopeTracking.contains(entry.getKey())) {
				// if the key is not in scopeTracking
				// that means the metric "disappeared"
				// reset the metric and mark the scopeState entry for removal.
				try {
					String[] fragments = entry.getKey().split("~");
					
					recordTrace(fragments[0], fragments[1], entry.getValue().toString(), fragments[2]);
					markedForRemoval.add(entry.getKey());
				} catch (Exception e) {
					if(logErrors) {
						log.warn("Failed to Reset Metric [" + entry.getKey() + "]", e);
					}
				}				
			}
		}		
		for(String key: markedForRemoval) {
			scopeState.remove(key);
		}
	}
}