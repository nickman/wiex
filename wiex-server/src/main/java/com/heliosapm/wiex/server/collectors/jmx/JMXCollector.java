package com.heliosapm.wiex.server.collectors.jmx;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;
import java.lang.management.ThreadInfo;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeDataSupport;
import javax.naming.Context;
import javax.naming.InitialContext;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.heliosapm.wiex.jmx.dynamic.annotation.JMXAttribute;
import com.heliosapm.wiex.jmx.dynamic.annotation.JMXManagedObject;
import com.heliosapm.wiex.jmx.dynamic.annotation.JMXOperation;
import com.heliosapm.wiex.server.collectors.BaseCollector;
import com.heliosapm.wiex.server.collectors.jmx.tracers.GroovyObjectTracer;
import com.heliosapm.wiex.server.helpers.XMLHelper;
import com.heliosapm.wiex.tracing.tracing.TracerFactory;

/**
 * <p>Title: JMXCollector</p>
 * <p>Description: Collects JMX Attributes and Traces</p> 
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Company: Helios Development Group</p>
 * @author Whitehead
 * @version $Revision: 1.10 $
 */
@JMXManagedObject(annotated=true, declared=false)
public class JMXCollector extends BaseCollector {
	
	
	/**	A list of JMX Attributes that are configured to be traced. */
	protected List<JMXAttributeTrace> attributeTraces = new ArrayList<JMXAttributeTrace>();
	/** The target MBeanServer Connection */
	protected MBeanServerConnection mBeanServerConnection = null;
	/** The MBeanServerConnectionFactory to get a JMX Connection From */
	protected MBeanServerConnectionFactory connectionFactory = null;
	/** The MBeanServerConnectionFactory class name */
	protected String connectionFactoryClassName = LocalMBeanServerConnectionFactory.class.getName();
	/** The MBeanServerConnectionFactory class */
	protected Class<?> connectionFactoryClass = null;
	
	
	/** The availability MBean Object Name */
	protected ObjectName availabilityMBean = null;
	/** The availability MBean Attribute */
	protected String availabilityAttribute = null;
	/** The availability segments */
	protected String[] availabilitySegment = null;
	/** The compiled availability segment name */
	protected String availabilitySegmentName = null;
	/** indicates if mxbean collection should be excuted. */
	protected boolean mXBeans = false;
	/** The MXBean compiled segment */
	protected String mXBeanSegment = null;
	/** A map of MXBean ObjectNames keyed by the ObjectName string */
	protected Map<String, ObjectName> mxBeanObjectNames = new HashMap<String, ObjectName>();
	/** A map of MemoryPool ObjectNames keyed by the memory pool name */
	protected Map<String, ObjectName> memoryPoolObjectNames = null;
	/** A map of GC ObjectNames keyed by the gc name */
	protected Map<String, ObjectName> gcObjectNames = null;
	/** A map of GC Collection and Elapsed Times keyed by the gc name */
	protected Map<String, long[]> gcTimes = new HashMap<String, long[]>();
	/** Indicates if compiler time monitoring is supported in the target VM */
	protected Boolean supportsCompilerTime = null;
	/** Indicates if thread contention monitoring is supported in the target VM */
	protected Boolean supportsThreadContention = null;	
	/** Indicates if thread deadlocking should be monitored */
	protected boolean deadLockMonitor = false;
	/** Indicates if thread status aggregate summary should be monitored */
	protected boolean threadMonitor = false;
	/** Thread State aggregator */
	protected Map<Thread.State, Integer> threadStates = new HashMap<Thread.State, Integer>(12);
	/** MXBean ObjectNames that should be included in MXBean Collection */
	protected Set<ObjectName> includeMXBeans = new HashSet<ObjectName>();
	/** MXBean ObjectNames that should be excluded in MXBean Collection */
	protected Set<ObjectName> excludeMXBeans = new HashSet<ObjectName>();
	/** GC MXBean Mask Object Name */
	protected ObjectName gcMXBean = null;
	/** Memory Pool MXBean Mask Object Name */
	protected ObjectName memoryPoolMXBean = null;
	
	/** MXBean ObjectNames and a boolean for ObjectNames that have been examined and included or excluded. */
	protected Map<ObjectName, Boolean> traceMXBeans = new HashMap<ObjectName, Boolean>(30);
	
	/** The MXBean Collection XML Element */
	protected Element mxBeanConfig = null;
	
	/** The number of times subsidiary mbeans should be queried for before stopping */
	protected int mbeanQueryAttempts = 5;
	/** The number of times subsidiary mbeans have been queried for */
	protected int mbeanQueryAttempted = 0;
	/** The number of elapsed collections that should occur before GC % time is calculated */
	protected int gCPollCycles = 5;
	/** The number of elapsed collections that have occured since GC % time was calculated */
	protected int gCPolledCycles = 0;
	/** Indicates if the one time collection of Runtime attributes has completed */
	protected boolean runtimeCollected = false;
	
	
	

	
	
	
	
	
	public static final String GROOVY_TYPE = "groovy.tracer.type";
	
	public static final String[] CLASS_LOADING_STATS = new String[]{"LoadedClassCount", "TotalLoadedClassCount", "UnloadedClassCount"};
	public static final String[] THREAD_STATS = new String[]{"ThreadCount", "DaemonThreadCount", "TotalStartedThreadCount"};
	
	public static final String[] NULL_SIG = new String[]{};
	public static final Object[] NULL_ARG = new Object[]{};
	
	

	/**
	 * Instantiates a new JMXCollector
	 */
	public JMXCollector() {
		super();
		// sets the default properties
		connectionFactoryProperties.put("sbstracing.jmx.factory.domain", "jboss");
		for(Thread.State state: Thread.State.values()) {
			threadStates.put(state, 0);
		}
	}
	
	/**
	 * Initializes the VERSION and MODULE.
	 * @see com.heliosapm.wiex.server.collectors.BaseCollector#init()
	 */
	public void init() {
		VERSION = "$Revision: 1.10 $";
		MODULE = "JMXCollector";
	}	
	
	/**
	 * Closes the connection factory and then calls super.stop().
	 * @see com.heliosapm.wiex.server.collectors.BaseCollector#stop()
	 */
	@JMXOperation(description="Stops The Service", expose=true, name="stop")
	public void stop() {
		connectionFactory.close();
		super.stop();		
	}
	
	/**
     * Starts the JMX Collector.
	 * @see com.heliosapm.wiex.server.collectors.BaseCollector#start()
	 */
	@JMXOperation(description="Starts The Service", expose=true, name="start")
	public void start() throws Exception {
		resetEnv();
		tokenizeConnectionProperties();
		formatAvailabilityNameSegment();
		super.start();		
	}
	
	/**
	 * Resets the environment on initial start and reconnect.
	 * The assumption is that on start or reset, the target MXBeans may have changed.
	 */
	protected void resetEnv() {
		mxBeanObjectNames.clear();
		memoryPoolObjectNames= null;
		gcObjectNames= null;
		gcTimes = new HashMap<String, long[]>();
		supportsCompilerTime= null;
		supportsThreadContention= null;
		traceMXBeans.clear();
		mbeanQueryAttempted = 0;
		gCPolledCycles = 0;
		runtimeCollected = false;
		if(mXBeanSegment==null) {
			mXBeanSegment = tracer.buildSegment(segmentPrefix, false, "MXBeans");
		} else {
			mXBeanSegment = formatName(mXBeanSegment);
		}
	}
	
