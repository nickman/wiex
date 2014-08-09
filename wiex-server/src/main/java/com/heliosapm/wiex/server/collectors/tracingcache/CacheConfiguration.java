/**
 * 
 */
package com.heliosapm.wiex.server.collectors.tracingcache;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import javax.management.ObjectName;



import org.w3c.dom.Node;

import com.heliosapm.wiex.server.server.utils.JMXUtils;
import com.heliosapm.wiex.server.server.utils.XMLHelper;


/**
 * <p>Title: CacheConfiguration</p>
 * <p>Description: A value object to hold the configuration of a <code>CacheValue</code>.</p> 
 * <p>Copyright: Copyright (c) 2008</p>
 * <p>Company: Helios Development Group</p>
 * @author Whitehead
 */

public class CacheConfiguration {
	
	/** The default object name of the cache service */
	public static final ObjectName CACHE_SERVICE_OBJECT_NAME = JMXUtils.safeObjectName("com.heliosapm.wiex.tracing.collectors:service=CacheService,name=Default");
	
	
	/** The JMX objectName of the target cache service. Defaults to <code>CACHE_SERVICE_OBJECT_NAME</code>. */
	protected ObjectName cacheServiceObjectName = CACHE_SERVICE_OBJECT_NAME;
	/** The name of the cache attribute */
	protected String attributeName = null;
	/** The class name of the cache container */
	protected String containerClassName = null;
	/** The cache container class */
	protected Class<?> containerClass = null;
	/** The attributes of the container instance */
	protected Map<String, String> containerAttributes = new HashMap<String, String>();
	/** A regex pattern to test a pattern for a match */
	protected String matchPatternValue = null;
	/** A compiled regex pattern to test a pattern for a match */
	protected Pattern matchPattern = null;
	/** A regex pattern to replace */
	protected String replacePatternValue = null;
	/** A compiled regex pattern to replace */
	protected Pattern replacePattern = null;
	/** The configured container class */
	protected CacheContainer cacheContainer = null;
	
	
	
	/**
	 * Parameterless constructor.
	 * Required since objects of this class will be instantiated reflectively.
	 */
	public CacheConfiguration() {
		
	}
	
	/**
	 * Builds and returns a configured CacheConfiguration.
	 * @param cacheElement A <code>CacheValue</code> XML node.
	 * @return
	 */
	public static CacheConfiguration getInstance(Node cacheElement) {
		CacheConfiguration cacheConfig = new CacheConfiguration();
		try {
			Node node = XMLHelper.getChildNodeByName(cacheElement, "objectName", false);
			if(node!=null) {
				cacheConfig.setCacheServiceObjectName(new ObjectName(node.getFirstChild().getNodeValue()));
			}
			String attrName = XMLHelper.getMandatoryNodeTextByName(cacheElement, "attributeName", false);			
			cacheConfig.setAttributeName(attrName);
			node = XMLHelper.getMandatoryChildNodeByName(cacheElement, "containerClass", false);
			cacheConfig.setContainerClassName(XMLHelper.getMandatoryNodeTextByName(node, "className", false));
			Map<String, String> attributes  = XMLHelper.getChildNodesAttributeAndValue(node, "attribute", "name", false);
			Class<CacheContainer> ccClazz  = (Class<CacheContainer>) Class.forName(cacheConfig.getContainerClassName(), true, cacheConfig.getClass().getClassLoader());
			
			Object obj = ccClazz.newInstance();
			for(Entry<String, String> entry: attributes.entrySet()) {
				JMXUtils.setAttribute(entry.getKey(), entry.getValue(), 
						JMXUtils.getAttributeType(ccClazz, entry.getKey()),  
						obj);
			}			
			cacheConfig.setCacheContainer((CacheContainer)obj);
			String tmp = XMLHelper.getNodeTextByName(cacheElement, "matchPattern", false);
			if(tmp!=null) cacheConfig.setMatchPatternValue(tmp);
			tmp = XMLHelper.getNodeTextByName(cacheElement, "replacePattern", false);
			if(tmp!=null) cacheConfig.setReplacePatternValue(tmp);
			return cacheConfig;
		} catch (Exception e) {
			throw new RuntimeException("Failed to create new CacheConfiguration", e);
		}
	}
	
