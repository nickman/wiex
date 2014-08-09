package com.heliosapm.wiex.server.collectors;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.heliosapm.wiex.server.collectors.origin.OriginAdapter;
import com.heliosapm.wiex.tracing.tracing.ITracer;
import com.heliosapm.wiex.tracing.tracing.TracerFactory;

/**
 * <p>Title: CollectorServlet</p>
 * <p>Description: A collector servlet that serves as a collection point for a remote metric tracer.</p> 
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Company: Helios Development Group</p>
 * @author Whitehead
 * @version $Revision: 1.2 $
 */
public class CollectorServlet extends HttpServlet {

	private static final long serialVersionUID = -1052524481482430941L;
	/** The parameter name constant for the metric category */
	public static final String METRIC_CATEGORY = "SBS.METRIC.CATEGORY";
	/** The parameter name constant for the metric name */
	public static final String METRIC_NAME = "SBS.METRIC.NAME";
	/** The parameter name constant for the metric value */
	public static final String METRIC_VALUE = "SBS.METRIC.VALUE";
	/** The parameter name constant for the metric type */
	public static final String METRIC_TYPE = "SBS.METRIC.TYPE";
	/** The parameter name constant for the metric type value of Long*/
	public static final String METRIC_TYPE_LONG = "LONG";
	/** The parameter name constant for the metric type value of Int*/
	public static final String METRIC_TYPE_INT = "INT";
	/** The parameter name constant for the metric type value of String*/
	public static final String METRIC_TYPE_STRING = "STRING";	
	/** The parameter name constant for the metric type value of Long Counter*/
	public static final String METRIC_TYPE_COUNTER_LONG = "CLONG";
	/** The parameter name constant for the metric type value of Int Counter*/
	public static final String METRIC_TYPE_COUNTER_INT = "ILONG";
	/** The parameter name constant for the metric type value of Incident*/
	public static final String METRIC_TYPE_INCIDENT = "INCIDENT";
	/** The parameter name constant for the metric type value of Batch*/
	public static final String METRIC_TYPE_BATCH = "BATCH";
	/** The parameter name constant for the Batch type record delimeter*/
	public static final String BATCH_REC_DELIM = "REC_DELIM";
	/** The parameter name constant for the Batch type field delimeter*/
	public static final String BATCH_FIELD_DELIM = "FLD_DELIM";
	
	/**	The servlet's tracer */
	protected ITracer tracer = null;
	
	/**	An origin adapter to decode the originating IP address to a domain specific name */
	protected OriginAdapter originAdapter = null;
	
	

	/**
	 * Basic constructor.
	 */
	public CollectorServlet() {
		
	}
	
	/**
	 * Initializes the servlet during startup lifecycle.
	 * @throws ServletException
	 */
	public void init(ServletConfig config) throws ServletException {
		tracer = TracerFactory.getInstance();
		String originAdapterClassName = config.getInitParameter("origin.adapter.class.name");
		if(originAdapterClassName!=null) {
			// the class name is not null, so instantiate the adapter.
			try {
				Class clazz = Class.forName(originAdapterClassName);
				originAdapter = (OriginAdapter)clazz.newInstance();
			} catch (Exception e) {
				log("Failed to instantiate OriginAdapter class:" + originAdapterClassName, e);
				originAdapter = null;
			}
		}
	}
	
