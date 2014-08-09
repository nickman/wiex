
package com.heliosapm.wiex.server.collectors.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.ReflectionException;

/**
 * <p>Title: BindVariable</p>
 * <p>Description: Container and access methods for query bind variables.</p> 
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Company: Helios Development Group</p>
 * @author Whitehead
 * @version $Revision: 1.2 $
 */

public class BindVariable {
	
	/**	The bind variable sequence */
	protected int number = 0;
	/**	The source of the data to be bound */
	protected String source = null;
	/**	The JDBC data type of the bind */
	protected String type = null;
	/**	The JMX ObjectName of the attribute sourced data */
	protected ObjectName objectName = null;
	/**	The JMX attribute name of the attribute sourced data */
	protected String attributeName = null;
	/** The MBean Server where the source can be retrieved */
	protected MBeanServer mBeanServer = null;
	
	
	
	/**
	 * @return the mBeanServer
	 */
	public MBeanServer getMBeanServer() {
		return mBeanServer;
	}

	/**
	 * @param beanServer the mBeanServer to set
	 */
	public void setMBeanServer(MBeanServer beanServer) {
		mBeanServer = beanServer;
	}

	/**
	 * @param number
	 * @param source
	 * @param type
	 */
	public BindVariable(int number, String source, String type) {
		this.number = number;
		this.source = source;
		this.type = type;
	}
	
	/**
	 * Executes the bind of the bind variable against the passed PreparedStatement
	 * @param ps
	 * @throws AttributeNotFoundException
	 * @throws InstanceNotFoundException
	 * @throws MBeanException
	 * @throws ReflectionException
	 * @throws SQLException
	 */
	public void bind(PreparedStatement ps) throws AttributeNotFoundException, InstanceNotFoundException, MBeanException, ReflectionException, SQLException {
		// get the value
		Object value = null;
		if(source.equals("attribute")) {
			value = mBeanServer.getAttribute(objectName, attributeName);
		}
		if(value==null) {
			throw new AttributeNotFoundException("Null value for source");
		}
		if(type.equals("String")) {
			ps.setString(number, value.toString());
		}
	}
	
	/**
	 * Returns a hash code value for the object. This method is supported for the benefit of hashtables such as those provided by java.util.Hashtable.
	 * @return a hash code value for this object.
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + number;
		return result;
	}
	
	/**
	 * @param obj the reference object with which to compare.
	 * @return true if this object is the same as the obj argument; false otherwise.
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
		final BindVariable other = (BindVariable) obj;
		if (number != other.number)
			return false;
		return true;
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
	 * @return the number
	 */
	public int getNumber() {
		return number;
	}
	/**
	 * @param number the number to set
	 */
	public void setNumber(int number) {
		this.number = number;
	}
	/**
	 * @return the objectName
	 */
	public ObjectName getObjectName() {
		return objectName;
	}
	/**
	 * @param objectName the objectName to set
	 */
	public void setObjectName(ObjectName objectName) {
		this.objectName = objectName;
	}
	/**
	 * @return the source
	 */
	public String getSource() {
		return source;
	}
	/**
	 * @param source the source to set
	 */
	public void setSource(String source) {
		this.source = source;
	}
	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}
	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}

	   /**
		 * Renders a readable string representing this bind variable. 
		 * @return a string representation of the object.
		 */
		public String toString() {
			StringBuilder buffer = new StringBuilder();
			buffer.append("BindVariable[");
			buffer.append("attributeName = ").append(attributeName);
			buffer.append(" number = ").append(number);
			buffer.append(" objectName = ").append(objectName);
			buffer.append(" source = ").append(source);
			buffer.append(" type = ").append(type);
			buffer.append("]");
			return buffer.toString();
		}
	
}
