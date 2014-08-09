package com.heliosapm.wiex.server.collectors.jdbc;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.management.ObjectName;

import org.apache.log4j.Logger;

import com.heliosapm.wiex.server.collectors.jdbc.cache.CachedResultSetImpl;



/**
 * <p>Title: SQLMapping</p>
 * <p>Description: A container for a column to metric mappings</p> 
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Company: Helios Development Group</p>
 * @author Whitehead
 * @version $Revision: 1.4 $
 */

public class SQLMapping {
	/**	The segment portion of the metric */
	protected String[] metricSegment = null;
	/**	The metric name */
	protected String metricName = null;
	/** The column id the data should come from */
	protected int column = -1;
	/** The tracing counter type */
	protected String counterType = null;
	/**	The attribute name */
	protected String attributeName = null;
	/**	The attribute type */
	protected String attributeType = null;
	/** Indicates if an attribute is defined */
	protected boolean attributeDefined = false;
	/** Indicates if a CacheResult is applied to this query */
	protected boolean cacheResult = false;
	/** The object name of the MBean in which to store the cached result set */
	protected ObjectName cacheResultObjectName = null;
	/** The attribute name within the MBean in which to store the cached result set */
	protected String cacheResultAttributeName = null;
	/** Is the mapping populated for any purpose */
	protected boolean populated = false;
	/** Indicates if a trace is applied to this query */
	protected boolean traceDefined = false;
	/** The container class name that should be used for cached query result sets */
	protected String containerClassName = CachedResultSetImpl.class.getName();
	/** The URL for the source groovy code to perform the result set post-process */
	protected URL postProcessorURL = null;
	/** The compiled groovy script to perform the result set post-process */
	protected Script postProcessorScript = null;
	/** The GroovyShell to support the post processing script */
	protected GroovyShell groovyShell = null;
	/** The last recompile time of the groovy script */
	protected long postProcessorLastCompile = Long.MIN_VALUE;
	/** The last processing elapsed time for the post processor */
	protected long postProcessorElapsedTime = 0;
	/** Indicates if PreparedStatements and Bind variables should be used */
	protected boolean useBinds = true;
	
	/** The groovy script binding to perform the result set post-process */
	protected Binding postProcessorBinding = null;
	
	//=================================
	// Scope Handling
	//=================================
	/** Defines if mapping is scoped */
	protected boolean scoped = false;
	/** Defines the metric reset value */
	protected String scopeResetValue = "0";
	//=================================
	/** The (default) name of the post processing method in the groovy script */
	public static final String POST_PROCESSOR_METHOD_NAME = "postProcess";
	
	/** The logger */
	protected static Logger log = Logger.getLogger(SQLMapping.class);
	
	
	/** The compund name pattern matcher */
	protected static Pattern namePattern = null;
	/** The bind variable pattern matcher */
	protected static Pattern bindPattern = null;
	/** The meta-data extractor pattern matcher */
	protected static Pattern metaDataPattern = null;
	
	/** The delimeter for flattened records */
	protected String flatten = null;
	
	
	static {
		namePattern = Pattern.compile("([a-zA-Z\\+\\-\\(\\)\\s-]+)|(\\{\\d++\\})");
		metaDataPattern = Pattern.compile("\\{([a-zA-Z0-9\\(\\)\\s-]+):(\\d++)\\}");
		bindPattern = Pattern.compile("\\d++");
	}
	
	
	/**
	 * Creates a new SQLMapping
	 */
	public SQLMapping() {
		
	}
	
	/**
	 * Creates a new SQLMapping
	 * @param metricSegment
	 * @param metricName
	 * @param column
	 * @param counterType
	 */
	public SQLMapping(String[] metricSegment, String metricName, int column, String counterType) {		
		this.setMetricSegment(metricSegment);
		this.setMetricName(metricName);
		this.setColumn(column);
		this.setCounterType(counterType);
	}
	/**
	 * @return the column
	 */
	public int getColumn() {
		return column;
	}
	/**
	 * @param column the column to set
	 */
	public void setColumn(int column) {
		this.column = column;
	}
	/**
	 * @return the counterType
	 */
	public String getCounterType() {		
		return counterType;
	}
	/**
	 * @return the counterType
	 */
	public String getCounterType(String[] values, String[] header) {		
		return format(counterType, values, header);
	}
	
	/**
	 * @param counterType the counterType to set
	 */
	public void setCounterType(String counterType) {
		this.counterType = counterType;
	}
	/**
	 * @return the metricName
	 */
	public String getMetricName() {
		return metricName;
	}

