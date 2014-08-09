/**
 * 
 */
package com.heliosapm.wiex.server.collectors.ajax;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Enumeration;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.heliosapm.wiex.tracing.tracing.ITracer;
import com.heliosapm.wiex.tracing.tracing.TracerFactory;

/**
 * Servlet Filter checks each request for client side submitted metrics. 
 * @author WhiteheN
 */
public class AjaxMetricsFilter implements Filter {
	/** The currently configured tracer */
	protected ITracer tracer = null;
	/** The tracing segment */
	protected String rootSegment = null;
	/** The instance logger */
	protected Logger log = null;
	/** The singleton instance of the config */
	protected AjaxMetricsFilterConfiguration config = null;
	
	/** a thread local defining the http request */
	public static ThreadLocal<String> JSAgentRequest = new ThreadLocal<String>(); 


	/**
	 * Creates a new instance of a AjaxMetricsFilter and acquires a handle to the current tracer.
	 */
	public AjaxMetricsFilter() {
		log = Logger.getLogger(getClass());
		tracer = TracerFactory.getInstance();
		rootSegment = tracer.buildSegment("Ajax Agent", "XMLHttp");
		config = AjaxMetricsFilterConfiguration.getInstance();
		log.info("Initialized AjaxMetricsFilter");
	}

	/**
	 * @see javax.servlet.Filter#destroy()
	 */
	public void destroy() {
	}

	/**
	 * @param request The servlet request
	 * @param response The servlet response
	 * @param filterChain The filter chain to propagate through
	 * @throws IOException
	 * @throws ServletException
	 * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
	 */
	public void doFilter(ServletRequest req, ServletResponse resp, FilterChain filterChain) throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest)req;
		HttpServletResponse response = (HttpServletResponse)resp;		
