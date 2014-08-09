package com.heliosapm.wiex.server.tracing.collectors.jdbc;

import groovy.lang.Binding;

import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import javax.management.InstanceNotFoundException;
import javax.management.ObjectName;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.heliosapm.wiex.jmx.dynamic.OperationNotFoundException;
import com.heliosapm.wiex.jmx.dynamic.annotation.JMXAttribute;
import com.heliosapm.wiex.jmx.dynamic.annotation.JMXManagedObject;
import com.heliosapm.wiex.jmx.dynamic.annotation.JMXOperation;
import com.heliosapm.wiex.server.server.utils.XMLHelper;
import com.heliosapm.wiex.server.tracing.collectors.BaseCollector;
import com.heliosapm.wiex.tracing.tracing.TracerFactory;
import com.heliosapm.wiex.tracing.tracing.jmx.metrics.StringMetric;


/**
 * <p>Title: JDBCCollector</p>
 * <p>Description: Collector for trracing data from a JDBC sourced database.</p> 
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Company: Helios Development Group</p>
 * @author Whitehead
 * @version $Revision: 1.7 $
 */
@JMXManagedObject(annotated=true, declared=false)
public class JDBCCollector extends BaseCollector {
	
	/**	The JDBCConnectionFactory to acquire connections from. */
	protected JDBCConnectionFactory connectionFactory = null;
	/**	The JDBCConnectionFactory class name */
	protected String connectionFactoryClassName = null;
	/**	The JDBCConnectionFactory class */
	protected Class connectionFactoryClass = null;
	/**	The  JDBCConnectionFactory configuration properties */
	protected Properties connectionFactoryProperties = null;
	/** The mapping of columns in the result to the metric names */
	protected Map<String, Set<SQLMapping>> sqlMappings = new HashMap<String, Set<SQLMapping>>();
	/** The bind variables for the query */
	protected Map<String, Set<BindVariable>> bindVariables = new HashMap<String, Set<BindVariable>>();
	
	/** The Availability Check Segment Name */
	protected String[] availabilitySegment = new String[]{""};
	/** The Availability Check SQL */
	protected String availabilitySQL = null;
	
	

	
	/**
	 * Instantiates a new JDBCCollector
	 */
	public JDBCCollector() {
		super();		
	}
	
	/**
	 * 
	 * @see com.heliosapm.wiex.server.collectors.BaseCollector#init()
	 */
	public void init() {
		VERSION = "$Revision: 1.7 $";
		MODULE = "JDBCCollector";		
	}
	