	/**
	 * Compiles the availability segment name.
	 */
	protected void formatAvailabilityNameSegment() {
		try {
			availabilitySegmentName = formatName(tracer.buildSegment(availabilitySegment));
		} catch (Exception e) {
			
		}		
	}
	
	/**
	 * Tokenizes connection properties against the local object name.
	 */
	protected void tokenizeConnectionProperties() {
		Properties p = new Properties();
		for(Entry<Object, Object> entry: connectionFactoryProperties.entrySet()) {
			p.put(entry.getKey(), formatName(entry.getValue().toString()).trim());
		}
		connectionFactoryProperties.clear();
		connectionFactoryProperties.putAll(p);
	}
	
	
	
	/**
	 * No Op.
	 * @return A meaningless token.
	 */
	@JMXAttribute(description="Adds a new JMX Attribute Trace", name="TargetAttributes")
	public Element getTargetAttribute() {
		return null;
	}
	
	
	/**
	 * No Op.
	 * Added back in for backward compatibility.
	 * @return VOID.
	 */
	@JMXAttribute(description="Adds a new JMX Attribute Trace (Legacy)", name="TargetAttribute")
	public String getTargetAttributeX() {
		return "VOID";
	}
	/**
	 * Adds a new target attribute to be collected.
	 * Added back in for backward compatibility.
	 * @param targetAttribute
	 */
	public void setTargetAttributeX(String targetAttribute) {
		try {
			String[] segments = targetAttribute.split("\\$");
			ObjectName on = new ObjectName(segments[0]);
			JMXAttributeTrace tr = null;
			if(segments.length==3) {
				tr = new JMXAttributeTrace(on, segments[1], segments[2]);
			} else if(segments.length==4) {
				tr = new JMXAttributeTrace(on, segments[1], segments[2], segments[3]);
			} else {
				if(logErrors) {
					log.error("Invalid Segment Count for target Attribute:" + targetAttribute);
				}
				return;
			}			 
			attributeTraces.add(tr);
		} catch (Exception e) {
			
		}
	}	
	
	
	/**
	 * Adds a new target attribute to be collected.
	 * <p>Substitution Macros:<ul>
	 * <li><b>{DOMAIN}</b>: Replacement token for the target MBean's ObjectName Domain.
	 * <li><b>{TARGET-PROPERTY:<property name>}</b>: Replacement token for the target MBean's ObjectName property identified by the configured name.
	 * <li><b>{THIS-PROPERTY:<property name>}</b>: Replacement token for the collector's MBean's ObjectName property identified by the configured name.
	 * <li><b>{ATTRIBUTE}</b>: Replacement token for the target MBean's ObjectName property identified by the configured name.
	 * <li>The <b>{DOMAIN}</b> and <b>{ATTRIBUTE}</b> substitution macros are also applied to the object tracers property values.</li>
	 * </ul>
	 * With the addition of the ObjectTracers, there is some new dicey logic in the config processing.<ul>
	 * <li>If a simple tracer is defined, the <code>TargetAttribute</code> attributes <code>metricName</code> and <code>type</code> ae mandatory.<li>
	 * <li>If no object tracers are defined, the <code>TargetAttribute</code> attributes <code>metricName</code> and <code>type</code> ae mandatory.<li>
	 * <li>If one or more object tracers are defined, and no simple tracer is defined the <code>TargetAttribute</code> attributes <code>metricName</code> and <code>type</code> ae optional, and ignored.<li> 
	 * </ul>
	 * @param xml
	 */
	public void setTargetAttribute(Element xml) {
		tracer = TracerFactory.getInstance();
		String nodeName = null;
		String targetObjectName = null;
		String attributeName = null;
		String type = null;
		String segmentName = null;
		String[] segmentNames = null;
		String metricName = null;
		String defaultValue = null;
		JMXAttributeTrace tr = null;
		ObjectName on = null;
		SimpleObjectTracer sot = null;
		String sotClassName = null;
		List<ObjectTracer> objectTracers = new ArrayList<ObjectTracer>();
		try {
			NodeList nodeList = xml.getChildNodes();
			for(int i = 0; i < nodeList.getLength(); i++) {
				nodeName = nodeList.item(i).getNodeName();
				boolean containsSimpleTracer = false;
				boolean containsObjectTracer = false;
				boolean mandatory = false; // Indicates if metricName and type are mandatory.
				if("TargetAttribute".equalsIgnoreCase(nodeName)) {
					try {
						// First we need the target object name
						targetObjectName = nodeList.item(i).getAttributes().getNamedItem("objectName").getNodeValue();
						on = new ObjectName(targetObjectName);
						
						// Check for simple tracer
						if(nodeList.item(i).getAttributes().getNamedItem("simpleTracer") != null) {
							sotClassName = nodeList.item(i).getAttributes().getNamedItem("simpleTracer").getNodeValue();
							sot = (SimpleObjectTracer)Class.forName(sotClassName).newInstance();
							containsSimpleTracer=true;
						}
						// Check for object tracers
						
						NodeList otNodeList = nodeList.item(i).getChildNodes();
						String otNodeName = null;
						int groovyTracers = 0;
						for(int x = 0; x < otNodeList.getLength(); x++) {
							otNodeName = otNodeList.item(x).getNodeName();
							if("ObjectTracers".equals(otNodeName)) {
								boolean groovyTracer = false;
								for(int ot = 0; ot < otNodeList.item(x).getChildNodes().getLength(); ot++) {
									Node objectTracerNode = otNodeList.item(x).getChildNodes().item(ot);
									if(!"ObjectTracer".equals(objectTracerNode.getNodeName())) continue;
									String otClassName =  objectTracerNode.getAttributes().getNamedItem("class").getNodeValue();
									ObjectTracer objectTracer = (ObjectTracer) Class.forName(otClassName).newInstance();																		
									for(int props = 0; props < objectTracerNode.getChildNodes().getLength(); props++) {
										if(!"Property".equals(objectTracerNode.getChildNodes().item(props).getNodeName())) continue;
										Node propertyNode = objectTracerNode.getChildNodes().item(props);
										String propName =  propertyNode.getAttributes().getNamedItem("name").getNodeValue();
										String propValue =  propertyNode.getAttributes().getNamedItem("value").getNodeValue();
										if(propValue != null) {
											propValue.replaceAll("\\{DOMAIN}", on.getDomain());
											propValue.replaceAll("\\{ATTRIBUTE}", on.getDomain());
										}
										// The only types of groovy tracers we are interested in are of the type eval, because this means
										// they are mandatory.
										groovyTracer = (GroovyObjectTracer.CLASS_NAME.equals(otClassName) && propName.equals(GROOVY_TYPE) && propValue.equals("eval"));
										if(groovyTracer) {
											groovyTracers++;
										}
										
										objectTracer.setProperty(propName, propValue);
									}
									objectTracer.setProperty("logErrors", "" + logErrors);
									objectTracers.add(objectTracer);
								}
							}
						}
						// Since an EVAL  groovy tracer is considered a simple tracer,
						// the containsObjectTracer value is only true if 
						// there are more than 0 objectTracers that are not groovy tracers.
						containsObjectTracer = (objectTracers.size()>0 && (objectTracers.size() > groovyTracers));
						// Accordingly, if there are more than 0 groovyTracers, containsSimpleTracer should be true
						containsSimpleTracer = (groovyTracers > 0 || containsSimpleTracer);
						
						// Set mandatory flag.
						if(containsSimpleTracer) mandatory = true;
						if(!containsObjectTracer) mandatory = true;
						if(containsObjectTracer && !containsSimpleTracer) mandatory = false;
						
						
						// Now parse the rest of the Tracer.
						attributeName = nodeList.item(i).getAttributes().getNamedItem("attributeName").getNodeValue();
						segmentName = nodeList.item(i).getAttributes().getNamedItem("segment").getNodeValue();
						try {
							defaultValue = nodeList.item(i).getAttributes().getNamedItem("defaultValue").getNodeValue();
						} catch (Exception e) {
							defaultValue = null;
						}
						 
						segmentName = segmentName.replaceAll("\\{DOMAIN}", on.getDomain());
						segmentName = segmentName.replaceAll("\\{ATTRIBUTE}", attributeName);
						segmentNames = segmentName.split(",");
						if(segmentNames.length < 1) {
							segmentNames = new String[]{segmentName};
						}						
						if(mandatory) {
							type = nodeList.item(i).getAttributes().getNamedItem("type").getNodeValue();
							metricName = nodeList.item(i).getAttributes().getNamedItem("metricName").getNodeValue();
							metricName = metricName.replaceAll("\\{DOMAIN}", on.getDomain());
							metricName = metricName.replaceAll("\\{ATTRIBUTE}", attributeName);							
						} else {
							type = null;
							metricName = null;
						}
						
						// Create the JMXAttributeTrace and apply the simple and object tracers as applicable.
						tr = new JMXAttributeTrace(on, attributeName, tracer.buildSegment(segmentPrefix, false, segmentNames), type, metricName);
						if(containsSimpleTracer) tr.setSimpleObjectTracer(sot);
						if(containsObjectTracer) tr.addObjectTracers(objectTracers);
						if(groovyTracers>0) tr.setGroovyTracers(true);
						// Pass the mandatory flag to the JMXAttributeTrace as this will help process the collect.
						tr.setMandatory(mandatory);
						// Set the default value
						tr.setDefaultValue(defaultValue);
						log.info("Created JMXTrace:" + targetObjectName + " - " + attributeName);
						attributeTraces.add(tr);
					} catch (Exception e) {
						if(logErrors) {
							log.error("Failed to Create JMXTrace", e);
						}
					}					
				}
				
			}
			
		} catch (Exception e) {
			log.error("Failed to Set Target Attributes", e);
		}
	}
	
