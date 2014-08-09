/**
 * Tracing Interceptor
 * @author WhiteheN 
 */
package com.heliosapm.wiex.tracing.tracing.interceptors;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.log4j.Logger;
import org.jboss.ejb.Container;
import org.jboss.invocation.Invocation;
import org.jboss.invocation.InvocationType;

import com.heliosapm.wiex.tracing.tracing.TracerFactory;

/**
 * <p>Title: TraceMultiInterceptor</p>
 * <p>Description: An extension of <code>MultiInterceptor</code> that checks 
 * the log4j trace status of <code>TRACE.&lt;package name&gt;</code> 
 * and if it is enabled, the call will be traced.
 * Othwerwise, the call will be passed straight through.</p>
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Organization: Helios Development Group</p>
 * @author Whitehead
 * @version $Revision: 1.1 $
 * 
 */

public class TraceMultiInterceptor extends MultiInterceptor {
	
	String ejbTraceCategory = null;
	String servletTraceCategory = null;
	
	public void setContainer(Container container) {
		super.setContainer(container);
		ejbTraceCategory = container.getBeanMetaData().getEjbClass();
	}
	
	/* (non-Javadoc)
	 * @see org.jboss.ejb.plugins.AbstractInterceptor#invokeHome(org.jboss.invocation.Invocation)
	 */
	public Object invokeHome(Invocation mi) throws Exception {
		String type = null;
		if (mi.getType().equals(InvocationType.LOCALHOME)) {
			type = "LocalHome";
		} else {
			type = "RemoteHome";
		}
		if(TracerFactory.isTraceEnabled(ejbTraceCategory)) {
			return invokeGeneric(mi, true, type);
		} else {
			return nextInterceptor.invokeHome(mi);
		}
	}

	/* (non-Javadoc)
	 * @see org.jboss.ejb.plugins.AbstractInterceptor#invoke(org.jboss.invocation.Invocation)
	 */
	public Object invoke(Invocation mi) throws Exception {
		String type = null;
		if (mi.getType().equals(InvocationType.LOCAL)) {
			type = "Local";
		} else {
			type = "Remote";
		}
		if(TracerFactory.isTraceEnabled(ejbTraceCategory)) {
			return invokeGeneric(mi, true, type);
		} else {
			return nextInterceptor.invoke(mi);
		}

	}
	
	/* (non-Javadoc)
	 * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
	 */
	public void doFilter(ServletRequest req, ServletResponse resp,
			FilterChain filterChain) throws IOException, ServletException {
		if(TracerFactory.isTraceEnabled(servletTraceCategory)) {
			super.doFilter(req, resp, filterChain);
		} else {
			filterChain.doFilter(req, resp);
		}
	}
	
	/* (non-Javadoc)
	 * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
	 */
	public void init(FilterConfig filterConfig) throws ServletException {
		servletTraceCategory = filterConfig.getServletContext().getServletContextName();
	}	
	

}