	/**
	 * @param results
	 * @return the formatted metric name.
	 */
	public String getMetricName(String[] results, String[] header) {
		return format(metricName, results, header);
	}
	
	/**
	 * @param metricName the metricName to set
	 */
	public void setMetricName(String metricName) {
		this.metricName = metricName;
	}

	/**
	 * @return the metricSegment
	 */
	public String[] getMetricSegment() {
		return metricSegment;
	}
	
	/**
	 * @return the metricSegment
	 */
	public String[] getMetricSegment(String[] results, String[] header) {
		String[] fmSegments = new String[metricSegment.length];
		for(int i = 0; i < metricSegment.length; i++) {
			fmSegments[i] = format(metricSegment[i], results, header);
		}
		return fmSegments;
	}

	
	/**
	 * @param metricSegment the metricSegment to set
	 */
	public void setMetricSegment(String[] metricSegment) {
		this.metricSegment = metricSegment;
	}
	
	/**
	 * [Re]Compiles the post processor groovy script.
	 * @throws IOException 
	 */
	public synchronized void compilePostProcessor() throws IOException {
		if(postProcessorURL == null || postProcessorBinding == null) return;
		long start = System.currentTimeMillis();
		if(groovyShell==null) {
			groovyShell = new GroovyShell(postProcessorBinding);
		}
		InputStream is = null;
		Reader ri = null;
		try {
			is = postProcessorURL.openStream();
			ri = new InputStreamReader(is);
			postProcessorScript = groovyShell.parse(ri);
			postProcessorLastCompile = System.currentTimeMillis();
			long elapsed = postProcessorLastCompile - start; 
			log.info("Compiled Groovy Script [" + postProcessorURL + "] in " + elapsed + " ms.");
		} finally {
			try { ri.close(); } catch (Exception e) {}
			try { is.close(); } catch (Exception e) {}
		}		
	}
	
	/**
	 * Calls the post processor script.
	 * @param input The unprocessed result set.
	 * @param methodName The name of the post processing method to call.
	 * @return The processed result set.
	 */
	public String[][] invokePostProcessor(String[][] input, String methodName) {
		String[][] ret = null;
		long start = System.currentTimeMillis();
		ret = (String[][])postProcessorScript.invokeMethod(methodName, input);
		postProcessorElapsedTime = System.currentTimeMillis() - start;
		return ret;
	}

	/**
	 * Calls the post processor script. Uses the default method name.
	 * @param input The unprocessed result set.
	 * @return The processed result set.
	 */
	public String[][] invokePostProcessor(String[][] input) {
		return (String[][]) invokePostProcessor(input, POST_PROCESSOR_METHOD_NAME);
	}

	/**
	 * @return A uniquely identifying hashCode.
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = super.hashCode();
		result = PRIME * result + ((attributeName == null) ? 0 : attributeName.hashCode());
		result = PRIME * result + ((attributeType == null) ? 0 : attributeType.hashCode());
		result = PRIME * result + column;
		result = PRIME * result + ((counterType == null) ? 0 : counterType.hashCode());
		result = PRIME * result + ((flatten == null) ? 0 : flatten.hashCode());
		result = PRIME * result + ((metricName == null) ? 0 : metricName.hashCode());
		result = PRIME * result + Arrays.hashCode(metricSegment);
		return result;
	}

	/**
	 * @param obj
	 * @return returns true if the passed object has the same identity as this object.
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		final SQLMapping other = (SQLMapping) obj;
		if (attributeName == null) {
			if (other.attributeName != null)
				return false;
		} else if (!attributeName.equals(other.attributeName))
			return false;
		if (attributeType == null) {
			if (other.attributeType != null)
				return false;
		} else if (!attributeType.equals(other.attributeType))
			return false;
		if (column != other.column)
			return false;
		if (counterType == null) {
			if (other.counterType != null)
				return false;
		} else if (!counterType.equals(other.counterType))
			return false;
		if (flatten == null) {
			if (other.flatten != null)
				return false;
		} else if (!flatten.equals(other.flatten))
			return false;
		if (metricName == null) {
			if (other.metricName != null)
				return false;
		} else if (!metricName.equals(other.metricName))
			return false;
		if (!Arrays.equals(metricSegment, other.metricSegment))
			return false;
		return true;
	}

		/**
		 * Renders a human readable string representing the SQLMapping.
		 * @return A String rendering the SQLMapping
		 * @see java.lang.Object#toString()
		 */
		public String toString() {
			StringBuilder buffer = new StringBuilder();
			buffer.append("SQLMapping[");
			buffer.append("\n\tcolumn = ").append(column);
			buffer.append("\n\tcounterType = ").append(counterType);
			buffer.append("\n\tmetricName = ").append(metricName);
			if (metricSegment == null) {
				buffer.append("\n\tmetricSegment = ").append("null");
			} else {
				buffer.append("\n\tmetricSegment = ");
				for(String s: metricSegment) {
					buffer.append("\n\t\t").append(s);
				}
			}
			if(isAttributeDefined()) {
				buffer.append("\n\tattributeName = ").append(attributeName);
				buffer.append("\n\tattributeType = ").append(attributeType);
			}
			if(isFlatten()) {
				buffer.append("\n\tflatten = ").append(flatten);
			}
			if(isCacheResult()) {
				buffer.append("\n\tCacheResultObjectName = ").append(cacheResultObjectName);
				buffer.append("\n\tCacheResultAttributeName = ").append(cacheResultAttributeName);
			}
			if(postProcessorURL != null) {
				buffer.append("\n\tPostProcessing URL = ").append(postProcessorURL);
				
			}
			if(!useBinds) {
				buffer.append("\n\tBind Variables False ");
				
			}		
			if(scoped) {
				buffer.append("\n\tScoped: true");
				buffer.append("\n\t\tScope Reset Value:").append(scopeResetValue);
			}
			
			buffer.append("\n]");
			return buffer.toString();
		}

