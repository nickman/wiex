/**
 * 
 */
package com.heliosapm.wiex.server.tracing.collectors.jdbc;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.Date;

import org.apache.log4j.Logger;

/**
 * <p>Title: JDBCHelper</p>
 * <p>Description: Static JDBC Helper Methods</p> 
 * <p>Copyright: Copyright (c) 2007</p>
 * <p>Company: Helios Development Group</p>
 * @author Whitehead
 * @version $Revision$
 */

public class JDBCHelper {
	
	protected static Logger log = Logger.getLogger(JDBCHelper.class);
	
	/**
	 * Creates a formatted HTML table from a JDBC ResultSet
	 * @param rset The JDBC ResultSet to format
	 * @return An HTML Table String.
	 */
	public static String generateHTMLForResultSet(ResultSet rset) {
		try {			
			StringBuilder buff = new StringBuilder("<TABLE BORDER=\"1\"><TR>");
			ResultSetMetaData rsmd = rset.getMetaData();
			int columnCount = rsmd.getColumnCount();
			int rowCount = 0;
			
			// Generate Header
			for(int i = 1; i <= columnCount; i++) {
				buff.append("<TH>").append(rsmd.getColumnLabel(i)).append("</TH>");
			}
			buff.append("</TR>");
			// Generate Data Records
			while(rset.next()) {
				rowCount++;
				buff.append("<TR>");
				for(int i = 1; i <= columnCount; i++) {
					buff.append("<TD>").append(rset.getString(i)).append("</TD>");
				}
				buff.append("</TR>");
			}
			buff.append("</TABLE>");
			buff.append("<i><font size=\"-2\">Row Count: ").append(rowCount).append(", Effective Date:").append(new Date()).append("</font></i>");
			return buff.toString();
		} catch (Exception e) {
			log.error("Failed to generate HTML from ResultSet", e);
			return "<H4>Failed to generate HTML from ResultSet:" + e + "</H4>";
		}
	}
}
