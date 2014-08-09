/**
 * 
 */
package com.heliosapm.wiex.server.collectors.jmx.tracers;

import groovy.lang.Binding;
import groovy.lang.GroovyCodeSource;
import groovy.lang.GroovyShell;
import groovy.lang.Script;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;
import org.codehaus.groovy.control.CompilerConfiguration;

import com.heliosapm.wiex.server.collectors.groovy.PreparedScript;
import com.heliosapm.wiex.server.collectors.jmx.AbstractObjectTracer;
import com.heliosapm.wiex.server.collectors.jmx.RenderedTrace;

/**
 * <p>Title: GroovyObjectTracer</p>
 * <p>Description: An object tracer implemented by a supplied groovy script.</p> 
 * <p>Copyright: Copyright (c) 2007</p>
 *  * <p>Company: Helios Development Group</p>
 * @author Whitehead
 * @version $Revision: 1.1 $
 */

public class GroovyObjectTracer extends AbstractObjectTracer {
	
	/** Indicates if the underlying script is initialized */
	protected AtomicBoolean inited = new AtomicBoolean(false);
	protected String segmentSuffix = null;
	protected PreparedScript preparedScript = null;
	protected boolean logErrors = false;
	protected Logger log = null;
	protected Script script = null;
	protected Binding binding = new Binding();
	protected URL url = null;
	protected long lastModified = 0;
	public static final String CLASS_NAME =  GroovyObjectTracer.class.getName();
	

	/**
	 * Executes the configured groovy script and returns a list of rendered traces.
	 * Optionally, the script can perform the tracing and return null.
	 * The following items will be injected into the script through the binding:<ul>
	 * <li><b>renderedTraces</b>: The list of rendered traces to populate</li>
	 * <li><b>jmxObject</b>:The retrieved JMX MBean attribute to trace</li>
	 * <li><b>remoteMBeanServer</b>:The remote MBeanServer connection</li>
	 * <li><b>localMBeanServer</b>:The local MBeanServer connection</li>
	 * <li><b>remoteObjectName</b>:The remote ObjectName</li>
	 * <li><b>localObjectName</b>:The local ObjectName</li>
	 * <li><b>segmentSuffix</b>:The defined segment suffix</li>
	 * <li><b>segmentPrefix</b>:The MBean's segment prefix element</li>
	 * <li><b>jndi</b>:The local jndi context</li>
	 * <li><b>binding</b>:A reference to the binding for maintaining state across calls.</li>
	 * <li><b>stateMap</b>:A bound hashMap for convenience in state management.</li>
	 * </ul>
	 * @param obj The object to perform tracing on.
	 * @return A list of rendered traces or null.
	 * @see com.adp.sbs.metrics.tracing.collectors.jmx.ObjectTracer#renderTracingValue(java.lang.Object)
	 */
	
	@SuppressWarnings("unchecked")
	public List<RenderedTrace> renderTracingValue(Object ... args) {
		String name = null;
		int mod = 0;
		for(int i = 1; i < args.length; i++) {
			mod = i%2;
			if(mod==1) {
				name = args[i].toString();
			} else {
				if(name!=null && args[i] !=null) {
					binding.setProperty(name, args[i]);
				}
			}			
		}
		List<RenderedTrace> renderedTraces = new ArrayList<RenderedTrace>();		
		if(!inited.get() || isModified(url, lastModified)) {
			try {
				initScript();
			} catch (Exception e) {				
				if(logErrors) {
					log.error("Failed to initialize GroovyObjectTracer", e);
					return null;
				}
			}
		}
		try { binding.getProperty("stateMap"); } catch (groovy.lang.MissingPropertyException mpe) {
			binding.setProperty("stateMap", new HashMap(properties));
		}
		binding.setProperty("renderedTraces", renderedTraces);
		binding.setProperty("jmxObject", args[0]);
		binding.setProperty("segmentSuffix", segmentSuffix);
		binding.setProperty("binding", binding);
		try {
			return (List<RenderedTrace>) script.run();			
		} catch (Exception e) {
			if(logErrors) {
				log.error("Failed to execute GroovyObjectTracer Script", e);				
			}
			return null;
		}
		
	}
	