	/**
	 * Acquires a new JDBC Connection
	 * @throws Exception
	 */
	protected Connection getConnection() throws Exception {
		try {
			if(connectionFactoryClass==null) {
				connectionFactoryClass = Class.forName(connectionFactoryClassName, true, getClass().getClassLoader());
 			}
			if(connectionFactory==null) {
				connectionFactory = (JDBCConnectionFactory)connectionFactoryClass.newInstance();
				connectionFactory.setProperties(connectionFactoryProperties);
			}
			return connectionFactory.getJDBCConnection();
		} catch (Exception e) {
			if(logErrors) log.error("Failed to Acquire JDBC Connection:" , e );
			throw e;
		}
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
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rset = null;
		ResultSetMetaData rsmd = null;
		String sql = null;
		Set<SQLMapping> sqlTraces = null;
		String[] results = null;
		try {
			conn = getConnection();
			for(Entry<String, Set<SQLMapping>> entry: sqlMappings.entrySet()) {
				try {
					sql = entry.getKey();
					sqlTraces = entry.getValue();
					ps = conn.prepareStatement(sql);
					// check binds
					
					// execute binds
					if(bindVariables.size()>0) {
						for(BindVariable bind : bindVariables.get(sql)) {
							try {
								bind.bind(ps);
							} catch (Exception e) {
								try { ps.close(); } catch (Exception e2) {}
								continue;
							}
						}
					}
					rset = ps.executeQuery();
					rsmd = rset.getMetaData();
					results = new String[rsmd.getColumnCount()];
					if(rset.next()) {
						for(int i = 0; i < rsmd.getColumnCount(); i++) {
							results[i] = rset.getString(i+1);
						}
					}
					for(SQLMapping sqlTrace: sqlTraces) {
						try {
							recordTrace(tracer.buildSegment(segmentPrefix, false, sqlTrace.getMetricSegment()), sqlTrace.getMetricName(), results[sqlTrace.getColumn()-1], sqlTrace.getCounterType());
							if(sqlTrace.isAttributeDefined()) {
								update(sqlTrace.getAttributeName(), results[sqlTrace.getColumn()-1]);
							}
							
						} catch (Exception te) {
							if(logErrors) log.error("Failed to record trace for:" + sqlTrace + "\nException was:" + te);
						}
					}
				} catch (Exception e) {
					if(logErrors) log.error("Failed to process query:" + sql + ":" + e);
				} finally {
					try { rset.close(); } catch (Exception e) {}
					try { ps.close(); } catch (Exception e) {}					
				}
			}
		} catch (Exception e) {
			if(logErrors) log.error("JDBC Collection Error", e);
		} finally {
			try { rset.close(); } catch (Exception e) {}
			try { ps.close(); } catch (Exception e) {}
			try { conn.close(); } catch (Exception e) {}
		}
		collectTime = System.currentTimeMillis()-start;
		TracerFactory.getInstance().recordMetric(MODULE, "Collect Time", collectTime);
		
	}
	
	/**
	 * The JDBCConnectionFactory class name.
	 * @return the connectionFactoryClassName
	 */
	@JMXAttribute(description="The JDBCConnectionFactory class name.", name="ConnectionFactoryClassName")
	public String getConnectionFactoryClassName() {
		return connectionFactoryClassName;
	}

	/**
	 * Sets the JDBCConnectionFactory class name.
	 * @param connectionFactoryClassName the connectionFactoryClassName to set
	 */
	public void setConnectionFactoryClassName(String connectionFactoryClassName) {
		this.connectionFactoryClassName = connectionFactoryClassName;		
	}

	/**
	 * The JDBCConnectionFactory connection properties.
	 * @return the connectionFactoryProperties
	 */
	@JMXAttribute(description="The JDBCConnectionFactory connection properties.", name="ConnectionFactoryProperties")
	public Properties getConnectionFactoryProperties() {
		return connectionFactoryProperties;
	}

	/**
	 * Sets the JDBCConnectionFactory connection properties.
	 * @param connectionFactoryProperties the connectionFactoryProperties to set
	 */
	public void setConnectionFactoryProperties(
			Properties connectionFactoryProperties) {
		this.connectionFactoryProperties = connectionFactoryProperties;
	}
	
	/**
	 * Sets up a SQL query to execute and the column to metric mapping for the results.
	 * Format is as follows:<br><code>
	 * &lt;Query sql="SELECT COUNT(*) FROM EMP"&gt;<br>
	 * 	    &lt;Mapping column="1" segment="Company|Employees" metricName="Count" type="INT" /&gt;<br>
	 * &lt;/Query&gt;<br></code>
	 * @param xml
	 */
	
