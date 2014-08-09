/**
 * 
 */
package com.heliosapm.wiex.server.server.rendering;

import javax.management.MBeanServer;

import com.heliosapm.wiex.jmx.dynamic.annotation.JMXManagedObject;

/**
 * <p>Title: IRenderer</p>
 * <p>Description: Interface describing a renderer which is custom adapter to create rich content from a collected data buffer.</p> 
 * <p>Copyright: Copyright (c) 2007</p>
 * <p>Company: Helios Development Group</p>
 * @author Whitehead
 * @version $Revision$
 */
@JMXManagedObject(annotated=true, declared=true)
public interface IRenderer {

	public void setName(String name);
	public String getName();
	public String getMimeType();
	public byte[] render(Object...args);
	public byte[] render();
	public void setMBeanServer(MBeanServer server);
	public void start() throws RendererStartException;
	
}