	/**
	 * Initializes the Groovy Script.
	 * @throws IOException
	 */
	protected void initScript() throws IOException {
		segmentSuffix = (String)properties.get(SEGMENT_SUFFIX);
		String le = (String)properties.get("logErrors");
		if(le==null) {
			logErrors  = false;
		} else {
			try {
				logErrors = Boolean.parseBoolean(le);
			} catch (Exception e) {
				logErrors  = true;
			}
		}
				
		log = (Logger)properties.get("Logger");
		if(log==null) log = Logger.getLogger(getClass());
		url = new URL(properties.get("sourceURL").toString());
		URLConnection urlConn = url.openConnection();
		lastModified = urlConn.getLastModified();
		urlConn.getInputStream().close();
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
			debug = Boolean.parseBoolean(properties.get("debug").toString());
			if(debug!=null) cc.setDebug(debug);
		} catch (Exception e) {}				
		try {
			recompInterval = Integer.parseInt(properties.get("recompinterval").toString());		
			//cc.setMinimumRecompilationIntervall(recompInterval);
		} catch (Exception e) {}		
		try {
			recompile = Boolean.parseBoolean(properties.get("recomp").toString());			
			if(recompile!=null) cc.setRecompileGroovySource(recompile);
			else recompile = new Boolean(false);
		} catch (Exception e) {
			recompile = new Boolean(false);
		}
		if(!recompile) {
			recompInterval = -1;					
		}
		//if(recompInterval!=null )cc.setMinimumRecompilationIntervall(recompInterval);
		try {
			scriptBaseClass = properties.get("scriptbaseclass").toString();
			if(scriptBaseClass!=null && (!"".equalsIgnoreCase(scriptBaseClass))) cc.setScriptBaseClass(scriptBaseClass);
		} catch (Exception e) {}
		try {
			encoding = properties.get("encoding").toString();
			if(encoding!=null && (!"".equalsIgnoreCase(encoding))) cc.setSourceEncoding(encoding);
		} catch (Exception e) {}		
		try {
			tolerance = Integer.parseInt(properties.get("tolerance").toString());
			if(tolerance!=null) cc.setTolerance(tolerance);
		} catch (Exception e) {}
		try {
			verbose = Boolean.parseBoolean(properties.get("verbose").toString());
			if(verbose!=null) cc.setVerbose(verbose);
		} catch (Exception e) {}
		try {
			warning = Integer.parseInt(properties.get("warning").toString());
			if(warning!=null) cc.setWarningLevel(warning);
		} catch (Exception e) {}
		long start = System.currentTimeMillis();
		for(Entry<String, Object> entry: properties.entrySet()) {
			binding.setProperty(entry.getKey(), entry.getValue());
		}
		GroovyShell shell = new GroovyShell(binding, cc);
		GroovyCodeSource groovyCodeSource = new GroovyCodeSource(url);  
		script = shell.parse(groovyCodeSource);
		long elapsed = System.currentTimeMillis()-start;
		log.info("Script Compiled From [" + url.toString() + "] In " + elapsed + " ms.");
		inited.set(true);
	}

	/**
	 * Tests to see if the source code at the URL has been modified since the last init.
	 * @param testUrl
	 * @param lastModified
	 * @return
	 * @throws IOException 
	 */
	protected boolean isModified(URL testUrl, long lastModified)  {
		if(lastModified==0) return false;
		try {
			URLConnection urlConn = testUrl.openConnection();
			long lastMod = urlConn.getLastModified();
			urlConn.getInputStream().close();
			return lastMod > lastModified;
		} catch (Exception e) { return false; }
	}
	
}