	public void setMapping(Element xml) {
		NodeList nodeList = xml.getChildNodes();
		NodeList queryNodes = null;
		String nodeName = null;
		String qNodeName = null;
		String sql = null;
		SQLMapping mapping = null;
		BindVariable bind = null;
		Set<SQLMapping> mappings = null;
		Set<BindVariable> binds = null;		
		for(int i = 0; i < nodeList.getLength(); i++) {
			nodeName = nodeList.item(i).getNodeName();
			if("Query".equalsIgnoreCase(nodeName)) {
				Node sqlNode = nodeList.item(i).getAttributes().getNamedItem("sql");
				if(sqlNode==null) {					
					sqlNode = XMLHelper.getChildNodeByName(nodeList.item(i), "SQL", true);
					sql = sqlNode.getFirstChild().getNodeValue();
				} else {
					sql = sqlNode.getNodeValue();
				}
				
				binds = new HashSet<BindVariable>();
				mappings = new HashSet<SQLMapping>();
				queryNodes = nodeList.item(i).getChildNodes();
				for(int x = 0; x < queryNodes.getLength(); x++) {
					qNodeName = queryNodes.item(x).getNodeName();
					mapping = new SQLMapping();
					if("Mapping".equalsIgnoreCase(qNodeName)) {						
						mapping.setColumn(Integer.parseInt(queryNodes.item(x).getAttributes().getNamedItem("column").getNodeValue()));
						String segValue = queryNodes.item(x).getAttributes().getNamedItem("segment").getNodeValue();
						String[] segment = segValue.split("\\|");
						if(segment.length==0) {
							segment = new String[]{segValue};
						}
						mapping.setMetricSegment(segment);
						mapping.setCounterType(queryNodes.item(x).getAttributes().getNamedItem("type").getNodeValue());
						mapping.setMetricName(queryNodes.item(x).getAttributes().getNamedItem("metricName").getNodeValue());
						mapping.setAttributeName(getOptionalValue(queryNodes.item(x).getAttributes(), "attributeName"));
						mapping.setAttributeType(getOptionalValue(queryNodes.item(x).getAttributes(), "attributeType"));
						mapping.setFlatten(getOptionalValue(queryNodes.item(x).getAttributes(), "flatten"));
						String scopedValue = getOptionalValue(queryNodes.item(x).getAttributes(), "scoped");
						boolean scoped = false;
						try {
							scoped = Boolean.parseBoolean(scopedValue);
							mapping.setScoped(scoped);
						} catch (Exception e) {
							mapping.setScoped(false);
						}
						scopedValue = getOptionalValue(queryNodes.item(x).getAttributes(), "scopeResetValue");
						try {
							mapping.setScopeResetValue(scopedValue);
						} catch (Exception e) {
							mapping.setScopeResetValue("0");
						}
						
						
						String bv = getOptionalValue(queryNodes.item(x).getAttributes(), "useBinds");
						if(bv!=null && "FALSE".equalsIgnoreCase(bv)) {
							mapping.setUseBinds(false);
						}
						//mappings.add(mapping);
						mapping.setPopulated(true);
						mapping.setTraceDefined(true);
					} else if("CacheResult".equalsIgnoreCase(qNodeName)) {
						try {
							mapping.setCacheResult(true);
							Node cacheResultNode = queryNodes.item(x);
							Map<String, String> containerAttributes = new HashMap<String, String>();
							String attrName = null;
							String attrValue = null;
							NodeList attrList = cacheResultNode.getChildNodes();
							for(int a = 0; a < attrList.getLength(); a++) {
								Node cacheAttributeNode = attrList.item(a);
								if("attribute".equalsIgnoreCase(cacheAttributeNode.getNodeName())) {
									attrName = cacheAttributeNode.getAttributes().getNamedItem("name").getNodeValue();
									attrValue = cacheAttributeNode.getChildNodes().item(0).getNodeValue();
									containerAttributes.put(attrName, attrValue);
								}
							}
							String targetObjectName = queryNodes.item(x).getAttributes().getNamedItem("objectName").getNodeValue();
							ObjectName cacheResultObjectName = new ObjectName(targetObjectName);
							String cacheResultAttributeName = queryNodes.item(x).getAttributes().getNamedItem("attributeName").getNodeValue();						
							String containerClassName = getOptionalValue(queryNodes.item(x).getAttributes(), "ContainerClass");
							String postProcessingURLStr = getOptionalValue(queryNodes.item(x).getAttributes(), "PostProcessURL");
							
							mapping.setCacheResultAttributeName(cacheResultAttributeName);
							mapping.setCacheResultObjectName(cacheResultObjectName);
							mapping.setPopulated(true);
							if(containerClassName != null) {
								mapping.setContainerClassName(containerClassName);
							}
							if(postProcessingURLStr != null) {
								try {
									initPostProcessor(mapping, postProcessingURLStr, cacheResultObjectName, cacheResultAttributeName);									
								} catch (Exception e) {
									if(logErrors) {
										log.error("Failed to Set Up Groovy Post Processor from URL:" + postProcessingURLStr, e);
									}
								}
							}
							initializeCachedResultMBean(cacheResultObjectName, cacheResultAttributeName, mapping.getContainerClassName(), containerAttributes);
						} catch (Exception e) {
							throw new RuntimeException("Exception Creating CacheResult", e);						
						}					
					} else if("Bind".equalsIgnoreCase(qNodeName)) {
						String numberStr = queryNodes.item(x).getAttributes().getNamedItem("number").getNodeValue();
						int number = Integer.parseInt(numberStr);
						String source = queryNodes.item(x).getAttributes().getNamedItem("source").getNodeValue();
						String type = queryNodes.item(x).getAttributes().getNamedItem("type").getNodeValue();					
						bind = new BindVariable(number, source, type);
						if("attribute".equals(source)) {
							String attributeName = queryNodes.item(x).getAttributes().getNamedItem("attributeName").getNodeValue();
							String objectNameStr = queryNodes.item(x).getAttributes().getNamedItem("objectName").getNodeValue();
							ObjectName objectName = null;
							try {
								objectName = new ObjectName(objectNameStr);
							} catch (Exception e) {
								throw new RuntimeException("Exception Creating ObjectName from [" + objectNameStr + "]", e);
							} 
							bind.setAttributeName(attributeName);
							bind.setObjectName(objectName);
							bind.setMBeanServer(mbeanServer);					
						}
						binds.add(bind);
					}
					if(mapping.isPopulated()) mappings.add(mapping);
				}
				bindVariables.put(sql, binds);
				sqlMappings.put(sql, mappings);
				
			} 
		}
	}
	