		/**
		 * @return the attributeName
		 */
		public String getAttributeName() {
			return attributeName;
		}
		
		/**
		 * Formats the attribute name and returns it.
		 * @param results The SQL row results.
		 * @return The formatted attribute name.
		 */
		public String getAttributeName(String[] results, String[] header) {
			return format(attributeName, results, header);
		}
		


		/**
		 * @param attributeName the attributeName to set
		 */
		public void setAttributeName(String attributeName) {
			this.attributeName = attributeName;
		}

		/**
		 * @return the attributeType
		 */
		public String getAttributeType() {
			return attributeType;
		}

		/**
		 * @param attributeType the attributeType to set
		 */
		public void setAttributeType(String attributeType) {
			this.attributeType = attributeType;
		}

		/**
		 * @return the attributeDefined
		 */
		public boolean isAttributeDefined() {
			return (attributeType != null && attributeName != null);
		}

		/**
		 * Formats the metric name by extracting the targeted result values and inserting them into the formated string.
		 * @param fText
		 * @param values
		 * @return A formated string.
		 */
		public static String format(String fText, String[] values, String[] header) {
			StringBuilder buff = new StringBuilder();
			Matcher m = namePattern.matcher(fText);
			Matcher metaMatcher = metaDataPattern.matcher(fText);
			String s = null;
			String metaName = null;
			int metaIndex = -1;
			boolean found = m.find();
			boolean found2 = metaMatcher.find();
			while(found) {
				s = m.group();				
				if(s.startsWith("{")) {
					Matcher m2 = bindPattern.matcher(s);
					if(m2.find()) {
						int i = Integer.parseInt(m2.group());
						buff.append(values[i]);					
					}
				} else {
					buff.append(s);
				}
				found = m.find();
			}
			if(found2) {
				try {
				if(metaMatcher.groupCount()==2) {
					metaName = metaMatcher.group(1);
					metaIndex = Integer.parseInt(metaMatcher.group(2));
					if("COLUMN-NAME".equalsIgnoreCase(metaName)) {
						buff.append(header[metaIndex]);
					}
				}
				} catch (Exception e) {
					
				}
			}
			return buff.toString();
		}
		
		//public static 

		/**
		 * @return the flatten
		 */
		public String getFlatten() {
			return flatten;
		}

		/**
		 * @param flatten the flatten to set
		 */
		public void setFlatten(String flatten) {
			if("CR".equalsIgnoreCase(flatten)) {
				this.flatten = "\n";
			} else if("TAB".equalsIgnoreCase(flatten)) {
				this.flatten = "\t";
			} else {
				this.flatten = flatten;
			}
		}
		
		/**
		 * Returns true if flatten is defined.
		 * @return true if flatten is defined.
		 */
		public boolean isFlatten() {
			return (flatten!=null);
		}

		/**
		 * @return the cacheResult
		 */
		public boolean isCacheResult() {
			return cacheResult;
		}

		/**
		 * @param cacheResult the cacheResult to set
		 */
		public void setCacheResult(boolean cacheResult) {
			this.cacheResult = cacheResult;
		}

