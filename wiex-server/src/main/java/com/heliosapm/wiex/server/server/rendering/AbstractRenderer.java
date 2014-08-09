/**
 * 
 */
package com.heliosapm.wiex.server.server.rendering;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.log4j.Logger;

import com.heliosapm.wiex.jmx.dynamic.annotation.JMXAttribute;
import com.heliosapm.wiex.jmx.dynamic.annotation.JMXManagedObject;

/**
 * <p>Title: AbstractRenderer</p>
 * <p>Description: Base abstract class for content renderers.</p> 
 * <p>Copyright: Copyright (c) 2007</p>
 * <p>Company: Helios Development Group</p>
 * @author Whitehead
 * @version $Revision$
 */
@JMXManagedObject(annotated=true, declared=true)
public abstract class AbstractRenderer implements IRenderer {

	/** The mime type of the returned content. */
	protected String mimeType = null;
	/** The name of the renderer */
	protected String name = null;
	/** The MBean where the renderer will read the source data from */
	protected ObjectName dataSourceMBean = null;
	/** The attribute in the source MBean where the renderer will read the source data from */
	protected String dataSourceAttribute = null;
	/** The MBeanServer to use to acquire the target result set. */
	protected MBeanServer server = null;
	/** The class logger */
	protected Logger log = null;
	/** The last time the content was detected as changed */
	protected long contentChanged = 0;
	/** The cached content */
	protected byte[] cachedContent = null;
	
	
	
	
	public AbstractRenderer() {
		log = Logger.getLogger(this.getClass());
	}
	
	/**
	 * Callback to initialize the rednerer after all attributes have been set.
	 * @throws RendererStartException
	 * @see com.heliosapm.wiex.server.server.rendering.IRenderer#start()
	 */
	public void start()  throws RendererStartException {
		
	}
	
	/**
	 * @return
	 * @see com.heliosapm.wiex.server.server.rendering.IRenderer#getMimeType()
	 */
	@JMXAttribute(name="getMimeOpName", description="getMimeOpDescription", introspect=true)
	public String getMimeType() {
		return mimeType;
	}
	
	/**
	 * Sets the operative MBeanServer.
	 * @param server
	 * @see com.heliosapm.wiex.server.server.rendering.IRenderer#setMBeanServer(javax.management.MBeanServer)
	 */
	public void setMBeanServer(MBeanServer server) {
		this.server = server;
	}
	
	
	/**
	 * Returns the named renderers mime type.
	 * @return
	 */
	public String getMimeOpName() {
		return name + "MimeType"; 
	}
	
	/**
	 * Returns the named renderers mime type description.
	 * @return
	 */
	public String getMimeOpDescription() {
		return name + " Mime Type (" + mimeType + ")"; 
	}	
	
	/**
	 * @return
	 * @see com.heliosapm.wiex.server.server.rendering.IRenderer#getName()
	 */	
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 * @see com.heliosapm.wiex.server.server.rendering.IRenderer#setName(java.lang.String)
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the dataSourceMBean
	 */	
	public ObjectName getDataSourceMBean() {
		return dataSourceMBean;
	}

	/**
	 * @param dataSourceMBean the dataSourceMBean to set
	 */
	public void setDataSourceMBean(ObjectName dataSourceMBean) {
		this.dataSourceMBean = dataSourceMBean;
	}

	/**
	 * @return the dataSourceAttribute
	 */
	public String getDataSourceAttribute() {
		return dataSourceAttribute;
	}

	/**
	 * @param dataSourceAttribute the dataSourceAttribute to set
	 */
	public void setDataSourceAttribute(String dataSourceAttribute) {
		this.dataSourceAttribute = dataSourceAttribute;
	}
	
	public String getRenderOpName() {
		return "render" + name;
	}
	
	public String getRenderOpDescription() {
		return "Renders Content for " + name;
	}
	
	/**
	 * Checks to see if the content from the datasource is fresher than the local time stamp.
	 * @return
	 */
	public boolean isContentFresh() {
		try {
			long contentTimeStamp = (Long)server.getAttribute(dataSourceMBean, dataSourceAttribute + "LastChangeTime");
			if(contentTimeStamp > contentChanged) {
				contentChanged = contentTimeStamp;
				return true;
			} else {
				return false;
			}
			
		} catch (Exception e) {
			throw new RuntimeException("Failed to acquire LastChangeTime for attribute[" + dataSourceAttribute + "] on MBean [" + dataSourceMBean.toString() + "]", e);
		}
	}
	

}
