
import javax.management.j2ee.statistics.*;
import javax.management.*;
import javax.naming.Context;
import org.jboss.invocation.*;
import com.adp.sbs.metrics.tracing.*;


/**
 * Reference implementation of a base class for managing the collection and reset of JSR77 stats from a remote server.
 * Whitehead, 8/7/2007
 * $Revision: 1.1 $
 * Additionally support j2eeTypes: JCAResource, JTAResource, JVM  (No current reset or concurrent management jsp)
 */

class JSR77Collector  {
	
	/** JMX Invocation Parameters */
	public static final String[] nullSig = [];
	public static final Object[] nullArg = [];
	
	/** State Control */
	public HashMap state = null;
	public Binding groovyBinding = null;
	
	/** SBSTracing Tracer */
	ITracer tracer = null;
	
	/** Error Logging */
	public boolean bLogErrors = false;
	
	/** reset frequency management */
	public int resetCount = 0;
	public int resetFrequency = 0;
	
	/** remote object simple name and type*/
	public String j2eeObject = null;
	public String j2eeObjectType = null;
	
	/** Segment names and ixes*/
	String segmentName = null;
	String segmentNameNS = null;
	String segmentSuff = null;
	String segmentPref = null;
	
	/** Remote Host Details */
	String hostAddress = null;
	String hostName = null;
	int port = 0;
	
	/** Remote WebConsole URLs */
	public URL concurrentURL = null;
	public URL resetInvocUrl = null;
	public URL resetPoolUrl = null;
	
	/** JMX Objects */
	public MBeanServerConnection localAgent = null;
	public MBeanServerConnection remoteAgent = null;
	public ObjectName localMBeanName = null;
	public ObjectName remoteMBeanName = null;
	
	/** Local JNDI Context */
	public Context ctx = null;
	
	
	
	
	public String toString() {
		StringBuilder buff = new StringBuilder("JSR77Collector");
		buff.append("\n\tJ2EEObject Name:").append(j2eeObject);
		buff.append("\n\tJ2EEObject Type:").append(j2eeObjectType);
		buff.append("\n\tSegment:").append(segmentName);
		buff.append("\n\tSegmentNS:").append(segmentNameNS);
		buff.append("\n\tRemote Host Address/Http Port/Name:").append(hostAddress).append("/").append(port).append("/").append(hostName);
		buff.append("\n\tLocalMBean:").append(localMBeanName);
		buff.append("\n\tRemoteMBean:").append(remoteMBeanName);
		buff.append("\n\tConcurrentURL:").append(concurrentURL);
		buff.append("\n\tResetInvocationsURL:").append(resetInvocUrl);
		buff.append("\n\tResetPoolURL:").append(resetPoolUrl);
		return buff.toString();
	}
	
	
	/** Creates a new JSR77Collector */
	public void init(groovy.lang.Binding groovyBinding) {
		this.groovyBinding = groovyBinding;
		state = (HashMap)this.groovyBinding.getProperty("stateMap");
		localAgent = (MBeanServerConnection)this.groovyBinding.getProperty("localMBeanServer");
		remoteAgent = (MBeanServerConnection)this.groovyBinding.getProperty("remoteMBeanServer");
		localMBeanName = (ObjectName)this.groovyBinding.getProperty("localObjectName");
		remoteMBeanName = (ObjectName)this.groovyBinding.getProperty("remoteObjectName");
		tracer = (ITracer)this.groovyBinding.getProperty("tracer");
		ctx = (Context)this.groovyBinding.getProperty("jndi");
		segmentSuff = (String)this.groovyBinding.getProperty("segmentSuffix");
		segmentPref = (String)this.groovyBinding.getProperty("segmentPrefix");
		bLogErrors = (Boolean)this.groovyBinding.getProperty("logErrors");
		resetFrequency = (Integer)this.groovyBinding.getProperty("resetFrequency");		
		def tmpStr = remoteMBeanName.getKeyProperty("name");
		if(tmpStr.contains("@")) {
			j2eeObject = tmpStr.split("@")[0];
		} else {
			j2eeObject = tmpStr;
		}
		j2eeObjectType = remoteMBeanName.getKeyProperty("j2eeType");
		hostName = (String)remoteAgent.getAttribute(new ObjectName("jboss.system:type=ServerInfo"), "HostName");
		hostAddress = (String)remoteAgent.getAttribute(new ObjectName("jboss.system:type=ServerInfo"), "HostAddress");
		try {
			def protocolObjectName = new ObjectName("jboss.web:address=%2F${hostAddress},type=ProtocolHandler,*");			
			ObjectName httpProtocolHandler = (ObjectName)remoteAgent.queryNames(protocolObjectName, null).iterator().next();
			port = (Integer)remoteAgent.getAttribute(httpProtocolHandler, "port");	
			println "Acquired HTTP Port:${port}";
		} catch (Exception e) {
			println "Failed To Acquire HTTP Port:${e}\n\tDefaulting to port 8080";
			port = 8080;
		}
		
		segmentName = tracer.buildSegment(segmentPref, true, segmentSuff, hostName, j2eeObjectType, j2eeObject);
		segmentNameNS = tracer.buildSegment(segmentPref, false, segmentSuff, hostName, j2eeObjectType, j2eeObject);
		concurrentURL = getConcurrentURL(j2eeObjectType, remoteMBeanName, hostAddress, port);
		resetInvocUrl = getResetInvocationStatsURL(j2eeObjectType, remoteMBeanName, hostAddress, port);	
		resetPoolUrl = 	getResetPoolStatsURL(j2eeObjectType, remoteMBeanName, hostAddress, port);	
		
	}
	