		/**
		 * @return the cacheResultAttributeName
		 */
		public String getCacheResultAttributeName() {
			return cacheResultAttributeName;
		}

		/**
		 * @param cacheResultAttributeName the cacheResultAttributeName to set
		 */
		public void setCacheResultAttributeName(String cacheResultAttributeName) {
			this.cacheResultAttributeName = cacheResultAttributeName;
		}

		/**
		 * @return the cacheResultObjectName
		 */
		public ObjectName getCacheResultObjectName() {
			return cacheResultObjectName;
		}

		/**
		 * @param cacheResultObjectName the cacheResultObjectName to set
		 */
		public void setCacheResultObjectName(ObjectName cacheResultObjectName) {
			this.cacheResultObjectName = cacheResultObjectName;
		}

		/**
		 * @return the populated
		 */
		public boolean isPopulated() {
			return populated;
		}

		/**
		 * @param populated the populated to set
		 */
		public void setPopulated(boolean populated) {
			this.populated = populated;
		}

		public boolean isTraceDefined() {
			return traceDefined;
		}

		public void setTraceDefined(boolean traceDefined) {
			this.traceDefined = traceDefined;
		}

		/**
		 * @return the containerClassName
		 */
		public String getContainerClassName() {
			return containerClassName;
		}

		/**
		 * @param containerClassName the containerClassName to set
		 */
		public void setContainerClassName(String containerClassName) {
			this.containerClassName = containerClassName;
		}
		
		/**
		 * Tests to see if the source code at the URL has been modified since the last init.
		 * @return
		 * @throws IOException 
		 */
		protected boolean isModified()  {
			if(postProcessorLastCompile==0) return false;
			try {
				URLConnection urlConn = postProcessorURL.openConnection();
				long lastMod = urlConn.getLastModified();
				urlConn.getInputStream().close();
				return lastMod > postProcessorLastCompile;
			} catch (Exception e) { return false; }
		}

		/**
		 * @return the postProcessorBinding
		 */
		public Binding getPostProcessorBinding() {
			return postProcessorBinding;
		}

		/**
		 * @param postProcessorBinding the postProcessorBinding to set
		 */
		public void setPostProcessorBinding(Binding postProcessorBinding) {
			this.postProcessorBinding = postProcessorBinding;
		}

		/**
		 * @return the postProcessorLastCompile
		 */
		public long getPostProcessorLastCompile() {
			return postProcessorLastCompile;
		}

		/**
		 * @param postProcessorLastCompile the postProcessorLastCompile to set
		 */
		public void setPostProcessorLastCompile(long postProcessorLastCompile) {
			this.postProcessorLastCompile = postProcessorLastCompile;
		}

		/**
		 * @return the postProcessorScript
		 */
		public Script getPostProcessorScript() {
			return postProcessorScript;
		}

		/**
		 * @param postProcessorScript the postProcessorScript to set
		 */
		public void setPostProcessorScript(Script postProcessorScript) {
			this.postProcessorScript = postProcessorScript;
		}

		/**
		 * @return the postProcessorURL
		 */
		public URL getPostProcessorURL() {
			return postProcessorURL;
		}

		/**
		 * @param postProcessorURL the postProcessorURL to set
		 */
		public void setPostProcessorURL(URL postProcessorURL) {
			this.postProcessorURL = postProcessorURL;
		}

		/**
		 * @return the postProcessorElapsedTime
		 */
		public long getPostProcessorElapsedTime() {
			return postProcessorElapsedTime;
		}

		public boolean isUseBinds() {
			return useBinds;
		}

		public void setUseBinds(boolean useBinds) {
			this.useBinds = useBinds;
		}

		/**
		 * @return the scoped
		 */
		public boolean isScoped() {
			return scoped;
		}

		/**
		 * @param scoped the scoped to set
		 */
		public void setScoped(boolean scoped) {
			this.scoped = scoped;
		}

		/**
		 * @return the scopeResetValue
		 */
		public String getScopeResetValue() {
			return scopeResetValue;
		}

		/**
		 * @param scopeResetValue the scopeResetValue to set
		 */
		public void setScopeResetValue(String scopeResetValue) {
			this.scopeResetValue = scopeResetValue;
		}		
}


/*
 		select username, opname , nvl(target, '*') ACTION, count(*) CNT, sum(sofar), units from V$SESSION_LONGOPS L
		WHERE START_TIME > sysdate-(1/24)
		group  by username, opname,  target, units
		order by count(*) desc
*/		
