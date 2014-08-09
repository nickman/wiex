/**
 * 
 */
package com.heliosapm.wiex.server.collectors.jdbc.cache;

import java.util.Date;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import com.heliosapm.wiex.jmx.dynamic.annotation.JMXAttribute;
import com.heliosapm.wiex.jmx.dynamic.annotation.JMXManagedObject;


/**
 * <p>Title: HistoryBufferingCachedResultSet</p>
 * <p>Description: Customized ManagedObjectDynamicMBean for handling cached result sets and buffering of the last <i>n</i> historical readings.</p> 
 * <p>Copyright: Copyright (c) 2007</p>
 * <p>Company: Helios Development Group</p>
 * @author Whitehead
 * @version $Revision$
 */
@JMXManagedObject (annotated=true, declared=false)
public class HistoryBufferingCachedResultSet extends CachedResultSetImpl {

	/**	 */
	private static final long serialVersionUID = -3935593829668285480L;
	/** The number of historical readings to buffer */
	protected int historySize = 1;
	/** The set to store the history */
	protected TreeMap<Object, String[][]> history = new TreeMap<Object, String[][]>();
	/** The history table header */
	protected String[] header = null;
	
	
	/**
	 * Create a new HistoryBufferingCachedResultSetHistoryBufferingCachedResultSet
	 * @param name The name of the cached result set.
	 */
	public HistoryBufferingCachedResultSet(String name) {
		super(name);		
	}
	
	/**
	 * Sets the result set and buffers history.
	 * @param resultSet the resultSet to set
	 */
	@Override
	public synchronized void setResultSet(String[][] resultSet) {		
		super.setResultSet(resultSet);
		if(resultSet==null || resultSet.length<1 || resultSet[0]==null || resultSet[0].length < 1) return;
		String dt = new Date().toString();
	
		if(history.size()==historySize) {
			String key = history.firstKey().toString();
			history.remove(key);				
		}
		if(history.size() < 1 && header==null) {
			header = new String[resultSet[0].length+1];
			header[0] = "Timestamp";
			for(int i = 0; i < resultSet[0].length; i++) {
				header[i+1] = resultSet[0][i];
			}
		}
		String[][] nResultSet = new String[resultSet.length-1][];
		for(int i = 1; i < resultSet.length; i++) {
			nResultSet[i-1] = resultSet[i];
		}			
		history.put(dt, nResultSet);
		
	}
	
	/**
	 * Generates and returns the cached result set history buffer.
	 * @return An array of cached result sets.
	 */
	@JMXAttribute (introspect=true, name="getHistoryName", description="getHistoryDescription")
	public synchronized Map getHistory() {
		return history;
	}
	
	/**
	 * Returns the number of data rows in history
	 * @return
	 */
	
	public int getHistoryRowCount() {
		int rowCount = 0;
		synchronized(history) {
			for(String[][] rs: history.values()) {
				rowCount += rs.length;
			}
		}
		return rowCount;
	}
	
	/**
	 * Renders the history buffer into an HTML table.
	 * @return
	 */
	@JMXAttribute(name="getHistoryRender", description="getHistoryRenderDescription", introspect=true)
	public synchronized String getRenderHistory() {
		StringBuilder buff = new StringBuilder("<table border=\"1\">");
		int totalDataRowCount = getHistoryRowCount();
		if(totalDataRowCount<1) {
			buff.append("</table>");			
			return buff.toString();			
		}
		String[][] hResultSet = new String[totalDataRowCount][this.columnCount+1];
		if(header!=null && header.length > 0) {
			buff.append("<tr>");
			for(String headerElement: header) {
				buff.append("<th>").append(headerElement).append("</th>");
			}
			buff.append("</tr>");
		}
		synchronized(history) {
			int row = 0;			
			for(Entry<Object, String[][]> entry: history.entrySet()) {
				String[][] sRes = entry.getValue();
				for(int sResRow = 0; sResRow < sRes.length; sResRow++) {
					hResultSet[row][0] = entry.getKey().toString();
					for(int sResCol = 0; sResCol < sRes[sResRow].length; sResCol++) {
						hResultSet[row][sResCol+1] = sRes[sResRow][sResCol]; 
					}
					row++;
				}									
			}
			
			if(hResultSet==null || hResultSet.length < 1 || hResultSet[0].length < 1) {
				buff.append("</table>");
				return buff.toString();
			}
			// Generate Details
			for(int i = 0; i < hResultSet.length; i++) {
				buff.append("<tr>");
				for(int x = 0; x < hResultSet[i].length; x++) {
					buff.append("<td>").append(hResultSet[i][x]).append("</td>");
				}
				buff.append("</tr>");
			}
		}
		buff.append("</table>");
		
		return buff.toString();
	}	
	