	/*
	Sample XML Config:
						<CacheResult>
							<objectName>com.heliosapm.wiex.monitoring:type=CachedResultSets,name=DC1</objectName>
							<attributeName>OracleConnectionsByServer</attributeName>
							<containerClass>
								<className>com.heliosapm.wiex.server.collectors.jdbc.cache.HistoryBufferingCachedResultSet2</className>							
								<attribute name="HistorySize">1</attribute>
							</containerClass>
							<matchPattern>.*</matchPattern>
							<replacePattern>.*</replacePattern>
						</CacheResult>

	 */
	
	
	/**
	 * @return the cacheServiceObjectName
	 */
	public ObjectName getCacheServiceObjectName() {
		return cacheServiceObjectName;
	}
	/**
	 * @param cacheServiceObjectName the cacheServiceObjectName to set
	 */
	public void setCacheServiceObjectName(ObjectName cacheServiceObjectName) {
		this.cacheServiceObjectName = cacheServiceObjectName;
	}
	/**
	 * @return the attributeName
	 */
	public String getAttributeName() {
		return attributeName;
	}
	/**
	 * @param attributeName the attributeName to set
	 */
	public void setAttributeName(String attributeName) {
		this.attributeName = attributeName;
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
	 * @return the containerAttributes
	 */
	public Map<String, String> getContainerAttributes() {
		return containerAttributes;
	}
	/**
	 * @param containerAttributes the containerAttributes to set
	 */
	public void setContainerAttributes(Map<String, String> containerAttributes) {
		this.containerAttributes = containerAttributes;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((attributeName == null) ? 0 : attributeName.hashCode());
		result = prime
				* result
				+ ((cacheServiceObjectName == null) ? 0
						: cacheServiceObjectName.hashCode());
		return result;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final CacheConfiguration other = (CacheConfiguration) obj;
		if (attributeName == null) {
			if (other.attributeName != null)
				return false;
		} else if (!attributeName.equals(other.attributeName))
			return false;
		if (cacheServiceObjectName == null) {
			if (other.cacheServiceObjectName != null)
				return false;
		} else if (!cacheServiceObjectName.equals(other.cacheServiceObjectName))
			return false;
		return true;
	}
	/**
	 * Constructs a <code>String</code> with all attributes
	 * in name = value format.
	 *
	 * @return a <code>String</code> representation 
	 * of this object.
	 */
	public String toString()
	{
	    final String TAB = "    ";
	
	    StringBuilder retValue = new StringBuilder();
	    
	    retValue.append("CacheConfiguration ( ")
	        .append(super.toString()).append(TAB)
	        .append("\n\tcacheServiceObjectName = ").append(this.cacheServiceObjectName).append(TAB)
	        .append("\n\tattributeName = ").append(this.attributeName).append(TAB)
	        .append("\n\tcontainerClassName = ").append(this.containerClassName).append(TAB)
	        .append("\n\tMatchPatternValue = ").append(this.matchPatternValue).append(TAB)
	        .append("\n\tcontainerAttributes = ");
	    	for(Entry<String, String> entry: containerAttributes.entrySet()) {
	    		retValue.append("\n\t\t").append(entry.getKey()).append(" = ").append(entry.getValue());
	    	}
	        
	    	retValue.append("\n )");
	     
	    return retValue.toString();
	}

	/**
	 * @return the matchPatternValue
	 */
	public String getMatchPatternValue() {
		return matchPatternValue;
	}

	/**
	 * @param matchPatternValue the matchPatternValue to set
	 */
	public void setMatchPatternValue(String matchPatternValue) {
		this.matchPatternValue = matchPatternValue;
		matchPattern = Pattern.compile(matchPatternValue);
	}

	/**
	 * @return the matchPattern
	 */
	public Pattern getMatchPattern() {
		return matchPattern;
	}

	/**
	 * @return the replacePatternValue
	 */
	public String getReplacePatternValue() {
		return replacePatternValue;
	}

	/**
	 * @param replacePatternValue the replacePatternValue to set
	 */
	public void setReplacePatternValue(String replacePatternValue) {
		this.replacePatternValue = replacePatternValue;
		replacePattern = Pattern.compile(replacePatternValue);
	}

	/**
	 * @return the replacePattern
	 */
	public Pattern getReplacePattern() {
		return replacePattern;
	}

	/**
	 * @return the cacheContainer
	 */
	public CacheContainer getCacheContainer() {
		return cacheContainer;
	}

	/**
	 * @param cacheContainer the cacheContainer to set
	 */
	protected void setCacheContainer(CacheContainer cacheContainer) {
		this.cacheContainer = cacheContainer;
	}

	
}