	/**
	 * MXBeanConfiguration
	 * @param mxBeanConfig An XML Element defining MXBean data collection configuration.
	 */
	public void setMXBeanCollection(Element mxBeanConfig) {
		this.mxBeanConfig = mxBeanConfig;
		String nodeName = null;
		String nodeValue = null;
		Node currentNode = null;
		Node mxBeanNode = mxBeanConfig;
		currentNode = XMLHelper.getChildNodeByName(mxBeanNode, "enabled", false);
		if(currentNode == null) {
			mXBeans = false;	
			log.info("MXBean Collection Disabled");
			return;			
		}
		nodeValue = currentNode.getFirstChild().getNodeValue();
		if("true".equalsIgnoreCase(nodeValue)) {
			mXBeans = true;
			log.info("MXBean Collection Enabled");
		} else {
			mXBeans = false;	
			log.info("MXBean Collection Disabled");
			return;
		}
		currentNode = XMLHelper.getChildNodeByName(mxBeanNode, "segment", false);
		if(currentNode == null) {
			mXBeanSegment = tracer.buildSegment(segmentPrefix, false, "MXBeans");
		} else {
			nodeValue = currentNode.getFirstChild().getNodeValue();
			String[] fragments = nodeValue.split(",");
			if(fragments==null || fragments.length==0) {
				tracer.buildSegment(segmentPrefix, false, "MXBeans");
			} else {
				mXBeanSegment = tracer.buildSegment(fragments);
				//mXBeanSegment = formatName(mXBeanSegment);
			}							
		}
		currentNode = XMLHelper.getChildNodeByName(mxBeanNode, "deadlockmonitor", false);
		if(currentNode != null) {
			nodeValue = currentNode.getFirstChild().getNodeValue();
			deadLockMonitor = "true".equalsIgnoreCase(nodeValue);			
		}
		currentNode = XMLHelper.getChildNodeByName(mxBeanNode, "threadmonitor", false);
		if(currentNode != null) {
			nodeValue = currentNode.getFirstChild().getNodeValue();
			threadMonitor = "true".equalsIgnoreCase(nodeValue);			
		}
		List<Node>includes = XMLHelper.getChildNodesByName(mxBeanNode, "include", false);
		for(Node includeNode: includes) {
			if(includeNode != null) {				
				try {
					nodeValue = null;
					nodeValue = includeNode.getFirstChild().getNodeValue();
					includeMXBeans.add(new ObjectName(nodeValue));
				} catch (Exception e) {
					log.warn("Include ObjectName Invalid [" + nodeValue + "]");
				}			
			}			
		}
		List<Node>excludes = XMLHelper.getChildNodesByName(mxBeanNode, "exclude", false);
		for(Node excludeNode: excludes) {
			if(excludeNode != null) {				
				try { 
					nodeValue = null;
					nodeValue = excludeNode.getFirstChild().getNodeValue();
					excludeMXBeans.add(new ObjectName(nodeValue));
				} catch (Exception e) {
					log.warn("Exclude ObjectName Invalid [" + nodeValue + "]");
				}			
			}			
		}		
	}
	
	
	/**
	 * The MXBean collection configuration XML element
	 * @return An XML Element for MXBean configuration.
	 */
	@JMXAttribute(description="The MXBean collection configuration XML element", name="MXBeanCollection")
	public Element getMXBeanCollection() {
		return mxBeanConfig;
	}
	
	/**
	 * Indicates if MXBean collection should occur on each poll.
	 * @return the mXBeans if true, MXBeans will be traced.
	 */
	@JMXAttribute(description="Indicates if MXBean collection should occur on each poll.", name="MXBeans")
	public boolean getMXBeans() {
		return mXBeans;
	}

	/**
	 * Indicates if MXBean collection should occur on each poll.
	 * @param beans the mXBeans to set
	 */
	public void setMXBeans(boolean mXBeans) {
		this.mXBeans = mXBeans;
		//mXBeanSegment = tracer.buildSegment(beans);
	}	
	