	/**
	 * Initializes the post processing groovy script.
	 * @param mapping The SQLMapping the groovy script is being prepared for.
	 * @param postProcessingURLStr The URL string representation for the source groovy script.
	 * @param targetMBean The cached JDBC result set target mbean object name. 
	 * @param targetAttribute The cached JDBC result set target mbean attribute name.
	 * @throws NamingException
	 * @throws IOException 
	 */
	protected void initPostProcessor(SQLMapping mapping, String postProcessingURLStr, ObjectName targetMBean, String targetAttribute) throws NamingException, IOException {
		URL postProcessorURL = new URL(postProcessingURLStr);
		mapping.setPostProcessorURL(postProcessorURL);
		Binding binding = new Binding();
		binding.setProperty("stateMap", new HashMap());
		binding.setProperty("localMBeanServer", mbeanServer);
		binding.setProperty("jndi", new InitialContext());
		binding.setProperty("binding", binding);
		binding.setProperty("tracer", tracer);
		binding.setProperty("localObjectName", objectName);
		mapping.setPostProcessorBinding(binding);
		mapping.compilePostProcessor();
		
		
		
		
	}
	

	
	/**
	 * Safely returns the attribute value or a null if it does not exist.
	 * @param nodeMap
	 * @param itemName
	 * @return the attribute value or null
	 */
	protected String getOptionalValue(NamedNodeMap nodeMap, String itemName) {
		try {
			return nodeMap.getNamedItem(itemName).getNodeValue();
		} catch (Exception e) {
			return null;
		}
	}
	
	/**
	 * Placeholder method only.
	 * @return A null.
	 */
	@JMXAttribute(description="A SQLMapping Mapping", name="Mapping")
	public Element getMapping() {
		return null;
	}

	
	/**
	 * Returns a report of the currently configured SQLTraces.
	 * @return A string report.
	 */
	@JMXOperation(description="Returns a report of the currently configured SQLTraces", expose=true, name="ReportSQLTraces")
	public String reportSQLTraces() {
		StringBuilder buff = new StringBuilder("SQLTraces");
		for(String sql: sqlMappings.keySet()) {
			buff.append("\n\tMappings");
			buff.append("\n\t\tSQL:" + sql);
			for(SQLMapping sqltrace: sqlMappings.get(sql)) {
				buff.append("\n\t\t\t").append(sqltrace.toString());
			}
			buff.append("\n\t\tBind Variables");
			if(bindVariables.size()>0) {
				for(BindVariable bind: bindVariables.get(sql)) {
					if(bind!=null) {
						buff.append("\n\t\t").append(bind.toString());
					}
				}			
			}
		}
		return buff.toString();		
	}
	
	
	