	/**
	 * @return the name of the historical buffer
	 */
	public String getHistoryName() {
		return name + "History";
	}
	
	/**
	 * @return the name of the historical render op
	 */
	public String getHistoryRender() {
		return "render" + name + "History";
	}
	
	/**
	 * @return the name of the historical render op description
	 */
	public String getHistoryRenderDescription() {
		return "Renders the " + name + " history";
	}	
	
	
	/**
	 * @return the description of the historical buffer
	 */
	public String getHistoryDescription() {
		return "The last " + historySize + " Cached Result Sets For " + name;
	}

	/**
	 * @return the max history Size
	 */
	@JMXAttribute(name="CurrentHistorySize", description="The current size of the history buffer")
	public int getMaxHistorySize() {
		return history.size();
		
	}
	
	/**
	 * @return the current historySize
	 */
	@JMXAttribute(name="HistorySize", description="The maximum size of the history buffer")
	public int getHistorySize() {
		return historySize;
	}
	

	/**
	 * @param historySize the historySize to set
	 */
	public void setHistorySize(int historySize) {
		this.historySize = historySize;
	}	
	
	@JMXAttribute (introspect=true, name="getHistoryLastChangeTimeName", description="getHistoryLastChangeTimeDescription")
	public long getHistoryLastChangeTime() {
		return lastChangeTime;
	}
	
	public String getHistoryLastChangeTimeName() {
		return name + "HistoryLastChangeTime";
	}
	
	public String getHistoryLastChangeTimeDescription() {
		return "Time Stamp of Last Change Time for " + name + " History";
	}		
	
	
	
}


/*
import javax.management.*;
import javax.naming.*;

class RMIAdapter {
	static Map connections = new HashMap();
	public static void getInstance(String url, Closure closure) {
		if(!connections.containsKey(url)) { 
			Properties p = new Properties();
			p.put(Context.PROVIDER_URL, url);
			p.put(Context.INITIAL_CONTEXT_FACTORY, "org.jnp.interfaces.NamingContextFactory");
			p.put("jnp.disableDiscovery", "true");
			def ctx = new InitialContext(p);
			MBeanServerConnection rmi = (MBeanServerConnection)ctx.lookup("/jmx/rmi/RMIAdaptor");
			ctx.close();
			connections.put(url, rmi);
		}
		MBeanServerConnection connection = (MBeanServerConnection)connections.get(url);
		closure.call(connection);
	}	
}

RMIAdapter.getInstance("localhost:1099", {
	String hostName = (String)it.getAttribute(new ObjectName("jboss.system:type=ServerInfo"), "HostName");
	println "Host:${hostName}";
	ObjectName on = new ObjectName("com.heliosapm.wiex.monitoring:type=CachedResultSets,name=DC1");
	String[][][] history = (String[][][])it.getAttribute(on, "UserLoginsHistory");
	println "History:\n${history}";
	history.each() { period ->
		println "Period ${period}"
		period.each() { series ->			
			series.each() {
				//println "\t${series[0]}, ${series[1]}";
			}
		}
	}	

});
/*
RMIAdapter.getInstance("localhost:1099", {
	String hostName = (String)it.getAttribute(new ObjectName("jboss.system:type=ServerInfo"), "HostName");
	println "Host:${hostName}";
	ObjectName on = new ObjectName("com.heliosapm.wiex.server.server.rendering:service=RendererManager");
	byte[] content = it.invoke(on, "renderServerLoginsPie", (Object[])[], (String[])[]);
	println "Acquired ${content.length} bytes of content"
	File f = new File("C:/temp/emps.jpg");
	OutputStream os = f.newOutputStream();
	os.write(content);
	os.flush();
	os.close();
});

*/