	/**
	 * Determines if a MXBean objectName should be collected based on the include and exclude contraints. 
	 * If the target is neither included or excluded, it will default to include.
	 * @param target The MXBean ObjectName to test.
	 * @return true if it should be collected, false if it should not.
	 */
	protected boolean shouldBeCollected(ObjectName target) {
		if(traceMXBeans.containsKey(target)) {
			return traceMXBeans.get(target);
		} else {
			// first examine includes
			if(includeMXBeans.contains(target)) {
				traceMXBeans.put(target, true);
				return true;
			}
			for(ObjectName on: includeMXBeans) {
				if(on.apply(target)) {
					traceMXBeans.put(target, true);
					return true;
				}
			}
			// examine excludes
			if(excludeMXBeans.contains(target)) {
				traceMXBeans.put(target, false);
				return false;
			}
			for(ObjectName on: excludeMXBeans) {
				if(on.apply(target)) {
					traceMXBeans.put(target, false);
					return false;
				}
			}
			// default is to include
			traceMXBeans.put(target, true);
			return true;
		}		
	}
	
	
	/**
	 * Acquires a new MBeanServer Connection
	 * @throws Exception
	 */
	protected void initMBeanServerConnection() throws Exception {
		try {
			if(connectionFactoryClass==null) {
				connectionFactoryClass = Class.forName(connectionFactoryClassName, true, getClass().getClassLoader());
			}
			if(connectionFactory==null) {
				connectionFactory = (MBeanServerConnectionFactory)connectionFactoryClass.newInstance();
				connectionFactory.setProperties(connectionFactoryProperties);
			}
			mBeanServerConnection = connectionFactory.getMBeanServerConnection();
		} catch (Exception e) {
			log.error("Failed to Acquire MBean Server Connection", e );
			throw e;
		}
	}
	
	/**
	 * Collects JMX Attribute values.
	 * @see com.heliosapm.wiex.server.collectors.BaseCollector#collect()
	 */
	@JMXOperation(description="Collects JMX Attributes", expose=true, name="collect")
	public void collect() {
		Context ctx = null;
		tracer = TracerFactory.getInstance();
		if(log.isDebugEnabled())log.debug(objectName.toString() + ": Collection Executed");
		long start = System.currentTimeMillis();
		Object result = null;
		try {
			if(mBeanServerConnection==null) {
				initMBeanServerConnection();
				resetEnv();
			}	
		} catch (Exception e) {
			if(logErrors) log.error("Failed to get MBeanServerConnection:" + e);
			if(availabilitySegmentName!=null) {
				tracer.recordCounterMetric(availabilitySegmentName, "Availability", 0);
			}
			traceDefaults();
			return;
		}	
		// Availability Check
		if(availabilityMBean != null && availabilityAttribute != null) {
			try {
				long st = System.currentTimeMillis();
				mBeanServerConnection.getAttribute(availabilityMBean, availabilityAttribute);
				long elapsed = System.currentTimeMillis() - st;
				tracer.recordCounterMetric(availabilitySegmentName, "Availability", 1);
				tracer.recordCounterMetric(availabilitySegmentName, "Response Time", elapsed);
			} catch (Exception e) {
				tracer.recordCounterMetric(availabilitySegmentName, "Availability", 0);
				if(logErrors) {
					log.error("Exception Running Availability Check for [" + availabilityMBean + "/" + availabilityAttribute, e);
				}
			}
		}
		for(JMXAttributeTrace tr: attributeTraces) {
			String traceName = null;
			String metricName = null;
			try {
				Iterator mbeans = mBeanServerConnection.queryNames(tr.getObjectName(), null).iterator();
				while(mbeans.hasNext()) {
					// Replace tokens on segmentName and metricName
					ObjectName on = (ObjectName)mbeans.next();
					result = mBeanServerConnection.getAttribute(on, tr.getAttributeName());
					if(tr.isMandatory() || tr.isGroovyTracers()) {  
						// isMandatory=true means there is a simple tracer, or no objectTracers,
						// so the metricName and tracer type need to be read.
						traceName = tr.getTraceName();
						traceName = formatName(traceName);	
						if(traceName.contains("{TARGET")) {
							traceName = formatName(traceName, on);
						}
						metricName = tr.getMetricName();
						metricName = formatName(metricName);
						if(metricName.contains("{TARGET")) {
							metricName = formatName(metricName, on);
						}
						if(tr.getSimpleObjectTracer()!=null) {
							// if a simple object tracer is configured, trace it
							recordTrace(traceName, metricName, tr.getSimpleObjectTracer().renderTracingValue(result), tr.getTracerType());
						} else {
							// if no simple tracer is defined, only do a plain trace if no object tracers are defined.
							if(tr.getObjectTracers().size()<1) {
								recordTrace(traceName, metricName, result.toString(), tr.getTracerType());
							}
						}
					}
					for(ObjectTracer objectTracer: tr.getObjectTracers()) {
						try {
							try { ctx = new InitialContext(); } catch (Exception e) {};
							List<RenderedTrace> renderedTraces = null;
							if(objectTracer instanceof GroovyObjectTracer) {
								objectTracer.renderTracingValue(result, "remoteMBeanServer", mBeanServerConnection, "localMBeanServer", mbeanServer ,"remoteObjectName", on, "localObjectName", objectName, "tracer", tracer, "segmentPrefix", segmentPrefix, "jndi", ctx);
							} else {
								objectTracer.renderTracingValue(result);
							}
							
							if(renderedTraces!=null) for(RenderedTrace renderedTrace: renderedTraces) {
								// Apply the object name property token substitution to the property values.
								String segment = renderedTrace.getSegment();
								segment = formatName(segment);																
								if(renderedTrace.getFrequency() != null && renderedTrace.getTimeUnit() != null) {									
									recordTrace(tracer.buildSegment(segmentPrefix, false, segment), renderedTrace.getMetric(), renderedTrace.getValue(), renderedTrace.getType(), renderedTrace.getFrequency(), renderedTrace.getTimeUnit());
								} else {
									recordTrace(tracer.buildSegment(segmentPrefix, false, segment), renderedTrace.getMetric(), renderedTrace.getValue(), renderedTrace.getType());
								}
							}
						} catch (Exception e) {
							if(logErrors) {
								log.error("Failed to process object tracer: " + objectTracer, e);
							}
						}
					}
				}				
			} catch (Exception e) {
				mBeanServerConnection = null;
				if(logErrors) log.error("Failed to read from MBeanServer:" + e);
				if(tr.getDefaultValue()!=null) {
					if(tr.getDefaultValue()!=null) {
						try {
							recordTrace(tracer.buildSegment(segmentPrefix, false, formatName(tr.getTraceName())), formatName(tr.getMetricName()), tr.getDefaultValue(), tr.getTracerType());
						} catch (Exception e2) {}
					}
				}
			}
		}
		try {
			long startMX = System.currentTimeMillis();
			if(mXBeans) {
				collectMXBeans();
				tracer.recordMetric(mXBeanSegment, "Collection Elapsed Time", System.currentTimeMillis()-startMX);
			}			
		} catch (Exception mxe) {
			if(logErrors) {
				log.error("MXBean Collection Error", mxe);
			}
		}
		collectTime = System.currentTimeMillis()-start;
		if(traceCollectionTime) {
			TracerFactory.getInstance().recordMetric(MODULE, "Collect Time", collectTime);
		}
	}
	

	
	