	/**
	 * Statis test.
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		String xml = "<Query sql=\"SELECT COUNT(*) FROM EMP\"><Mapping column=\"2\" segment=\"Company|Employees\" metricName=\"Size\" type=\"LONG\" /><Mapping column=\"2\" segment=\"Company|Employees\" metricName=\"Count\" type=\"INT\" /></Query>";
		InputSource insource = new InputSource(new StringReader(xml));
		XPath xpath = XPathFactory.newInstance().newXPath();
		XPathExpression xpathExpression = xpath.compile("//Query");
		NodeList nodeList = (NodeList)xpathExpression.evaluate(insource, XPathConstants.NODESET);
		JDBCCollector j = new JDBCCollector(); 
		j.setMapping((Element)nodeList.item(0));
	}
	
	public static void log(Object message) {
		System.out.println(message);
	}

	
	
	/**
	 * Creates (if required) a new String based attribute and updates it.
	 * @param attributeName
	 * @param value
	 */
	protected void update(String attributeName, Object value) {
		Object managedObject = null;
		try {			
			
			try {
				//mbeanServer.setAttribute(on, attribute);
				mbeanServer.invoke(objectName, "Update " + attributeName, new Object[]{value}, new String[]{"java.lang.Object"});
			} catch (InstanceNotFoundException ine) {
				// Should not happen. These managed objects are registered with the collection service.
			} catch (OperationNotFoundException ane) {				
				// Managed Object was not registered
				managedObject = new StringMetric(attributeName);
				mbeanServer.invoke(objectName, "addManagedObject", new Object[]{managedObject}, new String[]{"java.lang.Object"});
				//mbeanServer.setAttribute(on, attribute);
				mbeanServer.invoke(objectName, "Update " + attributeName, new Object[]{value}, new String[]{"java.lang.Object"});
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Returns the segment where the availability for a jdbc source will be traced.
	 * @return the availabilitySegment
	 */
	@JMXAttribute(description="The Configured Availability Segments", name="AvailabilitySegment")
	public String[] getAvailabilitySegment() {
		return availabilitySegment;
	}

	/**
	 * Returns the compiled availability segment name.
	 * @return the segment name for availability.
	 */
	@JMXAttribute(description="The Configured Availability Segment Name", name="AvailabilitySegmentName")
	public String getAvailabilitySegmentName() {
		return tracer.buildSegment(availabilitySegment);
	}
	
	/**
	 * Sets the segment where the availability for a jdbc source will be traced.
	 * @param availabilitySegment the availabilitySegment to set
	 */
	public void setAvailabilitySegment(String[] availabilitySegment) {
		this.availabilitySegment = availabilitySegment;
	}

	/**
	 * Gets the SQL that is used to test availability. 
	 * @return the availabilitySQL
	 */
	@JMXAttribute(description="The SQL that is used to test availability", name="AvailabilitySQL")
	public String getAvailabilitySQL() {
		return availabilitySQL;
	}

	/**
	 * Sets the SQL that is used to test availability.
	 * @param availabilitySQL the availabilitySQL to set
	 */
	public void setAvailabilitySQL(String availabilitySQL) {
		this.availabilitySQL = availabilitySQL;
	}
	
	
	

}


/*
select t.system_n, to_char(max(latestupdate), 'YYYY-MM-DD-HH24.MI.SS') from ds4net.dsservice_center_multi t 
where t.system_n in ('AS52','AS09','ASE5','AS15','ASED')
group by t.system_n
*/