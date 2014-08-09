/**
 * 
 */
package com.heliosapm.wiex.server.collectors.groovy;

import groovy.lang.Binding;
import groovy.lang.GroovyCodeSource;
import groovy.lang.GroovyShell;
import groovy.lang.Script;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import javax.naming.InitialContext;

import org.codehaus.groovy.control.CompilerConfiguration;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.heliosapm.wiex.jmx.dynamic.annotation.JMXAttribute;
import com.heliosapm.wiex.jmx.dynamic.annotation.JMXManagedObject;
import com.heliosapm.wiex.jmx.dynamic.annotation.JMXOperation;
import com.heliosapm.wiex.server.collectors.BaseCollector;

/**
 * <p>Title: GroovyCollector</p>
 * <p>Description: Collector that invokes an arbitrary groovy script.
 * There are two types of groovy supported in the collector:<ul>
 * <li>A script is compiled and has no return. All tracing should be executed in the the script.</li>
 * <li>An evaluate is a groovy expression which returns a String value to the collector which is then traced using the name and type.</li>
 * </ul></p>
 * <p>The following references are injected into the shell's binding:<ul>
 * <li>The MBeanServer that this collector MBean is registered in.</li>
 * <li>The default JNDI context</li>
 * <li>The current ObjectName</li>
 * <li>The collector's tracer.</li>
 * <li>The provided collector properties</li>
 * </ul>
 * </p> 
 * <p>Copyright: Copyright (c) 2007</p>
 * <p>Company: Helios Development Group</p>
 * @author Whitehead
 * @version $Revision: 1.3 $
 */
@JMXManagedObject(annotated=true, declared=false)
public class GroovyCollector extends BaseCollector {
	/**	A map of configured compiled scripts */
	protected Map<String, Script> groovyScripts = new HashMap<String, Script>();
	/**	A map of configured script source code */
	protected Map<String, GroovyCodeSource> groovyScriptCode = new HashMap<String, GroovyCodeSource>();
	/**	A map of configured script source */
	protected Map<String, String> groovyScriptSource = new HashMap<String, String>();	
	/** A Map of configured groovy expressions */
	protected Map<String, ScriptEvaluation> groovyEvaluations = new HashMap<String, ScriptEvaluation>();
	
	/** A binding passed to the groovy shell */
	protected Binding binding = null;
	/** Additional configuration properties passed to the groovy scripts in the binding */
	protected Properties shellProperties = null;
	/** The groovy shells */
	protected Map<String, GroovyShell> shells = new HashMap<String, GroovyShell>();
	

	
	/**
	 * Instantiates new GroovyCollector.
	 */
	public GroovyCollector() {
		super();
		binding = new Binding();
	}

	/**
	 * 
	 * @see com.heliosapm.wiex.server.collectors.BaseCollector#collect()
	 */
	@Override
	@JMXOperation(description="Executes configured groovy resources.", expose=true, name="collect")
	public void collect() {
		// Execute Scripts
		String scriptName = null;
		for(String key: groovyScripts.keySet()) {
			try {
				Script script = groovyScripts.get(key);
				script.run();
			} catch (Exception e) {
				if(logErrors) {
					log.error("Failure Executing Script[" + scriptName + "]", e);
				}
			}			
		}		
		// Execute Evaluations
		for(ScriptEvaluation se: groovyEvaluations.values()) {
			try {
				String retVal = se.getShell().evaluate(se.getGroovyText()).toString();
				recordTrace(tracer.buildSegment(segmentPrefix, false, se.getSegment()), se.getMetricName(), retVal, se.getMetricType());
			} catch (Exception e) {
				if(logErrors) {
					log.error("Failure Executing Evaluation[" + se + "]", e);
				}				
			}
		}
	}
	
	
	/**
	 * Generates a report of configured Groovy Evaluations
	 * @return HTML Table Report String
	 */
	@JMXOperation(description="Generates a report of configured Groovy Evaluations", expose=true, name="ReportGroovyEvals")
	public String reportEvaluations() {
		StringBuilder buff = new StringBuilder("<table border=\"1\">");
		buff.append("<tr><th>Name</th><th>Type</th><th>Segment</th><th>Groovy Code</th></tr>");
		for(ScriptEvaluation se: groovyEvaluations.values()) {
			buff.append("<tr>");
			buff.append("<td>").append(se.getMetricName()).append("</td>");
			buff.append("<td>").append(se.getMetricType()).append("</td>");
			buff.append("<td>").append(tracer.buildSegment(segmentPrefix, false, se.getSegment())).append("</td>");
			buff.append("<td>").append(se.getGroovyText()).append("</td>");
			buff.append("</tr>");
		}		
		buff.append("</table>");		
		return buff.toString();
	}
	