	/**
	 * Iterates through all registered attributes and traces the default, if defined. 
	 */
	protected void traceDefaults() {
		for(JMXAttributeTrace tr: attributeTraces) {
			if(tr.getDefaultValue()!=null) {
				try {
					//recordTrace(tracer.buildSegment(segmentPrefix, false, formatName(tr.getTraceName())), formatName(tr.getMetricName()), tr.getDefaultValue(), tr.getTracerType());
					recordTrace(formatName(tr.getTraceName()), formatName(tr.getMetricName()), tr.getDefaultValue(), tr.getTracerType());
				} catch (Exception e) {}
			}
			
		}
	}
	
	
	
	/**
	 * Executes the default MXBean collection
	 * @throws Exception
	 */
	protected void collectMXBeans() throws Exception {
		if(!mXBeans) return;
		// Collect Heap and Non Heap
		processMemory();
		// Collect Memory Pools
		processMemoryPools();
		// Collect GC
		processGCStats();
		// Collect Class Loading
		processClassLoading();
		// Compile Time
		processCompiler();
		// Threads
		processThreads();
		// Runtime Env
		processRuntime();
		
		if(mbeanQueryAttempted<=mbeanQueryAttempts) {
			mbeanQueryAttempted++;
		}	
	}
	
	/**
	 * One time collection of runtime MXBean stats
	 */
	protected void processRuntime() {
		ObjectName runTimeMXBean = null;
		try {
			if(runtimeCollected) return;
			String rootSegment = tracer.buildSegment(mXBeanSegment, false, "Runtime");
			runTimeMXBean = mxBeanObjectNames.get(ManagementFactory.RUNTIME_MXBEAN_NAME); 
			if(runTimeMXBean==null) {
				runTimeMXBean = new ObjectName(ManagementFactory.RUNTIME_MXBEAN_NAME);
				mxBeanObjectNames.put(ManagementFactory.RUNTIME_MXBEAN_NAME, runTimeMXBean);
			}
			if(!shouldBeCollected(runTimeMXBean)) {
				runtimeCollected = true;
				return;			
			}

			long startTime = (Long)mBeanServerConnection.getAttribute(runTimeMXBean, "StartTime");
			String[] inputArguments = (String[])mBeanServerConnection.getAttribute(runTimeMXBean, "InputArguments");
			StringBuilder buff = new StringBuilder();
			for(String s: inputArguments) {
				buff.append(s).append("\n");
			}
			
			tracer.recordTimeStamp(rootSegment, "Start Time", startTime);
			tracer.recordMetric(rootSegment, "JVM Input Arguments", buff.toString());
			runtimeCollected = true;
		} catch (InstanceNotFoundException ine) {
			if(logErrors) { log.warn("MXBean Collector (" + objectName + ") Could Not Locate MBean " + runTimeMXBean); }
		} catch (Exception e) {
			if(logErrors) {
				log.error("Failed to process MXBean Runtime Stats", e);
			}
			runtimeCollected = false;
		}
	}		
	
	
	/**
	 * Collects Thread stats
	 */
	protected void processThreads() {
		ObjectName threadMXBean = null;
		try {
			long totalStartedThreads = 0;
			long activeThreads = 0;
			long daemonThreads = 0;
			long nonDaemonThreads = 0;
			long monitorLockedThreads[] = null;
			ThreadInfo threadInfo = null;
			long totalBlockTime = 0;
			long totalBlockCount = 0;
			long totalWaitTime = 0;
			long totalWaitCount = 0;
			
			String rootSegment = tracer.buildSegment(mXBeanSegment, false, "Threads");
			threadMXBean = mxBeanObjectNames.get(ManagementFactory.THREAD_MXBEAN_NAME); 
			if(threadMXBean==null) {
				threadMXBean = new ObjectName(ManagementFactory.THREAD_MXBEAN_NAME);
				mxBeanObjectNames.put(ManagementFactory.THREAD_MXBEAN_NAME, threadMXBean);
			}
			if(!shouldBeCollected(threadMXBean)) return;
			AttributeList attrs = mBeanServerConnection.getAttributes(threadMXBean, THREAD_STATS);
			//public static final String[] THREAD_STATS = new String[]{"ThreadCount", "DaemonThreadCount", "TotalStartedThreadCount"};
			activeThreads = (Integer)getValue(attrs, THREAD_STATS[0]);
			daemonThreads = (Integer)getValue(attrs, THREAD_STATS[1]);
			totalStartedThreads = (Long)getValue(attrs, THREAD_STATS[2]);
			nonDaemonThreads = activeThreads - daemonThreads;
			
			tracer.recordCounterMetricDelta(rootSegment, "Threads Started", totalStartedThreads);
			tracer.recordCounterMetric(rootSegment, "Active Threads", activeThreads);
			tracer.recordCounterMetric(rootSegment, "Daemon Threads", daemonThreads);
			tracer.recordCounterMetric(rootSegment, "Non Daemon Threads", nonDaemonThreads);
			
			long tmStart = System.currentTimeMillis();
			long tmElapsed = 0;
			
			if(deadLockMonitor) {
				monitorLockedThreads = (long[])mBeanServerConnection.invoke(threadMXBean, "findMonitorDeadlockedThreads", NULL_ARG, NULL_SIG);
				if(monitorLockedThreads==null) {
					tracer.recordCounterMetric(rootSegment, "Monitor Deadlocked Threads", 0);					
				} else {
					tracer.recordCounterMetric(rootSegment, "Monitor Deadlocked Threads", monitorLockedThreads.length);
					if(supportsThreadContention==null) {					
						supportsThreadContention = (Boolean)mBeanServerConnection.getAttribute(threadMXBean, "ThreadContentionMonitoringSupported");
						if(supportsThreadContention) {
							boolean enabled = (Boolean)mBeanServerConnection.getAttribute(threadMXBean, "ThreadContentionMonitoringEnabled");
							if(!enabled) {
								try {
									mBeanServerConnection.setAttribute(threadMXBean, new Attribute("ThreadContentionMonitoringEnabled", true));
								} catch (Exception e) {
									log.warn("Failed to enable ThreadContentionMonitoring for" + objectName, e);
									supportsThreadContention = false;
								}
							}
						}
					}
					if(supportsThreadContention) {
						rootSegment = tracer.buildSegment(mXBeanSegment, false, "Threads", "Deadlocks");						
						CompositeData[] infos = (CompositeData[])mBeanServerConnection.invoke(threadMXBean, "getThreadInfo", new Object[]{monitorLockedThreads}, new String[]{"[J"});
						for(CompositeData info: infos) {
							threadInfo = ThreadInfo.from(info);
							totalBlockCount += threadInfo.getBlockedCount();
							totalBlockTime += threadInfo.getBlockedTime();
							totalWaitCount += threadInfo.getWaitedCount();
							totalWaitTime += threadInfo.getWaitedTime();
						}
						tracer.recordCounterMetricDelta(rootSegment, "Block Count", totalBlockCount);
						tracer.recordCounterMetricDelta(rootSegment, "Block Time", totalBlockTime);
						tracer.recordCounterMetricDelta(rootSegment, "Wait Count", totalWaitCount);
						tracer.recordCounterMetricDelta(rootSegment, "Wait Time", totalWaitTime);
					}
				}
				tmElapsed = System.currentTimeMillis() - tmStart;
				tracer.recordMetric(mXBeanSegment, "Deadlock Monitor Elapsed Time", tmElapsed);
			}
			if(threadMonitor) {
				rootSegment = tracer.buildSegment(mXBeanSegment, false, "Threads");
				tmStart = System.currentTimeMillis();
				long[] allThreads = (long[])mBeanServerConnection.getAttribute(threadMXBean, "AllThreadIds");
				CompositeData[] infos = (CompositeData[])mBeanServerConnection.invoke(threadMXBean, "getThreadInfo", new Object[]{allThreads}, new String[]{"[J"});
				tmElapsed = System.currentTimeMillis()-tmStart;				
				tracer.recordMetric(mXBeanSegment, "Thread Monitor Elapsed Time", tmElapsed);
				rootSegment = tracer.buildSegment(mXBeanSegment, false, "Threads", "States");
				resetThreadStatus();
				for(CompositeData info: infos) {
					threadInfo = ThreadInfo.from(info);
					incrementThreadState(threadInfo.getThreadState());
				}	
				for(Entry<Thread.State, Integer> entry: threadStates.entrySet()) {
					tracer.recordCounterMetric(rootSegment, entry.getKey().toString(), entry.getValue());
				}
				
			}
		} catch (InstanceNotFoundException ine) {
			if(logErrors) { log.warn("MXBean Collector (" + objectName + ") Could Not Locate MBean " + threadMXBean); }
		} catch (Exception e) {
			if(logErrors) {
				log.error("Failed to process MXBean Threading Stats", e);
			}
		}
	}
	