	/**
	 * Handles a remote tracer's submission of metrics.
	 * @throws ServletException
	 * @throws IOException 
	 */
	public void service(HttpServletRequest req, HttpServletResponse res) throws
    ServletException, IOException {
		try {			
			// test to see if an origin adapter is defined.
			// if it is, decode the orginating IP address.
			String decodedOrigin = null;
			if(originAdapter!=null) {
				decodedOrigin = originAdapter.decodeOrigin(req.getRemoteAddr());
			}
			String fieldDelimeter = null;
			String recordDelimeter = null;
			String value = null;
			String type = getSafeParameter(req, METRIC_TYPE);
			// Check to see if this metric submission is batching multiple metrics
			if(type.equalsIgnoreCase(METRIC_TYPE_BATCH)) {				
				// This is a batch submission.
				// Capture the field and record delimeters and the value 
				// and pass off to the processBatch method. 
				fieldDelimeter = getSafeParameter(req, BATCH_FIELD_DELIM);
				recordDelimeter = getSafeParameter(req, BATCH_REC_DELIM);
				value = req.getParameter(METRIC_VALUE);
				processBatch(value, fieldDelimeter, recordDelimeter, decodedOrigin);				
			} else {
				// the submission was not a batch submission.
				// parse out the category, name and value and trace.
				String category = getSafeParameter(req,METRIC_CATEGORY);
				String name = getSafeParameter(req, METRIC_NAME);
				value = req.getParameter(METRIC_VALUE);
				if((!type.equalsIgnoreCase(METRIC_TYPE_INCIDENT)) && (value == null || value.length() < 1)) {
					res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR , "Metric Value Null Or Not Recognized");
					return;
				}
				try {					
					recordTrace(category, name, value, type);
					if(originAdapter != null && decodedOrigin != null) {
						String locationCategory = originAdapter.rewriteCategory(category, tracer.getEscapedSegmentDelimeter(), decodedOrigin);
						if(locationCategory!=null) {
							recordTrace(locationCategory, name, value, type);
						}
					}					
				} catch (Exception e) {
					res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR , e.getMessage());
					return;				
				}	
			}
			res.setStatus(HttpServletResponse.SC_ACCEPTED);
		} catch (Exception e) {
			res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR , e.getMessage());
		}		
	}
	
	/**
	 * Processes a submitted batch of records.
	 * A batch is a single string conmtaining multiple metric submissions.
	 * Each individual metric submission is delimited by the Record Delimeter.
	 * Each field in a metric submission is delimited by the Field Delimeter.
	 * The fields in a metric submission should be ordered as follows: <OL>
	 * <li>Metric Category
	 * <li>Metric Name
	 * <li>Metric Value
	 * <li>Metric Type
	 * </OL>
	 * Note that no exceptions are thrown for batch submissions so the response to the caller will always be success.
	 * @param value The batch of metric submissions.
	 * @param fDelim The field delimeter.
	 * @param rDelim The record delimeter.
	 * @param decodedOrigin The decoded originating IP address. Ignored if null.
	 */
	public void processBatch(String value, String fDelim, String rDelim, String decodedOrigin) {
		String[] records = value.split(rDelim);
		String[] fields = null;
		for(String s: records) {
			fields = s.split(fDelim);
			try {				
				recordTrace(fields[0], fields[1], fields[2], fields[3]);
				if(originAdapter != null && decodedOrigin != null) {
					String locationCategory = originAdapter.rewriteCategory(fields[0], tracer.getEscapedSegmentDelimeter(), decodedOrigin);
					recordTrace(locationCategory, fields[1], fields[2], fields[3]);
				}
			} catch (Exception e) {
			}
		}
	}
	
	
	/**
	 * Records an individual metric.
	 * @param category Them metric resource segment.
	 * @param name The metric name.
	 * @param value The value of the submited metric.
	 * @param type The type of the submited metric.
	 * @throws Exception 
	 */
	public void recordTrace(String category, String name, String value, String type) throws Exception {
		tracer.trace(category);
		if(type.equalsIgnoreCase(METRIC_TYPE_LONG)) {
			tracer.recordMetric(category, name, Long.parseLong(value));
		} else if(type.equalsIgnoreCase(METRIC_TYPE_INT)) {
			tracer.recordMetric(category, name, Integer.parseInt(value));
		} else if(type.equalsIgnoreCase(METRIC_TYPE_STRING)) {
			tracer.recordMetric(category, name, value);
		} else if(type.equalsIgnoreCase(METRIC_TYPE_COUNTER_LONG)) {
			tracer.recordCounterMetric(category, name, Long.parseLong(value));
		} else if(type.equalsIgnoreCase(METRIC_TYPE_COUNTER_INT)) {
			tracer.recordCounterMetric(category, name, Integer.parseInt(value));
		} else if(type.equalsIgnoreCase(METRIC_TYPE_INCIDENT)) {
			tracer.recordMetric(category, name);
		} else {
			throw new Exception("Metric Type Not Recognized:" + type);
		}
	}
	
	/**
	 * Reads the requested parameter from the passed httpServlet request.
	 * If the result is null or zero length, an exception is thrown.
	 * @param req
	 * @param paramName
	 * @return The extracted parameter value.
	 * @throws Exception
	 */
	public String getSafeParameter(HttpServletRequest req, String paramName)  throws Exception {
		String s = req.getParameter(paramName);
		if(s!=null && s.length()>0) return s;
		else throw new Exception("Parameter " + paramName + " was null or zero length.");
	}

}