//			log.info("Request Context Path:" + request.getContextPath());
//			log.info("Request Local Name:" + request.getLocalName());
//			log.info("Request Path Info:" + request.getPathInfo());
//			log.info("Request Path Translated:" + request.getPathTranslated());
//			log.info("Request Query String:" + request.getQueryString());
//			log.info("Request Request URI:" + request.getRequestURI());
//			log.info("Request Request URL:" + request.getRequestURL());
		if(request.getRequestURI().contains("/agentconfig")) {
			byte[] configResponse = config.generateInitCommands().getBytes();
			response.setBufferSize(configResponse.length);
			response.setContentLength(configResponse.length);
			response.setContentType("application/javascript");			
			response.setIntHeader("agent-config-version", config.hashCode());
			OutputStream os = response.getOutputStream();
			os.write(configResponse);
			os.flush();
			os.close();
			return;			
		}
		/*
		String metricName = request.getParameter(AJAX_METRIC_URI);
		String elapsedTimeStr = request.getParameter(AJAX_METRIC_ELAPSED_TIME);
		String concurrentStr = request.getParameter(AJAX_METRIC_CONCURRENT);
		String exceptionStr = request.getParameter(AJAX_METRIC_EXCEPTION);
		String errCodeStr = request.getParameter(AJAX_METRIC_HCODE);
		if(metricName!=null && elapsedTimeStr!=null) {
			try {
				long elapsedTime = Long.parseLong(elapsedTimeStr);
				metricName = java.net.URLDecoder.decode(metricName, "UTF-8").replaceFirst("/", "");
				String seg = tracer.buildSegment(rootSegment, false, metricName);
				tracer.recordMetric(seg, "Client Elapsed Time" , elapsedTime);
				tracer.recordIntervalIncident(seg, "Responses Per Interval");
				if(exceptionStr!=null) {
					tracer.recordIntervalIncident(seg, "Exception");
				}
				if(errCodeStr!=null) {
					tracer.recordIntervalIncident(tracer.buildSegment(seg, "Exception"), errCodeStr);
				}
				try {
					if(concurrentStr!=null) {
						int concurrent = Integer.parseInt(concurrentStr);
						tracer.recordMetric(seg, "Concurrent Ajax Requests" , concurrent);
					}
				} catch (Exception e) {}
			} catch (Exception e) {
				log.warn("Failed to Trace", e);
			}
		}
		*/
		response.setIntHeader("agent-config-version", config.hashCode());
		response.setIntHeader(config.p(config.pDelta), (int)0);
		long start = System.currentTimeMillis();		
		UnCommitableServletResponse usr = new UnCommitableServletResponse(response);
		filterChain.doFilter(request, usr);		
		long elapsed = System.currentTimeMillis()-start;
		
			
		//response.setIntHeader(config.p(config.pDelta), (int)elapsed);
		response.setIntHeader(config.p(config.pDelta), (int)elapsed);
		log.info("Delta:" + elapsed + " Commited:" + response.isCommitted());
		usr.process();
		int contentLength = usr.getContLength();
		trace(request, contentLength);	
		/*
		StackTraceElement[] stacks = Thread.currentThread().getStackTrace();
		StringBuilder buff = new StringBuilder(1000);
		buff.append("=========Stack Trace===============");
		for(StackTraceElement stack: stacks) {
			buff.append("\n\t").append(stack.toString());
		}
		log.info("Stack Trace:" + buff.toString());
		*/

		//log.info("Delta Time:" + elapsed);
	}
	

	@SuppressWarnings("static-access")
	protected void trace(HttpServletRequest request, int contentLength) {
		long start = System.currentTimeMillis();
		String metricName = null;
		String elapsedTimeStr = null;
		String concurrentStr = null;
		String exceptionStr = null;
		String hCodeStr = null;
		String hDeltaStr = null;
		String timeToInterStr = null;
		String timeInterStr = null;
		String interCallbacksStr = null;		
		String serialStr = null;
		if(config.getRunOption(config.uploadInParams)) {
			metricName = request.getParameter(config.p(config.pUri));
			elapsedTimeStr = request.getParameter(config.p(config.pElapsed));
			concurrentStr = request.getParameter(config.p(config.pConcurrent));
			exceptionStr = request.getParameter(config.p(config.pException));
			hCodeStr = request.getParameter(config.p(config.pHcode));
			hDeltaStr = request.getParameter(config.p(config.pDelta));
			timeToInterStr = request.getParameter(config.p(config.pInteractive));
			timeInterStr = request.getParameter(config.p(config.pInteractiveTime));
			interCallbacksStr = request.getParameter(config.p(config.pInteractiveCallbacks));
			serialStr = request.getParameter(config.p(config.pSerialNumber));
		} else {
			metricName = request.getHeader(config.p(config.pUri));
			elapsedTimeStr = request.getHeader(config.p(config.pElapsed));
			concurrentStr = request.getHeader(config.p(config.pConcurrent));
			exceptionStr = request.getHeader(config.p(config.pException));
			hCodeStr = request.getHeader(config.p(config.pHcode));
			hDeltaStr = request.getHeader(config.p(config.pDelta));
			timeToInterStr = request.getHeader(config.p(config.pInteractive));
			timeInterStr = request.getHeader(config.p(config.pInteractiveTime));
			interCallbacksStr = request.getHeader(config.p(config.pInteractiveCallbacks));
			serialStr = request.getHeader(config.p(config.pSerialNumber));			
		}
		String seg = tracer.buildSegment(rootSegment, false, metricName);
		// Web App Specific Tracing
		try { tracer.recordMetric(seg, "Total Elapsed Time" , fmatl(elapsedTimeStr)); } catch (Exception e) {}
		try { tracer.recordIntervalIncident(seg, "Responses Per Interval");} catch (Exception e) {}
		try { tracer.recordMetric(seg, "Concurrent Ajax Requests" , fmati(concurrentStr));} catch (Exception e) {}
		try { tracer.recordMetric(seg, "Server Elapsed Time" , fmatl(hDeltaStr));} catch (Exception e) {}
		try { tracer.recordMetric(seg, "Time To Interactive" , fmatl(timeToInterStr));} catch (Exception e) {}
		try { tracer.recordMetric(seg, "Client Rendering Time" , fmatl(timeInterStr));} catch (Exception e) {}
		try { tracer.recordMetric(seg, "Interactive Callbacks" , fmati(interCallbacksStr));} catch (Exception e) {}
		try { tracer.recordMetric(seg, "Content Length" , contentLength);} catch (Exception e) {}
		try {
			long transportTime = Long.parseLong(timeToInterStr)-Long.parseLong(hDeltaStr);
			tracer.recordMetric(seg, "Transport Time" , transportTime);
		} catch (Exception e) {}
		
		try { if(exceptionStr!=null) tracer.recordIntervalIncident(tracer.buildSegment(seg, "Exceptions"), exceptionStr);} catch (Exception e) {}				
		try { if(hCodeStr!=null) tracer.recordIntervalIncident(tracer.buildSegment(seg, "Http Response Codes"), hCodeStr);} catch (Exception e) {}
		
		// Aggregate Tracing
		seg = rootSegment;
		try { tracer.recordMetric(seg, "Total Elapsed Time" , fmatl(elapsedTimeStr)); } catch (Exception e) {}
		try { tracer.recordIntervalIncident(seg, "Responses Per Interval");} catch (Exception e) {}
		try { tracer.recordMetric(seg, "Concurrent Ajax Requests" , fmati(concurrentStr));} catch (Exception e) {}
		try { tracer.recordMetric(seg, "Server Elapsed Time" , fmatl(hDeltaStr));} catch (Exception e) {}
		try { tracer.recordMetric(seg, "Time To Interactive" , fmatl(timeToInterStr));} catch (Exception e) {}
		try { tracer.recordMetric(seg, "Client Rendering Time" , fmatl(timeInterStr));} catch (Exception e) {}
		try { tracer.recordMetric(seg, "Interactive Callbacks" , fmati(interCallbacksStr));} catch (Exception e) {}
		try { tracer.recordMetric(seg, "Content Length" , contentLength);} catch (Exception e) {}
		try {
			long transportTime = Long.parseLong(timeToInterStr)-Long.parseLong(hDeltaStr);
			tracer.recordMetric(seg, "Transport Time" , transportTime);
		} catch (Exception e) {}
		
		try { if(exceptionStr!=null) tracer.recordIntervalIncident(tracer.buildSegment(seg, "Exceptions"), exceptionStr);} catch (Exception e) {}				
		try { if(hCodeStr!=null) tracer.recordIntervalIncident(tracer.buildSegment(seg, "Http Response Codes"), hCodeStr);} catch (Exception e) {}
		long elapsed = System.currentTimeMillis()-start;
		tracer.recordMetric(seg, "Tracing Time" , elapsed);
		log.info("Completed Tracing in " + elapsed + " ms.");
	}
	
	protected Long fmatl(String s) {
		try {
			return new Long(s);
		} catch (Exception e) {
			return null;
		}
	}

	protected Integer fmati(String s) {
		try {
			return new Integer(s);
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Initializes the filter instance
	 * @param filterConfig
	 * @throws ServletException
	 * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
	 */
	public void init(FilterConfig filterConfig) throws ServletException {
		if(!config.isInited()) {
			config.updateConfigration(filterConfig);
		}
	}

}