	/**
	 * Resets all thread counts.
	 */
	protected void resetThreadStatus() {
		for(Thread.State state: threadStates.keySet()) {
			threadStates.put(state, 0);
		}
	}
	
	/**
	 * Increments the thread status counter.
	 * @param state The thread state to increment.
	 */
	protected void incrementThreadState(Thread.State state) {
		threadStates.put(state, threadStates.get(state)+1);
	}
	
	/**
	 * Retreieves a named value from an attribute list.
	 * @param al
	 * @param s
	 * @return The attribute value, or null if it is not found.
	 */
	protected Object getValue(AttributeList al, String s) {
		for(Attribute attr: al.asList()) {
			if(s.equals(attr.getName())) return attr.getValue();
		}
		return null;
	}
	
	/**
	 * Collects JIT Compiler stats
	 */
	protected void processCompiler() {
		ObjectName jitMXBean = null;
		try {
			if(supportsCompilerTime != null && !supportsCompilerTime) return;
			String rootSegment = tracer.buildSegment(mXBeanSegment, false, "JIT Compiler");
			jitMXBean = mxBeanObjectNames.get(ManagementFactory.COMPILATION_MXBEAN_NAME); 
			if(jitMXBean==null) {
				jitMXBean = new ObjectName(ManagementFactory.COMPILATION_MXBEAN_NAME);
				mxBeanObjectNames.put(ManagementFactory.COMPILATION_MXBEAN_NAME, jitMXBean);
			}
			if(!shouldBeCollected(jitMXBean)) return;			
			if(supportsCompilerTime==null) {
				supportsCompilerTime = (Boolean)mBeanServerConnection.getAttribute(jitMXBean, "CompilationTimeMonitoringSupported");
			}
			if(!supportsCompilerTime) return;
			tracer.recordCounterMetricDelta(rootSegment, "Compile Time", (Long)mBeanServerConnection.getAttribute(jitMXBean, "TotalCompilationTime"));
		} catch (InstanceNotFoundException ine) {
			if(logErrors) { log.warn("MXBean Collector (" + objectName + ") Could Not Locate MBean " + jitMXBean); }			
		} catch (Exception e) {
			if(logErrors) {
				log.error("Failed to process MXBean Class Loading Stats", e);
			}
		}
	}	
	
	/**
	 * Collects memory stats
	 */
	protected void processClassLoading() {
		ObjectName clMXBean = null;
		try {
			String rootSegment = null;
			AttributeList stats = null;
			clMXBean = mxBeanObjectNames.get(ManagementFactory.CLASS_LOADING_MXBEAN_NAME); 
			if(clMXBean==null) {
				clMXBean = new ObjectName(ManagementFactory.CLASS_LOADING_MXBEAN_NAME);
				mxBeanObjectNames.put(ManagementFactory.CLASS_LOADING_MXBEAN_NAME, clMXBean);
			}
			if(!shouldBeCollected(clMXBean)) return;
			rootSegment = tracer.buildSegment(mXBeanSegment, false, "Class Loading");
			stats = mBeanServerConnection.getAttributes(clMXBean, CLASS_LOADING_STATS);
			for(Attribute attr: stats.asList()) {
				if("LoadedClassCount".equals(attr.getName())) {
					tracer.recordCounterMetric(rootSegment, attr.getName(), (Integer)attr.getValue());
				} else {
					tracer.recordCounterMetricDelta(rootSegment, attr.getName(), (Long)attr.getValue());
				}
			}
		} catch (InstanceNotFoundException ine) {
			if(logErrors) { log.warn("MXBean Collector (" + objectName + ") Could Not Locate MBean " + clMXBean); }			
		} catch (Exception e) {
			if(logErrors) {
				log.error("Failed to process MXBean Class Loading Stats", e);
			}
		}
	}
	
	
	/**
	 * Collects memory pool stats
	 */
	protected void processMemoryPools() {
		try {
			String rootSegment = null;
			String poolType = null;
			CompositeDataSupport  usage = null;			
			if(memoryPoolObjectNames==null) {
				memoryPoolObjectNames = new HashMap<String, ObjectName>();
				try {
					memoryPoolMXBean = new ObjectName(ManagementFactory.MEMORY_POOL_MXBEAN_DOMAIN_TYPE + ",*");
					if(!shouldBeCollected(memoryPoolMXBean)) return;
					Set<ObjectName> memoryPools = (Set<ObjectName>)mBeanServerConnection.queryNames(memoryPoolMXBean, null);
					if(memoryPools.size()==0) {
						if(mbeanQueryAttempted<mbeanQueryAttempts) {
							memoryPoolObjectNames=null;
							return;
							// will retry
						} 
					}
					for(ObjectName on: memoryPools) {
						if(shouldBeCollected(on)) {
							memoryPoolObjectNames.put(on.getKeyProperty("name"), on);
						}
					}
				} catch (Exception e) {
					// If an error occurs finding matching MBeans, it is probably because the target MBean was not available.
					// Set the collection to null and retry next time.
					memoryPoolObjectNames=null;
					return;
				}
					
			}
			if(!shouldBeCollected(memoryPoolMXBean)) return;
			
			for(Entry<String, ObjectName> entry: memoryPoolObjectNames.entrySet()) {
				poolType = (String)mBeanServerConnection.getAttribute(entry.getValue(), "Type");
				usage = (CompositeDataSupport)mBeanServerConnection.getAttribute(entry.getValue(), "Usage");
				rootSegment = tracer.buildSegment(mXBeanSegment, false, "Memory Pools", poolType, entry.getKey());
				for(String key: (Set<String>)usage.getCompositeType().keySet()) {
					tracer.recordCounterMetric(rootSegment, key, (Long)usage.get(key));
				}
				getPercentUsedOfCommited(usage, rootSegment);
				getPercentUsedOfCapacity(usage, rootSegment);
				
			}
		} catch (InstanceNotFoundException ine) {
			if(logErrors) { log.warn("MXBean Collector (" + objectName + ") Could Not Locate MBean " + memoryPoolMXBean); }						
		} catch (Exception e) {
			if(logErrors) {
				log.error("Failed to process MXBean Memory Pool Stats", e);
			}			
		}
	}
	
