/**
 * 
 */
package com.heliosapm.wiex.server.server.rendering;

import java.io.BufferedOutputStream;
import java.io.IOException;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.heliosapm.wiex.jmx.util.MBeanServerHelper;

/**
 * <p>Title: RenderingServlet</p>
 * <p>Description: Servlet for generating rendered content through the RenderingManagerService.</p> 
 * <p>Copyright: Copyright (c) 2007</p>
 * <p>Company: Helios Development Group</p>
 * @author Whitehead
 * @version $Revision$
 */

public class RenderingServlet extends HttpServlet {

	private static final long serialVersionUID = -3159999046387561010L;
	/** The logger for this class */
	protected Logger logger  = null;
	/** A handle to JMX Agent */
	protected MBeanServer server = null;
	/** The object name of the rendering manager */
	protected ObjectName renderingManager = null;
	/** Indicates if this is an include servlet */
	protected boolean includeServlet = false;
	/** The default name of the rendering manager */
	public static final String DEFAULT_RENDERING_MANAGER_NAME = "com.heliosapm.wiex.server.server.rendering:service=RendererManager";
	/** Null Args Constant */
	protected static final Object[] NULL_ARGS = new Object[]{};
	/** Null Signature Constant */
	protected static final String[] NULL_SIG = new String[]{};


	/**
	 * 
	 */
	public RenderingServlet() {
		logger = Logger.getLogger(this.getClass());
		logger.info("Instantiated RenderingServlet");
	}

	/**
	 * Initializes the MBeanServer handle and the rendering manager object name.
	 * The domain of the MBeanServer is read from the init parameter <code>rendering.jmx.domain</code>.
	 * If it is not found, the default is <b>jboss</b>.
	 * The rendering manager object name is read from the init parameter <code>rendering.jmx.objectname</code>.
	 * If it is not found, the default is <b>DEFAULT_RENDERING_MANAGER_NAME</b>.
	 * @param config
	 * @throws ServletException
	 * @see javax.servlet.GenericServlet#init(javax.servlet.ServletConfig)
	 */
	public void init(ServletConfig config) throws ServletException {
		try {
			String domain = config.getInitParameter("rendering.jmx.domain");
			if(domain==null) domain = "jboss";
			server = MBeanServerHelper.getMBeanServer(domain);
			String renderingManagerName = config.getInitParameter("rendering.jmx.objectname");
			if(renderingManagerName==null) renderingManagerName = DEFAULT_RENDERING_MANAGER_NAME;
			renderingManager = new ObjectName(renderingManagerName);
	
		} catch (Exception e) {
			logger.error("Exception Initializing RenderingServlet Class", e);
			throw new ServletException("Exception Initializing RenderingServlet Class", e);
		}
	}

	/**
	 * Parses the request and returns the byte array content from the requested renderer.
	 * @param req
	 * @param res
	 * @throws IOException
	 * @throws ServletException
	 * @see javax.servlet.http.HttpServlet#service(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	public void service(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
		String rendererName = req.getParameter("name");
		if(rendererName==null) {
			throw new ServletException("name parameter not found and is mandatory");
		}
		byte[] content = getContent(rendererName);			
		res.addHeader("Cache-Control","no-cache");
		res.addHeader("Pragma", "no-cache");
		res.addHeader("Expires", "0");       
		res.addHeader("Content-Type", "no-cache");
		res.setDateHeader("Expires", 0L);
		res.setContentType(getMimeType(rendererName));
		res.setContentLength(content.length);
		BufferedOutputStream bos = new BufferedOutputStream(res.getOutputStream());
		bos.write(content);
		bos.flush();
		bos.close();		
	}

	/**
	 * Acquires the mime type of the passed renderer.
	 * @param name The renderer name.
	 * @return A mime type.
	 */
	protected String getMimeType(String name) {
		try {
			return (String)server.getAttribute(renderingManager, name + "MimeType");
		} catch (Exception e) {
			throw new RuntimeException("Failed to get mime type for [" + name + "]", e);
		}
	}

	/**
	 * Acquires the content of the passed renderer.
	 * @param name The renderer name
	 * @return The content.
	 */
	protected byte[] getContent(String name) {
		try {
			return (byte[])server.invoke(renderingManager, "render" + name, NULL_ARGS, NULL_SIG);
		} catch (Exception e) {
			throw new RuntimeException("Failed to get content for [" + name + "]", e);
		}

	}

}