	/**
	 * Generates a report of configured Groovy Scripts.
	 * @return HTML Table Report String
	 */
	@JMXOperation(description="Generates a report of configured Groovy Scripts", expose=true, name="ReportGroovyScripts")
	public String reportScripts() {
		StringBuilder buff = new StringBuilder("<table border=\"1\">");
		buff.append("<tr><th>Name</th><th>Groovy Code</th></tr>");
		for(Entry entry: groovyScriptSource.entrySet()) {
			buff.append("<tr>");
			buff.append("<td>").append(entry.getKey().toString().split("~")[1]).append("</td>");
			buff.append("<td>").append(entry.getValue()).append("</td>");
			buff.append("</tr>");			
		}
		for(Entry<String, GroovyCodeSource> entry: groovyScriptCode.entrySet()) {
			
			buff.append("<tr>");
			buff.append("<td>").append(entry.getKey().toString().split("~")[1]).append("</td>");
			GroovyCodeSource gcs = entry.getValue();			
			buff.append("<td>").append(gcs.getName()).append("</td>");
			buff.append("</tr>");						
		}
		buff.append("</table>");		
		return buff.toString();
		
	}
	
	/**
	 * No-Op Placeholder.
	 * @return
	 */
	@JMXAttribute(description="A Groovy Collector", expose=true, name="GroovyCollector")
	public Element getGroovyCollector() {
		return null;
	}
	
	@JMXOperation(description="Starts the service.", expose=true, name="start")
	public void start() throws Exception {
		binding.setVariable("jndi", new InitialContext());
		binding.setVariable("mbeanServer", mbeanServer);
		binding.setVariable("tracer", tracer);
		binding.setVariable("segmentPrefix", segmentPrefix);
		binding.setVariable("logger", log);
		binding.setVariable("objectName", objectName);		
		binding.setVariable("binding", binding);
		for(Entry entry: shellProperties.entrySet()) {
			binding.setProperty(entry.getKey().toString(), entry.getValue());
		}
		// iterate keys in shells
		// for each shell, iterate groovyScriptSource, groovyScriptCode
		// and compile the scripts
		// store the scripts in groovyScripts
		
		StringBuilder buff = new StringBuilder("\n\t============================================================");
		buff.append("\n\tCompiling Scripts for GroovyService").append(objectName.toString());
		buff.append("\n\t============================================================");
		log.info(buff.toString());
		int sc = 0;		
		for(String shellKey: shells.keySet()) {
			try {
				GroovyShell shell = shells.get(shellKey);
				for(String scriptName: groovyScriptSource.keySet()) {
					String scriptSource = groovyScriptSource.get(scriptName);
					Script script = shell.parse(scriptSource);
					groovyScripts.put(scriptName, script);
					log.info("\tCompiled "+ scriptName);
					sc++;
				}
				for(String scriptCodeName: groovyScriptCode.keySet()) {
					GroovyCodeSource groovyCodeSource = groovyScriptCode.get(scriptCodeName);
					Script script = shell.parse(groovyCodeSource);
					groovyScripts.put(scriptCodeName, script);
					log.info("\tCompiled " + scriptCodeName);
					sc++;
				}				
			} catch (Throwable t) {
				if(logErrors) {
					log.error("Failed to parse script in: " + shellKey, t);
				}
			}
			buff = new StringBuilder("\n\t============================================================");
			buff.append("\n\tCompiled ").append(sc).append(" Scripts for GroovyService").append(objectName.toString());
			buff.append("\n\t============================================================");
			
		}
		super.start();
	}
	