	/**
	 * Collects garbage collector stats.
	 */
	protected void processGCStats() {
		try {
			String rootSegment = null;
			CompositeDataSupport  usage = null;			
			long collectionCount = 0;
			long collectionTime = 0;
			long elapsedTime = 0;
			long elapsedGCTime = 0;
			long currentTime = 0;
			long percentGCTime = 0;
			boolean pollGCPercent = false;
			if(gcObjectNames==null) {
				gcObjectNames = new HashMap<String, ObjectName>();
				try {
					gcMXBean = new ObjectName(ManagementFactory.GARBAGE_COLLECTOR_MXBEAN_DOMAIN_TYPE + ",*");
					if(!shouldBeCollected(gcMXBean)) return;
					Set<ObjectName> gcs = (Set<ObjectName>)mBeanServerConnection.queryNames(gcMXBean, null);
					if(gcs.size()==0) {
						if(mbeanQueryAttempted<mbeanQueryAttempts) {
							gcObjectNames=null;
							return;
							// will retry
						} 
					}
					
					for(ObjectName on: gcs) {
						if(shouldBeCollected(on)) {
							gcObjectNames.put(on.getKeyProperty("name"), on);
						}
					}
				} catch (Exception e) {
					// If an error occurs finding matching MBeans, it is probably because the target MBean was not available.
					// Set the collection to null and retry next time.
					gcObjectNames=null;
					return;
				}
			}	
			if(!shouldBeCollected(gcMXBean)) return;
			gCPolledCycles++;
			if(gCPolledCycles>gCPollCycles) {
				pollGCPercent=true;
				gCPolledCycles=0;
			} else {
				pollGCPercent=false;
			}
			for(Entry<String, ObjectName> entry: gcObjectNames.entrySet()) {
				rootSegment = tracer.buildSegment(mXBeanSegment, false, "Garbage Collectors", entry.getKey());
				collectionCount = (Long)mBeanServerConnection.getAttribute(entry.getValue(), "CollectionCount");
				collectionTime = (Long)mBeanServerConnection.getAttribute(entry.getValue(), "CollectionTime");
				currentTime = System.currentTimeMillis();
				tracer.recordCounterMetricDelta(rootSegment, "Collection Time", collectionTime);
				tracer.recordCounterMetricDelta(rootSegment, "Collection Count", collectionCount);
				
				if(pollGCPercent) {
					if(gcTimes.containsKey(entry.getKey())) {
						long[] times = gcTimes.get(entry.getKey());
						elapsedTime = times[0] - currentTime;
						elapsedGCTime = times[1] - collectionTime;
						try {
							percentGCTime = percent(elapsedGCTime, elapsedTime);
							tracer.recordCounterMetric(rootSegment, "% Time Spent in GC", percentGCTime);
						} catch (Exception e) {}
					} 
					gcTimes.put(entry.getKey(), new long[]{currentTime, collectionTime});					
				}
			}			
		} catch (InstanceNotFoundException ine) {
			if(logErrors) { log.warn("MXBean Collector (" + objectName + ") Could Not Locate MBean " + gcMXBean); }									
		} catch (Exception e) {
			if(logErrors) {
				log.error("Failed to process MXBean Garbage Collector Stats", e);
			}			
		}
	}
	
	
	/**
	 * Collects memory stats
	 */
	protected void processMemory() {
		ObjectName memoryMXBean = null;
		try {
			String rootSegment = null;
			
			memoryMXBean = mxBeanObjectNames.get(ManagementFactory.MEMORY_MXBEAN_NAME); 
			if(memoryMXBean==null) {
				memoryMXBean = new ObjectName(ManagementFactory.MEMORY_MXBEAN_NAME);
				mxBeanObjectNames.put(ManagementFactory.MEMORY_MXBEAN_NAME, memoryMXBean);
			}
			if(!shouldBeCollected(memoryMXBean)) return;
			CompositeDataSupport heap = (CompositeDataSupport) mBeanServerConnection.getAttribute(memoryMXBean, "HeapMemoryUsage");
			CompositeDataSupport nonHeap = (CompositeDataSupport) mBeanServerConnection.getAttribute(memoryMXBean, "NonHeapMemoryUsage");
			rootSegment = tracer.buildSegment(mXBeanSegment, false, "Memory", "Heap Memory Usage");
			for(String key: (Set<String>)heap.getCompositeType().keySet()) {
				tracer.recordCounterMetric(rootSegment, key, (Long)heap.get(key));
			}
			getPercentUsedOfCommited(heap, rootSegment);
			getPercentUsedOfCapacity(heap, rootSegment);
			rootSegment = tracer.buildSegment(mXBeanSegment, false, "Memory", "Non Heap Memory Usage");
			for(String key: (Set<String>)nonHeap.getCompositeType().keySet()) {
				tracer.recordCounterMetric(rootSegment, key, (Long)heap.get(key));
			}
			getPercentUsedOfCommited(nonHeap, rootSegment);
			getPercentUsedOfCapacity(nonHeap, rootSegment);
			
			rootSegment = tracer.buildSegment(mXBeanSegment, false, "Memory");
			tracer.recordCounterMetric(rootSegment, "Objects Pending Finalization", (Integer)mBeanServerConnection.getAttribute(memoryMXBean, "ObjectPendingFinalizationCount"));
		} catch (InstanceNotFoundException ine) {
			if(logErrors) { log.warn("MXBean Collector (" + objectName + ") Could Not Locate MBean " + memoryMXBean); }												
		} catch (Exception e) {
			if(logErrors) {
				log.error("Failed to process MXBean Memory Stats", e);
			}
		}
	}
	
	/**
	 * Calculates the percentage of commited memory in use.
	 * @param cd A CompositeDataSupport that can generate a MemoryUsage.
	 * @param rootSegment The root segment for the tracing. Will not trace if this is zero length.
	 * @return the percentage of commited memory in use
	 */
	protected long getPercentUsedOfCommited(CompositeDataSupport cd, String...rootSegment) {
		long value = -1;
		MemoryUsage memoryUsage = MemoryUsage.from(cd);
		try {
			value = percent(memoryUsage.getUsed(), memoryUsage.getCommitted());
			if(rootSegment.length>0) {
				tracer.recordCounterMetric(rootSegment[0], "Used %", value);
			}
		} catch (Exception e) {
			value = -1;
		}		
		return value;
	}
	
