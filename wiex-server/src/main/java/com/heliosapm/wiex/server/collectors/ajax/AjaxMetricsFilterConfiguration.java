/**
 * 
 */
package com.heliosapm.wiex.server.collectors.ajax;

import java.util.*;
import java.util.Map.*;
import org.apache.log4j.*;
import javax.servlet.FilterConfig;

/**
 * Configuration class for the AjaxMetricsFilter
 * @author WhiteheN
 */


public class AjaxMetricsFilterConfiguration {
	/** master indicator to set if the agent is enabled or not */
	protected boolean enabled = true;
	/** a map of the parameter names in use */
	protected Map<String, String> parameterNames = new HashMap<String, String>(12);
	/** a map of the run options in use */
	protected Map<String, Boolean> runOptions = new HashMap<String, Boolean>(4);
	/** a map of the batch options in use */
	protected Map<String, Integer> batchOptions = new HashMap<String, Integer>(2);
	/** the instance logger */
	protected Logger log = null;
	/** the singleton static instance of the config object */
	protected static AjaxMetricsFilterConfiguration config = null;
	/** the singleton static instance constructor synchrnonization lock */
	protected static Object lock = new Object();
	/** indicates if the config has been configured */
	protected boolean inited = false;
	/** the class name of the AjaxListener to be registered in the agent. */
	protected String listenerClassName = "AjaxListener";
	/** The variable name of the agent */
	protected String agentName = "xagent";
	/** The logging level of the client */
	protected int agentLogLevel = 0;
	
	
	
	//==========================================================================
	// Run Option property definitions
	//==========================================================================
	public static final String reportInteractive = "rReportInteractive";
	public static final String reportDelta = "rReportDelta";
	public static final String logging = "rLogging";
	public static final String uploadBatch = "rUploadBatch";
	public static final String uploadInParams = "rReportInParams";
	
	//==========================================================================
	// Batch Upload property definitions
	//==========================================================================	
	public static final String batchTime = "bBatchTime";
	public static final String batchSize = "bBatchSize";
	//==========================================================================
	// Parameter Name property definitions
	//==========================================================================	
	public static final String pUri = "pUri";
	public static final String pElapsed = "pElapsed";
	public static final String pConcurrent = "pConcurrent";
	public static final String pException = "pException";
	public static final String pHcode = "pHcode";
	public static final String pDelta = "pDelta";
	public static final String pFailure = "pFailure";
	public static final String pInteractive = "pInteractive";
	public static final String pInteractiveTime = "pInteractiveTime";
	public static final String pInteractiveCallbacks = "pInteractiveCallbacks";
	public static final String pSerialNumber = "pSerialNumber";
	public static final String pParameter = "pParameter";
	public static final String pRunOption = "pRunOption";
	
	
	/**
	 * Singleton accessor to the single instance of a AjaxMetricsFilterConfiguration
	 * @return The AjaxMetricsFilterConfiguration
	 */
	public static AjaxMetricsFilterConfiguration getInstance() {		
		if(config==null) {
			synchronized(lock) {
				if(config==null) {
					config = new AjaxMetricsFilterConfiguration();
				}
			}
		}
		return config;
	}
	
	/**
	 * Returns the paraneter name for the passed value.
	 * @param s
	 * @return The parameter name in effect.
	 */
	public String p(String s){
		return parameterNames.get(s);
	}
	
	/**
	 * Creates a new AjaxMetricsFilterConfiguration populated with the default values.
	 */
	protected AjaxMetricsFilterConfiguration() {
		log = Logger.getLogger(getClass());
		//==========================================================================
		// Set default property values
		//==========================================================================
		parameterNames.put(pUri,"AG_U");
		parameterNames.put(pElapsed,"AG_E");
		parameterNames.put(pConcurrent,"AG_C");
		parameterNames.put(pException,"AG_X");
		parameterNames.put(pHcode,"AG_H");
		parameterNames.put(pDelta,"AG_D");
		parameterNames.put(pFailure,"AG_F");
		parameterNames.put(pInteractive,"AG_I");
		parameterNames.put(pInteractiveTime,"AG_T");
		parameterNames.put(pInteractiveCallbacks,"AG_B");
		parameterNames.put(pSerialNumber,"AG_S");
		parameterNames.put(pParameter,"AG_P");
		parameterNames.put(pRunOption,"AG_O");	
		
		
		runOptions.put(reportInteractive, false);
		runOptions.put(reportDelta, true);
		runOptions.put(logging, false);
		runOptions.put(uploadBatch, false);
		runOptions.put(uploadInParams, false);
		
		batchOptions.put(batchTime, 40);
		batchOptions.put(batchSize, 5);
	}
	
	/**
	 * Looks up a run option value.
	 * @param name The name of the option.
	 * @return a true or false.
	 */
	public boolean getRunOption(String name) {
		return runOptions.get(name);
	}

	/**
	 * Looks up a parameter name.
	 * @param name The name of the parameter.
	 * @return A name.
	 */	
	public String getParamName(String name) {
		return parameterNames.get(name);
	}

	
	/**
	 * Generates a JavaScript command to update the JavaScript agent's parameter names.
	 * Follows this format: <code>agent.updateParameterNames({serial: 'XXXX', parameter: 'YYYY'});</code>
	 * @return A JavaScript Command.
	 */
	public String generateCommand(String type, Map<String, ?> props) {
		StringBuilder buff = new StringBuilder(agentName);		
		buff.append(".update").append(type).append("({");
		for(Entry<String, ?> entry: props.entrySet()) {
			if(entry.getValue() instanceof String) {
				buff.append(entry.getKey()).append(": '").append(entry.getValue()).append("',");
			} else {
				buff.append(entry.getKey()).append(": ").append(entry.getValue()).append(",");
			}
			
		}
		buff.deleteCharAt(buff.length()-1);
		buff.append("});");
		return buff.toString();
	}
	