	/**
	 * Adds a new GroovyCollector for processing.
	 * @param xml  // this node is GroovyCollector // getAttributes has all the collector attrs // 
	 */
	@SuppressWarnings("unused")
	public void setGroovyCollector(Element xml) {
		NamedNodeMap attributes = xml.getAttributes();
		String collectorName = attributes.getNamedItem("name").getNodeValue();
		CompilerConfiguration cc = new CompilerConfiguration(); 
		//==================================================================================================
		Boolean debug = null; // sets shell into debug mode.
		Integer recompInterval = null; // minimum recompilation interval
		Boolean recompile = null; // shell recompiles.
		String scriptBaseClass = null; // the base class to use for the generated scripts.
		String encoding = null;  // the encoding of the scripts to compile.
		Integer tolerance = null; // the number of compilation errors to tolerate before ending the compile.
		Boolean verbose = null; // verbose compilation
		Integer warning = null;  // sets the compiler warning level.
		//==================================================================================================
		try {
			debug = Boolean.parseBoolean(attributes.getNamedItem("debug").getNodeValue());
			if(debug!=null) cc.setDebug(debug);
		} catch (Exception e) {}				
		try {
			recompInterval = Integer.parseInt(attributes.getNamedItem("recompinterval").getNodeValue());			
		} catch (Exception e) {}		
		try {
			recompile = Boolean.parseBoolean(attributes.getNamedItem("recomp").getNodeValue());
			if(recompile!=null) cc.setRecompileGroovySource(recompile);
		} catch (Exception e) {}
		if(!recompile) {
			recompInterval = -1;					
		}
		//if(recompInterval!=null )cc.setMinimumRecompilationIntervall(recompInterval);
		try {
			scriptBaseClass = attributes.getNamedItem("scriptbaseclass").getNodeValue();
			if(scriptBaseClass!=null && (!"".equalsIgnoreCase(scriptBaseClass))) cc.setScriptBaseClass(scriptBaseClass);
		} catch (Exception e) {}
		try {
			encoding = attributes.getNamedItem("encoding").getNodeValue();
			if(encoding!=null && (!"".equalsIgnoreCase(encoding))) cc.setSourceEncoding(encoding);
		} catch (Exception e) {}		
		try {
			tolerance = Integer.parseInt(attributes.getNamedItem("tolerance").getNodeValue());
			if(tolerance!=null) cc.setTolerance(tolerance);
		} catch (Exception e) {}
		try {
			verbose = Boolean.parseBoolean(attributes.getNamedItem("verbose").getNodeValue());
			if(verbose!=null) cc.setVerbose(verbose);
		} catch (Exception e) {}
		try {
			warning = Integer.parseInt(attributes.getNamedItem("warning").getNodeValue());
			if(warning!=null) cc.setWarningLevel(warning);
		} catch (Exception e) {}
		GroovyShell shell = new GroovyShell(binding, cc);
		shells.put(collectorName, shell);
		
		
		NodeList scriptNodes = xml.getElementsByTagName("script");
		String url = null;
		String scriptName = null;
		Script script  = null;
		String groovyCode = null;
		for(int i = 0; i < scriptNodes.getLength(); i++) {
			try {
				Node scriptNode = scriptNodes.item(i);
				scriptName = scriptNodes.item(i).getAttributes().getNamedItem("name").getNodeValue();
				try {
					url = scriptNodes.item(i).getAttributes().getNamedItem("url").getNodeValue();
				} catch (Exception e) {url = null;}
				try {
					groovyCode = scriptNodes.item(i).getFirstChild().getNodeValue();
				} catch (Exception e) {groovyCode = null;}
				if(url==null) {
					// store URL and name  
					groovyScriptSource.put(collectorName + "~" + scriptName, groovyCode);
				} else {
					URL sourceURL = new URL(url);					
					GroovyCodeSource groovyCodeSource = new GroovyCodeSource(sourceURL);
					// store gcs and name	
					groovyScriptCode.put(collectorName + "~" + scriptName, groovyCodeSource);
				}				
			} catch (Exception e) {
				if(logErrors) {
					log.error("Failed to register groovy script:" + scriptName, e);
				}
			}			
		}
		NodeList evaluateNodes = xml.getElementsByTagName("evaluate");
		String segment = null;
		String[] segmentFragments = null;
		for(int i = 0; i < evaluateNodes.getLength(); i++) {
			Node evaluateNode = evaluateNodes.item(i);
			String evalName = evaluateNodes.item(i).getAttributes().getNamedItem("name").getNodeValue();
			String type = evaluateNodes.item(i).getAttributes().getNamedItem("type").getNodeValue();
			try {
				segment = evaluateNodes.item(i).getAttributes().getNamedItem("segment").getNodeValue();
				if(segment!=null) {
					segmentFragments = segment.split(",");
					if(segmentFragments!=null) {
						if(segmentFragments.length==0) {
							segmentFragments = new String[]{segment};
						} 
					} else {
						segmentFragments = new String[]{};
					}
				}
			} catch (Exception e) {
				segmentFragments = new String[]{};
			}
			groovyCode = evaluateNodes.item(i).getFirstChild().getNodeValue();
			ScriptEvaluation se = new ScriptEvaluation(groovyCode, evalName, type, segmentFragments, shell);
			groovyEvaluations.put(collectorName + "~" + evalName, se);
		}
		
		

	}
	
	

	/**
	 * Initialize module and version.
	 * @see com.heliosapm.wiex.server.collectors.BaseCollector#init()
	 */
	@Override
	public void init() {
		VERSION = "$Revision: 1.3 $";
		MODULE = "GroovyCollector";
	}

	/**
	 * @return the shellProperties
	 */
	@JMXAttribute(description="Groovy Collector Shell Properties", expose=true, name="ShellProperties")
	public Properties getShellProperties() {
		return shellProperties;
	}

	/**
	 * @param shellProperties the shellProperties to set
	 */
	public void setShellProperties(Properties shellProperties) {
		this.shellProperties = shellProperties;
	}

}

