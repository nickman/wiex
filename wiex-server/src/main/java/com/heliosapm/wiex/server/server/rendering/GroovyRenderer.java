/**
 * 
 */
package com.heliosapm.wiex.server.server.rendering;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.naming.InitialContext;

import com.heliosapm.wiex.jmx.dynamic.annotation.JMXAttribute;
import com.heliosapm.wiex.jmx.dynamic.annotation.JMXManagedObject;
import com.heliosapm.wiex.jmx.dynamic.annotation.JMXOperation;
import com.heliosapm.wiex.server.server.utils.GroovyScriptManager;


/**
 * <p>Title: GroovyRenderer</p>
 * <p>Description: A renderer that is executed against a supplied Groovy script.</p> 
 * <p>Copyright: Copyright (c) 2007</p>
 * <p>Company: Helios Development Group</p>
 * @author Whitehead
 * @version $Revision$
 */
@JMXManagedObject(annotated=true, declared=false)
public class GroovyRenderer extends AbstractRenderer {
	
	/** The URL for the script source code */
	protected URL sourceUrl = null;
	/** The groovy compilation properties */
	protected Properties groovyProperties = null;
	/** Additional properties to be passed into the script's binding */
	protected Properties scriptProperties = null;
	/** The Groovy Script Manager */
	protected GroovyScriptManager scriptManager = null;
	
	
	/**
	 * Callback to initialize the rednerer after all attributes have been set.
	 * @throws RendererStartException 
	 * @see com.heliosapm.wiex.server.server.rendering.IRenderer#start()
	 */
	public void start() throws RendererStartException {
		try {
			Map<String, Object> args = new HashMap<String, Object>();
			args.put("targetMBean", dataSourceMBean);
			args.put("targetAttribute", dataSourceAttribute);
			args.put("localMBeanServer", server);
			args.put("jndi", new InitialContext());			
			scriptManager = new GroovyScriptManager(sourceUrl, groovyProperties, scriptProperties, args);
			mimeType = (String)scriptManager.invokeMethod("getMimeType", null);
		} catch (Exception e) {
			throw new RendererStartException("Failed to start renderer from source [" + sourceUrl + "]", e);
		}
	}	

	/**
	 * Calls the compiled script and invokes the <code>render</code> method.
	 * Expects a return type of <code>byte[]</code>.
	 * @param args
	 * @return The rendered content in the type of <code>byte[]</code>
	 * @see com.heliosapm.wiex.server.server.rendering.IRenderer#render(java.lang.Object[])
	 */
	public byte[] render(Object... args) {		
		if(isContentFresh() || cachedContent == null) {
			cachedContent = (byte[])scriptManager.invokeMethod("render", args);
			return cachedContent;
		} else {
			return cachedContent;
		}
	}

	/**
	 * Calls the compiled script and invokes the <code>render</code> method.
	 * Expects a return type of <code>byte[]</code>.
	 * @return The rendered content in the type of <code>byte[]</code>
	 * @see com.heliosapm.wiex.server.server.rendering.IRenderer#render()
	 */
	@JMXOperation(name="getRenderOpName", description="getRenderOpDescription", introspect=true)
	public byte[] render() {		
		return render(new Object[]{});
	}

	/**
	 * @return the sourceUrl
	 */
	@JMXAttribute(name="getSourceURLName", description="getSourceURLNameDescription", introspect=true)
	public URL getSourceUrl() {
		return sourceUrl;
	}
	
	public String getSourceURLName() {
		return name + "GroovySourceURL";
	}
	
	public String getSourceURLNameDescription() {
		return "The Groovy Source URL for " + name;
	}
	

	/**
	 * @param sourceUrl the sourceUrl to set
	 */
	public void setSourceUrl(URL sourceUrl) {
		this.sourceUrl = sourceUrl;
	}


	
	
	/**
	 * @return the groovyProperties
	 */
	@JMXAttribute(name="getGroovyPropertiesName", description="getGroovyPropertiesDescription", introspect=true)
	public Properties getGroovyProperties() {
		return groovyProperties;
	}
	
	/**
	 * @param groovyProperties the groovyProperties to set
	 */	
	public void setGroovyProperties(Properties groovyProperties) {
		this.groovyProperties = groovyProperties;
	}
	
	/**
	 * @return
	 */
	public String getGroovyPropertiesName() {
		return name + "GroovyProperties";
	}
	
	/**
	 * @return
	 */
	public String getGroovyPropertiesDescription() {
		return "The Groovy Source Compile Options for " + name;
	}

	/**
	 * @return the scriptProperties
	 */
	@JMXAttribute(name="getScriptPropertiesName", description="getScriptPropertiesDescription", introspect=true)
	public Properties getScriptProperties() {
		return scriptProperties;
	}

	/**
	 * @param scriptProperties the scriptProperties to set
	 */
	public void setScriptProperties(Properties scriptProperties) {
		this.scriptProperties = scriptProperties;
	}
	
	/**
	 * @return
	 */
	public String getScriptPropertiesName() {
		return name + "ScriptProperties";
	}
	
	/**
	 * @return
	 */
	public String getScriptPropertiesDescription() {
		return "The Groovy Parameter Properties for " + name;
	}
	







}
