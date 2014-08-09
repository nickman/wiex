/**
 * 
 */
package com.heliosapm.wiex.server.server.rendering;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.heliosapm.wiex.jmx.dynamic.ManagedObjectDynamicMBean;
import com.heliosapm.wiex.jmx.dynamic.annotation.JMXAttribute;
import com.heliosapm.wiex.jmx.dynamic.annotation.JMXManagedObject;
import com.heliosapm.wiex.server.server.utils.JMXUtils;

/**
 * <p>Title: RenderingManagerService</p>
 * <p>Description: A service that manages and exposes configured renderers.</p> 
 * <p>Copyright: Copyright (c) 2007</p>
 * <p>Company: Helios Development Group</p>
 * @author Whitehead
 * @version $Revision$
 */
@JMXManagedObject(annotated=true, declared=false)
public class RenderingManagerService extends ManagedObjectDynamicMBean {
	/** The class logger */
	protected Logger log = Logger.getLogger(RenderingManagerService.class);
	/** A concurrent map of the registered renderers indexed by name */
	protected Map<String, IRenderer> renderers = new ConcurrentHashMap<String, IRenderer>();
	
	public RenderingManagerService() {
		reflectObject(this);
		log.info("Instantiated RenderingManagerService");
	}
	
	
	/**
	 * No Op.
	 * @return
	 */
	@JMXAttribute(name="Renderers", description="An XML Element describing a set of renderers.")
	public Element getRenderers() {
		return null;
	}
	
	/**
	 * Registers, creates and exposes a set of renderers. 
	 * @param xml
	 */
	
	public void setRenderers(Element xml) {
		String rNodeName = null;
		String rendClassName = null;
		String rendName = null;
		try {
			NodeList rNodeList = xml.getChildNodes();
			for(int x = 0; x < rNodeList.getLength(); x++) {
				rNodeName = rNodeList.item(x).getNodeName();
				if("Renderer".equalsIgnoreCase(rNodeName)) {
					rendClassName = rNodeList.item(x).getAttributes().getNamedItem("class").getNodeValue();
					rendName = rNodeList.item(x).getAttributes().getNamedItem("name").getNodeValue();
					createRenderer(rendClassName, rendName, rNodeList.item(x).getChildNodes());
				}						
			}
			updateMBeanInfo();
		} catch (Exception e) {
			log.error("Failed to Process Renderer Creation", e);
		}
		
	}
	
	/**
	 * Creates and registers a renderer.
	 * @param className
	 * @param name
	 * @param attributeList
	 * @throws ClassNotFoundException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws RendererStartException 
	 */
	protected void createRenderer(String className, String name, NodeList attributeList) throws ClassNotFoundException, InstantiationException, IllegalAccessException, RendererStartException {
		String attrName = null;
		String attrValue = null;
		Class attrType = null;
		Class clazz = Class.forName(className);
		IRenderer irenderer = (IRenderer)clazz.newInstance();
		irenderer.setName(name);
		irenderer.setMBeanServer(server);
		for(int i = 0; i < attributeList.getLength(); i++) {
			try {
				if("attribute".equalsIgnoreCase(attributeList.item(i).getNodeName())) {
					attrName = attributeList.item(i).getAttributes().getNamedItem("name").getNodeValue();
					attrValue = attributeList.item(i).getChildNodes().item(0).getNodeValue();
					attrType = JMXUtils.getAttributeType(clazz, attrName);
					JMXUtils.setAttribute(attrName, attrValue, attrType, irenderer);
					if(log.isDebugEnabled())log.debug("Set " + attrName + " to " + attrValue.toString());
				}
			} catch (Exception e) {
				log.error("Cannot Find Renderer Attribute [" + attrName + "] for type [" + className + "]");
				throw new InstantiationException("Cannot Find Renderer Attribute [" + attrName + "] for type [" + className + "]");
			}
		}
		irenderer.start();
		reflectObject(irenderer);
		log.info("Deployed Renderer:" + irenderer.getName());
	}
	

	
	
	
	
}
