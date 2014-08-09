/**
 * 
 */
package com.heliosapm.wiex.server.server.rendering;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;

import com.heliosapm.wiex.jmx.dynamic.annotation.JMXManagedObject;
import com.heliosapm.wiex.jmx.dynamic.annotation.JMXOperation;
import com.heliosapm.wiex.jmx.dynamic.annotation.JMXOperationParameter;

/**
 * <p>Title: CachedResultSetHTMLTableRenderer</p>
 * <p>Description: Renders an HTML Table from a cached JDBC Result Set</p> 
 * <p>Copyright: Copyright (c) 2007</p>
 * <p>Company: Helios Development Group</p>
 * @author Whitehead
 * @version $Revision$
 */
@JMXManagedObject(annotated=true, declared=false)
public class CachedResultSetHTMLTableRenderer extends AbstractRenderer {

	/**
	 * Simple constructor.
	 * Sets the the mime type to <code>text/html</code>.
	 */	
	public CachedResultSetHTMLTableRenderer() {
		mimeType = "text/html";
	}
	
	/**
	 * Acquires the cached result set from the configured mbean/attribute and renders an HTML Table.
	 * @param args
	 * @return A byte array comprised of a rendered HTML table.
	 * @see com.heliosapm.wiex.server.server.rendering.IRenderer#render(java.lang.Object[])
	 */
	//@JMXOperation(name="getRenderOpName", description="getRenderOpDescription", introspect=true)
	// If we overload the method, we will need 2 different op names.
	public byte[] render(@JMXOperationParameter(name="RenderArgs", description="Render Operation Parameters") Object...args) {
		if(isContentFresh() || cachedContent == null) {
			try {
				String[][] resultSet = (String[][])server.getAttribute(this.dataSourceMBean, this.dataSourceAttribute);
				StringBuilder buff = new StringBuilder("<table border=\"1\">");
				if(resultSet==null || resultSet.length < 1 || resultSet[0].length < 1) {
					buff.append("</table>");					
				} else {
					// Generate Details
					for(int i = 0; i < resultSet.length; i++) {
						buff.append("<tr>");
						for(int x = 0; x < resultSet[i].length; x++) {
							if(i==0) {
								buff.append("<th>").append(resultSet[i][x]).append("</th>");
							} else {
								buff.append("<td>").append(resultSet[i][x]).append("</td>");
							}
							
						}
						buff.append("</tr>");
					}
					buff.append("</table>");
				}
				byte[] retVal = buff.toString().getBytes();
				cachedContent = retVal;
				return retVal;
			} catch (Exception e) {
				log.error("Rendering Error:", e);
				return new byte[]{};
			}
		} else {
			return cachedContent;
		}
	}
	
	/**
	 * Acquires the cached result set from the configured mbean/attribute and renders an HTML Table.
	 * @return A byte array comprised of a rendered HTML table.
	 * @see com.heliosapm.wiex.server.server.rendering.IRenderer#render(java.lang.Object[])
	 */
	@JMXOperation(name="getRenderOpName", description="getRenderOpDescription", introspect=true)
	public byte[] render() {		
		return render(new Object[]{});
	}	
	
	/**
	 * Returns the named renderers render op description.
	 * @return
	 */
	public String getRenderOpDescription() {
		return name + " Renders an HTML Table"; 
	}	
	
	/**
	 * Returns the named renderers render op name.
	 * @return
	 */
	public String getRenderOpName() {
		return "render" + name; 
	}		
	

}