	/**
	 * Generates JavaScript commands to execute a full initialization configuration of the JavaScript Agent.
	 * @return A JavaScript Command Set.
	 */
	public String generateInitCommands() {
		StringBuilder buff = new StringBuilder();
		buff.append(generateCommand("ParameterNames", parameterNames));
		buff.append(generateCommand("RunOptions", runOptions));
		buff.append(generateCommand("BatchOptions", batchOptions));
		buff.append(agentName).append(".setLogLevel(").append(agentLogLevel).append(");");
		buff.append(agentName).append(".setEnabled(").append(enabled).append(");");
		buff.append(agentName).append(".setAjaxListener(new ").append(listenerClassName).append("(").append(agentName).append("));");
		
		return buff.toString();
	}
	
	/**
	 * Generates a string representation of the configuration options.
	 * @return A string
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuilder buff = new StringBuilder("AjaxMetricsFilterConfiguration:");
		buff.append("\n\tParameter Names:");
		for(Entry<String, String> entry: parameterNames.entrySet()) {
			buff.append("\n\t\t").append(entry.getKey()).append(":").append(entry.getValue());
		}
		buff.append("\n\tRun Options:");
		for(Entry<String, Boolean> entry: runOptions.entrySet()) {
			buff.append("\n\t\t").append(entry.getKey()).append(":").append(entry.getValue());
		}
		buff.append("\n\tBatch Options:");
		for(Entry<String, Integer> entry: batchOptions.entrySet()) {
			buff.append("\n\t\t").append(entry.getKey()).append(":").append(entry.getValue());
		}
		buff.append("\n\tListener Class Name:").append(listenerClassName);
		buff.append("\n\tAgent Logging Level:").append(agentLogLevel);
		buff.append("\n\tAgent Name:").append(agentName);
		return buff.toString();
	}
	
	/**
	 * Updates the configuration from the filter config.
	 * @param filterConfig
	 */
	@SuppressWarnings("unchecked")
	public void updateConfigration(FilterConfig filterConfig) {
		Enumeration<String> paramEnum = filterConfig.getInitParameterNames();
		while(paramEnum.hasMoreElements()) {
			String paramName = paramEnum.nextElement();
			if(paramName.startsWith("r")) {
				try {
					if(!runOptions.containsKey(paramName)) throw new Exception("Invalid Parameter Name");
					runOptions.put(paramName, Boolean.parseBoolean(filterConfig.getInitParameter(paramName)));
				} catch (Exception e) {
					log.warn("Init Parameter [" + paramName + "] is invalid or could not be set.", e);
				}
			} else if(paramName.startsWith("b")) {
				try {
					if(!batchOptions.containsKey(paramName)) throw new Exception("Invalid Parameter Name");
					batchOptions.put(paramName, Integer.parseInt(filterConfig.getInitParameter(paramName)));
				} catch (Exception e) {
					log.warn("Init Parameter [" + paramName + "] is invalid or could not be set.", e);
				}
			} else if(paramName.startsWith("p")) {
				try {
					if(!parameterNames.containsKey(paramName)) throw new Exception("Invalid Parameter Name");
					parameterNames.put(paramName, filterConfig.getInitParameter(paramName));
				} catch (Exception e) {
					log.warn("Init Parameter [" + paramName + "] is invalid or could not be set.", e);
				}
			} else if("listenerClassName".equals(paramName)) {
				listenerClassName = filterConfig.getInitParameter(paramName);
			} else if("agentLogLevel".equals(paramName)) {
				try {
					agentLogLevel = Integer.parseInt(filterConfig.getInitParameter(paramName));
				} catch (Exception e) {
					log.warn("Agent Log Level Could Not Be Set. Defaulting to [" + agentLogLevel + "].", e);
				}
								
			} else {
				log.warn("Init Parameter [" + paramName + "] was not recognized.");
			}
		}
		log.info("Completed AjaxMetrics Filter Configuration. Config is:\n" + toString());
		inited = true;
	}

	/**
	 * Generates a unique hashcode based off the hashCode of the toString() which will return a different 
	 * code when the config changes. 
	 * @return A hashCode of the object state.
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return toString().hashCode();
	}

	/**
	 * @return the inited
	 */
	public boolean isInited() {
		return inited;
	}

	/**
	 * @return the agentName
	 */
	public String getAgentName() {
		return agentName;
	}

	/**
	 * @param agentName the agentName to set
	 */
	public void setAgentName(String agentName) {
		this.agentName = agentName;
	}

	/**
	 * @return the listenerClassName
	 */
	public String getListenerClassName() {
		return listenerClassName;
	}

	/**
	 * @param listenerClassName the listenerClassName to set
	 */
	public void setListenerClassName(String listenerClassName) {
		this.listenerClassName = listenerClassName;
	}

	/**
	 * @return the enabled
	 */
	public boolean isEnabled() {
		return enabled;
	}

	/**
	 * @param enabled the enabled to set
	 */
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	/**
	 * @return the agentLogLevel
	 */
	public int getAgentLogLevel() {
		return agentLogLevel;
	}

	/**
	 * @param agentLogLevel the agentLogLevel to set
	 */
	public void setAgentLogLevel(int agentLogLevel) {
		this.agentLogLevel = agentLogLevel;
	}

	
	
	
	
	
}