	/** 
	 *  Builds the URL to the management jsp where concurrent invocations can be acquired.
	 *  Returns null if there is no concurrency stats for the passed type
	 */
	public static URL getConcurrentURL(String type, ObjectName remoteObjectName, String hostAddress, int port) {
		String managementJSP = getJSP(type);
		if(managementJSP==null) return null;
		if(type.equals("StatelessSessionBean") || type.equals("EntityBean") || type.equals("MessageDrivenBean")) {
			return new URL("http://${hostAddress}:${port}/web-console/${getJSP(type)}?ObjectName=${URLEncoder.encode(remoteObjectName.toString(), "UTF-8")}");
		} else {
			return null;
		}
	}

	/** 
	 *  Builds the URL to the management jsp where invocation stats can be reset.
	 *  Returns null if there is no inovcations stats reset for the passed type
	 */
	
	public static URL getResetInvocationStatsURL(String type, ObjectName remoteObjectName, String hostAddress, int port) {
		String managementJSP = getJSP(type);
		if(managementJSP==null) return null;
		if(type.equals("StatelessSessionBean") || type.equals("EntityBean") || type.equals("MessageDrivenBean")) {
			return new URL("http://${hostAddress}:${port}/web-console/${getJSP(type)}?doResetInvoc=true&ObjectName=${URLEncoder.encode(remoteObjectName.toString(), "UTF-8")}");			
		} else if(type.equals("StatelessSessionBean") || type.equals("Servlet")) {
			return new URL("http://${hostAddress}:${port}/web-console/${getJSP(type)}?doReset=true&ObjectName=${URLEncoder.encode(remoteObjectName.toString(), "UTF-8")}");
		} else  {
			return null;
		}
	}
	
	/** 
	 *  Builds the URL to the management jsp where pool stats can be reset.
	 *  Returns null if there is no pool stats reset for the passed type
	 */
	
	public static URL getResetPoolStatsURL(String type, ObjectName remoteObjectName, String hostAddress, int port) {
		String managementJSP = getJSP(type);
		if(managementJSP==null) return null;
		if(type.equals("StatelessSessionBean") || type.equals("EntityBean") || type.equals("MessageDrivenBean")) {			
			return new URL("http://${hostAddress}:${port}/web-console/${getJSP(type)}?doReset=true&ObjectName=${URLEncoder.encode(remoteObjectName.toString(), "UTF-8")}");			
		} else  {
			return null;
		}
	}
	
	
	public static String getJSP(String type) {
		if(type.equals("StatelessSessionBean")) {
			return "StatelessEjb.jsp";
		} else if(type.equals("EntityBean")) {
			return "EntityEjb.jsp";
		} else if(type.equals("MessageDrivenBean")) {
			return "MdbEjb.jsp";
		} else if(type.equals("Queue")) {
			return "Queue.jsp";
		} else if(type.equals("Topic")) {
			return "Topic.jsp";
		} else if(type.equals("Servlet")) {
			return "Servlet.jsp";
		} else  {
			return null;
		} 
		
		
	}
	
	/** Traces JSR77 Stats for the passed object */
	public void processJMXStats(Object jmxObj) {
		jmxObj.getStatistics().each() {
			if(it instanceof TimeStatistic) {		
				tracer.recordCounterMetric("${segmentName}${it.getName()}", "Max Time", it.getMaxTime());
				tracer.recordCounterMetric("${segmentName}${it.getName()}", "Min Time", it.getMinTime()==Long.MAX_VALUE ? 0 : it.getMinTime());
				tracer.recordCounterMetricDelta("${segmentName}${it.getName()}", "Invocation Rate", it.getCount()==Long.MAX_VALUE ? 0 : it.getCount());			
				try { long avg = it.getTotalTime()/it.getCount(); tracer.recordCounterMetric("${segmentName}${it.getName()}", "Avg Time", avg); } catch (Exception e) {}		
			} else if(it instanceof CountStatistic) {
				tracer.recordCounterMetricDelta("${segmentName}${it.getName()}", "Create Rate", it.getCount());
			} else if(it instanceof RangeStatistic) {
				tracer.recordCounterMetric("${segmentName}${it.getName()}", "Current", it.getCurrent());
				tracer.recordCounterMetric("${segmentName}${it.getName()}", "High", it.getHighWaterMark());
				tracer.recordCounterMetric("${segmentName}${it.getName()}", "Low", it.getLowWaterMark());
			}				
		}
		if(concurrentURL!=null) {
			def concurrencies = concurrentURL.getText().split("Actual concurrent invocations:")[1].split("\\)")[0].split("\\(max:");
			tracer.recordCounterMetric("${segmentNameNS}", "Concurrent Calls", Integer.parseInt(concurrencies[0].trim()));
			tracer.recordCounterMetric("${segmentNameNS}", "Maximum Concurrent Calls", Integer.parseInt(concurrencies[1].trim()));				
		}
		resetCount++;
		if(resetCount>=resetFrequency) {
			//println "Issuing Reset On ${j2eeObject}"
			resetCount = 0;
			if(resetInvocUrl!=null) {
				resetInvocUrl.getText();
				//println "Issuing Reset On Invocation Stats:${resetInvocUrl}"
			}
			if(resetPoolUrl!=null) {
				resetPoolUrl.getText();
				//println "Issuing Reset On Pool Stats:${resetPoolUrl}"
			}
		}
	}
}
return JSR77Collector;

