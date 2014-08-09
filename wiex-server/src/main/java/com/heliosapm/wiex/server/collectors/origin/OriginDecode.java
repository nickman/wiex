/**
 * 
 */
package com.heliosapm.wiex.server.collectors.origin;

/**
 * <p>Title: OriginDecode</p>
 * <p>Description: Decoded Origin Pojo for hibernate etc.</p> 
 * <p>Copyright: Copyright (c) 2007</p>
 * <p>Company: Helios Development Group</p>
 * @author Whitehead
 * @version $Revision: 1.1 $
 */

public class OriginDecode {
	/**	The origin, possibly pre-processed. (PK) */
	protected String origin = null;
	/**	The decoded origin representing the domain specific decoded origin */
	protected String decode = null;
	
	/**
	 * Parameterless constructor.
	 */
	public OriginDecode() {
	}

	
	/**
	 * Creates a new OriginDecode
	 * @param origin
	 * @param decode
	 */
	public OriginDecode(String origin, String decode) {
		this.origin = origin;
		this.decode = decode;
	}
	/**
	 * @return the decode
	 */
	public String getDecode() {
		return decode;
	}
	/**
	 * @param decode the decode to set
	 */
	public void setDecode(String decode) {
		this.decode = decode;
	}
	/**
	 * @return the origin
	 */
	public String getOrigin() {
		return origin;
	}
	/**
	 * @param origin the origin to set
	 */
	public void setOrigin(String origin) {
		this.origin = origin;
	}


	/**
	 * @return
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + ((origin == null) ? 0 : origin.hashCode());
		return result;
	}


	/**
	 * @param obj
	 * @return
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
		final OriginDecode other = (OriginDecode) obj;
		if (origin == null) {
			if (other.origin != null)
				return false;
		} else if (!origin.equals(other.origin))
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
	    
	    retValue.append("OriginDecode ( ")
	        .append(super.toString()).append(TAB)
	        .append("origin = ").append(this.origin).append(TAB)
	        .append("decode = ").append(this.decode).append(TAB)
	        .append(" )");
	    
	    return retValue.toString();
	}
}