	/**
	 * Calculates the percentage of total capacity memory in use.
	 * @param cd A CompositeDataSupport that can generate a MemoryUsage.
	 * @param rootSegment The root segment for the tracing. Will not trace if this is zero length.
	 * @return percentage of total capacity memory in use.
	 */
	protected long getPercentUsedOfCapacity(CompositeDataSupport cd, String...rootSegment) {
		long value = -1;
		MemoryUsage memoryUsage = MemoryUsage.from(cd);
		try {
			value = percent(memoryUsage.getUsed(), memoryUsage.getMax());
			if(rootSegment.length>0) {
				tracer.recordCounterMetric(rootSegment[0], "Capacity %", value);
			}
			
		} catch (Exception e) {
			value = -1;
		}		
		return value;
	}
	
	
	/**
	 * The number of attributes being monitored
	 * @return The number of attributes being monitored
	 */
	@JMXAttribute(description="The number of attributes being monitored.", name="AttributeCount")
	public int getAttributeCount() {
		return attributeTraces.size();
	}
	
	/**
	 * Reports Configured JMX Attributes
	 * @return A string representing the configured JMX Attributes ot be collected.`
	 * @see java.lang.Object#toString()
	 */
	@JMXOperation(description="Reports Configured JMX Attributes", expose=true, name="Report")
	public String toString() {
		StringBuilder builder = new StringBuilder();
		for(JMXAttributeTrace jt: attributeTraces) {
			builder.append(jt.toString()).append("\n");
		}		
		return builder.toString();
	}
	
	/**
	 * The MBeanServerConnectionFactory class name.
	 * @return the connectionFactoryClassName
	 */
	@JMXAttribute(description="The MBeanServerConnectionFactory class name.", name="ConnectionFactoryClassName")
	public String getConnectionFactoryClassName() {
		return connectionFactoryClassName;
	}

	/**
	 * Sets the MBeanServerConnectionFactory class name.
	 * @param connectionFactoryClassName the connectionFactoryClassName to set
	 */
	public void setConnectionFactoryClassName(String connectionFactoryClassName) {
		this.connectionFactoryClassName = connectionFactoryClassName;		
	}

	/**
	 * The MBeanServerConnectionFactory connection properties.
	 * @return the connectionFactoryProperties
	 */
	@JMXAttribute(description="The MBeanServerConnectionFactory connection properties.", name="ConnectionFactoryProperties")
	public Properties getConnectionFactoryProperties() {
		return connectionFactoryProperties;
	}

	/**
	 * Sets the MBeanServerConnectionFactory connection properties.
	 * @param connectionFactoryProperties the connectionFactoryProperties to set
	 */
	public void setConnectionFactoryProperties(
			Properties connectionFactoryProperties) {
		this.connectionFactoryProperties = connectionFactoryProperties;
	}

	/**
	 * Returns the attribute of the target availability MBean
	 * @return the availabilityAttribute
	 */
	@JMXAttribute(description="The attribute of the target availability MBean", name="AvailabilityAttribute")
	public String getAvailabilityAttribute() {
		return availabilityAttribute;
	}

	/**
	 * Sets the attribute of the target availability MBean.
	 * @param availabilityAttribute the availabilityAttribute to set
	 */
	public void setAvailabilityAttribute(String availabilityAttribute) {
		this.availabilityAttribute = availabilityAttribute;
	}

	/**
	 * Gets target availability MBean
	 * @return the availabilityMBean
	 */
	@JMXAttribute(description="The target availability MBean", name="AvailabilityMBean")
	public ObjectName getAvailabilityMBean() {
		return availabilityMBean;
	}

	/**
	 * Sets target availability MBean
	 * @param availabilityMBean the availabilityMBean to set
	 */
	public void setAvailabilityMBean(ObjectName availabilityMBean) {
		this.availabilityMBean = availabilityMBean;
	}

	/**
	 * Gets tracing segment of the availability check
	 * @return the availabilitySegment
	 */
	@JMXAttribute(description="The tracing segment of the availability check", name="AvailabilitySegment")
	public String[] getAvailabilitySegment() {
		return availabilitySegment;
	}

	/**
	 * Sets tracing segment of the availability check
	 * @param availabilitySegment the availabilitySegment to set
	 */
	public void setAvailabilitySegment(String[] availabilitySegment) {
		
		this.availabilitySegment = availabilitySegment;
		availabilitySegmentName = tracer.buildSegment(false, availabilitySegment);
	}

	/**
	 * @return the availabilitySegmentName
	 */
	@JMXAttribute(description="The compiled availability segment name.", name="AvailabilitySegmentName")
	public String getAvailabilitySegmentName() {
		return availabilitySegmentName;
	}



	/**
	 * @return the deadLockMonitor
	 */
	@JMXAttribute(description="Indicates if MXBean collection should run Deadlocked Thread Detection. (Expensive)", name="DeadLockMonitor")
	public boolean isDeadLockMonitor() {
		return deadLockMonitor;
	}

	/**
	 * @param deadLockMonitor the deadLockMonitor to set
	 */
	public void setDeadLockMonitor(boolean deadLockMonitor) {
		this.deadLockMonitor = deadLockMonitor;
	}

	/**
	 * @return the threadMonitor
	 */
	@JMXAttribute(description="Indicates if MXBean collection should run Thread Status Summary. (Expensive)", name="ThreadMonitor")
	public boolean isThreadMonitor() {
		return threadMonitor;
	}

	/**
	 * @param threadMonitor the threadMonitor to set
	 */
	public void setThreadMonitor(boolean threadMonitor) {
		this.threadMonitor = threadMonitor;
	}

	/**
	 * @return the mbeanQueryAttempts
	 */
	@JMXAttribute(description="The number of times subsidiary MBeans should be queried for.", name="MBeanQueryAttempts")
	public int getMBeanQueryAttempts() {
		return mbeanQueryAttempts;
	}

	/**
	 * @param mbeanQueryAttempts the mbeanQueryAttempts to set
	 */
	public void setMBeanQueryAttempts(int mbeanQueryAttempts) {
		this.mbeanQueryAttempts = mbeanQueryAttempts;
	}

	/**
	 * The number of elapsed collections that should occur before GC % time is calculated
	 * @return the gCPollCycles
	 */
	@JMXAttribute(description="The number of elapsed collections that should occur before GC % time is calculated.", name="GCPollCycles")
	public int getGCPollCycles() {
		return gCPollCycles;
	}

	/**
	 * @param pollCycles the gCPollCycles to set
	 */
	public void setGCPollCycles(int pollCycles) {
		gCPollCycles = pollCycles;
	}

	/**
	 * The number of elapsed collections that have occured since GC % time was calculated.
	 * @return the gCPolledCycles
	 */
	@JMXAttribute(description="The number of elapsed collections that have occured since GC % time was calculated.", name="GCPolledCycles")
	public int getGCPolledCycles() {
		return gCPolledCycles;
	}
	
	
}
